/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.orderpaymentapi.impl;

/**
 * Implementation for object encapsulating customer information required during payment instrument creation.
 */
public class CustomerContext {
	private String customerId;
	private String firstName;
	private String lastName;
	private String emailAddress;

	/**
	 * Constructor.
	 *
	 * @param customerId   customer identifier
	 * @param firstName    customer first name
	 * @param lastName     customer last name
	 * @param emailAddress customer email address
	 */
	public CustomerContext(final String customerId, final String firstName, final String lastName, final String emailAddress) {
		this.customerId = customerId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
	}

	/**
	 * Returns the assigned customer identifier.
	 *
	 * @return the unique {@link com.elasticpath.domain.customer.Customer} identifier
	 */
	public String getCustomerId() {
		return customerId;
	}

	/**
	 * Set the assigned customer identifier.
	 *
	 * @param customerId the unique {@link com.elasticpath.domain.customer.Customer} identifier
	 */
	public void setCustomerId(final String customerId) {
		this.customerId = customerId;
	}

	/**
	 * Returns the customer's first name.
	 *
	 * @return {@link com.elasticpath.domain.customer.Customer} first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Set the customer's first name.
	 *
	 * @param firstName {@link com.elasticpath.domain.customer.Customer} first name
	 */
	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Returns the customer's last name.
	 *
	 * @return {@link com.elasticpath.domain.customer.Customer} last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Set the customer's last name.
	 *
	 * @param lastName {@link com.elasticpath.domain.customer.Customer} last name
	 */
	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Returns the customer's email address.
	 *
	 * @return {@link com.elasticpath.domain.customer.Customer} email address
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * Set the customer's email address.
	 *
	 * @param emailAddress {@link com.elasticpath.domain.customer.Customer} email address
	 */
	public void setEmailAddress(final String emailAddress) {
		this.emailAddress = emailAddress;
	}
}
