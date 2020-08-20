/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.integration.epcommerce.personalization.impl;

import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.AccountTagStrategy;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.service.TagFactory;

/**
 * Creates account shared id Tag.
 */
@Component(service = AccountTagStrategy.class)
public class AccountSharedIdTagStrategy extends AbstractAccountTagStrategy {

	/** Tag name. */
	@VisibleForTesting
	static final String ACCOUNT_SHARED_ID = "ACCOUNT_SHARED_ID";

	@Reference
	private TagFactory tagFactory;

	@Override
	protected String tagName() {
		return ACCOUNT_SHARED_ID;
	}

	@Override
	protected Optional<Tag> createTag(final Customer account) {
		return Optional.of(tagFactory.createTagFromTagName(ACCOUNT_SHARED_ID, account.getSharedId()));
	}
}