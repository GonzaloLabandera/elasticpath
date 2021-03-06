/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.customer;

import java.io.Serializable;
import java.util.Locale;

import com.elasticpath.commons.listeners.TagSetListener;
import com.elasticpath.domain.pricing.PriceListStack;
import com.elasticpath.domain.shopper.SimpleCacheProvider;
import com.elasticpath.tags.TagSet;

/**
 * Container for transient data for customer session.
 * When customer session is persisted OpenJPA resets the transient fields
 * which is the rationale for existence of this object. Before customer session is
 * persisted all values in this contained must be copied into the persisted
 * customer session.
 *
 * Note this is a marker interface to identify the object when performing the
 * copying.
 */
public interface CustomerSessionTransientData extends Serializable, TagSetListener, SimpleCacheProvider {

	/**
	 * Get the locale of the customer corresponding to the shopping cart.
	 *
	 * @return the <code>Locale</code>
	 */
	Locale getLocale();

	/**
	 * Set the locale of the customer corresponding to the shopping cart.
	 *
	 * @param locale the <code>Locale</code>
	 */
	void setLocale(Locale locale);

	/**
	 *
	 * @return Price list stack, instance of {@link PriceListStack}.
	 */
	PriceListStack getPriceListStack();

	/**
	 * Set the {@link PriceListStack}.
	 *
	 * @param priceListStack instance to set.
	 */
	void setPriceListStack(PriceListStack priceListStack);

	/**
	 * Get the validity flag for price list stack.
	 *
	 * @return false if need to get fresh price list stack.
	 */
	boolean isPriceListStackValid();

	/**
	 * Set price list stack invalid flag.
	 *
	 * @param setPriceListStackValid flag to set.
	 */
	void setPriceListStackValid(boolean setPriceListStackValid);

	/**
	 * @return the customer's {@link TagSet}
	 */
	TagSet getCustomerTagSet();

	/**
	 * Set tag set.
	 *
	 * @param tagSet to set
	 */
	void setCustomerTagSet(TagSet tagSet);
}
