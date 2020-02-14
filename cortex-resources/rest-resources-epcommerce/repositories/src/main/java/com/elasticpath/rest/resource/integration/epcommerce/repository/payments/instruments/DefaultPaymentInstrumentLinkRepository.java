/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;

/**
 * The facade for operations with default payment instrument identifiers.
 */
public interface DefaultPaymentInstrumentLinkRepository {

	/**
	 * Finds the default profile payment instrument REST identifier by profile instruments REST identifier.
	 *
	 * @param identifier profile instruments REST identifier
	 * @return the default profile payment instrument REST identifier
	 */
	Observable<PaymentInstrumentIdentifier> getDefaultPaymentInstrumentIdentifier(PaymentInstrumentsIdentifier identifier);

}
