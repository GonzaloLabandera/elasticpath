/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;
import static com.elasticpath.core.messaging.domain.DomainEventType.CATEGORY_DELETED;
import static com.elasticpath.core.messaging.domain.DomainEventType.CATEGORY_LINK_DELETED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.log4j.Logger;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.TransactionStatus;

import com.elasticpath.catalog.entity.AbstractProjection;
import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.messages.RelayOutboxMessagesThreadExecutor;
import com.elasticpath.catalog.plugin.converter.impl.EntityToCategoryConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionHistoryRepository;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionRepository;
import com.elasticpath.catalog.update.processor.capabilities.CategoryUpdateProcessor;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.CategoryType;
import com.elasticpath.domain.catalog.DefaultValueRemovalForbiddenException;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.catalog.impl.CategoryImpl;
import com.elasticpath.domain.catalog.impl.CategoryTypeImpl;
import com.elasticpath.domain.catalog.impl.LinkedCategoryImpl;
import com.elasticpath.domain.message.handler.category.handler.CategoryDeletedEventHandler;
import com.elasticpath.domain.message.handler.category.handler.CategoryUnlinkedEventHandler;
import com.elasticpath.domain.store.Store;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Integration tests for {@link CategoryUpdateProcessorImpl}.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DirtiesDatabase
public class CategoryUpdateProcessorImplIntegrationTest extends DbTestCase {

	private static final Logger LOGGER = Logger.getLogger(CategoryUpdateProcessorImplIntegrationTest.class);

	private static final String CATEGORY_CODE = "code";
	private static final String CATEGORY_TYPE = "categoryType";
	private static final String CATEGORY_TYPE_GUID = "categoryTypeGuid";
	private static final String CATEGORY_GUID = "categoryGuid";
	private static final String LINKED_CATEGORY_GUID = "linkedCategoryGuid";
	private static final String CATEGORY_DISPLAY_NAME = "category";
	private static final String WRONG_COMPOUND_GUID = "wrongGuid|wrongCatalog";
	private static final String CATEGORY_GUID_CHILD = "childCategoryGuid";
	private static final String CATEGORY_GUID_CHILD_DELETED = "deletedChildCategoryGuid";
	private static final String CATEGORY_CODE_CHILD = "codeChild";
	private static final String CATEGORY_CODE_CHILD_DELETED = "codeChildDeleted";
	private static final int COUNT_CREATED_CATEGORIES = 3;
	private static final int ONE_MINUTE = 60 * 1000;

	private static final String CATALOG_MESSAGING_CAMEL_CONTEXT = "ep-catalog-messaging";
	private static final String JMS_CATALOG_ENDPOINT = "jms:topic:VirtualTopic.ep.catalog";

	@Autowired
	private EntityToCategoryConverter categoryConverter;

	@Autowired
	private CatalogProjectionRepository catalogProjectionRepository;

	@Autowired
	private CategoryUpdateProcessor categoryUpdateProcessor;

	@Autowired
	private CatalogProjectionHistoryRepository historyRepository;

	@Autowired
	private CategoryDeletedEventHandler categoryDeletedEventHandler;

	@Autowired
	private CategoryUnlinkedEventHandler categoryUnlinkedEventHandler;

	@Autowired
	private EventMessageFactory eventMessageFactory;

	@Autowired
	private RelayOutboxMessagesThreadExecutor relayOutboxMessagesThreadExecutor;

	@Autowired
	@Qualifier(CATALOG_MESSAGING_CAMEL_CONTEXT)
	private CamelContext catalogCamelContext;

	@Before
	public void setUp() throws Exception {
		catalogCamelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(JMS_CATALOG_ENDPOINT)
						.process(exchange -> LOGGER.info("Catalog endpoint exchange: " + exchange.getIn().getBody()));
			}
		});
		relayOutboxMessagesThreadExecutor.start();
	}

	@After
	public void tearDown() {
		relayOutboxMessagesThreadExecutor.stop();
	}

	@Test
	public void shouldRemoveDeletedChildProjectionFromParentChildList() throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCatalogEvents = 3;

		final Store store = createAndPersistStore();
		final Catalog catalog = store.getCatalog();
		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog);

		final NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		createAndPersistCategory(CATEGORY_GUID, CATEGORY_CODE, categoryType, catalog, false);
		createAndPersistChildCategory(CATEGORY_GUID_CHILD,
				CATEGORY_CODE_CHILD, categoryType, catalog, false);
		createAndPersistChildCategory(CATEGORY_GUID_CHILD_DELETED,
				CATEGORY_CODE_CHILD_DELETED, categoryType, catalog, true);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);

		final Iterable<ProjectionEntity> projectionEntities = catalogProjectionRepository.findAll();
		final ProjectionEntity projectionParentCategory = StreamSupport.stream(projectionEntities.spliterator(), false)
				.filter(projection -> projection.getCode().equals(CATEGORY_CODE))
				.findFirst().get();
		final com.elasticpath.catalog.entity.category.Category parentProjection = categoryConverter.convert(projectionParentCategory);

		assertThat(projectionEntities).hasSize(COUNT_CREATED_CATEGORIES);
		assertThat(parentProjection.getChildren().size()).isEqualTo(1);
		assertThat(parentProjection.getChildren().get(0)).isEqualTo(CATEGORY_CODE_CHILD);
	}

	@Test
	public void shouldCreateNewProjectionHistoryWhenEventIsDeletedAndSameProjectionNotExistInDatabase()
			throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCategoryDeletedEvents = 1;

		final Store store1 = createAndPersistStore();
		final Catalog catalog1 = store1.getCatalog();

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog1);
		final Category category = createAndPersistCategory(CATEGORY_GUID, CATEGORY_CODE, categoryType, catalog1, false);
		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, category.getCode(), store1.getCode()));

		final NotifyBuilder catalogNotifyBuilderDeletedCategory = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCategoryDeletedEvents).create();

		doInTransaction(status -> delete(category));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderDeletedCategory::matches);

		final Iterable<ProjectionEntity> projectionEntities = catalogProjectionRepository.findAll();
		final Iterable<ProjectionHistoryEntity> projectionHistoryEntities = historyRepository.findAll();

		assertThat(projectionEntities).hasSize(1);
		assertThat(projectionHistoryEntities).hasSize(1);
		assertThat(projectionEntities).extracting(ProjectionEntity::getCode).containsOnly(CATEGORY_CODE);
		assertThat(projectionEntities).extracting(ProjectionEntity::isDeleted).containsOnly(true);
		assertThat(projectionHistoryEntities).extracting(ProjectionHistoryEntity::getCode).containsOnly(CATEGORY_CODE);
		assertThat(projectionHistoryEntities).extracting(ProjectionHistoryEntity::isDeleted).containsOnly(false);
	}

	@Test
	public void shouldNotCreateAnyProjectionsWhenCategoryEntitiesNotExistAndEventCategoryDeleted() {
		final EventMessage eventMessage = eventMessageFactory.createEventMessage(CATEGORY_DELETED, WRONG_COMPOUND_GUID);
		categoryDeletedEventHandler.handleMessage(eventMessage);

		final Iterable<ProjectionEntity> projectionEntities = catalogProjectionRepository.findAll();
		final Iterable<ProjectionHistoryEntity> projectionHistoryEntities = historyRepository.findAll();

		assertThat(projectionEntities).isEmpty();
		assertThat(projectionHistoryEntities).isEmpty();
	}

	@Test
	public void shouldCreateNewProjectionWhenEventIsLinkCreatedAndSameProjectionNotExistInDatabase() throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCatalogEvents = 2;
		final int expectedNumberOfProjectionEntities = 2;

		final Store store1 = createAndPersistStore();
		final Catalog catalog1 = store1.getCatalog();

		final Store store2 = createAndPersistStore();
		final Catalog catalog2 = store2.getCatalog();

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog1);

		final NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		final Category category = createAndPersistCategory(CATEGORY_GUID, CATEGORY_CODE, categoryType, catalog1, false);
		createAndPersistLinkedCategory(LINKED_CATEGORY_GUID, category, catalog2);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);

		final Iterable<ProjectionEntity> projectionEntities = catalogProjectionRepository.findAll();
		final Iterable<ProjectionHistoryEntity> projectionHistoryEntities = historyRepository.findAll();

		assertThat(projectionEntities).hasSize(expectedNumberOfProjectionEntities);
		assertThat(projectionHistoryEntities).isEmpty();
		assertThat(projectionEntities).extracting(ProjectionEntity::getCode).containsOnly(CATEGORY_CODE);
		assertThat(projectionEntities).extracting(ProjectionEntity::isDeleted).containsOnly(false);
	}

	@Test
	public void shouldCreateNewProjectionHistoryWhenEventIsLinkDeletedAndSameProjectionExistsInDatabase()
			throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCatalogEvents = 1;
		final int expectedNumberOfProjectionEntities = 2;

		final Store store1 = createAndPersistStore();
		final Catalog catalog1 = store1.getCatalog();

		final Store store2 = createAndPersistStore();
		final Catalog catalog2 = store2.getCatalog();

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog1);
		final Category category = createAndPersistCategory(CATEGORY_GUID, CATEGORY_CODE, categoryType, catalog1, false);
		final Category linkedCategory = createAndPersistLinkedCategory(LINKED_CATEGORY_GUID, category, catalog2);

		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, category.getCode(), store1.getCode()));
		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, category.getCode(), store2.getCode()));

		final NotifyBuilder catalogNotifyBuilderDeletedCategory = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		doInTransaction(status -> delete(linkedCategory));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderDeletedCategory::matches);

		final Iterable<ProjectionEntity> projectionEntities = catalogProjectionRepository.findAll();
		final Iterable<ProjectionHistoryEntity> projectionHistoryEntities = historyRepository.findAll();

		assertThat(projectionEntities).hasSize(expectedNumberOfProjectionEntities);
		assertThat(projectionHistoryEntities).hasSize(1);
		assertThat(projectionEntities).extracting(ProjectionEntity::getCode).containsOnly(CATEGORY_CODE);
		assertThat(projectionEntities).filteredOn(projectionEntity -> projectionEntity.getProjectionId().getStore().equals(store1.getCode()))
				.extracting(ProjectionEntity::isDeleted).containsOnly(false);
		assertThat(projectionEntities).filteredOn(projectionEntity -> projectionEntity.getProjectionId().getStore().equals(store2.getCode()))
				.extracting(ProjectionEntity::isDeleted).containsOnly(true);
		assertThat(projectionHistoryEntities).extracting(ProjectionHistoryEntity::getCode).containsOnly(CATEGORY_CODE);
		assertThat(projectionHistoryEntities).extracting(ProjectionHistoryEntity::isDeleted).containsOnly(false);
	}

	@Test
	public void shouldNotCreateAnyProjectionsWhenCategoryEntitiesNotExistAndEventCategoryLinkDeleted() {
		final EventMessage eventMessage = eventMessageFactory.createEventMessage(CATEGORY_LINK_DELETED, WRONG_COMPOUND_GUID);
		categoryUnlinkedEventHandler.handleMessage(eventMessage);

		final Iterable<ProjectionEntity> projectionEntities = catalogProjectionRepository.findAll();
		final Iterable<ProjectionHistoryEntity> projectionHistoryEntities = historyRepository.findAll();

		assertThat(projectionEntities).isEmpty();
		assertThat(projectionHistoryEntities).isEmpty();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void childrenListShouldContainsChildrenInParticularOrderAndNotContainsDuplicateAndShouldBe6ProjectionsFor3CategoryAnd2Stores()
			throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCatalogEvents = 6;

		final long firstVersion = 1;
		final int expectedNumberOfProjections = 6;

		final Catalog catalog = createCatalog();
		createAndPersistStore(catalog);
		createAndPersistStore(catalog);

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog);

		final NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		final Category rootCategory = createAndPersistCategory("rootGuid", "rootCategory", categoryType, catalog, false);
		createAndPersistCategory("sub1Guid", "subCategory1", categoryType, catalog, false, rootCategory);
		createAndPersistCategory("sub2Guid", "subCategory2", categoryType, catalog, false, rootCategory);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);

		final Iterable<ProjectionEntity> projectionEntities = catalogProjectionRepository.findAll();
		final Iterable<ProjectionHistoryEntity> projectionHistoryEntities = historyRepository.findAll();

		final List<com.elasticpath.catalog.entity.category.Category> categories =
				Lists.newArrayList(projectionEntities).stream().map(categoryConverter::convert).collect(Collectors.toList());

		final List<String> expectedChildrenList = Arrays.asList("subCategory1", "subCategory2");

		assertThat(projectionEntities).hasSize(expectedNumberOfProjections);
		assertThat(projectionHistoryEntities).isEmpty();

		assertThat(projectionEntities).extracting(ProjectionEntity::isDeleted).containsOnly(false);
		assertThat(projectionEntities).extracting(ProjectionEntity::getVersion).containsOnly(firstVersion);
		assertThat(categories)
				.filteredOn(category -> category.getIdentity().getCode().equals("rootCategory"))
				.extracting(com.elasticpath.catalog.entity.category.Category::getChildren)
				.containsExactly(expectedChildrenList, expectedChildrenList);
	}

	@Test
	public void testThatChangingTheVisibilityOfTheParentCategoryToFalseLeadsToAChangeInTheVisibilityOfHisChildrenToFalse()
			throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCatalogEvents = 2;

		final long secondVersion = 2;
		final Store store = createAndPersistStore();
		final Catalog catalog = store.getCatalog();

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog);
		final Category parentCategory = createAndPersistCategory(CATEGORY_GUID, CATEGORY_CODE, categoryType, catalog, true);
		final Category childCategory = createAndPersistCategory(CATEGORY_GUID_CHILD, CATEGORY_CODE_CHILD, categoryType, catalog, false,
				parentCategory);

		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, parentCategory.getCode(), store.getCode()));
		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, childCategory.getCode(), store.getCode()));

		final NotifyBuilder catalogNotifyBuilderForUpdateCategories = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		parentCategory.setHidden(false);
		updateAndPersistCategory(parentCategory);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderForUpdateCategories::matches);

		final Iterable<ProjectionEntity> editProjectionEntities = catalogProjectionRepository.findAll();
		final ProjectionEntity editProjectionParentCategory = StreamSupport.stream(editProjectionEntities.spliterator(), false)
				.filter(projection -> projection.getCode().equals(CATEGORY_CODE))
				.findFirst().get();

		final ProjectionEntity editProjectionChildCategory = StreamSupport.stream(editProjectionEntities.spliterator(), false)
				.filter(projection -> projection.getCode().equals(CATEGORY_CODE_CHILD))
				.findFirst().get();

		assertThat(editProjectionParentCategory).extracting(ProjectionEntity::getVersion).isEqualTo(secondVersion);
		assertThat(editProjectionParentCategory).extracting(ProjectionEntity::isDeleted).isEqualTo(false);
		assertThat(editProjectionParentCategory).extracting(ProjectionEntity::getContent).isNotNull();
		assertThat(editProjectionChildCategory).extracting(ProjectionEntity::getVersion).isEqualTo(secondVersion);
		assertThat(editProjectionChildCategory).extracting(ProjectionEntity::isDeleted).isEqualTo(false);
		assertThat(editProjectionChildCategory).extracting(ProjectionEntity::getContent).isNotNull();
	}

	@Test
	public void testThatChangingTheVisibilityOfTheParentCategoryToTrueLeadsToAChangeInTheVisibilityOfHisChildrenToTrue()
			throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfEventsAfterCategoryUpdate = 3;

		final long secondVersion = 2;
		final long thirdVersion = 3;
		final Store store = createAndPersistStore();
		final Catalog catalog = store.getCatalog();
		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog);

		final Category parentCategory = createAndPersistCategory(CATEGORY_GUID, CATEGORY_CODE, categoryType, catalog, false);
		final Category childCategory = createAndPersistCategory(CATEGORY_GUID_CHILD, CATEGORY_CODE_CHILD, categoryType, catalog, false,
				parentCategory);

		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, parentCategory.getCode(), store.getCode()));
		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, childCategory.getCode(), store.getCode()));

		final NotifyBuilder catalogNotifyBuilderForUpdateCategories = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfEventsAfterCategoryUpdate).create();

		parentCategory.setHidden(true);
		updateAndPersistCategory(parentCategory);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderForUpdateCategories::matches);

		final Iterable<ProjectionEntity> editProjectionEntities = catalogProjectionRepository.findAll();
		final ProjectionEntity editProjectionParentCategory = StreamSupport.stream(editProjectionEntities.spliterator(), false)
				.filter(projection -> projection.getCode().equals(CATEGORY_CODE))
				.findFirst().get();

		final ProjectionEntity editProjectionChildCategory = StreamSupport.stream(editProjectionEntities.spliterator(), false)
				.filter(projection -> projection.getCode().equals(CATEGORY_CODE_CHILD))
				.findFirst().get();

		assertThat(editProjectionParentCategory).extracting(ProjectionEntity::getVersion).isEqualTo(thirdVersion);
		assertThat(editProjectionParentCategory).extracting(ProjectionEntity::isDeleted).isEqualTo(true);
		assertThat(editProjectionParentCategory).extracting(ProjectionEntity::getContent).isNull();
		assertThat(editProjectionChildCategory).extracting(ProjectionEntity::getVersion).isEqualTo(secondVersion);
		assertThat(editProjectionChildCategory).extracting(ProjectionEntity::isDeleted).isEqualTo(true);
		assertThat(editProjectionChildCategory).extracting(ProjectionEntity::getContent).isNull();
	}

	@Test
	public void testThatLinkedParentCategoryUpdateChildCategoryListIfChildWasExcludedOrIncluded() throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCategoryCreatedEvents = 6;
		final int expectedNumberOfCategoryUpdatedEventsAfterExclude = 3;
		final int expectedNumberOfCategoryUpdatedEventsAfterInclude = 2;

		final Catalog catalog = createCatalog();
		createAndPersistStore(catalog);

		final Catalog virtualCatalog = createAndPersistVirtualCatalog("virtualCatalog", "virtualCatalog");

		final Store vStore = createAndPersistStore(virtualCatalog);

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog);

		final NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCategoryCreatedEvents).create();

		final Category rootCategory = createAndPersistCategory("rootGuid", "rootCategory", categoryType, catalog, false);
		final Category subCategory1 = createAndPersistCategory("sub1Guid", "subCategory1", categoryType, catalog, false, rootCategory);
		final Category subCategory2 = createAndPersistCategory("sub2Guid", "subCategory2", categoryType, catalog, false, rootCategory);
		final Category rootLinkedCategory = createAndPersistLinkedCategory("linkedGuid", rootCategory, virtualCatalog);
		final Category linkedSubCategory1 = createAndPersistLinkedCategoryWithParent("linkedSubGuid1", subCategory1, virtualCatalog,
				rootLinkedCategory);
		createAndPersistLinkedCategoryWithParent("linkedSubGuid2", subCategory2, virtualCatalog, rootLinkedCategory);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);

		final Iterable<ProjectionEntity> projectionEntities = catalogProjectionRepository.findAll();

		final List<com.elasticpath.catalog.entity.category.Category> categoriesBeforeExclude =
				Lists.newArrayList(projectionEntities).stream().map(categoryConverter::convert).collect(Collectors.toList());

		final NotifyBuilder catalogNotifyBuilderAfterUpdate = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCategoryUpdatedEventsAfterExclude).create();

		linkedSubCategory1.setIncluded(false);
		doInTransaction(status -> saveOrUpdate(linkedSubCategory1));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderAfterUpdate::matches);

		final Iterable<ProjectionEntity> updatedProjectionEntities = catalogProjectionRepository.findAll();

		final List<com.elasticpath.catalog.entity.category.Category> categoriesAfterExclude =
				Lists.newArrayList(updatedProjectionEntities).stream().map(categoryConverter::convert).collect(Collectors.toList());

		final NotifyBuilder catalogNotifyBuilderAfterInclude = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCategoryUpdatedEventsAfterInclude).create();

		linkedSubCategory1.setIncluded(true);
		doInTransaction(status -> saveOrUpdate(linkedSubCategory1));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderAfterInclude::matches);

		final Iterable<ProjectionEntity> projectionEntitiesAfterInclude = catalogProjectionRepository.findAll();

		final List<com.elasticpath.catalog.entity.category.Category> categoriesAfterInclude =
				Lists.newArrayList(projectionEntitiesAfterInclude).stream().map(categoryConverter::convert).collect(Collectors.toList());

		assertThat(categoriesBeforeExclude)
				.filteredOn(category -> category.getIdentity().getStore().equals(vStore.getCode()))
				.filteredOn(category -> category.getIdentity().getCode().equals(rootCategory.getCode()))
				.flatExtracting(com.elasticpath.catalog.entity.category.Category::getChildren)
				.containsExactly(subCategory1.getCode(), subCategory2.getCode());

		assertThat(categoriesAfterExclude)
				.filteredOn(category -> category.getIdentity().getStore().equals(vStore.getCode()))
				.filteredOn(category -> category.getIdentity().getCode().equals(rootCategory.getCode()))
				.flatExtracting(com.elasticpath.catalog.entity.category.Category::getChildren)
				.containsExactly(subCategory2.getCode());

		assertThat(categoriesAfterInclude)
				.filteredOn(category -> category.getIdentity().getStore().equals(vStore.getCode()))
				.filteredOn(category -> category.getIdentity().getCode().equals(rootCategory.getCode()))
				.flatExtracting(com.elasticpath.catalog.entity.category.Category::getChildren)
				.contains(subCategory1.getCode(), subCategory2.getCode());
	}

	@Test
	public void shouldUpdateLinkedCategoryIfMasterCategoryWasUpdated() throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCatalogEvents = 2;

		final Store store1 = createAndPersistStore();
		final Catalog catalog1 = store1.getCatalog();

		final Store store2 = createAndPersistStore();
		final Catalog catalog2 = store2.getCatalog();

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog1);

		final Category category = createAndPersistCategory(CATEGORY_GUID, CATEGORY_CODE, categoryType, catalog1, false);
		createAndPersistLinkedCategory(LINKED_CATEGORY_GUID, category, catalog2);

		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, category.getCode(), store1.getCode()));
		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, category.getCode(), store2.getCode()));

		final NotifyBuilder catalogNotifyBuilderForUpdateCategories = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		category.setDisplayName("NewName", Locale.getDefault());
		doInTransaction(status -> persist(category));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderForUpdateCategories::matches);

		final Iterable<ProjectionEntity> projectionEntities = catalogProjectionRepository.findAll();
		final List<com.elasticpath.catalog.entity.category.Category> projectionLinkedCategoryList = StreamSupport
				.stream(projectionEntities.spliterator(), false)
				.map(projection -> categoryConverter.convert(projection))
				.collect(Collectors.toList());

		assertThat(projectionEntities).hasSize(2);
		assertThat(projectionLinkedCategoryList.get(0).getPath()).isEqualTo(projectionLinkedCategoryList.get(1).getPath());
		assertThat(projectionLinkedCategoryList.get(0).getParent()).isEqualTo(projectionLinkedCategoryList.get(1).getParent());
		assertThat(projectionLinkedCategoryList.get(0).getChildren()).isEqualTo(projectionLinkedCategoryList.get(1).getChildren());
		assertThat(projectionLinkedCategoryList.get(0).getProperties().get(0).getValue())
				.isEqualTo(projectionLinkedCategoryList.get(1).getProperties().get(0).getValue());
		assertThat(projectionLinkedCategoryList.get(0).getTranslations().get(0).getDisplayName()).isEqualTo("NewName");
	}

	@Test
	public void testThatIncludedLinkedCategoryShouldBeTombstonedIfMasterCategoryWas() throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCatalogEvents = 2;

		final Store store1 = createAndPersistStore();
		final Catalog catalog1 = store1.getCatalog();

		final Catalog virtualCatalog = createAndPersistVirtualCatalog("virtualCatalog", "virtualCatalog");
		final Store vStore = createAndPersistStore(virtualCatalog);

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog1);
		final Category masterCategory = createAndPersistCategory(CATEGORY_GUID, CATEGORY_CODE, categoryType, catalog1, false);
		final Category linkedCategory = createAndPersistLinkedCategory(LINKED_CATEGORY_GUID, masterCategory, virtualCatalog);

		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, masterCategory.getCode(), store1.getCode()));
		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, masterCategory.getCode(), vStore.getCode()));

		final com.elasticpath.catalog.entity.category.Category masterCategoryProjectionBeforeDelete = findCategoryProjectionByStoreAndCode(store1,
				masterCategory);
		final com.elasticpath.catalog.entity.category.Category linkedCategoryProjectionBeforeDelete = findCategoryProjectionByStoreAndCode(vStore,
				linkedCategory);

		final NotifyBuilder catalogNotifyBuilderForUpdateCategories = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		masterCategory.setHidden(true);
		doInTransaction(transactionStatus -> processUpdatedCategory(masterCategory, transactionStatus));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderForUpdateCategories::matches);

		final com.elasticpath.catalog.entity.category.Category masterCategoryProjectionAfterDelete = findCategoryProjectionByStoreAndCode(store1,
				masterCategory);
		final com.elasticpath.catalog.entity.category.Category linkedCategoryProjectionAfterDelete = findCategoryProjectionByStoreAndCode(vStore,
				linkedCategory);

		assertThat(masterCategoryProjectionBeforeDelete.isDeleted()).isEqualTo(linkedCategoryProjectionBeforeDelete.isDeleted());
		assertThat(linkedCategoryProjectionBeforeDelete.isDeleted()).isEqualTo(false);

		assertThat(masterCategoryProjectionAfterDelete.isDeleted()).isEqualTo(linkedCategoryProjectionAfterDelete.isDeleted());
		assertThat(linkedCategoryProjectionAfterDelete.isDeleted()).isEqualTo(true);
	}

	@Test
	public void testThatExcludedLinkedCategoryShouldBeTombstonedIfMasterCategoryWasnot() throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCatalogEvents = 1;

		final Store store1 = createAndPersistStore();
		final Catalog catalog1 = store1.getCatalog();

		final Catalog virtualCatalog = createAndPersistVirtualCatalog("virtualCatalog", "virtualCatalog");
		final Store vStore = createAndPersistStore(virtualCatalog);

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog1);
		final Category masterCategory = createAndPersistCategory(CATEGORY_GUID, CATEGORY_CODE, categoryType, catalog1, true);
		final Category linkedCategory = createAndPersistExcludedLinkedCategory(LINKED_CATEGORY_GUID, masterCategory, virtualCatalog);

		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, masterCategory.getCode(), store1.getCode()));
		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, masterCategory.getCode(), vStore.getCode()));

		final com.elasticpath.catalog.entity.category.Category masterCategoryProjectionBeforeDelete = findCategoryProjectionByStoreAndCode(store1,
				masterCategory);
		final com.elasticpath.catalog.entity.category.Category linkedCategoryProjectionBeforeDelete = findCategoryProjectionByStoreAndCode(vStore,
				linkedCategory);

		final NotifyBuilder catalogNotifyBuilderForUpdateCategories = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		masterCategory.setHidden(false);
		doInTransaction(transactionStatus -> processUpdatedCategory(masterCategory, transactionStatus));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderForUpdateCategories::matches);

		final com.elasticpath.catalog.entity.category.Category masterCategoryProjectionAfterDelete = findCategoryProjectionByStoreAndCode(store1,
				masterCategory);
		final com.elasticpath.catalog.entity.category.Category linkedCategoryProjectionAfterDelete = findCategoryProjectionByStoreAndCode(vStore,
				linkedCategory);

		assertThat(masterCategoryProjectionBeforeDelete.isDeleted()).isEqualTo(true);
		assertThat(linkedCategoryProjectionBeforeDelete.isDeleted()).isEqualTo(true);

		assertThat(masterCategoryProjectionAfterDelete.isDeleted()).isEqualTo(false);
		assertThat(linkedCategoryProjectionAfterDelete.isDeleted()).isEqualTo(true);
	}

	@Test
	public void testThatIncludedLinkedCategoryShouldBeNotTombstonedIfMasterCategoryWasnot() throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCatalogEvents = 2;

		final Store store1 = createAndPersistStore();
		final Catalog catalog1 = store1.getCatalog();

		final Catalog virtualCatalog = createAndPersistVirtualCatalog("virtualCatalog", "virtualCatalog");
		final Store vStore = createAndPersistStore(virtualCatalog);

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog1);
		final Category masterCategory = createAndPersistCategory(CATEGORY_GUID, CATEGORY_CODE, categoryType, catalog1, true);
		final Category linkedCategory = createAndPersistLinkedCategory(LINKED_CATEGORY_GUID, masterCategory, virtualCatalog);

		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, masterCategory.getCode(), store1.getCode()));
		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, masterCategory.getCode(), vStore.getCode()));

		final com.elasticpath.catalog.entity.category.Category masterCategoryProjectionBeforeDelete = findCategoryProjectionByStoreAndCode(store1,
				masterCategory);
		final com.elasticpath.catalog.entity.category.Category linkedCategoryProjectionBeforeDelete = findCategoryProjectionByStoreAndCode(vStore,
				linkedCategory);

		final NotifyBuilder catalogNotifyBuilderForUpdateCategories = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		masterCategory.setHidden(false);
		doInTransaction(transactionStatus -> processUpdatedCategory(masterCategory, transactionStatus));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderForUpdateCategories::matches);

		final com.elasticpath.catalog.entity.category.Category masterCategoryProjectionAfterDelete = findCategoryProjectionByStoreAndCode(store1,
				masterCategory);
		final com.elasticpath.catalog.entity.category.Category linkedCategoryProjectionAfterDelete = findCategoryProjectionByStoreAndCode(vStore,
				linkedCategory);

		assertThat(masterCategoryProjectionBeforeDelete.isDeleted()).isEqualTo(linkedCategoryProjectionBeforeDelete.isDeleted());
		assertThat(linkedCategoryProjectionBeforeDelete.isDeleted()).isEqualTo(true);

		assertThat(masterCategoryProjectionAfterDelete.isDeleted()).isEqualTo(linkedCategoryProjectionAfterDelete.isDeleted());
		assertThat(linkedCategoryProjectionAfterDelete.isDeleted()).isEqualTo(false);
	}

	@Test
	public void testThatExcludedLinkedCategoryShouldBeTombstonedIfMasterCategoryWas() throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCatalogEvents = 1;

		final Store store1 = createAndPersistStore();
		final Catalog catalog1 = store1.getCatalog();

		final Catalog virtualCatalog = createAndPersistVirtualCatalog("virtualCatalog", "virtualCatalog");
		final Store vStore = createAndPersistStore(virtualCatalog);

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog1);
		final Category masterCategory = createAndPersistCategory(CATEGORY_GUID, CATEGORY_CODE, categoryType, catalog1, false);
		final Category linkedCategory = createAndPersistExcludedLinkedCategory(LINKED_CATEGORY_GUID, masterCategory, virtualCatalog);

		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, masterCategory.getCode(), store1.getCode()));
		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, masterCategory.getCode(), vStore.getCode()));

		final com.elasticpath.catalog.entity.category.Category masterCategoryProjectionBeforeDelete = findCategoryProjectionByStoreAndCode(store1,
				masterCategory);
		final com.elasticpath.catalog.entity.category.Category linkedCategoryProjectionBeforeDelete = findCategoryProjectionByStoreAndCode(vStore,
				linkedCategory);

		final NotifyBuilder catalogNotifyBuilderForUpdateCategories = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		masterCategory.setHidden(true);
		doInTransaction(transactionStatus -> processUpdatedCategory(masterCategory, transactionStatus));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderForUpdateCategories::matches);

		final com.elasticpath.catalog.entity.category.Category masterCategoryProjectionAfterDelete = findCategoryProjectionByStoreAndCode(store1,
				masterCategory);
		final com.elasticpath.catalog.entity.category.Category linkedCategoryProjectionAfterDelete = findCategoryProjectionByStoreAndCode(vStore,
				linkedCategory);

		assertThat(masterCategoryProjectionBeforeDelete.isDeleted()).isEqualTo(false);
		assertThat(linkedCategoryProjectionBeforeDelete.isDeleted()).isEqualTo(true);

		assertThat(masterCategoryProjectionAfterDelete.isDeleted()).isEqualTo(true);
		assertThat(linkedCategoryProjectionAfterDelete.isDeleted()).isEqualTo(true);
	}

	@Test
	public void testThatDisableDateUpdatesAfterUpdatingProjection()
			throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCatalogEvents = 1;
		final Store store1 = createAndPersistStore();
		final Catalog catalog1 = store1.getCatalog();

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog1);
		final Category category = createAndPersistCategory(CATEGORY_GUID, CATEGORY_CODE, categoryType, catalog1, false, null);
		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(CATEGORY_IDENTITY_TYPE, category.getCode(), store1.getCode()));

		final com.elasticpath.catalog.entity.category.Category createdProjection = findCategoryProjectionByStoreAndCode(store1, category);
		assertThat(createdProjection).extracting(Projection::getDisableDateTime).isNull();

		final NotifyBuilder catalogNotifyBuilderForUpdateCategory = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		category.setEndDate(new Date(System.currentTimeMillis() + ONE_MINUTE));
		updateAndPersistCategory(category);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderForUpdateCategory::matches);

		final com.elasticpath.catalog.entity.category.Category updatedProjection = findCategoryProjectionByStoreAndCode(store1, category);
		assertThat(updatedProjection).extracting(Projection::getDisableDateTime).isNotNull();
	}

	@Test
	public void testThatLinkedChildrenCategoryUpdatedIfParentWasExcludedOrIncluded() throws DefaultValueRemovalForbiddenException {
		final int expectedNumberOfCategoryCreatedEvents = 4;
		final int expectedNumberOfCategoryUpdatedEventsAfterExclude = 3;
		final int expectedNumberOfCategoryUpdatedEventsAfterInclude = 1;

		final Catalog catalog = createCatalog();
		createAndPersistStore(catalog);
		final Catalog virtualCatalog = createAndPersistVirtualCatalog("virtualCatalog", "virtualCatalog");
		final Store vStore = createAndPersistStore(virtualCatalog);
		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE_GUID, CATEGORY_TYPE, catalog);

		final NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCategoryCreatedEvents).create();

		final Category rootCategory = createAndPersistCategory("rootGuid", "rootCategory", categoryType, catalog, false);
		final Category subCategory = createAndPersistCategory("sub1Guid", "subCategory1", categoryType, catalog, false, rootCategory);
		final Category rootLinkedCategory = createAndPersistLinkedCategory("linkedGuid", rootCategory, virtualCatalog);
		final Category linkedSubCategory = createAndPersistLinkedCategoryWithParent("linkedSubGuid1", subCategory, virtualCatalog,
				rootLinkedCategory);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);

		final Iterable<ProjectionEntity> projectionEntities = catalogProjectionRepository.findAll();
		final List<com.elasticpath.catalog.entity.category.Category> categoriesBeforeExclude =
				Lists.newArrayList(projectionEntities).stream().map(categoryConverter::convert).collect(Collectors.toList());

		final NotifyBuilder catalogNotifyBuilderAfterUpdate = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCategoryUpdatedEventsAfterExclude)
				.create();

		rootLinkedCategory.setIncluded(false);
		doInTransaction(status -> saveOrUpdate(rootLinkedCategory));
		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderAfterUpdate::matches);

		final Iterable<ProjectionEntity> updatedProjectionEntities = catalogProjectionRepository.findAll();
		final List<com.elasticpath.catalog.entity.category.Category> categoriesAfterExclude =
				Lists.newArrayList(updatedProjectionEntities).stream().map(categoryConverter::convert).collect(Collectors.toList());

		final NotifyBuilder catalogNotifyBuilderAfterInclude = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCategoryUpdatedEventsAfterInclude)
				.create();

		rootLinkedCategory.setIncluded(true);
		doInTransaction(status -> saveOrUpdate(rootLinkedCategory));
		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderAfterInclude::matches);

		final Iterable<ProjectionEntity> projectionEntitiesAfterInclude = catalogProjectionRepository.findAll();

		final List<com.elasticpath.catalog.entity.category.Category> categoriesAfterInclude =
				Lists.newArrayList(projectionEntitiesAfterInclude).stream().map(categoryConverter::convert).collect(Collectors.toList());

		assertThat(categoriesBeforeExclude)
				.filteredOn(category -> category.getIdentity().getStore().equals(vStore.getCode()))
				.filteredOn(category -> category.getIdentity().getCode().equals(linkedSubCategory.getCode()))
				.extracting(AbstractProjection::isDeleted)
				.containsExactly(false);

		assertThat(categoriesAfterExclude)
				.filteredOn(category -> category.getIdentity().getStore().equals(vStore.getCode()))
				.filteredOn(category -> category.getIdentity().getCode().equals(linkedSubCategory.getCode()))
				.extracting(AbstractProjection::isDeleted)
				.containsExactly(true);

		assertThat(categoriesAfterInclude)
				.filteredOn(category -> category.getIdentity().getStore().equals(vStore.getCode()))
				.filteredOn(category -> category.getIdentity().getCode().equals(linkedSubCategory.getCode()))
				.extracting(AbstractProjection::isDeleted)
				.containsExactly(false);
	}

	private com.elasticpath.catalog.entity.category.Category findCategoryProjectionByStoreAndCode(final Store store, final Category category) {
		final Iterable<ProjectionEntity> projectionEntitiesBeforeDelete = catalogProjectionRepository.findAll();

		final List<com.elasticpath.catalog.entity.category.Category> categoryList =
				Lists.newArrayList(projectionEntitiesBeforeDelete).stream().map(categoryConverter::convert).collect(Collectors.toList());

		return categoryList.stream()
				.filter(categoryProjection -> categoryProjection.getIdentity().getStore().equals(store.getCode()))
				.filter(categoryProjection -> categoryProjection.getIdentity().getCode().equals(category.getCode()))
				.findFirst()
				.orElse(null);
	}

	private Category createAndPersistChildCategory(final String guid, final String code, final CategoryType categoryType, final Catalog catalog,
												   final boolean hidden) {
		final Category category = createCategory(guid, code, categoryType, catalog, hidden, null, 0);
		category.setParentGuid(CATEGORY_GUID);
		doInTransaction(status -> persist(category));

		return category;
	}

	private TransactionStatus processUpdatedCategory(final Category updatedCategory, final TransactionStatus transactionStatus) {
		updateAndPersistCategory(updatedCategory);
		categoryUpdateProcessor.processCategoryUpdated(updateAndPersistCategory(updatedCategory));
		return transactionStatus;
	}

	private Store createAndPersistStore() throws DefaultValueRemovalForbiddenException {
		final Store store = createStore();
		store.setSupportedLocales(Collections.singletonList(Locale.getDefault()));

		return doInTransaction(status -> persist(store));
	}

	private Store createAndPersistStore(final Catalog catalog) throws DefaultValueRemovalForbiddenException {
		final Store store = createStore();
		store.setCatalog(catalog);
		store.setSupportedLocales(Collections.singletonList(catalog.getDefaultLocale()));

		return doInTransaction(status -> persist(store));
	}

	private CategoryType createAndPersistCategoryType(final String guid, final String name, final Catalog catalog) {
		final CategoryType categoryType = createCategoryType(guid, name, catalog);
		doInTransaction(status -> persist(categoryType));

		return categoryType;
	}

	private CategoryType createCategoryType(final String guid, final String name, final Catalog catalog) {
		final CategoryTypeImpl categoryType = new CategoryTypeImpl();
		categoryType.setGuid(guid);
		categoryType.setName(name);
		categoryType.setCatalog(catalog);
		categoryType.setCategoryAttributeGroupAttributes(Collections.emptySet());

		return categoryType;
	}

	private Category createAndPersistCategory(final String guid, final String code, final CategoryType categoryType, final Catalog catalog,
											  final boolean hidden, final Category parent) {
		final Category category = createCategory(guid, code, categoryType, catalog, hidden, parent, 0);
		doInTransaction(status -> persist(category));

		return category;
	}

	private Category updateAndPersistCategory(final Category category) {
		doInTransaction(status -> saveOrUpdate(category));
		return category;
	}

	private Category createAndPersistCategory(final String guid, final String code, final CategoryType categoryType, final Catalog catalog,
											  final boolean hidden) {
		final Category parent = null;
		final int ordering = 0;
		final Category category = createCategory(guid, code, categoryType, catalog, hidden, parent, ordering);
		doInTransaction(status -> persist(category));

		return category;
	}

	private Category createCategory(final String guid, final String code, final CategoryType categoryType, final Catalog catalog,
									final boolean hidden, final Category parent, final int ordering) {
		final Category category = new CategoryImpl();
		category.setGuid(guid);
		category.setCode(code);
		category.setDisplayName(CATEGORY_DISPLAY_NAME, Locale.ENGLISH);
		category.setCategoryType(categoryType);
		category.setCatalog(catalog);
		category.setStartDate(new Date());
		category.setHidden(hidden);
		category.setParent(parent);
		category.setAttributeValueMap(Collections.emptyMap());
		category.setOrdering(ordering);
		category.initialize();

		return category;
	}

	private Category createAndPersistLinkedCategory(final String guid, final Category masterCategory, final Catalog catalog) {
		final Category linkedCategory = createLinkedCategory(guid, masterCategory, catalog);
		doInTransaction(status -> persist(linkedCategory));

		return linkedCategory;
	}

	private Category createLinkedCategory(final String guid, final Category masterCategory, final Catalog catalog) {
		final LinkedCategoryImpl linkedCategory = new LinkedCategoryImpl();
		linkedCategory.setGuid(guid);
		linkedCategory.setMasterCategory(masterCategory);
		linkedCategory.setCatalog(catalog);
		linkedCategory.setIncluded(true);

		return linkedCategory;
	}

	private Category createAndPersistExcludedLinkedCategory(final String guid, final Category masterCategory, final Catalog catalog) {
		final Category linkedCategory = createExcludedLinkedCategory(guid, masterCategory, catalog);
		doInTransaction(status -> persist(linkedCategory));

		return linkedCategory;
	}

	private Category createExcludedLinkedCategory(final String guid, final Category masterCategory, final Catalog catalog) {
		final LinkedCategoryImpl linkedCategory = new LinkedCategoryImpl();
		linkedCategory.setGuid(guid);
		linkedCategory.setMasterCategory(masterCategory);
		linkedCategory.setCatalog(catalog);
		linkedCategory.setIncluded(false);

		return linkedCategory;
	}


	private Category createAndPersistLinkedCategoryWithParent(final String guid, final Category masterCategory,
															  final Catalog catalog, final Category parentCategory) {
		final Category linkedCategory = createLinkedCategory(guid, masterCategory, catalog);
		linkedCategory.setParent(parentCategory);

		return doInTransaction(status -> persist(linkedCategory));
	}

	private Catalog createAndPersistVirtualCatalog(final String name, final String code) {
		final Catalog virtualCatalog = new CatalogImpl();
		virtualCatalog.setName(name);
		virtualCatalog.setMaster(false);
		virtualCatalog.setDefaultLocale(Locale.getDefault());
		virtualCatalog.setCode(code);

		return doInTransaction(status -> persist(virtualCatalog));
	}

	private <T extends Persistable> T persist(final T entity) {
		getPersistenceEngine().saveOrUpdate(entity);
		return entity;
	}

	private <T extends Persistable> T saveOrUpdate(final T entity) {
		getPersistenceEngine().saveOrUpdate(entity);
		return entity;
	}

	private <T extends Persistable> T delete(final T entity) {
		getPersistenceEngine().delete(entity);
		return entity;
	}

	private boolean isProjectionExists(final String type, final String code, final String store) {
		return catalogProjectionRepository.extractProjectionEntity(type, code, store).isPresent();
	}

}
