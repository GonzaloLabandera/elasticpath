/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.integration.epcommerce.transform;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.elasticpath.domain.customer.CustomerCreditCard;
import com.elasticpath.domain.customer.PaymentToken;
import com.elasticpath.plugin.payment.dto.PaymentMethod;
import com.elasticpath.rest.definition.paymentmethods.CreditCardEntity;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.definition.paymenttokens.PaymentTokenEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.paymentmethods.PaymentMethodIdentifierResolver;
import com.elasticpath.rest.resource.transform.AbstractDomainTransformer;

/**
 * Transforms between {@link CustomerCreditCard} and {@link CreditCardEntity}, and vice versa.
 */
@Singleton
@Named("paymentMethodTransformer")
public class PaymentMethodTransformer extends AbstractDomainTransformer<PaymentMethod, PaymentMethodEntity> {

	private final PaymentMethodIdentifierResolver paymentMethodIdentifierResolver;

	/**
	 * Default constructor.
	 *
	 * @param paymentMethodIdentifierResolver the payment method identifier resolver.
	 */
	@Inject
	public PaymentMethodTransformer(
			@Named("paymentMethodIdentifierResolver")
			final PaymentMethodIdentifierResolver paymentMethodIdentifierResolver) {
		this.paymentMethodIdentifierResolver = paymentMethodIdentifierResolver;
	}

	@Override
	public PaymentMethod transformToDomain(final PaymentMethodEntity resourceEntity, final Locale locale) {
		throw new UnsupportedOperationException("This operation is not implemented.");
	}

	@Override
	public PaymentMethodEntity transformToEntity(final PaymentMethod paymentMethod, final Locale locale) {
		if (paymentMethod instanceof CustomerCreditCard) {
			return transformToCreditCard(paymentMethod);
		} else {
			return transformToPaymentToken(paymentMethod);
		}
	}

	private PaymentMethodEntity transformToCreditCard(final PaymentMethod paymentMethod) {
		final CustomerCreditCard customerCreditCard = (CustomerCreditCard) paymentMethod;

		return CreditCardEntity.builder()
				.withPaymentMethodId(paymentMethodIdentifierResolver.getIdentifierForPaymentMethod(customerCreditCard))
				.withCardholderName(trimToNull(customerCreditCard.getCardHolderName()))
				.withCardNumber(trimToNull(customerCreditCard.getMaskedCardNumber()))
				.withCardType(trimToNull(customerCreditCard.getCardType()))
				.withExpiryMonth(trimToNull(customerCreditCard.getExpiryMonth()))
				.withExpiryYear(trimToNull(customerCreditCard.getExpiryYear()))
				.withStartMonth(trimToNull(customerCreditCard.getStartMonth()))
				.withStartYear(trimToNull(customerCreditCard.getStartYear()))
				.withIssueNumber(customerCreditCard.getIssueNumber())
				.build();
	}

	private PaymentMethodEntity transformToPaymentToken(final PaymentMethod paymentMethod) {
		final PaymentToken paymentToken = (PaymentToken) paymentMethod;
		return PaymentTokenEntity.builder()
				.withDisplayName(paymentToken.getDisplayValue())
				.withToken(paymentToken.getValue())
				.withPaymentMethodId(paymentMethodIdentifierResolver.getIdentifierForPaymentMethod(paymentToken))
				.build();
	}
}
