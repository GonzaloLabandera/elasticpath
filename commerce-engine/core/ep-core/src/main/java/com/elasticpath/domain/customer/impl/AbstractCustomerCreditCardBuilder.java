/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
/*
 * 
 */
package com.elasticpath.domain.customer.impl;

import com.elasticpath.domain.customer.CustomerCreditCard;

/**
 * Abstract {@link CustomerCreditCard} builder.
 */
public abstract class AbstractCustomerCreditCardBuilder {

	private String cardHolderName;
	private String cardNumber;
	private String cardType;
	private String expiryMonth;
	private String expiryYear;
	private String guid;
	private String startYear;
	private String startMonth;
	private Integer issueNumber;
	private String securityCode;

	/**
	 * With card holder name.
	 *
	 * @param cardHolderName the card holder name
	 * @return the abstract customer credit card builder
	 */
	public AbstractCustomerCreditCardBuilder withCardHolderName(final String cardHolderName) {
		this.cardHolderName = cardHolderName;
		return this;
	}

	/**
	 * With card number.
	 *
	 * @param cardNumber the card number
	 * @return the abstract customer credit card builder
	 */
	public AbstractCustomerCreditCardBuilder withCardNumber(final String cardNumber) {
		this.cardNumber = cardNumber;
		return this;
	}

	/**
	 * With card type.
	 *
	 * @param cardType the card type
	 * @return the abstract customer credit card builder
	 */
	public AbstractCustomerCreditCardBuilder withCardType(final String cardType) {
		this.cardType = cardType;
		return this;
	}

	/**
	 * With expiry month.
	 *
	 * @param expiryMonth the expiry month
	 * @return the abstract customer credit card builder
	 */
	public AbstractCustomerCreditCardBuilder withExpiryMonth(final String expiryMonth) {
		this.expiryMonth = expiryMonth;
		return this;
	}

	/**
	 * With expiry year.
	 *
	 * @param expiryYear the expiry year
	 * @return the abstract customer credit card builder
	 */
	public AbstractCustomerCreditCardBuilder withExpiryYear(final String expiryYear) {
		this.expiryYear = expiryYear;
		return this;
	}

	/**
	 * With guid.
	 *
	 * @param guid the guid
	 * @return the abstract customer credit card builder
	 */
	public AbstractCustomerCreditCardBuilder withGuid(final String guid) {
		this.guid = guid;
		return this;
	}

	/**
	 * With start year.
	 *
	 * @param startYear the start year
	 * @return the abstract customer credit card builder
	 */
	public AbstractCustomerCreditCardBuilder withStartYear(final String startYear) {
		this.startYear = startYear;
		return this;
	}

	/**
	 * With start month.
	 *
	 * @param startMonth the start month
	 * @return the abstract customer credit card builder
	 */
	public AbstractCustomerCreditCardBuilder withStartMonth(final String startMonth) {
		this.startMonth = startMonth;
		return this;
	}

	/**
	 * With issue number.
	 *
	 * @param issueNumber the issue number
	 * @return the abstract customer credit card builder
	 */
	public AbstractCustomerCreditCardBuilder withIssueNumber(final Integer issueNumber) {
		this.issueNumber = issueNumber;
		return this;
	}

	/**
	 * With security code.
	 *
	 * @param securityCode the security code
	 * @return the abstract customer credit card builder
	 */
	public AbstractCustomerCreditCardBuilder withSecurityCode(final String securityCode) {
		this.securityCode = securityCode;
		return this;
	}

	/**
	 * Builds the customer credit card.
	 *
	 * @return the customer credit card
	 */
	public CustomerCreditCard build() {
		final CustomerCreditCard creditCard = create();
		creditCard.setCardHolderName(cardHolderName);
		creditCard.setCardNumber(cardNumber);
		creditCard.setCardType(cardType);
		creditCard.setExpiryMonth(expiryMonth);
		creditCard.setExpiryYear(expiryYear);
		creditCard.setGuid(guid);
		creditCard.setStartMonth(startMonth);
		creditCard.setStartYear(startYear);
		creditCard.setIssueNumber(issueNumber);
		creditCard.setSecurityCode(securityCode);
		return creditCard;
	}

	/**
	 * Abstract method, creates a new instance of the {@link CustomerCreditCard}.
	 *
	 * @return the customer credit card
	 */
	protected abstract CustomerCreditCard create();

}