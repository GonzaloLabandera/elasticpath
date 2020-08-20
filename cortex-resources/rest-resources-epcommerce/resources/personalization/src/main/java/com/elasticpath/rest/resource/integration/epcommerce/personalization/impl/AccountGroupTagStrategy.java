/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.integration.epcommerce.personalization.impl;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.AccountTagStrategy;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.service.TagFactory;

/**
 * Creates account group tag.
 */
@Component(service = AccountTagStrategy.class)
public class AccountGroupTagStrategy extends AbstractAccountTagStrategy {

	/** Name separator char. */
	@VisibleForTesting
	static final String NAME_SEPARATOR = ",";

	/** Tag name. */
	@VisibleForTesting
	static final String ACCOUNT_SEGMENT = "ACCOUNT_SEGMENT";

	@Reference
	private TagFactory tagFactory;

	@Override
	protected String tagName() {
		return ACCOUNT_SEGMENT;
	}

	@Override
	protected Optional<Tag> createTag(final Customer account) {
		final Collection<CustomerGroup> customerGroups = account.getCustomerGroups();
		if (customerGroups.isEmpty()) {
			return Optional.empty();
		}

		final String value = customerGroups.stream()
				.filter(CustomerGroup::isEnabled)
				.map(CustomerGroup::getName)
				.collect(Collectors.joining(NAME_SEPARATOR));

		return Optional.of(tagFactory.createTagFromTagName(ACCOUNT_SEGMENT, value));
	}
}