/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.spi.capabilities;

import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.spi.CatalogWriterCapability;

/**
 * A repository for {@link Option} projections.
 */
public interface OptionWriterRepository extends CatalogWriterCapability<Option> {
}
