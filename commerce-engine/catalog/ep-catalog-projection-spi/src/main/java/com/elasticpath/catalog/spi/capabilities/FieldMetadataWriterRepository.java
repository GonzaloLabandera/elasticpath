/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.spi.capabilities;

import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.spi.CatalogWriterCapability;

/**
 * A repository for {@link FieldMetadata} projections.
 */
public interface FieldMetadataWriterRepository extends CatalogWriterCapability<FieldMetadata> {
}