/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.history.PaymentHistory;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;
import com.elasticpath.provider.payment.service.provider.PaymentProviderService;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;

/**
 * Abstract processor of Payment API workflow endpoints.
 */
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod"})
public abstract class AbstractProcessor {
	private final PaymentHistory paymentHistory;
	private final MoneyDtoCalculator moneyDtoCalculator;
	private final PaymentAPIWorkflow paymentAPIWorkflow;

	private final BeanFactory beanFactory;

	private final PaymentProviderConfigurationService paymentProviderConfigurationService;
	private final PaymentProviderService paymentProviderService;

	/**
	 * Constructor.
	 *
	 * @param paymentProviderConfigurationService payment provider configuration service
	 * @param paymentProviderService              payment provider service
	 * @param paymentHistory                      payment history helper service
	 * @param moneyDtoCalculator                  calculator of mathematical operation with MoneyDto
	 * @param paymentAPIWorkflow                  payment API workflow facade
	 * @param beanFactory                         EP bean factory
	 */
	public AbstractProcessor(final PaymentProviderConfigurationService paymentProviderConfigurationService,
							 final PaymentProviderService paymentProviderService,
							 final PaymentHistory paymentHistory,
							 final MoneyDtoCalculator moneyDtoCalculator,
							 final PaymentAPIWorkflow paymentAPIWorkflow,
							 final BeanFactory beanFactory) {
		this.paymentProviderConfigurationService = paymentProviderConfigurationService;
		this.paymentProviderService = paymentProviderService;
		this.paymentHistory = paymentHistory;
		this.moneyDtoCalculator = moneyDtoCalculator;
		this.paymentAPIWorkflow = paymentAPIWorkflow;
		this.beanFactory = beanFactory;
	}

	/**
	 * Loads a payment provider, which is a combination of configuration and associated plugin.
	 *
	 * @param configurationGuid payment provider configuration guid
	 * @return new payment provider instance
	 */
	protected PaymentProvider getPaymentProvider(final String configurationGuid) {
		final PaymentProviderConfiguration paymentProviderConfiguration = paymentProviderConfigurationService.findByGuid(configurationGuid);
		if (paymentProviderConfiguration == null) {
			throw new IllegalStateException("Configuration with GUID " + configurationGuid + " is missing");
		}
		return paymentProviderService.createProvider(paymentProviderConfiguration);
	}

	/**
	 * Gets payment history analyzer.
	 *
	 * @return {@link PaymentHistory}
	 */
	protected PaymentHistory getPaymentHistory() {
		return paymentHistory;
	}

	/**
	 * Gets money DTO calculator.
	 *
	 * @return {@link MoneyDtoCalculator}
	 */
	protected MoneyDtoCalculator getMoneyDtoCalculator() {
		return moneyDtoCalculator;
	}

	/**
	 * Gets payment API workflow facade.
	 *
	 * @return {@link PaymentAPIWorkflow}
	 */
	protected PaymentAPIWorkflow getPaymentAPIWorkflow() {
		return paymentAPIWorkflow;
	}

	/**
	 * Gets EP bean factory.
	 *
	 * @return bean factory
	 */
	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}
}
