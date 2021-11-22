/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.shipping.ShippingOptionResult;
import com.elasticpath.service.shipping.ShippingOptionService;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.actions.CheckoutAction;
import com.elasticpath.service.shoppingcart.actions.FinalizeCheckoutAction;
import com.elasticpath.service.shoppingcart.actions.FinalizeCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutService;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContextImpl;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;
import com.elasticpath.service.shoppingcart.actions.impl.FinalizeCheckoutActionContextImpl;
import com.elasticpath.service.shoppingcart.actions.impl.PostCaptureCheckoutActionContextImpl;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;

/**
 * Provides a service to execute a shopping cart checkout.
 */
public class CheckoutServiceImpl implements CheckoutService {

	private static final Logger LOG = LogManager.getLogger(CheckoutServiceImpl.class);

	private List<CheckoutAction> setupActionList = Collections.emptyList();

	private List<ReversibleCheckoutAction> reversibleActionList = Collections.emptyList();

	private List<FinalizeCheckoutAction> finalizeActionList = Collections.emptyList();

	private ShippingOptionService shippingOptionService;

	private BeanFactory beanFactory;

	private CartOrderService cartOrderService;

	private PostCaptureCheckoutService postCaptureCheckoutService;

	@Override
	public void retrieveShippingOption(final ShoppingCart shoppingCart) {
		if (shoppingCart.requiresShipping()) {

			// find available shipping option by shipping address and store.
			final ShippingOptionResult shippingOptionResult = getShippingOptionService().getShippingOptions(shoppingCart);

			if (shippingOptionResult.isSuccessful()) {
				retrieveShippingOption(shoppingCart, shippingOptionResult);
			}
		} else {
			shoppingCart.clearSelectedShippingOption();
		}
	}

	/**
	 * Retrieves the available shipping options from the successful {@link ShippingOptionResult} given, and if the current one selected is not
	 * valid, it gets the default one (per {@link ShippingOptionService#getDefaultShippingOption(List)} and selects that one on the shopping cart.
	 *
	 * @param shoppingCart         the shopping cart to inspect and update.
	 * @param shippingOptionResult the successful {@link ShippingOptionResult}.
	 */
	protected void retrieveShippingOption(final ShoppingCart shoppingCart, final ShippingOptionResult shippingOptionResult) {
		final List<ShippingOption> availableShippingOptions = shippingOptionResult.getAvailableShippingOptions();

		if (CollectionUtils.isNotEmpty(availableShippingOptions)) {
			final String selectedShippingOptionCode = shoppingCart.getSelectedShippingOption().map(ShippingOption::getCode).orElse(null);
			useDefaultIfSelectedShippingOptionIsInvalid(shoppingCart, selectedShippingOptionCode, availableShippingOptions);
		} else {
			shoppingCart.clearSelectedShippingOption();
		}
	}

	/**
	 * Uses default shipping option if selected shipping option is invalid.
	 *
	 * @param shoppingCart               the shopping cart
	 * @param selectedShippingOptionCode the selected shipping option code
	 * @param availableShippingOptions   available shipping options
	 */
	protected void useDefaultIfSelectedShippingOptionIsInvalid(final ShoppingCart shoppingCart,
															   final String selectedShippingOptionCode,
															   final List<ShippingOption> availableShippingOptions) {
		if (!isValidShippingOptionCode(availableShippingOptions, selectedShippingOptionCode)) {
			final Optional<ShippingOption> defaultShippingOption = shippingOptionService.getDefaultShippingOption(availableShippingOptions);
			shoppingCart.setSelectedShippingOption(defaultShippingOption.orElse(null));
		}
	}

	/**
	 * Returns whether the the given shipping option code is contained in the list of {@link ShippingOption} objects.
	 *
	 * @param validShippingOptions the list of {@link ShippingOption} objects to search.
	 * @param shippingOptionCode the code to search for.
	 * @return {@code true} if any of the {@link ShippingOption} object's code match; {@code false} otherwise.
	 */
	protected boolean isValidShippingOptionCode(final List<ShippingOption> validShippingOptions, final String shippingOptionCode) {
		if (StringUtils.isEmpty(shippingOptionCode)) {
			return false;
		}
		return validShippingOptions.stream().anyMatch(shippingOption -> shippingOption.getCode().equals(shippingOptionCode));
	}

	@Override
	public CheckoutResults checkout(final ShoppingCart shoppingCart, final ShoppingCartTaxSnapshot pricingSnapshot,
									final CustomerSession customerSession) {
		return checkout(shoppingCart, pricingSnapshot, customerSession, true);
	}

	@Override
	public CheckoutResults checkout(
			final ShoppingCart shoppingCart,
			final ShoppingCartTaxSnapshot pricingSnapshot,
			final CustomerSession customerSession,
			final boolean throwExceptions) {
		final CheckoutResults checkoutResults = beanFactory.getPrototypeBean(ContextIdNames.CHECKOUT_RESULTS, CheckoutResults.class);
		if (!shoppingCart.isExchangeOrderShoppingCart()) {
			try {
				this.checkoutInternal(shoppingCart, pricingSnapshot, customerSession, false, false, null, checkoutResults);
			} catch (final RuntimeException exception) {
				if (throwExceptions || checkoutResults.getOrder() == null) {
					throw exception;
				}
				checkoutResults.setOrderFailed(true);
				checkoutResults.setFailureCause(exception);
			}
		}
		return checkoutResults;
	}

	@Override
	public void checkoutExchangeOrder(final OrderReturn exchange, final boolean awaitExchangeCompletion) {
		final CheckoutResults checkoutResults = beanFactory.getPrototypeBean(ContextIdNames.CHECKOUT_RESULTS, CheckoutResults.class);
		final ShoppingCart shoppingCart = exchange.getExchangeShoppingCart();
		if (shoppingCart.isExchangeOrderShoppingCart()) {
			final boolean isOrderExchange = true;
			this.checkoutInternal(shoppingCart, exchange.getExchangePricingSnapshot(), exchange.getExchangeCustomerSession(),
					isOrderExchange, awaitExchangeCompletion, exchange, checkoutResults);
			final PostCaptureCheckoutActionContext postCaptureCheckoutActionContext =
					new PostCaptureCheckoutActionContextImpl(checkoutResults.getOrder());
			postCaptureCheckoutService.completeCheckout(postCaptureCheckoutActionContext);
		}
	}

	/**
	 * Internal checkout method performs the actual checkout operations.
	 *
	 * @param shoppingCart            the {@link com.elasticpath.domain.shoppingcart.ShoppingCart}
	 * @param pricingSnapshot         the {@link com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot}
	 * @param customerSession         the {@link com.elasticpath.domain.customer.CustomerSession}
	 * @param isOrderExchange         whether this order is part of an exchange
	 * @param awaitExchangeCompletion whether the order created via this checkout must wait for completion of a physical
	 *                                exchange before being filled
	 * @param exchange                the OrderReturn linked to the order that is to be created as a result of this checkout
	 * @param checkoutResults         an empty {@code CheckoutResults} to be populated with the results of the checkout
	 */
	//CHECKSTYLE:OFF
	protected void checkoutInternal(final ShoppingCart shoppingCart,
									final ShoppingCartTaxSnapshot pricingSnapshot,
									final CustomerSession customerSession,
									final boolean isOrderExchange,
									final boolean awaitExchangeCompletion,
									final OrderReturn exchange,
									final CheckoutResults checkoutResults) {
		//CHECKSTYLE:ON
		final PreCaptureCheckoutActionContext actionContext = createActionContext(
				shoppingCart, pricingSnapshot, customerSession, isOrderExchange,
				awaitExchangeCompletion, exchange);

		// Keep track of ReversibleCheckoutAction objects so we can rollback if necessary.
		final List<ReversibleCheckoutAction> executedActions = new ArrayList<>();

		LOG.debug("Checkout process started.");

		// Execute setup actions
		for (final CheckoutAction action : setupActionList) {
			LOG.debug("Executing checkout action " + action.getClass().getName());
			action.execute(actionContext);
		}

		// Execute reversible actions
		try {
			for (final ReversibleCheckoutAction action : reversibleActionList) {
				executedActions.add(action);
				LOG.debug("Executing checkout action " + action.getClass().getName());
				action.execute(actionContext);
			}
		} catch (final EpSystemException e) {
			rollbackCheckout(executedActions, actionContext, e);
			throw e;
		} catch (final Exception e) {
			rollbackCheckout(executedActions, actionContext, e);
			throw new EpServiceException("Checkout failed.", e);
		} finally {
			checkoutResults.setOrder(actionContext.getOrder());
		}

		final FinalizeCheckoutActionContext finalizeActionContext = createFinalizeActionContext(actionContext);

		// Execute finalize actions
		for (final FinalizeCheckoutAction action : finalizeActionList) {
			LOG.debug("Executing checkout action " + action.getClass().getName());
			action.execute(finalizeActionContext);
		}

		checkoutResults.setEmailFailed(finalizeActionContext.isEmailFailed());

		/*TODO find a better way to pass completed order to ReturnAndExchangeService, as well as fit tests,
		  because the cart is empty at this point
		*/
		shoppingCart.setCompletedOrder(actionContext.getOrder());

		LOG.debug("Checkout process completed: Created order " + actionContext.getOrder().getOrderNumber());
	}

	/**
	 * Create an actionContext object for use by the SetupCheckoutAction and ReversibleCheckoutAction
	 * commands.
	 * <p/>
	 * Note: Protected to allow for extension.
	 *
	 * @param shoppingCart                the {@link com.elasticpath.domain.shoppingcart.ShoppingCart}
	 * @param shoppingCartPricingSnapshot the {@link com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot}
	 * @param customerSession             the {@link com.elasticpath.domain.customer.CustomerSession}
	 * @param isOrderExchange             is this an exchange?
	 * @param awaitExchangeCompletion     ???
	 * @param exchange                    the order return details
	 * @return a populated actionContext instance
	 */
	protected PreCaptureCheckoutActionContext createActionContext(
			final ShoppingCart shoppingCart,
			final ShoppingCartTaxSnapshot shoppingCartPricingSnapshot,
			final CustomerSession customerSession,
			final boolean isOrderExchange,
			final boolean awaitExchangeCompletion,
			final OrderReturn exchange) {
		return new PreCaptureCheckoutActionContextImpl(shoppingCart, shoppingCartPricingSnapshot, customerSession,
				isOrderExchange, awaitExchangeCompletion, exchange, this::findCartOrderInContext);
	}

	/**
	 * Create a finalizeActionContext object for use by the finalizeCheckoutAction commands.
	 *
	 * @param actionContext the original actionContext
	 * @return a populated finalizeActionContext instance
	 */
	protected FinalizeCheckoutActionContextImpl createFinalizeActionContext(
			final PreCaptureCheckoutActionContext actionContext) {
		return new FinalizeCheckoutActionContextImpl(actionContext);
	}

	private void rollbackCheckout(final List<ReversibleCheckoutAction> executedActions,
								  final PreCaptureCheckoutActionContext actionContext, final Exception exception) {
		if (exception instanceof PaymentsException) {
			LOG.debug("Payment processing error occurred during checkout", exception);
		} else {
			LOG.error("Error occurred during checkout", exception);
		}
		LOG.debug("Checkout rollback process started.");
		Lists.reverse(executedActions).forEach(action -> {
			try {
				LOG.debug("Executing checkout action rollback " + action.getClass().getName());
				action.rollback(actionContext);
			} catch (Exception ex) {
				LOG.error("Exception thrown during checkout rollback", ex);
			}
		});
		LOG.debug("Checkout rollback process completed.");
	}

	private CartOrder findCartOrderInContext(final Shopper shopper, final Order order) {
		if (shopper == null) {
			LOG.error("Shopper not found in checkout action context");
			return null;
		}

		if (order == null) {
			LOG.error("Order not found in checkout action context");
			return null;
		}

		final String storeCode = shopper.getStoreCode();
		final String cartOrderGuid = order.getCartOrderGuid();
		final CartOrder cartOrder = cartOrderService.findByStoreCodeAndGuid(storeCode, cartOrderGuid);
		if (cartOrder == null) {
			LOG.error("CartOrder not found by store code and guid: " + storeCode + " and " + cartOrderGuid);
		}

		return cartOrder;
	}

	protected ShippingOptionService getShippingOptionService() {
		return this.shippingOptionService;
	}

	public void setShippingOptionService(final ShippingOptionService shippingOptionService) {
		this.shippingOptionService = shippingOptionService;
	}

	protected List<CheckoutAction> getSetupActionList() {
		return setupActionList;
	}

	public void setSetupActionList(final List<CheckoutAction> setupActionList) {
		this.setupActionList = setupActionList;
	}

	protected List<ReversibleCheckoutAction> getReversibleActionList() {
		return reversibleActionList;
	}

	public void setReversibleActionList(final List<ReversibleCheckoutAction> reversibleActionList) {
		this.reversibleActionList = reversibleActionList;
	}

	protected List<FinalizeCheckoutAction> getFinalizeActionList() {
		return finalizeActionList;
	}

	public void setFinalizeActionList(final List<FinalizeCheckoutAction> finalizeActionList) {
		this.finalizeActionList = finalizeActionList;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void setCartOrderService(final CartOrderService cartOrderService) {
		this.cartOrderService = cartOrderService;
	}

	protected CartOrderService getCartOrderService() {
		return cartOrderService;
	}

	public void setPostCaptureCheckoutService(final PostCaptureCheckoutService postCaptureCheckoutService) {
		this.postCaptureCheckoutService = postCaptureCheckoutService;
	}

	protected PostCaptureCheckoutService getPostCaptureCheckoutService() {
		return postCaptureCheckoutService;
	}
}
