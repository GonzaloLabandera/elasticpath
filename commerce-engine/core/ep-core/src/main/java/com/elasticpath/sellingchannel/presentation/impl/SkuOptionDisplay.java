/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.sellingchannel.presentation.impl;

import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.service.pricing.impl.PaymentScheduleHelperImpl;

/**
 * Class to generate a String of sku option values.
 */

public final class SkuOptionDisplay {
	private static final String SKU_OPTION_VALUE_DISPLAY_NAME = "skuOptionValueDisplayName";

	/**
	 * Constructor.
	 */
	private SkuOptionDisplay() {
		throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * Gets a String representation of the sku option values, excluding frequency.
	 *
	 * @param productSku the sku to use
	 * @param locale     the locale to use
	 * @return a String.
	 */
	public static String getFilteredSkuDisplay(final ProductSku productSku, final Locale locale) {
		if (productSku == null) {
			return "";
		}
		return productSku.getOptionValues()
				.stream()
				.filter(skuOptionValue -> !PaymentScheduleHelperImpl.FREQUENCY_OPTION_KEY.equals(skuOptionValue.getSkuOption().getOptionKey()))
				.map(skuOptionValue -> skuOptionValue.getLocalizedProperties().getValue(SKU_OPTION_VALUE_DISPLAY_NAME, locale))
				.collect(Collectors.joining(","));
	}

	/**
	 * Gets a String representation of the named sku option.
	 *
	 * @param productSku the sku to use
	 * @param locale     the locale to use
	 * @param skuOptionName the name of the sku option to get
	 * @return a String.
	 */
	public static String getSkuOptionDisplayName(final ProductSku productSku, final Locale locale, final String skuOptionName) {
		if (productSku == null || StringUtils.isEmpty(skuOptionName)) {
			return "";
		}
		return productSku.getOptionValues().stream()
				.filter(skuOptionValue -> skuOptionValue.getSkuOption().getOptionKey().equalsIgnoreCase(skuOptionName))
				.map(skuOptionValue -> skuOptionValue.getLocalizedProperties().getValue(SKU_OPTION_VALUE_DISPLAY_NAME, locale))
				.findFirst()
				.orElse("");
	}
}
