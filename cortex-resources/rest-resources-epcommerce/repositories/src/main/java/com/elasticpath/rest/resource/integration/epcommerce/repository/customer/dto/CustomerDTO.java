/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.dto;

import javax.json.JsonObject;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * User that created from metadata values.
 */
public class CustomerDTO {
	private final String sharedId;
	private final String email;
	private final String firstName;
	private final String lastName;
	private final String username;
	private final String userCompany;
	private final String storeCode;

	/**
	 * Constructor.
	 *
	 * @param metadata  metadata needed to populate the DTO
	 * @param storeCode store Code
	 */
	public CustomerDTO(final JsonObject metadata, final String storeCode) {
		this.sharedId = StringUtils.firstNonBlank(metadata.getString("user-id", null));
		this.email = StringUtils.firstNonBlank(metadata.getString("user-email", null));
		this.firstName = StringUtils.firstNonBlank(metadata.getString("first-name", null));
		this.lastName = StringUtils.firstNonBlank(metadata.getString("last-name", null));
		this.username = StringUtils.firstNonBlank(metadata.getString("user-name", null));
		this.userCompany = StringUtils.firstNonBlank(metadata.getString("user-company", null));
		this.storeCode = storeCode;
	}

	public String getSharedId() {
		return sharedId;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getUsername() {
		return username;
	}

	public String getUserCompany() {
		return userCompany;
	}

	public String getStoreCode() {
		return storeCode;
	}

	@Override
	public boolean equals(final Object instance) {
		if (this == instance) {
			return true;
		}
		if (instance == null || getClass() != instance.getClass()) {
			return false;
		}
		CustomerDTO that = (CustomerDTO) instance;
		return Objects.equal(sharedId, that.sharedId)
				&& Objects.equal(email, that.email)
				&& Objects.equal(firstName, that.firstName)
				&& Objects.equal(lastName, that.lastName)
				&& Objects.equal(username, that.username)
				&& Objects.equal(userCompany, that.userCompany)
				&& Objects.equal(storeCode, that.storeCode);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(sharedId, email, firstName, lastName, username, userCompany, storeCode);
	}
}
