/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.calc.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shoppingcart.PriceCalculator;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.money.Money;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.PricingSnapshotRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.tax.TaxCalculationResult;

@RunWith(MockitoJUnitRunner.class)
public class CartTotalsCalculatorImplTest {

	private static final String STORE_CODE = "TEST_STORE";
	private static final String EXISTS_GUID = "exists guid";
	private static final String NOT_EXISTS_GUID = "not exists guid";
	private static final String LINE_ITEM_GUID = "line item guid";
	private static final String SKU_GUID = "sku guid";
	private static final String SKU_CODE = "sku code";
	private static final Money ZERO_CAD = Money.valueOf(BigDecimal.ZERO, Currency.getInstance("CAD"));
	private static final Money TEN_CAD = Money.valueOf(BigDecimal.TEN, Currency.getInstance("CAD"));

	@Mock
	private ShoppingCart shoppingCart;
	@Mock
	private ShoppingCartPricingSnapshot shoppingCartPricingSnapshot;
	@Mock
	private ShoppingItem shoppingItem;
	@Mock
	private ProductSku productSku;
	@Mock
	private ShoppingItemPricingSnapshot shoppingItemPricingSnapshot;
	@Mock
	private PriceCalculator priceCalculator;
	@Mock
	private ShoppingCartTaxSnapshot cartTaxSnapshot;
	@Mock
	private CartOrderRepository cartOrderRepository;
	@Mock
	private PricingSnapshotRepository pricingSnapshotRepository;
	@Mock
	private ProductSkuLookup productSkuLookup;
	@Mock
	private ExceptionTransformer exceptionTransformer;

	@InjectMocks
	private CartTotalsCalculatorImpl calculator;

	@Test
	public void ensureTotalIsCalculatedBeforeTotalIsReadForShoppingCartWithNoSubtotalDiscount() {
		when(cartOrderRepository.getEnrichedShoppingCart(STORE_CODE, EXISTS_GUID, CartOrderRepository.FindCartOrder.BY_CART_GUID))
				.thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshot(shoppingCart))
				.thenReturn(Single.just(shoppingCartPricingSnapshot));

		when(shoppingCartPricingSnapshot.getSubtotalMoney()).thenReturn(TEN_CAD);

		calculator.calculateTotalForShoppingCart(STORE_CODE, EXISTS_GUID)
				.test()
				.assertNoErrors()
				.assertValue(TEN_CAD);
	}

	@Test
	public void ensureErrorPropagationOfFailedGetShoppingCartWhenCalculatingShoppingCartTotal() {
		when(cartOrderRepository.getEnrichedShoppingCart(STORE_CODE, NOT_EXISTS_GUID, CartOrderRepository.FindCartOrder.BY_CART_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));

		calculator.calculateTotalForShoppingCart(STORE_CODE, NOT_EXISTS_GUID)
				.test()
				.assertError(ResourceOperationFailure.class);
	}

	@Test
	public void ensureTotalIsCalculatedBeforeTotalIsReadForCartOrder() {
		when(cartOrderRepository.getEnrichedShoppingCart(STORE_CODE, EXISTS_GUID, CartOrderRepository.FindCartOrder.BY_ORDER_GUID))
				.thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartTaxSnapshot(shoppingCart)).thenReturn(Single.just(cartTaxSnapshot));
		when(cartTaxSnapshot.getTotalMoney()).thenReturn(ZERO_CAD);

		calculator.calculateTotalForCartOrder(STORE_CODE, EXISTS_GUID)
				.test()
				.assertNoErrors()
				.assertValue(ZERO_CAD);
	}

	@Test
	public void ensureErrorPropagationOfFailedGetCartWhenCalculatingCartOrderTotal() {
		when(cartOrderRepository.getEnrichedShoppingCart(STORE_CODE, NOT_EXISTS_GUID, CartOrderRepository.FindCartOrder.BY_ORDER_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));

		calculator.calculateTotalForCartOrder(STORE_CODE, NOT_EXISTS_GUID)
				.test()
				.assertError(ResourceOperationFailure.class);
	}

	@Test
	public void testCalculateTotalForShoppingCart() {
		when(cartOrderRepository.getEnrichedShoppingCart(STORE_CODE, EXISTS_GUID, CartOrderRepository.FindCartOrder.BY_CART_GUID))
				.thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshot(shoppingCart))
				.thenReturn(Single.just(shoppingCartPricingSnapshot));

		when(shoppingCartPricingSnapshot.getSubtotalMoney()).thenReturn(TEN_CAD);

		calculator.calculateTotalForShoppingCart(STORE_CODE, EXISTS_GUID)
				.test()
				.assertNoErrors()
				.assertValue(TEN_CAD);
	}

	@Test
	public void testCalculateSubTotalWithoutTaxForCartOrder() {
		when(cartOrderRepository.getEnrichedShoppingCart(STORE_CODE, EXISTS_GUID, CartOrderRepository.FindCartOrder.BY_ORDER_GUID))
				.thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshot(shoppingCart))
				.thenReturn(Single.just(shoppingCartPricingSnapshot));
		when(shoppingCartPricingSnapshot.getSubtotalMoney()).thenReturn(TEN_CAD);

		calculator.calculateSubTotalForCartOrder(STORE_CODE, EXISTS_GUID)
				.test()
				.assertNoErrors()
				.assertValue(TEN_CAD);
	}

	@Test
	public void testCalculateTotalForLineItem() {
		when(cartOrderRepository.getEnrichedShoppingCart(STORE_CODE, EXISTS_GUID, CartOrderRepository.FindCartOrder.BY_CART_GUID))
				.thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshot(shoppingCart))
				.thenReturn(Single.just(shoppingCartPricingSnapshot));
		when(shoppingCart.getCartItemByGuid(LINE_ITEM_GUID))
				.thenReturn(shoppingItem);
		when(shoppingCartPricingSnapshot.getShoppingItemPricingSnapshot(shoppingItem))
				.thenReturn(shoppingItemPricingSnapshot);
		when(shoppingItemPricingSnapshot.hasPrice())
				.thenReturn(true);
		when(shoppingItemPricingSnapshot.getPriceCalc())
				.thenReturn(priceCalculator);
		when(priceCalculator.withCartDiscounts())
				.thenReturn(priceCalculator);
		when(priceCalculator.getMoney())
				.thenReturn(ZERO_CAD);

		calculator.calculateTotalForShoppingItem(STORE_CODE, EXISTS_GUID, LINE_ITEM_GUID)
				.test()
				.assertNoErrors()
				.assertValue(ZERO_CAD);
	}

	@Test
	public void testGetShoppingItemSnapshot() {
		ShoppingCart shoppingCart = mock(ShoppingCart.class);
		when(cartOrderRepository.getEnrichedShoppingCart(STORE_CODE, EXISTS_GUID, CartOrderRepository.FindCartOrder.BY_CART_GUID))
				.thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshot(shoppingCart))
				.thenReturn(Single.just(shoppingCartPricingSnapshot));
		when(shoppingCart.getCartItemByGuid(LINE_ITEM_GUID))
				.thenReturn(shoppingItem);
		when(shoppingCartPricingSnapshot.getShoppingItemPricingSnapshot(shoppingItem))
				.thenReturn(shoppingItemPricingSnapshot);
		when(shoppingItemPricingSnapshot.hasPrice())
				.thenReturn(true);

		calculator.getShoppingItemPricingSnapshot(STORE_CODE, EXISTS_GUID, LINE_ITEM_GUID)
				.test()
				.assertNoErrors()
				.assertValue(shoppingItemPricingSnapshot);
	}

	@Test
	public void testGetShoppingItemSnapshotNoPrice() {
		ShoppingCart shoppingCart = mock(ShoppingCart.class);
		when(cartOrderRepository.getEnrichedShoppingCart(STORE_CODE, EXISTS_GUID, CartOrderRepository.FindCartOrder.BY_CART_GUID))
				.thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshot(shoppingCart))
				.thenReturn(Single.just(shoppingCartPricingSnapshot));
		when(shoppingCart.getCartItemByGuid(LINE_ITEM_GUID))
				.thenReturn(shoppingItem);
		when(shoppingCartPricingSnapshot.getShoppingItemPricingSnapshot(shoppingItem))
				.thenReturn(shoppingItemPricingSnapshot);
		when(shoppingItemPricingSnapshot.hasPrice())
				.thenReturn(false);
		when(shoppingItem.getSkuGuid())
				.thenReturn(SKU_GUID);
		when(productSkuLookup.findByGuid(SKU_GUID))
				.thenReturn(productSku);
		when(productSku.getSkuCode())
				.thenReturn(SKU_CODE);
		when(exceptionTransformer.getResourceOperationFailure(any(EpValidationException.class)))
				.thenReturn(ResourceOperationFailure.notFound());

		calculator.getShoppingItemPricingSnapshot(STORE_CODE, EXISTS_GUID, LINE_ITEM_GUID)
				.test()
				.assertError(throwable -> ((ResourceOperationFailure) throwable).getResourceStatus().equals(ResourceStatus.NOT_FOUND));
	}

	@Test
	public void ensureTaxIsCalculatedBeforeTaxIsRead() {
		ShoppingCart shoppingCart = mock(ShoppingCart.class);
		when(cartOrderRepository.getEnrichedShoppingCart(STORE_CODE, EXISTS_GUID, CartOrderRepository.FindCartOrder.BY_ORDER_GUID))
				.thenReturn(Single.just(shoppingCart));

		ShoppingCartTaxSnapshot taxSnapshot = mock(ShoppingCartTaxSnapshot.class);
		when(pricingSnapshotRepository.getShoppingCartTaxSnapshot(shoppingCart)).thenReturn(Single.just(taxSnapshot));

		TaxCalculationResult expectedTax = mock(TaxCalculationResult.class);
		when(taxSnapshot.getTaxCalculationResult()).thenReturn(expectedTax);

		calculator.calculateTax(STORE_CODE, EXISTS_GUID)
				.test()
				.assertNoErrors()
				.assertValue(expectedTax);
	}

	@Test
	public void ensureErrorPropagationOfFailedGetCartWhenCalculatingTax() {
		when(cartOrderRepository.getEnrichedShoppingCart(STORE_CODE, NOT_EXISTS_GUID, CartOrderRepository.FindCartOrder.BY_ORDER_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));

		calculator.calculateTax(STORE_CODE, NOT_EXISTS_GUID)
				.test()
				.assertError(throwable -> ((ResourceOperationFailure) throwable).getResourceStatus().equals(ResourceStatus.NOT_FOUND));
	}
}