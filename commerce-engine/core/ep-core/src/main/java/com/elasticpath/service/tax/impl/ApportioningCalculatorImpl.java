/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
/**
 * 
 */
package com.elasticpath.service.tax.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.tax.ApportioningCalculator;


/**
 * Apportions a given amount among multiple items proportionally to the items'
 * values.
 * 
 * Example: A price of a bundle is $149.99 It consists of a camera, whose
 * original price is $200 and a bag $50
 * 
 * Let's call $149.99 - amountToApportion $200 and $50 - portions.
 * 
 * The expected result is: camera $119.99 bag $30.00 Let's call these numbers
 * proportions.
 * 
 */
public class ApportioningCalculatorImpl implements ApportioningCalculator {
	/**
	 * Default scale.
	 */
	protected static final int DEFAULT_SCALE = 2;
	/**
	 * Dividing scale. 
	 */
	protected static final int DIVIDE_SCALE = 10;
	
	private final DiscountableItemSorter sorter = new DiscountableItemSorter();
	private ProductSkuLookup productSkuLookup;

	/**
	 * Calculates apportioned amounts.
	 * 
	 * @param amountToApportion
	 *            amount to apportion.
	 * @param amounts
	 *            amounts.
	 * @return a map of apportioned amounts.
	 */
	@Override
	public Map<String, BigDecimal> calculateApportionedAmounts(
			final BigDecimal amountToApportion,
			final Map<String, BigDecimal> amounts) {
		final BigDecimal total = sumAmounts(amounts.values());
		if (total.compareTo(BigDecimal.ZERO) == 0) {
			return zeroApportionedAmounts(amounts);
		}

		return apportionedAmounts(amountToApportion, amounts, total);
	}

	private Map<String, BigDecimal> apportionedAmounts(final BigDecimal amountToApportion, final Map<String, BigDecimal> amounts,
			final BigDecimal total) {
		final Map<String, BigDecimal> apportionedAmounts = new HashMap<>();
		
		populateProportions(apportionedAmounts, amountToApportion, amounts, total);
		
		correctRoundingError(amountToApportion, amounts, apportionedAmounts);

		return apportionedAmounts;
	}

	/**
	 * Corrects rounding error.
	 * @param amountToApportion amountToApportion.
	 * @param amounts amounts.
	 * @param apportionedAmounts apportionedAmounts.
	 */
	protected void correctRoundingError(final BigDecimal amountToApportion, final Map<String, BigDecimal> amounts,
			final Map<String, BigDecimal> apportionedAmounts) {
		BigDecimal apportionedTotal = sumAmounts(apportionedAmounts.values());	
		
		if (apportionedTotal.compareTo(amountToApportion) != 0) {
			BigDecimal roundingError = amountToApportion.subtract(
					apportionedTotal);
			applyRoundingError(amounts, apportionedAmounts, roundingError);
		}
	}

	private void populateProportions(final Map<String, BigDecimal> apportionedAmounts, final BigDecimal amountToApportion,
			final Map<String, BigDecimal> amounts, final BigDecimal total) {
		for (Entry<String, BigDecimal> amountEntry : amounts.entrySet()) {
			BigDecimal apportionedPart = calculateProportion(amountToApportion, amountEntry.getValue(), total);
			apportionedAmounts.put(amountEntry.getKey(), apportionedPart);
		}
	}

	private Map<String, BigDecimal> zeroApportionedAmounts(final Map<String, BigDecimal> amounts) {
		Map<String, BigDecimal> apportionedAmounts = new HashMap<>();
		for (Entry<String, BigDecimal> amountEntry : amounts.entrySet()) {
			apportionedAmounts.put(amountEntry.getKey(), BigDecimal.ZERO);
		}
		return apportionedAmounts;
	}

	/**
	 * It is possible for rounding to cause the sum of the individual discounts
	 * to vary from the original discount. This method looks through the
	 * discounted items in order and modifies the discounts to correct the
	 * rounding error.
	 * @param amountsBeforeApportioning
	 *            amounts before apportioning.
	 * @param amountsAfterApportioning 
	 * 			  amounts after apportioning.
	 * @param roundingError
	 *            the amount of error in the discount, may be positive or
	 *            negative.
	 */
	protected void applyRoundingError(final Map<String, BigDecimal> amountsBeforeApportioning,
			final Map<String, BigDecimal> amountsAfterApportioning, final BigDecimal roundingError) {
		BigDecimal remainingError = roundingError;
		for (Entry<String, BigDecimal> amountEntry : amountsBeforeApportioning.entrySet()) {
			if (remainingError.compareTo(BigDecimal.ZERO) == 0) {
				return;
			}

			String identifier = amountEntry.getKey();
			BigDecimal amountAfterApportioning = amountsAfterApportioning.get(identifier);

			BigDecimal errorAdjustment = calculateErrorAdjustment(amountEntry.getValue(), amountAfterApportioning, remainingError);
			amountsAfterApportioning.put(identifier, amountAfterApportioning.add(errorAdjustment));
			
			remainingError = remainingError.subtract(errorAdjustment);
		}
	}

	/**
	 * 
	 * @param amountBeforeApportioning
	 *            amount before apportioning.
	 * @param amountAfterApportioning
	 *            amount after apportioning.
	 * @param remainingRoundingError
	 *            the rounding error left in the discount sum
	 * @return the amount of the error which can be taken off the discount for
	 *         this item
	 */
	protected BigDecimal calculateErrorAdjustment(
			final BigDecimal amountBeforeApportioning,
			final BigDecimal amountAfterApportioning, final BigDecimal remainingRoundingError) {
		if (amountBeforeApportioning.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}

		if (remainingRoundingError.compareTo(BigDecimal.ZERO) < 0) {
			return negativeAdjustment(amountAfterApportioning,
					remainingRoundingError);
		}

		return positiveAdjustment(amountBeforeApportioning,
				amountAfterApportioning, remainingRoundingError);
	}

	/**
	 * Calculates the positive remaining rounding error. For bundles, it should just return it 
	 * since there is no adjustment required. For discounts, it should compare the adjustment 
	 * result to the amountBeforeApportioning. If it's negative, it should take the
	 * re-adjust the discount.
	 * 
	 * @param amountBeforeApportioning amountBeforeApportioning
	 * @param amountAfterApportioning amountAfterApportioning
	 * @param remainingRoundingError remainingRoundingError
	 * @return the adjusted remaining rounding error
	 */
	protected BigDecimal positiveAdjustment(final BigDecimal amountBeforeApportioning, final BigDecimal amountAfterApportioning,
			final BigDecimal remainingRoundingError) {
		return remainingRoundingError;
	}

	private BigDecimal negativeAdjustment(final BigDecimal amountAfterApportioning, final BigDecimal remainingRoundingError) {
		BigDecimal amountAfterAdjustment = amountAfterApportioning.add(remainingRoundingError);
		if (amountAfterAdjustment.compareTo(BigDecimal.ZERO) < 0) {
			return amountAfterApportioning.negate();
		}
		return remainingRoundingError;
	}

	/**
	 * Calculates the proportion of the total contributed by the supplied
	 * portion and returns a proportional amount of the amountToApportion.
	 * 
	 * Invariants: 0 <= portion <= total proportion <= portion
	 * @param amountToApportion
	 *            the amount of discount to apportion
	 * @param portion
	 *            price of item to receive discount
	 * @param total
	 *            the total price of all items the discount applies to
	 * 
	 * @return proportional amount
	 */
	protected BigDecimal calculateProportion(final BigDecimal amountToApportion,
			final BigDecimal portion, final BigDecimal total) {
		BigDecimal proportion = portion.divide(total, DIVIDE_SCALE,
				RoundingMode.HALF_UP);
		return proportion.multiply(amountToApportion).setScale(DEFAULT_SCALE,
				RoundingMode.HALF_UP);
	}

	/**
	 * Summarises the amounts of the given collections.
	 * 
	 * @param amounts a collection
	 * @return total of amounts
	 */
	protected BigDecimal sumAmounts(final Collection<BigDecimal> amounts) {
		BigDecimal total = BigDecimal.ZERO;
		for (BigDecimal amount : amounts) {
			total = total.add(amount);
		}
		return total;
	}
	
	/**
	 * Extract {@link DiscountableItem} from {@link ShoppingItem}.
	 *
	 * @param shoppingItemPricingSnapshotMap a map of {@link ShoppingItem} to {@link ShoppingItemPricingSnapshot}
	 * @return a list of {@link DiscountableItem}.
	 */
	protected Collection<DiscountableItem> extractShoppingItem(
			final Map<? extends ShoppingItem, ShoppingItemPricingSnapshot> shoppingItemPricingSnapshotMap) {
		List<DiscountableItem> items = new ArrayList<>();
		for (final Entry<? extends ShoppingItem, ShoppingItemPricingSnapshot> entry : shoppingItemPricingSnapshotMap.entrySet()) {
			final ShoppingItem item = entry.getKey();
			final ShoppingItemPricingSnapshot itemPricingSnapshot = entry.getValue();

			//On Order exchange wizard tax calculation may be invoked 
			//on item without unit price
			if (!item.hasPrice()) {
				continue;
			}
			String guid = item.getGuid();
			BigDecimal amount = itemPricingSnapshot.getPriceCalc().withCartDiscounts().getAmount();
			ProductSku productSku = getProductSkuLookup().findByGuid(item.getSkuGuid());
			String skuCode = productSku.getSkuCode();
			
			DiscountableItem shoppingItemExtraction = new DiscountableItem(guid, skuCode, amount);
			items.add(shoppingItemExtraction);
		}
		
		return items;
	}

	/**
	 * Creates a price map sorted by price then sku code.
	 * @param shoppingItemPricingSnapshotMap a map of {@link ShoppingItem} to {@link ShoppingItemPricingSnapshot}.
	 * @return price map.
	 */
	protected Map<String, BigDecimal> createSortedPriceMap(final Map<? extends ShoppingItem, ShoppingItemPricingSnapshot>
																	shoppingItemPricingSnapshotMap) {
		final List<DiscountableItem> sortedItems = sorter.sortByPriceSku(extractShoppingItem(shoppingItemPricingSnapshotMap));
		return createPriceMapInOrder(sortedItems);
	}
	
	/**
	 * Creates a price map in order using {@link LinkedHashMap}.
	 * @param sortedItems a list of sorted {@link ShoppingItem}.
	 * @return the price map.
	 */
	private Map<String, BigDecimal> createPriceMapInOrder(final List<DiscountableItem> sortedItems) {
		Map<String, BigDecimal> sortedPriceMap = new LinkedHashMap<>();
		for (DiscountableItem orderingValue : sortedItems) {
			sortedPriceMap.put(orderingValue.getGuid(), orderingValue.getAmount());
		}
		return sortedPriceMap;
	}


	/**
	 * Flattens collection of ShoppingItems.
	 * I.e. returns only leaves:
	 * the item itself if it is not a bundle 
	 * or a list of the item's constituent if it is a bundle.
	 * 
	 * @param items input items
	 * @return selected items
	 */
	public List<ShoppingItem> selectApportionableItems(final Collection<ShoppingItem> items) {
		List<ShoppingItem> leaves = new ArrayList<>();
		populateLeaves(leaves, items);
		return leaves;
	}

	private void populateLeaves(final List<ShoppingItem> results, final Collection<ShoppingItem> items) {
		for (ShoppingItem item : items) {
			if (item.isBundle(getProductSkuLookup())) {
				populateLeaves(results, item.getBundleItems(getProductSkuLookup()));
			} else {
				results.add(item);
			}
		}
	}

	protected ProductSkuLookup getProductSkuLookup() {
		return productSkuLookup;
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

	/**
	 * Sorts {@link DiscountableItem} by amount and skucode in descending order.  
	 */
	protected static class DiscountableItemSorter {
		/**
		 * Sorts {@link DiscountableItem} by amount and skucode in descending order.
		 * 
		 * @param discountableShoppingItems shopping item attributes required by sorting algorithm
		 * @return sorted {@link DiscountableItem}. 
		 */
		public List<DiscountableItem> sortByPriceSku(final Collection<DiscountableItem> discountableShoppingItems) {			
			List<DiscountableItem> sortedItems = new ArrayList<>(discountableShoppingItems);
			Collections.sort(sortedItems, createComparator()); 
			
			return sortedItems;
		}

		private Comparator<DiscountableItem> createComparator() {
			return new Comparator<DiscountableItem>() {
				@Override
				public int compare(final DiscountableItem item1,
								   final DiscountableItem item2) {
					int result = item1.getAmount().compareTo(item2.getAmount());
					if (result == 0) {
						result = item1.getSkuCode().compareTo(item2.getSkuCode());
					}

					return -result;
				}

			};
		}
	}	

}