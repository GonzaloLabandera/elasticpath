/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects.containers;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.selenium.domainobjects.SkuOption;

/**
 * Sku option container class.
 */
public class SkuOptionContainer {
	private final List<SkuOption> skuOptions = new ArrayList<>();

	public List<SkuOption> getSkuOptions() {
		return skuOptions;
	}

	public void addSkuOption(final SkuOption skuOption) {
		if (skuOption == null) {
			return;
		}
		skuOptions.add(skuOption);
	}

	public String getSkuOptionCodeByPartialCode(final String code) {
		return getSkuOptions()
				.stream()
				.filter(sku -> sku.getCode().startsWith(code))
				.map(SkuOption::getCode)
				.findFirst()
				.orElse(null);
	}

	public SkuOption getSkuOptionByPartialCode(final String code) {
		return getSkuOptions()
				.stream()
				.filter(sku -> sku.getCode().startsWith(code))
				.findFirst()
				.orElse(null);
	}
}
