/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static java.util.stream.Collectors.toSet;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.offer.OfferCategories;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductCategory;

/**
 * Represents a class which implements extracting category logic for {@link com.elasticpath.catalog.entity.offer.Offer}.
 */
public class OfferCategoryExtractor {

	private final Set<OfferCategories> offerCategories;

	/**
	 * Constructor.
	 *
	 * @param product      {@link Product}.
	 * @param categoryList list of categories.
	 */
	public OfferCategoryExtractor(final Product product, final List<Category> categoryList) {
		offerCategories = product.getCategories()
				.stream()
				.map(category -> createOfferCategory(product, category, categoryList))
				.collect(toSet());
	}

	private OfferCategories createOfferCategory(final Product product, final com.elasticpath.domain.catalog.Category category,
												final List<Category> categoryList) {
		final String categoryCode = category.getCode();
		final ProductCategory productCategory = product.getProductCategory(category);
		final boolean defaultCategory = productCategory.isDefaultCategory();
		final Category categoryProjection = findCategoryInListByCode(categoryList, categoryCode);
		final Integer featured = productCategory.getFeaturedProductOrder() == 0
				? null
				: productCategory.getFeaturedProductOrder();
		final AvailabilityRules availabilityRules = Optional.ofNullable(categoryProjection).map(Category::getAvailabilityRules).orElse(null);
		final List<String> categoryPath = Optional.ofNullable(categoryProjection).map(Category::getPath).orElse(Collections.emptyList());
		final ZonedDateTime enableDateTime = Optional.ofNullable(availabilityRules).map(AvailabilityRules::getEnableDateTime).orElse(null);
		final ZonedDateTime disableDateTime = Optional.ofNullable(availabilityRules).map(AvailabilityRules::getDisableDateTime).orElse(null);

		return new OfferCategories(categoryCode, categoryPath, enableDateTime, disableDateTime,
				defaultCategory, featured);
	}

	private Category findCategoryInListByCode(final List<Category> categoryList, final String code) {
		return categoryList.stream()
				.filter(projection -> projection.getIdentity().getCode().equals(code))
				.findFirst()
				.orElse(null);
	}

	/**
	 * @return set of OfferCategories.
	 */
	public Set<OfferCategories> getOfferCategories() {
		return offerCategories;
	}
}
