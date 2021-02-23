/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;

/**
 * The facade for operations with payment instrument identifiers.
 */
public interface PaymentInstrumentToPaymentMethodLinkRepository {

	/**
	 * Finds the profile payment method REST identifier by profile instrument REST identifier.
	 *
	 * @param userId   unique user identifier
	 * @param identifier profile instrument REST identifier
	 * @return the profile payment method REST identifier
	 */
	Observable<ProfilePaymentMethodIdentifier> getProfilePaymentMethodIdentifier(String userId, PaymentInstrumentIdentifier identifier);

	/**
	 * Finds the order payment method REST identifier by order instrument REST identifier.
	 *
	 * @param identifier order instrument REST identifier
	 * @return the order payment method REST identifier
	 */
	Observable<OrderPaymentMethodIdentifier> getOrderPaymentMethodIdentifier(OrderPaymentInstrumentIdentifier identifier);

	/**
	 * Finds the account payment method REST identifier by account instrument REST identifier.
	 *
	 * @param accountId   unique user identifier
	 * @param identifier account instrument REST identifier
	 * @return the account payment method REST identifier
	 */
	Observable<AccountPaymentMethodIdentifier> getAccountPaymentMethodIdentifier(String accountId, AccountPaymentInstrumentIdentifier identifier);

}
