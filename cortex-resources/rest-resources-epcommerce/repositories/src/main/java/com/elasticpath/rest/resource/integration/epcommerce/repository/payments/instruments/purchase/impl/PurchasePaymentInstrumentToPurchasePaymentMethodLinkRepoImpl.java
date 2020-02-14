/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.purchase.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPurchasePaymentMethodIdentifier;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.base.GloballyIdentifiable;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentMethodIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.PaymentMethodRepository;

/**
 * The implementation of {@link PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepository} related operations.
 */
@Singleton
@Named("purchasePaymentInstrumentToPurchasePaymentMethodLinkRepository")
public class PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepoImpl implements
		PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepository {

	private final PaymentMethodRepository paymentMethodRepository;
	private final PaymentInstrumentManagementRepository paymentInstrumentManagementRepository;

	/**
	 * Constructor.
	 *
	 * @param paymentMethodRepository               payment method repository
	 * @param paymentInstrumentManagementRepository payment instrument management repository
	 */
	@Inject
	public PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepoImpl(
			@Named("paymentMethodRepository") final PaymentMethodRepository paymentMethodRepository,
			@Named("paymentInstrumentManagementRepository") final PaymentInstrumentManagementRepository paymentInstrumentManagementRepository) {
		this.paymentMethodRepository = paymentMethodRepository;
		this.paymentInstrumentManagementRepository = paymentInstrumentManagementRepository;
	}

	@Override
	@CacheResult
	public Observable<PurchasePaymentMethodIdentifier> getPurchasePaymentMethodIdentifier(final PurchasePaymentInstrumentIdentifier identifier) {
		final IdentifierPart<String> scope = getScope(identifier);

		final Observable<StorePaymentProviderConfig> storePaymentProviderConfigs =
				paymentMethodRepository.getStorePaymentProviderConfigsForStoreCode(scope.getValue());

		return getPaymentProviderIdByPurchasePaymentInstrumentIdentifier(identifier)
				.flatMapObservable(providerId -> filterForMatchingProviderId(storePaymentProviderConfigs, providerId))
				.map(GloballyIdentifiable::getGuid)
				.map(StringIdentifier::of)
				.map(storeConfigGuidIdentifier -> buildPurchasePaymentMethodIdentifier(identifier,
						storeConfigGuidIdentifier));
	}

	private IdentifierPart<String> getScope(final PurchasePaymentInstrumentIdentifier identifier) {
		return identifier.getPurchasePaymentInstruments().getPurchase().getPurchases().getScope();
	}

	private Single<String> getPaymentProviderIdByPurchasePaymentInstrumentIdentifier(final PurchasePaymentInstrumentIdentifier identifier) {
		return paymentInstrumentManagementRepository.getPaymentInstrumentByOrderPaymentInstrumentGuid(getPaymentInstrumentId(identifier))
				.map(PaymentInstrumentDTO::getPaymentProviderConfigurationGuid);
	}

	private String getPaymentInstrumentId(final PurchasePaymentInstrumentIdentifier identifier) {
		return identifier.getPaymentInstrumentId().getValue();
	}

	private Observable<StorePaymentProviderConfig> filterForMatchingProviderId(final Observable<StorePaymentProviderConfig> storeProviderConfigs,
																			   final String targetPaymentProviderId) {
		return storeProviderConfigs
				.filter(storePaymentProviderConfig -> storePaymentProviderConfig.getPaymentProviderConfigGuid().equals(targetPaymentProviderId));
	}

	protected PaymentMethodRepository getPaymentMethodRepository() {
		return paymentMethodRepository;
	}

	protected PaymentInstrumentManagementRepository getPaymentInstrumentManagementRepository() {
		return paymentInstrumentManagementRepository;
	}
}
