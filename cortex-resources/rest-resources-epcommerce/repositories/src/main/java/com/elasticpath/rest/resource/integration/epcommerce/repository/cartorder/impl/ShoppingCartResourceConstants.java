/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

/**
 * Constants for Shopping cart resource.
 */
public final class ShoppingCartResourceConstants {

	/**
	 * Error message used when default cart can't be found.
	 */
	@VisibleForTesting
	public static final String DEFAULT_CART_NOT_FOUND = "Default cart cannot be found.";
	/**
	 * Error message used when line item was not found.
	 */
	public static final String LINEITEM_WAS_NOT_FOUND = "No line item was found with GUID = %s.";

	/**
	 * Subject Attribute Metadata identifier.
	 */
	public static final String METADATA = "METADATA";

	/**
	 * Subject Attribute User-id identifier.
	 */
	public static final String SUBJECT_ATTRIBUTE_USER_ID = "user-id";
	/**
	 * Subject Attribute User name identifier.
	 */
	public static final String SUBJECT_ATTRIBUTE_USER_NAME = "user-name";

	/**
	 * Subject Attribute User email identifier.
	 */
	public static final String SUBJECT_ATTRIBUTE_USER_EMAIL = "user-email";

	/**
	 * Error message when creating a cart isn't supported.
	 */
	public static final String CREATE_CART_NOT_SUPPORTED = "Create cart not supported";
	/**
	 * All the identifiers.
	 */
	public static final List<String> SUBJECT_ATTRIBUTE_IDENTIFIERS
			= ImmutableList.of(SUBJECT_ATTRIBUTE_USER_ID, SUBJECT_ATTRIBUTE_USER_EMAIL, SUBJECT_ATTRIBUTE_USER_NAME);

	/**
	 * Private constructor.
	 */
	private ShoppingCartResourceConstants() {
		//no-op private constructor.
	}
}
