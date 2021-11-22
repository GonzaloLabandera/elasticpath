/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.test.integration.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.service.customer.AccountTreeService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Account Tree Service itest
 */
public class ParentReferenceAccountTreeServiceTest extends DbTestCase {

	@Autowired
	private AccountTreeService accountTreeService;

	@Autowired
	private CustomerService customerService;

	private Customer parent;

	private Customer child;

	private Customer root;

	private static final String CUSTOMER_NAME1 = "testparent@AccountTreeService.com";

	private static final String CUSTOMER_NAME2 = "testchild@AccountTreeService.com";

	private static final String CUSTOMER_NAME3 = "testroot@AccountTreeService.com";

	@Before
	public void setUp() {
		parent = createAccount(CUSTOMER_NAME1);
		child = createAccount(CUSTOMER_NAME2);
		root = createAccount(CUSTOMER_NAME3);
	}

	@DirtiesDatabase
	@Test
	public void testThatParentThrowExceptionWhenChildIsRoot() {
		accountTreeService.insertClosures(parent.getGuid(), root.getGuid());

		assertThatThrownBy(() -> accountTreeService.insertClosures(root.getGuid(), parent.getGuid()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("The child customer is a root of the parents' tree");
	}

	@DirtiesDatabase
	@Test
	public void testThatFetchSubtreeReturnsGuidsOfSubtreeMembers() {
		accountTreeService.insertClosures(parent.getGuid(), root.getGuid());
		accountTreeService.insertClosures(child.getGuid(), parent.getGuid());

		parent = customerService.findByGuid(parent.getGuid());
		root = customerService.findByGuid(root.getGuid());
		child = customerService.findByGuid(child.getGuid());

		final List<String> subtree = accountTreeService.findDescendantGuids(root.getGuid());
		assertThat(subtree).contains(parent.getGuid()).contains(child.getGuid());
	}

	@DirtiesDatabase
	@Test
	public void testThatFetchRootReturnsPathOfGuidsToRoot() {
		accountTreeService.insertClosures(parent.getGuid(), root.getGuid());
		accountTreeService.insertClosures(child.getGuid(), parent.getGuid());

		final List<String> pathToRoot = accountTreeService.findAncestorGuids(child.getGuid());
		assertThat(pathToRoot.get(0)).isEqualTo(root.getGuid());
		assertThat(pathToRoot.get(1)).isEqualTo(parent.getGuid());
	}

	private Customer createAccount(final String sharedId) {
		Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setSharedId(sharedId);
		customer.setCustomerType(CustomerType.ACCOUNT);
		customer.setBusinessName(sharedId);
		customer = customerService.add(customer);
		return customer;
	}
}
