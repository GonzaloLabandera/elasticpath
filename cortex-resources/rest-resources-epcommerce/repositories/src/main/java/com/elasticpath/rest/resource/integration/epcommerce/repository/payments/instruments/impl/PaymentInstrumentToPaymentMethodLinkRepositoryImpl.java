/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderPaymentMethodIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildProfilePaymentMethodIdentifier;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.base.GloballyIdentifiable;
import com.elasticpath.domain.orderpaymentapi.CorePaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CartOrderPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentToPaymentMethodLinkRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.PaymentMethodRepository;

/**
 * The implementation of {@link PaymentInstrumentToPaymentMethodLinkRepository} related operations.
 */
@Singleton
@Named("paymentInstrumentToPaymentMethodLinkRepository")
public class PaymentInstrumentToPaymentMethodLinkRepositoryImpl implements PaymentInstrumentToPaymentMethodLinkRepository {

	private final PaymentMethodRepository paymentMethodRepository;
	private final CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;
	private final CartOrderPaymentInstrumentRepository cartOrderPaymentInstrumentRepository;
	private final PaymentInstrumentManagementRepository paymentInstrumentManagementRepository;

	/**
	 * Constructor.
	 *
	 * @param paymentMethodRepository               payment method repository
	 * @param customerPaymentInstrumentRepository   customer instrument repository
	 * @param cartOrderPaymentInstrumentRepository  cart order instrument repository
	 * @param paymentInstrumentManagementRepository instrument management repository
	 */
	@Inject
	public PaymentInstrumentToPaymentMethodLinkRepositoryImpl(
			@Named("paymentMethodRepository") final PaymentMethodRepository paymentMethodRepository,
			@Named("customerPaymentInstrumentRepository") final CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository,
			@Named("cartOrderPaymentInstrumentRepository") final CartOrderPaymentInstrumentRepository cartOrderPaymentInstrumentRepository,
			@Named("paymentInstrumentManagementRepository") final PaymentInstrumentManagementRepository paymentInstrumentManagementRepository) {
		this.paymentMethodRepository = paymentMethodRepository;
		this.customerPaymentInstrumentRepository = customerPaymentInstrumentRepository;
		this.cartOrderPaymentInstrumentRepository = cartOrderPaymentInstrumentRepository;
		this.paymentInstrumentManagementRepository = paymentInstrumentManagementRepository;
	}

	@Override
	@CacheResult
	public Observable<ProfilePaymentMethodIdentifier> getProfilePaymentMethodIdentifier(final String userId,
																						final PaymentInstrumentIdentifier identifier) {
		final IdentifierPart<String> scope = identifier.getPaymentInstruments().getScope();
		final String customerPaymentInstrumentGuid = identifier.getPaymentInstrumentId().getValue();

		Observable<StorePaymentProviderConfig> storePaymentProviderConfigs =
				paymentMethodRepository.getStorePaymentProviderConfigsForStoreCode(scope.getValue());

		return getPaymentProviderIdForCustomerInstrumentGuid(customerPaymentInstrumentGuid)
				.flatMapObservable(providerId -> filterForMatchingProviderId(storePaymentProviderConfigs, providerId))
				.map(GloballyIdentifiable::getGuid)
				.map(StringIdentifier::of)
				.map(storeConfigGuidIdentifier -> buildProfilePaymentMethodIdentifier(StringIdentifier.of(userId), scope, storeConfigGuidIdentifier));
	}

	@Override
	public Observable<OrderPaymentMethodIdentifier> getOrderPaymentMethodIdentifier(final OrderPaymentInstrumentIdentifier identifier) {
		final IdentifierPart<String> orderId = identifier.getOrder().getOrderId();
		final IdentifierPart<String> scope = identifier.getOrder().getScope();
		final String cartOrderPaymentInstrumentGuid = identifier.getPaymentInstrumentId().getValue();

		Observable<StorePaymentProviderConfig> storePaymentProviderConfigs =
				paymentMethodRepository.getStorePaymentProviderConfigsForStoreCode(scope.getValue());

		return getPaymentProviderIdForCartOrderInstrumentGuid(cartOrderPaymentInstrumentGuid)
				.flatMapObservable(providerId -> filterForMatchingProviderId(storePaymentProviderConfigs, providerId))
				.map(GloballyIdentifiable::getGuid)
				.map(StringIdentifier::of)
				.map(storeConfigGuidIdentifier -> buildOrderPaymentMethodIdentifier(scope, storeConfigGuidIdentifier, orderId));
	}

	private Single<String> getPaymentProviderIdForCustomerInstrumentGuid(final String customerPaymentInstrumentGuid) {
		return customerPaymentInstrumentRepository.findByGuid(customerPaymentInstrumentGuid)
				.map(CustomerPaymentInstrument::getPaymentInstrumentGuid)
				.flatMap(paymentInstrumentManagementRepository::getPaymentInstrumentByGuid)
				.map(PaymentInstrumentDTO::getPaymentProviderConfigurationGuid);
	}

	private Single<String> getPaymentProviderIdForCartOrderInstrumentGuid(final String cartOrderPaymentInstrumentGuid) {
		return cartOrderPaymentInstrumentRepository.findByGuid(cartOrderPaymentInstrumentGuid)
				.map(CorePaymentInstrument::getPaymentInstrumentGuid)
				.flatMap(paymentInstrumentManagementRepository::getPaymentInstrumentByGuid)
				.map(PaymentInstrumentDTO::getPaymentProviderConfigurationGuid);
	}

	private Observable<StorePaymentProviderConfig> filterForMatchingProviderId(final Observable<StorePaymentProviderConfig> storeProviderConfigs,
																			   final String targetPaymentProviderId) {
		return storeProviderConfigs
				.filter(storePaymentProviderConfig -> storePaymentProviderConfig.getPaymentProviderConfigGuid().equals(targetPaymentProviderId));
	}

	protected PaymentMethodRepository getPaymentMethodRepository() {
		return paymentMethodRepository;
	}

	protected CustomerPaymentInstrumentRepository getCustomerPaymentInstrumentRepository() {
		return customerPaymentInstrumentRepository;
	}

	protected CartOrderPaymentInstrumentRepository getCartOrderPaymentInstrumentRepository() {
		return cartOrderPaymentInstrumentRepository;
	}

	protected PaymentInstrumentManagementRepository getPaymentInstrumentManagementRepository() {
		return paymentInstrumentManagementRepository;
	}
}
