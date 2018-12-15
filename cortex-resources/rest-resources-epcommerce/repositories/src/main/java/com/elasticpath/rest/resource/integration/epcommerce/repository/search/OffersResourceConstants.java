/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search;

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

	private OffersResourceConstants() {
		// prevent instantiation
	}

}
