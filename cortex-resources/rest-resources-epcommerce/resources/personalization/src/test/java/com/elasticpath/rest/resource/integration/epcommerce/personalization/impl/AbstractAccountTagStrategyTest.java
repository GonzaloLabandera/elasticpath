/**
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.personalization.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.AccountTagStrategy;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;

/**
 * Test the populate code path for edge cases.
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractAccountTagStrategyTest {

	private static final String TAG_NAME = "tagName";
	private static final String TAG_VALUE = "tagValue";

	private final AccountTagStrategy classUnderTest = new AbstractAccountTagStrategy() {
		@Override
		protected String tagName() {
			return TAG_NAME;
		}

		@Override
		protected Optional<Tag> createTag(final Customer customer) {
			return Optional.of(new Tag(TAG_VALUE));
		}
	};

	@Test
	public void testPopulateAddsNewTag() {
		final TagSet tagSet = new TagSet();

		classUnderTest.populate(null, tagSet);
		assertThat(tagSet.getTags()).hasSize(1);
		assertThat(tagSet.getTagValue(TAG_NAME).getValue()).isEqualTo(TAG_VALUE);
	}
}