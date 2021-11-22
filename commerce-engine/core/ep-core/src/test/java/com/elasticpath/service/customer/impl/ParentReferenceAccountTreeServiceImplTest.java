/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.customer.CustomerService;

/**
 * Test class for {@link ParentReferenceAccountTreeServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParentReferenceAccountTreeServiceImplTest {

	private static final String ACCOUNT_PARENT_GUID = "1";
	private static final String ACCOUNT_CHILD_GUID = "2";
	private static final int MAX_RESULTS = 5;
	private static final int FIRST_RESULT = 1;

	@Mock
	private CustomerService customerService;

	@Mock
	private PersistenceEngine persistenceEngine;

	@Mock
	private Customer parent;

	@InjectMocks
	private ParentReferenceAccountTreeServiceImpl parentReferenceAccountTreeService;

	@Before
	public void setUp() {
		when(parent.getGuid()).thenReturn(ACCOUNT_PARENT_GUID);
		when(customerService.getPersistenceEngine()).thenReturn(persistenceEngine);
	}

	@Test
	public void testThatFetchChildAccountGuidsReturnsChildAccountGuids() {
		final List<Object> childGuids = Collections.singletonList(ACCOUNT_CHILD_GUID);
		when(persistenceEngine.retrieveByNamedQueryWithList("ACCOUNT_CHILDREN_GUIDS_BY_PARENT_GUIDS",
				"guidsThisLevel", Collections.singletonList(ACCOUNT_PARENT_GUID), new Object[]{}, FIRST_RESULT, MAX_RESULTS)).thenReturn(childGuids);

		final List<String> childAccountGuids = parentReferenceAccountTreeService.findChildGuidsPaginated(parent.getGuid(),
				FIRST_RESULT, MAX_RESULTS);
		assertThat(childAccountGuids.get(0)).isEqualTo(ACCOUNT_CHILD_GUID);
	}

	@Test
	public void testThatFetchChildAccountGuidsPaginatedReturnsChildAccountGuids() {
		final List<Object> childGuids = Collections.singletonList(ACCOUNT_CHILD_GUID);
		when(persistenceEngine.retrieveByNamedQueryWithList("ACCOUNT_CHILDREN_GUIDS_BY_PARENT_GUIDS",
				"guidsThisLevel", Collections.singletonList(ACCOUNT_PARENT_GUID))).thenReturn(childGuids);

		final List<String> childAccountGuids = parentReferenceAccountTreeService.fetchChildAccountGuids(parent);
		assertThat(childAccountGuids.get(0)).isEqualTo(ACCOUNT_CHILD_GUID);
	}
}
