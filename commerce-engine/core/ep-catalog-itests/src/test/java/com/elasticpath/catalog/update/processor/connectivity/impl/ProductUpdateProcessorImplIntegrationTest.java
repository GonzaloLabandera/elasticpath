/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;
import static com.elasticpath.catalog.update.processor.connectivity.impl.ProductUpdateProcessorImplIntegrationTest.JMS_BROKER_URL;
import static com.elasticpath.domain.catalog.AvailabilityCriteria.AVAILABLE_FOR_PRE_ORDER;
import static com.elasticpath.domain.catalog.AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK;
import static com.elasticpath.domain.catalogview.impl.StoreAvailabilityRule.ALWAYS;
import static com.elasticpath.domain.catalogview.impl.StoreAvailabilityRule.HAS_STOCK;
import static com.elasticpath.domain.catalogview.impl.StoreAvailabilityRule.PRE_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;

import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

import com.elasticpath.catalog.plugin.converter.impl.OfferContent;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionHistoryRepository;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionRepository;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.CategoryType;
import com.elasticpath.domain.catalog.DefaultValueRemovalForbiddenException;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.catalog.impl.BrandImpl;
import com.elasticpath.domain.catalog.impl.BundleConstituentImpl;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.catalog.impl.CategoryImpl;
import com.elasticpath.domain.catalog.impl.CategoryTypeImpl;
import com.elasticpath.domain.catalog.impl.ProductBundleImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.catalog.impl.ProductTypeImpl;
import com.elasticpath.domain.misc.LocalizedPropertyValue;
import com.elasticpath.domain.misc.impl.BrandLocalizedPropertyValueImpl;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.StoreState;
import com.elasticpath.domain.store.StoreType;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.domain.store.WarehouseAddress;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.domain.store.impl.WarehouseAddressImpl;
import com.elasticpath.domain.store.impl.WarehouseImpl;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.domain.tax.impl.TaxCodeImpl;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.jta.JmsBrokerConfigurator;
import com.elasticpath.test.jta.XaTransactionTestSupport;
import com.elasticpath.test.util.Utils;

/**
 * Integration tests for {@link ProductUpdateProcessorImpl}.
 */
@JmsBrokerConfigurator(url = JMS_BROKER_URL)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DirtiesDatabase
public class ProductUpdateProcessorImplIntegrationTest extends XaTransactionTestSupport {

	public static final String JMS_BROKER_URL = "tcp://localhost:61625";

	private static final Logger LOGGER = Logger.getLogger(ProductUpdateProcessorImplIntegrationTest.class);

	private static final String DOMAIN_BULK_MESSAGING_CAMEL_CONTEXT = "ep-domain-bulk-messaging";
	private static final String DOMAIN_BULK_ENDPOINT = "direct:ep.domain.bulk";
	private static final String JMS_DOMAIN_BULK_ENDPOINT = "jms:queue:Consumer.projectionBulkChangeHandler.VirtualTopic.ep.domain.bulk";

	private static final String CATALOG_MESSAGING_CAMEL_CONTEXT = "ep-catalog-messaging";
	private static final String JMS_CATALOG_ENDPOINT = "jms:topic:VirtualTopic.ep.catalog";

	private static final String GUID = "guid";
	private static final String BRAND = "brand";
	private static final String CATEGORY = "category";
	private static final String CATALOG = "catalog";
	private static final String SKU_CODE = "skuCode";
	private static final String TAX_CODE = "taxCode";
	private static final String PRODUCT_TYPE = "productType";
	private static final String DISPLAY_NAME = "displayName";
	private static final String CATEGORY_TYPE = "categoryType";
	private static final String CITY = "city";
	private static final String COUNTRY = "country";
	private static final String STREET = "street";
	private static final String ZIP_OR_POSTAL_CODE = "zipOrPostalCode";
	private static final String WAREHOUSE = "warehouse";
	private static final String URL = "http://www.some-where-out-there.com";
	private static final String SENDER_ADDRESS = "some@name.com";
	private static final String ADMIN_EMAIL_ADDRESS = "admin@test.com";

	private static final String BULK_CHANGE_MAX_EVENT_SIZE = "2";

	private static final Date NOW = new Date();
	private static final Date YESTERDAY = DateUtils.addDays(NOW, -1);
	private static final Date TOMORROW = DateUtils.addDays(NOW, 1);
	private static final Date THIRTY_DAYS_AGO = DateUtils.addDays(NOW, -30);
	private static final Date THIRTY_FIVE_DAYS_AGO = DateUtils.addDays(NOW, -35);
	private static final Date TWENTY_FIVE_DAYS_AGO = DateUtils.addDays(NOW, -25);
	private static final Date TWENTY_DAYS_AGO = DateUtils.addDays(NOW, -20);
	private static final Date FIFTEEN_DAYS_AGO = DateUtils.addDays(NOW, -15);
	private static final Date THIRTY_DAYS_AFTER = DateUtils.addDays(NOW, 30);
	private static final Date THIRTY_FIVE_DAYS_AFTER = DateUtils.addDays(NOW, 35);
	private static final Date TWENTY_FIVE_DAYS_AFTER = DateUtils.addDays(NOW, 25);
	private static final Date TWENTY_DAYS_AFTER = DateUtils.addDays(NOW, 20);
	private static final Date FIFTEEN_DAYS_AFTER = DateUtils.addDays(NOW, 15);

	private ObjectMapper objectMapper;

	@Autowired
	@Qualifier(DOMAIN_BULK_MESSAGING_CAMEL_CONTEXT)
	private CamelContext domainBulkCamelContext;

	@Autowired
	@Qualifier(CATALOG_MESSAGING_CAMEL_CONTEXT)
	private CamelContext catalogCamelContext;

	@Autowired
	private CatalogProjectionRepository catalogProjectionRepository;

	@Autowired
	private CatalogProjectionHistoryRepository historyRepository;

	@BeforeClass
	public static void setUpClass() {
		System.setProperty("bulkChangeMaxEventSize", BULK_CHANGE_MAX_EVENT_SIZE);
	}

	@Before
	public void setUp() throws Exception {
		historyRepository.deleteAll();
		catalogProjectionRepository.deleteAll();
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		catalogCamelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(JMS_CATALOG_ENDPOINT)
						.process(exchange -> LOGGER.info("Catalog endpoint exchange: " + exchange.getIn().getBody()));
			}
		});
	}

	@Test
	public void shouldPublish2BulkEventsAndSetEnableAndDisableDateTimeAsInProductWhen3ProductBundleContainProductAndBulkChangeMaxEventSizeIs2()
			throws Exception {
		final int expectedNumberOfCatalogEventsBeforeProductUpdate = 9;
		final int expectedNumberOfBulkEvents = 2;
		final int expectedNumberOfCatalogEventsAfterProductUpdate = 3;

		final NotifyBuilder catalogNotifyBuilderBeforeProductUpdate = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEventsBeforeProductUpdate).create();

		final NotifyBuilder domainBulkNotifyBuilder = new NotifyBuilder(domainBulkCamelContext).from(DOMAIN_BULK_ENDPOINT)
				.wereSentTo(JMS_DOMAIN_BULK_ENDPOINT).whenExactlyCompleted(expectedNumberOfBulkEvents).create();

		final Catalog catalog = createAndPersistMasterCatalog(Utils.uniqueCode(CATALOG), CATALOG);

		final Warehouse warehouse = createAndPersistWarehouse();
		final Store store = createAndPersistStore(catalog, Collections.singletonList(warehouse));

		final Brand brand = createAndPersistBrand(Utils.uniqueCode(BRAND), Utils.uniqueCode(DISPLAY_NAME), catalog);

		final CategoryType categoryType = createAndPersistCategoryType(catalog);
		final Category category = createAndPersistCategory(Utils.uniqueCode(GUID), Utils.uniqueCode(CATEGORY), categoryType, catalog, YESTERDAY);
		final List<Category> categories = Collections.singletonList(category);

		final TaxCode taxCode = createAndPersistTaxCode();
		final ProductType productType = createAndPersistProductType(catalog, taxCode);
		final Product product1 = createAndPersistProduct("product1", productType, categories, brand, THIRTY_DAYS_AGO, THIRTY_DAYS_AFTER);
		final Product product2 = createAndPersistProduct("product2", productType, categories, brand, THIRTY_DAYS_AGO, THIRTY_DAYS_AFTER);
		final Product product3 = createAndPersistProduct("product3", productType, categories, brand, TWENTY_FIVE_DAYS_AGO, TWENTY_FIVE_DAYS_AFTER);
		final Product product4 = createAndPersistProduct("product4", productType, categories, brand, TWENTY_DAYS_AGO, TWENTY_DAYS_AFTER);

		final ProductBundle bundle1 = createAndPersistProductBundle("productBundle1", productType, categories, brand, Arrays.asList(product1,
				product2), THIRTY_DAYS_AGO, THIRTY_DAYS_AFTER);
		createAndPersistProductBundle("productBundle2", productType, categories, brand, Arrays.asList(bundle1, product3), TWENTY_FIVE_DAYS_AGO,
				TWENTY_FIVE_DAYS_AFTER);
		createAndPersistProductBundle("productBundle3", productType, categories, brand, Arrays.asList(product1, product4), TWENTY_DAYS_AGO,
				TWENTY_DAYS_AFTER);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderBeforeProductUpdate::matches);

		final NotifyBuilder catalogNotifyBuilderAfterProductUpdate = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEventsAfterProductUpdate).create();

		product1.setStartDate(FIFTEEN_DAYS_AGO);
		product1.setEndDate(FIFTEEN_DAYS_AFTER);
		doInTransaction(status -> persist(product1));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderAfterProductUpdate::matches);
		final Optional<ProjectionEntity> productBundle1 = catalogProjectionRepository.extractProjectionEntity(OFFER_IDENTITY_TYPE, "productBundle1",
				store.getCode());
		final Optional<ProjectionEntity> productBundle2 = catalogProjectionRepository.extractProjectionEntity(OFFER_IDENTITY_TYPE, "productBundle2",
				store.getCode());
		final Optional<ProjectionEntity> productBundle3 = catalogProjectionRepository.extractProjectionEntity(OFFER_IDENTITY_TYPE, "productBundle3",
				store.getCode());

		final OfferContent productBundle1Content = objectMapper.readValue(productBundle1.get().getContent(), OfferContent.class);
		final OfferContent productBundle2Content = objectMapper.readValue(productBundle2.get().getContent(), OfferContent.class);
		final OfferContent productBundle3Content = objectMapper.readValue(productBundle3.get().getContent(), OfferContent.class);

		assertThat(domainBulkNotifyBuilder.matches()).isTrue();
		assertThat(Date.from(productBundle1Content.getAvailabilityRules().getEnableDateTime().toInstant())).isEqualTo(FIFTEEN_DAYS_AGO);
		assertThat(Date.from(productBundle1Content.getAvailabilityRules().getDisableDateTime().toInstant())).isEqualTo(FIFTEEN_DAYS_AFTER);
		assertThat(Date.from(productBundle2Content.getAvailabilityRules().getEnableDateTime().toInstant())).isEqualTo(FIFTEEN_DAYS_AGO);
		assertThat(Date.from(productBundle2Content.getAvailabilityRules().getDisableDateTime().toInstant())).isEqualTo(FIFTEEN_DAYS_AFTER);
		assertThat(Date.from(productBundle3Content.getAvailabilityRules().getEnableDateTime().toInstant())).isEqualTo(FIFTEEN_DAYS_AGO);
		assertThat(Date.from(productBundle3Content.getAvailabilityRules().getDisableDateTime().toInstant())).isEqualTo(FIFTEEN_DAYS_AFTER);
	}

	@Test
	public void shouldPublish2BulkEventsAndSetEnableAndDisableDateTimeAsInProductBundlesWhen3ProductBundleContainProductAndBulkChangeMaxEventSizeIs2()
			throws Exception {
		final int expectedNumberOfCatalogEventsBeforeProductUpdate = 9;
		final int expectedNumberOfBulkEvents = 2;
		final int expectedNumberOfCatalogEventsAfterProductUpdate = 3;

		final NotifyBuilder catalogNotifyBuilderBeforeProductUpdate = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEventsBeforeProductUpdate).create();

		final NotifyBuilder domainBulkNotifyBuilder = new NotifyBuilder(domainBulkCamelContext).from(DOMAIN_BULK_ENDPOINT)
				.wereSentTo(JMS_DOMAIN_BULK_ENDPOINT).whenExactlyCompleted(expectedNumberOfBulkEvents).create();

		final Catalog catalog = createAndPersistMasterCatalog(Utils.uniqueCode(CATALOG), CATALOG);

		final Warehouse warehouse = createAndPersistWarehouse();
		final Store store = createAndPersistStore(catalog, Collections.singletonList(warehouse));

		final Brand brand = createAndPersistBrand(Utils.uniqueCode(BRAND), Utils.uniqueCode(DISPLAY_NAME), catalog);

		final CategoryType categoryType = createAndPersistCategoryType(catalog);
		final Category category = createAndPersistCategory(Utils.uniqueCode(GUID), Utils.uniqueCode(CATEGORY), categoryType, catalog, YESTERDAY);
		final List<Category> categories = Collections.singletonList(category);

		final TaxCode taxCode = createAndPersistTaxCode();
		final ProductType productType = createAndPersistProductType(catalog, taxCode);
		final Product product1 = createAndPersistProduct("product1", productType, categories, brand, FIFTEEN_DAYS_AGO, FIFTEEN_DAYS_AFTER);
		final Product product2 = createAndPersistProduct("product2", productType, categories, brand, THIRTY_DAYS_AGO, THIRTY_DAYS_AFTER);
		final Product product3 = createAndPersistProduct("product3", productType, categories, brand, TWENTY_FIVE_DAYS_AGO, TWENTY_FIVE_DAYS_AFTER);
		final Product product4 = createAndPersistProduct("product4", productType, categories, brand, TWENTY_DAYS_AGO, TWENTY_DAYS_AFTER);

		final ProductBundle bundle1 = createAndPersistProductBundle("productBundle1", productType, categories, brand, Arrays.asList(product1,
				product2), THIRTY_DAYS_AGO, THIRTY_DAYS_AFTER);
		createAndPersistProductBundle("productBundle2", productType, categories, brand, Arrays.asList(bundle1, product3), TWENTY_FIVE_DAYS_AGO,
				TWENTY_FIVE_DAYS_AFTER);
		createAndPersistProductBundle("productBundle3", productType, categories, brand, Arrays.asList(product1, product4), TWENTY_DAYS_AGO,
				TWENTY_DAYS_AFTER);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderBeforeProductUpdate::matches);

		final NotifyBuilder catalogNotifyBuilderAfterProductUpdate = new NotifyBuilder(catalogCamelContext).from(JMS_CATALOG_ENDPOINT)
				.whenExactlyCompleted(expectedNumberOfCatalogEventsAfterProductUpdate).create();

		product1.setStartDate(THIRTY_FIVE_DAYS_AGO);
		product1.setEndDate(THIRTY_FIVE_DAYS_AFTER);
		doInTransaction(status -> persist(product1));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderAfterProductUpdate::matches);
		final Optional<ProjectionEntity> productBundle1 = catalogProjectionRepository.extractProjectionEntity(OFFER_IDENTITY_TYPE, "productBundle1",
				store.getCode());
		final Optional<ProjectionEntity> productBundle2 = catalogProjectionRepository.extractProjectionEntity(OFFER_IDENTITY_TYPE, "productBundle2",
				store.getCode());
		final Optional<ProjectionEntity> productBundle3 = catalogProjectionRepository.extractProjectionEntity(OFFER_IDENTITY_TYPE, "productBundle3",
				store.getCode());

		final OfferContent productBundle1Content = objectMapper.readValue(productBundle1.get().getContent(), OfferContent.class);
		final OfferContent productBundle2Content = objectMapper.readValue(productBundle2.get().getContent(), OfferContent.class);
		final OfferContent productBundle3Content = objectMapper.readValue(productBundle3.get().getContent(), OfferContent.class);

		assertThat(domainBulkNotifyBuilder.matches()).isTrue();
		assertThat(Date.from(productBundle1Content.getAvailabilityRules().getEnableDateTime().toInstant())).isEqualTo(THIRTY_DAYS_AGO);
		assertThat(Date.from(productBundle1Content.getAvailabilityRules().getDisableDateTime().toInstant())).isEqualTo(THIRTY_DAYS_AFTER);
		assertThat(Date.from(productBundle2Content.getAvailabilityRules().getEnableDateTime().toInstant())).isEqualTo(TWENTY_FIVE_DAYS_AGO);
		assertThat(Date.from(productBundle2Content.getAvailabilityRules().getDisableDateTime().toInstant())).isEqualTo(TWENTY_FIVE_DAYS_AFTER);
		assertThat(Date.from(productBundle3Content.getAvailabilityRules().getEnableDateTime().toInstant())).isEqualTo(TWENTY_DAYS_AGO);
		assertThat(Date.from(productBundle3Content.getAvailabilityRules().getDisableDateTime().toInstant())).isEqualTo(TWENTY_DAYS_AFTER);
	}

	@Test
	public void shouldPublish1BulkEventsAndSetIsDeletedTrueWhen2ProductBundleContainProductAndBulkChangeMaxEventSizeIs2AndProductUpdatedHiddenTrue()
			throws Exception {
		final int expectedNumberOfCatalogEventsBeforeProductUpdate = 7;
		final int expectedNumberOfBulkEvents = 1;
		final int expectedNumberOfCatalogEventsAfterProductUpdate = 2;

		final NotifyBuilder catalogNotifyBuilderBeforeProductUpdate = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEventsBeforeProductUpdate).create();

		final NotifyBuilder domainBulkNotifyBuilder = new NotifyBuilder(domainBulkCamelContext).from(DOMAIN_BULK_ENDPOINT)
				.wereSentTo(JMS_DOMAIN_BULK_ENDPOINT).whenExactlyCompleted(expectedNumberOfBulkEvents).create();

		final Catalog catalog = createAndPersistMasterCatalog(Utils.uniqueCode(CATALOG), CATALOG);

		final Warehouse warehouse = createAndPersistWarehouse();
		createAndPersistStore(catalog, Collections.singletonList(warehouse));

		final Brand brand = createAndPersistBrand(Utils.uniqueCode(BRAND), Utils.uniqueCode(DISPLAY_NAME), catalog);

		final CategoryType categoryType = createAndPersistCategoryType(catalog);
		final Category category = createAndPersistCategory(Utils.uniqueCode(GUID), Utils.uniqueCode(CATEGORY), categoryType, catalog, YESTERDAY);
		final List<Category> categories = Collections.singletonList(category);

		final TaxCode taxCode = createAndPersistTaxCode();
		final ProductType productType = createAndPersistProductType(catalog, taxCode);
		final Product product1 = createAndPersistProduct("product1", productType, categories, brand, YESTERDAY);
		final Product product2 = createAndPersistProduct("product2", productType, categories, brand, YESTERDAY);
		final Product product3 = createAndPersistProduct("product3", productType, categories, brand, YESTERDAY);

		createAndPersistProductBundle("productBundle1", productType, categories, brand, Arrays.asList(product1, product2), false, YESTERDAY);
		createAndPersistProductBundle("productBundle2", productType, categories, brand, Arrays.asList(product1, product3), true, NOW);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderBeforeProductUpdate::matches);

		final NotifyBuilder catalogNotifyBuilderAfterProductUpdate = new NotifyBuilder(catalogCamelContext).from(JMS_CATALOG_ENDPOINT)
				.whenExactlyCompleted(expectedNumberOfCatalogEventsAfterProductUpdate).create();

		product1.setHidden(true);
		doInTransaction(status -> persist(product1));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderAfterProductUpdate::matches);

		final List<ProjectionEntity> projections = catalogProjectionRepository.findLatestProjectionsWithCodes(OFFER_IDENTITY_TYPE,
				Arrays.asList("productBundle1", "productBundle2"));

		assertThat(domainBulkNotifyBuilder.matches()).isTrue();
		assertThat(projections).extracting(ProjectionEntity::isDeleted).containsOnly(true);
	}

	@Test
	public void shouldPublish1BulkEventsAndSetIsDeletedAsInProductBundleTrueWhen2ProductBundleContainProductAndProductUpdatedHiddenFalse()
			throws Exception {
		final int expectedNumberOfCatalogEventsBeforeProductUpdate = 7;
		final int expectedNumberOfBulkEvents = 1;
		final int expectedNumberOfCatalogEventsAfterProductUpdate = 2;

		final NotifyBuilder catalogNotifyBuilderBeforeProductUpdate = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEventsBeforeProductUpdate).create();

		final NotifyBuilder domainBulkNotifyBuilder = new NotifyBuilder(domainBulkCamelContext).from(DOMAIN_BULK_ENDPOINT)
				.wereSentTo(JMS_DOMAIN_BULK_ENDPOINT).whenExactlyCompleted(expectedNumberOfBulkEvents).create();

		final Catalog catalog = createAndPersistMasterCatalog(Utils.uniqueCode(CATALOG), CATALOG);

		final Warehouse warehouse = createAndPersistWarehouse();
		createAndPersistStore(catalog, Collections.singletonList(warehouse));

		final Brand brand = createAndPersistBrand(Utils.uniqueCode(BRAND), Utils.uniqueCode(DISPLAY_NAME), catalog);

		final CategoryType categoryType = createAndPersistCategoryType(catalog);
		final Category category = createAndPersistCategory(Utils.uniqueCode(GUID), Utils.uniqueCode(CATEGORY), categoryType, catalog, YESTERDAY);
		final List<Category> categories = Collections.singletonList(category);

		final TaxCode taxCode = createAndPersistTaxCode();
		final ProductType productType = createAndPersistProductType(catalog, taxCode);
		final Product product1 = createAndPersistProduct("product1", productType, categories, brand, true, YESTERDAY);
		final Product product2 = createAndPersistProduct("product2", productType, categories, brand, YESTERDAY);
		final Product product3 = createAndPersistProduct("product3", productType, categories, brand, YESTERDAY);

		createAndPersistProductBundle("productBundle1", productType, categories, brand, Arrays.asList(product1, product2), false, YESTERDAY);
		createAndPersistProductBundle("productBundle2", productType, categories, brand, Arrays.asList(product1, product3), true, NOW);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderBeforeProductUpdate::matches);

		final NotifyBuilder catalogNotifyBuilderAfterProductUpdate = new NotifyBuilder(catalogCamelContext).from(JMS_CATALOG_ENDPOINT)
				.whenExactlyCompleted(expectedNumberOfCatalogEventsAfterProductUpdate).create();

		product1.setHidden(false);
		doInTransaction(status -> persist(product1));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderAfterProductUpdate::matches);

		final List<ProjectionEntity> projections = catalogProjectionRepository.findLatestProjectionsWithCodes(OFFER_IDENTITY_TYPE,
				Arrays.asList("productBundle1", "productBundle2"));

		assertThat(domainBulkNotifyBuilder.matches()).isTrue();
		assertThat(projections).filteredOn(projectionEntity -> Objects.equals(projectionEntity.getCode(), "productBundle1"))
				.extracting(ProjectionEntity::isDeleted).containsOnly(false);
		assertThat(projections).filteredOn(projectionEntity -> Objects.equals(projectionEntity.getCode(), "productBundle1"))
				.extracting(ProjectionEntity::getVersion).containsOnly(2L);
		assertThat(projections).filteredOn(projectionEntity -> Objects.equals(projectionEntity.getCode(), "productBundle2"))
				.extracting(ProjectionEntity::isDeleted).containsOnly(true);
		assertThat(projections).filteredOn(projectionEntity -> Objects.equals(projectionEntity.getCode(), "productBundle2"))
				.extracting(ProjectionEntity::getVersion).containsOnly(1L);
	}

	@Test
	public void shouldPublish1BulkEventAndSetAvailabilityRulesAsInProductWhenProductAvailabilityRulesSetAvailableForPreOrder()
			throws Exception {
		final int expectedNumberOfCatalogEventsBeforeProductUpdate = 7;
		final int expectedNumberOfBulkEvents = 1;
		final int expectedNumberOfCatalogEventsAfterProductUpdate = 2;

		final NotifyBuilder catalogNotifyBuilderBeforeProductUpdate = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEventsBeforeProductUpdate).create();

		final NotifyBuilder domainBulkNotifyBuilder = new NotifyBuilder(domainBulkCamelContext).from(DOMAIN_BULK_ENDPOINT)
				.wereSentTo(JMS_DOMAIN_BULK_ENDPOINT).whenExactlyCompleted(expectedNumberOfBulkEvents).create();

		final Catalog catalog = createAndPersistMasterCatalog(Utils.uniqueCode(CATALOG), CATALOG);

		final Warehouse warehouse = createAndPersistWarehouse();
		final Store store = createAndPersistStore(catalog, Collections.singletonList(warehouse));

		final Brand brand = createAndPersistBrand(Utils.uniqueCode(BRAND), Utils.uniqueCode(DISPLAY_NAME), catalog);

		final CategoryType categoryType = createAndPersistCategoryType(catalog);
		final Category category = createAndPersistCategory(Utils.uniqueCode(GUID), Utils.uniqueCode(CATEGORY), categoryType, catalog, YESTERDAY);
		final List<Category> categories = Collections.singletonList(category);

		final TaxCode taxCode = createAndPersistTaxCode();
		final ProductType productType = createAndPersistProductType(catalog, taxCode);
		final Product product1 = createAndPersistProduct("product1", productType, categories, brand, YESTERDAY, AVAILABLE_WHEN_IN_STOCK);
		final Product product2 = createAndPersistProduct("product2", productType, categories, brand, YESTERDAY, AVAILABLE_WHEN_IN_STOCK);
		final Product product3 = createAndPersistProduct("product3", productType, categories, brand, YESTERDAY, AVAILABLE_FOR_PRE_ORDER);

		final ProductBundle bundle1 = createAndPersistProductBundle("productBundle1", productType, categories, brand,
				Arrays.asList(product1, product2), YESTERDAY, AVAILABLE_WHEN_IN_STOCK);
		createAndPersistProductBundle("productBundle2", productType, categories, brand, Arrays.asList(bundle1, product3), NOW,
				AVAILABLE_WHEN_IN_STOCK);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderBeforeProductUpdate::matches);

		final NotifyBuilder catalogNotifyBuilderAfterProductUpdate = new NotifyBuilder(catalogCamelContext).from(JMS_CATALOG_ENDPOINT)
				.whenExactlyCompleted(expectedNumberOfCatalogEventsAfterProductUpdate).create();

		product1.setAvailabilityCriteria(AVAILABLE_FOR_PRE_ORDER);
		product1.setExpectedReleaseDate(NOW);
		doInTransaction(status -> persist(product1));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderAfterProductUpdate::matches);

		final Optional<ProjectionEntity> productBundle1 = catalogProjectionRepository.extractProjectionEntity(OFFER_IDENTITY_TYPE, "productBundle1",
				store.getCode());
		final Optional<ProjectionEntity> productBundle2 = catalogProjectionRepository.extractProjectionEntity(OFFER_IDENTITY_TYPE, "productBundle2",
				store.getCode());

		final OfferContent productBundle1Content = objectMapper.readValue(productBundle1.get().getContent(), OfferContent.class);
		final OfferContent productBundle2Content = objectMapper.readValue(productBundle2.get().getContent(), OfferContent.class);

		assertThat(domainBulkNotifyBuilder.matches()).isTrue();
		assertThat(productBundle1Content.getAvailabilityRules().getCanDiscover()).containsOnly(HAS_STOCK.getName(), PRE_ORDER.getName());
		assertThat(productBundle1Content.getAvailabilityRules().getCanView()).containsOnly(ALWAYS.getName());
		assertThat(productBundle1Content.getAvailabilityRules().getCanAddToCart()).containsOnly(HAS_STOCK.getName(), PRE_ORDER.getName());
		assertThat(productBundle2Content.getAvailabilityRules().getCanDiscover()).containsOnly(HAS_STOCK.getName(), PRE_ORDER.getName());
		assertThat(productBundle2Content.getAvailabilityRules().getCanView()).containsOnly(ALWAYS.getName());
		assertThat(productBundle2Content.getAvailabilityRules().getCanAddToCart()).containsOnly(HAS_STOCK.getName(), PRE_ORDER.getName());
	}

	@Test
	public void shouldPublish1BulkEventAndSetAvailabilityRulesAsInProductBundlesWhenProductAvailabilityRulesSetAvailableWhenInStock()
			throws Exception {
		final int expectedNumberOfCatalogEventsBeforeProductUpdate = 7;
		final int expectedNumberOfBulkEvents = 1;
		final int expectedNumberOfCatalogEventsAfterProductUpdate = 2;

		final NotifyBuilder catalogNotifyBuilderBeforeProductUpdate = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEventsBeforeProductUpdate).create();

		final NotifyBuilder domainBulkNotifyBuilder = new NotifyBuilder(domainBulkCamelContext).from(DOMAIN_BULK_ENDPOINT)
				.wereSentTo(JMS_DOMAIN_BULK_ENDPOINT).whenExactlyCompleted(expectedNumberOfBulkEvents).create();

		final Catalog catalog = createAndPersistMasterCatalog(Utils.uniqueCode(CATALOG), CATALOG);

		final Warehouse warehouse = createAndPersistWarehouse();
		final Store store = createAndPersistStore(catalog, Collections.singletonList(warehouse));

		final Brand brand = createAndPersistBrand(Utils.uniqueCode(BRAND), Utils.uniqueCode(DISPLAY_NAME), catalog);

		final CategoryType categoryType = createAndPersistCategoryType(catalog);
		final Category category = createAndPersistCategory(Utils.uniqueCode(GUID), Utils.uniqueCode(CATEGORY), categoryType, catalog, YESTERDAY);
		final List<Category> categories = Collections.singletonList(category);

		final TaxCode taxCode = createAndPersistTaxCode();
		final ProductType productType = createAndPersistProductType(catalog, taxCode);
		final Product product1 = createAndPersistProduct("product1", productType, categories, brand, false, YESTERDAY, TOMORROW, NOW,
				AVAILABLE_FOR_PRE_ORDER);
		final Product product2 = createAndPersistProduct("product2", productType, categories, brand, YESTERDAY, AVAILABLE_WHEN_IN_STOCK);
		final Product product3 = createAndPersistProduct("product3", productType, categories, brand, YESTERDAY, AVAILABLE_FOR_PRE_ORDER);

		final ProductBundle bundle1 = createAndPersistProductBundle("productBundle1", productType, categories, brand,
				Arrays.asList(product1, product2), YESTERDAY, AVAILABLE_WHEN_IN_STOCK);
		createAndPersistProductBundle("productBundle2", productType, categories, brand, Arrays.asList(bundle1, product3), NOW,
				AVAILABLE_WHEN_IN_STOCK);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderBeforeProductUpdate::matches);

		final NotifyBuilder catalogNotifyBuilderAfterProductUpdate = new NotifyBuilder(catalogCamelContext).from(JMS_CATALOG_ENDPOINT)
				.whenExactlyCompleted(expectedNumberOfCatalogEventsAfterProductUpdate).create();

		product1.setAvailabilityCriteria(AVAILABLE_WHEN_IN_STOCK);
		product1.setExpectedReleaseDate(null);
		doInTransaction(status -> persist(product1));

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilderAfterProductUpdate::matches);

		final Optional<ProjectionEntity> productBundle1 = catalogProjectionRepository.extractProjectionEntity(OFFER_IDENTITY_TYPE, "productBundle1",
				store.getCode());
		final Optional<ProjectionEntity> productBundle2 = catalogProjectionRepository.extractProjectionEntity(OFFER_IDENTITY_TYPE, "productBundle2",
				store.getCode());

		final OfferContent productBundle1Content = objectMapper.readValue(productBundle1.get().getContent(), OfferContent.class);
		final OfferContent productBundle2Content = objectMapper.readValue(productBundle2.get().getContent(), OfferContent.class);

		assertThat(domainBulkNotifyBuilder.matches()).isTrue();
		assertThat(productBundle1Content.getAvailabilityRules().getCanDiscover()).containsOnly(HAS_STOCK.getName());
		assertThat(productBundle1Content.getAvailabilityRules().getCanView()).containsOnly(ALWAYS.getName());
		assertThat(productBundle1Content.getAvailabilityRules().getCanAddToCart()).containsOnly(HAS_STOCK.getName());
		assertThat(productBundle2Content.getAvailabilityRules().getCanDiscover()).containsOnly(HAS_STOCK.getName(), PRE_ORDER.getName());
		assertThat(productBundle2Content.getAvailabilityRules().getCanView()).containsOnly(ALWAYS.getName());
		assertThat(productBundle2Content.getAvailabilityRules().getCanAddToCart()).containsOnly(HAS_STOCK.getName(), PRE_ORDER.getName());
	}

	private Catalog createAndPersistMasterCatalog(final String code, final String name) {
		final Catalog catalog = new CatalogImpl();
		catalog.setCode(code);
		catalog.setDefaultLocale(Locale.ENGLISH);
		catalog.setName(name);
		catalog.setMaster(true);

		return doInTransaction(status -> persist(catalog));
	}

	private Warehouse createAndPersistWarehouse() {
		final WarehouseAddress warehouseAddress = new WarehouseAddressImpl();
		warehouseAddress.setStreet1(Utils.uniqueCode(STREET));
		warehouseAddress.setCity(Utils.uniqueCode(CITY));
		warehouseAddress.setCountry(Utils.uniqueCode(COUNTRY));
		warehouseAddress.setZipOrPostalCode(ZIP_OR_POSTAL_CODE);

		final Warehouse warehouse = new WarehouseImpl();
		warehouse.setAddress(warehouseAddress);
		warehouse.setCode(Utils.uniqueCode(WAREHOUSE));
		warehouse.setName(Utils.uniqueCode(WAREHOUSE));

		return doInTransaction(status -> persist(warehouse));
	}

	private Store createAndPersistStore(final Catalog catalog, final List<Warehouse> warehouse) throws DefaultValueRemovalForbiddenException {
		final Store store = new StoreImpl();
		store.setCountry(Utils.uniqueCode(COUNTRY));
		store.setName(Utils.uniqueCode(DISPLAY_NAME));
		store.setStoreType(StoreType.B2B);
		store.setUrl(URL);
		store.setTimeZone(TimeZone.getDefault());
		store.setCode(Utils.uniqueCode(GUID));
		store.setEmailSenderName(Utils.uniqueCode(DISPLAY_NAME));
		store.setEmailSenderAddress(SENDER_ADDRESS);
		store.setStoreAdminEmailAddress(ADMIN_EMAIL_ADDRESS);
		store.setCatalog(catalog);
		store.setDefaultLocale(catalog.getDefaultLocale());
		store.setSupportedLocales(Collections.singletonList(catalog.getDefaultLocale()));
		store.setWarehouses(warehouse);
		store.setDefaultCurrency(Currency.getInstance(Locale.CANADA));
		store.setStoreState(StoreState.UNDER_CONSTRUCTION);

		return doInTransaction(status -> persist(store));
	}

	private CategoryType createAndPersistCategoryType(final Catalog catalog) {
		final CategoryType categoryType = createCategoryType(Utils.uniqueCode(GUID), Utils.uniqueCode(CATEGORY_TYPE), catalog);

		return doInTransaction(status -> persist(categoryType));
	}

	private CategoryType createCategoryType(final String guid, final String name, final Catalog catalog) {
		final CategoryType categoryType = new CategoryTypeImpl();
		categoryType.setGuid(guid);
		categoryType.setName(name);
		categoryType.setCatalog(catalog);

		return categoryType;
	}

	private Category createAndPersistCategory(final String guid, final String code, final CategoryType categoryType, final Catalog catalog,
											  final Date startDate) {
		final Category category = createCategory(guid, code, categoryType, catalog, startDate);

		return doInTransaction(status -> persist(category));
	}

	private Category createCategory(final String guid, final String code, final CategoryType categoryType, final Catalog catalog,
									final Date startDate) {
		final Category category = new CategoryImpl();
		category.setGuid(guid);
		category.setCode(code);
		category.setDisplayName(Utils.uniqueCode(DISPLAY_NAME), Locale.ENGLISH);
		category.setCategoryType(categoryType);
		category.setCatalog(catalog);
		category.setStartDate(startDate);
		category.setHidden(false);

		return category;
	}

	private TaxCode createAndPersistTaxCode() {
		final TaxCode taxCode = new TaxCodeImpl();
		taxCode.setGuid(Utils.uniqueCode(GUID));
		taxCode.setCode(Utils.uniqueCode(TAX_CODE));

		return doInTransaction(status -> persist(taxCode));
	}

	private ProductType createAndPersistProductType(final Catalog catalog, final TaxCode taxCode) {
		final ProductType productType = new ProductTypeImpl();
		productType.setGuid(Utils.uniqueCode(GUID));
		productType.setName(Utils.uniqueCode(PRODUCT_TYPE));
		productType.setCatalog(catalog);
		productType.setTaxCode(taxCode);

		return doInTransaction(status -> persist(productType));
	}

	private Product createAndPersistProduct(final String code, final ProductType productType, final List<Category> categories, final Brand brand,
											final Date startDate, final AvailabilityCriteria availabilityCriteria) {
		final boolean hidden = false;
		final Date expectedReleaseDate = null;
		final Date endDate = null;

		return createAndPersistProduct(code, productType, categories, brand, hidden, startDate, endDate, expectedReleaseDate, availabilityCriteria);
	}

	private Product createAndPersistProduct(final String code, final ProductType productType, final List<Category> categories, final Brand brand,
											final Date startDate) {
		final boolean hidden = false;
		final Date endDate = null;
		final Date expectedReleaseDate = null;
		final AvailabilityCriteria availabilityCriteria = null;

		return createAndPersistProduct(code, productType, categories, brand, hidden, startDate, endDate, expectedReleaseDate, availabilityCriteria);
	}

	private Product createAndPersistProduct(final String code, final ProductType productType, final List<Category> categories, final Brand brand,
											final Date startDate, final Date endDate) {
		final boolean hidden = false;
		final Date expectedReleaseDate = null;
		final AvailabilityCriteria availabilityCriteria = null;

		return createAndPersistProduct(code, productType, categories, brand, hidden, startDate, endDate, expectedReleaseDate, availabilityCriteria);
	}

	private Product createAndPersistProduct(final String code, final ProductType productType, final List<Category> categories, final Brand brand,
											final boolean hidden, final Date startDate) {
		final Date endDate = null;
		final Date expectedReleaseDate = null;
		final AvailabilityCriteria availabilityCriteria = null;

		return createAndPersistProduct(code, productType, categories, brand, hidden, startDate, endDate, expectedReleaseDate, availabilityCriteria);
	}

	@SuppressWarnings({"PMD.ExcessiveParameterList", "checkstyle:parameternumber"})
	private Product createAndPersistProduct(final String code, final ProductType productType, final List<Category> categories, final Brand brand,
											final boolean hidden, final Date startDate, final Date endDate, final Date expectedReleaseDate,
											final AvailabilityCriteria availabilityCriteria) {
		final Product product = new ProductImpl();
		product.setCode(code);
		product.setProductType(productType);
		categories.forEach(product::addCategory);
		product.setBrand(brand);
		product.setHidden(hidden);
		product.setStartDate(startDate);
		product.setEndDate(endDate);
		product.setExpectedReleaseDate(expectedReleaseDate);
		product.setLastModifiedDate(startDate);
		product.setAvailabilityCriteria(availabilityCriteria);

		final ProductSku productSku = createProductSku(Utils.uniqueCode(GUID), Utils.uniqueCode(SKU_CODE), startDate, Collections.emptyMap(),
				Collections.emptyMap());
		productSku.setProduct(product);

		final Map<String, ProductSku> productSkus = new HashMap<>();
		productSkus.put(productSku.getSkuCode(), productSku);

		product.setProductSkus(productSkus);

		return doInTransaction(status -> persist(product));
	}

	private ProductBundle createAndPersistProductBundle(final String code, final ProductType productType, final List<Category> categories,
														final Brand brand, final List<Product> products, final boolean hidden,
														final Date startDate) {
		final Date endDate = null;
		final AvailabilityCriteria availabilityCriteria = null;

		final ProductBundle productBundle = createProductBundle(code, productType, categories, brand, products, hidden, startDate, endDate,
				availabilityCriteria);

		final ProductSku productSku = createProductSku(Utils.uniqueCode(GUID), Utils.uniqueCode(SKU_CODE), startDate, Collections.emptyMap(),
				Collections.emptyMap());
		productSku.setProduct(productBundle);

		final Map<String, ProductSku> productSkus = new HashMap<>();
		productSkus.put(productSku.getSkuCode(), productSku);

		productBundle.setProductSkus(productSkus);

		return doInTransaction(status -> persist(productBundle));
	}

	private ProductBundle createAndPersistProductBundle(final String code, final ProductType productType, final List<Category> categories,
														final Brand brand, final List<Product> products, final Date startDate, final Date endDate) {
		final boolean hidden = false;
		final AvailabilityCriteria availabilityCriteria = null;

		final ProductBundle productBundle = createProductBundle(code, productType, categories, brand, products, hidden, startDate, endDate,
				availabilityCriteria);

		final ProductSku productSku = createProductSku(Utils.uniqueCode(GUID), Utils.uniqueCode(SKU_CODE), startDate, Collections.emptyMap(),
				Collections.emptyMap());
		productSku.setProduct(productBundle);

		final Map<String, ProductSku> productSkus = new HashMap<>();
		productSkus.put(productSku.getSkuCode(), productSku);

		productBundle.setProductSkus(productSkus);

		return doInTransaction(status -> persist(productBundle));
	}

	private ProductBundle createAndPersistProductBundle(final String code, final ProductType productType, final List<Category> categories,
														final Brand brand, final List<Product> products, final Date startDate,
														final AvailabilityCriteria availabilityCriteria) {
		final boolean hidden = false;
		final Date endDate = null;

		final ProductBundle productBundle = createProductBundle(code, productType, categories, brand, products, hidden, startDate, endDate,
				availabilityCriteria);

		final ProductSku productSku = createProductSku(Utils.uniqueCode(GUID), Utils.uniqueCode(SKU_CODE), startDate, Collections.emptyMap(),
				Collections.emptyMap());
		productSku.setProduct(productBundle);

		final Map<String, ProductSku> productSkus = new HashMap<>();
		productSkus.put(productSku.getSkuCode(), productSku);

		productBundle.setProductSkus(productSkus);

		return doInTransaction(status -> persist(productBundle));
	}

	@SuppressWarnings({"PMD.ExcessiveParameterList", "checkstyle:parameternumber"})
	private ProductBundle createProductBundle(final String code, final ProductType productType, final List<Category> categories, final Brand brand,
											  final List<Product> products, final boolean hidden, final Date startDate, final Date endDate,
											  final AvailabilityCriteria availabilityCriteria) {
		final ProductBundle productBundle = new ProductBundleImpl();
		products.stream().map(this::createBundleConstituent).forEach(productBundle::addConstituent);
		productBundle.setCode(code);
		productBundle.setProductType(productType);
		categories.forEach(productBundle::addCategory);
		productBundle.setBrand(brand);
		productBundle.setHidden(hidden);
		productBundle.setStartDate(startDate);
		productBundle.setEndDate(endDate);
		productBundle.setLastModifiedDate(startDate);
		productBundle.setAvailabilityCriteria(availabilityCriteria);
		productBundle.setCalculated(true);

		return productBundle;
	}

	private ProductSku createProductSku(final String guid, final String skuCode, final Date startDate,
										final Map<String, SkuOptionValue> optionValueMap, final Map<String, AttributeValue> attributeValueMap) {
		final ProductSku productSku = new ProductSkuImpl();
		productSku.setGuid(guid);
		productSku.setSkuCode(skuCode);
		productSku.setStartDate(startDate);
		productSku.setOptionValueMap(optionValueMap);
		productSku.setAttributeValueMap(attributeValueMap);

		return productSku;
	}

	private BundleConstituent createBundleConstituent(final Product product) {
		final BundleConstituent constituent = new BundleConstituentImpl();
		constituent.setGuid(Utils.uniqueCode(GUID));
		constituent.setConstituent(product);
		constituent.setQuantity(1);

		return constituent;
	}

	private Brand createAndPersistBrand(final String code, final String displayName, final Catalog catalog) {
		final Brand brand = new BrandImpl();
		brand.setCode(code);
		brand.setCatalog(catalog);

		final BrandLocalizedPropertyValueImpl localizedPropertyValue = new BrandLocalizedPropertyValueImpl();
		localizedPropertyValue.setLocalizedPropertyKey(displayName + "_" + catalog.getDefaultLocale());
		localizedPropertyValue.setValue(displayName + "_" + catalog.getDefaultLocale() + ":" + displayName);

		final Map<String, LocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();
		localizedPropertiesMap.put(displayName + "_" + catalog.getDefaultLocale(), localizedPropertyValue);

		brand.setLocalizedPropertiesMap(localizedPropertiesMap);

		return doInTransaction(status -> persist(brand));
	}

	private <T extends Persistable> T persist(final T entity) {
		return getPersistenceEngine().saveOrUpdate(entity);
	}

}
