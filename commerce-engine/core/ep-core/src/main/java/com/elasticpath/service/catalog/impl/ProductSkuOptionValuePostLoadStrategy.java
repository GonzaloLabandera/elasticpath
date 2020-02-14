/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.catalog.impl;

import com.elasticpath.domain.skuconfiguration.LazyLoadableSkuOptionValue;
import com.elasticpath.persistence.openjpa.support.AbstractEagerFieldPostLoadStrategy;
import com.elasticpath.service.catalog.SkuOptionService;

/**
 * This strategy injects SKU option values into {@link com.elasticpath.domain.skuconfiguration.LazyLoadableSkuOptionValue} instance,
 * like {@link com.elasticpath.domain.skuconfiguration.impl.JpaAdaptorOfSkuOptionValueImpl}
 * from the cache rather than db.
 *
 * The main reason for doing this is to reduce the memory footprint when loading products, especially multi-sku ones, because one multi-sku product
 * may contain tens or even hundreds of SKUs. The main consumers of the SKU option values are {@link com.elasticpath.domain.catalog.ProductType} and
 * {@link com.elasticpath.domain.catalog.ProductSku} entities and given that OpenJPA must load the same (possibly huge) set of values for each
 * consumer (1 ProductType + N ProductSKUs) per product, the memory footprint can be significantly impaired, leading to OutOfMemory error.
 *
 * By caching SKU options and values at startup and reusing cached values via this and {@link ProductTypePostLoadStrategy} strategies, SKU options
 * and values are efficiently used, allowing large catalogs to fit into relatively small (e.g. standard 3 GB) heaps.
 */
public class ProductSkuOptionValuePostLoadStrategy extends AbstractEagerFieldPostLoadStrategy<LazyLoadableSkuOptionValue> {
	private static final String SKU_OPTION_VALUE_FIELD = "skuOptionValue";

	private SkuOptionService skuOptionService;

	@Override
	public boolean canProcess(final Object obj) {
		return LazyLoadableSkuOptionValue.class.isInstance(obj);
	}

	@Override
	public String getFieldName() {
		return SKU_OPTION_VALUE_FIELD;
	}

	@Override
	public Object fetchObjectToLoad(final LazyLoadableSkuOptionValue productSkuOptionValue) {
		return skuOptionService
			.findOptionValueByOptionKeyAndValueUid(productSkuOptionValue.getOptionKey(), productSkuOptionValue.getSkuOptionValueUidInternal());
	}

	public void setSkuOptionService(final SkuOptionService skuOptionService) {
		this.skuOptionService = skuOptionService;
	}
}
