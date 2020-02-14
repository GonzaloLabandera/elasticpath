/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.profile.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentInstrumentEntity;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentInstrumentIdentifier;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerDefaultPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentManagementRepository;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;

/**
 * Repository for Payment Instrument Entity.
 *
 * @param <E> extends PaymentInstrumentEntity
 * @param <I> extends PaymentInstrumentIdentifier
 */
@Component
public class PaymentInstrumentEntityRepositoryImpl<E extends PaymentInstrumentEntity, I extends PaymentInstrumentIdentifier>
		implements Repository<PaymentInstrumentEntity, PaymentInstrumentIdentifier> {

	private ResourceOperationContext resourceOperationContext;
	private CustomerRepository customerRepository;
	private CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;
	private CustomerDefaultPaymentInstrumentRepository customerDefaultPaymentInstrumentRepository;
	private PaymentInstrumentManagementRepository paymentInstrumentManagementRepository;
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;

	@Override
	public Single<PaymentInstrumentEntity> findOne(final PaymentInstrumentIdentifier paymentInstrumentIdentifier) {
		final String customerPaymentInstrumentGuid = paymentInstrumentIdentifier.getPaymentInstrumentId().getValue();
		return customerPaymentInstrumentRepository.findByGuid(customerPaymentInstrumentGuid)
				.flatMap(instrument -> paymentInstrumentManagementRepository.getPaymentInstrumentByGuid(instrument.getPaymentInstrumentGuid())
						.flatMap(paymentInstrumentDTO -> customerDefaultPaymentInstrumentRepository.isDefault(instrument)
								.map(isDefault -> buildPaymentInstrumentEntity(isDefault, paymentInstrumentDTO.getData(),
										paymentInstrumentDTO.getName()))));
	}

	@Override
	public Observable<PaymentInstrumentIdentifier> findAll(final IdentifierPart<String> scope) {
		final String customerGuid = resourceOperationContext.getUserIdentifier();
		return customerRepository.getCustomer(customerGuid)
				.flatMapObservable(customer -> Observable.just(filteredPaymentInstrumentService
						.findCustomerPaymentInstrumentsForCustomerAndStore(customer, scope.getValue())))
				.flatMap(Observable::fromIterable)
				.map(instrument -> buildPaymentInstrumentIdentifier(scope, StringIdentifier.of(instrument.getGuid())));
	}

	@Override
	public Completable delete(final PaymentInstrumentIdentifier paymentInstrumentIdentifier) {
		final String instrumentGuid = paymentInstrumentIdentifier.getPaymentInstrumentId().getValue();
		return customerPaymentInstrumentRepository.findByGuid(instrumentGuid)
				.flatMapCompletable(instrument -> customerPaymentInstrumentRepository.remove(instrument));
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Reference
	public void setCustomerPaymentInstrumentRepository(final CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository) {
		this.customerPaymentInstrumentRepository = customerPaymentInstrumentRepository;
	}

	@Reference
	public void setCustomerDefaultPaymentInstrumentRepository(final CustomerDefaultPaymentInstrumentRepository
																	  customerDefaultPaymentInstrumentRepository) {
		this.customerDefaultPaymentInstrumentRepository = customerDefaultPaymentInstrumentRepository;
	}

	@Reference
	public void setPaymentInstrumentManagementRepository(final PaymentInstrumentManagementRepository paymentInstrumentManagementRepository) {
		this.paymentInstrumentManagementRepository = paymentInstrumentManagementRepository;
	}

	@Reference
	public void setFilteredPaymentInstrumentService(final FilteredPaymentInstrumentService filteredPaymentInstrumentService) {
		this.filteredPaymentInstrumentService = filteredPaymentInstrumentService;
	}
}
