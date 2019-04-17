/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.messages.impl;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.elasticpath.tools.sync.dstmessagelistener.messages.ChangeSetUser;

/**
 * Implementation of {@link ChangeSetUser}.
 */
public class ChangeSetUserImpl implements ChangeSetUser {

	private final String guid;
	private final String username;
	private final String firstName;
	private final String lastName;
	private final String emailAddress;

	/**
	 * Constructor.
	 *
	 * @param guid the user's GUID
	 * @param username the user's username
	 * @param firstName the user's first name
	 * @param lastName the user's last name
	 * @param emailAddress the user's email address
	 */
	public ChangeSetUserImpl(final String guid,
							 final String username,
							 final String firstName,
							 final String lastName,
							 final String emailAddress) {
		this.guid = guid;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
	}

	@Override
	public String getGuid() {
		return guid;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	@Override
	public String getEmailAddress() {
		return emailAddress;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		final ChangeSetUserImpl that = (ChangeSetUserImpl) other;

		final boolean guidIsEqual = Objects.equals(guid, that.guid);
		final boolean usernameIsEqual = Objects.equals(username, that.username);
		final boolean nameIsEqual = Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName);
		final boolean emailAddressIsEqual = Objects.equals(emailAddress, that.emailAddress);

		return guidIsEqual && usernameIsEqual && nameIsEqual && emailAddressIsEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				guid,
				username,
				firstName,
				lastName,
				emailAddress
		);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
