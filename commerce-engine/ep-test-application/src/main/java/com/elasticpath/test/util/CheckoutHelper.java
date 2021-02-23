/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.test.util;

import static com.elasticpath.commons.constants.ContextIdNames.SHIPPING_OPTION_SERVICE;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.constants.EpShippingContextIdNames;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.shipping.Region;
import com.elasticpath.domain.shipping.ShippingRegion;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shipping.ShippingOptionResult;
import com.elasticpath.service.shipping.ShippingOptionService;
import com.elasticpath.service.shipping.ShippingServiceLevelService;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutService;
import com.elasticpath.service.shoppingcart.actions.impl.PostCaptureCheckoutActionContextImpl;
import com.elasticpath.settings.refreshstrategy.impl.IntervalRefreshStrategyImpl;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;
import com.elasticpath.test.persister.PaymentInstrumentPersister;
import com.elasticpath.test.persister.StoreTestPersister;
import com.elasticpath.test.persister.TestApplicationContext;

/**
 * A helper to with all util methods for checkout.
 */
public class CheckoutHelper {

	private static final String HOLD_ALL_ORDERS_SETTING_PATH = "COMMERCE/SYSTEM/ONHOLD/holdAllOrdersForStore";
	private static final String HOLD_ALL_ORDERS_RESOLVE_PERMISSION_PATH = "COMMERCE/SYSTEM/ONHOLD/holdAllOrdersForStoreResolvePermission";
	private static final String RESOLVE_GENERIC_HOLD_PERMISSION = "RESOLVE_GENERIC_HOLD";
	private static final int FIRST_INDEX = 0;

	private final CheckoutService checkoutService;

	private final ShippingOptionService shippingOptionService;

	private final PricingSnapshotService pricingSnapshotService;

	private final TaxSnapshotService taxSnapshotService;

	private final StoreTestPersister storeTestPersister;

	private final ShippingServiceLevelService shippingServiceLevelService;

	private final PaymentInstrumentPersister paymentInstrumentPersister;

	private final PostCaptureCheckoutService postCaptureCheckoutService;

	private final EventOriginatorHelper eventOriginatorHelper;

	private final OrderService orderService;

	private final TestApplicationContext tac;

	/**
	 * Constructor.
	 *
	 * @param tac {@link TestApplicationContext}.
	 */
	public CheckoutHelper(final TestApplicationContext tac) {
		this.tac = tac;
		checkoutService = tac.getBeanFactory().getSingletonBean(ContextIdNames.CHECKOUT_SERVICE, CheckoutService.class);
		shippingOptionService = tac.getBeanFactory().getSingletonBean(SHIPPING_OPTION_SERVICE, ShippingOptionService.class);
		pricingSnapshotService = tac.getBeanFactory().getSingletonBean(ContextIdNames.PRICING_SNAPSHOT_SERVICE, PricingSnapshotService.class);
		taxSnapshotService = tac.getBeanFactory().getSingletonBean(ContextIdNames.TAX_SNAPSHOT_SERVICE, TaxSnapshotService.class);
		storeTestPersister = tac.getPersistersFactory().getStoreTestPersister();
		shippingServiceLevelService = tac.getBeanFactory().getSingletonBean(EpShippingContextIdNames.SHIPPING_SERVICE_LEVEL_SERVICE,
				ShippingServiceLevelService.class);
		paymentInstrumentPersister = tac.getPersistersFactory().getPaymentInstrumentPersister();
		postCaptureCheckoutService = tac.getBeanFactory().getSingletonBean(ContextIdNames.POST_CAPTURE_CHECKOUT_SERVICE,
				PostCaptureCheckoutService.class);
		orderService = tac.getBeanFactory().getSingletonBean(ContextIdNames.ORDER_SERVICE, OrderService.class);
		eventOriginatorHelper = tac.getBeanFactory().getSingletonBean(ContextIdNames.EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class);
	}

	/**
	 * Enrich a shopping cart with default payment info and shipping info.
	 *
	 * @param shoppingCart {@link ShoppingCart}.
	 */
	public void enrichShoppingCartForCheckout(final ShoppingCart shoppingCart) {
		paymentInstrumentPersister.persistPaymentInstrument(shoppingCart);
		final ShippingOption defaultShippingOption = getDefaultShippingOptionFromCart(shoppingCart);
		CustomerAddress address = createCustomerAddressMatchingShippingServiceLevel(defaultShippingOption);
		shoppingCart.setShippingAddress(address);
		shoppingCart.setSelectedShippingOption(defaultShippingOption);
		shoppingCart.setBillingAddress(address);
	}

	/**
	 * Checkout a shopping cart with default billing info, payment info, and shipping info.
	 *
	 * @param shoppingCart    {@link com.elasticpath.domain.shoppingcart.ShoppingCart}.
	 * @param customerSession {@link com.elasticpath.domain.customer.CustomerSession}
	 * @return {@link CheckoutResults}.
	 */
	public CheckoutResults checkoutWithDefaultInfo(final ShoppingCart shoppingCart, final CustomerSession customerSession) {
		enrichShoppingCartForCheckout(shoppingCart);

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		CheckoutResults checkoutResults = checkoutCartWithoutHolds(shoppingCart, taxSnapshot, customerSession, false);
		checkoutResults.setOrder(finalizeOrder(checkoutResults.getOrder()));
		return checkoutResults;
	}

	/**
	 * Checkout a shopping cart with default billing info, payment info, and shipping info.
	 *
	 * @param customerSession {@link CustomerSession}.
	 * @return {@link CheckoutResults}.
	 */
	public CheckoutResults checkoutWithDefaultInfo(final CustomerSession customerSession) {
		return checkoutWithDefaultInfo(customerSession.getShopper().getCurrentShoppingCart(), customerSession);
	}

	/**
	 * Checkout a shopping cart with default payment info but override selected shipping option
	 *
	 * @param customerSession    {@link CustomerSession}.
	 * @param shippingOptionName service option name (not code) specified
	 * @return {@link CheckoutResults}.
	 */
	public CheckoutResults checkoutWithDefaultInfoOverrideShipping(final CustomerSession customerSession, final String shippingOptionName) {
		final ShoppingCart shoppingCart = customerSession.getShopper().getCurrentShoppingCart();
		paymentInstrumentPersister.persistPaymentInstrument(shoppingCart);

		List<ShippingOption> defaultShippingOptions = getShippingOptionsFromCart(shoppingCart);
		boolean found = false;
		for (ShippingOption defaultShippingOption : defaultShippingOptions) {
			if (StringUtils.equals(shippingOptionName, defaultShippingOption.getDisplayName(shoppingCart.getShopper().getLocale()).orElse(null))) {
				CustomerAddress address = createCustomerAddressMatchingShippingServiceLevel(defaultShippingOption);
				shoppingCart.setBillingAddress(address);
				shoppingCart.setShippingAddress(address);
				shoppingCart.setSelectedShippingOption(defaultShippingOption);
				found = true;
				break;
			}
		}
		if (!found) {
			throw new EpSystemException("Chosen ShippingOption not available in for store.");
		}

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		CheckoutResults checkoutResults = checkoutCartWithoutHolds(shoppingCart, taxSnapshot, customerSession, false);
		checkoutResults.setOrder(finalizeOrder(checkoutResults.getOrder()));
		return checkoutResults;
	}

	private List<ShippingOption> getShippingOptionsFromCart(final ShoppingCart storeShoppingCart) {
		final ShippingOptionResult result = shippingOptionService.getAllShippingOptions(
				storeShoppingCart.getStore().getCode(),
				storeShoppingCart.getShopper().getLocale());

		final String errorMessage = format("Unable to get available shipping options for the given cart with guid '%s'.",
				storeShoppingCart.getGuid());

		result.throwExceptionIfUnsuccessful(
				errorMessage,
				singletonList(
						new StructuredErrorMessage(
								"shippingoptions.unavailable",
								errorMessage,
								ImmutableMap.of(
										"cart-id", storeShoppingCart.getGuid())
						)
				));

		return result.getAvailableShippingOptions();
	}

	private ShippingOption getDefaultShippingOptionFromCart(final ShoppingCart storeShoppingCart) {
		return getShippingOptionsFromCart(storeShoppingCart).get(FIRST_INDEX);
	}

	private CustomerAddress defaultCustomerAddress() {
		return storeTestPersister.createCustomerAddress("Ou", "Owen", "street1", "street2", "Vancouver", "CA", "BC", "zip", "phone");
	}

	private CustomerAddress createCustomerAddressMatchingShippingServiceLevel(final ShippingOption shippingOption) {
		final List<ShippingServiceLevel> shippingServiceLevels = shippingServiceLevelService.findAll();

		final ShippingServiceLevel matchedShippingServiceLevel = shippingServiceLevels.stream().filter(shippingServiceLevel ->
				ObjectUtils.equals(shippingServiceLevel.getCode(), shippingOption.getCode()))
				.findFirst().orElse(null);
		return createCustomerAddressMatchingShippingServiceLevel(matchedShippingServiceLevel);
	}

	private CustomerAddress createCustomerAddressMatchingShippingServiceLevel(final ShippingServiceLevel shippingServiceLevel) {
		final ShippingRegion shippingRegion = shippingServiceLevel.getShippingRegion();
		final Map<String, Region> regionMap = shippingRegion.getRegionMap();
		final Collection<Region> regions = regionMap.values();
		final Region firstRegion = regions.iterator().next();
		final String countryCodeFromFirstRegion = firstRegion.getCountryCode();
		final String firstSubCountryFromFirstRegion = firstRegion.getSubCountryCodeList().get(0);
		final CustomerAddress customerAddressMatchingShippingServiceLevel = defaultCustomerAddress();
		customerAddressMatchingShippingServiceLevel.setCountry(countryCodeFromFirstRegion);
		customerAddressMatchingShippingServiceLevel.setSubCountry(firstSubCountryFromFirstRegion);
		return customerAddressMatchingShippingServiceLevel;
	}

	/**
	 * Disables order holds and checkout the order but does not finalize, which means the order will not be released.
	 *
	 * @param shoppingCart - the cart to checkout
	 * @param taxSnapshot - the tax snapshot for the cart
	 * @param customerSession - the customers current session
	 * @param throwExceptions - if any encountered exceptions should be rethrown
	 * @return the CheckoutResults container
	 */
	public CheckoutResults checkoutCartWithoutHolds(ShoppingCart shoppingCart, ShoppingCartTaxSnapshot taxSnapshot,
			CustomerSession customerSession, boolean throwExceptions) {

		CheckoutResults checkoutResult = checkoutService.checkout(
				shoppingCart, taxSnapshot, customerSession, throwExceptions);

		return checkoutResult;
	}

	/**
	 * Enables order holds and checkout the order but does not finalize, which means the order will not be released.
	 *
	 * @param shoppingCart - the cart to checkout
	 * @param taxSnapshot - the tax snapshot for the cart
	 * @param customerSession - the customers current session
	 * @param throwExceptions - if any encountered exceptions should be rethrown
	 * @return the CheckoutResults container
	 */
	public CheckoutResults checkoutCartWithHold(ShoppingCart shoppingCart, ShoppingCartTaxSnapshot taxSnapshot,
			CustomerSession customerSession, boolean throwExceptions) {

		enableOrderHoldStrategies(shoppingCart.getStore().getCode());
		CheckoutResults checkoutResult = checkoutService.checkout(
				shoppingCart, taxSnapshot, customerSession, throwExceptions);

		return checkoutResult;
	}

	/**
	 * Runs the post capture checkout actions so that the order can be released and finalized.
	 *
	 * @param submittedOrder - the order that has been submitted and accepted
	 * @return the finalized order
	 */
	public Order finalizeOrder(Order submittedOrder) {
		return postCaptureCheckout(submittedOrder);
	}

	/**
	 * A convenience method that obtains the order from the CheckoutResults and invokes {@link #finalizeOrder(Order)}
	 * @param results the checkout results used to obtain the order
	 * @return the finalized order
	 */
	public Order finalizeOrder(CheckoutResults results) {
		return finalizeOrder(results.getOrder());
	}

	/**
	 * Runs the checkout and post capture checkout services against the order and bypasses all order hold processing.
	 *
	 * @param shoppingCart - the cart to checkout
	 * @param taxSnapshot - the tax snapshot for the cart
	 * @param customerSession - the customers current session
	 *
	 * @return the finalized order
	 */
	public Order checkoutCartAndFinalizeOrderWithoutHolds(ShoppingCart shoppingCart, ShoppingCartTaxSnapshot taxSnapshot,
			CustomerSession customerSession) {
		return checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, customerSession, false);
	}

	/**
	 * Runs the checkout and post capture checkout services against the order.
	 *
	 * @param shoppingCart - the cart to checkout
	 * @param taxSnapshot - the tax snapshot for the cart
	 * @param customerSession - the customers current session
	 * @param throwExceptions - if any encountered exceptions should be rethrown
	 *
	 * @return the finalized order
	 */
	public Order checkoutCartAndFinalizeOrder(ShoppingCart shoppingCart, ShoppingCartTaxSnapshot taxSnapshot,
			CustomerSession customerSession, boolean throwExceptions) {

		CheckoutResults checkoutResult = checkoutService.checkout(
				shoppingCart, taxSnapshot, customerSession, throwExceptions);
		Order postCaptureOrder =  orderService.findOrderByOrderNumber(checkoutResult.getOrder().getOrderNumber());
		postCaptureOrder.setModifiedBy(eventOriginatorHelper.getSystemOriginator());
		return postCaptureCheckout(postCaptureOrder);
	}

	/**
	 * Runs the checkout and post capture checkout services against the order and bypasses all order hold processing.
	 *
	 * @param shoppingCart - the cart to checkout
	 * @param taxSnapshot - the tax snapshot for the cart
	 * @param customerSession - the customers current session
	 * @param throwExceptions - if any encountered exceptions should be rethrown
	 *
	 * @return the finalized order
	 */
	public Order checkoutCartAndFinalizeOrderWithoutHolds(ShoppingCart shoppingCart, ShoppingCartTaxSnapshot taxSnapshot,
			CustomerSession customerSession, boolean throwExceptions) {

		return checkoutCartAndFinalizeOrder(shoppingCart, taxSnapshot,
				customerSession, throwExceptions);
	}

	/**
	 * Builds a PostCaptureCheckoutActionContext and invokes PostCaptureCheckoutService.checkout() on the order.
	 * @param order the order to invoke post capture actions against
	 * @return the resulting order
	 */
	public Order postCaptureCheckout(Order order) {
		PostCaptureCheckoutActionContext context = new PostCaptureCheckoutActionContextImpl(order);
		postCaptureCheckoutService.completeCheckout(context);
		Order postCaptureOrder =  orderService.findOrderByOrderNumber(order.getOrderNumber());
		postCaptureOrder.setModifiedBy(eventOriginatorHelper.getCustomerOriginator(order.getCustomer()));
		return postCaptureOrder;
	}

	/**
	 * Persists a setting that disables all order hold strategies.
	 *
	 * @param storeCode the store to disable order hold strategies within
	 */
	public void disableOrderHoldStrategies(final String storeCode) {
		setOrderHoldStrategy(storeCode, Boolean.FALSE);
	}

	/**
	 * Persists a setting that enables all order hold strategies.
	 *
	 * @param storeCode the store to disable order hold strategies within
	 */
	public void enableOrderHoldStrategies(final String storeCode) {
		setOrderHoldStrategy(storeCode, Boolean.TRUE);
		setOrderHoldResolvePermission(storeCode, RESOLVE_GENERIC_HOLD_PERMISSION);
		IntervalRefreshStrategyImpl.clearCache();
	}

	protected void setOrderHoldResolvePermission(final String storeCode, final String permission) {
		try {
			tac.getPersistersFactory().getSettingsTestPersister().persistSettings(
					HOLD_ALL_ORDERS_RESOLVE_PERMISSION_PATH,
					"RESOLVE_GENERIC_HOLD",
					"String",
					-1,
					storeCode,
					permission,
					"permission to resolve generic holds on orders for a store"
			);
		} catch (EpServiceException e) {
			//this usually means the setting has already been persisted - try to update instead
			tac.getPersistersFactory().getSettingsTestPersister().updateSettingValue(
					HOLD_ALL_ORDERS_RESOLVE_PERMISSION_PATH,
					storeCode,
					permission
			);
		}
	}

	protected void setOrderHoldStrategy(final String storeCode, final Boolean enableHoldStrategies) {
		try {
			tac.getPersistersFactory().getSettingsTestPersister().persistSettings(
					HOLD_ALL_ORDERS_SETTING_PATH,
					"false",
					"Boolean",
					-1,
					storeCode,
					enableHoldStrategies.toString(),
					"Hold all orders for a store"
			);
		} catch (EpServiceException e) {
			//this usually means the setting has already been persisted - try to update instead
			tac.getPersistersFactory().getSettingsTestPersister().updateSettingValue(
					HOLD_ALL_ORDERS_SETTING_PATH,
					storeCode,
					enableHoldStrategies.toString()
			);
		}
	}
}
