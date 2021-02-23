/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Implementation of {@link AccountStatusIdToAccountStatusMapHolder}.
 */
public class AccountStatusIdToAccountStatusMapHolderImpl implements AccountStatusIdToAccountStatusMapHolder {

	/**
	 * Map holding accountStatusId to account status mapping.
	 */
	private Map<Integer, String> accountStatusIdToAccountStatusMap;


	@Override
	public String getAccountStatusById(final int accountStatusId) {
		return accountStatusIdToAccountStatusMap.getOrDefault(accountStatusId, StringUtils.EMPTY);
	}

	@Override
	public void setAccountStatusIdToAccountStatusMap(final Map<Integer, String> accountStatusIdToAccountStatusMap) {
		this.accountStatusIdToAccountStatusMap = accountStatusIdToAccountStatusMap;
	}
}
