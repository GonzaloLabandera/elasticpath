/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.profile.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentInstrumentForFormEntity;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.ProfilePaymentInstrumentFormIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.StorePaymentProviderConfigRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;

/**
 * Repository for Profile Payment Instrument Form.
 *
 * @param <E> extends PaymentInstrumentForFormEntity
 * @param <I> extends ProfilePaymentInstrumentFormIdentifier
 */
@Component
public class ProfilePaymentInstrumentFormEntityRepositoryImpl
		<E extends PaymentInstrumentForFormEntity, I extends ProfilePaymentInstrumentFormIdentifier>
		implements Repository<PaymentInstrumentForFormEntity, ProfilePaymentInstrumentFormIdentifier> {

	private PaymentInstrumentRepository paymentInstrumentRepository;
	private CustomerRepository customerRepository;
	private ResourceOperationContext resourceOperationContext;
	private StorePaymentProviderConfigRepository storePaymentProviderConfigRepository;

	@Override
	public Single<PaymentInstrumentForFormEntity> findOne(final ProfilePaymentInstrumentFormIdentifier identifier) {

		final String providerConfigGuid = identifier.getProfilePaymentMethod().getPaymentMethodId().getValue();

		final String customerGuid = resourceOperationContext.getUserIdentifier();

		return customerRepository.getCustomer(customerGuid)
				.map(Customer::isRegistered)
				.flatMap(isRegistered -> paymentInstrumentRepository.getPaymentInstrumentCreationFieldsForProviderConfigGuid(providerConfigGuid)
						.flatMap(fields -> storePaymentProviderConfigRepository.requiresBillingAddress(providerConfigGuid)
								.map(requiresBillingAddress ->
										buildPaymentInstrumentForFormEntity(fields.getFields(),
												isRegistered && fields.isSaveable(), requiresBillingAddress))));
	}

	@Reference
	public void setPaymentInstrumentRepository(final PaymentInstrumentRepository paymentInstrumentRepository) {
		this.paymentInstrumentRepository = paymentInstrumentRepository;
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setStorePaymentProviderConfigRepository(final StorePaymentProviderConfigRepository storePaymentProviderConfigRepository) {
		this.storePaymentProviderConfigRepository = storePaymentProviderConfigRepository;
	}
}
