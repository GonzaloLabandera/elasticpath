/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.domain.customer;

import com.elasticpath.persistence.api.Persistable;

/**
 * The User Account Association.
 */
public interface UserAccountAssociation extends Persistable {

	/**
	 * Get the UIDPK.
	 *
	 * @return the UIDPK
	 */
	long getUidPk();

	/**
	 * Set the UIDPK.
	 *
	 * @param uidPk the new unique identifier.
	 */
	void setUidPk(long uidPk);

	/**
	 * Get GUID.
	 *
	 * @return the GUID
	 */
	String getGuid();

	/**
	 * Set GUID.
	 *
	 * @param guid the GUID
	 */
	void setGuid(String guid);

	/**
	 * Get the user customer guid.
	 *
	 * @return the user guid
	 */
	String getUserGuid();

	/**
	 * Set the user customer guid.
	 *
	 * @param user the user guid
	 */
	void setUserGuid(String user);

	/**
	 * Get the account customer guid.
	 *
	 * @return the account guid
	 */
	String getAccountGuid();

	/**
	 * Set account customer guid.
	 *
	 * @param account the account guid
	 */
	void setAccountGuid(String account);

	/**
	 * Get the Account Role.
	 *
	 * @return the account role
	 */
	AccountRole getAccountRole();

	/**
	 * Set the Account Role.
	 *
	 * @param accountRole the account role
	 */
	void setAccountRole(AccountRole accountRole);
}
