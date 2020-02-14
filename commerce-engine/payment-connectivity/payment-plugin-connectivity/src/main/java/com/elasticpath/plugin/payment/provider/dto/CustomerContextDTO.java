/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.dto;

/**
 * Class that represents information about customer.
 */
public class CustomerContextDTO {
	private String userId;
	private String lastName;
	private String firstName;
	private String email;

	/**
	 * Get customer user id.
	 *
	 * @return customer user id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Set customer user id.
	 *
	 * @param userId customer user id.
	 */
	public void setUserId(final String userId) {
		this.userId = userId;
	}

	/**
	 * Get customer last name.
	 *
	 * @return customer last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Set customer last name.
	 *
	 * @param lastName customer's last name.
	 */
	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Get customer first name.
	 *
	 * @return customer first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Set customer first name.
	 *
	 * @param firstName the customer first name.
	 */
	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Get customer email.
	 *
	 * @return customer email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Set customer email.
	 *
	 * @param email the customer email.
	 */
	public void setEmail(final String email) {
		this.email = email;
	}
}
