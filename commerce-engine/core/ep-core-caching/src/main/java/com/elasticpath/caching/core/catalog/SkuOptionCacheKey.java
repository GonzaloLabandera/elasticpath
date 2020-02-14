/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.caching.core.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;

/**
 * The key class used for caching SKU options. The {@link SkuOptionCacheKey#equals} method is implemented differently to enable
 * searches using a partial key. The search performance is O(N) but the number of SKU options is rarely high (going over 100).
 *
 * The tests, conducted on Ehcache cache with 100K and 1M elements, showed that sequential search is satisfactory (4-17ms for 100K; 25-90ms for 1M).
 *
 * With 10K (or less) of elements, the search performs like O(1).
 */
@SuppressWarnings("PMD.ShortMethodName")
public final class SkuOptionCacheKey {

	private final List<Long> productTypeUids = new ArrayList<>();
	private final String optionKeyIdx;

	private SkuOptionCacheKey(final String optionKey) {
		this.optionKeyIdx = optionKey;
	}
	private SkuOptionCacheKey(final Long productTypeUid) {
		this.optionKeyIdx = null;
		this.productTypeUids.add(productTypeUid);
	}

	/**
	 * Setter for option key field.
	 *
	 * @param optionKey the option key.
	 * @return current key instance.
	 */
	public static SkuOptionCacheKey of(final String optionKey) {
		return new SkuOptionCacheKey(optionKey);
	}

	/**
	 * Setter for product type ID.
	 *
	 * @param productTypeUid the product type ID.
	 * @return current key instance.
	 */
	public static SkuOptionCacheKey of(final Long productTypeUid) {
		return new SkuOptionCacheKey(productTypeUid);
	}

	/**
	 * Setter for product type ID.
	 *
	 * @param productTypeUid the product type ID.
	 * @return current key instance.
	 */
	public SkuOptionCacheKey withProductTypeUid(final Long productTypeUid) {
		this.productTypeUids.add(productTypeUid);
		return this;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		SkuOptionCacheKey that = (SkuOptionCacheKey) other;

		return Objects.equals(optionKeyIdx, that.optionKeyIdx)
			|| CollectionUtils.containsAny(productTypeUids, that.productTypeUids);
	}

	@Override
	public int hashCode() {
		return Objects.hash(optionKeyIdx);
	}
}
