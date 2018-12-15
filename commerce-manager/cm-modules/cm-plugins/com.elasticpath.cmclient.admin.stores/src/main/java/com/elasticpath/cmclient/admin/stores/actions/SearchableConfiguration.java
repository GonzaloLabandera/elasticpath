package com.elasticpath.cmclient.admin.stores.actions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;

import com.elasticpath.cmclient.admin.stores.editors.facets.FacetModel;
import com.elasticpath.domain.search.FacetGroup;
import com.elasticpath.domain.search.FacetType;
import com.elasticpath.domain.search.FieldKeyType;

/**
 * Gets the searchable configuration.
 */
public class SearchableConfiguration {

	private static final String PRICE = "Price";

	private final Collection<Locale> supportedLocales;
	private final String defaultLocaleString;

	private final Set<String> defaultStringProductAttributes = ImmutableSet.of("Product Name", "Product Sku Code",  "Brand", "Category");
	private final Set<String> defaultSkuAttributes = ImmutableSet.of("Height", "Width", "Length", "Weight");

	/**
	 * Constructor.
	 *
	 * @param defaultLocale the default locale.
	 * @param supportedLocales the supported locales.
	 */
	public SearchableConfiguration(final Locale defaultLocale, final Collection<Locale> supportedLocales) {
		this.supportedLocales = supportedLocales;
		this.defaultLocaleString = defaultLocale.toString();
	}

	/**
	 * Get search configuration.
	 *
	 * @return a list of search configuration.
	 */
	public Map<String, FacetModel> getSearchableConfiguration() {
		Map<String, FacetModel> storeFacetsMap = new HashMap<>();

		for (String attribute : defaultStringProductAttributes) {
			FacetModel facetModel = new FacetModel(attribute, FieldKeyType.STRING, true, FacetType.NO_FACET,
					supportedLocales, defaultLocaleString, "");
			facetModel.setFacetGroup(FacetGroup.OTHERS);
			facetModel.setAttributeKey(attribute);
			facetModel.setGuid(UUID.randomUUID().toString());
			storeFacetsMap.put(attribute, facetModel);
		}

		FacetModel price = new FacetModel(PRICE, FieldKeyType.DECIMAL, true, FacetType.NO_FACET,
				supportedLocales, defaultLocaleString, "");
		price.setFacetGroup(FacetGroup.OTHERS);
		price.setAttributeKey(PRICE);
		price.setGuid(UUID.randomUUID().toString());
		storeFacetsMap.put(PRICE, price);

		for (String attribute : defaultSkuAttributes) {
			FacetModel facetModel = new FacetModel(attribute, FieldKeyType.DECIMAL, true, FacetType.NO_FACET,
					supportedLocales, defaultLocaleString, "");
			facetModel.setFacetGroup(FacetGroup.OTHERS);
			facetModel.setAttributeKey(attribute);
			facetModel.setGuid(UUID.randomUUID().toString());
			storeFacetsMap.put(attribute, facetModel);
		}

		return storeFacetsMap;
	}
}