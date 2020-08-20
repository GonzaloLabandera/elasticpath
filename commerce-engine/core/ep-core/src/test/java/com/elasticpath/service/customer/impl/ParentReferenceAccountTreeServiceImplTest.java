/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
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
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.customer.CustomerService;

/**
 * Test class for {@link ParentReferenceAccountTreeServiceImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParentReferenceAccountTreeServiceImplTest {

	private static final String NOT_ACCOUNT_MESSAGE = "This customer is not of type ACCOUNT.";
	private static final String ACCOUNT_PARENT_GUID = "1";
	private static final String ACCOUNT_CHILD_GUID = "2";

	@Mock
	private CustomerService customerService;

	@Mock
	private PersistenceEngine persistenceEngine;

	@Mock
	private Customer parent;

	@Mock
	private Customer child;

	@InjectMocks
	private ParentReferenceAccountTreeServiceImpl parentReferenceAccountTreeService;

	@Before
	public void setUp() {
		when(parent.getGuid()).thenReturn(ACCOUNT_PARENT_GUID);
		when(parent.getCustomerType()).thenReturn(CustomerType.ACCOUNT);
		when(child.getGuid()).thenReturn(ACCOUNT_CHILD_GUID);
		when(child.getCustomerType()).thenReturn(CustomerType.ACCOUNT);
		when(customerService.getPersistenceEngine()).thenReturn(persistenceEngine);
	}

	@Test
	public void testThatParentAddsChildToParent() {
		parentReferenceAccountTreeService.parent(parent, child);
		verify(child).setParentGuid(ACCOUNT_PARENT_GUID);
		verify(customerService).update(child);
	}

	@Test
	public void testThatParentThrowExceptionWhenWrongCustomerType() {
		when(child.getCustomerType()).thenReturn(CustomerType.REGISTERED_USER);

		assertThatThrownBy(() -> parentReferenceAccountTreeService.parent(parent, child))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(NOT_ACCOUNT_MESSAGE);

		when(parent.getCustomerType()).thenReturn(CustomerType.REGISTERED_USER);

		assertThatThrownBy(() -> parentReferenceAccountTreeService.parent(parent, child))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(NOT_ACCOUNT_MESSAGE);
	}

	@Test
	public void testThatParentThrowExceptionWhenChildHasParent() {
		when(child.getParentGuid()).thenReturn(ACCOUNT_PARENT_GUID);

		assertThatThrownBy(() -> parentReferenceAccountTreeService.parent(parent, child))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("The child customer already has a parent.");
	}


	@Test
	public void testThatParentThrowExceptionWhenChildIsRoot() {
		when(persistenceEngine.retrieveByNamedQuery("ACCOUNT_PARENT_GUID_BY_CHILD_GUID", ACCOUNT_PARENT_GUID))
				.thenReturn(Collections.singletonList(ACCOUNT_CHILD_GUID));

		assertThatThrownBy(() -> parentReferenceAccountTreeService.parent(parent, child))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("The child customer is a root of the parents' tree");
	}


	@Test
	public void testThatFetchSubtreeReturnsGuidsOfSubtreeMembers() {
		final List<Object> childGuids = Collections.singletonList(ACCOUNT_CHILD_GUID);
		when(persistenceEngine.retrieveByNamedQueryWithList("ACCOUNT_CHILDREN_GUIDS_BY_PARENT_GUIDS",
				"guidsThisLevel", Collections.singletonList(ACCOUNT_PARENT_GUID))).thenReturn(childGuids);

		final List<String> subtree = parentReferenceAccountTreeService.fetchSubtree(parent);
		assertThat(subtree).isEqualTo(childGuids);
	}

	@Test
	public void testThatFetchSubtreeThrowExceptionWhenWrongCustomerType() {
		when(parent.getCustomerType()).thenReturn(CustomerType.REGISTERED_USER);

		assertThatThrownBy(() -> parentReferenceAccountTreeService.fetchSubtree(parent))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(NOT_ACCOUNT_MESSAGE);
	}

	@Test
	public void testThatFetchPathToRootReturnsPathOfGuidsToRoot() {
		when(persistenceEngine.retrieveByNamedQuery("ACCOUNT_PARENT_GUID_BY_CHILD_GUID", ACCOUNT_CHILD_GUID))
				.thenReturn(Collections.singletonList(ACCOUNT_PARENT_GUID));

		final List<String> pathToRoot = parentReferenceAccountTreeService.fetchPathToRoot(child);
		assertThat(pathToRoot.get(0)).isEqualTo(ACCOUNT_PARENT_GUID);
	}

	@Test
	public void testThatFetchPathToRootThrowExceptionWhenWrongCustomerType() {
		when(parent.getCustomerType()).thenReturn(CustomerType.REGISTERED_USER);

		assertThatThrownBy(() -> parentReferenceAccountTreeService.fetchPathToRoot(parent))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(NOT_ACCOUNT_MESSAGE);
	}

	@Test
	public void testThatFetchChildAccountGuidsReturnsChildAccountGuids() {
		final List<Object> childGuids = Collections.singletonList(ACCOUNT_CHILD_GUID);
		when(persistenceEngine.retrieveByNamedQueryWithList("ACCOUNT_CHILDREN_GUIDS_BY_PARENT_GUIDS",
				"guidsThisLevel", Collections.singletonList(ACCOUNT_PARENT_GUID))).thenReturn(childGuids);

		final List<String> childAccountGuids = parentReferenceAccountTreeService.fetchChildAccountGuids(parent);
		assertThat(childAccountGuids.get(0)).isEqualTo(ACCOUNT_CHILD_GUID);
	}

	@Test
	public void testThatFetchChildAccountGuidsThrowExceptionWhenWrongCustomerType() {
		when(parent.getCustomerType()).thenReturn(CustomerType.REGISTERED_USER);

		assertThatThrownBy(() -> parentReferenceAccountTreeService.fetchChildAccountGuids(parent))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage(NOT_ACCOUNT_MESSAGE);
	}
}
