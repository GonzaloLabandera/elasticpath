/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.dto;

/**
 * CustomerContextDTO builder.
 */
public final class CustomerContextDTOBuilder {
	private String userId;
	private String lastName;
	private String firstName;
	private String email;

	private CustomerContextDTOBuilder() {
	}

	/**
	 * CustomerContextDTO builder.
	 *
	 * @return the builder
	 */
	public static CustomerContextDTOBuilder builder() {
		return new CustomerContextDTOBuilder();
	}

	/**
	 * With customer user id.
	 *
	 * @param userId customer user id.
	 *
	 * @return the builder
	 */
	public CustomerContextDTOBuilder withUserId(final String userId) {
		this.userId = userId;
		return this;
	}

	/**
	 * With customer last name.
	 *
	 * @param lastName customer last name.
	 *
	 * @return the builder
	 */
	public CustomerContextDTOBuilder withLastName(final String lastName) {
		this.lastName = lastName;
		return this;
	}

	/**
	 * With customer first name.
	 *
	 * @param firstName customer last name.
	 *
	 * @return the builder
	 */
	public CustomerContextDTOBuilder withFirstName(final String firstName) {
		this.firstName = firstName;
		return this;
	}

	/**
	 * With customer email.
	 *
	 * @param email customer email.
	 *
	 * @return the builder
	 */
	public CustomerContextDTOBuilder withEmail(final String email) {
		this.email = email;
		return this;
	}

	/**
	 * Build customerContext DTO.
	 *
	 * @param prototype bean prototype
	 * @return customerContext DTO
	 */
	public CustomerContextDTO build(final CustomerContextDTO prototype) {
		if (userId == null) {
			throw new IllegalStateException("Builder is not fully initialized, userId is missing");
		}
		prototype.setUserId(userId);
		prototype.setFirstName(firstName);
		prototype.setLastName(lastName);
		prototype.setEmail(email);
		return prototype;
	}
}
