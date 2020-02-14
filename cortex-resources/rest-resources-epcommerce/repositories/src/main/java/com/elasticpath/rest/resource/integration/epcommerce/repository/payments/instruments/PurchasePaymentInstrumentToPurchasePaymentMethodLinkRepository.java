/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentMethodIdentifier;

/**
 * The facade for operations with purchase payment instrument identifiers.
 */
public interface PurchasePaymentInstrumentToPurchasePaymentMethodLinkRepository {

	/**
	 * Finds the purchase payment method REST identifier by purchase instrument REST identifier.
	 *
	 * @param identifier purchase instrument REST identifier
	 * @return the purchase payment method REST identifier
	 */
	Observable<PurchasePaymentMethodIdentifier> getPurchasePaymentMethodIdentifier(PurchasePaymentInstrumentIdentifier identifier);
}
