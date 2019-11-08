package com.elasticpath.test.integration;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.DataFormat;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.common.pricing.service.PriceListHelperService;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.CategoryGuidUtil;
import com.elasticpath.core.messaging.domain.DomainEventType;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.CategoryType;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.impl.BrandImpl;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionImpl;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionValueImpl;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.impl.DomainEventListener;
import com.elasticpath.service.catalog.BrandService;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.catalog.CategoryTypeService;
import com.elasticpath.service.catalog.SkuOptionService;
import com.elasticpath.service.catalog.impl.ProductServiceImpl;
import com.elasticpath.test.persister.TaxTestPersister;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * Tests that the {@link DomainEventListener} correctly handles changes in database and sends event messages to message queue.
 */
public class DomainEventListenerIntegrationTest extends BasicSpringContextTest {

	private static final String DOMAIN_MESSAGING_CAMEL_CONTEXT = "ep-domain-messaging";
	private static final String MOCK_DOMAIN_ENDPOINT = "mock:domain/events";

	private NotifyBuilder notifyBuilder;

	private SkuOption skuOption;

	private Brand brand;

	private Catalog catalog;

	private CategoryType categoryType;

	private Warehouse warehouse;

	private Category category;

	private TestDataPersisterFactory testDataPersisterFactory;

	@Autowired
	private SkuOptionService skuOptionService;

	@Autowired
	private BrandService brandService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private EventMessageFactory eventMessageFactory;

	@Autowired
	@Qualifier(DOMAIN_MESSAGING_CAMEL_CONTEXT)
	private CamelContext camelContext;

	@EndpointInject(uri = MOCK_DOMAIN_ENDPOINT, context = DOMAIN_MESSAGING_CAMEL_CONTEXT)
	private MockEndpoint mockDomainEventEndpoint;

	@EndpointInject(ref = "epDomainMessagingDomainEventExternalEndpoint")
	private Endpoint domainEventOutgoingEndpoint;

	@Autowired
	protected DataFormat eventMessageDataFormat;

	@Autowired
	protected CategoryTypeService categoryTypeService;

	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	@Qualifier("productServiceTarget")
	private ProductServiceImpl productService;

	@Before
	public void setUp() throws Exception {
		camelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(domainEventOutgoingEndpoint)
						.unmarshal(eventMessageDataFormat)
						.to(MOCK_DOMAIN_ENDPOINT);
			}
		});

		final SimpleStoreScenario scenario = getTac().useScenario(SimpleStoreScenario.class);
		catalog = scenario.getCatalog();
		skuOption = createSkuOption(catalog);
		brand = createBrand(catalog);
		categoryType = persistCategoryType("categoryType", catalog);
		warehouse = scenario.getWarehouse();
		category = scenario.getCategory();
		testDataPersisterFactory = getTac().getPersistersFactory();

		clearEndpoint();
	}


	/**
	 * Verify, that message with DomainEventType.SKU_OPTION_CREATED is sent to camel endpoint after SkuOption was persisted.
	 *
	 * @throws InterruptedException on failure
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterPersistSkuOption() throws InterruptedException {
		notifyBuilder = new NotifyBuilder(camelContext)
				.whenDone(1)
				.wereSentTo(MOCK_DOMAIN_ENDPOINT)
				.create();

		skuOptionService.add(skuOption);

		assertTrue(notifyBuilder.matches(1, TimeUnit.SECONDS));

		mockDomainEventEndpoint.message(0)
				.body(EventMessage.class)
				.isEqualTo(eventMessageFactory.createEventMessage(DomainEventType.SKU_OPTION_CREATED, skuOption.getGuid(), null));
		mockDomainEventEndpoint.assertIsSatisfied();
	}

	/**
	 * Verify, that message with DomainEventType.SKU_OPTION_UPDATED is sent to camel endpoint after SkuOption was updated.
	 *
	 * @throws InterruptedException on failure
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterAttachSkuOption() throws InterruptedException {
		notifyBuilder = new NotifyBuilder(camelContext)
				.whenDone(2)
				.wereSentTo(MOCK_DOMAIN_ENDPOINT)
				.create();

		skuOptionService.add(skuOption);

		final SkuOption updatedSkuOption = skuOptionService.get(skuOption.getUidPk());
		updatedSkuOption.setOptionKey("updatedOptionKey");

		clearEndpoint();

		skuOptionService.update(updatedSkuOption);

		assertTrue(notifyBuilder.matches(1, TimeUnit.SECONDS));

		mockDomainEventEndpoint.message(0)
				.body(EventMessage.class)
				.isEqualTo(eventMessageFactory.createEventMessage(DomainEventType.SKU_OPTION_UPDATED, updatedSkuOption.getGuid(), null));
		mockDomainEventEndpoint.assertIsSatisfied();
	}

	/**
	 * Verify, that message with DomainEventType.SKU_OPTION_DELETED is sent to camel endpoint after SkuOption was deleted.
	 *
	 * @throws InterruptedException on failure
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterDeleteSkuOption() throws InterruptedException {
		notifyBuilder = new NotifyBuilder(camelContext)
				.whenDone(2)
				.wereSentTo(MOCK_DOMAIN_ENDPOINT)
				.create();

		skuOptionService.add(skuOption);
		clearEndpoint();

		skuOptionService.remove(skuOption);

		assertTrue(notifyBuilder.matches(3, TimeUnit.SECONDS));

		mockDomainEventEndpoint.message(0)
				.body(EventMessage.class)
				.isEqualTo(eventMessageFactory.createEventMessage(DomainEventType.SKU_OPTION_DELETED, skuOption.getGuid(), null));
		mockDomainEventEndpoint.assertIsSatisfied();
	}

	/**
	 * Verify, that message with DomainEventType.CATEGORY_UPDATED is sent to camel endpoint after Category was reordered.
	 *
	 * @throws InterruptedException on failure
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterReorderingCategoryChildren() throws InterruptedException {
		notifyBuilder = new NotifyBuilder(camelContext)
				.whenDone(4)
				.wereSentTo(MOCK_DOMAIN_ENDPOINT)
				.create();

		final Category parent = createMasterCategory("parent", catalog, categoryType, null, 1);
		categoryService.saveOrUpdate(parent);

		final Category child1 = createMasterCategory("child1", catalog, categoryType, parent, 1);
		categoryService.saveOrUpdate(child1);
		categoryService.saveOrUpdate(createMasterCategory("child2", catalog, categoryType, parent, 0));

		clearEndpoint();
		categoryService.updateCategoryOrderUp(child1);

		assertTrue(notifyBuilder.matches(3, TimeUnit.SECONDS));

		mockDomainEventEndpoint.message(0)
				.body(EventMessage.class)
				.isEqualTo(eventMessageFactory.createEventMessage(DomainEventType.CATEGORY_UPDATED, child1.getCompoundGuid(), null));
		mockDomainEventEndpoint.assertIsSatisfied();
	}

	/**
	 * Verify, that message with DomainEventType.CATEGORY_CREATED is sent to camel endpoint after Category was persisted.
	 *
	 * @throws InterruptedException on failure
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterPersistCategory() throws InterruptedException {
		notifyBuilder = new NotifyBuilder(camelContext)
				.whenDone(1)
				.wereSentTo(MOCK_DOMAIN_ENDPOINT)
				.create();
		clearEndpoint();
		final Category category = createMasterCategory("category", catalog, categoryType, null, 0);
		categoryService.add(category);

		assertTrue(notifyBuilder.matches(1, TimeUnit.SECONDS));

		mockDomainEventEndpoint.message(0)
				.body(EventMessage.class)
				.isEqualTo(eventMessageFactory.createEventMessage(DomainEventType.CATEGORY_CREATED, category.getCompoundGuid(), null));
		mockDomainEventEndpoint.assertIsSatisfied();
	}

	/**
	 * Verify, that message with DomainEventType.CATEGORY_DELETED is sent to camel endpoint after Category was deleted.
	 *
	 * @throws InterruptedException on failure
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterDeleteCategory() throws InterruptedException {
		notifyBuilder = new NotifyBuilder(camelContext)
				.whenDone(2)
				.wereSentTo(MOCK_DOMAIN_ENDPOINT)
				.create();
		final Category category = createMasterCategory("category", catalog, categoryType, null, 0);
		categoryService.add(category);
		clearEndpoint();

		categoryService.removeCategoryTree(category.getUidPk());

		assertTrue(notifyBuilder.matches(3, TimeUnit.SECONDS));

		mockDomainEventEndpoint.message(0)
				.body(EventMessage.class)
				.isEqualTo(eventMessageFactory.createEventMessage(DomainEventType.CATEGORY_DELETED, category.getCompoundGuid(), null));
		mockDomainEventEndpoint.assertIsSatisfied();
	}

	/**
	 * Verify, that message with DomainEventType.BRAND_DELETED is sent to camel endpoint after Brand was deleted.
	 *
	 * @throws InterruptedException on failure
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterDeleteBrand() throws InterruptedException {
		notifyBuilder = new NotifyBuilder(camelContext)
				.whenDone(2)
				.wereSentTo(MOCK_DOMAIN_ENDPOINT)
				.create();

		brandService.add(brand);
		clearEndpoint();

		brandService.remove(brand);

		assertTrue(notifyBuilder.matches(3, TimeUnit.SECONDS));

		mockDomainEventEndpoint.message(0)
				.body(EventMessage.class)
				.isEqualTo(eventMessageFactory.createEventMessage(DomainEventType.BRAND_DELETED, brand.getGuid(), null));
		mockDomainEventEndpoint.assertIsSatisfied();
	}

	/**
	 * Verify, that message with DomainEventType.BRAND_CREATED is sent to camel endpoint after Brand was persisted.
	 *
	 * @throws InterruptedException on failure
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterPersistBrand() throws InterruptedException {
		notifyBuilder = new NotifyBuilder(camelContext)
				.whenDone(1)
				.wereSentTo(MOCK_DOMAIN_ENDPOINT)
				.create();
		clearEndpoint();

		brandService.add(brand);

		assertTrue(notifyBuilder.matches(1, TimeUnit.SECONDS));

		mockDomainEventEndpoint.message(0)
				.body(EventMessage.class)
				.isEqualTo(eventMessageFactory.createEventMessage(DomainEventType.BRAND_CREATED, brand.getGuid(), null));
		mockDomainEventEndpoint.assertIsSatisfied();
	}

	/**
	 * Verify, that message with DomainEventType.BRAND_UPDATED is sent to camel endpoint after Brand was updated.
	 *
	 * @throws InterruptedException on failure
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterAttachBrand() throws InterruptedException {
		notifyBuilder = new NotifyBuilder(camelContext)
				.whenDone(2)
				.wereSentTo(MOCK_DOMAIN_ENDPOINT)
				.create();

		brandService.add(brand);
		final Brand updatedBrand = brandService.get(brand.getUidPk());
		updatedBrand.setImageUrl("url");
		clearEndpoint();

		brandService.saveOrUpdate(updatedBrand);

		assertTrue(notifyBuilder.matches(1, TimeUnit.SECONDS));

		mockDomainEventEndpoint.message(0)
				.body(EventMessage.class)
				.isEqualTo(eventMessageFactory.createEventMessage(DomainEventType.BRAND_UPDATED, updatedBrand.getGuid(), null));
		mockDomainEventEndpoint.assertIsSatisfied();
	}

	/**
	 * Verify, that message with DomainEventType.PRODUCT_UPDATED is sent to camel endpoint after Product was updated.
	 *
	 * @throws InterruptedException on failure
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsTwoMessageQueueAfterUpdatingFeaturedProductListInProduct() throws InterruptedException {
		notifyBuilder = new NotifyBuilder(camelContext)
				.whenDone(1)
				.wereSentTo(MOCK_DOMAIN_ENDPOINT)
				.create();

		final Product product = persistProductWithSku();

		clearEndpoint();

		productService.refreshProductCategoryFeaturedField(product.getUidPk(), category.getUidPk(), 1);

		assertTrue(notifyBuilder.matches(3, TimeUnit.SECONDS));

		mockDomainEventEndpoint.message(0)
				.body(EventMessage.class)
				.isEqualTo(eventMessageFactory.createEventMessage(DomainEventType.PRODUCT_UPDATED, product.getGuid(), null));
		mockDomainEventEndpoint.assertIsSatisfied();
	}

	private void clearEndpoint() {
		try {
			Thread.sleep(1000);
			mockDomainEventEndpoint.reset();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Product persistProductWithSku() {
		TaxCode taxCode = testDataPersisterFactory.getTaxTestPersister().getTaxCode(TaxTestPersister.TAX_CODE_GOODS);
		final PriceListHelperService priceListHelperService = getBeanFactory().getSingletonBean(ContextIdNames.PRICE_LIST_HELPER_SERVICE,
				PriceListHelperService.class);
		Currency currency = priceListHelperService.getDefaultCurrencyFor(catalog);
		int orderLimit = Integer.MAX_VALUE;

		return testDataPersisterFactory.getCatalogTestPersister().persistProductWithSku(
				catalog,
				category,
				warehouse,
				BigDecimal.TEN,
				currency,
				"brandCode",
				"productCode",
				"productName",
				"skuCode",
				taxCode.getCode(),
				AvailabilityCriteria.ALWAYS_AVAILABLE,
				orderLimit);
	}

	public CategoryType persistCategoryType(final String categoryTypeName, final Catalog catalog) {
		final CategoryType categoryType = beanFactory.getBean(ContextIdNames.CATEGORY_TYPE);
		categoryType.setCatalog(catalog);
		categoryType.setName(categoryTypeName);
		categoryType.setGuid(categoryTypeName);
		return categoryTypeService.add(categoryType);
	}

	public Category createMasterCategory(final String categoryCode, final Catalog catalog, final CategoryType categoryType,
										 final Category parentCategory, final int ordering) {
		final CategoryGuidUtil guidUtil = new CategoryGuidUtil();
		final Category category = beanFactory.getBean(ContextIdNames.CATEGORY);
		category.setGuid(guidUtil.get(categoryCode, catalog.getCode()));
		category.setCode(categoryCode);
		category.setCatalog(catalog);
		category.setCategoryType(categoryType);
		category.setParent(parentCategory);
		category.setOrdering(ordering);
		return category;
	}

	private SkuOption createSkuOption(final Catalog catalog) {
		final SkuOption newSkuOption = new SkuOptionImpl();
		populateSkuOption(catalog, newSkuOption, "skuOptionName");

		final SkuOptionValue optionValue = new SkuOptionValueImpl();
		optionValue.setOptionValueKey("option1");
		newSkuOption.addOptionValue(optionValue);

		return newSkuOption;
	}

	private void populateSkuOption(final Catalog catalog, final SkuOption skuOption, final String name) {
		skuOption.setCatalog(catalog);
		skuOption.setOptionKey(name);
		skuOption.setDisplayName(name, Locale.ENGLISH);
	}

	private Brand createBrand(final Catalog catalog) {
		final Brand brand = new BrandImpl();
		brand.setCatalog(catalog);
		brand.setCode("code");
		((BrandImpl) brand).setDefaultValues();
		return brand;
	}

}
