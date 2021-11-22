/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.persistence.support.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static com.elasticpath.persistence.support.CustomerCriterion.ResultType.COUNT;
import static com.elasticpath.persistence.support.CustomerCriterion.ResultType.ENTITY;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import com.elasticpath.service.search.query.CustomerSearchCriteria;
import com.elasticpath.service.search.query.SortOrder;
import com.elasticpath.service.search.query.StandardSortBy;

/**
 * Test <code>CustomerCriterionImpl</code>.
 */
public class CustomerCriterionImplTest {

	private static final String SHAREDID = "sharedId";

	private final CustomerCriterionImpl customerCriterion = new CustomerCriterionImpl();

	private final Collection<String> stores = Collections.emptyList();

	/**
	 * Test method for 'com.elasticpath.persistence.support.impl.CustomerCriterionImpl.getCustomerSearchCriteria'.
	 */
	@Test
	public void testGetCustomerCriteria() {
		final String expectedQuery = "SELECT DISTINCT COUNT(c) FROM CustomerImpl AS c";

		CustomerSearchCriteria customerSearchCriteria = new CustomerSearchCriteria();

		CriteriaQuery result = customerCriterion.getCustomerSearchCriteria(customerSearchCriteria, stores, COUNT);
		assertThat(result.getQuery()).isEqualTo(expectedQuery);
	}

	/**
	 * Test method for 'com.elasticpath.persistence.support.impl.CustomerCriterionImpl.getCustomerSearchCriteria'.
	 */
	@Test
	public void testGetCustomerCriteriaWithParameters() {
		final String expectedQuery = "SELECT DISTINCT c FROM CustomerImpl AS c LEFT JOIN c.profileValueMap AS "
				+ "emls WHERE c.sharedId = ?1 AND (emls.localizedAttributeKey IS NULL OR emls.localizedAttributeKey = ?2)"
				+ " ORDER BY emls.shortTextValue ASC";

		CustomerSearchCriteria customerSearchCriteria = new CustomerSearchCriteria();
		customerSearchCriteria.setSharedId(SHAREDID);
		customerSearchCriteria.setSortingType(StandardSortBy.EMAIL);
		customerSearchCriteria.setSortingOrder(SortOrder.ASCENDING);

		CriteriaQuery result = customerCriterion.getCustomerSearchCriteria(customerSearchCriteria, stores, ENTITY);
		assertThat(result.getQuery()).isEqualTo(expectedQuery);
		assertThat(result.getParameters()).contains(SHAREDID);
	}
}
