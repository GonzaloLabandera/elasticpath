package com.elasticpath.service.catalogview.filterednavigation.facetconfigurationstrategies.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.FacetGroup;

/**
 * Tests for attribute facet configuration strategy.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributeFacetConfigurationStrategyTest {

	private static final String DESCRIPTION = "Strategy should process attribute facets";
	private final AttributeFacetConfigurationStrategy strategy = new AttributeFacetConfigurationStrategy();

	@Mock
	private Facet facet;


	@Test
	public void shouldProcessReturnsTrueForProductAttributeTypes() {
		when(facet.getFacetGroup()).thenReturn(FacetGroup.PRODUCT_ATTRIBUTE.getOrdinal());
		assertThat(strategy.shouldProcess(facet)).as(DESCRIPTION).isTrue();
	}
	@Test
	public void shouldProcessReturnsFalseForSKUAttributeTypes() {
		when(facet.getFacetGroup()).thenReturn(FacetGroup.SKU_OPTION.getOrdinal());
		assertThat(strategy.shouldProcess(facet)).as(DESCRIPTION).isTrue();
	}@Test
	public void shouldProcessReturnsFalseForOtherAttributeTypes() {
		when(facet.getFacetGroup()).thenReturn(FacetGroup.OTHERS.getOrdinal());
		assertThat(strategy.shouldProcess(facet)).as(DESCRIPTION).isTrue();
	}

}