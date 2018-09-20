/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.shipping;

import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import com.elasticpath.commons.exception.SCCMCurrencyMissingException;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.money.Money;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * A shippingCostCalculationMethod represents a method to be used for shipping cost calculation. It is a component of a shipping service level.
 */
public interface ShippingCostCalculationMethod extends Persistable {
	/**
	 * Get the type of shippingCostCalculationMethod, i.e. fixBase, fixedBaseAndOrderTotalPercentageMethod and etc. Make sure the type value match
	 * the bean id used in the spring bean factory configuration.
	 * 
	 * @return the shippingCostCalculationMethod type.
	 */
	String getType();

	/**
	 * Set the type of shippingCostCalculationMethod.
	 * 
	 * @param type the type of shippingCostCalculationMethod.
	 */
	void setType(String type);

	/**
	 * Get the parameters associated with this shippingCostCalculationMethod.
	 * 
	 * @return the parameters
	 */
	Set<ShippingCostCalculationParameter> getParameters();

	/**
	 * Set the parameters of this shippingCostCalculationMethod.
	 * 
	 * @param shippingCostCalculationParameters a set of <code>ShippingCostCalculationParameter</code> objects
	 */
	void setParameters(Set<ShippingCostCalculationParameter> shippingCostCalculationParameters);

	/**
	 * Return the array of the required parameter keys for the method.
	 * 
	 * @return an array of String of the required parameter keys for the method.
	 */
	String[] getParameterKeys();

	/**
	 * Return the text representation of this method for display to the user.
	 * 
	 * @return the text representation
	 */
	String getDisplayText();

	/**
	 * Calculation the shipping cost for the given shoppingCart.
	 *
	 * @param shoppingItems the shopping items to calculate the shipping cost of
	 * @param shippableItemsSubtotal the subtotal of the shippable shopping items
	 * @param currency the currency
	 * @param productSkuLookup a product sku lookup
	 * @return the shippingCost for the given line items.
	 * 
	 * @throws SCCMCurrencyMissingException if a matching param value for an expected currency is not found or if currency is null.
	 */
	Money calculateShippingCost(Collection<? extends ShoppingItem> shoppingItems, Money shippableItemsSubtotal, Currency currency,
								ProductSkuLookup productSkuLookup) throws SCCMCurrencyMissingException;

	/**
	 * Returns the default list of calculation parameters for this calculation method. That calculation parameters which are currency aware will be
	 * cloned for the particular currency.
	 * 
	 * @param currencyList list of <code>Currency</code>s, calculation parameters should adopt to.
	 * @return list of <code>ShippingCostCalculationParameter</code>s for this calculation method.
	 */
	List<ShippingCostCalculationParameter> getDefaultParameters(List<Currency> currencyList);
	
	/**
	 * Determine if a parameter exists in the list.
	 *
	 * @param key - the key of the parameter to find
	 * @param currency - one currency to match if there is the case
	 * @return <code> true</code> if the parameter exists in map
	 */
	boolean hasParameter(String key, Currency... currency);
}
