/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_FIELDS_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_INSTRUCTIONS_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_INSTRUCTIONS_FIELDS_DTO;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_PROVIDER_CONFIG_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.domain.orderpaymentapi.impl.PICRequestContext;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTOBuilder;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTOBuilder;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTOBuilder;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment.impl.OrderPaymentApiRepositoryImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;

/**
 * Tests for {@link OrderPaymentApiRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderPaymentApiRepositoryTest {

	private static final ImmutableMap<String, String> FORM_DATA = ImmutableMap.of("key", "data");
	private static final ImmutableMap<String, String> COMMUNICATION_INSTRUCTIONS = ImmutableMap.of("control key", "control data");
	private static final ImmutableMap<String, String> PAYLOAD = ImmutableMap.of("payload key", "payload data");

	@Mock
	private OrderPaymentApiService orderPaymentApiService;

	@Mock
	private PICFieldsRequestContext picFieldsRequestContext;

	@Mock
	private BeanFactory beanFactory;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@InjectMocks
	private OrderPaymentApiRepositoryImpl orderPaymentApiRepository;

	private PICInstructionsFieldsDTO instructionsFieldsDto;

	@Before
	public void setUp() {
		when(beanFactory.getPrototypeBean(PIC_INSTRUCTIONS_FIELDS_DTO, PICInstructionsFieldsDTO.class)).thenReturn(new PICInstructionsFieldsDTO());
		when(beanFactory.getPrototypeBean(PIC_INSTRUCTIONS_DTO, PICInstructionsDTO.class)).thenReturn(new PICInstructionsDTO());
		when(beanFactory.getPrototypeBean(PIC_FIELDS_DTO, PaymentInstrumentCreationFieldsDTO.class))
				.thenReturn(new PaymentInstrumentCreationFieldsDTO());

		orderPaymentApiRepository.setReactiveAdapter(reactiveAdapter);
		instructionsFieldsDto = PICInstructionsFieldsDTOBuilder.builder()
				.withFields(ImmutableList.of("Instructions Field 1", "Instructions Field 2"))
				.withStructuredErrorMessages(Collections.emptyList())
				.build(beanFactory);

		when(orderPaymentApiService.getPICInstructionsFields(PAYMENT_PROVIDER_CONFIG_ID, picFieldsRequestContext))
				.thenReturn(instructionsFieldsDto);
	}

	@Test
	public void getPICInstructionsFieldsReturnsExpectedInstructionsFields() {
		orderPaymentApiRepository.getPICInstructionsFields(PAYMENT_PROVIDER_CONFIG_ID, picFieldsRequestContext)
				.test()
				.assertNoErrors()
				.assertValue(instructionsFieldsDto);
	}

	@Test
	public void getPICInstructionsReturnsExpectedInstructions() {
		PICInstructionsDTO picInstructionsDTO = PICInstructionsDTOBuilder.builder()
				.withCommunicationInstructions(COMMUNICATION_INSTRUCTIONS)
				.withPayload(PAYLOAD)
				.build(beanFactory);

		when(orderPaymentApiService.getPICInstructions(eq(PAYMENT_PROVIDER_CONFIG_ID), eq(FORM_DATA), any(PICRequestContext.class)))
				.thenReturn(picInstructionsDTO);

		orderPaymentApiRepository.getPICInstructions(PAYMENT_PROVIDER_CONFIG_ID, FORM_DATA, mock(PICRequestContext.class))
				.test()
				.assertNoErrors()
				.assertValue(picInstructionsDTO);
	}

	@Test
	public void getPICFieldsReturnsExpectedFields() {
		PaymentInstrumentCreationFieldsDTO paymentInstrumentCreationFieldsDTO = PaymentInstrumentCreationFieldsDTOBuilder.builder()
				.withFields(ImmutableList.of("Field 1", "Field 2"))
				.withBlockingFields(Collections.emptyList())
				.withIsSaveable(true)
				.build(beanFactory);

		when(orderPaymentApiService.getPICFields(eq(PAYMENT_PROVIDER_CONFIG_ID), any(PICFieldsRequestContext.class)))
				.thenReturn(paymentInstrumentCreationFieldsDTO);

		orderPaymentApiRepository.getPICFields(PAYMENT_PROVIDER_CONFIG_ID, mock(PICFieldsRequestContext.class))
				.test()
				.assertNoErrors()
				.assertValue(paymentInstrumentCreationFieldsDTO);
	}

	@Test
	public void createPIReturnsExpectedFields() {
		String paymentInstrumentGuid = "PAYMENT_INSTRUMENT_GUID";
		when(orderPaymentApiService.createPI(eq(PAYMENT_PROVIDER_CONFIG_ID), eq(FORM_DATA), any(PICRequestContext.class)))
				.thenReturn(paymentInstrumentGuid);

		orderPaymentApiRepository.createPI(PAYMENT_PROVIDER_CONFIG_ID, FORM_DATA, mock(PICRequestContext.class))
				.test()
				.assertNoErrors()
				.assertValue(paymentInstrumentGuid);
	}

	@Test
	public void requiresBillingAddressReturnsServiceCallResult() {
		when(orderPaymentApiService.requiresBillingAddress(PAYMENT_PROVIDER_CONFIG_ID)).thenReturn(true);

		orderPaymentApiRepository.requiresBillingAddress(PAYMENT_PROVIDER_CONFIG_ID)
				.test()
				.assertNoErrors()
				.assertValue(true);
	}

	@Test
	public void requiresBillingAddressFailsWhenServiceCallFails() {
		IllegalStateException illegalStateException =
				new IllegalStateException("Configuration with GUID " + PAYMENT_PROVIDER_CONFIG_ID + " is missing");

		when(orderPaymentApiService.requiresBillingAddress(PAYMENT_PROVIDER_CONFIG_ID)).thenThrow(illegalStateException);

		orderPaymentApiRepository.requiresBillingAddress(PAYMENT_PROVIDER_CONFIG_ID)
				.test()
				.assertNoValues()
				.assertError(illegalStateException);
	}
}
