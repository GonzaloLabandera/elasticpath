/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.spi.capabilities;

import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.spi.CatalogWriterCapability;

/**
 * A repository for {@link Category} projections.
 */
public interface CategoryWriterRepository extends CatalogWriterCapability<Category> {
}
