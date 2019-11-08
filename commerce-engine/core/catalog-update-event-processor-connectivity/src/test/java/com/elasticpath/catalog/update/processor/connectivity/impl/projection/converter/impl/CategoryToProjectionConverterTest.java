/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static com.elasticpath.catalog.entity.constants.CategoryPropertiesNames.PROPERTY_CATEGORY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.ATTRIBUTE_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.attribute.AttributeReaderCapability;
import com.elasticpath.catalog.entity.category.CategoryProperties;
import com.elasticpath.catalog.entity.category.CategoryReaderCapability;
import com.elasticpath.catalog.entity.translation.AttributeTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.spi.CatalogProjectionPlugin;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.CategoryWriterRepository;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeGroup;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.attribute.AttributeValueGroup;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.CategoryType;
import com.elasticpath.domain.catalog.LocaleDependantFields;
import com.elasticpath.domain.catalog.impl.CategoryImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.CategoryLookup;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.store.StoreService;

/**
 * Tests {@link CategoryToProjectionConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"PMD.TooManyMethods"})
public class CategoryToProjectionConverterTest {
	private static final String TEST_VALUE = "testValue";
	private static final String STORE_CODE = "storeCode";
	private static final String CATEGORY_TYPE = "CategoryType";
	private static final String CATEGORY_CODE = "categoryCode";
	private static final String CATEGORY_GUID = "categoryGuid";
	private static final String PARENT_CATEGORY_CODE = "ParentCategoryCode";
	private static final ZonedDateTime DATE_TIME = ZonedDateTime.now();
	private static final ZonedDateTime PARENT_DATE_TIME = ZonedDateTime.now();
	private static final ZonedDateTime GREATEST_DATE_TIME = ZonedDateTime.now().plusDays(1);
	private static final String KEY_ATTRIBUTE = "keyAttribute";
	private static final Optional<AttributeReaderCapability> ATTRIBUTE_READER_CAPABILITY =
			Optional.ofNullable(mock(AttributeReaderCapability.class));
	private static final Optional<CategoryReaderCapability> CATEGORY_READER_CAPABILITY =
			Optional.ofNullable(mock(CategoryReaderCapability.class));
	private static final Optional<CategoryWriterRepository> CATEGORY_WRITER_CAPABILITY =
			Optional.ofNullable(mock(CategoryWriterRepository.class));
	@Mock
	private CategoryService categoryService;

	@Mock
	private CatalogService catalogService;

	@Mock
	private CatalogProjectionPluginProvider provider;

	@Mock
	private CatalogTranslationExtractor translationExtractor;

	@Mock
	private TimeService timeService;
	@Mock
	private CategoryLookup categoryLookup;

	@Mock
	private CatalogProjectionPlugin plugin;

	@Mock
	private StoreService storeService;

	@Before
	public void setup() {
		when(timeService.getCurrentTime()).thenReturn(new Date());
		when(provider.getCatalogProjectionPlugin()).thenReturn(plugin);
		when(provider.getCatalogProjectionPlugin()).thenReturn(plugin);
		when(plugin.getReaderCapability((AttributeReaderCapability.class))).thenReturn(ATTRIBUTE_READER_CAPABILITY);
		when(plugin.getReaderCapability((CategoryReaderCapability.class))).thenReturn(CATEGORY_READER_CAPABILITY);
		when(plugin.getWriterCapability((CategoryWriterRepository.class))).thenReturn(CATEGORY_WRITER_CAPABILITY);
	}

	/**
	 * Test, that converter get Category with store and catalog and convert it to Category projection.
	 */
	@Test
	public void testThatConverterGetCategoryWithFilledFieldsAndConvertItToProjection() {
		final Category category = mockCategory(CATEGORY_CODE);
		final Store store = mockStore();
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryResult = converter.convert(category, store, mockCatalog());
		final Property property = categoryResult.getProperties().get(0);
		assertThat(categoryResult.getIdentity().getType()).isEqualTo(CATEGORY_IDENTITY_TYPE);
		assertThat(categoryResult.getIdentity().getType()).isEqualTo(CATEGORY_IDENTITY_TYPE);
		assertThat(categoryResult.getIdentity().getCode()).isEqualTo(CATEGORY_CODE);
		assertThat(categoryResult.getIdentity().getStore()).isEqualTo(STORE_CODE);
		assertThat(categoryResult.getModifiedDateTime()).isNotNull();
		assertThat(property).isNotNull();
		assertThat(property.getValue()).isEqualTo(CATEGORY_TYPE);
		assertThat(property.getName()).isEqualTo(PROPERTY_CATEGORY_TYPE);
	}

	@Test
	public void extractPropertyCategoryTypeTest() {
		final Category category = mockCategory(CATEGORY_CODE);
		final Store store = mockStore();
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, store, mockCatalog());
		final Property property = categoryProjection.getProperties().get(0);
		assertThat(property).isNotNull();
		assertThat(property.getValue()).isEqualTo(CATEGORY_TYPE);
		assertThat(property.getName()).isEqualTo(PROPERTY_CATEGORY_TYPE);
	}

	@Test
	public void extractCategoryChildListTest() {
		final Category category = mockCategory(CATEGORY_CODE);
		final Category categoryChild = mockCategory(CATEGORY_CODE + "Child");
		final Store store = mockStore();
		when(categoryService.findDirectDescendantCategories(category.getGuid())).thenReturn(Collections.singletonList(categoryChild));
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, store, mockCatalog());
		final List<String> children = categoryProjection.getChildren();
		assertThat(children.size()).isEqualTo(1);
		assertThat(children.get(0)).isEqualTo(CATEGORY_CODE + "Child");
	}

	@Test
	public void extractEmptyCategoryChildListTest() {
		final Category category = mockCategory(CATEGORY_CODE);
		final Store store = mockStore();
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, store, mockCatalog());
		final List<String> children = categoryProjection.getChildren();
		assertThat(children.size()).isEqualTo(0);
	}

	@Test
	public void extractPathTest() {
		final Category categoryParent = mockCategory(PARENT_CATEGORY_CODE);
		final Category secondCategoryParent = mockCategory(PARENT_CATEGORY_CODE + "Second");
		final Category category = mockCategory(CATEGORY_CODE);
		when(categoryLookup.findParent(category)).thenReturn(categoryParent);
		when(categoryLookup.findParent(categoryParent)).thenReturn(secondCategoryParent);
		when(categoryLookup.findParent(secondCategoryParent)).thenReturn(null);
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());
		final List<String> path = categoryProjection.getPath();

		assertThat(path.size()).isEqualTo(2);
		assertThat(path.get(0)).isEqualTo(PARENT_CATEGORY_CODE + "Second");
		assertThat(path.get(1)).isEqualTo(PARENT_CATEGORY_CODE);
	}

	@Test
	public void extractEmptyPathTest() {
		final Category category = mockCategory(CATEGORY_CODE);
		when(categoryLookup.findParent(category)).thenReturn(null);
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());
		final List<String> path = categoryProjection.getPath();

		assertThat(path.size()).isEqualTo(0);
	}

	@Test
	public void extractParentTest() {
		final Category categoryParent = mockCategory(PARENT_CATEGORY_CODE);
		final Category category = mockCategory(CATEGORY_CODE);
		when(categoryLookup.findParent(category)).thenReturn(categoryParent);
		when(categoryLookup.findParent(categoryParent)).thenReturn(null);
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());
		final String parent = categoryProjection.getParent();

		assertThat(parent).isEqualTo(PARENT_CATEGORY_CODE);
	}

	@Test
	public void extractEmptyParentTest() {
		final Category category = mockCategory(CATEGORY_CODE);
		when(categoryLookup.findParent(category)).thenReturn(null);
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());
		final String parent = categoryProjection.getParent();

		assertThat(parent).isNull();
	}

	@Test
	public void enableDateTimeShouldHaveGreatestDateStartDateFromParentTest() {
		final Category categoryParent = mockCategory(PARENT_CATEGORY_CODE);
		final Category category = mockCategory(CATEGORY_CODE);
		when(categoryLookup.findParent(category)).thenReturn(categoryParent);
		when(categoryLookup.findParent(categoryParent)).thenReturn(null);
		when(category.getStartDate()).thenReturn(Date.from(DATE_TIME.toInstant()));
		when(categoryParent.getStartDate()).thenReturn(Date.from(GREATEST_DATE_TIME.toInstant()));
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());
		final AvailabilityRules rules = categoryProjection.getAvailabilityRules();

		assertThat(rules.getEnableDateTime()).isEqualTo(GREATEST_DATE_TIME);
	}

	@Test
	public void disableDateTimeShouldHaveSmallestEndDateFromParentTest() {
		final Category categoryParent = mockCategory(PARENT_CATEGORY_CODE);
		final Category category = mockCategory(CATEGORY_CODE);
		when(categoryLookup.findParent(category)).thenReturn(categoryParent);
		when(categoryLookup.findParent(categoryParent)).thenReturn(null);
		when(category.getEndDate()).thenReturn(Date.from(GREATEST_DATE_TIME.toInstant()));
		when(categoryParent.getEndDate()).thenReturn(Date.from(PARENT_DATE_TIME.toInstant()));
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());
		final AvailabilityRules rules = categoryProjection.getAvailabilityRules();

		assertThat(rules.getDisableDateTime()).isEqualTo(PARENT_DATE_TIME);
	}

	@Test
	public void enableDateTimeShouldHaveGreatestStartDateFromCurrentCategoryTest() {
		final Category categoryParent = mockCategory(PARENT_CATEGORY_CODE);
		final Category category = mockCategory(CATEGORY_CODE);
		when(categoryLookup.findParent(category)).thenReturn(categoryParent);
		when(categoryLookup.findParent(categoryParent)).thenReturn(null);
		when(category.getStartDate()).thenReturn(Date.from(GREATEST_DATE_TIME.toInstant()));
		when(categoryParent.getStartDate()).thenReturn(Date.from(PARENT_DATE_TIME.toInstant()));
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());
		final AvailabilityRules rules = categoryProjection.getAvailabilityRules();

		assertThat(rules.getEnableDateTime()).isEqualTo(GREATEST_DATE_TIME);
	}

	@Test
	public void disableDateTimeShouldHaveSmallestEndDateFromCurrentCategoryTest() {
		final Category categoryParent = mockCategory(PARENT_CATEGORY_CODE);
		final Category category = mockCategory(CATEGORY_CODE);
		when(categoryLookup.findParent(category)).thenReturn(categoryParent);
		when(categoryLookup.findParent(categoryParent)).thenReturn(null);
		when(category.getEndDate()).thenReturn(Date.from(DATE_TIME.toInstant()));
		when(categoryParent.getEndDate()).thenReturn(Date.from(GREATEST_DATE_TIME.toInstant()));
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());
		final AvailabilityRules rules = categoryProjection.getAvailabilityRules();

		assertThat(rules.getDisableDateTime()).isEqualTo(DATE_TIME);
	}

	@Test
	public void availabilityRulesShouldHaveCurrentCategoryStartDateAndCurrentCategoryEndDateTest() {
		final Category category = mockCategory(CATEGORY_CODE);
		when(categoryLookup.findParent(category)).thenReturn(null);
		when(category.getStartDate()).thenReturn(Date.from(DATE_TIME.toInstant()));
		when(category.getEndDate()).thenReturn(Date.from(DATE_TIME.toInstant()));
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());
		final AvailabilityRules rules = categoryProjection.getAvailabilityRules();

		assertThat(rules.getDisableDateTime()).isEqualTo(DATE_TIME);
		assertThat(rules.getEnableDateTime()).isEqualTo(DATE_TIME);
	}

	@Test
	public void availabilityRulesShouldHaveNullStartDateAndNullEndDateTest() {
		final Category category = mockCategory(CATEGORY_CODE);
		when(categoryLookup.findParent(category)).thenReturn(null);
		when(category.getStartDate()).thenReturn(null);
		when(category.getEndDate()).thenReturn(null);
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());
		final AvailabilityRules rules = categoryProjection.getAvailabilityRules();

		assertThat(rules.getDisableDateTime()).isNull();
		assertThat(rules.getEnableDateTime()).isNull();
	}

	@Test
	public void availabilityRulesShouldHaveCurrentCategoryEndDateTest() {
		final Category categoryParent = mockCategory(PARENT_CATEGORY_CODE);
		final Category category = mockCategory(CATEGORY_CODE);
		when(categoryLookup.findParent(category)).thenReturn(categoryParent);
		when(categoryLookup.findParent(categoryParent)).thenReturn(null);
		when(category.getEndDate()).thenReturn(Date.from(DATE_TIME.toInstant()));
		when(categoryParent.getEndDate()).thenReturn(null);
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());
		final AvailabilityRules rules = categoryProjection.getAvailabilityRules();

		assertThat(rules.getDisableDateTime()).isEqualTo(DATE_TIME);
	}

	@Test
	public void syndicatedCategoryShouldBeConvertedToNotDeletedProjection() {
		final Category category = mockCategory(CATEGORY_CODE);
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		when(categoryService.canSyndicate(category)).thenReturn(true);

		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());

		assertFalse(categoryProjection.isDeleted());
	}

	@Test
	public void notSyndicatedCategoryShouldBeConvertedToDeletedProjection() {
		final Category category = mockCategory(CATEGORY_CODE);
		when(categoryService.canSyndicate(category)).thenReturn(false);
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);

		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, mockStore(), mockCatalog());

		assertTrue(categoryProjection.isDeleted());
	}

	@Test
	public void testThatProviderGetCategoryWriterRepositoryToSaveParentProjectionWithUpdatedChildListWhenNewCategoryAdded() {
		final Catalog catalog = mockCatalog();
		final Category category = mockCategory(CATEGORY_CODE);
		final Category parentCategory = mockCategory(PARENT_CATEGORY_CODE);
		final Store store = mockStore();
		final com.elasticpath.catalog.entity.category.Category parentCategoryProjection = new com.elasticpath.catalog.entity.category.Category(
				new CategoryProperties(new ProjectionProperties("code", store.getCode(), ZonedDateTime.now(), false),
						Collections.emptyList()), new Object(), Collections.emptyList(), new ArrayList<>(),
				null, null, null);

		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);

		when(categoryService.canSyndicate(category)).thenReturn(true);
		when(categoryLookup.findParent(category)).thenReturn(parentCategory);
		when(CATEGORY_READER_CAPABILITY.get().get(store.getCode(), PARENT_CATEGORY_CODE)).thenReturn(Optional.of(parentCategoryProjection));
		when(categoryService.findDirectDescendantCategories(CATEGORY_GUID)).thenReturn(Collections.singletonList(category));

		converter.convert(category, store, catalog);

		verify(provider.getCatalogProjectionPlugin()).getWriterCapability(CategoryWriterRepository.class);
	}

	@Test
	public void testThatProviderGetCategoryWriterRepositoryToSaveParentProjectionWithUpdatedChildListWhenChildWasDeleted() {
		final Catalog catalog = mockCatalog();
		final Category category = mockCategory(CATEGORY_CODE);
		final Category parentCategory = mockCategory(PARENT_CATEGORY_CODE);
		final Store store = mockStore();
		final List<String> children = new ArrayList<>();
		children.add(CATEGORY_CODE);
		final com.elasticpath.catalog.entity.category.Category parentCategoryProjection = new com.elasticpath.catalog.entity.category.Category(
				new CategoryProperties(new ProjectionProperties("code", store.getCode(), ZonedDateTime.now(), false),
						Collections.emptyList()), new Object(), Collections.emptyList(), children,
				null, null, null);
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);

		when(categoryService.canSyndicate(category)).thenReturn(false);
		when(categoryLookup.findParent(category)).thenReturn(parentCategory);
		when(CATEGORY_READER_CAPABILITY.get().get(store.getCode(), PARENT_CATEGORY_CODE)).thenReturn(Optional.of(parentCategoryProjection));

		converter.convert(category, store, catalog);

		assertThat(parentCategoryProjection.getChildren()).isEmpty();
		verify(provider.getCatalogProjectionPlugin()).getWriterCapability(CategoryWriterRepository.class);
	}

	@Test
	public void testThatConverterUpdatesChildrenOrder() {
		final Catalog catalog = mockCatalog();
		final Category category = mockCategory(CATEGORY_CODE);

		final Category child1 = mockCategory("a1");
		final Category child2 = mockCategory("a2");

		final Category parentCategory = mockCategory(PARENT_CATEGORY_CODE);
		final Store store = mockStore();
		final com.elasticpath.catalog.entity.category.Category parentCategoryProjection = new com.elasticpath.catalog.entity.category.Category(
				new CategoryProperties(new ProjectionProperties(PARENT_CATEGORY_CODE, store.getCode(), ZonedDateTime.now(), false),
						Collections.emptyList()), new Object(), Collections.emptyList(), new ArrayList<>(Arrays.asList("a1", CATEGORY_CODE, "a2")),
				null, null, null);

		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);

		when(categoryService.canSyndicate(category)).thenReturn(true);
		when(categoryLookup.findParent(category)).thenReturn(parentCategory);
		when(CATEGORY_READER_CAPABILITY.get().get(store.getCode(), PARENT_CATEGORY_CODE)).thenReturn(Optional.of(parentCategoryProjection));
		when(categoryService.findDirectDescendantCategories(parentCategory.getGuid())).thenReturn(Arrays.asList(child1, child2, category));

		converter.convert(category, store, catalog);

		assertThat(parentCategoryProjection).extracting(com.elasticpath.catalog.entity.category.Category::getChildren)
				.isEqualTo(Arrays.asList("a1", "a2", CATEGORY_CODE));
	}

	@Test
	public void testThatListChildHasCorrectOrderAfterAddedDeletedChildCategory() {
		final Catalog catalog = mockCatalog();
		final Category category = mockCategory(CATEGORY_CODE);
		final Category category1 = mockCategory(CATEGORY_CODE + "1");
		final Category category2 = mockCategory(CATEGORY_CODE + "2");
		final Category parentCategory = mockCategory(PARENT_CATEGORY_CODE);
		final Store store = mockStore();
		final List<Category> children = Arrays.asList(category, category1, category2);
		final List<String> listChildCodesWithoutDeletedCategory = getCategoryCode(Arrays.asList(category1, category2));

		final com.elasticpath.catalog.entity.category.Category parentCategoryProjection = new com.elasticpath.catalog.entity.category.Category(
				new CategoryProperties(new ProjectionProperties(PARENT_CATEGORY_CODE, store.getCode(), ZonedDateTime.now(), false),
						Collections.emptyList()), new Object(), Collections.emptyList(), listChildCodesWithoutDeletedCategory,
				null, null, null);
		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);

		when(categoryService.canSyndicate(category)).thenReturn(true);
		when(categoryLookup.findParent(category)).thenReturn(parentCategory);
		when(categoryService.findDirectDescendantCategories(parentCategory.getGuid())).thenReturn(children);
		when(CATEGORY_READER_CAPABILITY.get().get(store.getCode(), PARENT_CATEGORY_CODE)).thenReturn(Optional.of(parentCategoryProjection));

		converter.convert(category, store, catalog);

		assertThat(parentCategoryProjection.getChildren().size()).isEqualTo(children.size());
		assertThat(parentCategoryProjection.getChildren().get(0)).isEqualTo(children.get(0).getCode());
		assertThat(parentCategoryProjection.getChildren().get(1)).isEqualTo(children.get(1).getCode());
		assertThat(parentCategoryProjection.getChildren().get(2)).isEqualTo(children.get(2).getCode());
	}

	@Test
	public void linkedChildCategoryStandTombstoneIfItExcludedAndMasterChildCategoryIsTombstoneBecomesNotTombstone() {
		final Store store = mockStore();
		final Category masterCategory = mockCategory(CATEGORY_CODE);
		final Category linkedCategory = mock(CategoryImpl.class);
		final Category masterChildCategory = mockCategory(CATEGORY_CODE + "Children");
		final Category linkedChildCategory = mock(CategoryImpl.class);
		final com.elasticpath.catalog.entity.category.Category linkedCategoryProjection = mockCategoryProjection(STORE_CODE + "Link");
		final com.elasticpath.catalog.entity.category.Category masterCategoryProjection = mockCategoryProjection(STORE_CODE);
		final com.elasticpath.catalog.entity.category.Category linkedChildCategoryProjection = mockCategoryProjection(STORE_CODE + "ChildLinked");
		final com.elasticpath.catalog.entity.category.Category masterChildCategoryProjection = mockCategoryProjection(STORE_CODE + "Children");

		when(catalogService.readAll(CATEGORY_IDENTITY_TYPE, CATEGORY_CODE + "Children")).thenReturn(Arrays.asList(linkedCategoryProjection,
				masterCategoryProjection, linkedChildCategoryProjection, masterChildCategoryProjection));
		when(categoryService.findLinkedCategories(anyLong())).thenReturn(Arrays.asList(linkedCategory, linkedChildCategory));
		when(categoryService.findDirectDescendantCategories(masterCategory.getGuid())).thenReturn(Collections.singletonList(masterChildCategory));
		when(categoryLookup.findParent(masterChildCategory)).thenReturn(masterCategory);

		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);

		CatalogProjectionPlugin catalogProjectionPlugin = mock(CatalogProjectionPlugin.class);
		CategoryWriterRepository categoryWriterRepository = mock(CategoryWriterRepository.class);
		AttributeReaderCapability attributeReaderCapability = mock(AttributeReaderCapability.class);
		CategoryReaderCapability categoryReaderCapability = mock(CategoryReaderCapability.class);
		when(provider.getCatalogProjectionPlugin()).thenReturn(catalogProjectionPlugin);
		when(catalogProjectionPlugin.getWriterCapability(CategoryWriterRepository.class)).thenReturn(Optional.ofNullable(categoryWriterRepository));
		when(catalogProjectionPlugin.getReaderCapability(AttributeReaderCapability.class)).thenReturn(Optional.ofNullable(attributeReaderCapability));
		when(catalogProjectionPlugin.getReaderCapability(CategoryReaderCapability.class)).thenReturn(Optional.ofNullable(categoryReaderCapability));
		when(categoryReaderCapability.get(store.getCode(), CATEGORY_CODE)).thenReturn(Optional.ofNullable(masterChildCategoryProjection));

		converter.convert(masterChildCategory, store, mockCatalog());

		final int wantedNumberOfInvocations = 4;
		verify(provider, times(wantedNumberOfInvocations)).getCatalogProjectionPlugin();
		verify(catalogProjectionPlugin, times(2)).getReaderCapability(CategoryReaderCapability.class);
		verify(catalogProjectionPlugin, times(1)).getReaderCapability(AttributeReaderCapability.class);
		verify(catalogProjectionPlugin, times(1)).getWriterCapability(CategoryWriterRepository.class);
	}

	@Test
	public void extractTranslationsTest() {
		final Catalog catalog = mockCatalog();
		final Store store = mockStore();
		final Category category = mockCategory(CATEGORY_CODE);
		final com.elasticpath.catalog.entity.attribute.Attribute attribute = mock(com.elasticpath.catalog.entity.attribute.Attribute.class);
		when(translationExtractor.getProjectionTranslations(any(), any(), any())).thenReturn(Collections.singletonList(new Translation("en",
				"displayName")));
		when(categoryService.canSyndicate(category)).thenReturn(true);
		when(ATTRIBUTE_READER_CAPABILITY.get().findAllWithCodes(store.getCode(), Collections.singletonList(KEY_ATTRIBUTE)))
				.thenReturn(Collections.singletonList(attribute));
		when(attribute.getIdentity()).thenReturn(new NameIdentity(ATTRIBUTE_IDENTITY_TYPE, KEY_ATTRIBUTE, STORE_CODE));
		when(attribute.getTranslations()).thenReturn(Collections.singletonList(new AttributeTranslation(new Translation("en",
				"attributeName"), "", false)));

		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);

		final com.elasticpath.catalog.entity.category.Category categoryProjection = converter.convert(category, store, catalog);

		assertThat(categoryProjection.getTranslations().get(0).getLanguage()).isEqualTo("en");
		assertThat(categoryProjection.getTranslations().get(0).getDisplayName()).isEqualTo("displayName");
		assertThat(categoryProjection.getTranslations().get(0).getDetails().get(0).getName()).isEqualTo(KEY_ATTRIBUTE);
		assertThat(categoryProjection.getTranslations().get(0).getDetails().get(0).getDisplayName()).isEqualTo("attributeName");
		assertThat(categoryProjection.getTranslations().get(0).getDetails().get(0).getValues().get(0)).isEqualTo(TEST_VALUE);
		assertThat(categoryProjection.getTranslations().get(0).getDetails().get(0).getDisplayValues().get(0)).isEqualTo(TEST_VALUE);
	}

	@Test
	public void testLinkedCategoryIsUpdatedIfMasterCategoryWasUpdated() {
		final Catalog catalog = mockCatalog();
		final Category category = mockCategory(CATEGORY_CODE);
		final Catalog linkedCatalog = mockCatalog();
		when(linkedCatalog.getCode()).thenReturn("catalog");
		final Category linkedCategory = mockLinkedCategory();
		final com.elasticpath.catalog.entity.category.Category linkedCategoryProjection = mockCategoryProjection(STORE_CODE + "Linked");
		final com.elasticpath.catalog.entity.category.Category masterCategory = mockCategoryProjection(STORE_CODE);
		final Category categoryChild = mockCategory(CATEGORY_CODE + "Child");
		final Store store = mockStore();

		when(linkedCategory.getCatalog()).thenReturn(catalog);
		when(categoryService.findDirectDescendantCategories(category.getGuid())).thenReturn(Collections.singletonList(categoryChild));
		when(linkedCategory.getCatalog()).thenReturn(linkedCatalog);
		when(catalogService.readAll(CATEGORY_IDENTITY_TYPE, CATEGORY_CODE)).thenReturn(Arrays.asList(linkedCategoryProjection, masterCategory));
		when(translationExtractor.getProjectionTranslations(any(), any(), any())).thenReturn(Collections.singletonList(new Translation("en",
				"displayName")));
		when(storeService.getCatalogCodeForStore(anyString())).thenReturn("catalog");
		when(storeService.findStoreWithCode(STORE_CODE + "Linked")).thenReturn(store);
		when(storeService.findStoresWithCatalogCode(catalog.getCode())).thenReturn(Collections.singletonList(store));
		when(categoryService.findLinkedCategories(anyLong())).thenReturn(Collections.singletonList(linkedCategory));

		final CategoryToProjectionConverter converter = new CategoryToProjectionConverter(categoryService, timeService, provider,
				translationExtractor, categoryLookup, catalogService, storeService);
		final com.elasticpath.catalog.entity.category.Category categoryResult = converter.convert(category, store, mockCatalog());

		ArgumentCaptor<com.elasticpath.catalog.entity.category.Category> categoryArgumentCaptor =
				ArgumentCaptor.forClass(com.elasticpath.catalog.entity.category.Category.class);
		verify(CATEGORY_WRITER_CAPABILITY.get(), atLeastOnce()).write(categoryArgumentCaptor.capture());
		final com.elasticpath.catalog.entity.category.Category linkedCategoryResult = categoryArgumentCaptor.getValue();

		assertThat(linkedCategoryResult.getProperties()).isEqualTo(categoryResult.getProperties());
		assertThat(linkedCategoryResult.getTranslations().get(0).getDisplayName())
				.isEqualTo(categoryResult.getTranslations().get(0).getDisplayName());
		assertThat(linkedCategoryResult.getChildren()).isEqualTo(categoryResult.getChildren());
		assertThat(linkedCategoryResult.getParent()).isEqualTo(categoryResult.getParent());
		assertThat(linkedCategoryResult.getPath()).isEqualTo(categoryResult.getPath());
		assertThat(linkedCategoryResult.getAvailabilityRules()).isEqualTo(categoryResult.getAvailabilityRules());
	}

	private Category mockCategory(final String code) {
		final LocaleDependantFields localeDependantFields = mock(LocaleDependantFields.class);
		final Category category = mock(CategoryImpl.class);
		final Category categoryParent = mock(CategoryImpl.class);
		final CategoryType categoryType = mock(CategoryType.class);
		final AttributeValue attributeValue = mock(AttributeValue.class);
		final Attribute attribute = mock(Attribute.class);
		final Translation translation = new Translation("en", "name");
		final AttributeGroup attributeGroup = mock(AttributeGroup.class);
		final AttributeValueGroup attributeValueGroup = mock(AttributeValueGroup.class);

		when(categoryType.getName()).thenReturn(CATEGORY_TYPE);
		when(category.getGuid()).thenReturn(CATEGORY_GUID);
		when(category.getCategoryType()).thenReturn(categoryType);
		when(categoryService.canSyndicate(category)).thenReturn(true);
		when(category.getCode()).thenReturn(code);
		when(category.getLocaleDependantFields(Locale.ENGLISH)).thenReturn(localeDependantFields);
		when(localeDependantFields.getDisplayName()).thenReturn("product_display_name");
		when(category.getAttributeValueMap()).thenReturn(Collections.singletonMap("", attributeValue));
		when(attributeValue.getAttribute()).thenReturn(attribute);
		when(attribute.getKey()).thenReturn(KEY_ATTRIBUTE);
		when(categoryLookup.findParent(category))
				.thenReturn(categoryParent);
		when(categoryParent.getCode()).thenReturn("parent");
		when(translationExtractor.getProjectionTranslations(any(), any(), any())).thenReturn(Collections.singletonList(translation));
		when(categoryType.getAttributeGroup()).thenReturn(attributeGroup);
		when(category.getAttributeValueGroup()).thenReturn(attributeValueGroup);
		when(attributeValue.getValue()).thenReturn(TEST_VALUE);
		when(attributeValue.getStringValue()).thenReturn(TEST_VALUE);
		when(attributeValue.getAttributeType()).thenReturn(AttributeType.SHORT_TEXT);
		when(attributeValueGroup.getFullAttributeValues(attributeGroup, Locale.ENGLISH))
				.thenReturn(Collections.singletonList(attributeValue));
		return category;
	}

	private Category mockLinkedCategory() {
		final LocaleDependantFields localeDependantFields = mock(LocaleDependantFields.class);
		final Category category = mock(CategoryImpl.class);
		final CategoryType categoryType = mock(CategoryType.class);
		final AttributeValue attributeValue = mock(AttributeValue.class);
		final Attribute attribute = mock(Attribute.class);
		final Translation translation = new Translation("en", "name");
		final AttributeGroup attributeGroup = mock(AttributeGroup.class);
		final AttributeValueGroup attributeValueGroup = mock(AttributeValueGroup.class);
		when(category.getCategoryType()).thenReturn(categoryType);
		when(category.isIncluded()).thenReturn(true);
		when(category.getLocaleDependantFields(Locale.ENGLISH)).thenReturn(localeDependantFields);
		when(localeDependantFields.getDisplayName()).thenReturn("product_display_name");
		when(category.getAttributeValueMap()).thenReturn(Collections.singletonMap("", attributeValue));
		when(attributeValue.getAttribute()).thenReturn(attribute);
		when(attribute.getKey()).thenReturn(KEY_ATTRIBUTE);
		when(translationExtractor.getProjectionTranslations(any(), any(), any())).thenReturn(Collections.singletonList(translation));
		when(categoryType.getAttributeGroup()).thenReturn(attributeGroup);
		when(category.getAttributeValueGroup()).thenReturn(attributeValueGroup);
		when(attributeValue.getValue()).thenReturn(TEST_VALUE);
		when(attributeValue.getStringValue()).thenReturn(TEST_VALUE);
		when(attributeValue.getAttributeType()).thenReturn(AttributeType.SHORT_TEXT);
		when(attributeValueGroup.getFullAttributeValues(attributeGroup, Locale.ENGLISH))
				.thenReturn(Collections.singletonList(attributeValue));
		return category;
	}

	private com.elasticpath.catalog.entity.category.Category mockCategoryProjection(final String store) {
		final NameIdentity nameIdentity = new NameIdentity("category", CATEGORY_CODE, store);
		final com.elasticpath.catalog.entity.category.Category category = mock(com.elasticpath.catalog.entity.category.Category.class);

		when(category.getIdentity()).thenReturn(nameIdentity);
		return category;
	}

	private Store mockStore() {
		final Store store = mock(Store.class);
		when(store.getCode()).thenReturn(STORE_CODE);
		when(store.getSupportedLocales()).thenReturn(Collections.singletonList(Locale.ENGLISH));
		when(store.getDefaultLocale()).thenReturn(Locale.ENGLISH);
		return store;
	}

	private Catalog mockCatalog() {
		final Catalog catalog = mock(Catalog.class);
		when(catalog.getDefaultLocale()).thenReturn(Locale.ENGLISH);
		return catalog;
	}

	private List<String> getCategoryCode(final List<Category> categories) {
		return categories.stream()
				.map(Category::getCode)
				.collect(Collectors.toList());
	}
}
