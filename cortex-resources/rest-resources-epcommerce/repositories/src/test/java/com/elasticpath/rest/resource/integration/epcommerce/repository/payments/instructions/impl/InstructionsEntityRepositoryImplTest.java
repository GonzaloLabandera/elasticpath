/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.buildTestAddressDTO;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.buildTestAddressEntity;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.buildTestCustomerAddress;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildEmptyCustomerAddressEntity;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentMethodConfigurationEntity;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Completable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.domain.orderpaymentapi.impl.PICRequestContext;
import com.elasticpath.domain.orderpaymentapi.impl.StorePaymentProviderConfigImpl;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.paymentinstructions.PaymentMethodConfigurationEntity;
import com.elasticpath.rest.definitions.validator.constants.ValidationMessages;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.validator.AddressValidator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.StorePaymentProviderConfigRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment.OrderPaymentApiRepository;


/**
 * Test for {@link InstructionsEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class InstructionsEntityRepositoryImplTest {

	private static final String PROVIDER_CONFIG_GUID = "PROVIDER_CONFIG_GUID";
	private static final String METHOD_ID = "METHOD_ID";
	private static final List<String> PIC_INSTRUCTIONS_FIELDS = asList(
			"Creation Instructions Field One",
			"Creation Instructions Field Two");
	private static final Map<String, String> PIC_INSTRUCTIONS_FIELDS_MAP = ImmutableMap.of(
			"Creation Instructions Field One", "",
			"Creation Instructions Field Two", "",
			"billing-address", buildEmptyCustomerAddressEntity().toString());
	private static final Map<String, String> CONTROL_DATA = ImmutableMap.of("control-data-key", "control-data-value");
	private static final Map<String, String> PAYLOAD_DATA = ImmutableMap.of("payload-data-key", "payload-data-value");
	private static final String USER_ID = "SHARED_ID";
	private static final Locale LOCALE = Locale.CANADA;
	private static final Currency CURRENCY = Currency.getInstance(LOCALE);
	private static final String FIRST_NAME = "Bruce";
	private static final String FAMILY_NAME = "Wayne";
	private static final String EMAIL_ADDRESS = "bat@cave.com";

	@InjectMocks
	private InstructionsEntityRepositoryImpl instructionsEntityRepository;

	@Mock
	private OrderPaymentApiRepository orderPaymentApiRepository;

	@Mock
	private StorePaymentProviderConfigRepository configRepository;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private AddressValidator addressValidator;

	@Mock
	private ConversionService conversionService;

	@Mock
	private Customer customer;

	@Before
	public void setUp() {
		when(customer.getUserId()).thenReturn(USER_ID);
		when(customer.getFirstName()).thenReturn(FIRST_NAME);
		when(customer.getLastName()).thenReturn(FAMILY_NAME);
		when(customer.getEmail()).thenReturn(EMAIL_ADDRESS);

		StorePaymentProviderConfig storePaymentProviderConfig = new StorePaymentProviderConfigImpl();
		storePaymentProviderConfig.setGuid(METHOD_ID);
		storePaymentProviderConfig.setPaymentProviderConfigGuid(PROVIDER_CONFIG_GUID);
		CustomerAddress customerAddress = buildTestCustomerAddress();

		when(orderPaymentApiRepository.getPICInstructions(eq(PROVIDER_CONFIG_GUID), eq(PIC_INSTRUCTIONS_FIELDS_MAP), any(PICRequestContext.class)))
				.thenReturn(Single.just(createPICInstructionsDTO()));
		when(orderPaymentApiRepository.getPICInstructionsFields(eq(PROVIDER_CONFIG_GUID), any(PICFieldsRequestContext.class)))
				.thenReturn(Single.just(createPICInstructionsFieldsDTO()));

		when(customerRepository.getCustomer(USER_ID)).thenReturn(Single.just(customer));
		when(configRepository.getPaymentProviderConfigIdByStorePaymentProviderConfigId(METHOD_ID)).thenReturn(Single.just(PROVIDER_CONFIG_GUID));
		when(configRepository.requiresBillingAddress(METHOD_ID)).thenReturn(Single.just(true));
		when(addressValidator.validate(any(com.elasticpath.rest.definition.addresses.AddressEntity.class))).thenReturn(Completable.complete());

		when(customerRepository.createAddressForCustomer(customer, customerAddress)).thenReturn(Single.just(customerAddress));

		when(conversionService.convert(any(CustomerAddress.class), eq(AddressDTO.class))).thenReturn(buildTestAddressDTO());
		when(conversionService.convert(buildTestAddressEntity(), CustomerAddress.class)).thenReturn(customerAddress);
		when(conversionService.convert(buildEmptyCustomerAddressEntity(), CustomerAddress.class)).thenReturn(customerAddress);
	}

	@Test
	public void testGetPaymentInstrumentCreationInstructionsFieldsForMethodIdWhenBillingAddressRequired() {
		instructionsEntityRepository.getPaymentInstrumentCreationInstructionsFieldsForMethodId(METHOD_ID, USER_ID, CURRENCY, LOCALE)
				.test()
				.assertNoErrors()
				.assertValue(buildPaymentMethodConfigurationEntity(PIC_INSTRUCTIONS_FIELDS, true));
	}

	@Test
	public void testGetPaymentInstrumentCreationInstructionsFieldsForMethodIdWhenBillingAddressNotRequired() {
		when(configRepository.requiresBillingAddress(METHOD_ID)).thenReturn(Single.just(false));
		instructionsEntityRepository.getPaymentInstrumentCreationInstructionsFieldsForMethodId(METHOD_ID, USER_ID, CURRENCY, LOCALE)
				.test()
				.assertNoErrors()
				.assertValue(buildPaymentMethodConfigurationEntity(PIC_INSTRUCTIONS_FIELDS, false));
	}

	@Test
	public void getPaymentInstrumentCreationInstructionsFieldsForMethodIdThrowsErrorWhenNoCustomerExists() {
		when(customerRepository.getCustomer(USER_ID)).thenReturn(Single.error(ResourceOperationFailure.notFound("Customer not found.")));
		instructionsEntityRepository.getPaymentInstrumentCreationInstructionsFieldsForMethodId(METHOD_ID, USER_ID, CURRENCY, LOCALE)
				.test()
				.assertError(ResourceOperationFailure.notFound("Customer not found."));
	}

	@Test
	public void getPaymentInstrumentCreationInstructionsFieldsForMethodIdThrowsErrorWhenNoStorePaymentProviderConfigExists() {
		when(configRepository.getPaymentProviderConfigIdByStorePaymentProviderConfigId(METHOD_ID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound("Configuration not found.")));
		instructionsEntityRepository.getPaymentInstrumentCreationInstructionsFieldsForMethodId(METHOD_ID, USER_ID, CURRENCY, LOCALE)
				.test()
				.assertError(ResourceOperationFailure.notFound("Configuration not found."));
	}

	@Test
	public void getPaymentInstrumentCreationInstructionsFieldsForMethodIdThrowsErrorWhenGetPICInstructionsFieldsFails() {
		when(orderPaymentApiRepository.getPICInstructionsFields(eq(PROVIDER_CONFIG_GUID), any(PICFieldsRequestContext.class)))
				.thenReturn(Single.error(ResourceOperationFailure.notFound("Error getting PICInstructionsFields.")));
		instructionsEntityRepository.getPaymentInstrumentCreationInstructionsFieldsForMethodId(METHOD_ID, USER_ID, CURRENCY, LOCALE)
				.test()
				.assertError(ResourceOperationFailure.notFound("Error getting PICInstructionsFields."));
	}

	@Test
	public void getPaymentInstrumentCreationInstructionsFieldsForMethodIdFailsWhenBillingAddressCheckFails() {
		when(configRepository.requiresBillingAddress(METHOD_ID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound("Failure determining billing address requirement.")));
		instructionsEntityRepository.getPaymentInstrumentCreationInstructionsFieldsForMethodId(METHOD_ID, USER_ID, CURRENCY, LOCALE)
				.test()
				.assertError(ResourceOperationFailure.notFound("Failure determining billing address requirement."));
	}

	@Test
	public void submitRequestPaymentInstructionsFormSucceedsWithRepeatAddress() {
		when(orderPaymentApiRepository.getPICInstructions(eq(PROVIDER_CONFIG_GUID), any(), any(PICRequestContext.class)))
				.thenReturn(Single.just(createPICInstructionsDTO()));

		PaymentMethodConfigurationEntity paymentMethodConfigurationEntity = PaymentMethodConfigurationEntity.builder()
				.withBillingAddress(buildTestAddressEntity())
				.addingProperty("Creation Instructions Field One", "")
				.addingProperty("Creation Instructions Field Two", "")
				.build();

		PICInstructionsDTO expected = createPICInstructionsDTO();

		instructionsEntityRepository.submitRequestInstructionsForm(METHOD_ID, LOCALE, CURRENCY, paymentMethodConfigurationEntity, USER_ID)
				.test()
				.assertNoErrors()
				.assertValue(actualPICInstructionsDTO -> actualPICInstructionsDTO.getPayload().equals(expected.getPayload())
						&& actualPICInstructionsDTO.getCommunicationInstructions().equals(expected.getCommunicationInstructions()));
	}

	@Test
	public void submitRequestPaymentInstructionsFormSucceedsWithNewAddress() {
		PaymentMethodConfigurationEntity paymentMethodConfigurationEntity = buildPaymentMethodConfigurationEntity(PIC_INSTRUCTIONS_FIELDS, true);

		PICInstructionsDTO expected = createPICInstructionsDTO();

		instructionsEntityRepository.submitRequestInstructionsForm(METHOD_ID, LOCALE, CURRENCY, paymentMethodConfigurationEntity, USER_ID)
				.test()
				.assertNoErrors()
				.assertValue(actualPICInstructionsDTO -> actualPICInstructionsDTO.getPayload().equals(expected.getPayload())
						&& actualPICInstructionsDTO.getCommunicationInstructions().equals(expected.getCommunicationInstructions()));
	}

	@Test
	public void submitRequestPaymentInstructionsFormSucceedsWhenAddressNotRequired() {
		PaymentMethodConfigurationEntity paymentMethodConfigurationEntity =
				buildPaymentMethodConfigurationEntity(Collections.emptyList(), false);

		PICInstructionsDTO instructionsDTO = createPICInstructionsDTO();

		when(orderPaymentApiRepository.getPICInstructions(eq(PROVIDER_CONFIG_GUID), eq(Collections.emptyMap()), any(PICRequestContext.class)))
				.thenReturn(Single.just(instructionsDTO));

		instructionsEntityRepository.submitRequestInstructionsForm(METHOD_ID, LOCALE, CURRENCY, paymentMethodConfigurationEntity, USER_ID)
				.test()
				.assertNoErrors()
				.assertValue(actualPICInstructionsDTO ->
						actualPICInstructionsDTO.getPayload().equals(instructionsDTO.getPayload())
								&& actualPICInstructionsDTO.getCommunicationInstructions().equals(instructionsDTO.getCommunicationInstructions()));
	}

	@Test
	public void submitRequestPaymentInstructionsFormFailsOnValidation() {
		PaymentMethodConfigurationEntity paymentMethodConfigurationEntity = buildPaymentMethodConfigurationEntity(PIC_INSTRUCTIONS_FIELDS, true);

		when(addressValidator.validate(any(com.elasticpath.rest.definition.addresses.AddressEntity.class)))
				.thenReturn(Completable.error(ResourceOperationFailure.badRequestBody(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY)));

		instructionsEntityRepository.submitRequestInstructionsForm(METHOD_ID, LOCALE, CURRENCY, paymentMethodConfigurationEntity, USER_ID)
				.test()
				.assertError(ResourceOperationFailure.badRequestBody(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY));
	}

	@Test
	public void submitRequestPaymentInstructionsFormFailsOnGetPICInstructions() {
		PaymentMethodConfigurationEntity paymentMethodConfigurationEntity = buildPaymentMethodConfigurationEntity(PIC_INSTRUCTIONS_FIELDS, true);

		when(orderPaymentApiRepository.getPICInstructions(eq(PROVIDER_CONFIG_GUID), eq(PIC_INSTRUCTIONS_FIELDS_MAP), any(PICRequestContext.class)))
				.thenReturn(Single.error(ResourceOperationFailure.notFound("Error getting PICInstructions.")));

		instructionsEntityRepository.submitRequestInstructionsForm(METHOD_ID, LOCALE, CURRENCY, paymentMethodConfigurationEntity, USER_ID)
				.test()
				.assertError(ResourceOperationFailure.notFound("Error getting PICInstructions."));

	}

	private PICInstructionsFieldsDTO createPICInstructionsFieldsDTO() {
		final PICInstructionsFieldsDTO fieldsDTO = new PICInstructionsFieldsDTO();
		fieldsDTO.setFields(PIC_INSTRUCTIONS_FIELDS);
		fieldsDTO.setStructuredErrorMessages(Collections.emptyList());
		return fieldsDTO;
	}

	private PICInstructionsDTO createPICInstructionsDTO() {
		final PICInstructionsDTO instructionsDTO = new PICInstructionsDTO();
		instructionsDTO.setPayload(PAYLOAD_DATA);
		instructionsDTO.setCommunicationInstructions(CONTROL_DATA);
		return instructionsDTO;
	}

}
