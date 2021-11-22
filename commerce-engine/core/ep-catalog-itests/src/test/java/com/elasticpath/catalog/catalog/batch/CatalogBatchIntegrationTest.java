/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.catalog.batch;

import static com.elasticpath.catalog.batch.CatalogJobRunnerImpl.CLEAN_UP_DATABASE_FLAG;
import static com.elasticpath.catalog.batch.message.CatalogBatchEventType.START_JOB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import javax.persistence.EntityNotFoundException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.RetryException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;

import com.elasticpath.catalog.batch.message.CatalogBatchEventMessageProcessor;
import com.elasticpath.catalog.exception.ValidationException;
import com.elasticpath.catalog.messages.RelayOutboxMessagesThreadExecutor;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionHistoryId;
import com.elasticpath.catalog.plugin.entity.ProjectionId;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionHistoryRepository;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionRepository;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.CategoryType;
import com.elasticpath.domain.catalog.DefaultValueRemovalForbiddenException;
import com.elasticpath.domain.catalog.impl.CategoryImpl;
import com.elasticpath.domain.catalog.impl.CategoryTypeImpl;
import com.elasticpath.domain.catalog.impl.LinkedCategoryImpl;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.store.Store;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.impl.EventMessageImpl;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.util.Utils;

/**
 * Integration tests for Catalog Batch functionality.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DirtiesDatabase
public class CatalogBatchIntegrationTest extends DbTestCase {

	private static final Logger LOGGER = LogManager.getLogger(CatalogBatchIntegrationTest.class);

	private static final String OPTION = "option";
	private static final String STORE = "store";
	private static final String CODE = "code";
	private static final String HASH_CODE = "hash";
	private static final String BUILD_ALL_OPTIONS = "BUILD_ALL_OPTIONS";
	private static final int TEST_JOB_EXECUTION_TIMEOUT = 5;
	private static final String RETRY_FAILING_JOB = "RETRY_FAILING_JOB";
	private static final String RETRY_SUCCEEDING_JOB = "RETRY_SUCCEEDING_JOB";
	private static final String FAILING_JOB_RUNTIME_EXCEPTION = "FAILING_JOB_RUNTIME_EXCEPTION";
	private static final String GUID = "guid";
	private static final String BUILD_ALL_CATEGORIES = "BUILD_ALL_CATEGORIES";
	private static final String CATEGORY = "category";
	private static final String CATEGORY_TYPE = "categoryType";
	private static final String CATEGORY_DISPLAY_NAME = "categoryName";

	private static final String CATALOG_MESSAGING_CAMEL_CONTEXT = "ep-catalog-messaging";
	private static final String JMS_CATALOG_ENDPOINT = "jms:topic:VirtualTopic.ep.catalog";

	@Autowired
	private CatalogBatchEventMessageProcessor catalogBatchEventMessageProcessor;

	@Autowired
	@Qualifier("ep-catalog-batch-messaging")
	private CamelContext camelContext;

	@Autowired
	private CatalogProjectionRepository projectionRepository;

	@Autowired
	private CatalogProjectionHistoryRepository projectionHistoryRepository;

	@Autowired
	private RelayOutboxMessagesThreadExecutor relayOutboxMessagesThreadExecutor;

	@Autowired
	@Qualifier("spyTransactionManager")
	private SpyTransactionManager spyTransactionManager;

	@Autowired
	@Qualifier("catalogJobLauncherTaskExecutor")
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Autowired
	@Qualifier(CATALOG_MESSAGING_CAMEL_CONTEXT)
	private CamelContext catalogCamelContext;

	@Before
	public void setUp() throws Exception {
		threadPoolTaskExecutor.setAwaitTerminationSeconds(TEST_JOB_EXECUTION_TIMEOUT);
		threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		threadPoolTaskExecutor.afterPropertiesSet();
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
	public void testProjectionAndProjectionHistoryRepositoryShouldBeEmptyWhenBuildAllOptionsMessageReceivedAndCleanUpDatabaseIsTrue()
			throws Exception {
		final String skuOptionCode = Utils.uniqueCode(CODE);
		final String storeCode = Utils.uniqueCode(STORE);
		final ProjectionEntity projectionEntity = persistProjectionEntity(OPTION, storeCode, skuOptionCode);
		persistProjectionHistoryEntity(projectionEntity);

		final Exchange exchange = createExchange(START_JOB, BUILD_ALL_OPTIONS, Collections.singletonMap(CLEAN_UP_DATABASE_FLAG, true));
		catalogBatchEventMessageProcessor.process(exchange);

		threadPoolTaskExecutor.shutdown();

		assertThat(projectionRepository.findAll()).isEmpty();
		assertThat(projectionHistoryRepository.findAll()).isEmpty();
	}

	@Test
	public void projectionAndProjectionHistoryRepositoryShouldNotBeEmptyWhenBuildAllOptionsMessageReceivedAndCleanUpDatabaseIsFalse()
			throws Exception {
		final String skuOptionCode = Utils.uniqueCode(CODE);
		final String storeCode = Utils.uniqueCode(STORE);
		final ProjectionEntity projectionEntity = persistProjectionEntity(OPTION, storeCode, skuOptionCode);
		final ProjectionHistoryEntity projectionHistoryEntity = persistProjectionHistoryEntity(projectionEntity);

		final Exchange exchange = createExchange(START_JOB, BUILD_ALL_OPTIONS, Collections.singletonMap(CLEAN_UP_DATABASE_FLAG, false));

		catalogBatchEventMessageProcessor.process(exchange);

		threadPoolTaskExecutor.shutdown();

		final Iterable<ProjectionEntity> projectionEntities = projectionRepository.findAll();
		assertThat(projectionEntities).containsExactly(projectionEntity);
		assertThat(projectionHistoryRepository.findAll()).containsExactly(projectionHistoryEntity);
	}

	@Test
	public void projectionRepositoryShouldContainsOneProjectionWhenBuildAllOptionsMessageReceivedAndCleanUpDatabaseIsTrue()
			throws Exception {
		final int expectedNumberOfCatalogEvents = 1;

		final Catalog catalog = createCatalog("Test Catalog", Locale.ENGLISH, Currency.getInstance(Locale.CANADA));

		final String storeCode = Utils.uniqueCode(STORE);
		persistStore(catalog, storeCode);

		final String skuOptionCode = Utils.uniqueCode(CODE);
		persistSkuOption(catalog, skuOptionCode);

		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(OPTION, skuOptionCode, storeCode));

		final Exchange exchange = createExchange(START_JOB, BUILD_ALL_OPTIONS, Collections.singletonMap(CLEAN_UP_DATABASE_FLAG, true));

		final NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		catalogBatchEventMessageProcessor.process(exchange);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);
		threadPoolTaskExecutor.shutdown();

		final Iterable<ProjectionEntity> projectionEntities = projectionRepository.findAll();
		assertThat(projectionEntities).hasSize(1);
		assertThat(projectionEntities).extracting(ProjectionEntity::getType).containsOnly(OPTION);
		assertThat(projectionEntities).extracting(ProjectionEntity::getStore).containsOnly(storeCode);
		assertThat(projectionEntities).extracting(ProjectionEntity::getCode).containsOnly(skuOptionCode);
		assertThat(projectionHistoryRepository.findAll()).isEmpty();
	}

	@Test
	public void projectionEntityAndProjectionHistoryEntityShouldBeStoredInRepositoriesWhenBuildAllOptionsMessageReceivedAndCleanUpDatabaseIsFalse()
			throws Exception {
		final Catalog catalog = createCatalog("Test Catalog", Locale.ENGLISH, Currency.getInstance(Locale.CANADA));

		final String storeCode = Utils.uniqueCode(STORE);
		persistStore(catalog, storeCode);

		final String skuOptionCode = Utils.uniqueCode(CODE);
		persistSkuOption(catalog, skuOptionCode);

		await().atMost(TEN_SECONDS).until(() -> isProjectionExists(OPTION, skuOptionCode, storeCode));

		final ProjectionEntity projectionEntity = projectionRepository.extractProjectionEntity(OPTION, skuOptionCode, storeCode)
				.orElseThrow(EntityNotFoundException::new);
		persistProjectionHistoryEntity(projectionEntity);

		final Exchange exchange = createExchange(START_JOB, BUILD_ALL_OPTIONS, Collections.singletonMap(CLEAN_UP_DATABASE_FLAG, false));

		catalogBatchEventMessageProcessor.process(exchange);

		threadPoolTaskExecutor.shutdown();

		final Iterable<ProjectionEntity> projectionEntities = projectionRepository.findAll();
		assertThat(projectionEntities).hasSize(1);
		assertThat(projectionEntities).extracting(ProjectionEntity::getType).containsOnly(OPTION);
		assertThat(projectionEntities).extracting(ProjectionEntity::getStore).containsOnly(storeCode);
		assertThat(projectionEntities).extracting(ProjectionEntity::getCode).containsOnly(skuOptionCode);

		final Iterable<ProjectionHistoryEntity> projectionHistoryEntities = projectionHistoryRepository.findAll();
		assertThat(projectionHistoryEntities).hasSize(1);
		assertThat(projectionHistoryEntities).extracting(ProjectionHistoryEntity::getType).containsOnly(OPTION);
		assertThat(projectionHistoryEntities).extracting(ProjectionHistoryEntity::getStore).containsOnly(storeCode);
		assertThat(projectionHistoryEntities).extracting(ProjectionHistoryEntity::getCode).containsOnly(skuOptionCode);
	}

	@Test
	public void projectionRepositoryShouldContainsSixProjectionsWhenBuildAllCategoriesMessageReceivedAndCleanUpDatabaseIsTrue() throws Exception {
		final int expectedNumberOfCatalogEventsCategoryCreated = 6;
		final int expectedNumberOfEventsAfterBatchProcessing = 2;

		final int sixProjections = 6;
		final int twoProjections = 2;

		final String store1 = Utils.uniqueCode(STORE);
		final Catalog catalog1 = createCatalog();
		persistStore(catalog1, store1);

		final String store2 = Utils.uniqueCode(STORE);
		final Catalog catalog2 = createCatalog();
		persistStore(catalog2, store2);

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE, catalog1);

		final NotifyBuilder categoryCreatedCatalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEventsCategoryCreated).create();

		final String categoryCode1 = Utils.uniqueCode(CODE);
		final Category category1 = createAndPersistCategory(categoryCode1, categoryType, catalog1, false);
		final String categoryCode2 = Utils.uniqueCode(CODE);
		final Category category2 = createAndPersistCategory(categoryCode2, categoryType, catalog1, false, category1);
		final String categoryCode3 = Utils.uniqueCode(CODE);
		final Category category3 = createAndPersistCategory(categoryCode3, categoryType, catalog1, false, category1);

		createAndPersistLinkedCategory(category1, catalog2);
		createAndPersistLinkedCategory(category2, catalog2);
		createAndPersistLinkedCategory(category3, catalog2);

		await().atMost(TEN_SECONDS).until(categoryCreatedCatalogNotifyBuilder::matches);

		final Exchange exchange = createExchange(START_JOB, BUILD_ALL_CATEGORIES, Collections.singletonMap(CLEAN_UP_DATABASE_FLAG, true));

		final NotifyBuilder batchProcessingNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfEventsAfterBatchProcessing).create();

		catalogBatchEventMessageProcessor.process(exchange);

		await().atMost(TEN_SECONDS).until(batchProcessingNotifyBuilder::matches);

		threadPoolTaskExecutor.shutdown();

		final Iterable<ProjectionEntity> projectionEntities = projectionRepository.findAll();

		assertThat(projectionEntities).hasSize(sixProjections);
		assertThat(projectionEntities).extracting(ProjectionEntity::getType).containsOnly(CATEGORY);
		assertThat(projectionEntities).extracting(ProjectionEntity::getStore).containsOnly(store1, store2);
		assertThat(projectionEntities).extracting(ProjectionEntity::getCode).filteredOn(Predicate.isEqual(categoryCode1)).hasSize(twoProjections);
		assertThat(projectionEntities).extracting(ProjectionEntity::getCode).filteredOn(Predicate.isEqual(categoryCode2)).hasSize(twoProjections);
		assertThat(projectionEntities).extracting(ProjectionEntity::getCode).filteredOn(Predicate.isEqual(categoryCode3)).hasSize(twoProjections);

		assertThat(projectionHistoryRepository.findAll()).isEmpty();
	}

	@Test
	public void projectionRepositoryShouldContainsSixProjectionsWhenBuildAllCategoriesMessageReceivedAndCleanUpDatabaseIsFalse() throws Exception {
		final int expectedNumberOfCatalogEventsCategoryCreated = 6;

		final int sixProjections = 6;
		final int twoProjections = 2;

		final String store1 = Utils.uniqueCode(STORE);
		final Catalog catalog1 = createCatalog();
		persistStore(catalog1, store1);

		final String store2 = Utils.uniqueCode(STORE);
		final Catalog catalog2 = createCatalog();
		persistStore(catalog2, store2);

		final CategoryType categoryType = createAndPersistCategoryType(CATEGORY_TYPE, catalog1);

		final NotifyBuilder categoryCreatedCatalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEventsCategoryCreated).create();

		final String categoryCode1 = Utils.uniqueCode(CODE);
		final Category category1 = createAndPersistCategory(categoryCode1, categoryType, catalog1, false);
		final String categoryCode2 = Utils.uniqueCode(CODE);
		final Category category2 = createAndPersistCategory(categoryCode2, categoryType, catalog1, false, category1);
		final String categoryCode3 = Utils.uniqueCode(CODE);
		final Category category3 = createAndPersistCategory(categoryCode3, categoryType, catalog1, false, category1);

		createAndPersistLinkedCategory(category1, catalog2);
		createAndPersistLinkedCategory(category2, catalog2);
		createAndPersistLinkedCategory(category3, catalog2);

		await().atMost(TEN_SECONDS).until(categoryCreatedCatalogNotifyBuilder::matches);

		final Exchange exchange = createExchange(START_JOB, BUILD_ALL_CATEGORIES, Collections.singletonMap(CLEAN_UP_DATABASE_FLAG, false));

		catalogBatchEventMessageProcessor.process(exchange);

		threadPoolTaskExecutor.shutdown();

		final Iterable<ProjectionEntity> projectionEntities = projectionRepository.findAll();

		assertThat(projectionEntities).hasSize(sixProjections);
		assertThat(projectionEntities).extracting(ProjectionEntity::getType).containsOnly(CATEGORY);
		assertThat(projectionEntities).extracting(ProjectionEntity::getStore).containsOnly(store1, store2);
		assertThat(projectionEntities).extracting(ProjectionEntity::getCode).filteredOn(Predicate.isEqual(categoryCode1)).hasSize(twoProjections);
		assertThat(projectionEntities).extracting(ProjectionEntity::getCode).filteredOn(Predicate.isEqual(categoryCode2)).hasSize(twoProjections);
		assertThat(projectionEntities).extracting(ProjectionEntity::getCode).filteredOn(Predicate.isEqual(categoryCode3)).hasSize(twoProjections);
	}

	@Test
	public void testRetryAndSuccess() throws Exception {
		final Exchange exchange = createExchange(START_JOB, RETRY_SUCCEEDING_JOB, Collections.emptyMap());
		spyTransactionManager.reset();
		catalogBatchEventMessageProcessor.process(exchange);
		final JobExecution execution = (JobExecution) exchange.getProperty("execution");
		while (isExecuting(execution)) {
			Thread.yield();
		}
		assertThat(Files.readAllLines(Paths.get("target", "RETRY_SUCCEEDING_JOB_FILE_WRITER_batch_committed.txt"))).containsExactly(
				"data frame number 1",
				"data frame number 2",
				"data frame number 3",
				"data frame number 4",
				"data frame number 5",
				"data frame number 6",
				"data frame number 7",
				"data frame number 8",
				"data frame number 9",
				"data frame number 10");
		assertThat(execution.getAllFailureExceptions()).isEmpty();
		assertThat(spyTransactionManager.isCommitted()).isTrue();
		assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		threadPoolTaskExecutor.shutdown();
	}

	@Test
	public void testRetryAndFailureWithDataAccessException() throws Exception {
		final Exchange exchange = createExchange(START_JOB, RETRY_FAILING_JOB, Collections.emptyMap());
		spyTransactionManager.reset();
		catalogBatchEventMessageProcessor.process(exchange);
		final JobExecution execution = (JobExecution) exchange.getProperty("execution");
		while (isExecuting(execution)) {
			Thread.yield();
		}
		assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
		assertThat(Paths.get("target", "RETRY_FAILING_JOB_FILE_WRITER_batch_committed.txt")).doesNotExist();
		assertThat(execution.getAllFailureExceptions()).isNotEmpty();
		assertThat(execution.getAllFailureExceptions().get(0)).isInstanceOf(RetryException.class);
		assertThat(execution.getAllFailureExceptions().get(0)).hasCauseInstanceOf(DataAccessException.class);
		assertThat(spyTransactionManager.isRolledBack()).isTrue();
		threadPoolTaskExecutor.shutdown();
	}

	@Test
	public void testFailureAndNoRetryWithRuntimeException() throws Exception {
		final Exchange exchange = createExchange(START_JOB, FAILING_JOB_RUNTIME_EXCEPTION, Collections.emptyMap());
		spyTransactionManager.reset();
		catalogBatchEventMessageProcessor.process(exchange);
		final JobExecution execution = (JobExecution) exchange.getProperty("execution");
		while (isExecuting(execution)) {
			Thread.yield();
		}
		assertThat(execution.getAllFailureExceptions()).isNotEmpty();
		assertThat(execution.getAllFailureExceptions().get(0)).hasCauseInstanceOf(ValidationException.class);
		assertThat(Paths.get("target", "FAILING_JOB_RUNTIME_EXCEPTION_FILE_WRITER_batch_committed.txt")).doesNotExist();
		assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
		assertThat(spyTransactionManager.isRolledBack()).isTrue();
		threadPoolTaskExecutor.shutdown();
	}

	private boolean isExecuting(final JobExecution execution) {
		return Arrays.asList(BatchStatus.STARTING, BatchStatus.STARTED, BatchStatus.STOPPING).contains(execution.getStatus());
	}

	private ProjectionEntity persistProjectionEntity(final String type, final String store, final String code) {
		final ProjectionId projectionId = new ProjectionId();
		projectionId.setType(type);
		projectionId.setStore(store);
		projectionId.setCode(code);

		final ProjectionEntity projectionEntity = new ProjectionEntity();
		projectionEntity.setProjectionId(projectionId);
		projectionEntity.setProjectionDateTime(new Date());
		projectionEntity.setContentHash(Utils.uniqueCode(HASH_CODE));
		projectionEntity.setGuid(UUID.randomUUID().toString());

		projectionRepository.save(projectionEntity);

		return projectionEntity;
	}

	private ProjectionHistoryEntity persistProjectionHistoryEntity(final ProjectionEntity projectionEntity) {
		final ProjectionHistoryId projectionHistoryId = new ProjectionHistoryId();
		projectionHistoryId.setVersion(projectionEntity.getVersion());
		projectionHistoryId.setType(projectionEntity.getType());
		projectionHistoryId.setStore(projectionEntity.getStore());
		projectionHistoryId.setCode(projectionEntity.getCode());

		final ProjectionHistoryEntity projectionHistoryEntity = new ProjectionHistoryEntity();
		projectionHistoryEntity.setHistoryId(projectionHistoryId);
		projectionHistoryEntity.setProjectionDateTime(projectionEntity.getProjectionDateTime());

		projectionHistoryRepository.save(projectionHistoryEntity);

		return projectionHistoryEntity;
	}

	private Exchange createExchange(final EventType eventType, final String guid, final Map<String, Object> data) {
		final EventMessageImpl eventMessage = new EventMessageImpl(eventType, guid, data);

		final Message message = new DefaultMessage(camelContext);
		message.setBody(eventMessage);

		final DefaultExchange exchange = new DefaultExchange(camelContext);
		exchange.setIn(message);

		return exchange;
	}

	private Store persistStore(final Catalog catalog, final String storeCode) throws DefaultValueRemovalForbiddenException {
		final Store store = createStore();
		store.setName("store");
		store.setCode(storeCode);
		store.setCatalog(catalog);
		store.setDefaultLocale(Locale.ENGLISH);
		store.setSupportedLocales(Collections.singletonList(Locale.ENGLISH));

		doInTransaction(status -> persist(store));

		return store;
	}

	private SkuOption persistSkuOption(final Catalog catalog, final String name) {
		final SkuOption skuOption = getBeanFactory().getPrototypeBean(ContextIdNames.SKU_OPTION, SkuOption.class);

		skuOption.setCatalog(catalog);
		skuOption.setOptionKey(name);
		skuOption.setDisplayName(name, Locale.ENGLISH);

		doInTransaction(status -> persist(skuOption));

		return skuOption;
	}

	private CategoryType createAndPersistCategoryType(final String name, final Catalog catalog) {
		final String guid = Utils.uniqueCode(GUID);
		final CategoryType categoryType = createCategoryType(guid, name, catalog);
		doInTransaction(status -> persist(categoryType));

		return categoryType;
	}

	private CategoryType createCategoryType(final String guid, final String name, final Catalog catalog) {
		final CategoryType categoryType = new CategoryTypeImpl();
		categoryType.setGuid(guid);
		categoryType.setName(name);
		categoryType.setCatalog(catalog);

		return categoryType;
	}

	private Category createAndPersistCategory(final String code, final CategoryType categoryType, final Catalog catalog, final boolean hidden) {
		final String guid = Utils.uniqueCode(GUID);
		final Category category = createCategory(guid, code, categoryType, catalog, hidden, null);

		doInTransaction(status -> persist(category));

		return category;
	}

	private Category createAndPersistCategory(final String code, final CategoryType categoryType, final Catalog catalog, final boolean hidden,
											  final Category parent) {
		final String guid = Utils.uniqueCode(GUID);
		final Category category = createCategory(guid, code, categoryType, catalog, hidden, parent);

		doInTransaction(status -> persist(category));

		return category;
	}

	private Category createCategory(final String guid, final String code, final CategoryType categoryType, final Catalog catalog,
									final boolean hidden, final Category parent) {
		final Category category = new CategoryImpl();
		category.setGuid(guid);
		category.setCode(code);
		category.setDisplayName(CATEGORY_DISPLAY_NAME, Locale.ENGLISH);
		category.setCategoryType(categoryType);
		category.setCatalog(catalog);
		category.setStartDate(new Date());
		category.setHidden(hidden);
		category.setParent(parent);

		return category;
	}

	private Category createAndPersistLinkedCategory(final Category masterCategory, final Catalog catalog) {
		final String guid = Utils.uniqueCode(GUID);
		final Category linkedCategory = createLinkedCategory(guid, masterCategory, catalog);

		doInTransaction(status -> persist(linkedCategory));

		return linkedCategory;
	}

	private Category createLinkedCategory(final String guid, final Category masterCategory, final Catalog catalog) {
		final LinkedCategoryImpl linkedCategory = new LinkedCategoryImpl();
		linkedCategory.setGuid(guid);
		linkedCategory.setMasterCategory(masterCategory);
		linkedCategory.setCatalog(catalog);

		return linkedCategory;
	}

	private <T extends Persistable> T persist(final T entity) {
		getPersistenceEngine().save(entity);
		return entity;
	}

	private boolean isProjectionExists(final String type, final String code, final String store) {
		return projectionRepository.extractProjectionEntity(type, code, store).isPresent();
	}

}
