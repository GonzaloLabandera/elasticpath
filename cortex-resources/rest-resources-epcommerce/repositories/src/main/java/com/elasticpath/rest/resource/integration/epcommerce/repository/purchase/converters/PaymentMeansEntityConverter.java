/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.converters;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.order.OrderAddress;
import com.elasticpath.domain.order.OrderPayment;
import com.elasticpath.plugin.payment.PaymentType;
import com.elasticpath.rest.definition.addresses.AddressDetailEntity;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.base.NameEntity;
import com.elasticpath.rest.definition.purchases.ExpirationDateEntity;
import com.elasticpath.rest.definition.purchases.PaymentMeansCreditCardEntity;
import com.elasticpath.rest.definition.purchases.PaymentMeansEntity;
import com.elasticpath.rest.definition.purchases.PaymentMeansPaymentTokenEntity;

/**
 * Converter for PaymentMeansEntity.
 */
@Singleton
@Named
public class PaymentMeansEntityConverter implements Converter<Pair<OrderPayment, OrderAddress>, PaymentMeansEntity> {

	private static final String VOICE_TYPE = "voice";

	@Override
	public PaymentMeansEntity convert(final Pair<OrderPayment, OrderAddress> orderPaymentOrderAddressPair) {
		OrderPayment orderPayment = orderPaymentOrderAddressPair.getFirst();

		PaymentType paymentType = orderPayment.getPaymentMethod();

		if (PaymentType.PAYMENT_TOKEN.equals(paymentType)) {
			return PaymentMeansPaymentTokenEntity.builder()
					.withPaymentMeansId(String.valueOf(orderPayment.getUidPk()))
					.withDisplayName(orderPayment.getDisplayValue())
					.build();
		}

		OrderAddress billingAddress = orderPaymentOrderAddressPair.getSecond();

		ExpirationDateEntity expirationDateEntity = ExpirationDateEntity.builder()
				.withMonth(orderPayment.getExpiryMonth())
				.withYear(orderPayment.getExpiryYear())
				.build();
		NameEntity nameEntity = NameEntity.builder()
				.withFamilyName(StringUtils.trimToNull(billingAddress.getLastName()))
				.withGivenName(StringUtils.trimToNull(billingAddress.getFirstName()))
				.build();
		AddressDetailEntity addressDetailEntity = AddressDetailEntity.builder()
				.withStreetAddress(StringUtils.trimToNull(billingAddress.getStreet1()))
				.withExtendedAddress(StringUtils.trimToNull(billingAddress.getStreet2()))
				.withLocality(StringUtils.trimToNull(billingAddress.getCity()))
				.withRegion(StringUtils.trimToNull(billingAddress.getSubCountry()))
				.withCountryName(StringUtils.trimToNull(billingAddress.getCountry()))
				.withPostalCode(StringUtils.trimToNull(billingAddress.getZipOrPostalCode()))
				.build();
		AddressEntity billingAddressEntity = AddressEntity.builder()
				.withName(nameEntity)
				.withAddress(addressDetailEntity)
				.build();

		return PaymentMeansCreditCardEntity.builder()
				.withPaymentMeansId(String.valueOf(orderPayment.getUidPk()))
				.withHolderName(orderPayment.getCardHolderName())
				.withPrimaryAccountNumberId(orderPayment.getDisplayValue())
				.withCardType(orderPayment.getCardType())
				.withExpiryDate(expirationDateEntity)
				.withTelephoneNumber(billingAddress.getPhoneNumber())
				.withTelephoneType(VOICE_TYPE)
				.withBillingAddress(billingAddressEntity)
				.build();
	}
}
