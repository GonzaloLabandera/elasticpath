/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.calc.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository.FindCartOrder.BY_CART_GUID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository.FindCartOrder.BY_ORDER_GUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.reactivex.Single;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.money.Money;
import com.elasticpath.rest.resource.integration.epcommerce.repository.calc.CartTotalsCalculator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.PricingSnapshotRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.tax.TaxCalculationResult;

/**
 * <p>
 * The CartTotalsCalculator is responsible calculating ShoppingCart related totals.
 * </p>
 * <p>
 * Since totals include discounts, please use the enriched ShoppingCart for all
 * totals calculations.  See {@link CartOrderRepository#getEnrichedShoppingCart(String, String,
 *                                    com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository.FindCartOrder)}.
 * NOTE: Please do not call ShoppingCartImpl.fireRules() directly.  Enriched cart will handle this for you.
 */
@Singleton
@Named("cartTotalsCalculator")
public class CartTotalsCalculatorImpl implements CartTotalsCalculator {

	private final CartOrderRepository cartOrderRepository;
	private final PricingSnapshotRepository pricingSnapshotRepository;
	private final ExceptionTransformer exceptionTransformer;
	private final ProductSkuLookup productSkuLookup;

	/**
	 * Constructor.
	 *
	 * @param cartOrderRepository       the cart order repository
	 * @param pricingSnapshotRepository the pricing snapshot repository
	 * @param exceptionTransformer      the exception transformer
	 * @param productSkuLookup          the product sku lookup
	 */
	@Inject
	CartTotalsCalculatorImpl(
			@Named("cartOrderRepository") final CartOrderRepository cartOrderRepository,
			@Named("pricingSnapshotRepository") final PricingSnapshotRepository pricingSnapshotRepository,
			@Named("exceptionTransformer") final ExceptionTransformer exceptionTransformer,
			@Named("productSkuLookup") final ProductSkuLookup productSkuLookup) {
		this.cartOrderRepository = cartOrderRepository;
		this.pricingSnapshotRepository = pricingSnapshotRepository;
		this.exceptionTransformer = exceptionTransformer;
		this.productSkuLookup = productSkuLookup;
	}

	@Override
	public Single<Money> calculateTotalForShoppingCart(final String storeCode, final String shoppingCartGuid) {
		return cartOrderRepository.getEnrichedShoppingCart(storeCode, shoppingCartGuid, BY_CART_GUID)
				.flatMap(pricingSnapshotRepository::getShoppingCartPricingSnapshot)
				.map(ShoppingCartPricingSnapshot::getSubtotalMoney);
	}

	@Override
	public Single<Money> calculateTotalForCartOrder(final String storeCode, final String cartOrderGuid) {
		return cartOrderRepository.getEnrichedShoppingCart(storeCode, cartOrderGuid, BY_ORDER_GUID)
				.flatMap(pricingSnapshotRepository::getShoppingCartTaxSnapshot)
				.map(ShoppingCartTaxSnapshot::getTotalMoney);
	}

	@Override
	public Single<Money> calculateSubTotalForCartOrder(final String storeCode, final String cartOrderGuid) {
		return cartOrderRepository.getEnrichedShoppingCart(storeCode, cartOrderGuid, BY_ORDER_GUID)
				.flatMap(pricingSnapshotRepository::getShoppingCartPricingSnapshot)
				.map(ShoppingCartPricingSnapshot::getSubtotalMoney);
	}

	@Override
	public Single<TaxCalculationResult> calculateTax(final String storeCode, final String cartOrderGuid) {
		return cartOrderRepository.getEnrichedShoppingCart(storeCode, cartOrderGuid, CartOrderRepository.FindCartOrder.BY_ORDER_GUID)
				.flatMap(pricingSnapshotRepository::getShoppingCartTaxSnapshot)
				.map(ShoppingCartTaxSnapshot::getTaxCalculationResult);
	}

	@Override
	public Single<Money> calculateTotalForShoppingItem(final String storeCode, final String shoppingCartGuid, final String cartItemGuid) {
		return getShoppingItemPricingSnapshot(storeCode, shoppingCartGuid, cartItemGuid)
				.map(this::getLineItemTotalFromCartPricingSnapshot);
	}

	@Override
	public Single<ShoppingItemPricingSnapshot> getShoppingItemPricingSnapshot(final String storeCode, final String shoppingCartGuid,
																			  final String cartItemGuid) {
		return cartOrderRepository.getEnrichedShoppingCart(storeCode, shoppingCartGuid, BY_CART_GUID)
				.flatMap(shoppingCart -> getShoppingItemPricingSnapshotIfPricePresent(shoppingCart, cartItemGuid));
	}

	@Override
	public boolean shoppingItemHasPrice(final String storeCode, final String shoppingCartGuid, final String cartItemGuid) {
		return cartOrderRepository.getEnrichedShoppingCart(storeCode, shoppingCartGuid, BY_CART_GUID)
				.flatMap(shoppingCart -> getShoppingItemPricingSnapshot(shoppingCart, cartItemGuid))
				.map(ShoppingItemPricingSnapshot::hasPrice)
				.blockingGet();
	}

	/**
	 * Get shopping item pricing snapshot for a shopping cart and cart item guid, or an error if the item doesn't have a price.
	 *
	 * @param shoppingCart the shopping cart
	 * @param cartItemGuid the cart item guid
	 * @return shopping item pricing snapshot
	 */
	protected Single<ShoppingItemPricingSnapshot> getShoppingItemPricingSnapshot(final ShoppingCart shoppingCart, final String cartItemGuid) {
		return pricingSnapshotRepository.getShoppingCartPricingSnapshot(shoppingCart)
				.map(shoppingCartPricingSnapshot -> shoppingCartPricingSnapshot.getShoppingItemPricingSnapshot(
						shoppingCart.getCartItemByGuid(cartItemGuid)));
	}

	/**
	 * Get shopping item pricing snapshot for a shopping cart and cart item guid, or an error if the item doesn't have a price.
	 *
	 * @param shoppingCart the shopping cart
	 * @param cartItemGuid the cart item guid
	 * @return shopping item pricing snapshot
	 */
	protected Single<ShoppingItemPricingSnapshot> getShoppingItemPricingSnapshotIfPricePresent(final ShoppingCart shoppingCart,
																							   final String cartItemGuid) {
		return pricingSnapshotRepository.getShoppingCartPricingSnapshot(shoppingCart)
				.map(shoppingCartPricingSnapshot -> shoppingCartPricingSnapshot.getShoppingItemPricingSnapshot(
						shoppingCart.getCartItemByGuid(cartItemGuid)))
				.flatMap(shoppingItemPricingSnapshot -> ensureShoppingItemPricingSnapshotHasPrice(shoppingItemPricingSnapshot,
						shoppingCart.getCartItemByGuid(cartItemGuid)));
	}

	/**
	 * Ensure that the passed shopping item pricing snapshot contains a price, and return an error if it does not.
	 *
	 * @param shoppingItemPricingSnapshot the shopping item pricing snapshot
	 * @param shoppingItem the shopping item
	 * @return shopping item pricing snapshot
	 */
	protected Single<ShoppingItemPricingSnapshot> ensureShoppingItemPricingSnapshotHasPrice(
			final ShoppingItemPricingSnapshot shoppingItemPricingSnapshot, final ShoppingItem shoppingItem) {
		if (!shoppingItemPricingSnapshot.hasPrice()) {
			return buildItemMissingPriceMessage(shoppingItem);
		}

		return Single.just(shoppingItemPricingSnapshot);
	}

	/**
	 * Return an observable error message indicating that price is missing.
	 *
	 * @param shoppingItem the shopping item
	 * @return the observable error
	 */
	protected Single<ShoppingItemPricingSnapshot> buildItemMissingPriceMessage(final ShoppingItem shoppingItem) {
		String skuCode = productSkuLookup.findByGuid(shoppingItem.getSkuGuid()).getSkuCode();
		String errorMessage = String.format("Item '%s' does not have a price.", skuCode);

		StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage("item.missing.price",
				errorMessage,
				ImmutableMap.of("item-code", skuCode));

		return Single.error(exceptionTransformer.getResourceOperationFailure(
				new EpValidationException(errorMessage, ImmutableList.of(structuredErrorMessage))));
	}

	/**
	 * Get the line item total from the ShoppingItemPricingSnapshot.
	 *
	 * @param shoppingItemPricingSnapshot shopping item pricing snapshot
	 * @return the line item total
	 */
	protected Money getLineItemTotalFromCartPricingSnapshot(final ShoppingItemPricingSnapshot shoppingItemPricingSnapshot) {
		return shoppingItemPricingSnapshot
				.getPriceCalc()
				.withCartDiscounts()
				.getMoney();
	}
}