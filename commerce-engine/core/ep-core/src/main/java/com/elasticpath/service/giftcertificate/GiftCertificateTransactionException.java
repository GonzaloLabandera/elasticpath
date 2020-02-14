/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.giftcertificate;


/**
 * Exception to throw if the some thing wrong on the gift certificate to complete the order.
 */
public class GiftCertificateTransactionException extends RuntimeException {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Constructor.
	 *
	 * @param message error message
	 */
	public GiftCertificateTransactionException(final String message) {
		super(message);
	}

}

