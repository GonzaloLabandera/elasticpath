/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentsIdentifier;

/**
 * Repository for Purchase Payment Instrument.
 */
public interface PurchasePaymentInstrumentRepository {

	/**
	 * Finds the purchase payment instruments associated with a purchase.
	 *
	 * @param scope      scope
	 * @param identifier {@link PurchasePaymentInstrumentsIdentifier}
	 * @return purchase payment instruments associated with a purchase in the form of {@link PurchasePaymentInstrumentIdentifier}
	 */
	Observable<PurchasePaymentInstrumentIdentifier> findPurchaseInstrumentsByPurchaseId(String scope,
																						PurchasePaymentInstrumentsIdentifier identifier);

	/**
	 * Finds a purchase payment instrument entity corresponding to the provided {@link PurchasePaymentInstrumentIdentifier}.
	 *
	 * @param purchasePaymentInstrumentIdentifier {@link PurchasePaymentInstrumentIdentifier}
	 * @return {@link PurchasePaymentInstrumentEntity}
	 */
	Single<PurchasePaymentInstrumentEntity> findOne(PurchasePaymentInstrumentIdentifier purchasePaymentInstrumentIdentifier);
}
