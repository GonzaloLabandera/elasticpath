/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_INSTRUMENT;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_INSTRUMENT_DATA;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_FIELDS_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_INSTRUCTIONS_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_INSTRUCTIONS_FIELDS_DTO;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_INSTRUCTIONS_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationFields;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationRequest;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationResponse;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICClientInteractionRequestCapability;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructions;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsFields;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsRequest;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.dto.PICRequestContextDTO;
import com.elasticpath.plugin.payment.provider.exception.ErrorMessage;
import com.elasticpath.plugin.payment.provider.exception.PaymentInstrumentCreationFailedException;
import com.elasticpath.plugin.payment.provider.exception.StructuredMessageType;
import com.elasticpath.provider.payment.domain.PaymentInstrument;
import com.elasticpath.provider.payment.domain.PaymentInstrumentData;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.PaymentsExceptionMessageId;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentService;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;

@RunWith(MockitoJUnitRunner.class)
public class PaymentInstrumentCreationProcessorImplTest {

	private static final String PAYMENT_PROVIDER_CONFIGURATION_GUID = "paymentProviderConfigurationGuid";
	private static final PaymentInstrumentCreationFields PIC_FIELDS = new PaymentInstrumentCreationFields(Collections.emptyList(), false);
	private static final Map<String, String> PIC_RESPONSE_DETAILS = ImmutableMap.of("key", "data");
	private static final String PAYMENT_INSTRUMENT_GUID = "paymentInstrumentGuid";
	private static final String PAYMENT_INSTRUMENT_NAME = "display-name";
	private static final String EMPTY_NAME_MESSAGE_ID = "payment.instrument.name.required";
	private static final String PLUGIN_SPECIFIC_ERROR_ID = "error.id";
	private static final String INSTRUCTIONS_KEY = "instructionsKey";
	private static final String INSTRUCTIONS_VALUE = "instructionsValue";
	private static final String TOKEN_DEBUG_MESSAGE = "Token is required.";

	@Mock
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	@Mock
	private PaymentInstrumentService paymentInstrumentService;

	@Mock
	private PaymentProviderService paymentProviderService;

	@Mock
	private BeanFactory beanFactory;

	@InjectMocks
	private PaymentInstrumentCreationProcessorImpl testee;

	@Captor
	private ArgumentCaptor<Set<PaymentInstrumentData>> paymentInstrumentDataSetCaptor;

	private final PICInstructionsFields instructionsFields = new PICInstructionsFields(ImmutableList.of("testField"));

	private final PICInstructions instructions = new PICInstructions(
			ImmutableMap.of("controlKey", "controlData"),
			ImmutableMap.of("payloadKey", "payloadData"));
	private final PaymentProvider capablePaymentProvider = mock(PaymentProvider.class);
	private final PaymentProviderConfiguration paymentProviderConfiguration = mock(PaymentProviderConfiguration.class);
	private final PICClientInteractionRequestCapability interactionCapability = mock(PICClientInteractionRequestCapability.class);
	private final PICCapability instructionsCapability = mock(PICCapability.class);
	private final PaymentInstrument paymentInstrument = mock(PaymentInstrument.class);
	private final PaymentInstrumentData paymentInstrumentData = mock(PaymentInstrumentData.class);
	private final PICFieldsRequestContextDTO picFieldsRequestContextDTO = mock(PICFieldsRequestContextDTO.class);
	private final PICRequestContextDTO picRequestContextDTO = mock(PICRequestContextDTO.class);

	@Before
	public void setUp() throws PaymentInstrumentCreationFailedException {
		when(beanFactory.getPrototypeBean(PAYMENT_INSTRUMENT, PaymentInstrument.class)).thenReturn(paymentInstrument);
		when(beanFactory.getPrototypeBean(PAYMENT_INSTRUMENT_DATA, PaymentInstrumentData.class)).thenReturn(paymentInstrumentData);
		when(beanFactory.getPrototypeBean(PIC_FIELDS_DTO, PaymentInstrumentCreationFieldsDTO.class))
				.thenReturn(new PaymentInstrumentCreationFieldsDTO());
		when(beanFactory.getPrototypeBean(PIC_INSTRUCTIONS_FIELDS_DTO, PICInstructionsFieldsDTO.class))
				.thenReturn(new PICInstructionsFieldsDTO());
		when(beanFactory.getPrototypeBean(PIC_INSTRUCTIONS_DTO, PICInstructionsDTO.class))
				.thenReturn(new PICInstructionsDTO());

		when(paymentProviderConfigurationService.findByGuid(PAYMENT_PROVIDER_CONFIGURATION_GUID)).thenReturn(paymentProviderConfiguration);

		when(interactionCapability.getPaymentInstrumentCreationInstructions(any(PICInstructionsRequest.class))).thenReturn(instructions);
		when(interactionCapability.getPaymentInstrumentCreationInstructionsFields(any(PICFieldsRequestContextDTO.class)))
				.thenReturn(instructionsFields);

		when(paymentProviderService.createProvider(paymentProviderConfiguration)).thenReturn(capablePaymentProvider);

		when(capablePaymentProvider.getCapability(PICClientInteractionRequestCapability.class)).thenReturn(Optional.of(interactionCapability));
		when(capablePaymentProvider.getCapability(PICCapability.class)).thenReturn(Optional.of(instructionsCapability));

		PaymentInstrumentCreationResponse response = mock(PaymentInstrumentCreationResponse.class);
		when(response.getDetails()).thenReturn(PIC_RESPONSE_DETAILS);
		when(instructionsCapability.createPaymentInstrument(any())).thenReturn(response);
		when(instructionsCapability.getPaymentInstrumentCreationFields(any(PICFieldsRequestContextDTO.class))).thenReturn(PIC_FIELDS);

		when(paymentInstrument.getGuid()).thenReturn(PAYMENT_INSTRUMENT_GUID);
	}

	@Test
	public void testGetPICInstructionFieldsShouldReturnExpectedPICInstructionsFieldsWhenPICClientInteractionRequestCapabilityExists() {
		final PICInstructionsFieldsDTO instructionsFieldsDTO = new PICInstructionsFieldsDTO();
		instructionsFieldsDTO.setFields(ImmutableList.of("testField"));
		instructionsFieldsDTO.setStructuredErrorMessages(Collections.emptyList());

		final PICInstructionsFieldsDTO result = testee.getPICInstructionFields(PAYMENT_PROVIDER_CONFIGURATION_GUID, picFieldsRequestContextDTO);
		assertThat(result).isEqualToComparingFieldByField(instructionsFieldsDTO);
	}

	@Test
	public void getPICInstructionsShouldReturnPICInstructionsWhenPICClientInteractionRequestCapabilityExists() {
		final PICInstructionsRequest request = new PICInstructionsRequest();
		when(beanFactory.getPrototypeBean(PIC_INSTRUCTIONS_REQUEST, PICInstructionsRequest.class)).thenReturn(request);

		final Map<String, String> formData = ImmutableMap.of(INSTRUCTIONS_KEY, INSTRUCTIONS_VALUE);
		final PICInstructionsDTO result = testee.getPICInstructions(PAYMENT_PROVIDER_CONFIGURATION_GUID, formData, picRequestContextDTO);

		assertThat(request.getFormData()).isEqualTo(formData);
		assertThat(request.getPICRequestContextDTO()).isEqualTo(picRequestContextDTO);
		assertThat(result).isEqualToComparingFieldByField(instructions);
	}

	@Test
	public void testGetPICInstructionFieldsShouldReturnEmptyFieldsWhenPICClientInteractionRequestCapabilityNotFound() {
		final PaymentProvider noCapabilitiesPaymentProvider = mock(PaymentProvider.class);
		when(paymentProviderService.createProvider(paymentProviderConfiguration)).thenReturn(noCapabilitiesPaymentProvider);

		PICInstructionsFieldsDTO result = testee.getPICInstructionFields(PAYMENT_PROVIDER_CONFIGURATION_GUID, picFieldsRequestContextDTO);
		assertThat(result.getFields()).isEmpty();
	}

	@Test
	public void testGetPICInstructionsShouldReturnEmptyInstructionsExceptionWhenPICClientInteractionRequestCapabilityNotFound() {
		when(beanFactory.getPrototypeBean(PIC_INSTRUCTIONS_REQUEST, PICInstructionsRequest.class)).thenReturn(new PICInstructionsRequest());
		final PaymentProvider noCapabilitiesPaymentProvider = mock(PaymentProvider.class);
		when(paymentProviderService.createProvider(paymentProviderConfiguration)).thenReturn(noCapabilitiesPaymentProvider);

		final Map<String, String> formData = ImmutableMap.of(INSTRUCTIONS_KEY, INSTRUCTIONS_VALUE);
		final PICInstructionsDTO result = testee.getPICInstructions(PAYMENT_PROVIDER_CONFIGURATION_GUID, formData, picRequestContextDTO);

		assertThat(result.getCommunicationInstructions()).isEmpty();
		assertThat(result.getPayload()).isEmpty();
	}

	@Test
	public void testGetPICInstructionFieldsShouldThrowExceptionToFrontendWhenPICClientInteractionFails()
			throws PaymentInstrumentCreationFailedException {
		when(interactionCapability.getPaymentInstrumentCreationInstructionsFields(any(PICFieldsRequestContextDTO.class)))
				.thenThrow(new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.ERROR,
						StringUtils.EMPTY,
						TOKEN_DEBUG_MESSAGE,
						Collections.emptyMap()))));

		Throwable throwable = catchThrowable(() -> testee.getPICInstructionFields(PAYMENT_PROVIDER_CONFIGURATION_GUID, picFieldsRequestContextDTO));
		assertThat(throwable).isInstanceOf(EpStructureErrorMessageException.class);
		EpStructureErrorMessageException exception = (EpStructureErrorMessageException) throwable;
		StructuredErrorMessage errorMessage = exception.getStructuredErrorMessages().iterator().next();
		assertThat(errorMessage.getMessageId()).isEqualTo(PaymentsExceptionMessageId.PAYMENT_INSTRUMENT_CREATION_FAILED.getKey());
	}

	@Test
	public void testGetPICInstructionFieldsShouldThrowExceptionWithPluginSpecificId()
			throws PaymentInstrumentCreationFailedException {
		when(interactionCapability.getPaymentInstrumentCreationInstructionsFields(any(PICFieldsRequestContextDTO.class)))
				.thenThrow(new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.ERROR,
						PLUGIN_SPECIFIC_ERROR_ID,
						TOKEN_DEBUG_MESSAGE,
						Collections.emptyMap()))));

		Throwable throwable = catchThrowable(() -> testee.getPICInstructionFields(PAYMENT_PROVIDER_CONFIGURATION_GUID, picFieldsRequestContextDTO));
		assertThat(throwable).isInstanceOf(EpStructureErrorMessageException.class);
		EpStructureErrorMessageException exception = (EpStructureErrorMessageException) throwable;
		StructuredErrorMessage errorMessage = exception.getStructuredErrorMessages().iterator().next();
		assertThat(errorMessage.getMessageId()).isEqualTo(PLUGIN_SPECIFIC_ERROR_ID);
	}

	@Test
	public void testGetPICInstructionsShouldThrowExceptionToFrontendWhenPICClientInteractionFails()
			throws PaymentInstrumentCreationFailedException {
		final PICInstructionsRequest request = new PICInstructionsRequest();
		when(beanFactory.getPrototypeBean(PIC_INSTRUCTIONS_REQUEST, PICInstructionsRequest.class)).thenReturn(request);
		when(interactionCapability.getPaymentInstrumentCreationInstructions(request))
				.thenThrow(new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.ERROR,
						StringUtils.EMPTY,
						TOKEN_DEBUG_MESSAGE,
						Collections.emptyMap()))));

		final Map<String, String> formData = ImmutableMap.of(INSTRUCTIONS_KEY, INSTRUCTIONS_VALUE);
		Throwable throwable = catchThrowable(() -> testee.getPICInstructions(PAYMENT_PROVIDER_CONFIGURATION_GUID, formData, picRequestContextDTO));
		assertThat(throwable).isInstanceOf(EpStructureErrorMessageException.class);
		EpStructureErrorMessageException exception = (EpStructureErrorMessageException) throwable;
		StructuredErrorMessage errorMessage = exception.getStructuredErrorMessages().iterator().next();
		assertThat(errorMessage.getMessageId()).isEqualTo(PaymentsExceptionMessageId.PAYMENT_INSTRUMENT_CREATION_FAILED.getKey());
	}

	@Test
	public void testGetPICInstructionsShouldThrowExceptionToFrontendWithPluginSpecificId()
			throws PaymentInstrumentCreationFailedException {
		final PICInstructionsRequest request = new PICInstructionsRequest();
		when(beanFactory.getPrototypeBean(PIC_INSTRUCTIONS_REQUEST, PICInstructionsRequest.class)).thenReturn(request);
		when(interactionCapability.getPaymentInstrumentCreationInstructions(request))
				.thenThrow(new PaymentInstrumentCreationFailedException(Collections.singletonList(new ErrorMessage(StructuredMessageType.ERROR,
						PLUGIN_SPECIFIC_ERROR_ID,
						TOKEN_DEBUG_MESSAGE,
						Collections.emptyMap()))));

		final Map<String, String> formData = ImmutableMap.of(INSTRUCTIONS_KEY, INSTRUCTIONS_VALUE);
		Throwable throwable = catchThrowable(() -> testee.getPICInstructions(PAYMENT_PROVIDER_CONFIGURATION_GUID, formData, picRequestContextDTO));
		assertThat(throwable).isInstanceOf(EpStructureErrorMessageException.class);
		EpStructureErrorMessageException exception = (EpStructureErrorMessageException) throwable;
		StructuredErrorMessage errorMessage = exception.getStructuredErrorMessages().iterator().next();
		assertThat(errorMessage.getMessageId()).isEqualTo(PLUGIN_SPECIFIC_ERROR_ID);
	}

	@Test
	public void testGetPICFieldsWithoutNameFiled() {
		assertThat(testee.getPICFields(PAYMENT_PROVIDER_CONFIGURATION_GUID, picFieldsRequestContextDTO).getFields())
				.contains(PAYMENT_INSTRUMENT_NAME);
	}

	@Test
	public void testGetPICFieldsWithNameFiled() throws PaymentInstrumentCreationFailedException {
		when(instructionsCapability.getPaymentInstrumentCreationFields(any(PICFieldsRequestContextDTO.class)))
				.thenReturn(new PaymentInstrumentCreationFields(Collections.singletonList("anyField"), false));
		assertThat(testee.getPICFields(PAYMENT_PROVIDER_CONFIGURATION_GUID, picFieldsRequestContextDTO).getFields())
				.contains(PAYMENT_INSTRUMENT_NAME);
	}

	@Test
	public void testCreatePISetsRequestValues() {
		final PaymentInstrumentCreationRequest request = new PaymentInstrumentCreationRequest();
		when(beanFactory.getPrototypeBean(PIC_REQUEST, PaymentInstrumentCreationRequest.class)).thenReturn(request);

		final Map<String, String> formData = ImmutableMap.of("instrumentField", "someValue", PAYMENT_INSTRUMENT_NAME, PAYMENT_INSTRUMENT_NAME);
		String paymentInstrumentGuid = testee.createPI(PAYMENT_PROVIDER_CONFIGURATION_GUID, formData, picRequestContextDTO);

		assertEquals(PAYMENT_INSTRUMENT_GUID, paymentInstrumentGuid);
		assertThat(request.getFormData()).isEqualTo(formData);
		assertThat(request.getPICRequestContextDTO()).isEqualTo(picRequestContextDTO);
	}

	@Test
	public void testCreatePIFailureWhenNameIsEmpty() {
		when(beanFactory.getPrototypeBean(PIC_REQUEST, PaymentInstrumentCreationRequest.class)).thenReturn(new PaymentInstrumentCreationRequest());

		assertThatThrownBy(() -> testee.createPI(PAYMENT_PROVIDER_CONFIGURATION_GUID, Collections.emptyMap(), picRequestContextDTO))
				.isInstanceOf(PaymentsException.class)
				.hasMessageContaining(EMPTY_NAME_MESSAGE_ID);
	}

	@Test
	public void testCreatePISuccessAndNameIsNotEmpty() throws PaymentInstrumentCreationFailedException {
		when(beanFactory.getPrototypeBean(PIC_REQUEST, PaymentInstrumentCreationRequest.class)).thenReturn(new PaymentInstrumentCreationRequest());

		String paymentInstrumentGuid = testee.createPI(PAYMENT_PROVIDER_CONFIGURATION_GUID,
				ImmutableMap.of(PAYMENT_INSTRUMENT_NAME, PAYMENT_INSTRUMENT_NAME),
				picRequestContextDTO);

		verify(paymentInstrumentService).saveOrUpdate(paymentInstrument);
		assertEquals(PAYMENT_INSTRUMENT_GUID, paymentInstrumentGuid);
		verify(paymentInstrument).setName(PAYMENT_INSTRUMENT_NAME);
		verify(paymentInstrument).setPaymentProviderConfiguration(paymentProviderConfiguration);

		verify(paymentInstrumentData).setKey("key");
		verify(paymentInstrumentData).setData("data");
		verify(paymentInstrument).setPaymentInstrumentData(paymentInstrumentDataSetCaptor.capture());
		assertEquals(paymentInstrumentData, paymentInstrumentDataSetCaptor.getValue().iterator().next());
	}

	@Test(expected = RuntimeException.class)
	public void testCreatePIFailure() throws PaymentInstrumentCreationFailedException {
		when(beanFactory.getPrototypeBean(PIC_REQUEST, PaymentInstrumentCreationRequest.class)).thenReturn(new PaymentInstrumentCreationRequest());
		when(instructionsCapability.createPaymentInstrument(any())).thenThrow(new RuntimeException());

		testee.createPI(PAYMENT_PROVIDER_CONFIGURATION_GUID, ImmutableMap.of(PAYMENT_INSTRUMENT_NAME, PAYMENT_INSTRUMENT_NAME),
				picRequestContextDTO);
	}

}
