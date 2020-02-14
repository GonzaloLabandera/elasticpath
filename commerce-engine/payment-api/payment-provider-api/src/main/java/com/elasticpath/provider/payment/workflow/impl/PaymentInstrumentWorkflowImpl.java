/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.workflow.impl;

import java.util.stream.Collectors;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.provider.payment.domain.PaymentInstrument;
import com.elasticpath.provider.payment.domain.PaymentInstrumentData;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationData;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTOBuilder;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentService;
import com.elasticpath.provider.payment.workflow.PaymentInstrumentWorkflow;

/**
 * Default implementation of {@link PaymentInstrumentWorkflow}.
 */
public class PaymentInstrumentWorkflowImpl implements PaymentInstrumentWorkflow {

	private final PaymentInstrumentService paymentInstrumentService;
	private final BeanFactory beanFactory;

	/**
	 * Constructor.
	 *
	 * @param paymentInstrumentService payment instrument service
	 * @param beanFactory              EP bean factory
	 */
	public PaymentInstrumentWorkflowImpl(final PaymentInstrumentService paymentInstrumentService, final BeanFactory beanFactory) {
		this.paymentInstrumentService = paymentInstrumentService;
		this.beanFactory = beanFactory;
	}

	@Override
	public PaymentInstrumentDTO findByGuid(final String guid) {
		return mapToDTO(paymentInstrumentService.findByGuid(guid));
	}

	/**
	 * Maps {@link PaymentInstrument} entity to DTO.
	 *
	 * @param entity entity
	 * @return dto
	 */
	protected PaymentInstrumentDTO mapToDTO(final PaymentInstrument entity) {
		if (entity == null) {
			return null;
		}
		PaymentProviderConfiguration providerConfiguration = entity.getPaymentProviderConfiguration();
		return PaymentInstrumentDTOBuilder.builder()
				.withGuid(entity.getGuid())
				.withName(entity.getName())
				.withData(entity.getPaymentInstrumentData().stream()
						.collect(Collectors.toMap(PaymentInstrumentData::getKey, PaymentInstrumentData::getData)))
				.withPaymentProviderConfigurationGuid(providerConfiguration.getGuid())
				.withPaymentProviderConfiguration(providerConfiguration.getPaymentConfigurationData().stream()
						.collect(Collectors.toMap(PaymentProviderConfigurationData::getKey, PaymentProviderConfigurationData::getData)))
				.withBillingAddressGuid(entity.getBillingAddressGuid())
				.withSupportingMultiCharges(entity.isSupportingMultiCharges())
				.withSingleReservePerPI(entity.isSingleReservePerPI())
				.build(beanFactory);
	}

	protected PaymentInstrumentService getPaymentInstrumentService() {
		return paymentInstrumentService;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}
}
