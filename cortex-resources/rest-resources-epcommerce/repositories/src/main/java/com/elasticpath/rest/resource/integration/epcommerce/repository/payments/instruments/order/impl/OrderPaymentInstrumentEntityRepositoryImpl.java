/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.order.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderPaymentInstrumentEntity;

import java.util.Currency;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.money.Money;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.base.CostEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CartOrderPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerDefaultPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.transform.MoneyTransformer;

/**
 * Repository for Order Payment Instrument Entity.
 *
 * @param <E> extends OrderPaymentInstrumentEntity
 * @param <I> extends OrderPaymentInstrumentIdentifier
 */
@Component
public class OrderPaymentInstrumentEntityRepositoryImpl<E extends OrderPaymentInstrumentEntity, I extends OrderPaymentInstrumentIdentifier>
		implements Repository<OrderPaymentInstrumentEntity, OrderPaymentInstrumentIdentifier> {

	private CartOrderPaymentInstrumentRepository cartOrderPaymentInstrumentRepository;
	private PaymentInstrumentManagementRepository paymentInstrumentManagementRepository;
	private CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;
	private CustomerDefaultPaymentInstrumentRepository customerDefaultPaymentInstrumentRepository;
	private CustomerRepository customerRepository;
	private ResourceOperationContext resourceOperationContext;
	private MoneyTransformer moneyTransformer;

	@Override
	public Single<OrderPaymentInstrumentEntity> findOne(final OrderPaymentInstrumentIdentifier orderPaymentInstrumentIdentifier) {
		final String instrumentGuid = orderPaymentInstrumentIdentifier.getPaymentInstrumentId().getValue();
		final String userIdentifier = resourceOperationContext.getUserIdentifier();
		final Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		final Currency currency = SubjectUtil.getCurrency(resourceOperationContext.getSubject());

		return cartOrderPaymentInstrumentRepository.findByGuid(instrumentGuid)
				.flatMap(cartOrderPaymentInstrument -> getOrderPaymentInstrument(userIdentifier, locale, currency, cartOrderPaymentInstrument));
	}

	private Single<OrderPaymentInstrumentEntity> getOrderPaymentInstrument(final String userIdentifier, final Locale locale, final Currency currency,
																		   final CartOrderPaymentInstrument cartOrderPaymentInstrument) {
		return Single.just(moneyTransformer.transformToEntity(Money.valueOf(cartOrderPaymentInstrument.getLimitAmount(), currency), locale))
				.flatMap(costEntity -> Single.just(cartOrderPaymentInstrument.getPaymentInstrumentGuid())
						.flatMap(paymentInstrumentGuid -> buildOrderPaymentInstrument(userIdentifier, costEntity, paymentInstrumentGuid)));
	}

	private Single<OrderPaymentInstrumentEntity> buildOrderPaymentInstrument(final String userIdentifier, final CostEntity costEntity,
																			 final String paymentInstrumentGuid) {
		return getIsSavedAndIsDefaultPair(userIdentifier, paymentInstrumentGuid)
				.flatMap(isSavedAndIsDefaultPair -> paymentInstrumentManagementRepository.getPaymentInstrumentByGuid(paymentInstrumentGuid)
						.map(paymentInstrument -> buildOrderPaymentInstrumentEntity(
								costEntity,
								isSavedAndIsDefaultPair.getSecond(),
								isSavedAndIsDefaultPair.getFirst(),
								paymentInstrument.getData(),
								paymentInstrument.getName())));
	}

	private Single<Pair<Boolean, Boolean>> getIsSavedAndIsDefaultPair(final String userIdentifier, final String paymentInstrumentGuid) {
		return customerRepository.getCustomer(userIdentifier)
				.flatMapObservable(customer -> customerPaymentInstrumentRepository.findByCustomer(customer)
						.filter(instrument -> instrument.getPaymentInstrumentGuid().equals(paymentInstrumentGuid))
						.flatMapSingle(instrument -> customerDefaultPaymentInstrumentRepository.isDefault(instrument)
								.map(isDefault -> Pair.of(true, isDefault))))
				.first(Pair.of(false, false));
	}

	@Override
	public Completable delete(final OrderPaymentInstrumentIdentifier paymentInstrumentIdentifier) {
		final String instrumentGuid = paymentInstrumentIdentifier.getPaymentInstrumentId().getValue();
		String userIdentifier = resourceOperationContext.getUserIdentifier();

		return customerRepository.getCustomer(userIdentifier)
				.flatMapCompletable(customer -> cartOrderPaymentInstrumentRepository.findByGuid(instrumentGuid)
						.flatMapCompletable(chosenInstrument -> filterProfilePaymentInstruments(customer, chosenInstrument)
								.andThen(cartOrderPaymentInstrumentRepository.remove(chosenInstrument))));
	}

	private Completable filterProfilePaymentInstruments(final Customer customer, final CartOrderPaymentInstrument chosenInstrument) {
		String paymentInstrumentGuid = chosenInstrument.getPaymentInstrumentGuid();
		return customerPaymentInstrumentRepository.findByCustomer(customer)
				.filter(profileInstrumentChoice -> profileInstrumentChoice.getPaymentInstrumentGuid().equals(paymentInstrumentGuid))
				.flatMapCompletable(customerPaymentInstrumentRepository::remove);
	}

	@Reference
	public void setCartOrderPaymentInstrumentRepository(final CartOrderPaymentInstrumentRepository cartOrderPaymentInstrumentRepository) {
		this.cartOrderPaymentInstrumentRepository = cartOrderPaymentInstrumentRepository;
	}

	@Reference
	public void setPaymentInstrumentManagementRepository(final PaymentInstrumentManagementRepository paymentInstrumentManagementRepository) {
		this.paymentInstrumentManagementRepository = paymentInstrumentManagementRepository;
	}

	@Reference
	public void setCustomerPaymentInstrumentRepository(final CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository) {
		this.customerPaymentInstrumentRepository = customerPaymentInstrumentRepository;
	}

	@Reference
	public void setCustomerDefaultPaymentInstrumentRepository(final CustomerDefaultPaymentInstrumentRepository defaultRepository) {
		this.customerDefaultPaymentInstrumentRepository = defaultRepository;
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
	public void setMoneyTransformer(final MoneyTransformer moneyTransformer) {
		this.moneyTransformer = moneyTransformer;
	}
}
