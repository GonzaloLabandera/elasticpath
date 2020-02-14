/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.domain.catalogview.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.constants.SeoConstants;
import com.elasticpath.domain.catalogview.SkuOptionValueFilter;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.service.catalog.SkuOptionService;

/**
 * The implementation of AttributeFilter.
 */
public class SkuOptionValueFilterImpl extends AbstractFilterImpl<SkuOptionValueFilter> implements SkuOptionValueFilter {

	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;

	private SkuOptionValue skuOptionValue;

	@Override
	public String getDisplayName(final Locale locale) {
		return skuOptionValue.getDisplayName(locale, true);
	}

	@Override
	public String getSeoName(final Locale locale) {
		return getDisplayName(locale);
	}

	/**
	 * Returns the SEO identifier of the filter.
	 *
	 * @return the SEO identifier of the filter.
	 */
	@Override
	public String getSeoId() {
		return SeoConstants.SKU_OPTION_VALUE_PREFIX + skuOptionValue.getSkuOption().getOptionKey() + skuOptionValue.getOptionValueKey();
	}

	@Override
	public Map<String, Object> parseFilterString(final String filterIdStr) {
		Map<String, Object> tokenMap = new HashMap<>();
		tokenMap.put(SKU_OPTION_VALUE_PROPERTY_KEY, skuOptionValue);
		return tokenMap;
	}

	@Override
	public void initialize(final Map<String, Object> properties) {
		skuOptionValue = (SkuOptionValue) properties.get(SKU_OPTION_VALUE_PROPERTY_KEY);
		setId(getSeoId());
	}

	/**
	 * Get sku option service.
	 *
	 * @return the sku option service
	 */
	protected SkuOptionService getSkuOptionService() {
		return getSingletonBean(ContextIdNames.SKU_OPTION_SERVICE, SkuOptionService.class);
	}

	@Override
	public int compareTo(final SkuOptionValueFilter other) {
		return 0;
	}

	@Override
	public SkuOptionValue getSkuOptionValue() {
		return this.skuOptionValue;
	}

}