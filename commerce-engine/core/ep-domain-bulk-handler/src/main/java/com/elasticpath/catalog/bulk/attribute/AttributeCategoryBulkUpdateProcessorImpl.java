/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.attribute;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.ATTRIBUTE_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryProperties;
import com.elasticpath.catalog.entity.translation.AttributeTranslation;
import com.elasticpath.catalog.entity.translation.CategoryTranslation;
import com.elasticpath.catalog.entity.translation.DetailsTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.entity.translation.TranslationUnit;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.service.misc.TimeService;

/**
 * An implementation of {@link AttributeCategoryBulkUpdateProcessor}.
 */
public class AttributeCategoryBulkUpdateProcessorImpl implements AttributeCategoryBulkUpdateProcessor {
	private final CatalogService catalogService;
	private final TimeService timeService;

	/**
	 * Constructor.
	 *
	 * @param catalogService catalogService.
	 * @param timeService    timeService.
	 */
	public AttributeCategoryBulkUpdateProcessorImpl(final CatalogService catalogService, final TimeService timeService) {
		this.catalogService = catalogService;
		this.timeService = timeService;
	}

	@Override
	public void updateCategoryAttributeDisplayNameInCategories(final List<String> categoryList, final String attribute) {
		final List<Attribute> attributes = catalogService.readAll(ATTRIBUTE_IDENTITY_TYPE, attribute);
		final List<Category> categories = catalogService.readAll(CATEGORY_IDENTITY_TYPE, categoryList);

		final List<Category> updatedCategories = categories.stream().filter(category -> !category.isDeleted())
				.map(category -> createCategoryWithUpdatedAttributeDisplayName(category, findAttributeForStore(attributes,
						category.getIdentity().getStore())))
				.collect(Collectors.toList());
		catalogService.saveOrUpdateAll(updatedCategories);
	}

	private Category createCategoryWithUpdatedAttributeDisplayName(final Category category, final Attribute updatedAttribute) {
		final List<CategoryTranslation> categoryTranslations = category.getTranslations().stream()
				.map(translation -> createCategoryTranslationWithUpdatedAttributeDisplayName(translation, updatedAttribute))
				.collect(Collectors.toList());

		return new Category(new CategoryProperties(
				new ProjectionProperties(category.getIdentity().getCode(),
						category.getIdentity().getStore(),
						ZonedDateTime.ofInstant(timeService.getCurrentTime().toInstant(), ZoneId.systemDefault()),
						false),
				category.getProperties()),
				category.getExtensions(),
				categoryTranslations,
				category.getChildren(),
				category.getAvailabilityRules(),
				category.getPath(),
				category.getParent());
	}

	private CategoryTranslation createCategoryTranslationWithUpdatedAttributeDisplayName(final CategoryTranslation categoryTranslation,
																						 final Attribute attribute) {
		final List<AttributeTranslation> attributeTranslations = attribute.getTranslations();
		final String language = categoryTranslation.getLanguage();
		final String updatedDisplayName = attributeTranslations.stream().filter(translation -> language.equals(translation.getLanguage()))
				.map(Translation::getDisplayName).findFirst().orElse(null);
		return new CategoryTranslation(
				categoryTranslation,
				buildDetails(categoryTranslation, attribute, updatedDisplayName));
	}

	private List<DetailsTranslation> buildDetails(final CategoryTranslation categoryTranslation, final Projection attribute,
												  final String updatedDisplayName) {
		final List<DetailsTranslation> detailsTranslations = new ArrayList<>();
		for (DetailsTranslation detailsTranslation : categoryTranslation.getDetails()) {
			if (attribute.getIdentity().getCode().equals(detailsTranslation.getName())) {
				detailsTranslations.add(new DetailsTranslation(new TranslationUnit(updatedDisplayName,
						detailsTranslation.getName()), detailsTranslation.getDisplayValues(), detailsTranslation.getValues()));
			} else {
				detailsTranslations.add(detailsTranslation);
			}
		}
		return detailsTranslations;
	}

	private Attribute findAttributeForStore(final List<Attribute> attributes, final String store) {
		return attributes.stream()
				.filter(attribute -> attribute.getIdentity().getStore().equals(store)).findAny()
				.orElseThrow(() -> new IllegalArgumentException("Attribute projection for " + store + " not found"));
	}
}
