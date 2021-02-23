package com.elasticpath.test.integration;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
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
import com.elasticpath.domain.messaging.OutboxMessage;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionImpl;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionValueImpl;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.camel.jackson.EventMessageObjectMapper;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.impl.DomainEventListener;
import com.elasticpath.service.catalog.BrandService;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.catalog.CategoryTypeService;
import com.elasticpath.service.catalog.SkuOptionService;
import com.elasticpath.service.catalog.impl.ProductServiceImpl;
import com.elasticpath.service.messaging.OutboxMessageService;
import com.elasticpath.test.persister.TaxTestPersister;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * Tests that the {@link DomainEventListener} correctly handles changes in database and sends event messages to message queue.
 */
public class DomainEventListenerIntegrationTest extends BasicSpringContextTest {

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
	private EventMessageObjectMapper eventMessageObjectMapper;

	@Autowired
	private CategoryTypeService categoryTypeService;

	@Autowired
	private OutboxMessageService outboxMessageService;

	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	@Qualifier("productServiceTarget")
	private ProductServiceImpl productService;

	@Before
	public void setUp() throws Exception {
		final SimpleStoreScenario scenario = getTac().useScenario(SimpleStoreScenario.class);
		catalog = scenario.getCatalog();
		skuOption = createSkuOption(catalog);
		brand = createBrand(catalog);
		categoryType = persistCategoryType("categoryType", catalog);
		warehouse = scenario.getWarehouse();
		category = scenario.getCategory();
		testDataPersisterFactory = getTac().getPersistersFactory();
	}

	/**
	 * Verify, that message with DomainEventType.SKU_OPTION_CREATED is sent to camel endpoint after SkuOption was persisted.
	 *
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterPersistSkuOption() throws JsonProcessingException {
		clearEventMessageOutbox();

		skuOptionService.add(skuOption);

		EventMessage expectedEventMessage = eventMessageFactory.createEventMessage(DomainEventType.SKU_OPTION_CREATED, skuOption.getGuid(), null);
		Assert.assertTrue(isEventMessageInOutbox(expectedEventMessage));
	}

	/**
	 * Verify, that message with DomainEventType.SKU_OPTION_UPDATED is sent to camel endpoint after SkuOption was updated.
	 *
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterAttachSkuOption() throws JsonProcessingException {
		skuOptionService.add(skuOption);

		final SkuOption updatedSkuOption = skuOptionService.get(skuOption.getUidPk());
		updatedSkuOption.setOptionKey("updatedOptionKey");

		clearEventMessageOutbox();

		skuOptionService.update(updatedSkuOption);

		EventMessage expectedEventMessage = eventMessageFactory.createEventMessage(DomainEventType.SKU_OPTION_UPDATED, updatedSkuOption.getGuid(), null);
		Assert.assertTrue(isEventMessageInOutbox(expectedEventMessage));
	}

	/**
	 * Verify, that message with DomainEventType.SKU_OPTION_DELETED is sent to camel endpoint after SkuOption was deleted.
	 *
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterDeleteSkuOption() throws JsonProcessingException {
		skuOptionService.add(skuOption);

		clearEventMessageOutbox();

		skuOptionService.remove(skuOption);

		EventMessage expectedEventMessage = eventMessageFactory.createEventMessage(DomainEventType.SKU_OPTION_DELETED, skuOption.getGuid(), null);
		Assert.assertTrue(isEventMessageInOutbox(expectedEventMessage));
	}

	/**
	 * Verify, that message with DomainEventType.CATEGORY_UPDATED is sent to camel endpoint after Category was reordered.
	 *
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterReorderingCategoryChildren() throws JsonProcessingException {
		final Category parent = createMasterCategory("parent", catalog, categoryType, null, 1);
		categoryService.saveOrUpdate(parent);

		final Category child1 = createMasterCategory("child1", catalog, categoryType, parent, 1);
		categoryService.saveOrUpdate(child1);
		categoryService.saveOrUpdate(createMasterCategory("child2", catalog, categoryType, parent, 0));

		clearEventMessageOutbox();

		categoryService.updateCategoryOrderUp(child1);

		EventMessage expectedEventMessage = eventMessageFactory.createEventMessage(DomainEventType.CATEGORY_UPDATED, child1.getCompoundGuid(), null);
		Assert.assertTrue(isEventMessageInOutbox(expectedEventMessage));
	}

	/**
	 * Verify, that message with DomainEventType.CATEGORY_CREATED is sent to camel endpoint after Category was persisted.
	 *
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterPersistCategory() throws JsonProcessingException {
		clearEventMessageOutbox();

		final Category category = createMasterCategory("category", catalog, categoryType, null, 0);
		categoryService.add(category);

		EventMessage expectedEventMessage = eventMessageFactory.createEventMessage(DomainEventType.CATEGORY_CREATED, category.getCompoundGuid(), null);
		Assert.assertTrue(isEventMessageInOutbox(expectedEventMessage));
	}

	/**
	 * Verify, that message with DomainEventType.CATEGORY_DELETED is sent to camel endpoint after Category was deleted.
	 *
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterDeleteCategory() throws JsonProcessingException {
		final Category category = createMasterCategory("category", catalog, categoryType, null, 0);
		categoryService.add(category);

		clearEventMessageOutbox();

		categoryService.removeCategoryTree(category.getUidPk());

		EventMessage expectedEventMessage = eventMessageFactory.createEventMessage(DomainEventType.CATEGORY_DELETED, category.getCompoundGuid(), null);
		Assert.assertTrue(isEventMessageInOutbox(expectedEventMessage));
	}

	/**
	 * Verify, that message with DomainEventType.BRAND_DELETED is sent to camel endpoint after Brand was deleted.
	 *
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterDeleteBrand() throws JsonProcessingException {
		brandService.add(brand);

		clearEventMessageOutbox();

		brandService.remove(brand);

		EventMessage expectedEventMessage = eventMessageFactory.createEventMessage(DomainEventType.BRAND_DELETED, brand.getGuid(), null);
		Assert.assertTrue(isEventMessageInOutbox(expectedEventMessage));
	}

	/**
	 * Verify, that message with DomainEventType.BRAND_CREATED is sent to camel endpoint after Brand was persisted.
	 *
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterPersistBrand() throws JsonProcessingException {
		clearEventMessageOutbox();

		brandService.add(brand);

		EventMessage expectedEventMessage = eventMessageFactory.createEventMessage(DomainEventType.BRAND_CREATED, brand.getGuid(), null);
		Assert.assertTrue(isEventMessageInOutbox(expectedEventMessage));
	}

	/**
	 * Verify, that message with DomainEventType.BRAND_UPDATED is sent to camel endpoint after Brand was updated.
	 *
	 */
	@DirtiesDatabase
	@Test
	public void testThatDomainEventMessageSendsToMessageQueueAfterAttachBrand() throws JsonProcessingException {
		brandService.add(brand);

		clearEventMessageOutbox();

		final Brand updatedBrand = brandService.get(brand.getUidPk());
		updatedBrand.setImageUrl("url");
		brandService.saveOrUpdate(updatedBrand);

		EventMessage expectedEventMessage = eventMessageFactory.createEventMessage(DomainEventType.BRAND_UPDATED, updatedBrand.getGuid(), null);
		Assert.assertTrue(isEventMessageInOutbox(expectedEventMessage));
	}

	/**
	 * Verify, that message with DomainEventType.PRODUCT_UPDATED is sent to camel endpoint after Product was updated.
	 *
	 */
	@DirtiesDatabase
	@Test
	public void testDomainEventMessageAfterUpdatingFeaturedProductListInCategory() throws JsonProcessingException {
		final Product product = persistProductWithSku();

		clearEventMessageOutbox();

		productService.refreshProductCategoryFeaturedField(product.getUidPk(), category.getUidPk(), 1);

		EventMessage expectedEventMessage = eventMessageFactory.createEventMessage(DomainEventType.PRODUCT_UPDATED, product.getGuid(), null);
		Assert.assertTrue(isEventMessageInOutbox(expectedEventMessage));
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

	private CategoryType persistCategoryType(final String categoryTypeName, final Catalog catalog) {
		final CategoryType categoryType = beanFactory.getPrototypeBean(ContextIdNames.CATEGORY_TYPE, CategoryType.class);
		categoryType.setCatalog(catalog);
		categoryType.setName(categoryTypeName);
		categoryType.setGuid(categoryTypeName);
		return categoryTypeService.add(categoryType);
	}

	private Category createMasterCategory(final String categoryCode, final Catalog catalog, final CategoryType categoryType,
										 final Category parentCategory, final int ordering) {
		final CategoryGuidUtil guidUtil = new CategoryGuidUtil();
		final Category category = beanFactory.getPrototypeBean(ContextIdNames.CATEGORY, Category.class);
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

	private boolean isEventMessageInOutbox(final EventMessage expectedEventMessage) throws JsonProcessingException {
		String expectedEventMessageString = eventMessageObjectMapper.writeValueAsString(expectedEventMessage);
		List<OutboxMessage> messageOutboxList = outboxMessageService.list();
		return messageOutboxList.stream()
				.anyMatch(messageOutbox -> messageOutbox.getMessageBody().equals(expectedEventMessageString));
	}

	private void clearEventMessageOutbox() {
		outboxMessageService.list().forEach(outboxMessage -> outboxMessageService.remove(outboxMessage));
	}
}
