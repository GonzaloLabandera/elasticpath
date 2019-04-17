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
	public static final String SELECTOR_NAME = "facet-selector";

	/**
	 * Rule for facet multi-selectors.
	 */
	public static final String SELECTION_RULE = "many";

	/**
	 * Rule for facet multi-selectors.
	 */
	public static final String CATEGORY_CODE_PROPERTY = "category-code";

	/**
	 * Default applied facet map for applied facet identifier.
	 */
	public static final Map<String, String> DEFAULT_APPLIED_FACETS = ImmutableMap.of("", "");

	private OffersResourceConstants() {
		// prevent instantiation
	}

}
