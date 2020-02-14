/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.giftcertificate;

import java.math.BigDecimal;
import java.util.List;

import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.domain.payment.GiftCertificateTransaction;
import com.elasticpath.money.Money;
import com.elasticpath.service.EpPersistenceService;
import com.elasticpath.service.giftcertificate.impl.GiftCertificateTransactionResponse;
import com.elasticpath.service.misc.TimeService;

/**
 * Gift certificate transaction service. All payment gateway calls should end up calling the GiftCertificateTransactionService.
 */
public interface GiftCertificateTransactionService extends EpPersistenceService {

	/**
	 * Adds the given {@link GiftCertificateTransaction}.
	 *
	 * @param giftCertificateTransaction the {@link GiftCertificateTransaction} to add
	 * @return the persisted instance of {@link GiftCertificateTransaction}
	 */
	GiftCertificateTransaction saveOrUpdate(GiftCertificateTransaction giftCertificateTransaction);

	/**
	 * Generic get method for all persistable domain models.
	 *
	 * @param uid the persisted instance uid
	 * @return the persisted instance if exists, otherwise null
	 */
	@Override
	Object getObject(long uid);

	/**
	 * Pre-authorize a payment.
	 *
	 * @param giftCertificate the giftCertificate
	 * @param amount          the amount of the payment
	 * @return {@link GiftCertificateTransactionResponse} the transaction response
	 */
	GiftCertificateTransactionResponse preAuthorize(GiftCertificate giftCertificate, Money amount);

	/**
	 * Captures a payment on a previously authorized amount.
	 *
	 * @param giftCertificate   the gift certificate
	 * @param authorizationCode the authorization code
	 * @param amount            the amount of the payment
	 * @return {@link GiftCertificateTransactionResponse} the transaction response
	 */
	GiftCertificateTransactionResponse capture(GiftCertificate giftCertificate, String authorizationCode, Money amount);

	/**
	 * Reverse a previous pre-authorization.
	 *
	 * @param giftCertificate   the gift certificate
	 * @param authorizationCode the authorization code
	 * @param amount            the amount of the payment
	 */
	void reversePreAuthorization(GiftCertificate giftCertificate, String authorizationCode, Money amount);

	/**
	 * Modifies amount of a previous pre-authorization.
	 *
	 * @param giftCertificate   the gift certificate
	 * @param authorizationCode the authorization code
	 * @param amount            the amount of the payment
	 */
	void modifyPreAuthorization(GiftCertificate giftCertificate, String authorizationCode, Money amount);

	/**
	 * Refunds previously captured payment.
	 *
	 * @param giftCertificate   the gift certificate
	 * @param authorizationCode the authorization code
	 * @param amount            the amount of the refund payment
	 */
	void refund(GiftCertificate giftCertificate, String authorizationCode, Money amount);

	/**
	 * Obtains the balance of the gift certificate.
	 *
	 * @param giftCertificate the gift certificate
	 * @return the balance amount
	 */
	BigDecimal getBalance(GiftCertificate giftCertificate);

	/**
	 * Set the time service.
	 *
	 * @param timeService the <code>TimeService</code> instance.
	 */
	void setTimeService(TimeService timeService);

	/**
	 * Calculate authorized and captured amount by transactions.
	 *
	 * @param allTransactions list of included transactions.
	 * @return total authorized and captured amount.
	 */
	BigDecimal calcTransactionBalance(List<GiftCertificateTransaction> allTransactions);

	/**
	 * Retrieves a list of {@link com.elasticpath.domain.payment.GiftCertificateTransaction}s for the given giftCertificate.
	 *
	 * @param giftCertificate the given {@link com.elasticpath.domain.catalog.GiftCertificate}
	 * @return total authorized and captured amount.
	 */
	List<GiftCertificateTransaction> getGiftCertificateTransactions(GiftCertificate giftCertificate);

}

