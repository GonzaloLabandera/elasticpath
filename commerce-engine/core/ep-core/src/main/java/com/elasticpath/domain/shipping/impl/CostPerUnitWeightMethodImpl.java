/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.shipping.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import org.apache.openjpa.persistence.DataCache;

import com.elasticpath.commons.exception.SCCMCurrencyMissingException;
import com.elasticpath.domain.shipping.ShippingCostCalculationParametersEnum;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.money.Money;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * Shipping cost calculation method that calculates the shipping cost as unit price * order weight. It needs one parameter: unit price.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("costPerUnitWeightMethod")
@DataCache(enabled = true)
public class CostPerUnitWeightMethodImpl extends AbstractShippingCostCalculationMethodImpl {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/** Make sure the METHOD_TYPE value match the bean id used in the spring bean factory configuration. */
	private static final String METHOD_TYPE = "costPerUnitWeightMethod";

	private static final String METHOD_TEXT = "CostPerUnitWeightMethod_method_text";

	/** Set of keys required for this shipping cost calculation method. */
	private static final String[] PARAMETER_KEYS = new String[] { ShippingCostCalculationParametersEnum.COST_PER_UNIT_WEIGHT.getKey() };

	/**
	 * Must be implemented by subclasses to return their type. Make sure this matches the hibernate subclass discriminator-value and the spring
	 * context bean id for this RuleAction implementation.
	 * 
	 * @return the kind of the action subclass.
	 */
	@Override
	@Transient
	protected String getMethodType() {
		return METHOD_TYPE;
	}

	/**
	 * Return an array of parameter keys required by this rule action.
	 * 
	 * @return the parameter key array
	 */
	@Override
	@Transient
	public String[] getParameterKeys() {
		return PARAMETER_KEYS.clone();
	}

	/**
	 * Return the text representation of this method for display to the user.
	 * 
	 * @return the text representation
	 */
	@Override
	@Transient
	public String getDisplayText() {
		return METHOD_TEXT;
	}

	@Override
	public Money calculateShippingCost(final Collection<? extends ShoppingItem> lineItems,
										final Money shippableItemsSubtotal,
										final Currency currency,
										final ProductSkuLookup productSkuLookup) throws SCCMCurrencyMissingException {
		final BigDecimal costPerUnitWeight = new BigDecimal(
				this.getParamValue(ShippingCostCalculationParametersEnum.COST_PER_UNIT_WEIGHT.getKey(), currency));
		return Money.valueOf(
				costPerUnitWeight.multiply(calculateTotalWeight(lineItems, productSkuLookup)).setScale(2, BigDecimal.ROUND_HALF_UP), currency);
	}
}
