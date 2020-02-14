/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTO;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTOBuilder;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginsDTO;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentProviderWorkflow;

/**
 * Default implementation of {@link PaymentProviderWorkflow}.
 */
public class PaymentProviderWorkflowImpl implements PaymentProviderWorkflow {

	private final PaymentProviderService paymentProviderService;
	private final BeanFactory beanFactory;

	/**
	 * Constructor.
	 *
	 * @param paymentProviderService payment provider  service
	 * @param beanFactory            EP bean factory
	 */
	public PaymentProviderWorkflowImpl(final PaymentProviderService paymentProviderService, final BeanFactory beanFactory) {
		this.paymentProviderService = paymentProviderService;
		this.beanFactory = beanFactory;
	}

	@Override
	public PaymentProviderPluginsDTO findAll() {
		List<PaymentProviderPluginDTO> paymentProviderPluginDTOList = paymentProviderService.getPlugins().stream()
				.map(this::mapToDTO)
				.collect(Collectors.toList());
		return wrapPaymentProviderPluginDtoList(paymentProviderPluginDTOList);
	}

	/**
	 * Wrap plugin dto list into dto class.
	 *
	 * @param pluginDtoList plugin dto list
	 * @return dto
	 */
	protected PaymentProviderPluginsDTO wrapPaymentProviderPluginDtoList(final List<PaymentProviderPluginDTO> pluginDtoList) {
		final PaymentProviderPluginsDTO paymentProviderPluginsDTO = beanFactory.getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_PLUGINS_DTO, PaymentProviderPluginsDTO.class);
		paymentProviderPluginsDTO.setPaymentProviderPluginDTOs(pluginDtoList);
		return paymentProviderPluginsDTO;
	}

	/**
	 * Maps {@link PaymentProviderPlugin} bean to DTO.
	 *
	 * @param entity plugin bean
	 * @return dto
	 */
	protected PaymentProviderPluginDTO mapToDTO(final PaymentProviderPlugin entity) {
		if (entity == null) {
			return null;
		}
		return PaymentProviderPluginDTOBuilder.builder()
				.withPluginBeanName(entity.getUniquePluginId())
				.withPaymentVendorId(entity.getPaymentVendorId())
				.withPaymentMethodId(entity.getPaymentMethodId())
				.withConfigurationKeys(entity.getConfigurationKeys())
				.build(beanFactory);
	}

	protected PaymentProviderService getPaymentProviderService() {
		return paymentProviderService;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}
}
