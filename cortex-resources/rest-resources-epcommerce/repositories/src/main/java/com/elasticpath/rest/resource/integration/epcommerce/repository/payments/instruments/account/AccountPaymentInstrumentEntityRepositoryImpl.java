/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.account;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildAccountPaymentInstrumentIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentInstrumentEntity;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentEntity;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerDefaultPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;

/**
 * Repository for Payment Instrument Entity.
 *
 * @param <E> extends PaymentInstrumentEntity
 * @param <I> extends PaymentInstrumentIdentifier
 */
@Component
public class AccountPaymentInstrumentEntityRepositoryImpl<E extends PaymentInstrumentEntity, I extends AccountPaymentInstrumentIdentifier>
		implements Repository<PaymentInstrumentEntity, AccountPaymentInstrumentIdentifier> {

	private CustomerRepository customerRepository;
	private CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;
	private CustomerDefaultPaymentInstrumentRepository customerDefaultPaymentInstrumentRepository;
	private PaymentInstrumentManagementRepository paymentInstrumentManagementRepository;
	private PaymentInstrumentRepository paymentInstrumentRepository;
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;

	@Override
	public Single<PaymentInstrumentEntity> findOne(final AccountPaymentInstrumentIdentifier accountPaymentInstrumentIdentifier) {
		final String customerPaymentInstrumentGuid = accountPaymentInstrumentIdentifier.getAccountPaymentInstrumentId().getValue();
		return customerPaymentInstrumentRepository.findByGuid(customerPaymentInstrumentGuid)
				.flatMap(instrument -> paymentInstrumentManagementRepository.getPaymentInstrumentByGuid(instrument.getPaymentInstrumentGuid())
						.flatMap(paymentInstrumentDTO -> customerDefaultPaymentInstrumentRepository.isDefault(instrument)
								.map(isDefault -> buildPaymentInstrumentEntity(isDefault, paymentInstrumentDTO.getData(),
										paymentInstrumentDTO.getName()))));
	}

	@Override
	public Observable<AccountPaymentInstrumentIdentifier> findAll(final IdentifierPart<String> scope) {
		final IdentifierPart<String> accountId = paymentInstrumentRepository.getAccountIdFromResourceOperationContext();
		if (accountId == null) {
			return Observable.empty();
		}
		return customerRepository.getCustomer(accountId.getValue())
				.flatMapObservable(customer -> Observable.just(filteredPaymentInstrumentService
						.findCustomerPaymentInstrumentsForCustomerAndStore(customer, scope.getValue())))
				.flatMap(Observable::fromIterable)
				.map(instrument -> buildAccountPaymentInstrumentIdentifier(scope, accountId, StringIdentifier.of(instrument.getGuid())));
	}

	@Override
	public Completable delete(final AccountPaymentInstrumentIdentifier accountPaymentInstrumentIdentifier) {
		final String instrumentGuid = accountPaymentInstrumentIdentifier.getAccountPaymentInstrumentId().getValue();
		return customerPaymentInstrumentRepository.findByGuid(instrumentGuid)
				.flatMapCompletable(instrument -> customerPaymentInstrumentRepository.remove(instrument));
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

	@Reference
	public void setPaymentInstrumentRepository(final PaymentInstrumentRepository paymentInstrumentRepository) {
		this.paymentInstrumentRepository = paymentInstrumentRepository;
	}
}
