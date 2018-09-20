/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.shipping.impl;

import static org.junit.Assert.assertEquals;

import static com.elasticpath.domain.shipping.impl.ShippingCostTestDataFactory.aCostCalculationParam;
import static com.elasticpath.domain.shipping.impl.ShippingCostTestDataFactory.someCalculationParams;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.commons.exception.SCCMCurrencyMissingException;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.shipping.ShippingCostCalculationParameter;
import com.elasticpath.domain.shipping.ShippingCostCalculationParametersEnum;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.money.Money;
import com.elasticpath.service.catalog.ProductSkuLookup;

/** Test cases for <code>AbstractShippingCostCalculationMethodImpl</code>.*/
public class AbstractShippingCostCalculationMethodImplTest {

	private static final String DUMMY_METHOD_TYPE = "dummyMethodType";

	private static final String DUMMY_METHOD_TEXT = "dummyMethodDisplayText";

	/** Set of keys required for this shipping cost calculation method. */
	protected static final String[] DUMMY_PARAMETER_KEYS = new String[] { "KEY1", "KEY2" };

	private static final String KEY1 = "testKey1";

	private static final String VALUE1 = "testValue1";

	private static final String KEY2 = "testKey2";

	private static final String VALUE2 = "testValue2";

	private static final String FIFTEEN = "15.0";

	private static final String ELEVEN = "11.00";

	private static final Currency CURRENCY_USD = Currency.getInstance(Locale.US);
	private static final Currency CURRENCY_FRANCE = Currency.getInstance(Locale.FRANCE);
	private static final Currency CURRENCY_ENGLISH = Currency.getInstance(Locale.UK);

	private AbstractShippingCostCalculationMethodImpl dummyShippingCostCalculationMethodImpl;

	private ProductSku productSku;

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	@Mock private ProductSkuLookup productSkuLookup;

	/**
	 * Prepare for each test.
	 * @throws Exception on error
	 */
	@Before
	public void setUp() throws Exception {

		this.dummyShippingCostCalculationMethodImpl = new AbstractShippingCostCalculationMethodImpl() {
			private static final long serialVersionUID = 6168317855634775901L;

			@Override
			protected String getMethodType() {
				return DUMMY_METHOD_TYPE;
			}

			@Override
			public String[] getParameterKeys() {
				return DUMMY_PARAMETER_KEYS.clone();
			}

			@Override
			public String getDisplayText() {
				return DUMMY_METHOD_TEXT;
			}

			@Override
			public Money calculateShippingCost(final Collection<? extends ShoppingItem> lineItems,
					final Money shippableItemsSubtotal,
					final Currency currency,
					final ProductSkuLookup productSkuLookup) {
				return null;
			}
		};

		productSku = new ProductSkuImpl();
		productSku.setGuid("skuGuid");
		productSku.setShippable(true);

		final Product product = new ProductImpl();
		product.initialize();
		product.addOrUpdateSku(productSku);

		context.checking(new Expectations() { {
			allowing(productSkuLookup).findByGuid("skuGuid"); will(returnValue(productSku));
		} });
	}

	/**
	 * Test method for 'com.elasticpath.domain.shipping.impl.ShippingCostCalculationMethodImpl.getType()'.
	 */
	@Test
	public void testGetType() {
		assertEquals(DUMMY_METHOD_TYPE, this.dummyShippingCostCalculationMethodImpl.getType());
	}

	/**
	 * Test method for 'com.elasticpath.domain.shipping.impl.ShippingCostCalculationMethodImpl.getParameters()'.
	 */
	@Test
	public void testGetSetParameters() {
		Set<ShippingCostCalculationParameter> paramSet = getParameterSet();
		this.dummyShippingCostCalculationMethodImpl.setParameters(paramSet);
		assertEquals(this.dummyShippingCostCalculationMethodImpl.getParameters(), paramSet);
	}

	/**
	 * Tests that when retrieving a param value for a specified currency but with no currency set on the
	 * calculation method an exception will be thrown.
	 */
	@Test(expected = SCCMCurrencyMissingException.class)
	public void testGetParamWithCurrencyWhenNullCurrencyThrowsException() {
		//no currency set
		final ShippingCostCalculationParameter param1
				= aCostCalculationParam(ShippingCostCalculationParametersEnum.FIXED_PRICE, new BigDecimal(ELEVEN));

		final Set <ShippingCostCalculationParameter> params = someCalculationParams(param1);

		dummyShippingCostCalculationMethodImpl.setParameters(params);

		dummyShippingCostCalculationMethodImpl.getParamValue(ShippingCostCalculationParametersEnum.FIXED_PRICE.getKey(), CURRENCY_ENGLISH);
	}

	/**
	 * Tests that when retrieving a param value for a specified currency but with no matching currency on the
	 * calculation method an exception will be thrown.
	 */
	@Test(expected = SCCMCurrencyMissingException.class)
	public void testGetParamWithCurrencyWhenNoMatchingCurrencyThrowsException() {
		final Set <ShippingCostCalculationParameter> params
				= someCalculationParams(
				aCostCalculationParam(ShippingCostCalculationParametersEnum.FIXED_PRICE,
						new BigDecimal(ELEVEN), CURRENCY_ENGLISH),
				aCostCalculationParam(ShippingCostCalculationParametersEnum.FIXED_PRICE,
						new BigDecimal(ELEVEN), CURRENCY_FRANCE));

		dummyShippingCostCalculationMethodImpl.setParameters(params);

		dummyShippingCostCalculationMethodImpl.getParamValue(ShippingCostCalculationParametersEnum.FIXED_PRICE.getKey(), CURRENCY_USD);
	}

	/**
	 * Test that calculateRegularPriceShippingCost with a matching currency set on the
	 * fixed price cost calculation method returns the correct value.
	 */
	@Test
	public void testGetParamWithCurrencyWhenMatchingCurrencyReturnsParamValue() {

		final Set <ShippingCostCalculationParameter> params
				= someCalculationParams(
				aCostCalculationParam(ShippingCostCalculationParametersEnum.FIXED_PRICE,
						new BigDecimal(ELEVEN), CURRENCY_ENGLISH),
				aCostCalculationParam(ShippingCostCalculationParametersEnum.FIXED_PRICE,
						new BigDecimal(FIFTEEN), CURRENCY_FRANCE));

		dummyShippingCostCalculationMethodImpl.setParameters(params);

		final String paramValue = dummyShippingCostCalculationMethodImpl.getParamValue(ShippingCostCalculationParametersEnum.FIXED_PRICE.getKey(),
				CURRENCY_ENGLISH);

		assertEquals("Expected value does not equal returned value from getParamValue",
				ELEVEN, paramValue);
	}

	private Set<ShippingCostCalculationParameter> getParameterSet() {
		Set<ShippingCostCalculationParameter> paramSet = new HashSet<>();

		ShippingCostCalculationParameter shippingCostCalculationParameter = new ShippingCostCalculationParameterImpl();
		shippingCostCalculationParameter.setKey(KEY1);
		shippingCostCalculationParameter.setValue(VALUE1);
		paramSet.add(shippingCostCalculationParameter);

		shippingCostCalculationParameter = new ShippingCostCalculationParameterImpl();
		shippingCostCalculationParameter.setKey(KEY2);
		shippingCostCalculationParameter.setValue(VALUE2);
		paramSet.add(shippingCostCalculationParameter);

		return paramSet;
	}

	/**
	 * Test that a lineitem's weight is only added to the total weight if the lineitem's productsku is shippable.
	 */
	@Test
	public void testShoppingItemWeightCountsOnlyIfShippable() {
		//two line items with identical weight and quantity, but only one is shippable
		final BigDecimal weight = BigDecimal.TEN;
		final int quantity = 1;
		final String shippableSkuGuid = "shippableProductSku";
		final String nonShippableSkuGuid = "nonShippableProductSku";

		final ShoppingItem shippableLineItem = context.mock(ShoppingItem.class, "shippableLineItem");
		final ShoppingItem nonShippableLineItem = context.mock(ShoppingItem.class, "nonShippableLineItem");
		final ProductSku shippableProductSku = context.mock(ProductSku.class, shippableSkuGuid);
		final ProductSku nonShippableProductSku = context.mock(ProductSku.class, nonShippableSkuGuid);
		context.checking(new Expectations() { {
			allowing(shippableLineItem).getSkuGuid(); will(returnValue(shippableSkuGuid));
			allowing(shippableLineItem).isShippable(productSkuLookup); will(returnValue(true));
			oneOf(shippableLineItem).getQuantity(); will(returnValue(quantity));
			allowing(nonShippableLineItem).getSkuGuid(); will(returnValue(nonShippableSkuGuid));
			allowing(nonShippableLineItem).isShippable(productSkuLookup); will(returnValue(false));
			allowing(nonShippableLineItem).getQuantity(); will(returnValue(quantity));
			allowing(shippableProductSku).isShippable(); will(returnValue(true));
			oneOf(shippableProductSku).getWeight(); will(returnValue(weight));
			allowing(nonShippableProductSku).isShippable(); will(returnValue(false));
			allowing(nonShippableProductSku).getWeight(); will(returnValue(weight));

			allowing(productSkuLookup).findByGuid(shippableSkuGuid); will(returnValue(shippableProductSku));
			allowing(productSkuLookup).findByGuid(nonShippableSkuGuid); will(returnValue(nonShippableProductSku));
		} });
		List<ShoppingItem> lineItems = new ArrayList<>();
		lineItems.add(shippableLineItem);
		lineItems.add(nonShippableLineItem);

		AbstractShippingCostCalculationMethodImpl method = new AbstractShippingCostCalculationMethodImpl() {

			private static final long serialVersionUID = 2168107203838643471L;

			@Override
			protected String getMethodType() {
				return DUMMY_METHOD_TYPE;
			}

			@Override
			public String[] getParameterKeys() {
				return DUMMY_PARAMETER_KEYS.clone();
			}

			@Override
			public String getDisplayText() {
				return DUMMY_METHOD_TEXT;
			}

			@Override
			public Money calculateShippingCost(final Collection<? extends ShoppingItem> lineItems,
					final Money shippableItemsSubtotal,
					final Currency currency,
					final ProductSkuLookup productSkuLookup) {
				return null;
			}
		};

		method.calculateTotalWeight(lineItems, productSkuLookup);
	}

}
