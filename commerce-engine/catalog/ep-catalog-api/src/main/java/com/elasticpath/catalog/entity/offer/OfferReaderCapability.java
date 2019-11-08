/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import com.elasticpath.catalog.ReadLatestVersionCapability;

/**
 * Represents an interface for Offer projections reader
 * as part of Capabilities Pattern implementation.
 */
public interface OfferReaderCapability extends ReadLatestVersionCapability<Offer> {
}
