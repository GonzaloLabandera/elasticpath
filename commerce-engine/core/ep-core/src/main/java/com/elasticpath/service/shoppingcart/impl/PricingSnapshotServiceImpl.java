/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.shoppingcart.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.shipping.ShippingCostCalculationMethod;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.DiscountRecord;
import com.elasticpath.domain.shoppingcart.MutablePromotionRecordContainer;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.money.Money;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.rules.EpRuleEngine;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShippableItemsSubtotalCalculator;

/**
 * Methods for getting a pricing snapshot.
 */
@SuppressWarnings("PMD.GodClass") // TODO this is a legitimate god class. Recommend refactoring as part of cart concurrency/perf initiative.
public class PricingSnapshotServiceImpl implements PricingSnapshotService {

	private EpRuleEngine ruleEngine;

	private BeanFactory beanFactory;

	private CartDirector cartDirector;

	private ProductSkuLookup productSkuLookup;
	private ShippableItemsSubtotalCalculator shippableItemsSubtotalCalculator;

	@Override
	public ShoppingCartPricingSnapshot getPricingSnapshotForCart(final ShoppingCart shoppingCart) {
		if (shoppingCart == null) {
			throw new IllegalArgumentException("ShoppingCart parameter must not be null.");
		}

		if (shoppingCart instanceof ShoppingCartImpl) {
			fireRules((ShoppingCartImpl) shoppingCart);

			return (ShoppingCartPricingSnapshot) shoppingCart;
		}

		throw new IllegalArgumentException(
				"This implementation expects ShoppingCart to be a ShoppingCartImpl.  "
						+ "Unexpected ShoppingCart type: [" + shoppingCart.getClass() + "]");
	}

	@Override
	public ShoppingItemPricingSnapshot getPricingSnapshotForOrderSku(final OrderSku orderSku) {
		return (ShoppingItemPricingSnapshot) orderSku;
	}

	/**
	 * <p>Forces the shopping cart to apply promotion rules. Promotion rules will usually be applied by the cart automatically as required.
	 * However, it is sometimes necessary to force the cart to fire rules when the cart is loaded without a state change.</p>
	 * <p>In case of exclusive tax calculation, the order discount is deducted before-tax; however, in case of inclusive tax calculation,
	 * the order discount is deducted after-tax.</p>
	 *
	 * @param shoppingCart the shopping cart
	 */
	protected void fireRules(final ShoppingCartImpl shoppingCart) {
		if (shoppingCart.isExchangeOrderShoppingCart()) {
			calculatePrePromotionShippingPrices(shoppingCart, shoppingCart, shoppingCart.getShippingServiceLevelList());
		} else {
			shoppingCart.clearPromotions();

			final MutablePromotionRecordContainer promotionRecordContainer = shoppingCart.getMutablePromotionRecordContainer();
			promotionRecordContainer.clear();

			// Ensure catalog promotions are applied
			getCartDirector().refresh(shoppingCart);
			storeCatalogPromotionRecords(shoppingCart, shoppingCart, promotionRecordContainer);

			getRuleEngine().fireOrderPromotionRules(shoppingCart, shoppingCart.getCustomerSession());

			calculatePrePromotionShippingPrices(shoppingCart, shoppingCart, shoppingCart.getShippingServiceLevelList());

			getRuleEngine().fireOrderPromotionSubtotalRules(shoppingCart, shoppingCart.getCustomerSession());

			// Discount Record Container contains two separate collections that denote rule application:
			// 1. The set of rules that were applicable to this cart/order (includes rules that were superseded by better promos)
			// 2. The set of rules that are flagged as Limited Usage, which have not exceeded the usage allocation, **even if not applicable to this
			// cart/order**.
			//
			// The limited usage rules are set on the cart from within a Rule Condition (LimitedUsagePromotionConditionImpl), but _other_ conditions
			// within the same rule may not match the cart, which means the rule should not actually apply.
			//
			// This block of code looks through the limited usage rules collection and removes anything that was never actually applied.
			//
			// This entire scenario is all rather non-obvious - if you weren't familiar with the effects of LimitedUsagePromotionConditionImpl (and
			// why would you be?), this will be quite confusing. This is definitely a candidate for refactoring.  Perhaps this complexity could be
			// concealed within the DiscountRecordContainer itself?
			for (final Map.Entry<String, Long> limitedUsageRuleEntry : promotionRecordContainer.getLimitedUsagePromotionRuleCodes().entrySet()) {
				if (!promotionRecordContainer.getAppliedRules().contains(limitedUsageRuleEntry.getValue())) {
					promotionRecordContainer.removeLimitedUsagePromotionRuleCode(limitedUsageRuleEntry.getKey());
				}
			}
		}

	}

	/**
	 * For each cart item, retrieves any and all {@link DiscountRecord} corresponding to a catalog promo (i.e. those stored on the
	 * {@link com.elasticpath.domain.catalog.Price Price}), and sets them to the given
	 * {@link MutablePromotionRecordContainer PromotionRecordContainer}.
	 *
	 * @param shoppingCart the shopping cart
	 * @param cartPricingSnapshot the pricing snapshot corresponding to the shopping cart
	 * @param promotionRecordContainer the promotion record container onto which the catalog discount records should be stored
	 */
	private void storeCatalogPromotionRecords(final ShoppingCart shoppingCart,
												final ShoppingCartPricingSnapshot cartPricingSnapshot,
												final MutablePromotionRecordContainer promotionRecordContainer) {
		for (final ShoppingItem shoppingItem : shoppingCart.getAllItems()) {
			final ShoppingItemPricingSnapshot shoppingItemPricingSnapshot = cartPricingSnapshot.getShoppingItemPricingSnapshot(shoppingItem);
			final Collection<DiscountRecord> discountRecords = shoppingItemPricingSnapshot.getPrice().getDiscountRecords();
			for (final DiscountRecord discountRecord : discountRecords) {
				promotionRecordContainer.addDiscountRecord(discountRecord);
			}
		}
	}

	/**
	 * Calculates the cost of each shipping service level prior to applying shipping discounts.
	 *
	 * @param shoppingCart the shopping cart
	 * @param cartPricingSnapshot the pricing snapshot corresponding to the shopping cart
	 * @param shippingServiceLevelList the shipping service levels for which prices should be calculated
	 */
	protected void calculatePrePromotionShippingPrices(final ShoppingCartImpl shoppingCart,
														final ShoppingCartPricingSnapshot cartPricingSnapshot,
														final Iterable<ShippingServiceLevel> shippingServiceLevelList) {
		final Collection<ShoppingItem> apportionedLeafItems = shoppingCart.getApportionedLeafItems();

		final Currency currency = shoppingCart.getCustomerSession().getCurrency();
		final Money shippableItemsSubtotal = getShippableItemsSubtotalCalculator().calculateSubtotalOfShippableItems(apportionedLeafItems,
																									cartPricingSnapshot,
																									currency);
		for (final ShippingServiceLevel shippingServiceLevel : shippingServiceLevelList) {
			final ShippingCostCalculationMethod calculationMethod = shippingServiceLevel.getShippingCostCalculationMethod();

			final Money shippingCost = calculationMethod.calculateShippingCost(apportionedLeafItems,
																		shippableItemsSubtotal,
																		currency,
																		getProductSkuLookup());

			shoppingCart.setShippingListPrice(shippingServiceLevel.getCode(), shippingCost);
		}
	}

	/**
	 * Calculate the discount for shipment total.
	 *
	 * @param discountByShoppingItem map of discounts per shopping item
	 * @param shoppingItems the shopping items
	 * @param splitShipmentMode whether this is a split shipment
	 * @param cartSubtotalDiscount the cart subtotal discount
	 * @return the discount for shipment total
	 */
	protected Money getDiscountForShipment(final Map<String, BigDecimal> discountByShoppingItem, final List<OrderSku> shoppingItems,
											final boolean splitShipmentMode, final Money cartSubtotalDiscount) {
		if (!splitShipmentMode) {
			return cartSubtotalDiscount;
		}
		BigDecimal discount = BigDecimal.ZERO;
		for (OrderSku sku : shoppingItems) {
			if (discountByShoppingItem.containsKey(sku.getGuid())) {
				discount = discount.add(discountByShoppingItem.get(sku.getGuid()));
			}
		}
		return Money.valueOf(discount, cartSubtotalDiscount.getCurrency());
	}

	protected EpRuleEngine getRuleEngine() {
		return ruleEngine;
	}

	public void setRuleEngine(final EpRuleEngine ruleEngine) {
		this.ruleEngine = ruleEngine;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected ProductSkuLookup getProductSkuLookup() {
		return productSkuLookup;
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

	protected CartDirector getCartDirector() {
		return cartDirector;
	}

	public void setCartDirector(final CartDirector cartDirector) {
		this.cartDirector = cartDirector;
	}

	public void setShippableItemsSubtotalCalculator(final ShippableItemsSubtotalCalculator shippableItemsSubtotalCalculator) {
		this.shippableItemsSubtotalCalculator = shippableItemsSubtotalCalculator;
	}

	protected ShippableItemsSubtotalCalculator getShippableItemsSubtotalCalculator() {
		return shippableItemsSubtotalCalculator;
	}

}
