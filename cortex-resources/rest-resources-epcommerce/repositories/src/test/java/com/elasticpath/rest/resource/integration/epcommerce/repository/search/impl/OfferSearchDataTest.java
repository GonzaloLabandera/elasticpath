/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Tests for OfferSearchData.
 */
public class OfferSearchDataTest {

	private static final String KEYWORD = "keyword";
	private static final Map<String, String> APPLIED_FACETS = new HashMap<>();
	private static final int PAGESIZE = 1;
	private static final int PAGEID = 1;

	private final OfferSearchData data = new OfferSearchData(PAGEID, PAGESIZE, SCOPE, APPLIED_FACETS, KEYWORD);

	@Test
	public void testEqualsSelf() {
		assertThat(data.equals(data)).isTrue();
	}

	@Test
	public void testEqualityOfFields() {
		OfferSearchData other = new OfferSearchData(PAGEID, PAGESIZE, SCOPE, APPLIED_FACETS, KEYWORD);
		assertThat(data.equals(other)).isTrue();
	}
}