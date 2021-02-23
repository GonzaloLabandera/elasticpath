package com.elasticpath.cucumber.definitions;

import static org.assertj.core.api.Assertions.assertThat;

import cucumber.api.java.en.Then;

import com.elasticpath.cortex.dce.LoginSteps;
import com.elasticpath.cortexTestObjects.FindItemBy;
import com.elasticpath.cortexTestObjects.Item;

/**
 * Promotions Cortex step definitions.
 */
public class PromotionCortexDefinition {

	/**
	 * Verifies item price for registered customer.
	 *
	 * @param sku   item sku code.
	 * @param price item price.
	 * @param store store.
	 */
	@Then("^the item price for sku (.+) is (.+) when public shopper retrieve the item price in store (.+)$")
	public void verifyPublicItemPrice(final String sku, final String price, final String store) {
		verifyPublicItemPriceFromCortex(price, sku, store);
	}

	private void verifyPublicItemPriceFromCortex(final String price, final String sku,
												 final String store) {
		LoginSteps.loginAsPublicUserWithScope(store);
		FindItemBy.skuCode(sku);
		Item.price();

		assertThat(Item.getPurchasePrice("display"))
				.as("Expected item price not match.")
				.isEqualTo(price);
	}
}
