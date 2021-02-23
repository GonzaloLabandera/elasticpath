/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.Map;

/**
 * Holder for accountStatusId to account status map.
 */
public interface AccountStatusIdToAccountStatusMapHolder {

	/**
	 * Obtains string representation of given account status enum accountStatusId.
	 *
	 * @param accountStatusId account status enum accountStatusId
	 * @return String representation of enum associated with given accountStatusId
	 */
	String getAccountStatusById(int accountStatusId);

	/**
	 * Sets the account status Id to account status map.
	 *
	 * @param accountStatusIdToAccountStatusMap account status id to account status map.
	 */
	void setAccountStatusIdToAccountStatusMap(Map<Integer, String> accountStatusIdToAccountStatusMap);
}
