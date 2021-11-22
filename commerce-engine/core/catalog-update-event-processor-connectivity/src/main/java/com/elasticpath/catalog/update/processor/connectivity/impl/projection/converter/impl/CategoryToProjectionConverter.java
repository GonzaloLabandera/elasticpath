/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;
import static java.util.stream.Collectors.toList;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.catalog.CatalogReaderCapability;
import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.attribute.AttributeReaderCapability;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryProperties;
import com.elasticpath.catalog.entity.category.CategoryReaderCapability;
import com.elasticpath.catalog.entity.translation.CategoryTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.extractor.ProjectionLocaleAdapter;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.CatalogWriterCapability;
import com.elasticpath.catalog.spi.capabilities.CategoryWriterRepository;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.catalog.update.processor.connectivity.impl.exception.NoCapabilityMatchedException;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.Converter;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter.LocaleDependantFieldsAdapter;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.CategoryLookup;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.store.StoreService;

/**
 * A projection converter which converts Category {@link com.elasticpath.domain.catalog.Category} to Category {@link Category}.
 */
public class CategoryToProjectionConverter implements Converter<com.elasticpath.domain.catalog.Category, Category> {

	private static final Logger LOGGER = LogManager.getLogger(CategoryToProjectionConverter.class);

	private final CategoryService categoryService;
	private final CatalogProjectionPluginProvider provider;
	private final CatalogTranslationExtractor catalogTranslationExtractor;
	private final TimeService timeService;
	private final CategoryLookup categoryLookup;
	private final CatalogService catalogService;
	private final StoreService storeService;

	/**
	 * Constructor.
	 *
	 * @param categoryService             the category service {@link CategoryService}.
	 * @param timeService                 the time service {@link TimeService}.
	 * @param provider                    {@link CatalogProjectionPluginProvider}.
	 * @param catalogTranslationExtractor {@link CatalogTranslationExtractor}.
	 * @param categoryLookup              the category lookup.
	 * @param catalogService              the catalog service.
	 * @param storeService                the store service.
	 */
	public CategoryToProjectionConverter(final CategoryService categoryService,
										 final TimeService timeService,
										 final CatalogProjectionPluginProvider provider,
										 final CatalogTranslationExtractor catalogTranslationExtractor,
										 final CategoryLookup categoryLookup,
										 final CatalogService catalogService,
										 final StoreService storeService
	) {
		this.categoryService = categoryService;
		this.provider = provider;
		this.catalogTranslationExtractor = catalogTranslationExtractor;
		this.timeService = timeService;
		this.categoryLookup = categoryLookup;
		this.catalogService = catalogService;
		this.storeService = storeService;
	}

	/**
	 * Convert Category {@link com.elasticpath.domain.catalog.Category} to Category {@link Category}
	 * for particularly store {@link Store}.
	 *
	 * @param source  {@link com.elasticpath.domain.catalog.Category}.
	 * @param store   {@link Store}.
	 * @param catalog {@link Catalog}.
	 * @return projection {@link Category}.
	 */
	@Override
	public Category convert(final com.elasticpath.domain.catalog.Category source, final Store store, final Catalog catalog) {
		if (!categoryService.canSyndicate(source)) {
			LOGGER.debug("Creating DELETED projection record for category " + source.getCode()
					+ " because it does not meet syndication requirements.");
			return createTombstoneCategory(source, store, catalog);
		}

		return createCategory(source, store, catalog);
	}

	private Category createCategory(final com.elasticpath.domain.catalog.Category source, final Store store, final Catalog catalog) {
		final ZonedDateTime currentTime = convertToZonedDateTime(timeService.getCurrentTime());
		final CategoryTranslationExtractor categoryTranslationExtractor = getTranslationExtractor(catalog, store, source);
		final ProjectionProperties projectionProperties = new ProjectionProperties(source.getCode(), store.getCode(), currentTime, false);
		final List<Property> categoryProperties = new CategoryPropertyExtractor(source).getPropertyList();
		final com.elasticpath.domain.catalog.Category parent = extractParent(source);
		final String parentCode = extractParentCode(parent);
		setChildProjectionsToNotTombstone(source, store, catalog);
		refreshParentChildren(getParentProjection(store.getCode(), parentCode), parent);
		final Category category = new Category(new CategoryProperties(projectionProperties, categoryProperties), new Object(),
				categoryTranslationExtractor.getCategoryTranslations(), extractChildren(source), extractAvailabilityRules(source),
				extractPath(source), extractParentCode(parent));

		updateLinkedCategories(category, source.getUidPk(), catalog);

		return category;
	}

	private Category createTombstoneCategory(final com.elasticpath.domain.catalog.Category source, final Store store, final Catalog catalog) {
		final ZonedDateTime currentTime = convertToZonedDateTime(timeService.getCurrentTime());
		final ProjectionProperties projectionProperties = new ProjectionProperties(source.getCode(), store.getCode(), currentTime, true);
		final com.elasticpath.domain.catalog.Category parent = extractParent(source);
		final String parentCode = extractParentCode(parent);
		setChildProjectionsToTombstone(source, store, catalog);
		removeChildFromParentChildList(getParentProjection(store.getCode(), parentCode), source);

		final Category category = new Category(new CategoryProperties(projectionProperties, Collections.emptyList()));

		updateLinkedCategories(category, source.getUidPk(), catalog);

		return category;
	}

	private void setChildProjectionsToTombstone(final com.elasticpath.domain.catalog.Category source, final Store store, final Catalog catalog) {
		extractAllChildrenCodes(source).stream()
				.filter(StringUtils::isNoneEmpty)
				.forEach(children -> updateChildProjectionToTombstone(store, children, catalog));
	}

	private void setChildProjectionsToNotTombstone(final com.elasticpath.domain.catalog.Category source, final Store store, final Catalog catalog) {
		extractAllChildrenCodes(source).stream()
				.filter(StringUtils::isNoneEmpty)
				.forEach(children -> updateChildProjectionToNotTombstone(store, children, catalog));
	}

	private void updateChildProjectionToTombstone(final Store store, final String childrenCode,
												  final Catalog catalog) {
		final Optional<Category> childProjection = getChildProjection(store.getCode(), childrenCode);
		childProjection.ifPresent(category -> writeProjection(createTombstoneCategory(
				categoryLookup.findByCategoryCodeAndCatalog(childrenCode, catalog), store, catalog)));
	}

	private void updateLinkedCategories(final Category masterCategory, final Long uid, final Catalog catalog) {
		final List<Category> categoryList = catalogService.readAll(CATEGORY_IDENTITY_TYPE, masterCategory.getIdentity().getCode());
		final List<com.elasticpath.domain.catalog.Category> linkedCategories = categoryService.findLinkedCategories(uid).stream()
				.filter(com.elasticpath.domain.catalog.Category::isIncluded)
				.collect(toList());
		final List<String> storeCodesInCatalog = storeService.findStoresWithCatalogCode(catalog.getCode()).stream()
				.map(Store::getCode).collect(toList());

		if (!linkedCategories.isEmpty()) {
			categoryList.stream()
					.filter(category -> !storeCodesInCatalog.contains(category.getIdentity().getStore()))
					.map(category -> createUpdatedLinkedCategory(masterCategory, category, linkedCategories))
					.forEach(this::writeProjection);
		}
	}

	private Category createUpdatedLinkedCategory(final Category masterProjection, final Category linkedProjection,
												 final List<com.elasticpath.domain.catalog.Category> linkedCategories) {
		final String catalogCode = storeService.getCatalogCodeForStore(linkedProjection.getIdentity().getStore());
		final Optional<com.elasticpath.domain.catalog.Category> linkedCategory = linkedCategories.stream()
				.filter(category -> category.getCatalog().getCode().equals(catalogCode))
				.findFirst();

		final ZonedDateTime currentTime = convertToZonedDateTime(timeService.getCurrentTime());
		final ProjectionProperties projectionProperties = new ProjectionProperties(linkedProjection.getIdentity().getCode(),
				linkedProjection.getIdentity().getStore(), currentTime, masterProjection.isDeleted());
		final Optional<CategoryTranslationExtractor> categoryTranslationExtractor =
				linkedCategory.map(category -> getTranslationExtractor(category.getCatalog(),
						storeService.findStoreWithCode(linkedProjection.getIdentity().getStore()), category));
		final List<CategoryTranslation> translations = categoryTranslationExtractor
				.filter(extractor -> !masterProjection.isDeleted())
				.map(CategoryTranslationExtractor::getCategoryTranslations)
				.orElse(null);

		return new Category(new CategoryProperties(projectionProperties, masterProjection.getProperties()),
				new Object(), translations, masterProjection.getChildren(),
				masterProjection.getAvailabilityRules(), masterProjection.getPath(), masterProjection.getParent());
	}

	private void updateChildProjectionToNotTombstone(final Store store, final String childrenCode,
													 final Catalog catalog) {
		final Optional<Category> childProjection = getChildProjection(store.getCode(), childrenCode);
		childProjection.ifPresent(category -> writeProjection(createCategory(categoryLookup.findByCategoryCodeAndCatalog(childrenCode, catalog),
				store,
				catalog)));
	}

	private Optional<Category> getChildProjection(final String storeCode, final String childrenCode) {
		return getReader(CategoryReaderCapability.class)
				.get(storeCode, childrenCode);
	}

	private Category getParentProjection(final String storeCode, final String parentCode) {
		return getReader(CategoryReaderCapability.class)
				.get(storeCode, parentCode).orElse(null);
	}

	private void writeProjection(final Category projection) {
		final CategoryWriterRepository repository = getWriter(CategoryWriterRepository.class);
		repository.write(projection);
	}

	private void removeChildFromParentChildList(final Category parentProjection, final com.elasticpath.domain.catalog.Category source) {
		if (Objects.nonNull(parentProjection) && parentProjection.getChildren() != null
				&& parentProjection.getChildren().contains(source.getCode())) {
			parentProjection.getChildren().remove(source.getCode());
			writeProjection(parentProjection);
		}
	}

	private void refreshParentChildren(final Category parentProjection, final com.elasticpath.domain.catalog.Category parent) {
		final List<String> children = Optional.ofNullable(parent).map(this::extractChildren).orElse(Collections.emptyList());

		if (Objects.nonNull(parentProjection) && !parentProjection.isDeleted() && !parentProjection.getChildren().equals(children)) {
			parentProjection.getChildren().clear();
			parentProjection.getChildren().addAll(children);

			writeProjection(parentProjection);
		}
	}

	private CategoryTranslationExtractor getTranslationExtractor(final Catalog catalog,
																 final Store store,
																 final com.elasticpath.domain.catalog.Category category) {
		final ProjectionLocaleAdapter adapter = new LocaleDependantFieldsAdapter(catalog.getDefaultLocale(),
				category,
				store.getSupportedLocales());
		final List<Translation> translations = catalogTranslationExtractor.getProjectionTranslations(store.getDefaultLocale(),
				store.getSupportedLocales(),
				adapter);
		return new CategoryTranslationExtractor(translations, extractDetails(category, store), category, store, catalog);
	}

	private List<Attribute> extractDetails(final com.elasticpath.domain.catalog.Category category, final Store store) {
		final List<String> categoryAttributeKeys = category.getAttributeValueMap().values().stream()
				.map(AttributeValue::getAttribute)
				.map(com.elasticpath.domain.attribute.Attribute::getKey)
				.collect(toList());
		return getReader(AttributeReaderCapability.class).findAllWithCodes(store.getCode(), categoryAttributeKeys);
	}

	private List<String> extractChildren(final com.elasticpath.domain.catalog.Category category) {
		final List<com.elasticpath.domain.catalog.Category> childCategoryList =
				categoryService.findDirectDescendantCategories(category.getGuid());
		return childCategoryList.stream()
				.filter(categoryService::canSyndicate)
				.map(com.elasticpath.domain.catalog.Category::getCode)
				.collect(toList());
	}

	private List<String> extractAllChildrenCodes(final com.elasticpath.domain.catalog.Category category) {
		final List<com.elasticpath.domain.catalog.Category> childCategoryList =
				categoryService.findDirectDescendantCategories(category.getGuid());
		return childCategoryList.stream()
				.map(com.elasticpath.domain.catalog.Category::getCode)
				.collect(toList());
	}

	private ZonedDateTime convertToZonedDateTime(final Date date) {
		return Optional.ofNullable(date)
				.map(dateValue -> ZonedDateTime.ofInstant(dateValue.toInstant(), ZoneId.of("GMT")))
				.orElse(null);
	}

	private AvailabilityRules extractAvailabilityRules(final com.elasticpath.domain.catalog.Category source) {
		final ZonedDateTime enableDateTime = extractCategoriesData(source, Collections.emptyList(),
				com.elasticpath.domain.catalog.Category::getStartDate)
				.stream()
				.max(Date::compareTo)
				.map(date -> ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("GMT"))).orElse(null);
		final ZonedDateTime disableDateTime = extractCategoriesData(source, Collections.emptyList(),
				com.elasticpath.domain.catalog.Category::getEndDate).stream()
				.min(Date::compareTo)
				.map(date -> ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("GMT"))).orElse(null);

		return new AvailabilityRules(enableDateTime, disableDateTime);
	}

	private com.elasticpath.domain.catalog.Category extractParent(final com.elasticpath.domain.catalog.Category source) {
		return categoryLookup.findParent(source);
	}

	private String extractParentCode(final com.elasticpath.domain.catalog.Category source) {
		return Optional.ofNullable(source).map(com.elasticpath.domain.catalog.Category::getCode).orElse(null);
	}

	private List<String> extractPath(final com.elasticpath.domain.catalog.Category source) {
		final com.elasticpath.domain.catalog.Category parent = categoryLookup.findParent(source);

		return Optional.ofNullable(parent)
				.map(category -> extractCategoriesData(parent, Collections.emptyList(), com.elasticpath.domain.catalog.Category::getCode))
				.map(Lists::reverse)
				.orElseGet(Collections::emptyList);
	}

	/**
	 * Get the same data from given category and its relatives. For example list of start date for given Category and all start Date of its
	 * relatives.
	 *
	 * @param source   is given Category.
	 * @param data     is list for extracting the data.
	 * @param function is function for extracting data from Category.
	 * @param <T>      id type of data for extracting.
	 * @return list of data without null value.
	 */
	private <T> List<T> extractCategoriesData(final com.elasticpath.domain.catalog.Category source, final List<T> data,
											  final Function<com.elasticpath.domain.catalog.Category, T> function) {
		final com.elasticpath.domain.catalog.Category parent = categoryLookup.findParent(source);
		final List<T> sourceFieldAsList = Optional.ofNullable(function.apply(source))
				.map(Collections::singletonList)
				.orElseGet(Collections::emptyList);

		final List<T> mergedData = Stream.of(data, sourceFieldAsList)
				.flatMap(List::stream)
				.collect(toList());

		return Optional.ofNullable(parent)
				.map(category -> extractCategoriesData(category, mergedData, function))
				.orElse(mergedData);
	}

	private <T extends CatalogReaderCapability> T getReader(final Class<T> capabilityClass) {
		return provider.getCatalogProjectionPlugin()
				.getReaderCapability(capabilityClass)
				.orElseThrow(NoCapabilityMatchedException::new);
	}

	private <T extends CatalogWriterCapability<?>> T getWriter(final Class<T> capabilityClass) {
		return provider.getCatalogProjectionPlugin()
				.getWriterCapability(capabilityClass)
				.orElseThrow(NoCapabilityMatchedException::new);
	}
}
