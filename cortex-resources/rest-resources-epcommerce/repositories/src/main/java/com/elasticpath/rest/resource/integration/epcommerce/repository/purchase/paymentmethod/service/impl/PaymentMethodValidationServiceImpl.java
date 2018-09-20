/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.paymentmethod.service.impl;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.money.Money;
import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentmethods.PaymentmethodInfoIdentifier;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.calc.TotalsCalculator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.paymentmethod.service.PaymentMethodValidationService;
import com.elasticpath.rest.schema.StructuredMessageTypes;
import com.elasticpath.rest.util.math.NumberUtil;

/**
 * Implementation of the PaymentMethodValidationService.
 * FIXME this MUST move to PaymentMethod resource after PaymentMethod resource is converted to Helix
 */
@Component
public class PaymentMethodValidationServiceImpl implements PaymentMethodValidationService {

	private static final String MESSAGE_NEED_PAYMENT_METHOD = "A payment method must be provided before you can complete the purchase.";

	private CartOrderRepository cartOrderRepository;
	private TotalsCalculator totalsCalculator;

	@Override
	public Observable<LinkedMessage<PaymentmethodInfoIdentifier>> validatePaymentForOrder(final OrderIdentifier orderIdentifier) {
		return isMissingPaymentMethod(orderIdentifier)
				.flatMapObservable(noPaymentMethod -> getLinkedMessage(noPaymentMethod, orderIdentifier));
	}

	/**
	 * Check if order doesn't have payment method.
	 *
	 * @param orderIdentifier orderIdentifier
	 * @return true if order doesn't have payment method
	 */
	protected Single<Boolean> isMissingPaymentMethod(final OrderIdentifier orderIdentifier) {
		String storeCode = orderIdentifier.getScope().getValue();
		String cartOrderGuid = orderIdentifier.getOrderId().getValue();

		return isPaymentRequired(storeCode, cartOrderGuid)
				.flatMap(paymentIsRequired -> {
					if (paymentIsRequired) {
						return cartOrderRepository.findByGuidAsSingle(storeCode, cartOrderGuid)
								.map(cartOrder -> cartOrder.getPaymentMethod() == null);
					}
					//Payment is not required so cannot be missing payment method
					return Single.just(false);
				});
	}

	/**
	 * Checks if paymentMethod is required for the following cart order.
	 *
	 * @param storeCode scope
	 * @param cartOrderGuid cart order GUID
	 * @return true if payment is required
	 */
	protected Single<Boolean> isPaymentRequired(final String storeCode, final String cartOrderGuid) {
		return totalsCalculator.calculateSubTotalForCartOrderSingle(storeCode, cartOrderGuid)
				.map(this::isCartOrderTotalPositive);
	}

	private boolean isCartOrderTotalPositive(final Money money) {
		return NumberUtil.isPositive(money.getAmount());
	}

	/**
	 * Get linked message if order doesn't have payment method specified.
	 *
	 * @param paymentMethodNotSpecified true if order doesn't have payment method
	 * @param orderIdentifier   orderIdentifier
	 * @return linked message for payment method
	 */
	protected Observable<LinkedMessage<PaymentmethodInfoIdentifier>> getLinkedMessage(
			final Boolean paymentMethodNotSpecified, final OrderIdentifier orderIdentifier) {
		if (paymentMethodNotSpecified) {
			return Observable.just(createLinkedMessage(PaymentmethodInfoIdentifier.builder()
					.withOrder(orderIdentifier)
					.build()));
		}
		return Observable.empty();
	}

	/**
	 * Create linked message.
	 *
	 * @param paymentmethodInfoIdentifier paymentmethodInfoIdentifier
	 * @return message with link to payment method info identifier
	 */
	protected LinkedMessage<PaymentmethodInfoIdentifier> createLinkedMessage(final PaymentmethodInfoIdentifier paymentmethodInfoIdentifier) {
		return LinkedMessage.<PaymentmethodInfoIdentifier>builder()
				.withType(StructuredMessageTypes.NEEDINFO)
				.withId(StructuredErrorMessageIdConstants.NEED_PAYMENT_METHOD)
				.withDebugMessage(MESSAGE_NEED_PAYMENT_METHOD)
				.withLinkedIdentifier(paymentmethodInfoIdentifier)
				.build();
	}

	@Reference
	public void setCartOrderRepository(final CartOrderRepository cartOrderRepository) {
		this.cartOrderRepository = cartOrderRepository;
	}

	@Reference
	public void setTotalsCalculator(final TotalsCalculator totalsCalculator) {
		this.totalsCalculator = totalsCalculator;
	}
}
