/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.shoppingcart.impl;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Iterables;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.auto.Mock;
import org.jmock.lib.action.CustomAction;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.CatalogLocaleImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.misc.impl.LocalizedPropertiesImpl;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.order.impl.OrderSkuImpl;
import com.elasticpath.domain.shipping.ShippingCostCalculationMethod;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.DiscountRecord;
import com.elasticpath.domain.shoppingcart.PromotionRecordContainer;
import com.elasticpath.domain.shoppingcart.ShippingPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.domain.shoppingcart.impl.CatalogItemDiscountRecordImpl;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.domain.tax.impl.TaxCodeImpl;
import com.elasticpath.money.Money;
import com.elasticpath.plugin.tax.domain.TaxAddress;
import com.elasticpath.plugin.tax.domain.TaxOperationContext;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.rules.EpRuleEngine;
import com.elasticpath.service.tax.TaxCalculationResult;
import com.elasticpath.service.tax.TaxCalculationService;
import com.elasticpath.service.tax.TaxCodeRetriever;
import com.elasticpath.test.jmock.AbstractCatalogDataTestCase;

@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.TestClassWithoutTestCases" })
public class PricingSnapshotServiceImplTest extends AbstractCatalogDataTestCase {

	private static final String CAD = "CAD";
	private static final Currency CURRENCY = Currency.getInstance(CAD);

	private static final BigDecimal SUBTOTAL = new BigDecimal("65").setScale(2);

	private static final String SALES_TAX_CODE_GOODS = "GOODS";

	@Mock
	private EpRuleEngine ruleEngine;

	@Mock
	private CartDirector cartDirector;

	@Mock
	private TaxCalculationService taxCalculationService;

	private PricingSnapshotServiceImpl pricingSnapshotService;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		stubGetBean(ContextIdNames.CATALOG_LOCALE, CatalogLocaleImpl.class);
		stubGetBean(ContextIdNames.LOCALIZED_PROPERTIES, LocalizedPropertiesImpl.class);
		stubGetBean(ContextIdNames.PRODUCT_SKU_LOOKUP, getProductSkuLookup());
		stubGetBean(ContextIdNames.SHIPPABLE_ITEMS_SUBTOTAL_CALCULATOR, getShippableItemsSubtotalCalculator());
		mockOrderSkuFactory();

		pricingSnapshotService = new PricingSnapshotServiceImpl();
		pricingSnapshotService.setBeanFactory(getBeanFactory());
		pricingSnapshotService.setCartDirector(cartDirector);
		pricingSnapshotService.setProductSkuLookup(getProductSkuLookup());
		pricingSnapshotService.setRuleEngine(ruleEngine);
		pricingSnapshotService.setShippableItemsSubtotalCalculator(getShippableItemsSubtotalCalculator());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void verifyCataloguePromosAreAddedToTheDiscountRecordContainer() throws Exception {
		final ShoppingCartImpl shoppingCart = getShoppingCart();
		final BigDecimal discountAmount = new BigDecimal("1.00");
		final long ruleId = 123L;
		final long actionId = 456L;

		final DiscountRecord discountRecord = new CatalogItemDiscountRecordImpl(ruleId, actionId, discountAmount);

		final TaxCalculationResult taxCalculationResult = context.mock(TaxCalculationResult.class);
		context.checking(new Expectations() {
			{
				allowing(taxCalculationService).calculateTaxes(
						with(any(String.class)),
						with(any(TaxAddress.class)),
						with(any(TaxAddress.class)),
						with(any(Money.class)),
						with(any(Map.class)),
						with(any(Money.class)),
						with(any(TaxOperationContext.class)));
				will(returnValue(taxCalculationResult));
				allowing(taxCalculationService).calculateTaxesAndAddToResult(
						with(any(TaxCalculationResult.class)),
						with(any(String.class)),
						with(any(TaxAddress.class)),
						with(any(TaxAddress.class)),
						with(any(Money.class)),
						with(any(Map.class)),
						with(any(Money.class)),
						with(any(TaxOperationContext.class)));
				will(returnValue(taxCalculationResult));

				allowing(taxCalculationResult).applyTaxes(with(any(Collection.class)));

				allowing(cartDirector).refresh(shoppingCart);
				will(new CustomAction("Adding catalogue promotions to cart items") {
					@Override
					public Object invoke(final Invocation invocation) throws Throwable {
						final ShoppingItem shoppingItem = Iterables.getFirst(shoppingCart.getAllItems(), null);

						if (shoppingItem != null) {
							final ShoppingItemPricingSnapshot shoppingItemPricingSnapshot =
									shoppingCart.getShoppingItemPricingSnapshot(shoppingItem);
							final Price price = shoppingItemPricingSnapshot.getPrice();
							price.addDiscountRecord(discountRecord);
						}

						return null;
					}
				});

				allowing(ruleEngine).fireOrderPromotionRules(shoppingCart, shoppingCart.getCustomerSession());
				allowing(ruleEngine).fireOrderPromotionSubtotalRules(shoppingCart, shoppingCart.getCustomerSession());
			}
		});

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);

		final PromotionRecordContainer promotionRecordContainer = pricingSnapshot.getPromotionRecordContainer();

		assertThat(promotionRecordContainer.getAllDiscountRecords(), contains(discountRecord));
	}

	@Test
	public void verifySnapshotForOrderSkuIsSameOrderSku() throws Exception {
		final OrderSku orderSku = new OrderSkuImpl();

		final ShoppingItemPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForOrderSku(orderSku);

		assertSame(orderSku, pricingSnapshot);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void verifyPrePromotionShippingCostsCalculatedAndSet() throws Exception {
		final ShoppingCartImpl shoppingCart = getShoppingCart();

		final String newShippingServiceLevelCode = "SHIP0001";
		final ShippingServiceLevel newShippingServiceLevel = context.mock(ShippingServiceLevel.class);
		final ShippingCostCalculationMethod shippingCostCalculationMethod = context.mock(ShippingCostCalculationMethod.class);

		final List<ShippingServiceLevel> shippingServiceLevelList = shoppingCart.getShippingServiceLevelList();
		shoppingCart.clearSelectedShippingServiceLevel();
		shippingServiceLevelList.clear();
		shippingServiceLevelList.add(newShippingServiceLevel);
		shoppingCart.setShippingServiceLevelList(shippingServiceLevelList);

		final Money shippingAmount = Money.valueOf(BigDecimal.TEN, CURRENCY);

		final TaxCalculationResult taxCalculationResult = context.mock(TaxCalculationResult.class);
		context.checking(new Expectations() {
			{
				allowing(taxCalculationService).calculateTaxes(
						with(any(String.class)),
						with(any(TaxAddress.class)),
						with(any(TaxAddress.class)),
						with(any(Money.class)),
						with(any(Map.class)),
						with(any(Money.class)),
						with(any(TaxOperationContext.class)));
				will(returnValue(taxCalculationResult));
				allowing(taxCalculationService).calculateTaxesAndAddToResult(
						with(any(TaxCalculationResult.class)),
						with(any(String.class)),
						with(any(TaxAddress.class)),
						with(any(TaxAddress.class)),
						with(any(Money.class)),
						with(any(Map.class)),
						with(any(Money.class)),
						with(any(TaxOperationContext.class)));
				will(returnValue(taxCalculationResult));

				allowing(taxCalculationResult).applyTaxes(with(any(Collection.class)));

				allowing(cartDirector).refresh(shoppingCart);
				allowing(ruleEngine).fireOrderPromotionRules(shoppingCart, shoppingCart.getCustomerSession());
				allowing(ruleEngine).fireOrderPromotionSubtotalRules(shoppingCart, shoppingCart.getCustomerSession());

				allowing(newShippingServiceLevel).getShippingCostCalculationMethod();
				will(returnValue(shippingCostCalculationMethod));

				allowing(newShippingServiceLevel).getCode();
				will(returnValue(newShippingServiceLevelCode));

				allowing(getShippableItemsSubtotalCalculator()).calculateSubtotalOfShippableItems(
						with(any(Collection.class)),
						with(any(ShoppingCartPricingSnapshot.class)),
						with(any(Currency.class)));
				will(returnValue(Money.valueOf(SUBTOTAL, CURRENCY)));

				allowing(shippingCostCalculationMethod).calculateShippingCost(
						with(any(Collection.class)),
						with(Money.valueOf(SUBTOTAL, CURRENCY)),
						with(any(Currency.class)),
						with(any(ProductSkuLookup.class)));
				will(returnValue(shippingAmount));
			}
		});

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);

		final ShippingPricingSnapshot shippingPricingSnapshot = pricingSnapshot.getShippingPricingSnapshot(newShippingServiceLevel);
		assertEquals(shippingAmount, shippingPricingSnapshot.getShippingListPrice());
		assertEquals(shippingAmount, shippingPricingSnapshot.getShippingPromotedPrice());
	}

	@Override
	protected Product newProductImpl() {
		return new ProductImpl() {
			private static final long serialVersionUID = 5000000001L;

			@Override
			public String getDisplayName(final Locale locale) {
				return "Test Display Name";
			}
		};
	}

	private void mockOrderSkuFactory() {
		final TaxCodeImpl taxCode = new TaxCodeImpl();
		taxCode.setCode(SALES_TAX_CODE_GOODS);

		final TaxCodeRetriever taxCodeRetriever = context.mock(TaxCodeRetriever.class);
		final TimeService timeService = context.mock(TimeService.class);
		context.checking(new Expectations() {
			{
				allowing(taxCodeRetriever).getEffectiveTaxCode(with(any(ProductSku.class)));
				will(returnValue(taxCode));

				allowing(timeService).getCurrentTime();
				will(returnValue(new Date()));
			}
		});

		OrderSkuFactoryImpl orderSkuFactory = new OrderSkuFactoryImpl() {
			@Override
			protected OrderSku createSimpleOrderSku() {
				return new OrderSkuImpl();
			}
		};
		orderSkuFactory.setTaxCodeRetriever(taxCodeRetriever);
		orderSkuFactory.setBundleApportioner(getBundleApportioningCalculator());
		orderSkuFactory.setDiscountApportioner(getDiscountApportioningCalculator());
		orderSkuFactory.setProductSkuLookup(getProductSkuLookup());
		orderSkuFactory.setTimeService(timeService);
		stubGetBean(ContextIdNames.ORDER_SKU_FACTORY, orderSkuFactory);
	}

}