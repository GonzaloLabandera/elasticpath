/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.plugin.tax.common;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Extension of the Apache Commons HashCodeBuilder that makes use of the TaxCacheKeyHash instead of the usual hashcode if available.
 */
public class TaxCacheKeyHashCodeBuilder extends HashCodeBuilder {
	@Override
	public HashCodeBuilder append(final Object object) {
		if (object instanceof TaxableCacheKeyHash) {
			TaxableCacheKeyHash taxableCacheKeyHash = (TaxableCacheKeyHash) object;
			return append(taxableCacheKeyHash.getTaxCacheKeyHash());
		}
		return super.append(object);
	}
}
