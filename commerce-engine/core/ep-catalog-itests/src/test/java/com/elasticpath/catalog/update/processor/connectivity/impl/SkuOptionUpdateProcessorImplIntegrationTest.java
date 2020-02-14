/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OPTION_IDENTITY_TYPE;
import static com.elasticpath.catalog.update.processor.connectivity.impl.SkuOptionUpdateProcessorImplIntegrationTest.JMS_BROKER_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;
import static org.awaitility.Duration.TWO_SECONDS;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionHistoryId;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionHistoryRepository;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionRepository;
import com.elasticpath.catalog.update.processor.capabilities.SkuOptionUpdateProcessor;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.DefaultValueRemovalForbiddenException;
import com.elasticpath.domain.misc.impl.SkuOptionLocalizedPropertyValueImpl;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.jta.JmsBrokerConfigurator;
import com.elasticpath.test.jta.XaTransactionTestSupport;
import com.elasticpath.test.util.Utils;

/**
 * Integration tests for {@link SkuOptionUpdateProcessorImpl}.
 */
@JmsBrokerConfigurator(url = JMS_BROKER_URL)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DirtiesDatabase
public class SkuOptionUpdateProcessorImplIntegrationTest extends XaTransactionTestSupport {

	public static final String JMS_BROKER_URL = "tcp://localhost:61619";

	private static final Logger LOGGER = Logger.getLogger(SkuOptionUpdateProcessorImplIntegrationTest.class);

	private static final String SKU_OPTION_KEY = "optionKey";
	private static final String SKU_DISPLAY_NAME = "displayName";
	private static final String NEW_SKU_DISPLAY_NAME = "newDisplayName";

	private static final String CATALOG_MESSAGING_CAMEL_CONTEXT = "ep-catalog-messaging";
	private static final String JMS_CATALOG_ENDPOINT = "jms:topic:VirtualTopic.ep.catalog";

	@Autowired
	private SkuOptionUpdateProcessor skuOptionUpdateProcessor;

	@Autowired
	private CatalogProjectionRepository catalogProjectionRepository;

	@Autowired
	private CatalogProjectionHistoryRepository historyRepository;

	@Autowired
	@Qualifier(CATALOG_MESSAGING_CAMEL_CONTEXT)
	private CamelContext catalogCamelContext;

	private Catalog catalog;
	private Store store;

	@Before
	public void setUp() throws Exception {
		store = createAndPersistStore();
		catalog = store.getCatalog();
		historyRepository.deleteAll();
		catalogProjectionRepository.deleteAll();
		catalogCamelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(JMS_CATALOG_ENDPOINT)
						.process(exchange -> LOGGER.info("Catalog endpoint exchange: " + exchange.getIn().getBody()));
			}
		});
	}

	@Test
	public void shouldAppendedProjectionWhenEventIsCreatedAndNoCorrespondingProjectionExists() {
		final int expectedNumberOfCatalogEventsBeforeProductUpdate = 1;

		final NotifyBuilder catalogNotifyBuilderBeforeProductUpdate = new NotifyBuilder(catalogCamelContext).from(JMS_CATALOG_ENDPOINT)
				.whenExactlyCompleted(expectedNumberOfCatalogEventsBeforeProductUpdate).create();

		final SkuOption skuOption = createAndPersistSkuOption();

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderBeforeProductUpdate::matches);

		final List<ProjectionEntity> projections = catalogProjectionRepository.extractProjectionsByTypeAndCode(OPTION_IDENTITY_TYPE,
				skuOption.getOptionKey());

		assertThat(projections).hasSize(1);
	}

	@Test
	public void shouldAppendedProjectionWhenEventIsUpdatedAndNoCorrespondingProjectionExists() {
		final SkuOption skuOption = createAndPersistSkuOption();

		await().atMost(TWO_SECONDS).until(() -> isProjectionExists(OPTION_IDENTITY_TYPE, skuOption.getOptionKey(), store.getCode()));

		catalogProjectionRepository.deleteAll();

		skuOptionUpdateProcessor.processSkuOptionUpdated(skuOption);
		final List<ProjectionEntity> projections
				= catalogProjectionRepository.extractProjectionsByTypeAndCode(OPTION_IDENTITY_TYPE,
				skuOption.getOptionKey());

		assertThat(projections).hasSize(1);
	}

	@Test
	public void shouldUnchangedProjectionWhenEventIsUpdatedAndSameProjectionIsExist() {
		final SkuOption skuOption = createAndPersistSkuOption();

		await().atMost(TWO_SECONDS).until(() -> isProjectionExists(OPTION_IDENTITY_TYPE, skuOption.getOptionKey(), store.getCode()));

		final List<ProjectionEntity> savedProjections
				= catalogProjectionRepository.extractProjectionsByTypeAndCode(OPTION_IDENTITY_TYPE,
				skuOption.getOptionKey());

		final String savedHash = savedProjections.get(0).getContentHash();

		skuOptionUpdateProcessor.processSkuOptionUpdated(skuOption);
		final List<ProjectionEntity> updatedProjections
				= catalogProjectionRepository.extractProjectionsByTypeAndCode(OPTION_IDENTITY_TYPE,
				skuOption.getOptionKey());

		final String updatedHash = updatedProjections.get(0).getContentHash();

		assertThat(savedHash).isEqualTo(updatedHash);

	}

	@Test
	public void shouldAddedNewProjectionWhenEventIsUpdatedAndSameProjectionNotExist() {
		final SkuOption skuOption = createAndPersistSkuOption();

		await().atMost(TWO_SECONDS).until(() -> isProjectionExists(OPTION_IDENTITY_TYPE, skuOption.getOptionKey(), store.getCode()));

		skuOption.setDisplayName(NEW_SKU_DISPLAY_NAME, Locale.ENGLISH);
		SkuOptionLocalizedPropertyValueImpl value = new SkuOptionLocalizedPropertyValueImpl();
		value.setLocalizedPropertyKey("skuOptionDisplayName_en_US");
		value.setValue("skuOptionDisplayName_en_US:" + NEW_SKU_DISPLAY_NAME);
		skuOption.getLocalizedProperties().getLocalizedPropertiesMap().put("skuOptionDisplayName_en_US", value);
		skuOptionUpdateProcessor.processSkuOptionUpdated(skuOption);
		final List<ProjectionEntity> projections
				= catalogProjectionRepository.extractProjectionsByTypeAndCode(OPTION_IDENTITY_TYPE,
				skuOption.getOptionKey());
		final long projectionsCount = catalogProjectionRepository.count();
		final ProjectionHistoryId historyId = new ProjectionHistoryId();
		historyId.setStore(projections.get(0).getStore());
		historyId.setCode(projections.get(0).getCode());
		historyId.setType(projections.get(0).getType());
		historyId.setVersion(1L);
		final ProjectionHistoryEntity historyEntity = historyRepository.findOne(historyId);

		assertThat(projectionsCount).isEqualTo(1);
		assertThat(projections.get(0).getVersion()).isEqualTo(2L);
		assertThat(historyEntity.getVersion()).isEqualTo(1L);
	}

	@Test
	public void shouldAppendedProjectionWhenEventIsDeleted() {
		final SkuOption skuOption = createAndPersistSkuOption();

		await().atMost(TWO_SECONDS).until(() -> isProjectionExists(OPTION_IDENTITY_TYPE, skuOption.getOptionKey(), store.getCode()));

		List<ProjectionEntity> notDeletedProjections
				= catalogProjectionRepository.findNotDeletedProjectionEntities(OPTION_IDENTITY_TYPE,
				skuOption.getOptionKey());
		assertThat(notDeletedProjections.size()).isEqualTo(1);

		final ProjectionEntity entity = notDeletedProjections.get(0);

		skuOptionUpdateProcessor.processSkuOptionDeleted(skuOption.getOptionKey());
		final ProjectionHistoryId historyId = new ProjectionHistoryId();
		historyId.setStore(entity.getStore());
		historyId.setCode(entity.getCode());
		historyId.setType(entity.getType());
		historyId.setVersion(1L);
		final ProjectionHistoryEntity historyEntity = historyRepository.findOne(historyId);
		final Optional<ProjectionEntity> deletedProjection
				= catalogProjectionRepository.extractProjectionEntity(entity.getType(), entity.getCode(),
				entity.getStore());

		assertThat(historyEntity.getVersion()).isEqualTo(1L);
		assertThat(deletedProjection).isNotEmpty();

		assertTrue(deletedProjection.get().isDeleted());
		assertThat(catalogProjectionRepository.findNotDeletedProjectionEntities(OPTION_IDENTITY_TYPE,
				skuOption.getOptionKey()).size()).isEqualTo(0);

		assertThat(deletedProjection.get().getContent()).isNull();
		assertThat(deletedProjection.get().getContentHash()).isNull();
		assertThat(deletedProjection.get().getSchemaVersion()).isNull();
	}

	private Store createAndPersistStore() throws DefaultValueRemovalForbiddenException {
		final Store store = createStore();
		store.setSupportedLocales(Collections.singletonList(Locale.getDefault()));

		return doInTransaction(status -> persist(store));
	}

	private SkuOption createAndPersistSkuOption() {
		final SkuOption skuOption = getBeanFactory().getPrototypeBean(ContextIdNames.SKU_OPTION, SkuOption.class);
		skuOption.initialize();
		skuOption.setOptionKey(Utils.uniqueCode(SKU_OPTION_KEY));
		skuOption.setCatalog(catalog);
		skuOption.setDisplayName(Utils.uniqueCode(SKU_DISPLAY_NAME), Locale.ENGLISH);

		SkuOptionLocalizedPropertyValueImpl value = new SkuOptionLocalizedPropertyValueImpl();
		value.setLocalizedPropertyKey("skuOptionDisplayName_en_US");
		value.setValue("skuOptionDisplayName_en_US:" + SKU_DISPLAY_NAME);
		skuOption.getLocalizedProperties().getLocalizedPropertiesMap().put("skuOptionDisplayName_en_US", value);

		return doInTransaction(status -> persist(skuOption));
	}

	private <T extends Persistable> T persist(final T entity) {
		getPersistenceEngine().saveOrUpdate(entity);
		return entity;
	}

	private boolean isProjectionExists(final String type, final String code, final String store) {
		return catalogProjectionRepository.extractProjectionEntity(type, code, store).isPresent();
	}

}
