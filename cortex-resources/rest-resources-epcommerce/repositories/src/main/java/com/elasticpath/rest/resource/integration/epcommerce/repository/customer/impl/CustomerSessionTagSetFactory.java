/*
 * Copyright © 2015 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.AccountTagStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerTagStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.UserTraitsToTagSetTransformer;
import com.elasticpath.tags.TagSet;

/**
 * Constructs a TagSet for use in a Customer Session.
 */
@Singleton
@Named("customerSessionTagSetFactory")
public class CustomerSessionTagSetFactory {

	private final ResourceOperationContext resourceOperationContext;
	private final CustomerTagStrategyRegistry tagStrategyRegistry;
	private final UserTraitsToTagSetTransformer userTraitTransformer;

	/**
	 * Constructor.
	 *
	 * @param resourceOperationContext the resource operation context.
	 * @param tagStrategyRegistry tag strategy registry.
	 * @param userTraitTransformer The customer session tagger.
	 */
	@Inject
	CustomerSessionTagSetFactory(
			@Named("resourceOperationContext")
			final ResourceOperationContext resourceOperationContext,
			@Named("customerTagStrategyRegistry")
			final CustomerTagStrategyRegistry tagStrategyRegistry,
			@Named("userTraitsToTagSetTransformer")
			final UserTraitsToTagSetTransformer userTraitTransformer) {

		this.resourceOperationContext = resourceOperationContext;
		this.tagStrategyRegistry = tagStrategyRegistry;
		this.userTraitTransformer = userTraitTransformer;
	}

	/**
	 * Create a TagSet.
	 *
	 * @param shopper the shopper to create a TagSet for
	 * @return the tag set
	 */
	TagSet createTagSet(final Shopper shopper) {
		final Subject subject = resourceOperationContext.getSubject();
		final TagSet tagSet = subject == null
			? new TagSet()
			: userTraitTransformer.transformUserTraitsToTagSet(subject);

		populateCustomerTagStrategies(shopper.getCustomer(), tagSet);

		populateAccountTagStrategies(shopper.getAccount(), tagSet);

		return tagSet;
	}

	private void populateCustomerTagStrategies(final Customer customer, final TagSet tagSet) {
		for (CustomerTagStrategy customerTagStrategy : tagStrategyRegistry.getStrategies()) {
			customerTagStrategy.populate(customer, tagSet);
		}
	}

	private void populateAccountTagStrategies(final Customer account, final TagSet tagSet) {
		if (Objects.nonNull(account)) {
			for (final AccountTagStrategy accountTagStrategy : tagStrategyRegistry.getAccountStrategies()) {
				accountTagStrategy.populate(account, tagSet);
			}
		}
	}
}
