/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.shoppingcart.impl;

import java.math.BigDecimal;
import java.util.Objects;

import com.elasticpath.domain.rules.RuleAction;
import com.elasticpath.domain.rules.RuleParameter;
import com.elasticpath.domain.shoppingcart.ShoppingCart;

/**
 * Represents a discount to the shipping amount.
 */
public class ShippingDiscountRecordImpl extends AbstractDiscountRecordImpl {

	/** Serial Version ID. **/
	private static final long serialVersionUID = 5000000002L;

	private final String shippingLevelCode;

	/**
	 * Constructor.
	 *
	 * @param shippingServiceLevelCode the code of the shipping service level to which the discount applies
	 * @param ruleId the ID of the rule that applied this discount
	 * @param actionId the ID of the rule action that applied this discount
	 * @param discountAmount The amount of this discount per discount record
	 */
	public ShippingDiscountRecordImpl(final String shippingServiceLevelCode,
										final long ruleId,
										final long actionId,
										final BigDecimal discountAmount) {
		super(ruleId, actionId, discountAmount);
		this.shippingLevelCode = shippingServiceLevelCode;
	}

	/**
	 * A coupon is used once for a shipping discount.
	 * 
	 * @param action The action (ignored).
	 * @param shoppingCart the cart.
	 * @return The number of coupon uses.
	 */
	@Override
	public int getCouponUsesRequired(final RuleAction action, final ShoppingCart shoppingCart) {
		if (isSuperceded() || shoppingCart.getSelectedShippingServiceLevel() == null) {
			return 0;
		}
		String uidShippingLevel = action.getParamValue(RuleParameter.SHIPPING_SERVICE_LEVEL_CODE_KEY);
		String shippingCode = shoppingCart.getSelectedShippingServiceLevel().getCode();
		if (!shippingCode.equals(uidShippingLevel)) {
			return 0;
		}
		return 1;
	}

	public String getShippingLevelCode() {
		return shippingLevelCode;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof ShippingDiscountRecordImpl)) {
			return false;
		}

		final ShippingDiscountRecordImpl otherShippingDiscountRecord = (ShippingDiscountRecordImpl) other;

		return Objects.equals(getRuleId(), otherShippingDiscountRecord.getRuleId())
				&& Objects.equals(getActionId(), otherShippingDiscountRecord.getActionId())
				&& Objects.equals(getDiscountAmount(), otherShippingDiscountRecord.getDiscountAmount())
				&& Objects.equals(getShippingLevelCode(), otherShippingDiscountRecord.getShippingLevelCode());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getRuleId(), getActionId(), getDiscountAmount(), getShippingLevelCode());
	}

}
