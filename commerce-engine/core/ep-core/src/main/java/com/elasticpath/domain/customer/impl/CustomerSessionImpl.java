/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.customer.impl;

import java.util.Currency;
import java.util.Locale;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.TagSetInvalidationDeterminer;
import com.elasticpath.domain.pricing.PriceListStack;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.service.rules.impl.AbstractRuleEngineImpl;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;


/**
 * The default implementation of <code>CustomerSession</code>.
 */
@SuppressWarnings("PMD.UselessOverridingMethod")
public class CustomerSessionImpl implements CustomerSession {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private TagSet tagSet;

	private PriceListStack priceListStack;

	private boolean priceListStackValid;

	private Locale locale;
	private Currency currency;
	private Shopper shopper;

	private transient TagSetInvalidationDeterminer priceListStackInvalidationDeterminer;
	private transient TagSetInvalidationDeterminer promoInvalidationDeterminer;


	/**
	 * Default constructor.
	 */
	public CustomerSessionImpl() {
		setupTagSet();
	}

	// CustomerSessionTransientData interface

	@Override
	public Locale getLocale() {
		// LocaleUtils.toLocale is null-safe but will throw an IllegalArgumentException if the
		// input is not a valid Locale string.
		try {
			return this.locale;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public void setLocale(final Locale locale) {
		if (locale != null) {
			this.locale = locale;
		}
	}

	@Override
	public PriceListStack getPriceListStack() {
		return priceListStack;
	}

	@Override
	public void setPriceListStack(final PriceListStack priceListStack) {
		this.priceListStack = priceListStack;
		this.setPriceListStackValid(true);
	}

	@Override
	public boolean isPriceListStackValid() {
		return priceListStackValid;
	}

	@Override
	public void setPriceListStackValid(final boolean priceListStackValid) {
		this.priceListStackValid = priceListStackValid;
	}

	private void setupTagSet() {
		this.tagSet = new TagSet();
		this.tagSet.addListener(this);
	}

	@Override
	public TagSet getCustomerTagSet() {
		return tagSet;
	}
	/**
	 * @param tagSet the customer's {@link TagSet}
	 */
	@Override
	public void setCustomerTagSet(final TagSet tagSet) {
		if (this.getCustomerTagSet() != null) {
			this.getCustomerTagSet().removeListener(this);
		}
		this.tagSet = tagSet;
		this.setPriceListStackValid(false);
		if (this.getCustomerTagSet() != null) {
			this.getCustomerTagSet().addListener(this);
		}
	}

	@Override
	public void onEvent(final String key, final Tag tag) {

		if (priceListStackInvalidationDeterminer.needInvalidate(key)) {
			setPriceListStackValid(false);
			shopper.getCache().cacheInvalidate(AbstractRuleEngineImpl.RULE_IDS_KEY);

		} else if (promoInvalidationDeterminer.needInvalidate(key)) {
			shopper.getCache().cacheInvalidate(AbstractRuleEngineImpl.RULE_IDS_KEY);
		}
	}

	// CustomerSession interface

	@Override
	public void setShopper(final Shopper shopper) {
		if (shopper == null) {
			throw new EpServiceException("Shopper should not be null.");
		}
		this.shopper = shopper;
	}

	@Override
	public Shopper getShopper() {
		return shopper;
	}

	/**
	 * Get the currency of the customer corresponding to the shopping cart.
	 *
	 * @return the <code>Currency</code>
	 */
	@Override
	public Currency getCurrency() {
		return this.currency;
	}

	/**
	 * Set the currency of the customer corresponding to the shopping cart.
	 *
	 * @param currency the <code>Currency</code>
	 */
	@Override
	public void setCurrency(final Currency currency) {
		this.currency = currency;
		setPriceListStackValid(false);
	}

	public TagSetInvalidationDeterminer getPriceListStackInvalidationDeterminer() {
		return priceListStackInvalidationDeterminer;
	}

	public void setPriceListStackInvalidationDeterminer(final TagSetInvalidationDeterminer priceListStackInvalidationDeterminer) {
		this.priceListStackInvalidationDeterminer = priceListStackInvalidationDeterminer;
	}

	public TagSetInvalidationDeterminer getPromoInvalidationDeterminer() {
		return promoInvalidationDeterminer;
	}

	public void setPromoInvalidationDeterminer(final TagSetInvalidationDeterminer promoInvalidationDeterminer) {
		this.promoInvalidationDeterminer = promoInvalidationDeterminer;
	}
}
