/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.catalogview.filterednavigation.facetconfigurationstrategies;

import com.elasticpath.domain.search.Facet;
import com.elasticpath.service.catalogview.filterednavigation.FilteredNavigationConfiguration;

/**
 * Strategies for configuring facets.
 */
public interface FacetConfigurationStrategy {

	/**
	 * Should the strategy process the facet.
	 * @param facet the facet.
	 * @return true if it should process, false otherwise.
	 */
	boolean shouldProcess(Facet facet);

	/**
	 * Process the facet.
	 * @param config the Filtered Navigation Config.
	 * @param facet the facet.
	 */
	void process(FilteredNavigationConfiguration config, Facet facet);
}
