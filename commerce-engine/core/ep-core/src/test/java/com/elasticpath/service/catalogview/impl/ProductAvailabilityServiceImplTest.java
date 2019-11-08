/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.catalogview.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.common.dto.SkuInventoryDetails;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.catalog.impl.CategoryImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.catalog.impl.ProductTypeImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.test.BeanFactoryExpectationsFactory;

public class ProductAvailabilityServiceImplTest {

	@Rule
	public JUnitRuleMockery context = new JUnitRuleMockery();

	private static final long BIG_POSITIVE_MILLIS_OFFSET = 999999999L;

	private ProductAvailabilityServiceImpl productAvailabilityService;
	private BeanFactoryExpectationsFactory bfef;
	private BeanFactory beanFactory;
	private TimeService timeService;
	private ProductSku sku;
	private ProductSku sku2;
	private ProductSku singleSku;
	private Product multiSkuProduct;
	private Product singleSkuProduct;
	private Catalog catalog;
	private Store store;
	private Category category;
	private Date currentTime;
	private ProductType multiSkuProductType;
	private ProductType singleSkuProductType;

	@Before
	public void setUp() {
		timeService = context.mock(TimeService.class);
		beanFactory = context.mock(BeanFactory.class);
		bfef = new BeanFactoryExpectationsFactory(context, beanFactory);

		currentTime = new Date(System.currentTimeMillis() + BIG_POSITIVE_MILLIS_OFFSET);

		catalog = new CatalogImpl();
		catalog.setMaster(true);

		store = new StoreImpl();
		store.setCatalog(catalog);

		category = new CategoryImpl();
		category.setCatalog(catalog);

		sku = new ProductSkuImpl();
		sku.setSkuCode("sku1");

		sku2 = new ProductSkuImpl();
		sku2.setSkuCode("sku2");

		singleSku = new ProductSkuImpl();
		singleSku.setSkuCode("singleSku");

		multiSkuProductType = new ProductTypeImpl();
		multiSkuProductType.setMultiSku(true);

		multiSkuProduct = new ProductImpl();
		multiSkuProduct.addOrUpdateSku(sku);
		multiSkuProduct.addOrUpdateSku(sku2);
		multiSkuProduct.setProductType(multiSkuProductType);

		singleSkuProductType = new ProductTypeImpl();
		singleSkuProductType.setMultiSku(false);

		singleSkuProduct = new ProductImpl();
		singleSkuProduct.addOrUpdateSku(singleSku);
		singleSkuProduct.setProductType(singleSkuProductType);

		context.checking(new Expectations() { {
			allowing(timeService).getCurrentTime();
			will(returnValue(currentTime));
		} });

		productAvailabilityService = new ProductAvailabilityServiceImpl();
		productAvailabilityService.setTimeService(timeService);
	}

	@After
	public void tearDown() {
		bfef.close();
	}

	@Test
	public void testIsSkuAvailableHappyPath() {
		sku.setStartDate(new Date(currentTime.getTime() - 1));
		sku2.setEndDate(new Date(currentTime.getTime() - 1));
		multiSkuProduct.setStartDate(new Date(currentTime.getTime() - 1));

		SkuInventoryDetails inventoryDetails = new SkuInventoryDetails();
		inventoryDetails.setHasSufficientUnallocatedQty(true);
		inventoryDetails.setAvailableQuantityInStock(1);

		assertTrue(productAvailabilityService.isSkuAvailable(multiSkuProduct, sku, inventoryDetails));
	}

	@Test
	public void testIsSkuAvailableWithSkuDateBoundaryMiss() {
		sku.setStartDate(new Date(currentTime.getTime() - 1));
		sku.setEndDate(new Date(currentTime.getTime() - 1));
		sku2.setStartDate(new Date(currentTime.getTime() - 1));
		multiSkuProduct.setStartDate(new Date(currentTime.getTime() - 1));

		SkuInventoryDetails inventoryDetails = new SkuInventoryDetails();
		inventoryDetails.setHasSufficientUnallocatedQty(true);
		inventoryDetails.setAvailableQuantityInStock(1);

		assertFalse(productAvailabilityService.isSkuAvailable(multiSkuProduct, sku, inventoryDetails));
	}

	@Test
	public void testSingleSkuSsAvailableWithSkuDateBoundaryMiss() {
		singleSku.setStartDate(new Date(currentTime.getTime() - 2));
		singleSku.setEndDate(new Date(currentTime.getTime() - 1));
		singleSkuProduct.setStartDate(new Date(currentTime.getTime() - 2));

		SkuInventoryDetails inventoryDetails = new SkuInventoryDetails();
		inventoryDetails.setHasSufficientUnallocatedQty(true);
		inventoryDetails.setAvailableQuantityInStock(1);

		assertTrue(productAvailabilityService.isSkuAvailable(singleSkuProduct, singleSku, inventoryDetails));
	}

	@Test
	public void testIsSkuDisplayableWithAvailableSku() {
		store.setDisplayOutOfStock(false);

		sku.setStartDate(new Date(currentTime.getTime() - 1));
		sku2.setStartDate(new Date(currentTime.getTime() - 1));
		sku2.setEndDate(new Date(currentTime.getTime() - 1));
		multiSkuProduct.setStartDate(new Date(currentTime.getTime() - 1));
		multiSkuProduct.addCategory(category);

		SkuInventoryDetails inventoryDetails = new SkuInventoryDetails();
		inventoryDetails.setHasSufficientUnallocatedQty(true);
		inventoryDetails.setAvailableQuantityInStock(1);
		inventoryDetails.setAvailabilityCriteria(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
		store.setDisplayOutOfStock(false);

		assertTrue(productAvailabilityService.isSkuDisplayable(multiSkuProduct, sku, store, inventoryDetails));
	}

	@Test
	public void testIsSkuNotDisplayableWithSkuOutOfDateRange() {
		store.setDisplayOutOfStock(false);

		sku.setStartDate(new Date(currentTime.getTime() - 1));
		sku.setEndDate(new Date(currentTime.getTime() - 1));
		sku2.setStartDate(new Date(currentTime.getTime() - 1));
		multiSkuProduct.setStartDate(new Date(currentTime.getTime() - 1));
		multiSkuProduct.addCategory(category);

		SkuInventoryDetails inventoryDetails = new SkuInventoryDetails();
		inventoryDetails.setHasSufficientUnallocatedQty(true);
		inventoryDetails.setAvailableQuantityInStock(1);
		inventoryDetails.setAvailabilityCriteria(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
		store.setDisplayOutOfStock(false);

		assertFalse(productAvailabilityService.isSkuDisplayable(multiSkuProduct, sku, store, inventoryDetails));
	}

	/**
	 * Tests {@link com.elasticpath.service.catalogview.ProductAvailabilityService#isSkuDisplayable(com.elasticpath.domain.catalog.Product,
	 * com.elasticpath.domain.catalog.ProductSku, com.elasticpath.domain.store.Store, com.elasticpath.common.dto.SkuInventoryDetails)} if it returns
	 * true when product is always available.
	 */
	@Test
	public void testIsProductDisplayableReturnsTrueWhenProductIsAlwaysAvailable() {
		// given
		SkuInventoryDetails inventoryDetails = new SkuInventoryDetails();
		inventoryDetails.setAvailabilityCriteria(AvailabilityCriteria.ALWAYS_AVAILABLE);
		store.setDisplayOutOfStock(false);

		Product product = createSingleSkuProduct();
		product.addCategory(category);

		// test
		assertTrue(productAvailabilityService.isSkuDisplayable(product, product.getDefaultSku(), store, inventoryDetails));
	}

	/**
	 * Tests {@link com.elasticpath.service.catalogview.ProductAvailabilityService#isSkuDisplayable(com.elasticpath.domain.catalog.Product,
	 * com.elasticpath.domain.catalog.ProductSku, com.elasticpath.domain.store.Store, com.elasticpath.common.dto.SkuInventoryDetails)} if it returns
	 * false when product is out of stock and store hides out of stock items.
	 */
	@Test
	public void testIsProductDisplayableReturnsFalseWhenProductIsOutOfStockAndStoreHidesOutOfStockItems() {
		// given
		SkuInventoryDetails inventoryDetails = new SkuInventoryDetails();
		inventoryDetails.setAvailabilityCriteria(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
		store.setDisplayOutOfStock(false);
		Product product = createSingleSkuProduct();

		// test
		assertFalse(productAvailabilityService.isSkuDisplayable(product, product.getDefaultSku(), store, inventoryDetails));
	}

	/**
	 * Tests {@link com.elasticpath.service.catalogview.ProductAvailabilityService#isSkuDisplayable(com.elasticpath.domain.catalog.Product,
	 * com.elasticpath.domain.catalog.ProductSku, com.elasticpath.domain.store.Store, com.elasticpath.common.dto.SkuInventoryDetails)} if it returns
	 * true when product is out of stock and store displays out of stock items.
	 */
	@Test
	public void testIsProductDisplayableReturnsFalseWhenProductIsOutOfStockAndStoreDisplaysOutOfStockItems() {
		// given
		SkuInventoryDetails inventoryDetails = new SkuInventoryDetails();
		inventoryDetails.setAvailabilityCriteria(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
		store.setDisplayOutOfStock(true);

		Product product = createSingleSkuProduct();
		product.addCategory(category);

		// test
		assertTrue(productAvailabilityService.isSkuDisplayable(product, product.getDefaultSku(), store, inventoryDetails));
	}


	/**
	 * Tests {@link com.elasticpath.service.catalogview.ProductAvailabilityService#isSkuDisplayable(com.elasticpath.domain.catalog.Product,
	 * com.elasticpath.domain.catalog.ProductSku, com.elasticpath.domain.store.Store, com.elasticpath.common.dto.SkuInventoryDetails)} if it returns
	 * true when product is in pre-order state.
	 */
	@Test
	public void testIsProductDisplayableReturnsTrueWhenProductIsInPreOrderState() {
		// given
		SkuInventoryDetails inventoryDetails = new SkuInventoryDetails();
		inventoryDetails.setAvailabilityCriteria(AvailabilityCriteria.AVAILABLE_FOR_PRE_ORDER);
		store.setDisplayOutOfStock(false);

		Product product = createSingleSkuProduct();
		product.addCategory(category);

		// test
		assertTrue(productAvailabilityService.isSkuDisplayable(product, product.getDefaultSku(), store, inventoryDetails));
	}

	/**
	 * Tests {@link com.elasticpath.service.catalogview.ProductAvailabilityService#isSkuDisplayable(com.elasticpath.domain.catalog.Product,
	 * com.elasticpath.domain.catalog.ProductSku, com.elasticpath.domain.store.Store, com.elasticpath.common.dto.SkuInventoryDetails)} if it returns
	 * true when product is in back-order state.
	 */
	@Test
	public void testIsProductDisplayableReturnsTrueWhenProductIsInBackOrderState() {
		// given
		SkuInventoryDetails inventoryDetails = new SkuInventoryDetails();
		inventoryDetails.setAvailabilityCriteria(AvailabilityCriteria.AVAILABLE_FOR_BACK_ORDER);
		store.setDisplayOutOfStock(false);

		Product product = createSingleSkuProduct();
		product.addCategory(category);

		// test
		assertTrue(productAvailabilityService.isSkuDisplayable(product, product.getDefaultSku(), store, inventoryDetails));
	}

	/**
	 * Tests {@link com.elasticpath.service.catalogview.ProductAvailabilityService#canProductSyndicate(com.elasticpath.domain.catalog.Product)} if it
	 * returns true when product can be syndicated.
	 */
	@Test
	public void testIsProductCanBeSyndicatedIfItNotHiddenAndEndDateAfterCurrentTime() {
		// given
		Product product = createSingleSkuProduct();
		product.addCategory(category);
		product.setHidden(false);
		product.setEndDate(new Date(currentTime.getTime() + 1L));

		// test
		assertTrue(productAvailabilityService.canProductSyndicate(product));
	}

	/**
	 * Tests {@link com.elasticpath.service.catalogview.ProductAvailabilityService#canProductSyndicate(com.elasticpath.domain.catalog.Product)} if it
	 * returns false when product can not be syndicated.
	 */
	@Test
	public void testIsProductCannotBeSyndicatedIfItHidden() {
		// given
		Product product = createSingleSkuProduct();
		product.addCategory(category);
		product.setHidden(true);
		product.setEndDate(new Date(currentTime.getTime() + 1L));

		// test
		assertFalse(productAvailabilityService.canProductSyndicate(product));
	}

	/**
	 * Tests {@link com.elasticpath.service.catalogview.ProductAvailabilityService#canProductSyndicate(com.elasticpath.domain.catalog.Product)} if it
	 * returns false when product can not be syndicated.
	 */
	@Test
	public void testIsProductCannotBeSyndicatedIfItNotHiddenAndEndDateBeforeCurrentTime() {
		// given
		Product product = createSingleSkuProduct();
		product.addCategory(category);
		product.setHidden(false);
		product.setEndDate(new Date(currentTime.getTime() - 1L));

		// test
		assertFalse(productAvailabilityService.canProductSyndicate(product));
	}

	/**
	 * Tests {@link com.elasticpath.service.catalogview.ProductAvailabilityService#canSkuSyndicate(com.elasticpath.domain.catalog.ProductSku)} if it
	 * returns true when ProductSku can be syndicated.
	 */
	@Test
	public void testIsProductCanBeSyndicatedIfEndDateAfterCurrentTime() {
		// given
		ProductSku productSku = new ProductSkuImpl();
		productSku.setEndDate(new Date(currentTime.getTime() + 1L));

		// test
		assertTrue(productAvailabilityService.canSkuSyndicate(productSku));
	}

	/**
	 * Tests {@link com.elasticpath.service.catalogview.ProductAvailabilityService#canSkuSyndicate(com.elasticpath.domain.catalog.ProductSku)} if it
	 * returns false when ProductSku can not be syndicated.
	 */
	@Test
	public void testIsProductCannotBeSyndicatedIfEndDateBeforeCurrentTime() {
		// given
		ProductSku productSku = new ProductSkuImpl();
		productSku.setEndDate(new Date(currentTime.getTime() - 1L));

		// test
		assertFalse(productAvailabilityService.canSkuSyndicate(productSku));
	}

	private Product createSingleSkuProduct() {
		ProductSkuImpl sku = new ProductSkuImpl();
		sku.setSkuCode("SINGLE_SKU");

		Product product = new ProductImpl();
		product.setStartDate(new Date(currentTime.getTime() - 1L));
		product.addOrUpdateSku(sku);

		return product;
	}
}
