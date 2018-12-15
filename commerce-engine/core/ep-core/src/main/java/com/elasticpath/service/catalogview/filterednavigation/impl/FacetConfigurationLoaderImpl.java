/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.catalogview.filterednavigation.impl;

import java.util.List;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.service.catalogview.filterednavigation.FilteredNavigationConfiguration;
import com.elasticpath.service.catalogview.filterednavigation.FilteredNavigationConfigurationLoader;
import com.elasticpath.service.catalogview.filterednavigation.facetconfigurationstrategies.FacetConfigurationStrategy;
import com.elasticpath.service.search.FacetService;
import com.elasticpath.settings.provider.SettingValueProvider;

/**
 * Loads facets for TFACET table.
 */
public class FacetConfigurationLoaderImpl implements FilteredNavigationConfigurationLoader {

	private BeanFactory beanFactory;
	private FacetService facetService;
	private List<FacetConfigurationStrategy> facetConfigurationStrategies;
	private SettingValueProvider<String> separatorInTokenProvider;

	@Override
	public FilteredNavigationConfiguration loadFilteredNavigationConfiguration(final String storeCode) {
		FilteredNavigationConfiguration config = beanFactory.getBean("filteredNavigationConfiguration");
		List<Facet> facets = getFacetService().findAllFacetableFacetsForStore(storeCode);
		facets.stream().filter(facet -> !facet.getDisplayNameMap().isEmpty())
				.forEach(facet -> getFacetConfigurationStrategyForFacet(facet).process(config, facet));
		return config;
	}

	private FacetConfigurationStrategy getFacetConfigurationStrategyForFacet(final Facet facet) {

		return getFacetConfigurationStrategies().stream()
				.filter(strategy -> strategy.shouldProcess(facet))
				.findFirst()
				.orElseThrow(() -> new EpSystemException("No strategy found to process facet"));
	}


	@Override
	public String getSeparatorInToken() {
		return getSeparatorInTokenProvider().get();
	}


	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public FacetService getFacetService() {
		return facetService;
	}

	public void setFacetService(final FacetService facetService) {
		this.facetService = facetService;
	}


	public List<FacetConfigurationStrategy> getFacetConfigurationStrategies() {
		return facetConfigurationStrategies;
	}

	public void setFacetConfigurationStrategies(final List<FacetConfigurationStrategy> facetConfigurationStrategies) {
		this.facetConfigurationStrategies = facetConfigurationStrategies;
	}

	protected SettingValueProvider<String> getSeparatorInTokenProvider() {
		return separatorInTokenProvider;
	}

	public void setSeparatorInTokenProvider(final SettingValueProvider<String> separatorInTokenProvider) {
		this.separatorInTokenProvider = separatorInTokenProvider;
	}

}