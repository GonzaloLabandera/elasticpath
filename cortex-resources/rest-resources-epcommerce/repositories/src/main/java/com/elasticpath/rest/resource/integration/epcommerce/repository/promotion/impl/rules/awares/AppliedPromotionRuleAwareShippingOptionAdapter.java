/**
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.impl.rules.awares;

import java.util.Collection;

import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.PromotionRecordContainer;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.AppliedPromotionRuleAware;

/**
* Adapts a ShippingServiceLevel to the AppliedPromotionRuleAware interface.
*/
public class AppliedPromotionRuleAwareShippingOptionAdapter implements AppliedPromotionRuleAware<Long> {
	private final ShoppingCartPricingSnapshot cartPricingSnapshot;
	private final ShippingServiceLevel shippingServiceLevel;

	/**
	 * Constructor.
	 *
	 * @param cartPricingSnapshot the cart pricing snapshot
	 * @param shippingServiceLevel The shipping option.
	 */
	public AppliedPromotionRuleAwareShippingOptionAdapter(final ShoppingCartPricingSnapshot cartPricingSnapshot,
															final ShippingServiceLevel shippingServiceLevel) {
		this.cartPricingSnapshot = cartPricingSnapshot;
		this.shippingServiceLevel = shippingServiceLevel;
	}

	@Override
	public Collection<Long> getAppliedRules() {
		final PromotionRecordContainer promotionRecordContainer = cartPricingSnapshot.getPromotionRecordContainer();
		return promotionRecordContainer.getAppliedRulesByShippingServiceLevel(shippingServiceLevel);
	}
}