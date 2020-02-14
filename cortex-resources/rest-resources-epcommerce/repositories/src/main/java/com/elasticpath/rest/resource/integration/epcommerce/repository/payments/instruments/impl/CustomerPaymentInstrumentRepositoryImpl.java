/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.orderpaymentapi.CustomerPaymentInstrumentService;

/**
 * The facade for operations with customer payment instruments.
 */
@Singleton
@Named("customerPaymentInstrumentRepository")
public class CustomerPaymentInstrumentRepositoryImpl implements CustomerPaymentInstrumentRepository {

	private static final String UNABLE_TO_SAVE_MESSAGE = "Unable to save customer payment instrument.";

	private final CustomerPaymentInstrumentService customerPaymentInstrumentService;
	private ReactiveAdapter reactiveAdapter;

	/**
	 * Constructor.
	 *
	 * @param customerPaymentInstrumentService customerPaymentInstrumentService
	 * @param reactiveAdapter                  reactiveAdapter
	 */
	@Inject
	public CustomerPaymentInstrumentRepositoryImpl(
			@Named("customerPaymentInstrumentService") final CustomerPaymentInstrumentService customerPaymentInstrumentService,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter) {
		this.customerPaymentInstrumentService = customerPaymentInstrumentService;
		this.reactiveAdapter = reactiveAdapter;
	}

	@Override
	public Single<CustomerPaymentInstrument> saveOrUpdate(final CustomerPaymentInstrument customerPaymentInstrument) {
		return reactiveAdapter.fromServiceAsSingle(() ->
				customerPaymentInstrumentService.saveOrUpdate(customerPaymentInstrument), UNABLE_TO_SAVE_MESSAGE)
				.onErrorResumeNext(Single.error(ResourceOperationFailure.serverError(UNABLE_TO_SAVE_MESSAGE)));
	}

	@Override
	@CacheResult
	public Single<CustomerPaymentInstrument> findByGuid(final String guid) {
		return reactiveAdapter.fromServiceAsSingle(() -> customerPaymentInstrumentService.findByGuid(guid),
				"Customer payment instrument not found for guid " + guid + ".");
	}

	@Override
	public Completable remove(final CustomerPaymentInstrument customerPaymentInstrument) {
		return reactiveAdapter.fromServiceAsCompletable(() -> customerPaymentInstrumentService.remove(customerPaymentInstrument));
	}

	@Override
	public Observable<CustomerPaymentInstrument> findByCustomer(final Customer customer) {
		return reactiveAdapter.fromService(() -> customerPaymentInstrumentService.findByCustomer(customer))
				.flatMap(Observable::fromIterable);
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	protected CustomerPaymentInstrumentService getCustomerPaymentInstrumentService() {
		return customerPaymentInstrumentService;
	}

	public ReactiveAdapter getReactiveAdapter() {
		return reactiveAdapter;
	}
}
