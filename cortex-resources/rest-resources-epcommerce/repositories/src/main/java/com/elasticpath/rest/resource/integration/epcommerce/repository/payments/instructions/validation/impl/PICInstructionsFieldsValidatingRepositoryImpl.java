/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.validation.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPICFieldsRequestContext;

import java.util.Collection;
import java.util.Currency;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.validation.PICInstructionsFieldsValidatingRepository;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;

/**
 * Implements @{link PICInstructionsFieldsValidatingRepository}.
 */
@Singleton
@Named("paymentInstructionFieldsValidationService")
public class PICInstructionsFieldsValidatingRepositoryImpl implements PICInstructionsFieldsValidatingRepository {
	private final CustomerService customerService;
	private final StorePaymentProviderConfigService storePaymentProviderConfigService;
	private final OrderPaymentApiService orderPaymentApiService;

	/**
	 * Constructor.
	 *
	 * @param customerService        customer service.
	 * @param configService          store payment provider configuration service.
	 * @param orderPaymentApiService order payment API service.
	 */
	@Inject
	public PICInstructionsFieldsValidatingRepositoryImpl(
			@Named("customerService") final CustomerService customerService,
			@Named("storePaymentProviderConfigService") final StorePaymentProviderConfigService configService,
			@Named("orderPaymentApiService") final OrderPaymentApiService orderPaymentApiService) {
		this.customerService = customerService;
		this.storePaymentProviderConfigService = configService;
		this.orderPaymentApiService = orderPaymentApiService;
	}

	@Override
	public Collection<StructuredErrorMessage> validate(final String methodId,
													   final Currency currency,
													   final Locale locale,
													   final String userId) {
		final Customer customer = customerService.findByGuid(userId);
		final StorePaymentProviderConfig config = storePaymentProviderConfigService.findByGuid(methodId);

		return orderPaymentApiService.getPICInstructionsFields(config.getPaymentProviderConfigGuid(),
				buildPICFieldsRequestContext(locale, currency, customer))
				.getStructuredErrorMessages();
	}
}
