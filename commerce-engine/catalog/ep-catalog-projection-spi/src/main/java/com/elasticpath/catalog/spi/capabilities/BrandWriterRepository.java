/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.spi.capabilities;

import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.spi.CatalogWriterCapability;

/**
 * A repository for {@link Brand} projections.
 */
public interface BrandWriterRepository extends CatalogWriterCapability<Brand> {
}