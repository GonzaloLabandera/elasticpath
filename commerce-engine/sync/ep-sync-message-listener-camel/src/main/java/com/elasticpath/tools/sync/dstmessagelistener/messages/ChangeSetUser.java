/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.messages;

/**
 * A user involved in the creation or publishing of a Change Set.
 */
public interface ChangeSetUser {

	/**
	 * Returns the user GUID.
	 *
	 * @return the user GUID
	 */
	String getGuid();

	/**
	 * Returns the username.
	 *
	 * @return the username
	 */
	String getUsername();

	/**
	 * Returns the first name.
	 *
	 * @return the first name
	 */
	String getFirstName();

	/**
	 * Returns the last name.
	 *
	 * @return the last name
	 */
	String getLastName();

	/**
	 * Returns the email address.
	 *
	 * @return the email address
	 */
	String getEmailAddress();

}
