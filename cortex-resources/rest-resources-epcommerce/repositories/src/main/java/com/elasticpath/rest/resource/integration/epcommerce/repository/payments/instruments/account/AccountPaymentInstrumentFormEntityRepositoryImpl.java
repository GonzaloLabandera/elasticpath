/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.account;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentInstrumentForFormEntity;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
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
public class AccountPaymentInstrumentFormEntityRepositoryImpl
		<E extends PaymentInstrumentForFormEntity, I extends AccountPaymentInstrumentFormIdentifier>
		implements Repository<PaymentInstrumentForFormEntity, AccountPaymentInstrumentFormIdentifier> {

	private PaymentInstrumentRepository paymentInstrumentRepository;
	private CustomerRepository customerRepository;
	private StorePaymentProviderConfigRepository storePaymentProviderConfigRepository;

	@Override
	public Single<PaymentInstrumentForFormEntity> findOne(final AccountPaymentInstrumentFormIdentifier identifier) {

		final String providerConfigGuid = identifier.getAccountPaymentMethod().getAccountPaymentMethodId().getValue();

		final String accountGuid = paymentInstrumentRepository.getAccountIdFromResourceOperationContext().getValue();

		return customerRepository.getCustomer(accountGuid)
				.map(Customer::isRegistered)
				.flatMap(isRegistered -> paymentInstrumentRepository
						.getAccountPaymentInstrumentCreationFieldsForProviderConfigGuid(providerConfigGuid, accountGuid)
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
	public void setStorePaymentProviderConfigRepository(final StorePaymentProviderConfigRepository storePaymentProviderConfigRepository) {
		this.storePaymentProviderConfigRepository = storePaymentProviderConfigRepository;
	}
}
