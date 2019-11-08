/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.spi.capabilities;

import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.spi.CatalogWriterCapability;

/**
 * A repository for {@link Offer} projections.
 */
public interface OfferWriterRepository extends CatalogWriterCapability<Offer> {
}
