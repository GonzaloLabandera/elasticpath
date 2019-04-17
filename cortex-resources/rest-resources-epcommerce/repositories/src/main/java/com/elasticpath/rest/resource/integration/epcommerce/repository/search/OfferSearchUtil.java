/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl.PRODUCT_GUID_KEY;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetIdIdentifierPart;
import com.elasticpath.rest.definition.offersearches.FacetIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetsIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.service.search.query.KeywordSearchCriteria;
import com.elasticpath.service.search.query.ProductSearchCriteria;

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
	 * Create a search criteria for offer search.
	 * @param appliedFacets selected facets that will be filtered
	 * @param locale locale
	 * @param currency currency
	 * @param facetingEnabled facet enabled
	 * @param category category
	 * @param storeCode store code
	 * @return an offer search criteria
	 */
	public static ProductSearchCriteria createNavigationSearchCriteria(final Map<String, String> appliedFacets, final Locale locale,
																	   final Currency currency, final boolean facetingEnabled,
																	   final Category category, final String storeCode) {
		ProductSearchCriteria criteria = new ProductSearchCriteria();
		criteria.setCurrency(currency);
		criteria.setLocale(locale);
		criteria.setDisplayableOnly(true);
		criteria.setActiveOnly(true);
		criteria.setFacetingEnabled(facetingEnabled);
		criteria.setAppliedFacets(appliedFacets);
		criteria.setFuzzySearchDisabled(true);
		criteria.setOnlyWithinDirectCategory(false);
		criteria.setOnlyInCategoryAndSubCategory(true);
		criteria.setDirectCategoryUid(category.getUidPk());
		criteria.setCatalogCode(category.getCatalog().getCode());
		criteria.setStoreCode(storeCode);
		return criteria;
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

	/**
	 * Builds offer identifier.
	 * @param product product
	 * @param scope scope
	 * @return offer identifier
	 */
	public static OfferIdentifier buildOfferIdentifier(final Product product, final IdentifierPart<String> scope) {
		return OfferIdentifier.builder()
				.withOfferId(CompositeIdentifier.of(ImmutableMap.of(PRODUCT_GUID_KEY, product.getGuid())))
				.withScope(scope)
				.build();
	}
}
