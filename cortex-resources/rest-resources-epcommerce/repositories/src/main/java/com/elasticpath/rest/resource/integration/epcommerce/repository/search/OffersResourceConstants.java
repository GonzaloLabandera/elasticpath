/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Constants for Offers Resource.
 */
public final class OffersResourceConstants {

	/**
	 * Cache-Control max age.
	 */
	public static final int MAX_AGE = 600;

	/**
	 * Name of facet selector.
	 */
	public static final String FACET_SELECTOR_NAME = "facet-selector";

	/**
	 * Name of sort selector.
	 */
	public static final String SORT_SELECTOR_NAME = "sort-attribute-selector";

	/**
	 * Rule for facet multi-selectors.
	 */
	public static final String SELECTION_RULE_MANY = "many";

	/**
	 * Rule for single selectors.
	 */
	public static final String SELECTION_RULE_ONE = "1";

	/**
	 * Rule for facet multi-selectors.
	 */
	public static final String CATEGORY_CODE_PROPERTY = "category-code";

	/**
	 * Default applied facet map for applied facet identifier.
	 */
	public static final Map<String, String> DEFAULT_APPLIED_FACETS = ImmutableMap.of("", "");

	/**
	 * Sort key.
	 */
	public static final String SORT = "sort";

	private OffersResourceConstants() {
		// prevent instantiation
	}

}
