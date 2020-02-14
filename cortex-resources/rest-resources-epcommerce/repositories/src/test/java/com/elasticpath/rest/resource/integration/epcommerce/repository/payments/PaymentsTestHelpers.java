/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments;

import com.google.common.collect.ImmutableMap;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.impl.CustomerAddressImpl;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.rest.definition.base.AddressEntity;
import com.elasticpath.rest.definition.base.NameEntity;


/**
 * Helper methods Payment tests.
 */
public final class PaymentsTestHelpers {

	private static final String FIRST_NAME = "Bruce";
	private static final String FAMILY_NAME = "Wayne";
	private static final String REGION = "Gotham";
	private static final String STREET_ADDRESS = "228 Bat Lane";
	private static final String EXTENDED_ADDRESS = "Wayne Manor";
	private static final String POSTAL_CODE = "228-626";
	private static final String LOCALITY = "Gotham City";
	private static final String COUNTRY = "Country";
	private static final String PHONE_NUMBER = "228-363-2287";

	/**
	 * Constant for payment provider id.
	 */
	public static final String PAYMENT_PROVIDER_ID = "test-payment-provider";

	/**
	 * Constant map for test form data.
	 */
	public static final ImmutableMap<String, String> TEST_MAP = ImmutableMap.of("key", "data");

	/**
	 * Constant for payment provider configuration ID.
	 */
	public static final String PAYMENT_PROVIDER_CONFIG_ID = "PAYMENT_PROVIDER_CONFIG_ID";

	/**
	 * Constant for payment provider configuration name.
	 */
	public static final String PAYMENT_PROVIDER_CONFIG_NAME = "PAYMENT_PROVIDER_CONFIG_NAME";

	/**
	 * Constant for payment provider configuration display name.
	 */
	public static final String PAYMENT_PROVIDER_CONFIG_DISPLAY_NAME = "PAYMENT_PROVIDER_CONFIG_DISPLAY_NAME";

	/**
	 * Constant for store payment provider configuration ID.
	 */
	public static final String STORE_PAYMENT_PROVIDER_ID = "STORE_PAYMENT_PROVIDER_CONFIG_ID";

	/**
	 * Constant for cart order payment instrument ID.
	 */
	public static final String CART_ORDER_PAYMENT_INSTRUMENT_ID = "CART_ORDER_PAYMENT_INSTRUMENT_ID";

	/**
	 * Constant for customer payment instrument ID.
	 */
	public static final String CUSTOMER_PAYMENT_INSTRUMENT_ID = "CUSTOMER_PAYMENT_INSTRUMENT_ID";

	/**
	 * Constant for payment instrument ID.
	 */
	public static final String PAYMENT_INSTRUMENT_ID = "PAYMENT_INSTRUMENT_ID";

	/**
	 * Constant for order payment instrument ID.
	 */
	public static final String ORDER_PAYMENT_INSTRUMENT_ID = "ORDER_PAYMENT_INSTRUMENT_ID";

	/**
	 * Constant for order ID.
	 */
	public static final String ORDER_ID = "ORDER_ID";

	/**
	 * Constant for purchase ID.
	 */
	public static final String PURCHASE_ID = "PURCHASE_ID";

	/**
	 * Constant for customer ID.
	 */
	public static final String CUSTOMER_ID = "CUSTOMER_ID";

	private PaymentsTestHelpers() {
		// Empty constructor.
	}

	/**
	 * Helper method to build a test address dto.
	 *
	 * @return address dto
	 */
	public static AddressDTO buildTestAddressDTO() {
		AddressDTO addressDTO = new AddressDTO();
		addressDTO.setCity(LOCALITY);
		addressDTO.setCountry("Country");
		addressDTO.setFirstName(FIRST_NAME);
		addressDTO.setLastName(FAMILY_NAME);
		addressDTO.setPhoneNumber(PHONE_NUMBER);
		addressDTO.setStreet1(STREET_ADDRESS);
		addressDTO.setStreet2(EXTENDED_ADDRESS);
		addressDTO.setSubCountry("Gotham");
		addressDTO.setZipOrPostalCode(POSTAL_CODE);
		return addressDTO;
	}

	/**
	 * Helper method to build a test address entity.
	 *
	 * @return address entity
	 */
	public static com.elasticpath.rest.definition.addresses.AddressEntity buildTestAddressEntity() {

		AddressEntity baseAddressEntity = AddressEntity.builder()
				.withStreetAddress(STREET_ADDRESS)
				.withExtendedAddress(EXTENDED_ADDRESS)
				.withRegion(REGION)
				.withPostalCode(POSTAL_CODE)
				.withLocality(LOCALITY)
				.withCountryName(COUNTRY)
				.build();

		NameEntity nameEntity = NameEntity.builder()
				.withGivenName(FIRST_NAME)
				.withFamilyName(FAMILY_NAME)
				.build();

		return com.elasticpath.rest.definition.addresses.AddressEntity.builder()
				.withOrganization("Wayne Enterprises")
				.withAddress(baseAddressEntity)
				.withName(nameEntity)
				.withPhoneNumber(PHONE_NUMBER)
				.build();
	}

	/**
	 * Helper method to build a test customer address.
	 *
	 * @return customer address
	 */
	public static CustomerAddress buildTestCustomerAddress() {
		CustomerAddress customerAddress = new CustomerAddressImpl();
		customerAddress.setCity(LOCALITY);
		customerAddress.setCountry(COUNTRY);
		customerAddress.setFirstName(FIRST_NAME);
		customerAddress.setLastName(FAMILY_NAME);
		customerAddress.setPhoneNumber(PHONE_NUMBER);
		customerAddress.setStreet1(STREET_ADDRESS);
		customerAddress.setStreet2(EXTENDED_ADDRESS);
		customerAddress.setSubCountry(REGION);
		customerAddress.setZipOrPostalCode(POSTAL_CODE);
		return customerAddress;
	}

	/**
	 * Helper method to build a test payment instrument dto.
	 *
	 * @return payment instrument dto
	 */
	public static PaymentInstrumentDTO createTestPaymentInstrumentDTO() {
		final PaymentInstrumentDTO dto = new PaymentInstrumentDTO();
		dto.setGUID("test-payment-instrument-uuid");
		dto.setName("Test Name");
		dto.setData(TEST_MAP);
		dto.setPaymentProviderConfigurationGuid(PAYMENT_PROVIDER_ID);
		dto.setPaymentProviderConfiguration(ImmutableMap.of("configKey", "configValue"));
		dto.setBillingAddressGuid("billingAddressGuid");
		dto.setSupportingMultiCharges(false);
		dto.setSingleReservePerPI(false);
		return dto;
	}

	/**
	 * Helper method to build a test payment provider config dto.
	 *
	 * @param providerConfigGuid  provider config guid
	 * @param providerConfigName  provider config name
	 * @param providerDisplayName provider config display name
	 * @return payment provider config dto
	 */
	public static PaymentProviderConfigDTO createTestPaymentProviderConfiguration(final String providerConfigGuid,
																				  final String providerConfigName,
																				  final String providerDisplayName) {
		final PaymentProviderConfigDTO dto = new PaymentProviderConfigDTO();
		dto.setGuid(providerConfigGuid);
		dto.setConfigurationName(providerConfigName);
		dto.setDefaultDisplayName(providerDisplayName);
		return dto;
	}
}
