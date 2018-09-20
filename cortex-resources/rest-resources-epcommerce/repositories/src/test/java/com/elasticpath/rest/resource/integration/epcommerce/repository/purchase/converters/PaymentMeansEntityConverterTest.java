/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.order.OrderAddress;
import com.elasticpath.domain.order.OrderPayment;
import com.elasticpath.domain.order.impl.OrderAddressImpl;
import com.elasticpath.domain.order.impl.OrderPaymentImpl;
import com.elasticpath.plugin.payment.PaymentType;
import com.elasticpath.rest.definition.addresses.AddressDetailEntity;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.base.NameEntity;
import com.elasticpath.rest.definition.purchases.ExpirationDateEntity;
import com.elasticpath.rest.definition.purchases.PaymentMeansCreditCardEntity;
import com.elasticpath.rest.definition.purchases.PaymentMeansEntity;
import com.elasticpath.rest.definition.purchases.PaymentMeansPaymentTokenEntity;


/**
 * The test of {@link PaymentMeansEntityConverter}.
 */
public class PaymentMeansEntityConverterTest {
	private static final String VOICE_TYPE = "voice";
	private static final String EXPIRY_YEAR = "EXPIRY_YEAR";
	private static final String EXPIRY_MONTH = "EXPIRY_MONTH";
	private static final String CARD_TYPE = "CARD_TYPE";
	private static final String CARD_HOLDER_NAME = "CARD_HOLDER_NAME";
	private static final String MASKED_CREDITCARD_NUMBER = "MASKED_CREDITCARD_NUMBER";
	private static final String POSTAL_CODE = "POSTAL_CODE";
	private static final String COUNTRY = "COUNTRY";
	private static final String REGION = "REGION";
	private static final String CITY = "CITY";
	private static final String STREET2 = "STREET2";
	private static final String STREET1 = "STREET1";
	private static final String PHONE_NUMBER = "PHONE_NUMBER";
	private static final String LAST_NAME = "LAST_NAME";
	private static final String FIRST_NAME = "FIRST_NAME";

	public static final Long TEST_UID_PK = 1000L;

	private final PaymentMeansEntityConverter paymentMeansEntityConverter = new PaymentMeansEntityConverter();

	/**
	 * Test convert to {@link PaymentMeansCreditCardEntity}.
	 */
	@Test
	public void testTransformToCreditCardPaymentMeansEntity() {
		Pair<OrderPayment, OrderAddress> orderPaymentOrderAddressPair = createOrderPaymentOrderAddressPair(createCreditCardOrderPayment());

		PaymentMeansEntity creditCardPaymentMeansDto = paymentMeansEntityConverter.convert(orderPaymentOrderAddressPair);

		PaymentMeansCreditCardEntity expectedPaymentMeansCreditCardEntity = createCreditCardPaymentMeansDto();
		assertEquals("The paymentMeans DTOs should be the same.", expectedPaymentMeansCreditCardEntity, creditCardPaymentMeansDto);
	}

	/**
	 * Test convert to {@link PaymentMeansCreditCardEntity}.
	 */
	@Test
	public void testResultOfTransformToCreditCardIsAssignableToCreditCardPaymentMeansDto() {
		OrderPaymentImpl orderPayment = new OrderPaymentImpl();
		orderPayment.setPaymentMethod(PaymentType.CREDITCARD);
		Pair<OrderPayment, OrderAddress> orderPaymentWrapper = Pair.of(orderPayment, new OrderAddressImpl());

		PaymentMeansEntity creditCardPaymentMeansDto = paymentMeansEntityConverter.convert(orderPaymentWrapper);

		assertTrue(PaymentMeansCreditCardEntity.class.isAssignableFrom(creditCardPaymentMeansDto.getClass()));
	}

	/**
	 * Test convert to {@link PaymentMeansPaymentTokenEntity}.
	 */
	@Test
	public void testResultOfTransformToPaymentTokenIsAssignableToPaymentMeansPaymentTokenEntity() {
		OrderPaymentImpl orderPayment = new OrderPaymentImpl();
		orderPayment.setPaymentMethod(PaymentType.PAYMENT_TOKEN);
		Pair<OrderPayment, OrderAddress> orderPaymentWrapper = Pair.of(orderPayment, new OrderAddressImpl());

		PaymentMeansEntity paymentTokenEntity = paymentMeansEntityConverter.convert(orderPaymentWrapper);

		assertTrue(PaymentMeansPaymentTokenEntity.class.isAssignableFrom(paymentTokenEntity.getClass()));
	}

	private Pair<OrderPayment, OrderAddress> createOrderPaymentOrderAddressPair(final OrderPayment orderPayment) {
		OrderAddress orderAddress = new OrderAddressImpl();
		orderAddress.setFirstName(FIRST_NAME);
		orderAddress.setLastName(LAST_NAME);
		orderAddress.setPhoneNumber(PHONE_NUMBER);
		orderAddress.setStreet1(STREET1);
		orderAddress.setStreet2(STREET2);
		orderAddress.setCity(CITY);
		orderAddress.setSubCountry(REGION);
		orderAddress.setCountry(COUNTRY);
		orderAddress.setZipOrPostalCode(POSTAL_CODE);
		return Pair.of(orderPayment, orderAddress);
	}

	private OrderPayment createCreditCardOrderPayment() {
		OrderPayment orderPayment = new OrderPaymentImpl() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getDisplayValue() {
				return MASKED_CREDITCARD_NUMBER;
			}
		};
		orderPayment.setCardHolderName(CARD_HOLDER_NAME);
		orderPayment.setCardType(CARD_TYPE);
		orderPayment.setExpiryMonth(EXPIRY_MONTH);
		orderPayment.setExpiryYear(EXPIRY_YEAR);

		orderPayment.setUidPk(TEST_UID_PK);

		return orderPayment;
	}

	private PaymentMeansCreditCardEntity createCreditCardPaymentMeansDto() {

		ExpirationDateEntity expirationDateEntity = ExpirationDateEntity.builder()
				.withMonth(EXPIRY_MONTH)
				.withYear(EXPIRY_YEAR)
				.build();
		NameEntity nameEntity = NameEntity.builder()
				.withFamilyName(LAST_NAME)
				.withGivenName(FIRST_NAME)
				.build();
		AddressDetailEntity addressDetailEntity = AddressDetailEntity.builder()
				.withStreetAddress(STREET1)
				.withExtendedAddress(STREET2)
				.withLocality(CITY)
				.withRegion(REGION)
				.withCountryName(COUNTRY)
				.withPostalCode(POSTAL_CODE)
				.build();

		AddressEntity billingAddressEntity = AddressEntity.builder()
				.withName(nameEntity)
				.withAddress(addressDetailEntity)
				.build();

		return PaymentMeansCreditCardEntity.builder()
				.withPaymentMeansId(String.valueOf(TEST_UID_PK))
				.withHolderName(CARD_HOLDER_NAME)
				.withPrimaryAccountNumberId(MASKED_CREDITCARD_NUMBER)
				.withCardType(CARD_TYPE)
				.withExpiryDate(expirationDateEntity)
				.withBillingAddress(billingAddressEntity)
				.withTelephoneNumber(PHONE_NUMBER)
				.withTelephoneType(VOICE_TYPE)
				.build();
	}
}
