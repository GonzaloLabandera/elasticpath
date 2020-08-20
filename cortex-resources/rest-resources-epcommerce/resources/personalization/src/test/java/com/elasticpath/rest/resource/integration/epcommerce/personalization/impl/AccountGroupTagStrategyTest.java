/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.integration.epcommerce.personalization.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.personalization.impl.AccountGroupTagStrategy.ACCOUNT_SEGMENT;
import static com.elasticpath.rest.resource.integration.epcommerce.personalization.impl.AccountGroupTagStrategy.NAME_SEPARATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.service.TagFactory;

/**
 * Test class for {@link AccountGroupTagStrategy}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountGroupTagStrategyTest {

	private static final String CUSTOMER_GROUP_NAME_1 = "Group1";
	private static final String CUSTOMER_GROUP_NAME_2 = "Group2";
	private static final String CUSTOMER_GROUP_NAME_3 = "Group3";

	@Mock
	private TagFactory tagFactory;
	@Mock
	private Customer account;

	@InjectMocks
	private AccountGroupTagStrategy classUnderTest;

	@Test
	public void testTagName() {
		final String tagName = classUnderTest.tagName();

		assertEquals(ACCOUNT_SEGMENT, tagName);
	}

	@Test
	public void testGetAccountGroupTag() {
		arrangeSuccessFlowMocks();

		final Optional<Tag> result = classUnderTest.createTag(account);

		assertTrue("Expected tag to be present", result.isPresent());
		final Tag actual = result.get();
		assertEquals("Expected customerGroup does not match result.", CUSTOMER_GROUP_NAME_1, actual.getValue());
	}

	@Test
	public void verifyMultipleCustomerGroupsHandledCorrectly() {
		final List<CustomerGroup> customerGroups = arrangeMultipleCustomerGroups();
		arrangeSuccessFlowMocks(customerGroups);

		final Optional<Tag> result = classUnderTest.createTag(account);

		assertTrue("Expected tag to be present", result.isPresent());
		final Tag actual = result.get();
		final String expectedTagValue = Joiner.on(NAME_SEPARATOR)
				.join(CUSTOMER_GROUP_NAME_1, CUSTOMER_GROUP_NAME_2, CUSTOMER_GROUP_NAME_3);
		assertEquals("Expected customerGroup does not match result.", expectedTagValue, actual.getValue());
	}

	@Test
	public void verifyDisabledCustomerGroupsNotIncludedInSubjectAttributeValue() {
		final List<CustomerGroup> customerGroups = arrangeMultipleCustomerGroups();
		final CustomerGroup disabledGroup = customerGroups.get(1);
		when(disabledGroup.isEnabled()).thenReturn(Boolean.FALSE);
		arrangeSuccessFlowMocks(customerGroups);

		final Optional<Tag> result = classUnderTest.createTag(account);

		assertTrue("Expected tag to be present", result.isPresent());
		final Tag actual = result.get();
		assertFalse("The resulting value should not contain the group name of the disabled Customer Group.",
				actual.getValue().toString().contains(disabledGroup.getName()));
	}

	private void arrangeSuccessFlowMocks() {
		final List<CustomerGroup> customerGroups = Collections.singletonList(createMockCustomerGroup(CUSTOMER_GROUP_NAME_1));
		arrangeSuccessFlowMocks(customerGroups);
	}

	private List<CustomerGroup> arrangeMultipleCustomerGroups() {
		return ImmutableList.of(
			createMockCustomerGroup(CUSTOMER_GROUP_NAME_1),
			createMockCustomerGroup(CUSTOMER_GROUP_NAME_2),
			createMockCustomerGroup(CUSTOMER_GROUP_NAME_3));
	}

	private CustomerGroup createMockCustomerGroup(final String groupName) {
		final CustomerGroup customerGroup = mock(CustomerGroup.class);
		when(customerGroup.getName()).thenReturn(groupName);
		when(customerGroup.isEnabled()).thenReturn(Boolean.TRUE);
		return customerGroup;
	}

	private void arrangeSuccessFlowMocks(final List<CustomerGroup> customerGroups) {
		when(account.getCustomerGroups()).thenReturn(customerGroups);
		final String value = customerGroups.stream()
				.filter(CustomerGroup::isEnabled)
				.map(CustomerGroup::getName)
				.collect(Collectors.joining(NAME_SEPARATOR));

		when(tagFactory.createTagFromTagName(ACCOUNT_SEGMENT, value))
			.thenReturn(new Tag(value));
	}
}
