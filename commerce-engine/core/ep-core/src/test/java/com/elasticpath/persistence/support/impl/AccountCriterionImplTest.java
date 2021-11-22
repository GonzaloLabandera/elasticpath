/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.persistence.support.impl;

import static com.elasticpath.persistence.support.AccountCriterion.ResultType.COUNT;
import static com.elasticpath.persistence.support.AccountCriterion.ResultType.ENTITY;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.service.search.query.AccountSearchCriteria;
import com.elasticpath.service.search.query.SortOrder;
import com.elasticpath.service.search.query.StandardSortBy;


/**
 * Test <code>AccountCriterionImpl</code>.
 */
public class AccountCriterionImplTest {

	private static final String SHAREDID = "sharedId";

	private final AccountCriterionImpl accountCriterion = new AccountCriterionImpl();

	/**
	 * Test method for 'com.elasticpath.persistence.support.impl.AccountCriterionImpl.getAccountSearchCriteria'.
	 */
	@Test
	public void testGetAccountCriteria() {
		final String expectedQuery = "SELECT DISTINCT COUNT(c) FROM CustomerImpl AS c WHERE c.customerType = ?1";

		AccountSearchCriteria accountSearchCriteria = new AccountSearchCriteria();

		CriteriaQuery result = accountCriterion.getAccountSearchCriteria(accountSearchCriteria, COUNT);
		assertThat(result.getQuery()).isEqualTo(expectedQuery);
	}

	/**
	 * Test method for 'com.elasticpath.persistence.support.impl.AccountCriterionImpl.getAccountSearchCriteria'.
	 */
	@Test
	public void testGetAccountCriteriaWithParameters() {
		final String expectedQuery = "SELECT DISTINCT c FROM CustomerImpl AS c LEFT JOIN c.profileValueMap AS nmbr WHERE c.customerType = ?1 "
				+ "AND c.sharedId = ?2 ORDER BY nmbr.shortTextValue ASC";

		AccountSearchCriteria accountSearchCriteria = new AccountSearchCriteria();
		accountSearchCriteria.setSharedId(SHAREDID);
		accountSearchCriteria.setSortingType(StandardSortBy.BUSINESS_NUMBER);
		accountSearchCriteria.setSortingOrder(SortOrder.ASCENDING);

		CriteriaQuery result = accountCriterion.getAccountSearchCriteria(accountSearchCriteria, ENTITY);
		assertThat(result.getQuery()).isEqualTo(expectedQuery);
		assertThat(result.getParameters()).contains(CustomerType.ACCOUNT);
		assertThat(result.getParameters()).contains(SHAREDID);
	}
}
