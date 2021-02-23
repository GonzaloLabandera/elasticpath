/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.caching.core.rules;

import java.util.Objects;

/**
 * Selling context cache key.
 */
public class SellingContextCacheKey {

	private final Integer scenarioId;
	private final String storeCode;
	private final String catalogCode;

	/**
	 * Constructor.
	 *
	 * @param scenarioId  scenario
	 * @param storeCode   store code
	 * @param catalogCode catalog code
	 */
	protected SellingContextCacheKey(final Integer scenarioId, final String storeCode, final String catalogCode) {
		this.scenarioId = scenarioId;
		this.storeCode = storeCode;
		this.catalogCode = catalogCode;
	}

	/**
	 * Factory method to create code with both storeCode and catalogCode, used to init the cache.
	 *
	 * @param scenarioId  scenario
	 * @param storeCode   store code
	 * @param catalogCode catalog code
	 * @return new key
	 */
	public static SellingContextCacheKey createKey(final Integer scenarioId, final String storeCode, final String catalogCode) {
		return new SellingContextCacheKey(scenarioId, storeCode, catalogCode);
	}

	/**
	 * Factory method to create code for search by store code.
	 *
	 * @param scenarioId scenario
	 * @param storeCode  store code
	 * @return new key
	 */
	public static SellingContextCacheKey createStoreCodeKey(final Integer scenarioId, final String storeCode) {
		return new SellingContextCacheKey(scenarioId, storeCode, null);
	}

	/**
	 * Factory method to create code for search by store code.
	 *
	 * @param scenarioId  scenario
	 * @param catalogCode catalog code
	 * @return new key
	 */
	public static SellingContextCacheKey createCatalogCodeKey(final Integer scenarioId, final String catalogCode) {
		return new SellingContextCacheKey(scenarioId, null, catalogCode);
	}

	public String getStoreCode() {
		return storeCode;
	}

	public String getCatalogCode() {
		return catalogCode;
	}

	public Integer getScenarioId() {
		return scenarioId;
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}

		SellingContextCacheKey that = (SellingContextCacheKey) object;
		return Objects.equals(scenarioId, that.scenarioId)
				&& Objects.equals(storeCode, that.storeCode)
				&& Objects.equals(catalogCode, that.catalogCode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(scenarioId);
	}
}
