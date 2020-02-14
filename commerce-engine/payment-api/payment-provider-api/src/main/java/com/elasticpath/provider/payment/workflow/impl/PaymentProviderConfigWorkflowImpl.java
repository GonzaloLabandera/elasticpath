/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow.impl;

import static com.elasticpath.provider.payment.PaymentLocalizedPropertiesImpl.PAYMENT_LOCALIZED_PROPERTY_NAME;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION_DATA;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.provider.payment.PaymentLocalizedProperties;
import com.elasticpath.provider.payment.PaymentLocalizedPropertyValue;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationData;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTOBuilder;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationBuilder;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.workflow.PaymentProviderConfigWorkflow;

/**
 * Default implementation of {@link PaymentProviderConfigWorkflow}.
 */
public class PaymentProviderConfigWorkflowImpl implements PaymentProviderConfigWorkflow {

	private static final String SEPARATOR = "_";

	private final PaymentProviderConfigurationService paymentProviderConfigurationService;
	private final BeanFactory beanFactory;

	/**
	 * Constructor.
	 *
	 * @param paymentProviderConfigurationService payment provider configuration service
	 * @param beanFactory                         EP bean factory
	 */
	public PaymentProviderConfigWorkflowImpl(
			final PaymentProviderConfigurationService paymentProviderConfigurationService,
			final BeanFactory beanFactory) {
		this.paymentProviderConfigurationService = paymentProviderConfigurationService;
		this.beanFactory = beanFactory;
	}

	@Override
	public List<PaymentProviderConfigDTO> findAll() {
		return paymentProviderConfigurationService.findAll().stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());
	}

	@Override
	public List<PaymentProviderConfigDTO> findByStatus(final PaymentProviderConfigurationStatus status) {
		return paymentProviderConfigurationService.findByStatus(status).stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());
	}

	@Override
	public PaymentProviderConfigDTO findByGuid(final String guid) {
		return mapToDTO(paymentProviderConfigurationService.findByGuid(guid));
	}

	@Override
	public List<PaymentProviderConfigDTO> findByGuids(final List<String> guids) {
		return paymentProviderConfigurationService.findByGuids(guids).stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());
	}

	@Override
	public PaymentProviderConfigDTO saveOrUpdate(
			final PaymentProviderConfigDTO paymentProviderConfigurationDTO) {
		PaymentProviderConfiguration paymentProviderConfiguration = null;

		if (paymentProviderConfigurationDTO != null) {
			paymentProviderConfiguration =
					paymentProviderConfigurationService.findByGuid(paymentProviderConfigurationDTO.getGuid());
		}

		if (paymentProviderConfiguration == null) {
			paymentProviderConfiguration = beanFactory.getPrototypeBean(
					PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION, PaymentProviderConfiguration.class);
		}

		return mapToDTO(paymentProviderConfigurationService.saveOrUpdate(
				mapToDomain(paymentProviderConfiguration, paymentProviderConfigurationDTO)));
	}

	/**
	 * Maps {@link PaymentProviderConfiguration} entity to DTO.
	 *
	 * @param entity entity
	 * @return dto
	 */
	protected PaymentProviderConfigDTO mapToDTO(final PaymentProviderConfiguration entity) {
		if (entity == null) {
			return null;
		}
		return PaymentProviderConfigDTOBuilder.builder()
				.withGuid(entity.getGuid())
				.withConfigurationName(entity.getConfigurationName())
				.withDefaultDisplayName(entity.getDefaultDisplayName())
				.withPaymentProviderPluginBeanName(entity.getPaymentProviderPluginId())
				.withStatus(entity.getStatus())
				.withPaymentConfigurationData(entity.getPaymentConfigurationData().stream()
						.collect(Collectors.toMap(PaymentProviderConfigurationData::getKey, PaymentProviderConfigurationData::getData)))
				.withLocalizedNames(convertToMap(entity.getPaymentLocalizedProperties()))
				.build(beanFactory);
	}

	/**
	 * Maps {@link PaymentProviderConfigDTO} to {@link PaymentProviderConfiguration} entity.
	 *
	 * @param domain empty entity instance
	 * @param dto    dto
	 * @return populated entity
	 */
	protected PaymentProviderConfiguration mapToDomain(final PaymentProviderConfiguration domain, final PaymentProviderConfigDTO dto) {
		if (dto == null) {
			return null;
		}

		return PaymentProviderConfigurationBuilder.builder()
				.withGuid(dto.getGuid() == null ? domain.getGuid() : dto.getGuid())
				.withConfigurationName(dto.getConfigurationName())
				.withDefaultDisplayName(dto.getDefaultDisplayName())
				.withPaymentProviderPluginId(dto.getPaymentProviderPluginBeanName())
				.withStatus(dto.getStatus())
				.withPaymentConfigurationData(dto.getPaymentConfigurationData().keySet().stream()
						.map(key -> createPaymentProviderConfigurationData(key, dto.getPaymentConfigurationData().get(key)))
						.collect(Collectors.toSet()))
				.withPaymentLocalizedProperties(convertToPaymentLocalizedProperties(dto.getLocalizedNames()))
				.build(domain);
	}

	/**
	 * Converts {@link PaymentProviderConfigDTO} Payment API dto to {@link PaymentProviderConfigurationData} entity.
	 *
	 * @param key  config key
	 * @param data config data
	 * @return entity
	 */
	protected PaymentProviderConfigurationData createPaymentProviderConfigurationData(final String key, final String data) {
		final PaymentProviderConfigurationData configData = beanFactory.getPrototypeBean(
				PAYMENT_PROVIDER_CONFIGURATION_DATA, PaymentProviderConfigurationData.class);
		configData.setKey(key);
		configData.setData(data);
		return configData;
	}

	/**
	 * Converts {@link PaymentLocalizedProperties} entity to map.
	 *
	 * @param paymentLocalizedProperties entity
	 * @return map of strings
	 */
	protected Map<String, String> convertToMap(final PaymentLocalizedProperties paymentLocalizedProperties) {
		return paymentLocalizedProperties.getPaymentLocalizedPropertiesMap().entrySet().stream()
				.collect(Collectors.toMap(entry -> extractLanguage(entry.getKey()), entry -> entry.getValue().getValue()));
	}

	/**
	 * Extracts language from localized property map key.
	 *
	 * @param source localized property map key
	 * @return language code
	 */
	protected String extractLanguage(final String source) {
		return source.split(SEPARATOR, 2)[1];
	}

	/**
	 * Converts map of strings to {@link PaymentLocalizedProperties} entity.
	 *
	 * @param localizedNames map of localized names
	 * @return entity
	 */
	protected PaymentLocalizedProperties convertToPaymentLocalizedProperties(final Map<String, String> localizedNames) {
		final PaymentLocalizedProperties paymentLocalizedProperties = beanFactory.getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_LOCALIZED_PROPERTIES, PaymentLocalizedProperties.class);

		paymentLocalizedProperties.setPaymentLocalizedPropertiesMap(createPaymentLocalizedPropertiesMap(localizedNames),
				PaymentProviderApiContextIdNames.PAYMENT_LOCALIZED_PROPERTY_VALUE);

		return paymentLocalizedProperties;
	}

	/**
	 * Creates localized properties map from map of strings.
	 *
	 * @param localizedNames map of strings
	 * @return localized properties map
	 */
	protected Map<String, PaymentLocalizedPropertyValue> createPaymentLocalizedPropertiesMap(final Map<String, String> localizedNames) {
		return localizedNames.entrySet().stream()
				.collect(Collectors.toMap(entry -> createKeyForPaymentLocalizedPropertiesMap(entry.getKey()),
						entry -> convertToPaymentLocalizedPropertyValue(entry.getKey(), entry.getValue())));
	}

	/**
	 * Encodes language code into a localized properties map key.
	 *
	 * @param language language code
	 * @return localized properties map key
	 */
	protected String createKeyForPaymentLocalizedPropertiesMap(final String language) {
		return PAYMENT_LOCALIZED_PROPERTY_NAME + SEPARATOR + language;
	}

	/**
	 * Creates {@link PaymentLocalizedPropertyValue} entity from language and corresponding text.
	 *
	 * @param language language code
	 * @param name     text value
	 * @return entity
	 */
	protected PaymentLocalizedPropertyValue convertToPaymentLocalizedPropertyValue(final String language, final String name) {
		final PaymentLocalizedPropertyValue paymentLocalizedPropertyValue = beanFactory.getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_LOCALIZED_PROPERTY_VALUE, PaymentLocalizedPropertyValue.class);

		paymentLocalizedPropertyValue.setPaymentLocalizedPropertyKey(PAYMENT_LOCALIZED_PROPERTY_NAME + SEPARATOR + language);
		paymentLocalizedPropertyValue.setValue(name);

		return paymentLocalizedPropertyValue;
	}

	protected PaymentProviderConfigurationService getPaymentProviderConfigurationService() {
		return paymentProviderConfigurationService;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}
}
