/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.catalogview.filterednavigation.facetconfigurationstrategies.impl;

import static com.elasticpath.domain.catalogview.BrandFilter.BRAND_PROPERTY_KEY;
import static com.elasticpath.domain.catalogview.impl.CategoryFilterImpl.CATEGORY_KEY;
import static com.elasticpath.service.search.solr.FacetConstants.BRAND;
import static com.elasticpath.service.search.solr.FacetConstants.CATEGORY;
import static com.elasticpath.service.search.solr.FacetConstants.PRICE;
import static com.elasticpath.service.search.solr.FacetConstants.SIZE_ATTRIBUTES;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.common.dto.search.RangeFacet;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalogview.BrandFilter;
import com.elasticpath.domain.catalogview.CategoryFilter;
import com.elasticpath.domain.catalogview.FilterDisplayInfo;
import com.elasticpath.domain.catalogview.PriceFilter;
import com.elasticpath.domain.catalogview.RangeFilter;
import com.elasticpath.domain.catalogview.SizeRangeFilter;
import com.elasticpath.domain.catalogview.SizeRangeFilterConstants;
import com.elasticpath.domain.catalogview.SizeType;
import com.elasticpath.domain.catalogview.impl.FilterDisplayInfoImpl;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.FacetGroup;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.BrandService;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.catalogview.filterednavigation.FilteredNavigationConfiguration;
import com.elasticpath.service.catalogview.filterednavigation.facetconfigurationstrategies.FacetConfigurationStrategy;
import com.elasticpath.service.store.StoreService;

/**
 * Product facet configuration strategy.
 */
public class ProductFacetConfigurationStrategy implements FacetConfigurationStrategy {
	private static final Logger LOG = LogManager.getLogger(ProductFacetConfigurationStrategy.class);
	private static final String LOWER_BOUND_GREATER_THAN_UPPER_BOUND_ERROR =
			"Range facet lower bound %f is higher than upper bound %f for Facet with uidpk %d%n";

	private static final String DASH = "-";
	private static final String UNDERSCORE = "_";

	private BeanFactory beanFactory;


	@Override
	public boolean shouldProcess(final Facet facet) {
		return facet.getFacetGroup() == FacetGroup.FIELD.getOrdinal();
	}

	@Override
	public void process(final FilteredNavigationConfiguration config, final Facet facet) {

		StoreService storeService = beanFactory.getSingletonBean(ContextIdNames.STORE_SERVICE, StoreService.class);

		Store store = storeService.findStoreWithCode(facet.getStoreCode());
		Collection<Currency> currencies = store.getSupportedCurrencies();

		Catalog catalog = store.getCatalog();

		final String facetName = facet.getFacetName();
		if (BRAND.equals(facetName)) {
			addBrandsToConfig(config, catalog);
		} else if (CATEGORY.equals(facetName)) {
			addCategoryToConfig(config, catalog);
		} else if (PRICE.equals(facetName)) {
			addPriceToConfig(config, currencies, facet);
		} else if (SIZE_ATTRIBUTES.contains(facetName)) {
			addSizesToConfig(config, facet, facetName);
		}
		config.getFacetMap().put(facet.getFacetGuid(), facet);
		config.getOthersGuidMap().put(facetName, facet.getFacetGuid());
	}

	private void addBrandsToConfig(final FilteredNavigationConfiguration config, final Catalog catalog) {
		List<BrandFilter> brandFilters = config.getBrandFilters();
		BrandService brandService = beanFactory.getSingletonBean(ContextIdNames.BRAND_SERVICE, BrandService.class);
		brandService.findAllBrandsFromCatalogList(ImmutableList.of(catalog)).forEach(brand -> {
			BrandFilter brandFilter = beanFactory.getPrototypeBean(ContextIdNames.BRAND_FILTER, BrandFilter.class);
			brandFilter.initialize(ImmutableMap.of(BRAND_PROPERTY_KEY, ImmutableSet.of(brand)));
			brandFilters.add(brandFilter);
		});
	}

	private void addCategoryToConfig(final FilteredNavigationConfiguration config, final Catalog catalog) {
		List<CategoryFilter> categoryFilters = config.getCategoryFilters();
		CategoryService categoryService = beanFactory.getSingletonBean(ContextIdNames.CACHING_CATEGORY_SERVICE, CategoryService.class);
		categoryService.findCategoriesByCatalogUid(catalog.getUidPk()).forEach(category -> {
			CategoryFilter categoryFilter = beanFactory.getPrototypeBean(ContextIdNames.CATEGORY_FILTER, CategoryFilter.class);
			categoryFilter.initialize(ImmutableMap.of(CATEGORY_KEY, category));
			categoryFilter.setCatalog(catalog);
			categoryFilters.add(categoryFilter);
		});
	}


	private void addSizesToConfig(final FilteredNavigationConfiguration config, final Facet facet, final String fieldKey) {
		SortedSet<RangeFacet> rangeFacets = facet.getSortedRangeFacet();
		for (RangeFacet rangeFacet : rangeFacets) {
			final BigDecimal lowerBound = rangeFacet.getStart();
			boolean noLowerBound = lowerBound == null;
			final BigDecimal upperBound = rangeFacet.getEnd();
			boolean noUpperBound = upperBound == null;
			if (lowerBoundGreaterThanUpperBound(lowerBound, noLowerBound, upperBound, noUpperBound)) {
				LOG.warn(String.format(LOWER_BOUND_GREATER_THAN_UPPER_BOUND_ERROR, lowerBound, upperBound, facet.getUidPk()));
				continue;
			}
			SizeRangeFilter sizeRangeFilter = getBeanFactory().getPrototypeBean(ContextIdNames.SIZE_FILTER, SizeRangeFilter.class);
			Map<String, Object> properties = new HashMap<>();
			properties.put(SizeRangeFilterConstants.TYPE, SizeType.valueOfLabel(fieldKey));
			properties.put(RangeFilter.LOWER_VALUE_PROPERTY, lowerBound);
			properties.put(RangeFilter.UPPER_VALUE_PROPERTY, upperBound);
			properties.put(SizeRangeFilterConstants.ALIAS, lowerBound + UNDERSCORE + upperBound);
			sizeRangeFilter.initialize(properties);

			Map<Locale, FilterDisplayInfo> localizedDisplayMap = new HashMap<>();
			populateDisplayNameWithRangeFacet(rangeFacet, lowerBound, upperBound, localizedDisplayMap);

			sizeRangeFilter.setLocalizedDisplayMap(localizedDisplayMap);

			config.getAllSizeRangeFilters().put(sizeRangeFilter.getId(), sizeRangeFilter);
		}
	}

	private void populateDisplayNameWithRangeFacet(final RangeFacet rangeFacet, final BigDecimal start, final BigDecimal end,
												   final Map<Locale, FilterDisplayInfo> localeFilterDisplayMap) {
		rangeFacet.getDisplayNameMap().keySet().forEach(localeString -> {
			FilterDisplayInfo filterDisplayInfo = new FilterDisplayInfoImpl();
			filterDisplayInfo.setDisplayName(rangeFacet.getDisplayNameMap().getOrDefault(localeString, start + DASH + end));
			localeFilterDisplayMap.put(LocaleUtils.toLocale(localeString), filterDisplayInfo);
		});
	}


	private boolean lowerBoundGreaterThanUpperBound(final BigDecimal lowerBound, final boolean noLowerBound, final BigDecimal upperBound,
													final boolean noUpperBound) {
		return !noLowerBound && !noUpperBound && lowerBound.compareTo(upperBound) > 0;
	}

	private void addPriceToConfig(final FilteredNavigationConfiguration config,
								  final Collection<Currency> currencies, final Facet facet) {
		for (Currency currency : currencies) {
			SortedSet<RangeFacet> rangeFacets = facet.getSortedRangeFacet();
			PriceFilter rootFilter = getBeanFactory().getPrototypeBean(ContextIdNames.PRICE_FILTER, PriceFilter.class);
			rootFilter.setId(currency.getCurrencyCode());
			rootFilter.setCurrency(currency);
			rootFilter.setLocalized(false);
			config.getAllPriceRanges().put(rootFilter.getId(), rootFilter);
			for (RangeFacet rangeFacet : rangeFacets) {
				final BigDecimal lowerBound = rangeFacet.getStart();
				boolean noLowerBound = lowerBound == null;
				final BigDecimal upperBound = rangeFacet.getEnd();
				boolean noUpperBound = upperBound == null;
				if (lowerBoundGreaterThanUpperBound(lowerBound, noLowerBound, upperBound, noUpperBound)) {
					LOG.warn(String.format(LOWER_BOUND_GREATER_THAN_UPPER_BOUND_ERROR, lowerBound, upperBound, facet.getUidPk()));
					continue;
				}
				PriceFilter priceFilter = getBeanFactory().getPrototypeBean(ContextIdNames.PRICE_FILTER, PriceFilter.class);
				Map<String, Object> properties = new HashMap<>();
				priceFilter.setLocalized(false);
				properties.put(PriceFilter.CURRENCY_PROPERTY, rootFilter.getCurrency());
				properties.put(RangeFilter.LOWER_VALUE_PROPERTY, lowerBound);
				properties.put(RangeFilter.UPPER_VALUE_PROPERTY, upperBound);
				properties.put(PriceFilter.ALIAS_PROPERTY, currency.getCurrencyCode() + lowerBound + DASH + upperBound);
				priceFilter.initialize(properties);
				Map<Locale, FilterDisplayInfo> localeFilterDisplayMap = new HashMap<>();
				populateDisplayNameWithRangeFacet(rangeFacet, lowerBound, upperBound, localeFilterDisplayMap);
				priceFilter.setLocalizedDisplayMap(localeFilterDisplayMap);
				rootFilter.addChild(priceFilter);

				config.getBottomLevelPriceRanges(currency).put(priceFilter, priceFilter);
				config.getAllPriceRanges().put(priceFilter.getId(), priceFilter);
			}
		}
	}


	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
