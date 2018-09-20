/*
 * Copyright (c) Elastic Path Software Inc., 2006-2014
 */
package com.elasticpath.service.tax;

import java.util.Collection;
import java.util.Currency;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.tax.TaxCategory;
import com.elasticpath.money.Money;
import com.elasticpath.plugin.tax.domain.TaxDocument;

/**
 * Represents the results of a tax calculation.
 */
public interface TaxCalculationResult {

	/**
	 * Adds the given amount of tax to the specified <code>TaxCategory</code>.
	 * @param taxCategory the <code>TaxCategory</code> to add taxes to
	 * @param amount the amount of tax to add
	 */
	void addTaxValue(TaxCategory taxCategory, Money amount);

	/**
	 * Adds the given amount of tax to the ShippingTax.
	 * 
	 * <p>Note: {@link TaxCalculationResult#addTaxValue(TaxCategory, com.elasticpath.money.Money)} must be called
	 * as well. Otherwise {@link #getTotalTaxes()} will not return correct amount of taxes.
	 *
	 * @param shippingTax the shipping tax
	 */
	void addShippingTax(Money shippingTax);
	
	/**
	 * Retrieves the shipping tax.
	 *
	 * @return the shipping tax
	 */
	Money getShippingTax();
	
	/**
	 * Retrieves the amount of tax that will be charged for the specified
	 * <code>TaxCategory</code>.
	 * @param taxCategory the <code>TaxCategory</code> to retrieve taxes for
	 * @return the amount of taxes for that category
	 */
	Money getTaxValue(TaxCategory taxCategory);

	/**
	 * Retrieves the mapping of TaxCategories to values.
	 * @return the map of TaxCategories to values.
	 */
	Map<TaxCategory, Money> getTaxMap();

	/**
	 * Retrieves an iterator of the <code>TaxCategory</code>s in this
	 * <code>TaxCalculationResult</code>.
	 * @return an iterator of the <code>TaxCategory</code>s in this
	 * <code>TaxCalculationResult</code>
	 */
	Iterator<TaxCategory> getTaxCategoriesIterator();

	/**
	 * Retrives a <code>Set</code> of the <code>TaxCategory</code>s in this
	 * <code>TaxCalculationResult</code>.
	 * @return a <code>Set</code> of the <code>TaxCategory</code>s in this
	 * <code>TaxCalculationResult</code>.
	 */
	Set<TaxCategory> getTaxCategoriesSet();

	/**
	 * Sets whether or not the taxes are to be calculated for a price-inclusive
	 * tax jurisdiction.
	 * @param taxInclusive whether or not the tax jurisdiction is price
	 * inclusive
	 */
	void setTaxInclusive(boolean taxInclusive);

	/**
	 * Retrieves whether or not the taxes were calculated for a price-inclusive
	 * tax jurisdiction.
	 * @return whether or not the tax jurisdiction is price
	 * inclusive
	 *
	 */
	boolean isTaxInclusive();

	/**
	 * Retrieves the total amount of tax included in the item prices.
	 * @return the amount of tax in the item price
	 * @deprecated do not use as it does not make sense for the current implementation. Use {@link #getTotalItemTax()} if required.
	 */
	@Deprecated
	Money getTaxInItemPrice();

	/**
	 * Adds to the total amount of tax included in the item prices.
	 * @param taxInItemPrice the amount of tax to add
	 * @deprecated do not use as it does not make sense for the current implementation and will be removed in the future
	 */
	@Deprecated
	void addToTaxInItemPrice(Money taxInItemPrice);

	/**
	 * Retrieves the cost of shipping before tax.
	 * @return the cost of shipping before tax which is never null
	 */
	Money getBeforeTaxShippingCost();

	/**
	 * Sets the cost of shipping before tax.
	 * @param beforeTaxShippingCost the cost of shipping before tax
	 */
	void setBeforeTaxShippingCost(Money beforeTaxShippingCost);

	/**
	 * Returns the value of the shopping cart before tax.
	 * @return the value of the shopping cart before tax which is never null
	 */
	Money getBeforeTaxSubTotal();

	/**
	 * Sets the value of the shopping cart before tax.
	 * @param beforeTaxSubTotal the value of the shopping cart before tax
	 */
	void setBeforeTaxSubTotal(Money beforeTaxSubTotal);

	/**
	 * Retrieves the total amount of taxes.
	 * @return the total amount of taxes
	 */
	Money getTotalTaxes();

	/**
	 * Retrieves the amount of tax for the line item with the supplied GUID.
	 *
	 * @param itemId the GUID of the associated line item.
	 * @return the total amount of tax on the specified line item or null if line item not found in result
	 */
	Money getLineItemTax(String itemId);
	
	/**
	 * Sets the default currency for this <code>TaxCalculationResult</code>.
	 * This value will be used when there is not enough information to
	 * determine the currency.
	 * @param defaultCurrency the default currency to use
	 */
	void setDefaultCurrency(Currency defaultCurrency);
	
	/**
	 * Gets the taxes on the items on which the tax was calculated.
	 * 
	 * @return the taxes amount
	 */
	Money getTotalItemTax();

	/**
	 * Adds a tax amount for the line item associated with the specified GUID.
	 *
	 * <p>Note: {@link #addTaxValue(TaxCategory, Money)} must be called
	 * as well. Otherwise {@link #getTotalTaxes()} will not return correct amount of taxes.
	 * 
	 * @param amount the amount to be added
	 * @param itemId the GUID of the associated line item
	 */
	void addItemTax(String itemId, Money amount);

	/**
	 * Adds a value to the total before tax shipping cost amount.
	 * 
	 * @param value the value to be added
	 */
	void addBeforeTaxShippingCost(Money value);


	/**
	 * Adds a value to the total before tax items price amount.
	 * 
	 * @param value the value to be added
	 */
	void addBeforeTaxItemPrice(Money value);
	
	/**
	 * Adds before tax value without discount to the already existing value.
	 * 
	 * @param value the value to add
	 * @deprecated this should not be used as the method does not make sense and will be removed in the future.
	 */
	@Deprecated
	void addBeforeTaxWithoutDiscount(Money value);

	/**
	 * Returns the value of the shopping cart before tax without the discount applied.
	 * 
	 * @return the value of the shopping cart before tax without the discount applied. 
	 * 	It is always not null.
	 * @deprecated use {@link #getSubtotal()} instead
	 */
	@Deprecated
	Money getBeforeTaxSubTotalWithoutDiscount();
	
	/**
	 * Sets the before tax subtotal without the discount applied.
	 * 
	 * @param amount the amount to be set
	 * @deprecated this should not be used as the method does not make sense and will be removed in the future.
	 */
	@Deprecated
	void setBeforeTaxSubTotalWithoutDiscount(Money amount);
	
	/**
	 * Gets the subtotal which a sum of all the items' cost.
	 * 
	 * @return the subtotal
	 */
	Money getSubtotal();

	/**
	 * Takes the collection of shopping items which the tax calculation was run on and sets their tax fields to the 
	 * results of the calculation. It is an error to supply a <code>ShoppingItem</code> which was not supplied to the 
	 * <code>TaxCalculationService</code> which produced this <code>TaxCalculationResult</code>
	 * 
	 *  @param shoppingItems collection of items which needs their tax field set.
	 */
	void applyTaxes(Collection<? extends ShoppingItem> shoppingItems);

	/**
	 * Gets the tax document.
	 *
	 * @return the tax document
	 */
	TaxDocument getTaxDocument();

	/**
	 * Sets the tax document.
	 *
	 * @param document the tax document
	 */
	void setTaxDocument(TaxDocument document);

	/**
	 * Set default values for a given currency. 
	 *
	 * @param currency the given currency
	 */
	void initialize(Currency currency);
}
