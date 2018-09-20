/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.shoppingcart;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.PriceSchedule;
import com.elasticpath.domain.catalog.PriceScheduleType;
import com.elasticpath.domain.catalog.PricingScheme;
import com.elasticpath.domain.catalog.SimplePrice;
import com.elasticpath.domain.quantity.Quantity;
import com.elasticpath.domain.subscriptions.PaymentSchedule;
import com.elasticpath.money.Money;

/**
 * Factory to create a map of recurring frequency to FrequencyAndRecurringPrice. 
 * 
 */
public class FrequencyAndRecurringPriceFactory {

	/**
	 * 
	 * @param cartItems The items in the cart.
	 * @return a map of frequency to FrequencyAndRecurringPrice.
	 */
	public Map<Quantity, FrequencyAndRecurringPrice> getFrequencyMap(final Map<? extends ShoppingItem, ShoppingItemPricingSnapshot> cartItems) {
		HashMap<Quantity, FrequencyAndRecurringPrice> result = new LinkedHashMap<>();
		createFrequencyMap(cartItems, result);
		return result;
	}
	
	/**
	 * 	Creates the recurring price frequency map.
	 * @param cartItems The items in the cart.
	 * @param result The map containing recurring frequency and price.
	 */
	protected void createFrequencyMap(final Map<? extends ShoppingItem, ShoppingItemPricingSnapshot> cartItems,
										final Map<Quantity, FrequencyAndRecurringPrice> result) {
		for (final Map.Entry<? extends ShoppingItem, ShoppingItemPricingSnapshot> entry : cartItems.entrySet()) {
			final ShoppingItemPricingSnapshot itemPricingSnapshot = entry.getValue();
			final Price price = itemPricingSnapshot.getPrice();
			PricingScheme pricingScheme = price.getPricingScheme();
			
			if (pricingScheme != null) {
				Collection<PriceSchedule> priceColl = pricingScheme.getSchedules(PriceScheduleType.RECURRING);
				for (PriceSchedule schedule : priceColl) {	
					addRecurringPriceToMap(result, entry.getKey(), itemPricingSnapshot, schedule); // add recurring price to map
				}
			}
		}
	}

	/**
	 * Adds a recurring price to the frequency map.
	 * @param result the frequency map
	 * @param bean the shopping item
	 * @param itemPricingSnapshot the shopping item pricing snapshot
	 * @param priceSchedule the price schedule to use for getting recurring frequency
	 */
	private void addRecurringPriceToMap(final Map<Quantity, FrequencyAndRecurringPrice> result,
										final ShoppingItem bean,
										final ShoppingItemPricingSnapshot itemPricingSnapshot,
										final PriceSchedule priceSchedule) {
		PaymentSchedule schedule = priceSchedule.getPaymentSchedule(); // get the payment schedule associated with the price schedule

		// get the simple price
		SimplePrice recurringPrice = itemPricingSnapshot.getPrice().getPricingScheme().getSimplePriceForSchedule(priceSchedule);
		Money lowestPrice = recurringPrice.getLowestPrice(bean.getQuantity()); //get the lowest price tier from the simple price
		Money actualPrice = lowestPrice.multiply(bean.getQuantity()); // get lowest price * quantity of product
		Quantity key = schedule.getPaymentFrequency();

		if (result.containsKey(key)) {
			// if the frequency name already exists, update the price
			result.get(key).addPrice(actualPrice);
		} else {
			// create the key/value pair in the map
			FrequencyAndRecurringPrice value = new FrequencyAndRecurringPrice(schedule.getPaymentFrequency(), actualPrice, schedule.getName());
			result.put(key, value);
		}
	}
}
