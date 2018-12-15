/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.catalogview.filterednavigation.facetconfigurationstrategies.impl;

import java.util.HashMap;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalogview.SkuOptionValueFilter;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.FacetGroup;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.service.catalog.SkuOptionService;
import com.elasticpath.service.catalogview.filterednavigation.FilteredNavigationConfiguration;
import com.elasticpath.service.catalogview.filterednavigation.facetconfigurationstrategies.FacetConfigurationStrategy;


/**
 * Sku Option facet configuration strategy.
 */
public class SkuOptionFacetConfigurationStrategy implements FacetConfigurationStrategy {

	private SkuOptionService skuOptionService;
	private BeanFactory beanFactory;


	@Override
	public boolean shouldProcess(final Facet facet) {
		return facet.getFacetGroup() == FacetGroup.SKU_OPTION.getOrdinal();
	}

	@Override
	public void process(final FilteredNavigationConfiguration config, final Facet facet) {
		SkuOption skuOption = skuOptionService.findByKey(facet.getBusinessObjectId());
		for (SkuOptionValue skuOptionValue : skuOption.getOptionValues()) {
			SkuOptionValueFilter skuOptionValueFilter = beanFactory.getBean(ContextIdNames.SKU_OPTION_VALUE_FILTER);
			Map<String, Object> properties = new HashMap<>();
			properties.put(SkuOptionValueFilter.SKU_OPTION_VALUE_PROPERTY_KEY, skuOptionValue);
			skuOptionValueFilter.initialize(properties);
			config.getAllSkuOptionValueFilters().put(skuOptionValueFilter.getSeoId(), skuOptionValueFilter);
			config.getFacetMap().put(facet.getFacetGuid(), facet);
			config.getSkuOptionGuidMap().put(facet.getBusinessObjectId(), facet.getFacetGuid());
		}
	}

	public SkuOptionService getSkuOptionService() {
		return skuOptionService;
	}

	public void setSkuOptionService(final SkuOptionService skuOptionService) {
		this.skuOptionService = skuOptionService;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
