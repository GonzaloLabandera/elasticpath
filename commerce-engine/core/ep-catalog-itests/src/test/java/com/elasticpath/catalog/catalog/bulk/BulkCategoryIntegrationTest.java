/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.catalog.bulk;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

import com.elasticpath.catalog.messages.RelayOutboxMessagesThreadExecutor;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionRepository;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.CatalogTestPersister;
import com.elasticpath.test.persister.TaxTestPersister;
import com.elasticpath.test.persister.testscenarios.MultiCategoryScenario;

/**
 * Tests that Category bulk works correctly after link/unlink and include/exclude category.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DirtiesDatabase
public class BulkCategoryIntegrationTest extends DbTestCase {

	private static final Logger LOGGER = LogManager.getLogger(BulkCategoryIntegrationTest.class);

	private static final String CATALOG_MESSAGING_CAMEL_CONTEXT = "ep-catalog-messaging";
	private static final String JMS_CATALOG_ENDPOINT = "jms:topic:VirtualTopic.ep.catalog";

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CatalogProjectionRepository catalogProjectionRepository;

	@Autowired
	private RelayOutboxMessagesThreadExecutor relayOutboxMessagesThreadExecutor;

	@Autowired
	@Qualifier("catalogTestPersister")
	private CatalogTestPersister catalogTestPersister;

	@Autowired
	@Qualifier(CATALOG_MESSAGING_CAMEL_CONTEXT)
	private CamelContext catalogCamelContext;

	private Category parentLinkedCategory;
	private Catalog masterCatalog;
	private Category masterCategory;
	private Store virtualStore;

	@Before
	public void setUp() throws Exception {
		catalogCamelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(JMS_CATALOG_ENDPOINT)
						.process(exchange -> LOGGER.info("Catalog endpoint exchange: " + exchange.getIn().getBody()));
			}
		});

		final int expectedNumberOfCatalogEvents = 2;

		final NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext).from(JMS_CATALOG_ENDPOINT)
				.whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		final MultiCategoryScenario scenario = getTac().useScenario(MultiCategoryScenario.class);
		parentLinkedCategory = scenario.getLinkedCategory();
		masterCatalog = scenario.getCategory().getCatalog();
		masterCategory = scenario.getCategory();
		virtualStore = scenario.getVirtualStore();

		relayOutboxMessagesThreadExecutor.start();
		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);
	}

	@After
	public void tearDown() {
		relayOutboxMessagesThreadExecutor.stop();
	}

	@Test
	public void testThatOfferProjectionsShouldBeTombstoneForStoreBasedOnLinkedCategoryWhenCategoryUnlinked() {
		final int expectedNumberOfCatalogEvents = 2;
		addProduct();
		checkThatAllOfferProjectionsAreNotTombstone();

		final NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		categoryService.removeLinkedCategoryTree(parentLinkedCategory);
		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);

		checkThatOfferProjectionForVirtualCatalogIsTombstone();
	}

	@Test
	public void testThatOfferProjectionsShouldBeTombstoneForStoreBasedOnLinkedCategoryWhenCategoryExcluded() {
		final int expectedNumberOfCatalogEvents = 2;
		addProduct();
		checkThatAllOfferProjectionsAreNotTombstone();

		final NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		categoryService.removeCategoryProducts(parentLinkedCategory);
		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);

		checkThatOfferProjectionForVirtualCatalogIsTombstone();
	}

	@Test
	public void testThatOfferProjectionsShouldBeNotTombstoneForStoreBasedOnLinkedCategoryWhenCategoryIncluded() {
		final int expectedNumberOfCatalogEventsAfterAdd = 2;
		final int expectedNumberOfCatalogEventsAfterRemove = 2;

		addProduct();
		checkThatAllOfferProjectionsAreNotTombstone();

		NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEventsAfterRemove).create();

		categoryService.removeCategoryProducts(parentLinkedCategory);
		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);
		checkThatOfferProjectionForVirtualCatalogIsTombstone();

		catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEventsAfterAdd).create();

		categoryService.addLinkedCategoryProducts(parentLinkedCategory);
		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);

		checkThatAllOfferProjectionsAreNotTombstone();
	}

	@Test
	public void testThatOfferProjectionsAreCreatedAfterLinkedCategory() {
		addProduct();
		checkThatOfferExistsForStoreBasedOnVirtualCatalog();
	}

	private void addProduct() {
		final int expectedNumberOfCatalogEvents = 2;

		final NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		getTac().getPersistersFactory()
				.getCatalogTestPersister()
				.persistSimpleProduct("BASE_AMOUNT_TEST_PRODUCT", "newTestType", masterCatalog, masterCategory,
						getTac().getPersistersFactory().getTaxTestPersister().getTaxCode(TaxTestPersister.TAX_CODE_GOODS));
		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);
	}

	private void checkThatAllOfferProjectionsAreNotTombstone() {
		List<ProjectionEntity> result = StreamSupport.stream(catalogProjectionRepository.findAll().spliterator(), false)
				.filter(projection -> projection.getProjectionId().getType().equals("offer"))
				.filter(projection -> !projection.isDeleted()).collect(Collectors.toList());
		assertEquals(2, result.size());
	}

	private void checkThatOfferProjectionForVirtualCatalogIsTombstone() {
		List<ProjectionEntity> result = StreamSupport.stream(catalogProjectionRepository.findAll().spliterator(), false)
				.filter(projection -> projection.getProjectionId().getType().equals("offer"))
				.filter(projection -> projection.getProjectionId().getStore().equals(virtualStore.getCode()))
				.filter(ProjectionEntity::isDeleted).collect(Collectors.toList());
		assertEquals(1, result.size());
	}

	private void checkThatOfferExistsForStoreBasedOnVirtualCatalog() {
		List<ProjectionEntity> result = StreamSupport.stream(catalogProjectionRepository.findAll().spliterator(), false)
				.filter(projection -> projection.getProjectionId().getType().equals("offer"))
				.filter(projection -> !projection.isDeleted())
				.filter(projection -> projection.getProjectionId().getStore().equals(virtualStore.getCode())).collect(Collectors.toList());
		assertEquals(1, result.size());
	}
}
