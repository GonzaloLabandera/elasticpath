/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.category;

import com.elasticpath.catalog.ReadLatestVersionCapability;

/**
 * Represents an interface for Category projections reader as part of Capabilities Pattern implementation.
 */
public interface CategoryReaderCapability extends ReadLatestVersionCapability<Category> {
}
