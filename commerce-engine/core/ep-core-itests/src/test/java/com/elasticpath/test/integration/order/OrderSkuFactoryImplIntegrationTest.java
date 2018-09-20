/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.test.integration.order;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.action.CustomAction;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.catalog.impl.ProductTypeImpl;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;
import com.elasticpath.money.Money;
import com.elasticpath.service.catalog.ProductService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.shoppingcart.BundleApportioningCalculator;
import com.elasticpath.service.shoppingcart.impl.OrderSkuFactoryImpl;
import com.elasticpath.service.tax.DiscountApportioningCalculator;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.CatalogTestPersister;
import com.elasticpath.test.persister.TaxTestPersister;
import com.elasticpath.test.persister.TestDataPersisterFactory;

/** */
public class OrderSkuFactoryImplIntegrationTest extends BasicSpringContextTest {
	private static final BigDecimal ZERO = BigDecimal.ZERO;

	private static final String SKU_FREE_ITEM = "SKU_FREE_ITEM";
	private static final String SKU_BUNDLE = "SKU_BUNDLE";
	private static final String SKU_BALL = "SKU_BALL";
	private static final String SKU_NET = "SKU_NET";
	private static final String SKU_SHOE = "SKU_SHOE";
	private static final String NESTED_SKU_BUNDLE = "NESTED_SKU_BUNDLE";

	private static final BigDecimal SKU_SHOE_PRICE = valueOf(21.01);
	private static final BigDecimal SKU_NET_PRICE = valueOf(9.95);
	private static final BigDecimal SKU_BALL_PRICE = valueOf(20.00);
	private static final BigDecimal SKU_FREE_ITEM_PRICE = valueOf(0.00);
	private static final BigDecimal BUNDLE_PRICE1 = valueOf(38.81);
	private static final BigDecimal BUNDLE_PRICE2 = valueOf(71.29);

	private static int counter = 0;

	private OrderSkuFactoryImpl factory;
	private CatalogTestPersister catalogTestPersister;
	private ProductSkuLookup productSkuLookup;
	private ProductService productService;

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	@Mock
	private ShoppingCartTaxSnapshot pricingSnapshot;

	private ShoppingCart shoppingCart;

	private static BigDecimal valueOf(final double value) {
		return BigDecimal.valueOf(value).setScale(2);
	}

	@Before
	public void setUp() {
		final BeanFactory beanFactory = getTac().getBeanFactory();
		productSkuLookup = beanFactory.getBean(ContextIdNames.PRODUCT_SKU_LOOKUP);
		productService = beanFactory.getBean(ContextIdNames.PRODUCT_SERVICE);

		TestDataPersisterFactory persisterFactory = getTac().getPersistersFactory();
		catalogTestPersister = persisterFactory.getCatalogTestPersister();

		context.checking(new Expectations() {
			{
				allowing(pricingSnapshot).getShoppingItemTaxSnapshot(with(any(ShoppingItem.class)));
				will(new CustomAction("Returns the same OrderSku as a ShoppingItemPricingSnapshot") {
					@Override
					public Object invoke(final Invocation invocation) {
						return invocation.getParameter(0);
					}
				});
			}
		});

		factory = new OrderSkuFactoryImpl() {
			@Override
			protected void copyFields(final ShoppingItem shoppingItem, final OrderSku orderSku, final Locale locale) {
				orderSku.setSkuGuid(shoppingItem.getSkuGuid());
			}

			@Override
			protected void copyData(final ShoppingItem cartItem, final OrderSku orderSku) {
				// do nothing
			}
		};
		factory.setBeanFactory(beanFactory);
		factory.setBundleApportioner((BundleApportioningCalculator) beanFactory.getBean(ContextIdNames.BUNDLE_APPORTIONING_CALCULATOR));
		factory.setDiscountApportioner((DiscountApportioningCalculator) beanFactory.getBean(ContextIdNames.DISCOUNT_APPORTIONING_CALCULATOR));
		factory.setProductSkuLookup(productSkuLookup);
	}

	@Test
	@DirtiesDatabase
	public void testBundleShoppingItemWithQuantityOneBundlePrice1() {
		ShoppingItem rootShoppingItem = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 1, true);
		ShoppingItem skuBallItem = createShoppingItemWithRandomGuids(SKU_BALL, SKU_BALL_PRICE, ZERO, 1, false);
		ShoppingItem skuNetItem = createShoppingItemWithRandomGuids(SKU_NET, SKU_NET_PRICE, ZERO, 1, false);
		ShoppingItem skuShoeItem = createShoppingItemWithRandomGuids(SKU_SHOE, SKU_SHOE_PRICE, ZERO, 1, false);
		rootShoppingItem.addChildItem(skuBallItem);
		rootShoppingItem.addChildItem(skuNetItem);
		rootShoppingItem.addChildItem(skuShoeItem);

		Collection<OrderSku> orderSkus = factory.createOrderSkus(Arrays.asList(rootShoppingItem), pricingSnapshot, Locale.US);
		OrderSku skuBundle01 = orderSkus.iterator().next();

		assertEquals(3, skuBundle01.getBundleItems(productSkuLookup).size());

		Iterator<OrderSku> itemIterator = getSortedOrderSkus(skuBundle01.getBundleItems(productSkuLookup)).iterator();

		assertOrderSku(itemIterator.next(), SKU_BALL, valueOf(15.23), 1);
		assertOrderSku(itemIterator.next(), SKU_NET, valueOf(7.58), 1);
		assertOrderSku(itemIterator.next(), SKU_SHOE, valueOf(16.00), 1);
	}

	@Test
	@DirtiesDatabase
	public void testBundleShoppingItemWithQuantityOneBundlePrice2() {
		ShoppingItem rootShoppingItem = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE2, ZERO, 1, true);
		ShoppingItem skuBallItem = createShoppingItemWithRandomGuids(SKU_BALL, SKU_BALL_PRICE, ZERO, 1, false);
		ShoppingItem skuNetItem = createShoppingItemWithRandomGuids(SKU_NET, SKU_NET_PRICE, ZERO, 1, false);
		ShoppingItem skuShoeItem = createShoppingItemWithRandomGuids(SKU_SHOE, SKU_SHOE_PRICE, ZERO, 1, false);
		rootShoppingItem.addChildItem(skuBallItem);
		rootShoppingItem.addChildItem(skuNetItem);
		rootShoppingItem.addChildItem(skuShoeItem);

		Collection<OrderSku> orderSkus = factory.createOrderSkus(Arrays.asList(rootShoppingItem), pricingSnapshot, Locale.US);
		OrderSku skuBundle02 = orderSkus.iterator().next();

		assertEquals(3, skuBundle02.getBundleItems(productSkuLookup).size());

		Iterator<OrderSku> itemIterator = getSortedOrderSkus(skuBundle02.getBundleItems(productSkuLookup)).iterator();

		assertOrderSku(itemIterator.next(), SKU_BALL, valueOf(27.98), 1);
		assertOrderSku(itemIterator.next(), SKU_NET, valueOf(13.92), 1);
		assertOrderSku(itemIterator.next(), SKU_SHOE, valueOf(29.39), 1);
	}

	@Test
	@DirtiesDatabase
	public void testBundleShoppingItemWithQuantityOneHavingOneFreeItem() {
		ShoppingItem rootShoppingItem = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 1, true);
		ShoppingItem skuBallItem = createShoppingItemWithRandomGuids(SKU_BALL, SKU_BALL_PRICE, ZERO, 1, false);
		ShoppingItem skuNetItem = createShoppingItemWithRandomGuids(SKU_NET, SKU_NET_PRICE, ZERO, 1, false);
		ShoppingItem skuFreeItem = createShoppingItemWithRandomGuids(SKU_FREE_ITEM, SKU_FREE_ITEM_PRICE, ZERO, 1, false);
		rootShoppingItem.addChildItem(skuBallItem);
		rootShoppingItem.addChildItem(skuNetItem);
		rootShoppingItem.addChildItem(skuFreeItem);

		Collection<OrderSku> orderSkus = factory.createOrderSkus(Arrays.asList(rootShoppingItem), pricingSnapshot, Locale.US);
		OrderSku skuBundle03 = orderSkus.iterator().next();

		assertEquals(3, skuBundle03.getBundleItems(productSkuLookup).size());

		Iterator<OrderSku> itemIterator = getSortedOrderSkus(skuBundle03.getBundleItems(productSkuLookup)).iterator();

		assertOrderSku(itemIterator.next(), SKU_BALL, valueOf(25.92), 1);
		assertOrderSku(itemIterator.next(), SKU_FREE_ITEM, valueOf(0.00), 1);
		assertOrderSku(itemIterator.next(), SKU_NET, valueOf(12.89), 1);
	}

	@Test
	@DirtiesDatabase
	public void testBundleShoppingItemWithQuantityOneHavingThreeFreeItems() {
		ShoppingItem rootShoppingItem = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 1, true);
		ShoppingItem skuBallItem = createShoppingItemWithRandomGuids(SKU_BALL, SKU_BALL_PRICE, ZERO, 1, false);
		ShoppingItem skuFreeItem1 = createShoppingItemWithRandomGuids(SKU_FREE_ITEM, SKU_FREE_ITEM_PRICE, ZERO, 1, false);
		ShoppingItem skuFreeItem2 = createShoppingItemWithRandomGuids(SKU_FREE_ITEM, SKU_FREE_ITEM_PRICE, ZERO, 1, false);
		ShoppingItem skuFreeItem3 = createShoppingItemWithRandomGuids(SKU_FREE_ITEM, SKU_FREE_ITEM_PRICE, ZERO, 1, false);
		rootShoppingItem.addChildItem(skuBallItem);
		rootShoppingItem.addChildItem(skuFreeItem1);
		rootShoppingItem.addChildItem(skuFreeItem2);
		rootShoppingItem.addChildItem(skuFreeItem3);

		Collection<OrderSku> orderSkus = factory.createOrderSkus(Arrays.asList(rootShoppingItem), pricingSnapshot, Locale.US);
		OrderSku skuBundle04 = orderSkus.iterator().next();

		assertEquals(4, skuBundle04.getBundleItems(productSkuLookup).size());

		Iterator<OrderSku> itemIterator = getSortedOrderSkus(skuBundle04.getBundleItems(productSkuLookup)).iterator();

		assertOrderSku(itemIterator.next(), SKU_BALL, valueOf(38.81), 1);
		assertOrderSku(itemIterator.next(), SKU_FREE_ITEM, valueOf(0.00), 1);
		assertOrderSku(itemIterator.next(), SKU_FREE_ITEM, valueOf(0.00), 1);
		assertOrderSku(itemIterator.next(), SKU_FREE_ITEM, valueOf(0.00), 1);
	}

	@Test
	@DirtiesDatabase
	public void testBundleShoppingItemWithQuantityOneHavingOneItems() {
		ShoppingItem rootShoppingItem = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 1, true);
		ShoppingItem skuBallItem = createShoppingItemWithRandomGuids(SKU_BALL, SKU_BALL_PRICE, ZERO, 1, false);
		rootShoppingItem.addChildItem(skuBallItem);

		Collection<OrderSku> orderSkus = factory.createOrderSkus(Arrays.asList(rootShoppingItem), pricingSnapshot, Locale.US);
		OrderSku skuBundle05 = orderSkus.iterator().next();

		assertEquals(1, skuBundle05.getBundleItems(productSkuLookup).size());

		Iterator<OrderSku> itemIterator = getSortedOrderSkus(skuBundle05.getBundleItems(productSkuLookup)).iterator();

		assertOrderSku(itemIterator.next(), SKU_BALL, valueOf(38.81), 1);
	}

	@Test
	@DirtiesDatabase
	public void testBundleShoppingItemWithMultipleQuantities1() {
		ShoppingItem rootShoppingItem = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 1, true);
		ShoppingItem skuBallItem = createShoppingItemWithRandomGuids(SKU_BALL, SKU_BALL_PRICE, ZERO, 1, false);
		ShoppingItem skuNetItem = createShoppingItemWithRandomGuids(SKU_NET, SKU_NET_PRICE, ZERO, 7, false);
		ShoppingItem skuShoeItem = createShoppingItemWithRandomGuids(SKU_SHOE, SKU_SHOE_PRICE, ZERO, 13, false);
		rootShoppingItem.addChildItem(skuBallItem);
		rootShoppingItem.addChildItem(skuNetItem);
		rootShoppingItem.addChildItem(skuShoeItem);

		Collection<OrderSku> orderSkus = factory.createOrderSkus(Arrays.asList(rootShoppingItem), pricingSnapshot, Locale.US);
		OrderSku skuBundle06 = orderSkus.iterator().next();

		assertEquals(5, skuBundle06.getBundleItems(productSkuLookup).size());

		Iterator<OrderSku> itemIterator = getSortedOrderSkus(skuBundle06.getBundleItems(productSkuLookup)).iterator();

		assertOrderSku(itemIterator.next(), SKU_BALL, valueOf(2.14), 1);
		assertOrderSku(itemIterator.next(), SKU_NET, valueOf(1.06), 4);
		assertOrderSku(itemIterator.next(), SKU_NET, valueOf(1.07), 3);
		assertOrderSku(itemIterator.next(), SKU_SHOE, valueOf(2.24), 3);
		assertOrderSku(itemIterator.next(), SKU_SHOE, valueOf(2.25), 10);
	}

	@Test
	@DirtiesDatabase
	public void testBundleShoppingItemWithNestedBundle() {
		ShoppingItem rootShoppingItem = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 1, true);
		ShoppingItem nestedShoppingItem = createShoppingItemWithRandomGuids(NESTED_SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 1, true);
		
		ShoppingItem nestedSkuBallItem = createShoppingItemWithRandomGuids("NESTED_" + SKU_BALL, SKU_BALL_PRICE, ZERO, 1, false);
		ShoppingItem nestedSkuNetItem = createShoppingItemWithRandomGuids("NESTED_" + SKU_NET, SKU_NET_PRICE, ZERO, 1, false);
		ShoppingItem nestedSkuShoeItem = createShoppingItemWithRandomGuids("NESTED_" + SKU_SHOE, SKU_SHOE_PRICE, ZERO, 1, false);
		nestedShoppingItem.addChildItem(nestedSkuBallItem);
		nestedShoppingItem.addChildItem(nestedSkuNetItem);
		nestedShoppingItem.addChildItem(nestedSkuShoeItem);

		ShoppingItem skuNetItem = createShoppingItemWithRandomGuids(SKU_NET, SKU_NET_PRICE, ZERO, 1, false);
		ShoppingItem skuShoeItem = createShoppingItemWithRandomGuids(SKU_SHOE, SKU_SHOE_PRICE, ZERO, 1, false);

		rootShoppingItem.addChildItem(nestedShoppingItem);
		rootShoppingItem.addChildItem(skuNetItem);
		rootShoppingItem.addChildItem(skuShoeItem);

		Collection<OrderSku> orderSkus = factory.createOrderSkus(Arrays.asList(rootShoppingItem), pricingSnapshot, Locale.US);
		OrderSku skuBundle07 = orderSkus.iterator().next();

		assertEquals(3, skuBundle07.getBundleItems(productSkuLookup).size());

		Iterator<OrderSku> itemIterator = getSortedOrderSkus(skuBundle07.getBundleItems(productSkuLookup)).iterator();

		// assertion for nested bundle
		OrderSku nestedSku = itemIterator.next();
		assertEquals(3, nestedSku.getBundleItems(productSkuLookup).size());
		Iterator<OrderSku> nestItemIterator = getSortedOrderSkus(nestedSku.getBundleItems(productSkuLookup)).iterator();
		assertOrderSku(nestItemIterator.next(), "NESTED_" + SKU_BALL, valueOf(9.48), 1);
		assertOrderSku(nestItemIterator.next(), "NESTED_" + SKU_NET, valueOf(4.71), 1);
		assertOrderSku(nestItemIterator.next(), "NESTED_" + SKU_SHOE, valueOf(9.95), 1);

		assertOrderSku(itemIterator.next(), SKU_NET, valueOf(4.71), 1);
		assertOrderSku(itemIterator.next(), SKU_SHOE, valueOf(9.96), 1);
	}

	/** 
	 * Tests creation of order skus when the root bundle contains 2 different bundles of same products.
	 */
	@Test
	@DirtiesDatabase
	public void testCreateOrderSkusWithBundleContains2BundlesOfSameProducts() {
		ShoppingItem nestedShoppingItem1 = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 1, true);
		ShoppingItem nestedShoppingItem2 = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 1, true);
		nestedShoppingItem1.addChildItem(createShoppingItemWithRandomGuids("NESTED_" + SKU_BALL, SKU_BALL_PRICE, ZERO, 1, false));
		nestedShoppingItem2.addChildItem(createShoppingItemWithRandomGuids("NESTED_" + SKU_BALL, SKU_BALL_PRICE, ZERO, 1, false));
		
		ShoppingItem rootShoppingItem = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1.add(SKU_BALL_PRICE), ZERO, 1, true);
		rootShoppingItem.addChildItem(nestedShoppingItem1);
		rootShoppingItem.addChildItem(nestedShoppingItem2);

		Collection<OrderSku> orderSkus = factory.createOrderSkus(Arrays.asList(rootShoppingItem), pricingSnapshot, Locale.US);
		OrderSku skuBundle07 = orderSkus.iterator().next();
		
		assertEquals(2, skuBundle07.getBundleItems(productSkuLookup).size());
		
		Iterator<OrderSku> itemIterator = getSortedOrderSkus(skuBundle07.getBundleItems(productSkuLookup)).iterator();
		
		// assertion for nested bundle #1
		OrderSku nestedSku = itemIterator.next();
		assertEquals(1, nestedSku.getBundleItems(productSkuLookup).size());
		Iterator<OrderSku> nestItemIterator = getSortedOrderSkus(nestedSku.getBundleItems(productSkuLookup)).iterator();
		
		// should be half of the root bundle price since it contains same constituent twice
		assertOrderSku(nestItemIterator.next(), "NESTED_" + SKU_BALL, valueOf(29.40), 1);
	}
	
	/** 
	 * Tests creation of order skus when the root bundle contains 2 different bundles of same products with different quantities.
	 */
	@Test
	@DirtiesDatabase
	public void testCreateOrderSkusWithBundleContains2BundlesOfSameProductsWithDifferentQuantities() {
		ShoppingItem nestedShoppingItem1 = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 1, true);
		ShoppingItem nestedShoppingItem2 = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 1, true);
		nestedShoppingItem1.addChildItem(createShoppingItemWithRandomGuids("NESTED_" + SKU_BALL, SKU_BALL_PRICE, ZERO, 1, false));
		nestedShoppingItem2.addChildItem(createShoppingItemWithRandomGuids("NESTED_" + SKU_BALL, SKU_BALL_PRICE, ZERO, 1, false));
		
		ShoppingItem rootShoppingItem = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1.add(SKU_BALL_PRICE), ZERO, 1, true);
		rootShoppingItem.addChildItem(nestedShoppingItem1);
		rootShoppingItem.addChildItem(nestedShoppingItem2);

		Collection<OrderSku> orderSkus = factory.createOrderSkus(Arrays.asList(rootShoppingItem), pricingSnapshot, Locale.US);
		OrderSku skuBundle07 = orderSkus.iterator().next();
		
		assertEquals(2, skuBundle07.getBundleItems(productSkuLookup).size());
		
		Iterator<OrderSku> itemIterator = getSortedOrderSkus(skuBundle07.getBundleItems(productSkuLookup)).iterator();
		
		// assertion for nested bundle #1
		OrderSku nestedSku = itemIterator.next();
		assertEquals(1, nestedSku.getBundleItems(productSkuLookup).size());
		Iterator<OrderSku> nestItemIterator = getSortedOrderSkus(nestedSku.getBundleItems(productSkuLookup)).iterator();
		
		// should be half of the root bundle price since it contains same constituent twice
		assertOrderSku(nestItemIterator.next(), "NESTED_" + SKU_BALL, valueOf(29.40), 1);
	}
	
	@Test
	@DirtiesDatabase
	public void testBundleShoppingItemWithMultipleQuantities2() {
		ShoppingItem rootShoppingItem = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 1, true);
		ShoppingItem skuBallItem = createShoppingItemWithRandomGuids(SKU_BALL, SKU_BALL_PRICE, ZERO, 3, false);
		ShoppingItem skuNetItem = createShoppingItemWithRandomGuids(SKU_NET, SKU_NET_PRICE, ZERO, 7, false);
		ShoppingItem skuShoeItem = createShoppingItemWithRandomGuids(SKU_SHOE, SKU_SHOE_PRICE, ZERO, 9, false);
		rootShoppingItem.addChildItem(skuBallItem);
		rootShoppingItem.addChildItem(skuNetItem);
		rootShoppingItem.addChildItem(skuShoeItem);

		Collection<OrderSku> orderSkus = factory.createOrderSkus(Arrays.asList(rootShoppingItem), pricingSnapshot, Locale.US);
		OrderSku skuBundle08 = orderSkus.iterator().next();

		assertEquals(6, skuBundle08.getBundleItems(productSkuLookup).size());

		Iterator<OrderSku> itemIterator = getSortedOrderSkus(skuBundle08.getBundleItems(productSkuLookup)).iterator();

		assertOrderSku(itemIterator.next(), SKU_BALL, valueOf(2.43), 1);
		assertOrderSku(itemIterator.next(), SKU_BALL, valueOf(2.44), 2);
		assertOrderSku(itemIterator.next(), SKU_NET, valueOf(1.21), 6);
		assertOrderSku(itemIterator.next(), SKU_NET, valueOf(1.22), 1);
		assertOrderSku(itemIterator.next(), SKU_SHOE, valueOf(2.55), 2);
		assertOrderSku(itemIterator.next(), SKU_SHOE, valueOf(2.56), 7);
	}

	@Test
	@DirtiesDatabase
	public void testBundleShoppingItemWithMultipleQuantities3() {
		ShoppingItem rootShoppingItem = createShoppingItemWithRandomGuids(SKU_BUNDLE, BUNDLE_PRICE1, ZERO, 11, true);
		ShoppingItem skuBallItem = createShoppingItemWithRandomGuids(SKU_BALL, SKU_BALL_PRICE, ZERO, 33, false);
		ShoppingItem skuNetItem = createShoppingItemWithRandomGuids(SKU_NET, SKU_NET_PRICE, ZERO, 77, false);
		ShoppingItem skuShoeItem = createShoppingItemWithRandomGuids(SKU_SHOE, SKU_SHOE_PRICE, ZERO, 99, false);
		rootShoppingItem.addChildItem(skuBallItem);
		rootShoppingItem.addChildItem(skuNetItem);
		rootShoppingItem.addChildItem(skuShoeItem);

		Collection<OrderSku> orderSkus = factory.createOrderSkus(Arrays.asList(rootShoppingItem), pricingSnapshot, Locale.US);
		OrderSku skuBundle09 = orderSkus.iterator().next();

		assertEquals(6, skuBundle09.getBundleItems(productSkuLookup).size());

		Iterator<OrderSku> itemIterator = getSortedOrderSkus(skuBundle09.getBundleItems(productSkuLookup)).iterator();

		assertOrderSku(itemIterator.next(), SKU_BALL, valueOf(2.43), 11);
		assertOrderSku(itemIterator.next(), SKU_BALL, valueOf(2.44), 22);
		assertOrderSku(itemIterator.next(), SKU_NET, valueOf(1.21), 66);
		assertOrderSku(itemIterator.next(), SKU_NET, valueOf(1.22), 11);
		assertOrderSku(itemIterator.next(), SKU_SHOE, valueOf(2.55), 22);
		assertOrderSku(itemIterator.next(), SKU_SHOE, valueOf(2.56), 77);
	}

	@Test
	@DirtiesDatabase
	public void testBundleShoppingItemWithMultipleQuantities4() {
		ShoppingItem rootShoppingItem = createShoppingItemWithRandomGuids(SKU_BUNDLE, valueOf(105.00), ZERO, 1, true);
		ShoppingItem pd1Item = createShoppingItemWithRandomGuids("PD1", valueOf(75.00), ZERO, 1, false);
		ShoppingItem pd21Item = createShoppingItemWithRandomGuids("PD21", valueOf(15.00), ZERO, 3, false);
		ShoppingItem pd22Item = createShoppingItemWithRandomGuids("PD22", valueOf(15.00), ZERO, 4, false);
		ShoppingItem pd31Item = createShoppingItemWithRandomGuids("PD31", valueOf(55.00), ZERO, 2, false);
		ShoppingItem pd32Item = createShoppingItemWithRandomGuids("PD32", valueOf(55.00), ZERO, 2, false);

		rootShoppingItem.addChildItem(pd1Item);
		rootShoppingItem.addChildItem(pd21Item);
		rootShoppingItem.addChildItem(pd22Item);
		rootShoppingItem.addChildItem(pd31Item);
		rootShoppingItem.addChildItem(pd32Item);

		Collection<OrderSku> orderSkus = factory.createOrderSkus(Arrays.asList(rootShoppingItem), pricingSnapshot, Locale.US);
		OrderSku skuBundle11 = orderSkus.iterator().next();

		assertEquals(8, skuBundle11.getBundleItems(productSkuLookup).size());

		Iterator<OrderSku> itemIterator = getSortedOrderSkus(skuBundle11.getBundleItems(productSkuLookup)).iterator();

		assertOrderSku(itemIterator.next(), "PD1", valueOf(19.69), 1);
		assertOrderSku(itemIterator.next(), "PD21", valueOf(3.93), 1);
		assertOrderSku(itemIterator.next(), "PD21", valueOf(3.94), 2);
		assertOrderSku(itemIterator.next(), "PD22", valueOf(3.93), 1);
		assertOrderSku(itemIterator.next(), "PD22", valueOf(3.94), 3);
		assertOrderSku(itemIterator.next(), "PD31", valueOf(14.44), 2);
		assertOrderSku(itemIterator.next(), "PD32", valueOf(14.43), 1);
		assertOrderSku(itemIterator.next(), "PD32", valueOf(14.44), 1);
	}

	/**
	 * Sorts the {@link OrderSku} by ascendent according to sku code and unit price for the convenience of assertion.
	 * 
	 * @param orderSkus a collection of {@link OrderSku}.
	 * @return a list of sorted {@link OrderSku}.
	 */
	private List<OrderSku> getSortedOrderSkus(final Collection<? extends ShoppingItem> orderSkus) {
		List<OrderSku> sortedOrderSkus = new ArrayList<>();
		for (ShoppingItem sku : orderSkus) {
			sortedOrderSkus.add((OrderSku) sku);
		}

		Collections.sort(sortedOrderSkus, new Comparator<OrderSku>() {
			@Override
			public int compare(final OrderSku sku1, final OrderSku sku2) {
				ProductSku psku1 = productSkuLookup.findByGuid(sku1.getSkuGuid());
				ProductSku psku2 = productSkuLookup.findByGuid(sku2.getSkuGuid());

				int result = psku1.getSkuCode().compareTo(psku2.getSkuCode());
				if (result == 0) {
					result = sku1.getUnitPrice().compareTo(sku2.getUnitPrice());
				}

				return result;
			}
		});

		return sortedOrderSkus;
	}

	/**
	 * Asserts the guid, unit price and quantity of an {@link OrderSku}.
	 * 
	 * @param actualOrderSku
	 * @param productSkuCode
	 * @param unitPrice
	 * @param quantity
	 */
	private void assertOrderSku(final OrderSku actualOrderSku, final String productSkuCode, final BigDecimal unitPrice, final int quantity) {
		ProductSku sku = productSkuLookup.findByGuid(actualOrderSku.getSkuGuid());
		assertEquals(productSkuCode, sku.getSkuCode());
		assertEquals(unitPrice, actualOrderSku.getUnitPrice());
		assertEquals(quantity, actualOrderSku.getQuantity());
	}

	private ShoppingItem createShoppingItemWithRandomGuids(
			final String skuCode, final BigDecimal unitPrice, final BigDecimal unitDiscount, final int quantity, final boolean isBundle) {
		Product product;

		ProductSku sku = productSkuLookup.findBySkuCode(skuCode);
		if (sku == null) {
			Catalog catalog = catalogTestPersister.persistDefaultMasterCatalog();
			Category category = catalogTestPersister.persistDefaultCategories(catalog);
			if (!isBundle) {
				product = catalogTestPersister.persistProductWithSku(catalog, category, null, BigDecimal.ONE, Currency.getInstance("USD"),
						"brand", "product" + ++counter, "productName", skuCode, TaxTestPersister.TAX_CODE_GOODS, AvailabilityCriteria.ALWAYS_AVAILABLE, 0);
			} else {
				product = getTac().getBeanFactory().getBean(ContextIdNames.PRODUCT_BUNDLE);
				product.setProductType(
						catalogTestPersister.persistProductType("productType" + ++counter, catalog, TaxTestPersister.TAX_CODE_GOODS, false));
				product.setCategories(Collections.singleton(category));
				product.setCode("productBundle" + ++counter);
				sku = getTac().getBeanFactory().getBean(ContextIdNames.PRODUCT_SKU);
				sku.setSkuCode(skuCode);
				product.addOrUpdateSku(sku);

				product = productService.saveOrUpdate(product);
			}
			sku = product.getSkuByCode(skuCode);
		}

		ShoppingItem item = new ShoppingItemImpl();
		item.setGuid(new RandomGuidImpl().toString());
		item.setSkuGuid(sku.getGuid());

		Price price = new PriceImpl();
		price.setListPrice(Money.valueOf(unitPrice, Currency.getInstance(Locale.CANADA)));
		item.setPrice(quantity, price);
		sku.getProduct().setProductType(new ProductTypeImpl());
		item.applyDiscount(unitDiscount, productSkuLookup);
		
		return item;
	}
}
