/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.DefaultPaymentInstrumentLinkRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;

/**
 * The implementation of {@link DefaultPaymentInstrumentLinkRepository} related operations.
 */
@Singleton
@Named("defaultPaymentInstrumentLinkRepository")
public class DefaultPaymentInstrumentLinkRepositoryImpl implements DefaultPaymentInstrumentLinkRepository {

	@Inject
	@Named("resourceOperationContext")
	private ResourceOperationContext resourceOperationContext;

	@Inject
	@Named("customerRepository")
	private CustomerRepository customerRepository;

	@Inject
	@Named("filteredPaymentInstrumentService")
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;

	@Inject
	@Named("reactiveAdapter")
	private ReactiveAdapter reactiveAdapter;

	@Override
	public Observable<PaymentInstrumentIdentifier> getDefaultPaymentInstrumentIdentifier(final PaymentInstrumentsIdentifier identifier) {
		return getPaymentInstrumentIdentifier(identifier, resourceOperationContext.getUserIdentifier());
	}

	@Override
	public Observable<PaymentInstrumentIdentifier> getContextAwareDefaultPaymentInstrumentIdentifier(final PaymentInstrumentsIdentifier identifier) {
		final String customerGuid = customerRepository.getCustomerGuid(resourceOperationContext.getUserIdentifier(),
				resourceOperationContext.getSubject());

		return getPaymentInstrumentIdentifier(identifier, customerGuid);
	}

	@Override
	public Observable<AccountPaymentInstrumentIdentifier> getAccountDefaultPaymentInstrumentIdentifier(
			final AccountPaymentInstrumentsIdentifier identifier) {
		String accountGuid = identifier.getAccount().getAccountId().getValue();
		String storeCode = identifier.getAccount().getAccounts().getScope().getValue();

		return customerRepository.getCustomer(accountGuid)
				.flatMapMaybe(account ->
						reactiveAdapter.fromServiceAsMaybe(() -> filteredPaymentInstrumentService
								.findDefaultPaymentInstrumentForCustomerAndStore(account, storeCode)))
				.flatMapObservable(defaultInstrument -> Observable.just(AccountPaymentInstrumentIdentifier.builder()
						.withAccountPaymentInstruments(identifier)
						.withAccountPaymentInstrumentId(StringIdentifier.of(defaultInstrument.getGuid()))
						.build()))
				.switchIfEmpty(Observable.empty());
	}

	private Observable<PaymentInstrumentIdentifier> getPaymentInstrumentIdentifier(final PaymentInstrumentsIdentifier identifier,
																				   final String customerGuid) {
		final String storeCode = identifier.getScope().getValue();

		return customerRepository.getCustomer(customerGuid)
				.flatMapMaybe(customer -> reactiveAdapter.fromServiceAsMaybe(() ->
						filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(customer, storeCode)))
				.flatMapObservable(defaultInstrument -> Observable.just(PaymentInstrumentIdentifier.builder()
						.withPaymentInstrumentId(StringIdentifier.of(defaultInstrument.getGuid()))
						.withPaymentInstruments(identifier)
						.build()))
				.switchIfEmpty(Observable.empty());
	}
}
