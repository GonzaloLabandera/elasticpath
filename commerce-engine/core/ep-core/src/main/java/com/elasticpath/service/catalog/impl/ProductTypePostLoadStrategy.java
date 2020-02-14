/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.catalog.impl;

import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.persistence.openjpa.support.AbstractEagerFieldPostLoadStrategy;
import com.elasticpath.service.catalog.SkuOptionService;

/**
 * This strategy injects SKU options into {@link ProductType} instance, from the cache rather than db.
 * @see {@link ProductSkuOptionValuePostLoadStrategy}
 */
public class ProductTypePostLoadStrategy extends AbstractEagerFieldPostLoadStrategy<ProductType> {
	private static final String SKU_OPTIONS_FIELD = "skuOptions";

	private SkuOptionService skuOptionService;

	@Override
	public boolean canProcess(final Object obj) {
		return ProductType.class.isInstance(obj);
	}

	@Override
	public String getFieldName() {
		return SKU_OPTIONS_FIELD;
	}

	@Override
	public Object fetchObjectToLoad(final ProductType productType) {
		return skuOptionService.findByProductTypeUid(productType.getUidPk());
	}

	public void setSkuOptionService(final SkuOptionService skuOptionService) {
		this.skuOptionService = skuOptionService;
	}
}
