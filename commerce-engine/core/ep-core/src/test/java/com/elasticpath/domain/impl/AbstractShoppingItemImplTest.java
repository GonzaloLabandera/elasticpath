/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.testing.EqualsTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.PriceSchedule;
import com.elasticpath.domain.catalog.PriceScheduleType;
import com.elasticpath.domain.catalog.PricingScheme;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.catalog.impl.PriceScheduleImpl;
import com.elasticpath.domain.catalog.impl.PricingSchemeImpl;
import com.elasticpath.domain.catalog.impl.ProductBundleImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.catalog.impl.ProductTypeImpl;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.domain.quantity.Quantity;
import com.elasticpath.domain.shoppingcart.DiscountRecord;
import com.elasticpath.domain.shoppingcart.ItemType;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemRecurringPrice;
import com.elasticpath.domain.shoppingcart.impl.CartItem;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemRecurringPriceImpl;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemSimplePrice;
import com.elasticpath.domain.subscriptions.PaymentSchedule;
import com.elasticpath.domain.subscriptions.impl.PaymentScheduleImpl;
import com.elasticpath.money.Money;
import com.elasticpath.sellingchannel.ShoppingItemRecurringPriceAssembler;
import com.elasticpath.sellingchannel.impl.ShoppingItemRecurringPriceAssemblerImpl;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.catalog.SkuOptionService;
import com.elasticpath.service.pricing.impl.PaymentScheduleHelperImpl;

/**
 * Tests the {@code AbstractShoppingItemImpl} class.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.GodClass" })
@RunWith(MockitoJUnitRunner.class)
public class AbstractShoppingItemImplTest {

	private static final String PRODUCT_SKU3 = "productSku3";
	private static final String PRODUCT_SKU = "productSku";
	private static final String PRODUCT_SKU2 = "productSku2";

	private static final Quantity MONTHLY_QTY = new Quantity(1, "month");
	private static final Quantity BI_MONTHLY_QTY = new Quantity(2, "month");
	private static final Quantity ANNUALLY_QTY = new Quantity(1, "year");
	private static final Quantity BI_ANNUALLY_QTY = new Quantity(2, "year");

	private static final Currency CURRENCY_CAD = Currency.getInstance(Locale.CANADA);

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private ProductSkuLookup productSkuLookup;

	private ShoppingItemRecurringPriceAssemblerImpl recurringPriceAssembler;

	/**
	 * Set up required before each test.
	 */
	@Before
	public void setUp() {
		recurringPriceAssembler = new ShoppingItemRecurringPriceAssemblerImpl();
		recurringPriceAssembler.setBeanFactory(beanFactory);

		when(beanFactory.getSingletonBean(ContextIdNames.SHOPPING_ITEM_RECURRING_PRICE_ASSEMBLER, ShoppingItemRecurringPriceAssembler.class))
				.thenReturn(recurringPriceAssembler);
		when(beanFactory.getPrototypeBean(ContextIdNames.SHOPPING_ITEM_RECURRING_PRICE, ShoppingItemRecurringPrice.class))
				.thenAnswer(invocation -> new ShoppingItemRecurringPriceImpl());

		final PaymentScheduleHelperImpl paymentScheduleHelper = getPaymentScheduleHelper();

		final SkuOptionService skuOptionService = mock(SkuOptionService.class);
		when(skuOptionService.findOptionValueByKey("shoppingItemRecurringPrice 2")).thenReturn(null);
		paymentScheduleHelper.setSkuOptionService(skuOptionService);

		recurringPriceAssembler.setPaymentScheduleHelper(paymentScheduleHelper);

	}

	/**
	 * Test method for ShoppingCartItemImpl.hashCode().
	 */
	@Test
	public void testHashCode() {
		final String guid1 = "guid1";
		ShoppingItem cartItemImpl = new ShoppingItemImpl();
		cartItemImpl.setGuid(guid1);

		final String guid2 = "guid2";
		ShoppingItem cartItemImpl2 = new ShoppingItemImpl();
		cartItemImpl2.setGuid(guid2);

		assertThat(cartItemImpl).hasSameHashCodeAs(cartItemImpl);
		assertThat(cartItemImpl2.hashCode()).isNotEqualTo(cartItemImpl.hashCode());

		Map<ShoppingItem, String> testMap = new HashMap<>();

		testMap.put(cartItemImpl, "1");
		testMap.put(cartItemImpl2, "2");
		testMap.put(cartItemImpl, "4");

		assertThat(testMap).containsExactly(entry(cartItemImpl, "4"), entry(cartItemImpl2, "2"));
	}

	/**
	 * Test for ShoppingCartItemImpl.equals().
	 */
	@Test
	public void testEquals() {
		final String guid1 = "guid1";
		ShoppingItem cartItemImpl = new ShoppingItemImpl();
		cartItemImpl.setGuid(guid1);

		final String guid2 = "guid2";
		ShoppingItem cartItemImpl2 = new ShoppingItemImpl();
		cartItemImpl2.setGuid(guid2);

		new EqualsTester()
			.addEqualityGroup(cartItemImpl)
			.addEqualityGroup(cartItemImpl2)
			.testEquals();

	}

	/**
	 * Tests that two children with the same sku can be added as bundle items.
	 **/
	@Test
	public void testAddingTwoBundleItemsWithSameSku() {
		ShoppingItemImpl item = new ShoppingItemImpl();
		ProductSku itemSku = new ProductSkuImpl();
		itemSku.setProduct(new ProductBundleImpl());
		itemSku.setGuid("item-sku");
		item.setSkuGuid(itemSku.getGuid());
		ShoppingItemImpl child1 = new ShoppingItemImpl();
		ProductSku sku1 = new ProductSkuImpl();
		sku1.setSkuCode("skuA");
		sku1.setGuid(sku1.getSkuCode());
		child1.setSkuGuid(sku1.getGuid());
		child1.setBundleConstituent(true);
		child1.setItemType(ItemType.BUNDLE_CONSTITUENT);

		ShoppingItemImpl child2 = new ShoppingItemImpl();
		ProductSku sku2 = new ProductSkuImpl();
		sku2.setSkuCode("skuA");
		sku2.setGuid(sku2.getSkuCode());
		child2.setSkuGuid(sku2.getGuid());
		child2.setBundleConstituent(true);
		child2.setItemType(ItemType.BUNDLE_CONSTITUENT);
		item.addChildItem(child1);
		item.addChildItem(child2);

		givenProductSkuLookupWillFindSku(itemSku);
		givenProductSkuLookupWillFindSku(sku1);
		givenProductSkuLookupWillFindSku(sku2);

		assertThat(item.getBundleItems(productSkuLookup))
			.as("Should be two separate items")
			.hasSize(2);
	}

	/** Test for testHasDependentCartItems(). */
	@Test
	public void testHasBundleItems() {
		ProductSku sku = new ProductSkuImpl();
		sku.setProduct(new ProductBundleImpl());
		sku.setGuid("guid");
		givenProductSkuLookupWillFindSku(sku);

		CartItem cartItemImpl = new ShoppingItemImpl();
		cartItemImpl.setSkuGuid(sku.getGuid());
		CartItem bundleConstituentCartItem = new ShoppingItemImpl();
		bundleConstituentCartItem.setBundleConstituent(true);
		bundleConstituentCartItem.setItemType(ItemType.BUNDLE_CONSTITUENT);
		assertThat(cartItemImpl.hasBundleItems(productSkuLookup)).isFalse();
		cartItemImpl.addChildItem(bundleConstituentCartItem);
		assertThat(cartItemImpl.hasBundleItems(productSkuLookup)).isTrue();
	}

	/**
	 * Bundle should be shippable if it's constituents all are.
	 */
	@Test
	public void testIsShippableFully() {
		ShoppingItem cartItem = mockBundleCartItem(PRODUCT_SKU);
		ShoppingItem bundleConstituentItem = mockCartItem(true, PRODUCT_SKU2);

		cartItem.addChildItem(bundleConstituentItem);

		assertThat(cartItem.isShippable(productSkuLookup))
			.as("Bundle should be shippable if its constituents all are.")
			.isTrue();
	}

	/**
	 * Test for bundle with one constituent shippable, one not. Bundle should be considered shippable.
	 */
	@Test
	public void testIsShippablePartial() {
		ShoppingItem cartItem = mockBundleCartItem(PRODUCT_SKU);
		ShoppingItem dependentCartItem = mockCartItem(true, PRODUCT_SKU2);
		ShoppingItem dependentItem2 = mockCartItem(false, PRODUCT_SKU3);

		cartItem.addChildItem(dependentCartItem);
		cartItem.addChildItem(dependentItem2);

		assertThat(cartItem.isShippable(productSkuLookup))
			.as("Bundle should be considered shippable if one constituent shippable, one not")
			.isTrue();
	}

	/**
	 * Test for bundle marked as shippable but constituents marked as unshippable.
	 */
	@Test
	public void testIsShippableBundleButNotConstituents() {
		ShoppingItem cartItem = mockBundleCartItem(PRODUCT_SKU);
		ShoppingItem dependentCartItem = mockCartItem(false, PRODUCT_SKU2);

		cartItem.addChildItem(dependentCartItem);

		assertThat(cartItem.isShippable(productSkuLookup))
			.as("bundle marked as shippable but constituents marked as unshippable should be unshippable.")
			.isFalse();
	}

	/**
	 * Test for shippable when neither the bundle nor the constituent are shippable.
	 */
	@Test
	public void testNoPartShippable() {
		ShoppingItem cartItem = mockBundleCartItem(PRODUCT_SKU);
		ShoppingItem dependentCartItem = mockCartItem(false, PRODUCT_SKU2);

		cartItem.addChildItem(dependentCartItem);

		assertThat(cartItem.isShippable(productSkuLookup))
			.as("Bundle cart item with self and constituents not shippable should not be shippable.")
			.isFalse();
	}


	private ShoppingItem mockBundleCartItem(final String name) {
		final ProductBundle bundle = mock(ProductBundle.class, name + "_bundle");
		final ProductSku productSku = mock(ProductSku.class, name);

		String skuGuid = new RandomGuidImpl().toString();
		when(productSku.getProduct()).thenReturn(bundle);
		when(productSku.getGuid()).thenReturn(skuGuid);

		when(productSkuLookup.findByGuid(skuGuid)).thenReturn(productSku);

		ShoppingItem cartItem = new ShoppingItemImpl();
		productSku.setProduct(bundle);
		cartItem.setSkuGuid(productSku.getGuid());
		return cartItem;
	}

	private ShoppingItem mockCartItem(final boolean shippable, final String name) {
		final Product product = mock(Product.class, name + "_product");
		final ProductSku productSku = mock(ProductSku.class, name);

		String skuGuid = new RandomGuidImpl().toString();
		when(productSku.isShippable()).thenReturn(shippable);
		when(productSku.getProduct()).thenReturn(product);
		when(productSku.getGuid()).thenReturn(skuGuid);

		when(productSkuLookup.findByGuid(skuGuid)).thenReturn(productSku);

		ShoppingItem cartItem = new ShoppingItemImpl();
		productSku.setProduct(product);
		cartItem.setSkuGuid(productSku.getGuid());
		return cartItem;
	}

	/**
	 * Test that the lowest a cartItem amount can be is zero (never negative),
	 * regardless of the discount amount.
	 */
	@Test
	public void testLowestAmountIsZero() {
		final BigDecimal lowestPrice = BigDecimal.ONE;
		final BigDecimal discount = BigDecimal.TEN;
		Currency currency = Currency.getInstance("CAD");
		AbstractShoppingItemImpl item = prepareShoppingItem(lowestPrice, currency, false);
		item.applyDiscount(discount, productSkuLookup);
		assertThat(item.calculateItemTotal())
			.as("The cartItem amount should never be less than zero regardless of the discount amount")
			.isEqualByComparingTo(BigDecimal.ZERO);
	}


	/**
	 *	test up of internal ShoppingItemRecurringPrice SET.
	 *	need to create at least two prices/x/x/x/ objects with no SimplePrice object in them
	 *	make sure to call setRecurringPrices to exercise the set dirty flag
	 */
	@Test
	public void testGetLowestPriceAndCalculateItemTotalWithRecurringPrices() {
		ShoppingItemRecurringPrice shoppingItemRecurringPrice1 = new ShoppingItemRecurringPriceImpl();
		shoppingItemRecurringPrice1.setPaymentScheduleName("shoppingItemRecurringPrice 1");
		shoppingItemRecurringPrice1.setPaymentFrequency(MONTHLY_QTY);
		shoppingItemRecurringPrice1.setScheduleDuration(ANNUALLY_QTY);
		shoppingItemRecurringPrice1.setSimplePrice(
				new ShoppingItemSimplePrice(new BigDecimal("10.99"), new BigDecimal("8.99"), new BigDecimal("7.99")));

		ShoppingItemRecurringPrice shoppingItemRecurringPrice2 = new ShoppingItemRecurringPriceImpl();
		shoppingItemRecurringPrice1.setPaymentScheduleName("shoppingItemRecurringPrice 2");
		shoppingItemRecurringPrice1.setPaymentFrequency(BI_MONTHLY_QTY);
		shoppingItemRecurringPrice1.setScheduleDuration(BI_ANNUALLY_QTY);
		shoppingItemRecurringPrice1.setSimplePrice(
				new ShoppingItemSimplePrice(new BigDecimal("7.77"), new BigDecimal("6.66"), new BigDecimal("5.55")));

		Set<ShoppingItemRecurringPrice> recurringPrices = new HashSet<>();

		recurringPrices.add(shoppingItemRecurringPrice1);
		recurringPrices.add(shoppingItemRecurringPrice2);

		AbstractShoppingItemImpl item = prepareShoppingItemWithRecurringPrice(recurringPrices, CURRENCY_CAD);

		assertThat(item.getLowestUnitPrice().getAmount()).isEqualTo("0.00");  //unit price SHOULD return 0.00 for recurring priced items
		assertThat(item.calculateItemTotal()).isEqualTo("0.00");  //unit price SHOULD return 0.00 for calculating the item total too
	}

	private AbstractShoppingItemImpl prepareShoppingItemWithRecurringPrice(final Set<ShoppingItemRecurringPrice> recurringPrices,
			final Currency currency) {
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class)).thenAnswer(invocation -> new PriceImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICING_SCHEME, PricingScheme.class)).thenAnswer(invocation -> new PricingSchemeImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_SCHEDULE, PriceSchedule.class)).thenAnswer(invocation -> new PriceScheduleImpl());

		AbstractShoppingItemImpl item = new ShoppingItemImpl() {  //no overriding of get prices
			private static final long serialVersionUID = -2100849016179282868L;

			@Override
			public Currency getCurrency() {
				return currency;
			}

			@Override
			protected ShoppingItemRecurringPriceAssembler getShoppingItemRecurringPriceAssembler() {
				return recurringPriceAssembler;
			}

			@Override
			public <T> T getPrototypeBean(final String name, final Class<T> clazz) {
				return beanFactory.getPrototypeBean(name, clazz);
			}

			@Override
			public <T> T getSingletonBean(final String name, final Class<T> clazz) {
				return beanFactory.getSingletonBean(name, clazz);
			}
		};
		item.setRecurringPrices(recurringPrices);

		return item;
	}


	/**
	 * Test that the amount is equal to the shoppingItem price multiplied by the quantity of the cart item
	 * minus the discount applied to the item.
	 */
	@Test
	public void testAmountCalculation() {
		final int quantity = 10;
		final int shoppingItemPrice = 20;
		final int discount = 1;
		final BigDecimal lowestPrice = new BigDecimal(shoppingItemPrice);
		Currency currency = Currency.getInstance("CAD");
		AbstractShoppingItemImpl item = prepareShoppingItem(lowestPrice, currency, false);
		item.applyDiscount(new BigDecimal(discount), productSkuLookup);
		item.setQuantity(quantity);
		assertThat(item.calculateItemTotal().intValue()).isEqualTo(shoppingItemPrice * quantity - discount);
	}

	/**
	 * Test that the amount is equal to the shoppingItem price multiplied by the quantity of the cart item
	 * minus the discount applied to the item.
	 */
	@Test
	public void testAmountCalculationDiscountSkipped() {
		final int quantity = 10;
		final int shoppingItemPrice = 20;
		final int discount = 1;
		final BigDecimal lowestPrice = new BigDecimal(20);
		Currency currency = Currency.getInstance("CAD");
		AbstractShoppingItemImpl item = prepareShoppingItem(lowestPrice, currency, true);
		item.applyDiscount(new BigDecimal(discount), productSkuLookup);
		item.setQuantity(quantity);

		assertThat(item.calculateItemTotal().intValue()).isEqualTo(shoppingItemPrice * quantity);
	}


	/**.*/
	@Test
	public void testSetNullFieldValues() {
		AbstractShoppingItemImpl item = new ShoppingItemImpl();
		item.mergeFieldValues(null);
		assertThat(item.getItemData()).isEmpty();
	}

	/**.*/
	@Test
	public void testSetTwoFieldValues() {
		AbstractShoppingItemImpl item = new ShoppingItemImpl();
		Map<String, String> values = new HashMap<>();
		values.put("key1", "value1");
		values.put("key2", "value2");
		item.mergeFieldValues(values);
		assertThat(item.getItemData()).hasSize(2);
		assertThat(item.getFieldValue("key1")).isEqualTo("value1");
		assertThat(item.getFieldValue("key2")).isEqualTo("value2");
	}

	private AbstractShoppingItemImpl prepareShoppingItem(final BigDecimal lowestUnitPrice,
			final Currency currency, final boolean excludedFromDiscount) {
		final String skuGuid = "sku-guid";
		final ProductType productType = mock(ProductType.class);
		final Product product = mock(Product.class);
		final ProductSku productSku = mock(ProductSku.class);
		AbstractShoppingItemImpl item = new ShoppingItemImpl() {
			private static final long serialVersionUID = -4910510721160996614L;

			@Override
			public BigDecimal findLowestUnitPrice() {
				return lowestUnitPrice;
			}
		};
		item.setSkuGuid(skuGuid);
		item.setCurrency(currency);

		when(productType.isExcludedFromDiscount()).thenReturn(excludedFromDiscount);
		when(product.getProductType()).thenReturn(productType);
		when(productSku.getProduct()).thenReturn(product);
		when(productSkuLookup.findByGuid(skuGuid)).thenReturn(productSku);
		return item;
	}

	private PaymentScheduleHelperImpl getPaymentScheduleHelper() {
		final PaymentScheduleHelperImpl paymentScheduleHelper = new PaymentScheduleHelperImpl();
		paymentScheduleHelper.setBeanFactory(beanFactory);

		when(beanFactory.getPrototypeBean(ContextIdNames.PAYMENT_SCHEDULE, PaymentSchedule.class)).thenReturn(new PaymentScheduleImpl());

		return paymentScheduleHelper;
	}

	/**
	 * Tests the canReceiveCartPromotion() method, when the item is set as not discountable.
	 */
	@Test
	public void testCanReceiveCartPromotionNonDiscountable() {
		AbstractShoppingItemImpl shoppingItem = createShoppingItem(false);

		assertThat(shoppingItem.canReceiveCartPromotion(productSkuLookup)).isFalse();
	}

	/**
	 * Tests the canReceiveCartPromotion() method, when the item is set as not discountable, and has a purchase-time price.
	 */
	@Test
	public void testCanReceiveCartPromotionNonDiscountablePurchaseTimePrice() {
		AbstractShoppingItemImpl shoppingItem = createShoppingItem(false);

		PriceImpl price = createSimplePrice(BigDecimal.TEN);
		shoppingItem.setPrice(1, price);
		assertThat(shoppingItem.canReceiveCartPromotion(productSkuLookup)).isFalse();
	}

	/**
	 * Tests the canReceiveCartPromotion() method, when the item is set as not discountable, and has a recurring price.
	 */
	@Test
	public void testCanReceiveCartPromotionNonDiscountableRecurringPrice() {
		AbstractShoppingItemImpl shoppingItem = createShoppingItem(false);

		PriceImpl price = createSimplePrice(BigDecimal.ZERO);
		price.setPricingScheme(getRecurringScheme());
		shoppingItem.setPrice(1, price);
		assertThat(shoppingItem.canReceiveCartPromotion(productSkuLookup)).isFalse();
	}

	/**
	 * Tests the canReceiveCartPromotion() method, when the item is set as not discountable, and has a mixed
	 * recurring and purchase-time price.
	 */
	@Test
	public void testCanReceiveCartPromotionNonDiscountableMixedPrice() {
		AbstractShoppingItemImpl shoppingItem = createShoppingItem(false);

		PriceImpl price = createSimplePrice(BigDecimal.TEN);
		price.setPricingScheme(getRecurringScheme());
		shoppingItem.setPrice(1, price);
		assertThat(shoppingItem.canReceiveCartPromotion(productSkuLookup)).isFalse();
	}

	/**
	 * Tests the canReceiveCartPromotion() method, when the item is set as discountable.
	 */
	@Test
	public void testCanReceiveCartPromotionDiscountable() {
		AbstractShoppingItemImpl shoppingItem = createShoppingItem(true);

		assertThat(shoppingItem.canReceiveCartPromotion(productSkuLookup)).isTrue();
	}

	/**
	 * Tests the canReceiveCartPromotion() method, when the item is set as discountable, and has a purchase-time price.
	 */
	@Test
	public void testCanReceiveCartPromotionDiscountablePurchaseTimePrice() {
		AbstractShoppingItemImpl shoppingItem = createShoppingItem(true);

		PriceImpl price = createSimplePrice(BigDecimal.TEN);
		shoppingItem.setPrice(1, price);
		assertThat(shoppingItem.canReceiveCartPromotion(productSkuLookup)).isTrue();
	}

	/**
	 * Tests the canReceiveCartPromotion() method, when the item is set as discountable, and has a recurring price.
	 */
	@Test
	public void testCanReceiveCartPromotionDiscountableRecurringPrice() {
		AbstractShoppingItemImpl shoppingItem = createShoppingItem(true);

		PriceImpl price = createSimplePrice(BigDecimal.ZERO);
		price.setPricingScheme(getRecurringScheme());
		shoppingItem.setPrice(1, price);
		assertThat(shoppingItem.canReceiveCartPromotion(productSkuLookup)).isFalse();
	}

	/**
	 * Tests the canReceiveCartPromotion() method, when the item is set as discountable, and has a mixed
	 * recurring and purchase-time price.
	 */
	@Test
	public void testCanReceiveCartPromotionDiscountableMixedPrice() {
		AbstractShoppingItemImpl shoppingItem = createShoppingItem(true);

		PriceImpl price = createSimplePrice(BigDecimal.TEN);
		price.setPricingScheme(getRecurringScheme());
		shoppingItem.setPrice(1, price);
		assertThat(shoppingItem.canReceiveCartPromotion(productSkuLookup)).isTrue();
	}

	/**
	 * Tests the setFieldValue() method to check that the correct
	 * key and value is stored as item data.
	 */
	@Test
	public void testSetFieldValueForKeyAndValue() {
		final String key = "Record";
		final String value = "Test Data";
		final AbstractShoppingItemImpl shoppingItem = new ShoppingItemImpl();
		shoppingItem.setFieldValue(key, value);

		assertThat(shoppingItem.getItemData()).containsKey(key);
		assertThat(shoppingItem.getItemData().get(key).getKey()).isEqualTo(key);
		assertThat(shoppingItem.getItemData().get(key).getValue()).isEqualTo(value);
	}

	/**
	 * Tests that setFieldValue() can successfully modify existing item data value.
	 */
	@Test
	public void testModifyValueOfExistingItemData() {
		final String key = "Record";
		final String oldValue = "Old Data";
		final String newValue = "New Data";
		final AbstractShoppingItemImpl shoppingItem = new ShoppingItemImpl();
		shoppingItem.setFieldValue(key, oldValue);

		assertThat(shoppingItem.getItemData().get(key).getValue()).isEqualTo(oldValue);

		shoppingItem.setFieldValue(key, newValue);

		assertThat(shoppingItem.getItemData().get(key).getValue()).isEqualTo(newValue);
	}

	/**
	 * Tests that setFieldValue() modifies existing item data
	 * instead of replacing it with a new item data with same key.
	 */
	@Test
	public void testExistingItemDataObjectModified() {
		final String key = "Record";
		final String oldValue = "Old Data";
		final String newValue = "New Data";
		final AbstractShoppingItemImpl shoppingItem = new ShoppingItemImpl();
		shoppingItem.setFieldValue(key, oldValue);

		AbstractItemData itemData = shoppingItem.getItemData().get(key);
		shoppingItem.setFieldValue(key, newValue);

		assertThat(shoppingItem.getItemData().get(key).getValue()).isEqualTo(newValue);
		assertThat(shoppingItem.getItemData().get(key))
			.as("Expected setFieldValue to modify existing item data object")
			.isEqualTo(itemData);
	}

	@Test
	public void verifyClearDiscountRemovesAllDiscountRecords() {
		// Given a Shopping Item whose price contains a discount record
		final PriceImpl price = createSimplePrice(BigDecimal.TEN);
		price.addDiscountRecord(mock(DiscountRecord.class));

		final AbstractShoppingItemImpl shoppingItem = createShoppingItem(true);
		shoppingItem.setPrice(1, price);

		// When discounts are cleared
		shoppingItem.clearDiscount();

		// Then the price does not contain any discount records
		assertThat(price.getDiscountRecords()).isEmpty();
	}

	@Test
	public void verifyHasPriceTrueWhenHasLowestUnitPrice() {
		final AbstractShoppingItemImpl shoppingItem = createShoppingItem(true);
		shoppingItem.setListUnitPriceInternal(BigDecimal.TEN);

		assertThat(shoppingItem.hasPrice())
			.as("Expected hasPrice to return true when an internal unit price is set")
			.isTrue();
	}

	@Test
	public void verifyHasPriceTrueWhenPriceExists() {
		final AbstractShoppingItemImpl shoppingItem = createShoppingItem(true);
		shoppingItem.setPrice(1, createSimplePrice(BigDecimal.TEN));

		assertThat(shoppingItem.hasPrice())
			.as("Expected hasPrice to return true when a Price is set")
			.isTrue();
	}

	@Test
	public void verifyHasPriceTrueWhenPriceZeroDollars() {
		final AbstractShoppingItemImpl shoppingItem = createShoppingItem(true);
		shoppingItem.setPrice(1, createSimplePrice(BigDecimal.ZERO));

		assertThat(shoppingItem.hasPrice())
			.as("Expected hasPrice to return true when a Price is $0")
			.isTrue();
	}

	@Test
	public void shouldReturnFalseWhenComparingSingleSkuShoppingItemWithAnotherItem() {
		ShoppingItem shoppingItem = createShoppingItemWithSkuAndOptionalNameFieldValues(PRODUCT_SKU, null);
		ShoppingItem comparisonItem = createShoppingItemWithSkuAndOptionalNameFieldValues(PRODUCT_SKU, null);

		ProductType productType = createProductTypeAndSetMultiSku(false);
		Product product = createProductOfProductType(productType);

		ProductSku itemSku = createItemSkuWithProductAndGuid(product, PRODUCT_SKU);

		givenProductSkuLookupWillFindSku(itemSku);

		assertThat(shoppingItem.isMultiSku(productSkuLookup)).isFalse();
		assertThat(comparisonItem.getSkuGuid()).isEqualTo(shoppingItem.getSkuGuid());
		assertThat(comparisonItem.getFields()).isEqualTo(shoppingItem.getFields());

		assertThat(shoppingItem.isSameMultiSkuItem(productSkuLookup, comparisonItem)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenComparingShoppingItemsWithDifferentSkus() {
		ShoppingItem shoppingItem = createShoppingItemWithSkuAndOptionalNameFieldValues(PRODUCT_SKU, null);
		ShoppingItem comparisonItem = createShoppingItemWithSkuAndOptionalNameFieldValues(PRODUCT_SKU2, null);

		ProductType productType = createProductTypeAndSetMultiSku(true);
		Product product = createProductOfProductType(productType);

		ProductSku itemSku = createItemSkuWithProductAndGuid(product, PRODUCT_SKU);

		givenProductSkuLookupWillFindSku(itemSku);

		assertThat(shoppingItem.isMultiSku(productSkuLookup)).isTrue();
		assertThat(comparisonItem.getSkuGuid()).isNotEqualTo(shoppingItem.getSkuGuid());
		assertThat(comparisonItem.getFields()).isEqualTo(shoppingItem.getFields());

		assertThat(shoppingItem.isSameMultiSkuItem(productSkuLookup, comparisonItem)).isFalse();
	}

	@Test
	public void shouldReturnFalseWhenComparingShoppingItemsWithSameSkusAndDifferentFields() {
		ShoppingItem shoppingItem = createShoppingItemWithSkuAndOptionalNameFieldValues(PRODUCT_SKU, "Batman");
		ShoppingItem comparisonItem = createShoppingItemWithSkuAndOptionalNameFieldValues(PRODUCT_SKU, "Robin");

		ProductType productType = createProductTypeAndSetMultiSku(true);
		Product product = createProductOfProductType(productType);

		ProductSku itemSku = createItemSkuWithProductAndGuid(product, PRODUCT_SKU);

		givenProductSkuLookupWillFindSku(itemSku);

		assertThat(shoppingItem.isMultiSku(productSkuLookup)).isTrue();
		assertThat(comparisonItem.getSkuGuid()).isEqualTo(shoppingItem.getSkuGuid());
		assertThat(comparisonItem.getFields()).isNotEqualTo(shoppingItem.getFields());

		assertThat(shoppingItem.isSameMultiSkuItem(productSkuLookup, comparisonItem)).isFalse();
	}

	@Test
	public void shouldReturnTrueWhenComparingShoppingItemsWithSameSkusAndSameFields() {
		ShoppingItem shoppingItem = createShoppingItemWithSkuAndOptionalNameFieldValues(PRODUCT_SKU, null);
		ShoppingItem comparisonItem = createShoppingItemWithSkuAndOptionalNameFieldValues(PRODUCT_SKU, null);

		ProductType productType = createProductTypeAndSetMultiSku(true);
		Product product = createProductOfProductType(productType);

		ProductSku itemSku = createItemSkuWithProductAndGuid(product, PRODUCT_SKU);

		givenProductSkuLookupWillFindSku(itemSku);

		assertThat(shoppingItem.isMultiSku(productSkuLookup)).isTrue();
		assertThat(comparisonItem.getSkuGuid()).isEqualTo(shoppingItem.getSkuGuid());
		assertThat(comparisonItem.getFields()).isEqualTo(shoppingItem.getFields());

		assertThat(shoppingItem.isSameMultiSkuItem(productSkuLookup, comparisonItem)).isTrue();
	}

	private PriceImpl createSimplePrice(final BigDecimal amount) {
		PriceImpl price = new PriceImpl();
		price.setListPrice(Money.valueOf(amount, Currency.getInstance(Locale.US)));
		return price;
	}

	private PricingScheme getRecurringScheme() {
		PricingScheme pricingScheme = new PricingSchemeImpl();
		PriceSchedule priceSchedule = new PriceScheduleImpl();
		priceSchedule.setType(PriceScheduleType.RECURRING);
		PaymentSchedule paymentSchedule = new PaymentScheduleImpl();
		paymentSchedule.setName("Monthly");
		priceSchedule.setPaymentSchedule(paymentSchedule);
		pricingScheme.setPriceForSchedule(priceSchedule, createSimplePrice(BigDecimal.TEN));
		return pricingScheme;
	}

	private AbstractShoppingItemImpl createShoppingItem(final boolean discountable) {
		AbstractShoppingItemImpl shoppingItem = new ShoppingItemImpl() {
			private static final long serialVersionUID = -6875611418637317084L;

			@Override
			public boolean isDiscountable(final ProductSkuLookup productSkuLookup) {
				return discountable;
			}

			@Override
			public <T> T getSingletonBean(final String name, final Class<T> clazz) {
				return beanFactory.getSingletonBean(name, clazz);
			}
		};
		return shoppingItem;
	}

	private Product createProductOfProductType(final ProductType productType) {
		Product product = new ProductImpl();
		product.setProductType(productType);
		return product;
	}

	private ProductType createProductTypeAndSetMultiSku(final boolean isMultiSku) {
		ProductType productType = new ProductTypeImpl();
		productType.setMultiSku(isMultiSku);
		return productType;
	}

	private ProductSku createItemSkuWithProductAndGuid(final Product product, final String guid) {
		ProductSku productSku = new ProductSkuImpl();
		productSku.setProduct(product);
		productSku.setGuid(guid);
		return productSku;
	}

	private void givenProductSkuLookupWillFindSku(final ProductSku sku) {
		when(productSkuLookup.findByGuid(sku.getGuid())).thenReturn(sku);
	}

	private ShoppingItem createShoppingItemWithSkuAndOptionalNameFieldValues(final String sku, final String nameFieldValue) {
		ShoppingItem shoppingItem = new ShoppingItemImpl();
		shoppingItem.setSkuGuid(sku);
		shoppingItem.setFieldValue("name", Objects.isNull(nameFieldValue) ? "Batman" : nameFieldValue);
		return shoppingItem;
	}
}
