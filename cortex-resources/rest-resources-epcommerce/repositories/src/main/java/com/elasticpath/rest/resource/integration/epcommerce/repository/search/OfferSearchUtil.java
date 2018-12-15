/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.definition.searches.FacetIdIdentifierPart;
import com.elasticpath.rest.definition.searches.FacetIdentifier;
import com.elasticpath.rest.definition.searches.FacetsIdentifier;
import com.elasticpath.service.search.query.KeywordSearchCriteria;

/**
 * Utility class for offer search.
 */
public final class OfferSearchUtil {

	private OfferSearchUtil() {
		// this class should not be instantiated.
	}

	/**
	 * Create a search criteria for offer search.
	 * @param keyword the keyword used to search
	 * @param store store
	 * @param appliedFacets selected facets that will be filtered
	 * @param locale locale
	 * @param currency currency
	 * @param facetingEnabled facet enabled
	 * @return an offer search criteria
	 */
	public static KeywordSearchCriteria createSearchCriteria(final String keyword, final Store store, final Map<String, String> appliedFacets,
															 final Locale locale, final Currency currency, final boolean facetingEnabled) {
		KeywordSearchCriteria offerSearchCriteria = new KeywordSearchCriteria();
		offerSearchCriteria.setStoreCode(store.getCode());
		offerSearchCriteria.setCatalogCode(store.getCatalog().getCode());
		offerSearchCriteria.setKeyword(keyword);
		offerSearchCriteria.setCurrency(currency);
		offerSearchCriteria.setLocale(locale);
		offerSearchCriteria.setDisplayableOnly(true);
		offerSearchCriteria.setActiveOnly(true);
		offerSearchCriteria.setFacetingEnabled(facetingEnabled);
		offerSearchCriteria.setAppliedFacets(appliedFacets);
		offerSearchCriteria.setOfferSearch(true);
		return offerSearchCriteria;
	}

	/**
	 * Builds a facet identifier.
	 * @param facetsIdentifier facets identifier
	 * @param facetGuid facet guid
	 * @return facet identifier
	 */
	public static FacetIdentifier buildFacetIdentifier(final FacetsIdentifier facetsIdentifier, final String facetGuid) {
		return FacetIdentifier.builder()
				.withFacets(facetsIdentifier)
				.withFacetId(FacetIdIdentifierPart.of(facetGuid))
				.build();
	}
}
