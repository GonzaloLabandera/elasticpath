/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.customer;

import com.elasticpath.persistence.api.Entity;
import com.elasticpath.plugin.payment.dto.PaymentMethod;

/**
 * A <code>CustomerCreditCard</code> is a credit card stored by a store-front customer.
 */
public interface CustomerCreditCard extends Entity, PaymentMethod {
	/**
	 * @return the cardHolderName
	 */
	String getCardHolderName();

	/**
	 * @param cardHolderName the cardHolderName to set
	 */
	void setCardHolderName(String cardHolderName);

	/**
	 * @return the cardNumber
	 */
	String getCardNumber();

	/**
	 * @param cardNumber the cardNumber to set
	 */
	void setCardNumber(String cardNumber);

	/**
	 * @return the cardType
	 */
	String getCardType();

	/**
	 * @param cardType the cardType to set
	 */
	void setCardType(String cardType);

	/**
	 * @return the expiryMonth
	 */
	String getExpiryMonth();

	/**
	 * @param expiryMonth the expiryMonth to set
	 */
	void setExpiryMonth(String expiryMonth);

	/**
	 * @return the expiryYear
	 */
	String getExpiryYear();

	/**
	 * @param expiryYear the expiryYear to set
	 */
	void setExpiryYear(String expiryYear);

	/**
	 * @return the issueNumber
	 */
	Integer getIssueNumber();

	/**
	 * @param issueNumber the issueNumber to set
	 */
	void setIssueNumber(Integer issueNumber);

	/**
	 * @return the startMonth
	 */
	String getStartMonth();

	/**
	 * @param startMonth the startMonth to set
	 */
	void setStartMonth(String startMonth);

	/**
	 * @return the startYear
	 */
	String getStartYear();

	/**
	 * @param startYear the startYear to set
	 */
	void setStartYear(String startYear);

	/**
	 * Decrypts and returns the full credit card number. Access to this method should be restricted!
	 *
	 * @return the decrypted credit card number
	 */
	String getUnencryptedCardNumber();

	/**
	 * Decrypts and returns the masked credit card number: ************5381. Useful for displaying in receipts, GUI, order history, etc.
	 *
	 * @return the masked credit card number
	 */
	String getMaskedCardNumber();

	/**
	 * Encrypts the credit card number.
	 */
	void encrypt();

	/**
	 * Set the 3 or 4 digit security code from the back of the card.
	 * This value IS NOT persistent.
	 * @param securityCode the security code
	 */
	void setSecurityCode(String securityCode);

	/**
	 * Get the 3 or 4 digit security code from the back of the card.
	 * This value cannot be persisted and will not be available unless
	 * the user has specified it.
	 * @return the security code
	 */
	String getSecurityCode();
	
	/**
	 * Implementation of equals that delegates to a reflective equals method.
	 * This is a workaround for the {@link com.elasticpath.domain.customer.impl.CustomerCreditCardImpl#equals(Object)} implementation that
	 * performs an OR on GUID equality and several (but not all fields). 
	 * @param obj the card to check for equality
	 * @return true if equal, false otherwise.
	 */
	boolean reflectiveEquals(Object obj);

	/**
	 * Copies all the fields from another credit card into this credit card.
	 * Card number is optional, as it may have already been encrypted.
	 *
	 * @param creditCard the credit card from which to copy fields
	 * @param includeNumber specify whether to include the card number in the copy
	 */
	void copyFrom(CustomerCreditCard creditCard, boolean includeNumber);

}
