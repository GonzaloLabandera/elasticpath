/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.integration.epcommerce.personalization.impl;

import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerTagStrategy;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.service.TagFactory;

/**
 * Creates customer shared id Tag.
 */
@Component(service = CustomerTagStrategy.class)
public class CustomerSharedIdTagStrategy extends AbstractCustomerTagStrategy {

	/** Tag name. */
	@VisibleForTesting
	static final String CUSTOMER_SHARED_ID = "CUSTOMER_SHARED_ID";

	@Reference
	private TagFactory tagFactory;

	@Override
	protected String tagName() {
		return CUSTOMER_SHARED_ID;
	}

	@Override
	protected Optional<Tag> createTag(final Customer customer) {
		return Optional.of(tagFactory.createTagFromTagName(CUSTOMER_SHARED_ID, customer.getSharedId()));
	}
}
