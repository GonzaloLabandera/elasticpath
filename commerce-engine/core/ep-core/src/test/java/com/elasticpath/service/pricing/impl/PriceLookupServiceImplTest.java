/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.pricing.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.PriceSchedule;
import com.elasticpath.domain.catalog.PriceScheduleType;
import com.elasticpath.domain.catalog.PriceTier;
import com.elasticpath.domain.catalog.PricingScheme;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.SimplePrice;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.catalog.impl.PriceScheduleImpl;
import com.elasticpath.domain.catalog.impl.PriceTierImpl;
import com.elasticpath.domain.catalog.impl.PricingSchemeImpl;
import com.elasticpath.domain.catalog.impl.ProductBundleImpl;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.pricing.BaseAmount;
import com.elasticpath.domain.pricing.BaseAmountObjectType;
import com.elasticpath.domain.pricing.PriceAdjustment;
import com.elasticpath.domain.pricing.PriceListStack;
import com.elasticpath.domain.pricing.impl.BaseAmountImpl;
import com.elasticpath.domain.pricing.impl.PriceAdjustmentImpl;
import com.elasticpath.domain.pricing.impl.PriceListStackImpl;
import com.elasticpath.money.Money;
import com.elasticpath.service.catalog.impl.BundleIdentifierImpl;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.pricing.BaseAmountService;
import com.elasticpath.service.pricing.PaymentScheduleHelper;
import com.elasticpath.service.pricing.PriceAdjustmentService;
import com.elasticpath.test.util.MatcherFactory;

/**
 * Test the Price lookup service.
 */
@RunWith(MockitoJUnitRunner.class)
public class PriceLookupServiceImplTest {
	private static final String REC_LIST_PRICE = "3.49";
	private static final String SKU_GUID0 = "SKU-GUID0";
	private static final Currency USD = Currency.getInstance("USD");
	private static final String INVALID_PL_GUID = "invalidPlGuid";
	private static final String GUID = "GUID";
	private static final String SKU = "SKU";

	private static final String ANY = "any";
	private static final int ANY_PRICE_TIERS_COUNT = 2;
	private static final String ANY1 = "any1";
	private static final int ANY1_PRICE_TIERS_COUNT = 3;
	private static final String ANY2 = "any2";
	private static final int ANY2_PRICE_TIERS_COUNT = 1;

	private static final String PRODUCT_GUID = "PRODUCT-GUID";
	private static final String SKU_GUID1 = "SKU-GUID1";
	private static final String PL_GUID = "PLR";

	private static final int TWELVE = 12;
	private static final int THIRTEEN = 13;

	private final PriceLookupServiceImpl service = new PriceLookupServiceImpl();

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private PriceAdjustmentService priceAdjustmentService;

	@Mock
	private BaseAmountService baseAmountService;

	private PricePopulatorImpl pricePopulator;
	private BaseAmountFinderImpl baseAmountFinder;
	private final PricedEntityFactoryImpl pricedEntityFactory = new PricedEntityFactoryImpl();

	@Mock
	private PaymentScheduleHelper paymentScheduleHelper;

	/**
	 * Setup.
	 */
	@Before
	public void setUp() {
		baseAmountFinder = new BaseAmountFinderImpl();

		pricePopulator = new PricePopulatorImpl();
		pricePopulator.setBeanFactory(beanFactory);

		pricedEntityFactory.setBaseAmountFinder(baseAmountFinder);
		pricedEntityFactory.setPricePopulator(pricePopulator);
		pricedEntityFactory.setBeanFactory(beanFactory);
		pricedEntityFactory.setPaymentScheduleHelper(paymentScheduleHelper);
		pricedEntityFactory.setBundleIdentifier(new BundleIdentifierImpl());
		pricedEntityFactory.setDefaultDataSource(baseAmountService);

		final TimeService timeService = mock(TimeService.class);
		when(beanFactory.getSingletonBean(ContextIdNames.TIME_SERVICE, TimeService.class)).thenReturn(timeService);
		when(timeService.getCurrentTime()).thenReturn(new Date());

		service.setBeanFactory(beanFactory);
		service.setPriceAdjustmentService(priceAdjustmentService);
		service.setBaseAmountFinder(baseAmountFinder);
		service.setPricedEntityFactory(pricedEntityFactory);
	}


	/**
	 * Test that if there is no sku price in the catalog, price lookup falls
	 * back to the Product's price in the catalog.
	 */
	@Test
	public void testPriceLookupFallbackNoSkuPrice() {
		final Price verifyPrice = new PriceImpl();
		final Money listPrice = Money.valueOf(THIRTEEN, USD);
		verifyPrice.setListPrice(listPrice);

		final List<BaseAmount> list = new ArrayList<>();
		BaseAmount amount = new BaseAmountImpl(
			"guid-2", PRODUCT_GUID, "PRODUCT",
			new BigDecimal(TWELVE), new BigDecimal(THIRTEEN), new BigDecimal(THIRTEEN),
			ANY);
		list.add(amount);

		final List<String> objGuids = new ArrayList<>();
		objGuids.add(SKU);
		objGuids.add(PRODUCT_GUID);

		final PriceListStackImpl stack = new PriceListStackImpl();
		stack.addPriceList(ANY);
		stack.setCurrency(USD);

		final ProductSku sku = mock(ProductSku.class);
		final Product product = mock(Product.class);

		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_TIER, PriceTier.class)).thenAnswer(invocation -> new PriceTierImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class)).thenAnswer(invocation -> new PriceImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_SCHEDULE, PriceSchedule.class)).thenAnswer(invocation -> new PriceScheduleImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICING_SCHEME, PricingScheme.class)).thenAnswer(invocation -> new PricingSchemeImpl());

		when(sku.getSkuCode()).thenReturn(SKU);
		when(sku.getProduct()).thenReturn(product);
		when(product.getGuid()).thenReturn(PRODUCT_GUID);

		when(baseAmountService.getBaseAmounts(stack.getPriceListStack(), objGuids)).thenReturn(list);

		when(paymentScheduleHelper.getPaymentSchedule(sku)).thenReturn(null);

		assertThat(service.getSkuPrice(sku, stack).getListPrice()).isEqualTo(verifyPrice.getListPrice(TWELVE));
	}

	/**
	 * Test for get prices for all price lists for sku.
	 */
	@Test
	public void testGetSkuPrices() {

		final Product product = mock(Product.class);
		final ProductSku sku0 = mock(ProductSku.class, "sku0");

		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class)).thenAnswer(invocation -> new PriceImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_TIER, PriceTier.class)).thenAnswer(invocation -> new PriceTierImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_LIST_STACK, PriceListStack.class)).thenAnswer(invocation -> new PriceListStackImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_SCHEDULE, PriceSchedule.class)).thenAnswer(invocation -> new PriceScheduleImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICING_SCHEME, PricingScheme.class)).thenAnswer(invocation -> new PricingSchemeImpl());


		when(sku0.getSkuCode()).thenReturn(SKU_GUID1);

		when(sku0.getProduct()).thenReturn(product);

		when(product.getGuid()).thenReturn("someGuid-1");

		when(paymentScheduleHelper.getPaymentSchedule(sku0)).thenReturn(null);

		PriceListStackImpl plStack = new PriceListStackImpl();
		plStack.addPriceList(ANY);
		plStack.addPriceList(ANY1);
		plStack.addPriceList(ANY2);
		plStack.setCurrency(Currency.getInstance("CAD"));

		final List<BaseAmount> baList = new ArrayList<>();

		// first
		BaseAmount amount = new BaseAmountImpl("guid-1-1", SKU_GUID1, SKU,
			new BigDecimal(TWELVE), BigDecimal.TEN, BigDecimal.TEN, ANY);
		baList.add(amount);

		amount = new BaseAmountImpl("guid-1-2", SKU_GUID1, SKU,
			new BigDecimal(TWELVE + 1), new BigDecimal(THIRTEEN), new BigDecimal(THIRTEEN), ANY);
		baList.add(amount);

		// second price list
		amount = new BaseAmountImpl("guid-1-3", SKU_GUID1, SKU,
			new BigDecimal(TWELVE), BigDecimal.TEN, BigDecimal.TEN, ANY1);
		baList.add(amount);

		amount = new BaseAmountImpl("guid-1-4", SKU_GUID1, SKU,
			new BigDecimal(TWELVE + 1), new BigDecimal(THIRTEEN), new BigDecimal(THIRTEEN), ANY1);
		baList.add(amount);

		amount = new BaseAmountImpl("guid-1-5", SKU_GUID1, SKU,
			new BigDecimal(TWELVE + 2), new BigDecimal(THIRTEEN + 1), new BigDecimal(THIRTEEN + 1), ANY1);
		baList.add(amount);

		// third price list
		amount = new BaseAmountImpl("guid-1-6", SKU_GUID1, SKU,
			new BigDecimal(TWELVE), BigDecimal.TEN, BigDecimal.TEN, ANY2);

		baList.add(amount);

		BaseAmountServiceImpl baService = new BaseAmountServiceImpl() {
			@Override
			public List<BaseAmount> getBaseAmounts(final List<String> plGuids, final List<String> objectGuids) {
				return baList;
			}
		};
		pricedEntityFactory.setDefaultDataSource(baService);

		final PriceLookupServiceImpl testService = new PriceLookupServiceImpl();
		testService.setBeanFactory(beanFactory);
		testService.setBaseAmountFinder(baseAmountFinder);
		testService.setPriceAdjustmentService(priceAdjustmentService);
		testService.setPricedEntityFactory(pricedEntityFactory);

		Map<String, Price> rez = testService.getSkuPrices(sku0, plStack);

		assertThat(rez).isNotNull();

		assertThat(rez.get(ANY2).getPriceTiers()).hasSize(ANY2_PRICE_TIERS_COUNT);

		assertThat(rez.get(ANY).getPriceTiers()).hasSize(ANY_PRICE_TIERS_COUNT);

		assertThat(rez.get(ANY1).getPriceTiers()).hasSize(ANY1_PRICE_TIERS_COUNT);
	}

	/**
	 * Test that the lowest price sku is selected.
	 */
	@Test
	public void testLowestSkuPrice() {
		final Price verifyPrice = new PriceImpl();
		final Money listPrice = Money.valueOf(BigDecimal.TEN, Currency.getInstance("UAH"));
		verifyPrice.setListPrice(listPrice);

		final List<BaseAmount> list = new ArrayList<>();

		BaseAmount amount = new BaseAmountImpl("guid-1-0", SKU_GUID0, SKU,
			new BigDecimal(TWELVE), new BigDecimal(THIRTEEN), new BigDecimal(THIRTEEN), ANY);
		list.add(amount);

		amount = new BaseAmountImpl("guid-1-1", SKU_GUID1, SKU,
			new BigDecimal(TWELVE), BigDecimal.TEN, BigDecimal.TEN, ANY); // lowest is 10
		list.add(amount);

		amount = new BaseAmountImpl("guid-2", PRODUCT_GUID, "PRODUCT",
			new BigDecimal(TWELVE), new BigDecimal(THIRTEEN), new BigDecimal(THIRTEEN), ANY);
		list.add(amount);

		final Product product = mock(Product.class);
		final ProductSku sku0 = mock(ProductSku.class, "sku0");
		final ProductSku sku1 = mock(ProductSku.class, "sku1");
		final StoreProduct storeProduct = mock(StoreProduct.class);

		final Map<String, ProductSku> prodSkus = new HashMap<>();
		prodSkus.put(SKU_GUID0, sku0);
		prodSkus.put(SKU_GUID1, sku1);

		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class)).thenAnswer(invocation -> new PriceImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_TIER, PriceTier.class)).thenAnswer(invocation -> new PriceTierImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_SCHEDULE, PriceSchedule.class)).thenAnswer(invocation -> new PriceScheduleImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICING_SCHEME, PricingScheme.class)).thenAnswer(invocation -> new PricingSchemeImpl());

		final PriceListStackImpl plStack = new PriceListStackImpl();
		plStack.addPriceList(ANY);
		plStack.setCurrency(Currency.getInstance("UAH"));

		final List<String> objectGuids = new ArrayList<>();
		objectGuids.add(PRODUCT_GUID);
		objectGuids.add(SKU_GUID0);
		objectGuids.add(SKU_GUID1);

		when(sku0.getProduct()).thenReturn(product);
		when(sku1.getProduct()).thenReturn(product);
		when(sku0.getSkuCode()).thenReturn(SKU_GUID0);
		when(sku1.getSkuCode()).thenReturn(SKU_GUID1);
		when(sku0.isWithinDateRange(any(Date.class))).thenReturn(true);
		when(sku1.isWithinDateRange(any(Date.class))).thenReturn(true);

		when(product.getProductSkus()).thenReturn(prodSkus);
		when(product.getGuid()).thenReturn(PRODUCT_GUID);

		when(storeProduct.getProductSkus()).thenReturn(prodSkus);

		for (final String skuGuid : prodSkus.keySet()) {
			final List<String> skuGuids = new ArrayList<>();
			skuGuids.add(PRODUCT_GUID);
			skuGuids.add(skuGuid);
			when(baseAmountService.getBaseAmounts(eq(plStack.getPriceListStack()), argThat(MatcherFactory.listContaining(skuGuids))))
					.thenReturn(list);
		}

		when(paymentScheduleHelper.isPaymentScheduleCapable(storeProduct)).thenReturn(false);
		when(paymentScheduleHelper.isPaymentScheduleCapable(product)).thenReturn(false);
		when(paymentScheduleHelper.getPaymentSchedule(sku0)).thenReturn(null);
		when(paymentScheduleHelper.getPaymentSchedule(sku1)).thenReturn(null);

		assertThat(service.getProductPrice(product, plStack).getListPrice(TWELVE).getAmount())
				.isEqualTo(verifyPrice.getListPrice(TWELVE).getAmount());

		Collection<Product> storeProducts = new ArrayList<>();
		storeProducts.add(storeProduct);
		Map<Product, Price> productsPrices = service.getProductsPrices(storeProducts, plStack);
		assertThat(productsPrices.get(storeProduct).getListPrice(TWELVE).getAmount()).isEqualTo(verifyPrice.getListPrice(TWELVE).getAmount());
	}

	/**
	 * Test that the returned map is never null, even when using an invalid price list GUID.
	 */
	@Test
	public void testGetProductBundlePriceAdjustmentsMapWithInvalidPriceList() {

		final ProductBundleImpl productBundle = new ProductBundleImpl();
		productBundle.setCalculated(Boolean.FALSE);
		productBundle.setGuid(GUID);

		when(priceAdjustmentService.findByPriceListAndBundleConstituentsAsMap(INVALID_PL_GUID, new ArrayList<>())).thenReturn(new HashMap<>());

		assertThat(service.getProductBundlePriceAdjustmentsMap(productBundle, INVALID_PL_GUID))
			.as("Should not return null when no price adjustments are found due to an invalid price list GUID.")
			.isNotNull();
		assertThat(service.getProductBundlePriceAdjustmentsMap(productBundle, INVALID_PL_GUID))
			.as("Should return an empty map when no price adjustment are found due to an invalid price list GUID.")
			.isEmpty();
	}

	/**
	 * Test that the price adjustment filtering for bundles works.
	 */
	@Test
	public void testGetProductBundlePriceAdjustmentsMapWithZeroPriceAdjustments1() {
		final ProductBundle productBundle = mock(ProductBundle.class);

		final BundleConstituent constituent1 = mock(BundleConstituent.class);
		final ConstituentItem constituentItem1 = mock(ConstituentItem.class);

		final BundleConstituent constituent2 = mock(BundleConstituent.class, "constituent2");
		final ConstituentItem constituentItem2 = mock(ConstituentItem.class, "constituentItem2");

		final BundleConstituent constituent3 = mock(BundleConstituent.class, "constituent3");
		final ConstituentItem constituentItem3 = mock(ConstituentItem.class, "constituentItem3");

		final List<BundleConstituent> bundleConstituents = ImmutableList.of(constituent1, constituent2, constituent3);

		final Map<String, PriceAdjustment> priceAdjustments = new HashMap<>();
		priceAdjustments.put(ANY, createPA(ANY, 0));
		priceAdjustments.put(ANY1, createPA(ANY1, 1));
		priceAdjustments.put(ANY2, createPA(ANY2, -1));
		when(productBundle.isCalculated()).thenReturn(Boolean.FALSE);

		when(priceAdjustmentService.findByPriceListAndBundleConstituentsAsMap(GUID, ImmutableList.of(ANY, ANY1, ANY2))).thenReturn(priceAdjustments);

		when(productBundle.getConstituents()).thenReturn(bundleConstituents);


		when(constituent1.getGuid()).thenReturn(ANY);
		when(constituent1.getConstituent()).thenReturn(constituentItem1);
		when(constituentItem1.isBundle()).thenReturn(false);

		when(constituent2.getGuid()).thenReturn(ANY1);
		when(constituent2.getConstituent()).thenReturn(constituentItem2);
		when(constituentItem2.isBundle()).thenReturn(false);

		when(constituent3.getGuid()).thenReturn(ANY2);
		when(constituent3.getConstituent()).thenReturn(constituentItem3);
		when(constituentItem3.isBundle()).thenReturn(false);

		final Map<String, PriceAdjustment> expectedAdjustments = new HashMap<>();
		expectedAdjustments.put(ANY1, priceAdjustments.get(ANY1));
		assertThat(service.getProductBundlePriceAdjustmentsMap(productBundle, GUID)).isEqualTo(expectedAdjustments);
	}

	/**
	 * Test that the price adjustment filtering for bundles works.
	 */
	@Test
	public void testGetProductBundlePriceAdjustmentsMapWithZeroPriceAdjustments2() {
		final ProductBundle productBundle = mock(ProductBundle.class);

		final BundleConstituent constituent1 = mock(BundleConstituent.class);
		final ConstituentItem constituentItem1 = mock(ConstituentItem.class);

		final BundleConstituent constituent2 = mock(BundleConstituent.class, "constituent2");
		final ConstituentItem constituentItem2 = mock(ConstituentItem.class, "constituentItem2");

		final BundleConstituent constituent3 = mock(BundleConstituent.class, "constituent3");
		final ConstituentItem constituentItem3 = mock(ConstituentItem.class, "constituentItem3");

		final List<BundleConstituent> bundleConstituents = ImmutableList.of(constituent1, constituent2, constituent3);

		final Map<String, PriceAdjustment> priceAdjustments = new HashMap<>();
		priceAdjustments.put(ANY, createPA(ANY, 0));
		priceAdjustments.put(ANY1, createPA(ANY1, 1));
		priceAdjustments.put(ANY2, createPA(ANY2, -1));
		when(productBundle.isCalculated()).thenReturn(Boolean.TRUE);

		when(priceAdjustmentService.findByPriceListAndBundleConstituentsAsMap(GUID, ImmutableList.of(ANY, ANY1, ANY2))).thenReturn(priceAdjustments);

		when(productBundle.getConstituents()).thenReturn(bundleConstituents);

		when(constituent1.getGuid()).thenReturn(ANY);
		when(constituent1.getConstituent()).thenReturn(constituentItem1);
		when(constituentItem1.isBundle()).thenReturn(false);

		when(constituent2.getGuid()).thenReturn(ANY1);
		when(constituent2.getConstituent()).thenReturn(constituentItem2);
		when(constituentItem2.isBundle()).thenReturn(false);

		when(constituent3.getGuid()).thenReturn(ANY2);
		when(constituent3.getConstituent()).thenReturn(constituentItem3);
		when(constituentItem3.isBundle()).thenReturn(false);

		final Map<String, PriceAdjustment> expectedAdjustments = new HashMap<>();
		expectedAdjustments.put(ANY2, priceAdjustments.get(ANY2));
		assertThat(service.getProductBundlePriceAdjustmentsMap(productBundle, GUID)).isEqualTo(expectedAdjustments);
	}




	private PriceAdjustment createPA(final String guid, final int amount) {
		final PriceAdjustment priceAdj = new PriceAdjustmentImpl();
		priceAdj.setGuid(guid);
		priceAdj.setAdjustmentAmount(BigDecimal.valueOf(amount));
		return priceAdj;
	}


	private BaseAmount createBaseAmount(final String priceListGuid,
			final String quantity, final String list, final String sale,
			final String objectGuid, final String objectType) {
		BaseAmountImpl baseAmount = new BaseAmountImpl();
		baseAmount.setPriceListDescriptorGuid(priceListGuid);
		baseAmount.setObjectGuid(objectGuid);
		baseAmount.setObjectType(objectType);
		baseAmount.setSaleValue(new BigDecimal(sale));
		baseAmount.setListValue(new BigDecimal(list));
		baseAmount.setQuantity(new BigDecimal(quantity));
		return baseAmount;
	}


	/**
	 * Test that requesting a multi-faceted price returns the expected facets for a SKU.
	 */
	@Test
	public void testPricingScheme() {
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class)).thenAnswer(invocation -> new PriceImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_TIER, PriceTier.class)).thenAnswer(invocation -> new PriceTierImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_SCHEDULE, PriceSchedule.class)).thenAnswer(invocation -> new PriceScheduleImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICING_SCHEME, PricingScheme.class)).thenAnswer(invocation -> new PricingSchemeImpl());

		final List<BaseAmount> amounts = new ArrayList<>();
		BaseAmount baseAmount = createBaseAmount(PL_GUID, "1", "17.99", "14.99", PRODUCT_GUID, BaseAmountObjectType.PRODUCT.toString());

		amounts.add(baseAmount);

		final PriceListStackImpl stack = new PriceListStackImpl();
		stack.addPriceList(PL_GUID);
		stack.setCurrency(USD);

		final Product product = mock(Product.class);
		final ProductSku productSku = mock(ProductSku.class);
		final Map<String, ProductSku> prodSkus = new HashMap<>();
		prodSkus.put("SKU-GUID2", productSku);

		final List<String> objectGuids = new ArrayList<>();
		objectGuids.add("SKU-GUID2");
		objectGuids.add(PRODUCT_GUID);

		when(product.getGuid()).thenReturn(PRODUCT_GUID);
		when(productSku.getProduct()).thenReturn(product);
		when(productSku.getSkuCode()).thenReturn("SKU-GUID2");

		when(baseAmountService.getBaseAmounts(stack.getPriceListStack(), objectGuids)).thenReturn(amounts);
		when(paymentScheduleHelper.getPaymentSchedule(productSku)).thenReturn(null);

		Price price = service.getSkuPrice(productSku, stack);

		assertPriceValue("Classic price returned wrong price", "17.99", price.getListPrice());

		assertThat(price.getPricingScheme()).as("A price scheme should be available").isNotNull();
		PricingScheme scheme = price.getPricingScheme();

		assertThat(scheme.getSchedules())
			.as("Incorrect number of schedules")
			.hasSize(1);

		Collection<PriceSchedule> schedules = scheme.getSchedules(PriceScheduleType.PURCHASE_TIME);
		assertThat(schedules)
			.as("A purchase time price schedule should be found")
			.isNotEmpty();

		PriceSchedule purchaseTimeSchedule = schedules.iterator().next();
		SimplePrice oneTimePrice = scheme.getSimplePriceForSchedule(purchaseTimeSchedule);
		assertThat(oneTimePrice)
			.as("A one-time price should be defined")
			.isNotNull();
		assertPriceValue("The one-time price incorrect", "17.99", oneTimePrice.getListPrice(1));

		Collection<PriceSchedule> recurringSchedules = scheme.getSchedules(PriceScheduleType.RECURRING);
		assertThat(recurringSchedules)
			.as("A recurring price schedule shouldn't be found")
			.isEmpty();
	}

	/**
	 * Test that a calculated bundle gets a price based on the constituents.
	 */
	@Test
	public void testCalculatedBundlePrice() {
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class)).thenAnswer(invocation -> new PriceImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_TIER, PriceTier.class)).thenAnswer(invocation -> new PriceTierImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_SCHEDULE, PriceSchedule.class)).thenAnswer(invocation -> new PriceScheduleImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICING_SCHEME, PricingScheme.class)).thenAnswer(invocation -> new PricingSchemeImpl());

		final ProductSku productSku = mock(ProductSku.class);
		final ProductBundle productBundle = mock(ProductBundle.class);
		final Map<String, ProductSku> skuMap = new HashMap<>();
		skuMap.put(SKU_GUID0, productSku);

		final List<BundleConstituent> constituents = new ArrayList<>();
		final BundleConstituent constituent = mock(BundleConstituent.class);
		constituents.add(constituent);

		final ConstituentItem constituentItem = mock(ConstituentItem.class);
		final Product constituentProduct = mock(Product.class, "constituentProduct");
		final ProductSku constituentProductProductSku = mock(ProductSku.class, "sku2");

		final String constituentGuid = "CONSTITUENT";
		final String constituentSkuGuid = "CONSTITUENT_SKU";
		final Map<String, ProductSku> constituentSkuMap = new HashMap<>();
		constituentSkuMap.put(constituentSkuGuid, constituentProductProductSku);

		final List<BaseAmount> amounts = new ArrayList<>();
		BaseAmount baseAmount1 = createBaseAmount(PL_GUID, "1", "8.99", REC_LIST_PRICE, constituentGuid, BaseAmountObjectType.PRODUCT.toString());
		amounts.add(baseAmount1);

		final PriceListStackImpl stack = new PriceListStackImpl();
		stack.addPriceList(PL_GUID);
		stack.setCurrency(USD);

		final List<String> objectGuids = new ArrayList<>();
		objectGuids.add(SKU_GUID0);
		objectGuids.add(PRODUCT_GUID);

		final List<String> constitutentGuids = new ArrayList<>();
		constitutentGuids.add(constituentGuid);
		constitutentGuids.add(constituentSkuGuid);

		when(productSku.getProduct()).thenReturn(productBundle);

		when(productBundle.isCalculated()).thenReturn(Boolean.TRUE);
		when(productBundle.getConstituents()).thenReturn(constituents);
		when(productBundle.getSelectionRule()).thenReturn(null);

		when(constituent.getConstituent()).thenReturn(constituentItem);
		when(constituent.getQuantity()).thenReturn(1);

		when(constituent.getPriceAdjustmentForPriceList(PL_GUID)).thenReturn(null);

		when(constituentItem.isProduct()).thenReturn(true);
		when(constituentItem.getProduct()).thenReturn(constituentProduct);
		when(constituentProduct.getGuid()).thenReturn(constituentGuid);
		when(constituentProduct.getProductSkus()).thenReturn(constituentSkuMap);
		when(constituentProductProductSku.getSkuCode()).thenReturn(constituentSkuGuid);
		when(constituentProductProductSku.getProduct()).thenReturn(constituentProduct);
		when(constituentProductProductSku.isWithinDateRange(any(Date.class))).thenReturn(true);

		when(baseAmountService.getBaseAmounts(eq(stack.getPriceListStack()), argThat(MatcherFactory.listContaining(constitutentGuids))))
				.thenReturn(amounts);

		when(paymentScheduleHelper.isPaymentScheduleCapable(constituentProduct)).thenReturn(false);
		when(paymentScheduleHelper.getPaymentSchedule(constituentProductProductSku)).thenReturn(null);
		Price skuPrice = service.getSkuPrice(productSku, stack);
		assertThat(skuPrice).isNotNull();
	}

	/**
	 * Test that a calculated bundle doesn't get a price when one of the constituents has a null price,
	 * even if the bundle itself has a base amount.
	 */
	@Test
	public void testCalculatedBundlePriceWithNullConstituentPrice() {
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class)).thenAnswer(invocation -> new PriceImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICING_SCHEME, PricingScheme.class)).thenAnswer(invocation -> new PricingSchemeImpl());

		final ProductSku productSku = mock(ProductSku.class);
		final ProductBundle productBundle = mock(ProductBundle.class);

		final List<BundleConstituent> constituents = new ArrayList<>();
		final BundleConstituent constituent = mock(BundleConstituent.class);
		constituents.add(constituent);

		final ConstituentItem constituentItem = mock(ConstituentItem.class);
		final Product constituentProduct = mock(Product.class, "constituentProduct");

		final String constituentGuid = "CONSTITUENT";

		final List<BaseAmount> amounts = new ArrayList<>();
		BaseAmount baseAmount1 = createBaseAmount(PL_GUID, "1", "9.49", REC_LIST_PRICE, PRODUCT_GUID, BaseAmountObjectType.PRODUCT.toString());
		amounts.add(baseAmount1);

		final PriceListStackImpl stack = new PriceListStackImpl();
		stack.addPriceList(PL_GUID);
		stack.setCurrency(USD);

		final List<String> objectGuids = new ArrayList<>();
		objectGuids.add(SKU_GUID0);
		objectGuids.add(PRODUCT_GUID);

		final List<String> constitutentGuids = new ArrayList<>();
		constitutentGuids.add(constituentGuid);

		when(productSku.getProduct()).thenReturn(productBundle);

		when(productBundle.isCalculated()).thenReturn(Boolean.TRUE);
		when(productBundle.getConstituents()).thenReturn(constituents);

		when(constituent.getConstituent()).thenReturn(constituentItem);
		when(constituentItem.isProduct()).thenReturn(true);
		when(constituentItem.getProduct()).thenReturn(constituentProduct);
		when(constituentProduct.getProductSkus()).thenReturn(Collections.emptyMap());

		when(paymentScheduleHelper.isPaymentScheduleCapable(constituentProduct)).thenReturn(false);

		when(productBundle.getSelectionRule()).thenReturn(null);


		Price skuPrice = service.getSkuPrice(productSku, stack);
		assertThat(skuPrice).isNull();

	}


	private void assertPriceValue(final String message, final String expected, final Money actual) {
		assertThat(actual.getAmount().toString()).as(message).isEqualTo(expected);
	}

}
