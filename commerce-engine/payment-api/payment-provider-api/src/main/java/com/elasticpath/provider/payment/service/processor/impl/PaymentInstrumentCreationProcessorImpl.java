/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor.impl;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_INSTRUMENT;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_INSTRUMENT_DATA;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_INSTRUCTIONS_REQUEST;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_REQUEST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.common.dto.StructuredErrorMessageType;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationFields;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationRequest;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationResponse;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICClientInteractionRequestCapability;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructions;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsFields;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsRequest;
import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.dto.PICRequestContextDTO;
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
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTOBuilder;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTOBuilder;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTOBuilder;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentService;
import com.elasticpath.provider.payment.service.processor.PaymentInstrumentCreationProcessor;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;

/**
 * Default implementation of {@link PaymentInstrumentCreationProcessor}.
 */
public class PaymentInstrumentCreationProcessorImpl implements PaymentInstrumentCreationProcessor {

	private static final String NAME_FIELD = "display-name";

	private final PaymentProviderConfigurationService paymentProviderConfigurationService;
	private final PaymentProviderService paymentProviderService;
	private final BeanFactory beanFactory;
	private final PaymentInstrumentService paymentInstrumentService;

	/**
	 * Constructor.
	 *
	 * @param paymentProviderConfigurationService payment provider configuration service
	 * @param paymentProviderService              payment provider service
	 * @param paymentInstrumentService            payment instrument service
	 * @param beanFactory                         EP bean factory
	 */
	public PaymentInstrumentCreationProcessorImpl(final PaymentProviderConfigurationService paymentProviderConfigurationService,
												  final PaymentProviderService paymentProviderService,
												  final PaymentInstrumentService paymentInstrumentService,
												  final BeanFactory beanFactory) {
		this.paymentProviderConfigurationService = paymentProviderConfigurationService;
		this.paymentProviderService = paymentProviderService;
		this.paymentInstrumentService = paymentInstrumentService;
		this.beanFactory = beanFactory;
	}

	@Override
	public PICInstructionsFieldsDTO getPICInstructionFields(final String paymentProviderConfigurationGuid,
															final PICFieldsRequestContextDTO context) {
		return getPICClientInteractionRequestCapability(paymentProviderConfigurationGuid)
				.map(picClientInteractionRequestCapability -> createPicInstructionsFieldsDTO(context, picClientInteractionRequestCapability))
				.orElseGet(() -> PICInstructionsFieldsDTOBuilder.builder()
						.withFields(Collections.emptyList())
						.withStructuredErrorMessages(Collections.emptyList())
						.build(beanFactory));
	}

	@Override
	public PICInstructionsDTO getPICInstructions(final String paymentProviderConfigurationGuid,
												 final Map<String, String> instructionsMap,
												 final PICRequestContextDTO context) {
		final PICInstructionsRequest request = createInstructionsRequest(instructionsMap, context);
		return getPICClientInteractionRequestCapability(paymentProviderConfigurationGuid)
				.map(capability -> createPicInstructionsDTO(request, capability))
				.orElseGet(() -> PICInstructionsDTOBuilder.builder()
						.withCommunicationInstructions(Collections.emptyMap())
						.withPayload(Collections.emptyMap())
						.build(beanFactory));
	}

	/**
	 * Creates PIC instructions plugin capability request.
	 *
	 * @param instructionsForm instructions form values
	 * @param context          additional context
	 * @return request
	 */
	protected PICInstructionsRequest createInstructionsRequest(final Map<String, String> instructionsForm,
															   final PICRequestContextDTO context) {
		final PICInstructionsRequest request = beanFactory.getPrototypeBean(PIC_INSTRUCTIONS_REQUEST, PICInstructionsRequest.class);
		request.setFormData(instructionsForm);
		request.setPICRequestContextDTO(context);
		return request;
	}

	/**
	 * Finds {@link PICClientInteractionRequestCapability} by payment provider configuration id.
	 *
	 * @param configurationGuid payment provider configuration id
	 * @return optional plugin capability
	 */
	protected Optional<PICClientInteractionRequestCapability> getPICClientInteractionRequestCapability(final String configurationGuid) {
		final PaymentProviderConfiguration paymentProviderConfiguration = paymentProviderConfigurationService.findByGuid(configurationGuid);
		if (paymentProviderConfiguration == null) {
			throw new IllegalStateException("Configuration with GUID " + configurationGuid + " is missing");
		}
		final PaymentProvider paymentProvider = paymentProviderService.createProvider(paymentProviderConfiguration);
		return paymentProvider.getCapability(PICClientInteractionRequestCapability.class);
	}

	@Override
	public PaymentInstrumentCreationFieldsDTO getPICFields(final String paymentProviderConfigurationGuid,
														   final PICFieldsRequestContextDTO context) {
		final PaymentProviderConfiguration configuration = paymentProviderConfigurationService.findByGuid(paymentProviderConfigurationGuid);
		final PaymentProvider paymentProvider = paymentProviderService.createProvider(configuration);
		final Optional<PaymentInstrumentCreationFieldsDTO> paymentInstrumentCreationFields = paymentProvider
				.getCapability(PICCapability.class)
				.map(pICCapability -> createPaymentInstrumentCreationFieldsDTO(context, pICCapability));

		return paymentInstrumentCreationFields.orElse(PaymentInstrumentCreationFieldsDTOBuilder.builder()
				.withFields(Collections.singletonList(NAME_FIELD))
				.withBlockingFields(Collections.emptyList())
				.withIsSaveable(true)
				.build(beanFactory));
	}

	@Override
	public String createPI(final String paymentProviderConfigurationGuid,
						   final Map<String, String> instrumentMap,
						   final PICRequestContextDTO context) {
		final PaymentProviderConfiguration configuration = paymentProviderConfigurationService.findByGuid(paymentProviderConfigurationGuid);
		if (configuration == null) {
			throw new IllegalStateException("Configuration with GUID " + paymentProviderConfigurationGuid + " is missing");
		}

		final PaymentInstrumentCreationRequest createRequest = createPICRequest(instrumentMap, context);
		final String instrumentName = createRequest.getFormData().get(NAME_FIELD);
		final PaymentInstrument paymentInstrument = beanFactory.getPrototypeBean(PAYMENT_INSTRUMENT, PaymentInstrument.class);
		paymentInstrument.setPaymentProviderConfiguration(configuration);
		if (StringUtils.isEmpty(instrumentName)) {
			throw new PaymentsException(PaymentsExceptionMessageId.PAYMENT_INSTRUMENT_EMPTY_NAME, ImmutableMap.of("field-name", NAME_FIELD));
		}
		paymentInstrument.setName(instrumentName);

		final PaymentProvider paymentProvider = paymentProviderService.createProvider(configuration);
		final Optional<PaymentInstrumentCreationResponse> response = paymentProvider.getCapability(PICCapability.class)
				.map(picCapability -> createPaymentInstrument(createRequest, picCapability));

		final Set<PaymentInstrumentData> paymentInstrumentData = response.map(PaymentInstrumentCreationResponse::getDetails)
				.orElse(Collections.emptyMap())
				.entrySet()
				.stream()
				.map(this::createPaymentInstrumentData)
				.collect(Collectors.toSet());
		paymentInstrument.setPaymentInstrumentData(paymentInstrumentData);
		paymentInstrument.setBillingAddressGuid(getBillingAddressGuid(createRequest));
		paymentInstrument.setSingleReservePerPI(paymentProvider.isSingleReservePerPI());

		paymentInstrumentService.saveOrUpdate(paymentInstrument);

		return paymentInstrument.getGuid();
	}

	/**
	 * Creates payment instrument creation plugin capability request.
	 *
	 * @param instrumentMap instrument field values
	 * @param context       additional context
	 * @return request
	 */
	protected PaymentInstrumentCreationRequest createPICRequest(final Map<String, String> instrumentMap, final PICRequestContextDTO context) {
		final PaymentInstrumentCreationRequest createRequest = beanFactory.getPrototypeBean(PIC_REQUEST, PaymentInstrumentCreationRequest.class);
		createRequest.setFormData(instrumentMap);
		createRequest.setPICRequestContextDTO(context);
		return createRequest;
	}

	/**
	 * Creates {@link PaymentInstrumentData} entity.
	 *
	 * @param entry key-value pair
	 * @return entity
	 */
	protected PaymentInstrumentData createPaymentInstrumentData(final Map.Entry<String, String> entry) {
		PaymentInstrumentData data = beanFactory.getPrototypeBean(PAYMENT_INSTRUMENT_DATA, PaymentInstrumentData.class);
		data.setKey(entry.getKey());
		data.setData(entry.getValue());
		return data;
	}

	/**
	 * Converts {@link PICInstructions} Plugin API object to {@link PICInstructionsDTO} Payment API namespace.
	 *
	 * @param picInstructions PIC instructions DTO of Plugin API
	 * @return PIC instructions DTO of Payment API
	 */
	protected PICInstructionsDTO mapToPICInstructionsDTO(final PICInstructions picInstructions) {
		return PICInstructionsDTOBuilder.builder()
				.withCommunicationInstructions(picInstructions.getCommunicationInstructions())
				.withPayload(picInstructions.getPayload())
				.build(beanFactory);
	}

	/**
	 * Converts {@link PICInstructionsFields} Plugin API object to {@link PICInstructionsFieldsDTO} Payment API namespace.
	 *
	 * @param picInstructionsFields PIC instructions fields DTO of Plugin API
	 * @return PIC instructions fields DTO of Payment API
	 */
	protected PICInstructionsFieldsDTO mapToPICInstructionFieldsDTO(final PICInstructionsFields picInstructionsFields) {
		return PICInstructionsFieldsDTOBuilder.builder()
				.withFields(picInstructionsFields.getFields())
				.withStructuredErrorMessages(Collections.emptyList())
				.build(beanFactory);
	}

	private String getBillingAddressGuid(final PaymentInstrumentCreationRequest paymentInstrumentCreationRequest) {
		return Optional.ofNullable(paymentInstrumentCreationRequest.getPICRequestContextDTO().getAddressDTO())
				.map(AddressDTO::getGuid)
				.orElse(null);
	}

	private PICInstructionsDTO createPicInstructionsDTO(final PICInstructionsRequest request,
														final PICClientInteractionRequestCapability capability) {
		try {
			return mapToPICInstructionsDTO(capability.getPaymentInstrumentCreationInstructions(request));
		} catch (PaymentInstrumentCreationFailedException e) {
			final List<StructuredErrorMessage> structuredErrorMessageList = createStructuredErrorMessageList(e);
			throw new PaymentsException(PaymentsExceptionMessageId.PAYMENT_INSTRUMENT_CREATION_FAILED, structuredErrorMessageList, e);
		}
	}

	private PaymentInstrumentCreationResponse createPaymentInstrument(final PaymentInstrumentCreationRequest createRequest,
																	  final PICCapability picCapability) {
		try {
			return picCapability.createPaymentInstrument(createRequest);
		} catch (PaymentInstrumentCreationFailedException e) {
			final List<StructuredErrorMessage> structuredErrorMessageList = createStructuredErrorMessageList(e);
			throw new PaymentsException(PaymentsExceptionMessageId.PAYMENT_INSTRUMENT_CREATION_FAILED, structuredErrorMessageList, e);
		}
	}

	private PICInstructionsFieldsDTO createPicInstructionsFieldsDTO(final PICFieldsRequestContextDTO context,
																	final PICClientInteractionRequestCapability capability) {
		PICInstructionsFieldsDTO dto;
		try {
			dto = mapToPICInstructionFieldsDTO(capability.getPaymentInstrumentCreationInstructionsFields(context));
		} catch (PaymentInstrumentCreationFailedException e) {
			dto = processInstructionException(e);
		}
		return dto;
	}

	private PaymentInstrumentCreationFieldsDTO createPaymentInstrumentCreationFieldsDTO(final PICFieldsRequestContextDTO context,
																						final PICCapability pICCapability) {
		PaymentInstrumentCreationFieldsDTO dto;
		try {
			dto = createInstrumentFieldsDTO(pICCapability.getPaymentInstrumentCreationFields(context));
		} catch (PaymentInstrumentCreationFailedException e) {
			dto = processInstrumentException(e);
		}
		return dto;
	}

	private PaymentInstrumentCreationFieldsDTO createInstrumentFieldsDTO(final PaymentInstrumentCreationFields creationFields) {
		List<String> picFields = creationFields.getFields();
		if (!picFields.contains(NAME_FIELD)) {
			picFields = new ArrayList<>(picFields);
			picFields.add(NAME_FIELD);
		}

		return PaymentInstrumentCreationFieldsDTOBuilder.builder()
				.withFields(picFields)
				.withBlockingFields(Collections.emptyList())
				.withIsSaveable(creationFields.isSaveable())
				.build(beanFactory);
	}

	private PaymentInstrumentCreationFieldsDTO processInstrumentException(final PaymentInstrumentCreationFailedException exception) {
		if (!exception.getStructuredErrorMessages().isEmpty()
				&& exception.getStructuredErrorMessages().stream().allMatch(message -> StructuredMessageType.NEEDINFO.equals(message.getType()))) {

			return PaymentInstrumentCreationFieldsDTOBuilder.builder()
					.withFields(Collections.singletonList(NAME_FIELD))
					.withBlockingFields(createStructuredErrorMessageList(exception))
					.withIsSaveable(true)
					.build(beanFactory);
		}

		final List<StructuredErrorMessage> structuredErrorMessageList = createStructuredErrorMessageList(exception);
		throw new PaymentsException(PaymentsExceptionMessageId.PAYMENT_INSTRUMENT_CREATION_FAILED, structuredErrorMessageList, exception);
	}

	private PICInstructionsFieldsDTO processInstructionException(final PaymentInstrumentCreationFailedException exception) {
		if (!exception.getStructuredErrorMessages().isEmpty()
				&& exception.getStructuredErrorMessages().stream().allMatch(message -> StructuredMessageType.NEEDINFO.equals(message.getType()))) {

			return PICInstructionsFieldsDTOBuilder.builder()
					.withFields(Collections.singletonList(NAME_FIELD))
					.withStructuredErrorMessages(createStructuredErrorMessageList(exception))
					.build(beanFactory);
		}

		final List<StructuredErrorMessage> structuredErrorMessageList = createStructuredErrorMessageList(exception);
		throw new PaymentsException(PaymentsExceptionMessageId.PAYMENT_INSTRUMENT_CREATION_FAILED, structuredErrorMessageList, exception);
	}

	private List<StructuredErrorMessage> createStructuredErrorMessageList(final PaymentInstrumentCreationFailedException exception) {
		return exception.getStructuredErrorMessages()
				.stream()
				.map(message -> new StructuredErrorMessage(convertType(message.getType()),
						StringUtils.defaultIfBlank(message.getMessageId(), PaymentsExceptionMessageId.PAYMENT_INSTRUMENT_CREATION_FAILED.getKey()),
						message.getDebugMessage(),
						message.getData()))
				.collect(Collectors.toList());
	}

	private StructuredErrorMessageType convertType(final StructuredMessageType type) {
		final StructuredErrorMessageType structuredErrorMessageType;
		switch (type) {
			case NEEDINFO:
			structuredErrorMessageType = StructuredErrorMessageType.NEEDINFO;
			break;
			case WARNING:
			structuredErrorMessageType = StructuredErrorMessageType.WARNING;
			break;
			case INFORMATION:
			structuredErrorMessageType = StructuredErrorMessageType.INFORMATION;
			break;
			case PROMOTION:
			structuredErrorMessageType = StructuredErrorMessageType.PROMOTION;
			break;
			default:
				structuredErrorMessageType = StructuredErrorMessageType.ERROR;
				break;
		}
		return structuredErrorMessageType;
	}

	protected PaymentProviderConfigurationService getPaymentProviderConfigurationService() {
		return paymentProviderConfigurationService;
	}

	protected PaymentProviderService getPaymentProviderService() {
		return paymentProviderService;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	protected PaymentInstrumentService getPaymentInstrumentService() {
		return paymentInstrumentService;
	}
}
