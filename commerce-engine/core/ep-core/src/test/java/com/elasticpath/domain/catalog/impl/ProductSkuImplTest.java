/*
 * Copyright (c) Elastic Path Software Inc., 2006
 *
 */
package com.elasticpath.domain.catalog.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.InventoryDetails;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.EpInvalidValueBindException;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.attribute.impl.AttributeValueGroupFactoryImpl;
import com.elasticpath.domain.attribute.impl.ExtAttributeValueFactoryTestImpl;
import com.elasticpath.domain.attribute.impl.ExtAttributeValueGroupFactoryTestImpl;
import com.elasticpath.domain.attribute.impl.ExtAttributeValueGroupTestImpl;
import com.elasticpath.domain.attribute.impl.ExtAttributeValueTestImpl;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.DigitalAsset;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.skuconfiguration.impl.JpaAdaptorOfSkuOptionValueImpl;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionImpl;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionValueImpl;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.domain.tax.impl.TaxCodeImpl;
import com.elasticpath.inventory.dao.InventoryDao;
import com.elasticpath.inventory.dao.InventoryJournalDao;
import com.elasticpath.inventory.domain.Inventory;
import com.elasticpath.inventory.domain.impl.InventoryImpl;
import com.elasticpath.inventory.impl.InventoryFacadeImpl;
import com.elasticpath.inventory.strategy.InventoryJournalRollup;
import com.elasticpath.inventory.strategy.InventoryStrategy;
import com.elasticpath.inventory.strategy.impl.InventoryJournalRollupImpl;
import com.elasticpath.inventory.strategy.impl.JournalingInventoryStrategy;
import com.elasticpath.service.catalog.ProductInventoryManagementService;
import com.elasticpath.service.catalog.ProductSkuService;
import com.elasticpath.service.catalog.impl.ProductInventoryManagementServiceImpl;

/**
 * Test <code>ProductSkuImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.GodClass" })
public class ProductSkuImplTest {

	private static final int QTY_500 = 500;

	private static final int QTY_4 = 4;

	private static final int QTY_3 = 3;

	private static final int QUANTITY_ON_HAND = 5;

	private static final int RESERVED_QUANTITY = 3;

	private static final int REORDER_MINIMUM = 2;

	private static final String SALES_TAX_CODE_BOOKS = "BOOKS";
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String JAN_1_2010 = "2010-01-01";
	private static final String JAN_2_2010 = "2010-01-02";
	private static final String MAR_1_2010 = "2010-03-01";
	private static final String JAN_3_2010 = "2010-01-03";
	private static final String JAN_1_2009 = "2009-01-01";

	private ProductSkuImpl productSkuImpl;

	@Mock
	private InventoryDao inventoryDao;

	@Mock
	private InventoryJournalDao inventoryJournalDao;

	@Mock
	private ProductSkuService productSkuService;

	private static final long WAREHOUSE_UID = 100L;

	@Mock
	private Product product;

	@Mock
	private BeanFactory beanFactory;

	/**
	 * Prepare for tests.
	 *
	 * @throws Exception in case of error happens
	 */
	@Before
	public void setUp() throws Exception {
		when(product.hasMultipleSkus()).thenReturn(true);
		productSkuImpl = new ProductSkuImpl() {
			private static final long serialVersionUID = 1L;

			@Override
			public <T> T getBean(final String beanName) {
				return beanFactory.getBean(beanName);
			}
		};
		productSkuImpl.initialize();
		productSkuImpl.setProduct(product);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.productSkuImpl.getStartDate()'.
	 */
	@Test
	public void testGetStartDate() {
		// Start Date is supposed to have a default value .
		assertThat(productSkuImpl.getStartDate()).isNotNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.productSkuImpl.setStartDate(Date)'.
	 */
	@Test
	public void testSetStartDate() {
		final Date date = new Date();
		productSkuImpl.setStartDate(date);
		assertThat(productSkuImpl.getStartDate()).isSameAs(date);
	}


	/**
	 * Test method for 'com.elasticpath.domain.impl.productSkuImpl.getEndDate()'.
	 */
	@Test
	public void testGetEndDate() {
		assertThat(productSkuImpl.getEndDate()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.productSkuImpl.setEndDate(Date)'.
	 */
	@Test
	public void testSetEndDate() {
		final Date date = new Date();
		productSkuImpl.setEndDate(date);
		assertThat(productSkuImpl.getEndDate()).isSameAs(date);
	}


	/**
	 * Test method for 'com.elasticpath.domain.impl.ProductSkuImpl.getSkuCode()'.
	 */
	@Test
	public void testGetSetSkuCode() {
		final String testSku1 = "testSku1";
		productSkuImpl.setSkuCode(testSku1);
		assertThat(productSkuImpl.getSkuCode()).isSameAs(testSku1);

		final String testSku2 = "testSku2";
		productSkuImpl.setSkuCode(testSku2);
		assertThat(productSkuImpl.getSkuCode()).isSameAs(testSku2);
	}

	/** Test method that checks for sufficient quantity. */
	@Test
	public void testSufficientQuantity() {
		final Map<String, Inventory> inventoryMap = new HashMap<>();
		productSkuImpl.setProductInternal(product);
		final Inventory inventory = getInitializedInventory();
		inventoryMap.put(productSkuImpl.getSkuCode(), inventory);

		final Map<String, InventoryJournalRollup> rollupMap = new HashMap<>();
		rollupMap.put(productSkuImpl.getSkuCode(), new InventoryJournalRollupImpl());

		final int expectedMapCalls = 5;

		final Set<String> skuCodes = inventoryMap.keySet();

		when(inventoryDao.getInventoryMap(skuCodes, WAREHOUSE_UID)).thenReturn(inventoryMap);

		when(product.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);

		when(inventoryJournalDao.getInventoryRollupsForSkusInWarehouse(skuCodes, WAREHOUSE_UID)).thenReturn(rollupMap);

		assertThat(getProductInventoryManagementService().hasSufficientInventory(productSkuImpl, WAREHOUSE_UID, 1)).isTrue();
		assertThat(getProductInventoryManagementService().hasSufficientInventory(productSkuImpl, WAREHOUSE_UID, 2)).isTrue();
		assertThat(getProductInventoryManagementService().hasSufficientInventory(productSkuImpl, WAREHOUSE_UID, QTY_3)).isFalse();
		assertThat(getProductInventoryManagementService().hasSufficientInventory(productSkuImpl, WAREHOUSE_UID, QTY_4)).isFalse();
		assertThat(getProductInventoryManagementService().hasSufficientInventory(productSkuImpl, WAREHOUSE_UID, QTY_500)).isFalse();

		assertThatThrownBy(() -> getProductInventoryManagementService().hasSufficientInventory(productSkuImpl, WAREHOUSE_UID, 0))
			.isInstanceOf(EpDomainException.class);

		assertThatThrownBy(() -> getProductInventoryManagementService().hasSufficientInventory(productSkuImpl, WAREHOUSE_UID, -1))
			.isInstanceOf(EpDomainException.class);

		verify(inventoryDao, times(expectedMapCalls)).getInventoryMap(skuCodes, WAREHOUSE_UID);
	}

	/** Test method that checks for sufficient quantity. */
	@Test
	public void testSufficientQuantityWhenInfinite() {
		productSkuImpl.setProductInternal(product);

		when(product.getAvailabilityCriteria()).thenReturn(AvailabilityCriteria.ALWAYS_AVAILABLE);

		assertThat(getProductInventoryManagementService().hasSufficientInventory(productSkuImpl, WAREHOUSE_UID, QTY_500)).isTrue();

	}

	/**
	 * Returns an inventory object with these values:. QUANTITY_ON_HAND = 5; RESERVED_QUANTITY = 3; REORDER_MINIMUM = 2
	 *
	 * @return an initialized inventory object
	 */
	private Inventory getInitializedInventory() {
		Inventory inventory = new InventoryImpl();

		inventory.setSkuCode(productSkuImpl.getSkuCode());

		inventory.setReorderMinimum(REORDER_MINIMUM);
		inventory.setQuantityOnHand(QUANTITY_ON_HAND);
		inventory.setReservedQuantity(RESERVED_QUANTITY);
		productSkuImpl.getProduct().setAvailabilityCriteria(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
		inventory.setWarehouseUid(WAREHOUSE_UID);

		return inventory;
	}

	private ProductInventoryManagementService getProductInventoryManagementService() {
		ProductInventoryManagementServiceImpl productInventoryManagementService =
				new ProductInventoryManagementServiceImpl();
		InventoryCalculatorImpl inventoryCalculator = new InventoryCalculatorImpl() {
			@Override
			protected InventoryDetails createInventoryDetails() {
				return new InventoryDetails();
			}
		};
		productInventoryManagementService.setInventoryCalculator(inventoryCalculator);

		JournalingInventoryStrategy journalingInventoryStrategy = new JournalingInventoryStrategy();
		journalingInventoryStrategy.setInventoryDao(inventoryDao);
		journalingInventoryStrategy.setInventoryJournalDao(inventoryJournalDao);
		journalingInventoryStrategy.setProductSkuService(productSkuService);

		InventoryFacadeImpl inventoryFacade = new InventoryFacadeImpl();
		Map<String, InventoryStrategy> strategies = new HashMap<>();
		strategies.put("allocatedjournaling", journalingInventoryStrategy);
		inventoryFacade.setStrategies(strategies);
		inventoryFacade.selectStrategy("allocatedjournaling");

		productInventoryManagementService.setInventoryFacade(inventoryFacade);

		return productInventoryManagementService;
	}

	/** Test method setProduct(Product). */
	@Test
	public void testSetProduct() {
		Product product = new ProductImpl();
		productSkuImpl.setProduct(product);
		assertThat(productSkuImpl.getProduct()).isSameAs(product);
		assertThat(product.getProductSkus()).containsValue(productSkuImpl);
	}

	/** Test method setOptionValueMap(). */
	@Test
	public void testSetOptionValueMap() {
		final Map<String, SkuOptionValue> optionValueMap = new HashMap<>();
		final SkuOptionValueImpl skuOptionValueImpl = new SkuOptionValueImpl();
		optionValueMap.put("option_name", skuOptionValueImpl);
		productSkuImpl.setOptionValueMap(optionValueMap);
		assertThat(productSkuImpl.getOptionValueMap()).isSameAs(optionValueMap);
		assertThat(productSkuImpl.getOptionValues()).contains(skuOptionValueImpl);
	}

	/** Test method setSkuOptionValue(). */
	@Test
	public void testSetSkuOptionValue() {
		final SkuOptionValue skuOptionValue1 = new SkuOptionValueImpl();
		final String valueCode1 = "aaa";
		skuOptionValue1.setOptionValueKey(valueCode1);

		final SkuOptionValue skuOptionValue2 = new SkuOptionValueImpl();
		final String valueCode2 = "bbb";
		skuOptionValue2.setOptionValueKey(valueCode2);

		final SkuOption skuOption = new SkuOptionImpl();
		skuOption.addOptionValue(skuOptionValue1);
		skuOption.addOptionValue(skuOptionValue2);

		final JpaAdaptorOfSkuOptionValueImpl adaptor = new JpaAdaptorOfSkuOptionValueImpl();
		when(beanFactory.getBean(ContextIdNames.SKU_OPTION_VALUE_JPA_ADAPTOR)).thenReturn(adaptor);

		productSkuImpl.setSkuOptionValue(skuOption, valueCode1);
		assertThat(productSkuImpl.getSkuOptionValue(skuOption).getOptionValueKey()).isEqualTo(skuOptionValue1.getOptionValueKey());

		// Set sku option to another value
		productSkuImpl.setSkuOptionValue(skuOption, valueCode2);
		assertThat(productSkuImpl.getSkuOptionValue(skuOption).getOptionValueKey()).isEqualTo(skuOptionValue2.getOptionValueKey());

		// Set sku option to a undefined value
		assertThatThrownBy(() -> productSkuImpl.setSkuOptionValue(skuOption, "undefinedCode"))
			.isInstanceOf(EpInvalidValueBindException.class);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.ProductSkuImpl.getDigitalAsset()'.
	 */
	@Test
	public void testGetSetDigitalAssets() {

		final DigitalAsset digitalAsset = new DigitalAssetImpl();
		productSkuImpl.setDigital(true);
		assertThat(productSkuImpl.isDownloadable()).isFalse(); // Digital product can be without associated asset.
		productSkuImpl.setDigitalAsset(digitalAsset);
		assertThat(productSkuImpl.getDigitalAsset()).isSameAs(digitalAsset);
		assertThat(productSkuImpl.isDownloadable()).isTrue();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.productSkuImpl.setWidth(width)'.
	 */
	@Test
	public void testGetSetWidth() {
		final BigDecimal width = BigDecimal.TEN;
		this.productSkuImpl.setWidth(width);
		assertThat(productSkuImpl.getWidth()).isSameAs(width);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.productSkuImpl.setLength()'.
	 */
	@Test
	public void testGetSetLength() {
		final BigDecimal length = BigDecimal.TEN;
		this.productSkuImpl.setLength(length);
		assertThat(productSkuImpl.getLength()).isSameAs(length);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.productSkuImpl.setHeight()'.
	 */
	@Test
	public void testGetSetHeight() {
		final BigDecimal height = BigDecimal.TEN;
		this.productSkuImpl.setHeight(height);
		assertThat(productSkuImpl.getHeight()).isSameAs(height);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.productSkuImpl.setWeight()'.
	 */
	@Test
	public void testGetSetWeight() {
		final BigDecimal weight = BigDecimal.TEN;
		this.productSkuImpl.setWeight(weight);
		assertThat(productSkuImpl.getWeight()).isSameAs(weight);
	}

	/**
	 * Test that when the product sku's skuCode is changed, the product updates its map of skuCode-productSkus to match.
	 */
	@Test
	public void testChangeSkuCode() {
		Product product = new ProductImpl();
		final String skuCode1 = "testSkuCodeOne";
		final String skuCode2 = "testSkuCodeTwo";
		productSkuImpl.setSkuCode(skuCode1);
		productSkuImpl.setProduct(product);
		ProductSku skuInMap = productSkuImpl.getProduct().getProductSkus().get(skuCode1);
		assertThat(skuInMap)
			.as("The product sku map should have been updated with the new skuCode as a key.")
			.isNotNull();
		assertThat(skuInMap)
			.as("The product sku map should contain the updated sku with the new skuCode as its key.")
			.isSameAs(productSkuImpl);
		productSkuImpl.setSkuCode(skuCode2);
		// try to get the sku from the map using the OLD skuCode as the key
		skuInMap = productSkuImpl.getProduct().getProductSkus().get(skuCode1);
		assertThat(skuInMap)
			.as("The old skuCode-productSku entry in the map should have been removed.")
			.isNull();
		// try to get the sku from the map using the NEW skuCode as the key
		skuInMap = productSkuImpl.getProduct().getProductSkus().get(skuCode2);
		assertThat(skuInMap)
			.as("The product sku map should contain the updated sku with the new skuCode as its key.")
			.isSameAs(productSkuImpl);
	}



	/**
	 * Test that a new sku gets added to it's parent product's sku collection.
	 */
	@Test
	public void testProductBidirectionalRelationshipForNewSku() {
		final Product mockProduct = mock(Product.class, "mockProduct");
		when(mockProduct.getProductSkus()).thenReturn(Collections.emptyMap());
		productSkuImpl.setProduct(mockProduct);
		verify(mockProduct).getProductSkus();
		verify(mockProduct).addOrUpdateSku(productSkuImpl);
	}

	/**
	 * Test that a sku that is already in its parent product's sku collection doesn't get added again.
	 */
	@Test
	public void testProductBidirectionalRelationshipForExistingSku() {
		final Product mockProduct = mock(Product.class, "mockProduct");
		final Map<String, ProductSku> skus = new HashMap<>();
		skus.put(productSkuImpl.getGuid(), productSkuImpl);
		when(mockProduct.getProductSkus()).thenReturn(skus);
		productSkuImpl.setProduct(mockProduct);
		verify(mockProduct).getProductSkus();
		verify(mockProduct, never()).addOrUpdateSku(any(ProductSku.class));
	}

	/**
	 * Tests validation trigger of ProductSkuImpl.setHeight().
	 */
	@Test
	public void testNonNegativeHeight() {
		ProductSku productSku = new ProductSkuImpl();
		assertThatThrownBy(() -> productSku.setHeight(BigDecimal.TEN.negate())).isInstanceOf(EpDomainException.class);

		productSku.setHeight(BigDecimal.TEN);
		productSku.setHeight(BigDecimal.ZERO);
		productSku.setHeight(null);
	}

	/**
	 * Tests validation trigger of ProductSkuImpl.setWeight().
	 */
	@Test
	public void testNonNegativeWeight() {
		ProductSku productSku = new ProductSkuImpl();
		assertThatThrownBy(() -> productSku.setWeight(BigDecimal.TEN.negate())).isInstanceOf(EpDomainException.class);

		productSku.setWeight(BigDecimal.TEN);
		productSku.setWeight(BigDecimal.ZERO);
		productSku.setWeight(null);
	}

	/**
	 * Tests validation trigger of ProductSkuImpl.setLength().
	 */
	@Test
	public void testNonNegativeLength() {
		ProductSku productSku = new ProductSkuImpl();
		assertThatThrownBy(() -> productSku.setLength(BigDecimal.TEN.negate())).isInstanceOf(EpDomainException.class);

		productSku.setLength(BigDecimal.TEN);
		productSku.setLength(BigDecimal.ZERO);
		productSku.setLength(null);
	}

	/**
	 * Tests validation trigger of ProductSkuImpl.setWidth().
	 */
	@Test
	public void testNonNegativeWidth() {
		ProductSku productSku = new ProductSkuImpl();
		assertThatThrownBy(() -> productSku.setWidth(BigDecimal.TEN.negate())).isInstanceOf(EpDomainException.class);

		productSku.setWidth(BigDecimal.TEN);
		productSku.setWidth(BigDecimal.ZERO);
		productSku.setWidth(null);
	}

	/**
	 * Test that the effective start date is calculated correctly for multi-sku products.
	 *
	 * @throws ParseException in case of date parsing error.
	 */
	@Test
	public void testEffectiveStartDateForMultiSkuProduct() throws ParseException {
		ProductSku productSku = new ProductSkuImpl();
		Product product = createProduct(true);
		productSku.setProduct(product);

		final SimpleDateFormat yyyyMMdd = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);

		product.setStartDate(yyyyMMdd.parse(JAN_1_2010));
		productSku.setStartDate(null);
		Date effectiveDate = productSku.getEffectiveStartDate();
		assertThat(yyyyMMdd.format(effectiveDate)).as("The effective start date should be the non-null date").isEqualTo(JAN_1_2010);

		product.setStartDate(yyyyMMdd.parse(JAN_2_2010));
		productSku.setStartDate(yyyyMMdd.parse(MAR_1_2010));
		effectiveDate = productSku.getEffectiveStartDate();
		assertThat(effectiveDate)
			.as("The effective start date should be the greater date")
			.isEqualTo(MAR_1_2010);

		product.setStartDate(yyyyMMdd.parse(JAN_3_2010));
		productSku.setStartDate(yyyyMMdd.parse(JAN_1_2009));
		effectiveDate = productSku.getEffectiveStartDate();
		assertThat(effectiveDate)
			.as("The effective start date should be the greater date")
			.isEqualTo(JAN_3_2010);
	}

	/**
	 * Test that the effective start date is calculated correctly for single-sku products.
	 *
	 * @throws ParseException in case of date parsing error.
	 */
	@Test
	public void testEffectiveStartDateForSingleSkuProduct() throws ParseException {
		ProductSku productSku = new ProductSkuImpl();
		Product product = createProduct(false);
		productSku.setProduct(product);

		final SimpleDateFormat yyyyMMdd = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);

		product.setStartDate(yyyyMMdd.parse(JAN_1_2010));
		productSku.setStartDate(null);
		Date effectiveDate = productSku.getEffectiveStartDate();
		assertThat(yyyyMMdd.format(effectiveDate)).as("The effective start date should be the non-null date").isEqualTo(JAN_1_2010);

		product.setStartDate(yyyyMMdd.parse(JAN_2_2010));
		productSku.setStartDate(yyyyMMdd.parse(MAR_1_2010));
		effectiveDate = productSku.getEffectiveStartDate();
		assertThat(effectiveDate)
				.as("The effective start date should match the product")
				.isEqualTo(JAN_2_2010);

		product.setStartDate(yyyyMMdd.parse(JAN_3_2010));
		productSku.setStartDate(yyyyMMdd.parse(JAN_1_2009));
		effectiveDate = productSku.getEffectiveStartDate();
		assertThat(effectiveDate)
				.as("The effective start date should match the product")
				.isEqualTo(JAN_3_2010);
	}

	/**
	 * Test that the effective end date is calculated correctly for multi-sku products.
	 *
	 * @throws ParseException in case of date parsing error.
	 */
	@Test
	public void testEffectiveEndDateForMultiSkuProduct() throws ParseException {
		ProductSku productSku = new ProductSkuImpl();
		Product product = createProduct(true);
		productSku.setProduct(product);

		final SimpleDateFormat yyyyMMdd = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);

		product.setEndDate(null);
		productSku.setEndDate(null);
		Date effectiveDate = productSku.getEffectiveEndDate();
		assertThat(effectiveDate)
			.as("The effective end date should be the null if both end dates are null")
			.isNull();

		product.setStartDate(yyyyMMdd.parse(JAN_2_2010));

		product.setEndDate(yyyyMMdd.parse("2010-01-15"));
		productSku.setEndDate(null);
		effectiveDate = productSku.getEffectiveEndDate();
		assertThat(effectiveDate)
			.as("The effective end date should be the non-null date")
			.isEqualTo("2010-01-15");

		product.setEndDate(null);
		productSku.setEndDate(yyyyMMdd.parse("2010-03-05"));
		effectiveDate = productSku.getEffectiveEndDate();
		assertThat(effectiveDate)
			.as("The effective end date should be the non-null date")
			.isEqualTo("2010-03-05");

		product.setEndDate(yyyyMMdd.parse("2010-02-01"));
		productSku.setEndDate(yyyyMMdd.parse("2010-03-06"));
		effectiveDate = productSku.getEffectiveEndDate();
		assertThat(effectiveDate)
			.as("The effective end date should be the lesser date")
			.isEqualTo("2010-02-01");

		product.setEndDate(yyyyMMdd.parse("2010-04-01"));
		productSku.setEndDate(yyyyMMdd.parse("2010-01-05"));
		effectiveDate = productSku.getEffectiveEndDate();
		assertThat(effectiveDate)
			.as("The effective end date should be the lesser date")
			.isEqualTo("2010-01-05");

		product.setEndDate(yyyyMMdd.parse("2010-04-01"));
		productSku.setEndDate(yyyyMMdd.parse("2009-12-01"));
		effectiveDate = productSku.getEffectiveEndDate();
		assertThat(effectiveDate)
			.as("The effective end date should be the start date if it would have been earlier")
			.isEqualTo(JAN_2_2010);
	}

	/**
	 * Test checking of date within a range.
	 *
	 * @throws ParseException in case of date parsing error
	 */
	@Test
	public void testIsWithinDateRange() throws ParseException {
		final SimpleDateFormat yyyyMMdd = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
		Date dateToCheck = yyyyMMdd.parse("2010-03-03");

		productSkuImpl.setStartDate(yyyyMMdd.parse("2010-02-02"));
		productSkuImpl.setEndDate(null);
		assertThat(productSkuImpl.isWithinDateRange(dateToCheck))
			.as("Sku with no end date that started before given date should be within range")
			.isTrue();

		productSkuImpl.setStartDate(yyyyMMdd.parse(MAR_1_2010));
		productSkuImpl.setEndDate(yyyyMMdd.parse("2010-12-31"));
		assertThat(productSkuImpl.isWithinDateRange(dateToCheck))
			.as("Sku within range should be recognized as such")
			.isTrue();

		productSkuImpl.setStartDate(yyyyMMdd.parse("2010-02-01"));
		productSkuImpl.setEndDate(yyyyMMdd.parse("2010-02-28"));
		assertThat(productSkuImpl.isWithinDateRange(dateToCheck))
			.as("Sku with end date before given date should not be within range")
			.isFalse();

		productSkuImpl.setStartDate(yyyyMMdd.parse("2010-04-01"));
		productSkuImpl.setEndDate(yyyyMMdd.parse("2010-05-31"));
		assertThat(productSkuImpl.isWithinDateRange(dateToCheck))
			.as("Sku with start date after given date should not be within range")
			.isFalse();
	}

	@Test
	public void testIsWithinDateRangeWithNullStartDate() {
		productSkuImpl.setStartDate(null);
		productSkuImpl.setEndDate(null);
		assertThat(productSkuImpl.isWithinDateRange(new Date()))
			.as("Sku with null start date should be treated as open ended")
			.isTrue();

		productSkuImpl.setStartDate(null);
		productSkuImpl.setEndDate(new Date(System.currentTimeMillis() - 1));
		assertThat(productSkuImpl.isWithinDateRange(new Date()))
			.as("End dates should still apply even if start date is null")
			.isFalse();
	}

	/**
	 * Ensures that if you override the attribute value map in {@link ProductSku}, then the attribute value group map is
	 * initialized correctly.
	 */
	@Test
	public void testAttributeValueGroupMapExtension() {
		final Map<String, AttributeValue> attributeValueMap = new HashMap<>();
		final ElasticPath elasticPath = mock(ElasticPath.class, "elasticPath-1");

		ProductSku productSku = new ProductSkuImpl() {
			private static final long serialVersionUID = 1L;

			@Override
			public Map<String, AttributeValue> getAttributeValueMap() {
				return attributeValueMap;
			}

			@Override
			public void setAttributeValueMap(final Map<String, AttributeValue> attributeValueMap) {
				// should set the attribute value map, does nothing for this test
			}

			@Override
			public ElasticPath getElasticPath() {
				return elasticPath;
			}
		};

		productSku.initialize();
		assertThat(productSku.getAttributeValueGroup().getAttributeValueMap())
			.as("Map should be set on the group by the factory")
			.isSameAs(attributeValueMap);
	}

	/**
	 * Test that extension classes can override the AttributeValueGroup and ProductSkuAttributeValue implementation classes.
	 */
	@Test
	public void testThatExtensionClassesCanOverrideAttributeValueImplementations() {
		AttributeImpl attribute = new AttributeImpl();
		attribute.setLocalizedProperties(mock(LocalizedProperties.class));
		attribute.setAttributeType(AttributeType.SHORT_TEXT);
		attribute.setDisplayName("name", Locale.ENGLISH);
		attribute.setKey("name");

		ExtProductSkuImpl sku = new ExtProductSkuImpl();
		sku.getAttributeValueGroup().setStringAttributeValue(attribute, null, "beanie-weenie");

		assertThat(sku.getAttributeValueGroup())
			.as("AttributeValueGroup implementation class should have been overridden")
			.isInstanceOf(ExtAttributeValueGroupTestImpl.class);
		assertThat(sku.getAttributeValueGroup().getAttributeValue("name", null))
			.as("AttributeValueImpl implementation class should have been overridden")
			.isInstanceOf(ExtAttributeValueTestImpl.class);
	}

	@Test
	public void testGetSetTaxCode() {
		final ProductImpl productImpl = new ProductImpl();
		productSkuImpl.setProduct(productImpl);

		final TaxCode bookTaxCode = new TaxCodeImpl();
		final long bookTaxCodeUid = 100L;
		bookTaxCode.setUidPk(bookTaxCodeUid);
		bookTaxCode.setCode(SALES_TAX_CODE_BOOKS);
		productSkuImpl.setTaxCodeOverride(bookTaxCode);
		assertThat(productSkuImpl.getTaxCodeOverride()).isEqualTo(bookTaxCode);
	}

	private Product createProduct(final boolean isMultiSku) {
		Product product = new ProductImpl();
		ProductType productType = new ProductTypeImpl();
		productType.setMultiSku(isMultiSku);
		product.setProductType(productType);
		return product;
	}

	/**
	 * Faux Sku Domain Impl extension class.
	 */
	private static class ExtProductSkuImpl extends ProductSkuImpl {
		private static final long serialVersionUID = -7416852828431543838L;

		@Override
		protected AttributeValueGroupFactoryImpl getAttributeValueGroupFactory() {
			return new ExtAttributeValueGroupFactoryTestImpl(new ExtAttributeValueFactoryTestImpl());
		}
	}
}
