/*
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.domain.order.impl;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.security.CreditCardEncrypter;
import com.elasticpath.domain.impl.AbstractEpDomainImpl;

/**
 * Represents a credit card number. Credit card masking and encryption logic is encapsulated within this class.
 */
@Embeddable
public class CreditCardNumber extends AbstractEpDomainImpl {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private String encryptedCardNumber;
	private boolean sanitized;

	/**
	 * Gets the encrypted credit card number; <code>null</code> if the credit card number is stored masked.
	 * <p/>
	 * There were concerns that making this method public might be a security issue. Here are the reasons why
	 * that is not the case:
	 * <ul>
	 * <li>(A) this class is only used internally by <code>OrderPaymentImpl</code>,
	 * <li>(B) it is never passed to a client, and
	 * <li>(C) <code>OrderPaymentImpl</code> provides no setter method for this.
	 * </ul>
	 * Therefore access to the encrypted credit card number is completely controlled by <code>OrderPaymentImpl</code>.
	 * 
	 * @return the encrypted credit card number; <code>null</code> if the credit card number is stored masked
	 */
	@Basic
	@Column(name = "CARD_NUMBER")
	public String getEncryptedCardNumber() {
		return encryptedCardNumber;
	}

	public void setEncryptedCardNumber(final String encryptedCardNumber) {
		this.encryptedCardNumber = encryptedCardNumber;
	}

	/**
	 * Returns a masked credit card number.
	 * 
	 * @return a masked credit card number
	 */
	public String getMaskedCardNumber() {
		String fullCardNumber = getFullCardNumber();
		if (null != fullCardNumber) {
			return this.getCreditCardEncrypter().mask(fullCardNumber);
		}
		return null;
	}

	/**
	 * Sets the full credit card number.
	 * 
	 * @param fullCardNumber the full credit card number.
	 */
	public void setFullCardNumber(final String fullCardNumber) {
		if (StringUtils.isBlank(fullCardNumber)) {
			encryptedCardNumber = null;
		} else {
			encryptedCardNumber = this.getCreditCardEncrypter().encrypt(fullCardNumber);
		}
	}

	/**
	 * Returns the full, unencrypted credit card number (e.g. "4012888888881881"). If the credit card number stored is not
	 * encrypted, <code>null</code> is returned.
	 * 
	 * @return the full, unencrypted credit card number; <code>null</code> if the stored credit card is no encrypted
	 */
	@Transient
	public String getFullCardNumber() {
		if (null != encryptedCardNumber) {
			return this.getCreditCardEncrypter().decrypt(encryptedCardNumber);
		}
		return null;
	}

	/**
	 * Returns true if this credit card number has been sanitized prior to persistence.
	 * @return true if the number has sanitized
	 */
	@Transient
	public boolean isSanitized() {
		return sanitized;
	}

	/**
	 * Set to true if this credit card number has been sanitized prior to persistence.
	 * @param sanitized true if this credit card number has been sanitized
	 */
	public void setSanitized(final boolean sanitized) {
		this.sanitized = sanitized;
	}

	/**
	 * Returns an instance of {@link CreditCardEncrypter}.
	 * 
	 * @return an instance of {@link CreditCardEncrypter}
	 */
	@Transient
	private CreditCardEncrypter getCreditCardEncrypter() {
		return getBean(ContextIdNames.CREDIT_CARD_ENCRYPTER);
	}
}