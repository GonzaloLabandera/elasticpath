/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl;

import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import io.reactivex.Single;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.OrderPayment;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.plugin.payment.PaymentType;
import com.elasticpath.plugin.payment.dto.PaymentMethod;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.purchases.CreatePurchaseFormIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseFormEntity;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.PricingSnapshotRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.PurchaseRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.service.CartHasItemsService;

/**
 * Purchases Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class PurchaseFormEntityRepositoryImpl<E extends PurchaseFormEntity, I extends PurchaseIdentifier>
		implements Repository<PurchaseFormEntity, PurchaseIdentifier> {

	/**
	 * Error message for non purchasable order.
	 */
	@VisibleForTesting
	static final String NOT_PURCHASABLE = "The products selected are not purchasable";

	private CartOrderRepository cartOrderRepository;

	private ResourceOperationContext resourceOperationContext;

	private PurchaseRepository purchaseRepository;

	private PricingSnapshotRepository pricingSnapshotRepository;

	private CustomerSessionRepository customerSessionRepository;

	private CartHasItemsService cartHasItemsService;

	@Override
	public Single<SubmitResult<PurchaseIdentifier>> submit(final PurchaseFormEntity purchaseFormEntity, final IdentifierPart<String> scope) {
		Optional<ResourceIdentifier> identifier = resourceOperationContext.getResourceIdentifier();
		if (!identifier.isPresent()) {
			return Single.error(ResourceOperationFailure.serverError(NOT_PURCHASABLE));
		}
		CreatePurchaseFormIdentifier purchaseFormIdentifier = (CreatePurchaseFormIdentifier) identifier.get();
		String orderId = purchaseFormIdentifier.getOrder().getOrderId().getValue();
		String store = scope.getValue();
		
		return isOrderPurchasable(scope, orderId)
				.flatMap(purchasable -> {
					if (purchasable) {
						return createPurchaseIdentifier(orderId, store)
								.map(purchaseIdentifier -> SubmitResult.<PurchaseIdentifier>builder()
										.withIdentifier(purchaseIdentifier)
										.withStatus(SubmitStatus.CREATED)
										.build());
					}
					return Single.error(ResourceOperationFailure.serverError(NOT_PURCHASABLE));
				});
	}

	private Single<Boolean> isOrderPurchasable(final IdentifierPart<String> scope, final String orderId) {
		OrderIdentifier identifier = OrderIdentifier.builder()
				.withOrderId(StringIdentifier.of(orderId))
				.withScope(scope)
				.build();

		return cartHasItemsService.isCartEmpty(identifier).map(isCartEmpty -> !isCartEmpty);
	}

	/**
	 * Construct purchase identifier.
	 *
	 * @param orderId order id
	 * @param store   store
	 * @return purchase identifier
	 */
	protected Single<PurchaseIdentifier> createPurchaseIdentifier(final String orderId, final String store) {
		return cartOrderRepository.findByGuidAsSingle(store, orderId)
				.flatMap(cartOrder -> createOrderPaymentFromCartOrder(cartOrder)
						.flatMap(orderPayment -> getShoppingCart(StringIdentifier.of(store), cartOrder)
								.flatMap(shoppingCart -> pricingSnapshotRepository.getShoppingCartTaxSnapshot(shoppingCart)
										.flatMap(taxSnapshot -> customerSessionRepository.findOrCreateCustomerSessionAsSingle()
												.flatMap(customerSession -> checkout(shoppingCart, taxSnapshot, customerSession, orderPayment))))))
				.map(purchaseId -> buildPurchaseIdentifier(store, purchaseId));
	}

	/**
	 * Build the PurchaseIdentifier.
	 *
	 * @param scope      scope
	 * @param purchaseId purchaseId
	 * @return purchase identifier
	 */
	protected PurchaseIdentifier buildPurchaseIdentifier(final String scope, final String purchaseId) {
		return PurchaseIdentifier.builder()
				.withPurchases(PurchasesIdentifier.builder()
						.withScope(StringIdentifier.of(scope))
						.build())
				.withPurchaseId(StringIdentifier.of(purchaseId))
				.build();
	}

	/**
	 * Get the shopping cart.
	 *
	 * @param scope     scope
	 * @param cartOrder cartOrder
	 * @return the shopping cart
	 */
	protected Single<ShoppingCart> getShoppingCart(final IdentifierPart<String> scope, final CartOrder cartOrder) {
		return cartOrderRepository.getEnrichedShoppingCartSingle(scope.getValue(), cartOrder);
	}

	/**
	 * Create the order payment from cart order.
	 *
	 * @param cartOrder cartOrder
	 * @return the order payment
	 */
	protected Single<OrderPayment> createOrderPaymentFromCartOrder(final CartOrder cartOrder) {
		PaymentMethod paymentMethod = cartOrder.getPaymentMethod();
		if (paymentMethod == null) {
			return createEmptyPaymentTokenOrderPaymentForZeroTotalPurchase();
		} else {
			return purchaseRepository.getOrderPaymentFromPaymentMethod(paymentMethod);
		}
	}

	/**
	 * Create an empty paymentToken order payment.
	 *
	 * @return empty paymentToken order payment
	 */
	protected Single<OrderPayment> createEmptyPaymentTokenOrderPaymentForZeroTotalPurchase() {
		return purchaseRepository.createNewOrderPaymentEntity()
				.map(this::setOrderPaymentAsPaymentToken);
	}

	/**
	 * Set the Order payment method as a payment token.
	 *
	 * @param orderPayment orderPayment
	 * @return orderPayment
	 */
	protected OrderPayment setOrderPaymentAsPaymentToken(final OrderPayment orderPayment) {
		orderPayment.setPaymentMethod(PaymentType.PAYMENT_TOKEN);
		return orderPayment;
	}

	/**
	 * Checkout.
	 *
	 * @param shoppingCart    shoppingCart
	 * @param taxSnapshot     taxSnapshot
	 * @param customerSession customerSession
	 * @param orderPayment    order payment
	 * @return purchase id
	 */
	protected Single<String> checkout(
			final ShoppingCart shoppingCart,
			final ShoppingCartTaxSnapshot taxSnapshot,
			final CustomerSession customerSession,
			final OrderPayment orderPayment) {

		return purchaseRepository.checkout(shoppingCart, taxSnapshot, customerSession, orderPayment)
				.flatMap(this::handleCheckoutResults);
	}

	/**
	 * Handles the checkout result. Returning the purchase id if the checkout succeeded and an error if checkout fails.
	 *
	 * @param checkoutResult checkoutResult
	 * @return the purchase id if the checkout succeeded and an error if checkout fails
	 */
	protected Single<String> handleCheckoutResults(final CheckoutResults checkoutResult) {
		if (checkoutResult.isOrderFailed()) {
			return handleCheckoutFailure(checkoutResult.getFailureCause());
		} else {
			return Single.just(checkoutResult.getOrder().getGuid());
		}
	}

	/**
	 * Handles the checkout process failure.
	 *
	 * @param checkoutException checkoutException
	 * @return the error wrapped in a Single
	 */
	protected Single<String> handleCheckoutFailure(final RuntimeException checkoutException) {
		if (checkoutException instanceof EpServiceException) {
			String message = ExceptionUtils.getRootCauseMessage(checkoutException);
			return Single.error(ResourceOperationFailure.stateFailure(message));
		} else {
			return Single.error(ResourceOperationFailure.stateFailure("The purchase failed: " + checkoutException.getMessage()));
		}
	}

	@Reference
	public void setCartOrderRepository(final CartOrderRepository cartOrderRepository) {
		this.cartOrderRepository = cartOrderRepository;
	}

	@Reference
	public void setPurchaseRepository(final PurchaseRepository purchaseRepository) {
		this.purchaseRepository = purchaseRepository;
	}

	@Reference
	public void setPricingSnapshotRepository(final PricingSnapshotRepository pricingSnapshotRepository) {
		this.pricingSnapshotRepository = pricingSnapshotRepository;
	}

	@Reference
	public void setCustomerSessionRepository(final CustomerSessionRepository customerSessionRepository) {
		this.customerSessionRepository = customerSessionRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setCartHasItemsService(final CartHasItemsService cartHasItemsService) {
		this.cartHasItemsService = cartHasItemsService;
	}


}
