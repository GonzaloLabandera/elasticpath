/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerDefaultPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.orderpaymentapi.CustomerDefaultPaymentInstrumentService;

/**
 * The facade for operations with customers' default payment instruments.
 */
@Singleton
@Named("customerDefaultPaymentInstrumentRepository")
public class CustomerDefaultPaymentInstrumentRepositoryImpl implements CustomerDefaultPaymentInstrumentRepository {

	private final CustomerDefaultPaymentInstrumentService customerDefaultPaymentInstrumentService;
	private ReactiveAdapter reactiveAdapter;

	/**
	 * Constructor.
	 *
	 * @param customerDefaultPaymentInstrumentService customerDefaultPaymentInstrumentService
	 * @param reactiveAdapter                         reactiveAdapter
	 */
	@Inject
	public CustomerDefaultPaymentInstrumentRepositoryImpl(
			@Named("customerDefaultPaymentInstrumentService") final CustomerDefaultPaymentInstrumentService customerDefaultPaymentInstrumentService,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter) {
		this.customerDefaultPaymentInstrumentService = customerDefaultPaymentInstrumentService;
		this.reactiveAdapter = reactiveAdapter;
	}

	@Override
	public Completable saveAsDefault(final CustomerPaymentInstrument customerPaymentInstrument) {
		return reactiveAdapter.fromServiceAsCompletable(() -> customerDefaultPaymentInstrumentService.saveAsDefault(customerPaymentInstrument));
	}

	@CacheResult
	@Override
	public Maybe<CustomerPaymentInstrument> getDefaultForCustomer(final Customer customer) {
		return reactiveAdapter.fromServiceAsMaybe(() -> customerDefaultPaymentInstrumentService.getDefaultForCustomer(customer));
	}

	@Override
	public Single<Boolean> hasDefaultPaymentInstrument(final Customer customer) {
		return reactiveAdapter.fromServiceAsSingle(() -> customerDefaultPaymentInstrumentService.hasDefaultPaymentInstrument(customer));
	}

	@Override
	public Single<Boolean> isDefault(final CustomerPaymentInstrument customerPaymentInstrument) {
		return reactiveAdapter.fromServiceAsSingle(() -> customerDefaultPaymentInstrumentService.isDefault(customerPaymentInstrument));
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	protected CustomerDefaultPaymentInstrumentService getCustomerDefaultPaymentInstrumentService() {
		return customerDefaultPaymentInstrumentService;
	}

	public ReactiveAdapter getReactiveAdapter() {
		return reactiveAdapter;
	}
}
