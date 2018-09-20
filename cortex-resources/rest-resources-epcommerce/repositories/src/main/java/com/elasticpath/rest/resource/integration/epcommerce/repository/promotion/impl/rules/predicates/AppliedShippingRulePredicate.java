/**
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.impl.rules.predicates;

import java.util.Set;

import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.PromotionRecordContainer;
import com.elasticpath.domain.shoppingcart.ShippingPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.RulePredicate;

/**
 * An AppliedShippingRulePredicate matches Rules that contain at least one
 * shipping related RuleAction clause that target a specific shipping option.
 */
public class AppliedShippingRulePredicate implements RulePredicate<Rule> {

	private final ShoppingCartPricingSnapshot pricingSnapshot;
	private final ShippingServiceLevel shippingServiceLevel;

	/**
	 * Constructs the thing.
	 *
	 * @param pricingSnapshot the shopping cart pricing snapshot
	 * @param shippingServiceLevel The shipping option.
	 */
	public AppliedShippingRulePredicate(final ShoppingCartPricingSnapshot pricingSnapshot, final ShippingServiceLevel shippingServiceLevel) {
		this.pricingSnapshot = pricingSnapshot;
		this.shippingServiceLevel = shippingServiceLevel;
	}

	@Override
	public boolean isSatisfied(final Rule rule) {
		final PromotionRecordContainer promotionRecordContainer = pricingSnapshot.getPromotionRecordContainer();
		final Set<Long> appliedShippingRules = promotionRecordContainer.getAppliedRulesByShippingServiceLevel(shippingServiceLevel);

		return isShippingDiscounted(pricingSnapshot, shippingServiceLevel) && appliedShippingRules.contains(rule.getUidPk());
	}

	// TODO: If you move this check up the call stack before the Rule lookup you
	// can optimise by saving db calls.
	private boolean isShippingDiscounted(final ShoppingCartPricingSnapshot pricingSnapshot, final ShippingServiceLevel shippingServiceLevel) {
		final ShippingPricingSnapshot shippingPricingSnapshot = pricingSnapshot.getShippingPricingSnapshot(shippingServiceLevel);
		return !shippingPricingSnapshot.getShippingListPrice().equals(shippingPricingSnapshot.getShippingPromotedPrice());
	}

}
