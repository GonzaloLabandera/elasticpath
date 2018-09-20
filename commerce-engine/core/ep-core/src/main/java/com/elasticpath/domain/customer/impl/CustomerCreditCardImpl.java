/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.customer.impl;

import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.openjpa.persistence.DataCache;
import org.apache.openjpa.persistence.jdbc.VersionStrategy;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.security.CreditCardEncrypter;
import com.elasticpath.domain.customer.CustomerCreditCard;
import com.elasticpath.domain.impl.ElasticPathImpl;
import com.elasticpath.persistence.api.EntityUtils;

/**
 * The default implementation of <code>Customer</code>.
 */
@Entity
@VersionStrategy("state-comparison")
@Table (name = CustomerCreditCardImpl.TABLE_NAME)
@DataCache(enabled = false)
public class CustomerCreditCardImpl extends AbstractPaymentMethodImpl<CustomerCreditCardImpl> implements CustomerCreditCard {

	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;

	private static final int MEDIUM_TEXT_LENGTH = 100;
	private static final int YEAR_LENGTH = 4;
	private static final int CARD_TYPE_LENGTH = 50;
	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TCUSTOMERCREDITCARD";

	private String cardType;

	private String cardHolderName;

	private String cardNumber;

	private String expiryYear;

	private String expiryMonth;

	private String startYear;

	private String startMonth;

	private Integer issueNumber;

	private String securityCode;

	private String guid;

	/**
	 * The default constructor.
	 */
	public CustomerCreditCardImpl() {
		super();
	}

	/**
	 * Set default values for those fields need default values.
	 */
	@Override
	public void initialize() {
		initializeGuid();
	}

	/**
	 * Initializes the GUID.
	 */
	protected void initializeGuid() {
		EntityUtils.initializeGuid(this);
	}

	/**
	 * @return the cardHolderName
	 */
	@Override
	@Basic
	@Column (name = "CARD_HOLDER_NAME", length = MEDIUM_TEXT_LENGTH, nullable = false)
	public String getCardHolderName() {
		return cardHolderName;
	}

	/**
	 * @param cardHolderName the cardHolderName to set
	 */
	@Override
	public void setCardHolderName(final String cardHolderName) {
		this.cardHolderName = cardHolderName;
	}

	/**
	 * @return the cardNumber
	 */
	@Override
	@Basic
	@Column (name = "CARD_NUMBER")
	public String getCardNumber() {
		return cardNumber;
	}

	/**
	 * @param cardNumber the cardNumber to set
	 */
	@Override
	public void setCardNumber(final String cardNumber) {
		this.cardNumber = cardNumber;
	}

	/**
	 * @return the cardType
	 */
	@Override
	@Basic
	@Column (name = "CARD_TYPE", length = CARD_TYPE_LENGTH)
	public String getCardType() {
		return cardType;
	}

	/**
	 * @param cardType the cardType to set
	 */
	@Override
	public void setCardType(final String cardType) {
		this.cardType = cardType;
	}

	/**
	 * @return the expiryMonth
	 */
	@Override
	@Basic
	@Column (name = "EXPIRY_MONTH", length = 2)
	public String getExpiryMonth() {
		return expiryMonth;
	}

	/**
	 * @param expiryMonth the expiryMonth to set
	 */
	@Override
	public void setExpiryMonth(final String expiryMonth) {
		this.expiryMonth = expiryMonth;
	}

	/**
	 * @return the expiryYear
	 */
	@Override
	@Basic
	@Column (name = "EXPIRY_YEAR", length = YEAR_LENGTH)
	public String getExpiryYear() {
		return expiryYear;
	}

	/**
	 * @param expiryYear the expiryYear to set
	 */
	@Override
	public void setExpiryYear(final String expiryYear) {
		this.expiryYear = expiryYear;
	}

	/**
	 * @return the issueNumber
	 */
	@Override
	@Basic
	@Column (name = "ISSUE_NUMBER")
	public Integer getIssueNumber() {
		return issueNumber;
	}

	/**
	 * @param issueNumber the issueNumber to set
	 */
	@Override
	public void setIssueNumber(final Integer issueNumber) {
		this.issueNumber = issueNumber;
	}

	/**
	 * @return the startMonth
	 */
	@Override
	@Basic
	@Column (name = "START_MONTH", length = 2)
	public String getStartMonth() {
		return startMonth;
	}

	/**
	 * @param startMonth the startMonth to set
	 */
	@Override
	public void setStartMonth(final String startMonth) {
		this.startMonth = startMonth;
	}

	/**
	 * @return the startYear
	 */
	@Override
	@Basic
	@Column (name = "START_YEAR", length = YEAR_LENGTH)
	public String getStartYear() {
		return startYear;
	}

	/**
	 * @param startYear the startYear to set
	 */
	@Override
	public void setStartYear(final String startYear) {
		this.startYear = startYear;
	}

	/**
	 * Decrypts and returns the full credit card number. Access to this method should be restricted!
	 *
	 * @return the decrypted credit card number
	 */
	@Override
	@Transient
	public String getUnencryptedCardNumber() {
		return getCreditCardEncrypter().decrypt(getCardNumber());
	}

	/**
	 * Decrypts and returns the masked credit card number: ************5381. Useful for displaying in receipts, GUI, order history, etc.
	 *
	 * @return the masked credit card number
	 */
	@Override
	@Transient
	public String getMaskedCardNumber() {
		return getCreditCardEncrypter().decryptAndMask(getCardNumber());
	}

	/**
	 * Returns the credit card encrypter.
	 *
	 * @return the credit card encrypter
	 */
	@Transient
	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	private CreditCardEncrypter getCreditCardEncrypter() {
		return ElasticPathImpl.getInstance().getBean(ContextIdNames.CREDIT_CARD_ENCRYPTER);
	}

	/**
	 * Encrypts the credit card number.
	 */
	@Override
	public void encrypt() {
		if (getCardNumber() != null) {
			setCardNumber(getCreditCardEncrypter().encrypt(getCardNumber()));
		}
	}

	/**
	 * Set the 3 or 4 digit security code from the back of the card.
	 * This value IS NOT persistent.
	 * @param securityCode the security code
	 */
	@Override
	public void setSecurityCode(final String securityCode) {
		this.securityCode = securityCode;
	}

	/**
	 * Get the 3 or 4 digit security code from the back of the card.
	 * This value cannot be persisted and will not be available unless
	 * the user has specified it.
	 * @return the security code
	 */
	@Override
	@Transient
	public String getSecurityCode() {
		return securityCode;
	}

	/**
	 * Set the guid.
	 *
	 * @param guid the guid to set.
	 */
	@Override
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	/**
	 * Return the guid.
	 * @return the guid.
	 */
	@Override
	@Basic
	@Column(name = "GUID")
	public String getGuid() {
		return guid;
	}

	@Override
	public boolean reflectiveEquals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
	
	/**
	 * Returns <code>true</code> if this category equals to the given object.
	 *
	 * @param obj the given object
	 * @return <code>true</code> if this category equals to the given object
	 */
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof CustomerCreditCardImpl)) {
			return false;
		}

		// Compare object id
		if (this == obj) {
			return true;
		}

		final CustomerCreditCardImpl other = (CustomerCreditCardImpl) obj;
		final boolean detailsSame = Objects.equals(cardNumber, other.cardNumber)
				&& Objects.equals(cardHolderName, other.cardHolderName)
				&& Objects.equals(expiryMonth, other.expiryMonth)
				&& Objects.equals(expiryYear, other.expiryYear)
				&& Objects.equals(cardType, other.cardType);

		return detailsSame || Objects.equals(getGuid(), other.getGuid());
	}

	@Override
	public int hashCode() {
		return Objects.hash(cardNumber, cardHolderName, expiryMonth, expiryYear, cardType);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Copies all the fields from another credit card into this credit card.
	 * Card number is optional, as it may have already been encrypted.
	 *
	 * @param creditCard the credit card from which to copy fields
	 * @param includeNumber specify whether to include the card number in the copy
	 */
	@Override
	public void copyFrom(final CustomerCreditCard creditCard, final boolean includeNumber) {
		setCardHolderName(creditCard.getCardHolderName());
		setCardType(creditCard.getCardType());
		setExpiryMonth(creditCard.getExpiryMonth());
		setExpiryYear(creditCard.getExpiryYear());
		setIssueNumber(creditCard.getIssueNumber());
		setSecurityCode(creditCard.getSecurityCode());
		setStartMonth(creditCard.getStartMonth());
		setStartYear(creditCard.getStartYear());
		if (includeNumber) {
			setCardNumber(creditCard.getCardNumber());
		}
	}

	@Override
	public CustomerCreditCardImpl copy() {
		CustomerCreditCardImpl customerCreditCardImpl = new CustomerCreditCardImpl();
		customerCreditCardImpl.initialize();
		customerCreditCardImpl.copyFrom(this, true);
		return customerCreditCardImpl;
	}
}
