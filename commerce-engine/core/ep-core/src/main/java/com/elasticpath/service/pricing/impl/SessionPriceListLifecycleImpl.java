/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.pricing.impl;

import java.util.Currency;

import com.elasticpath.common.pricing.service.PriceListLookupService;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.pricing.PriceListStack;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.pricing.SessionPriceListLifecycle;
import com.elasticpath.tags.TagSet;

/**
 * Lazy PriceList lifecycle implementation that only updates when the
 * Shoppers personalisation/pricing context has changed.
 */
public class SessionPriceListLifecycleImpl implements SessionPriceListLifecycle {

	private PriceListLookupService priceListLookupService;

	@Override
	public void refreshPriceListStack(final CustomerSession customerSession, final Store store) {
		String catalogCode = store.getCatalog().getCode();
		if (!customerSession.isPriceListStackValid()) {
			Currency currency = customerSession.getCurrency();
			TagSet customerTagSet = customerSession.getCustomerTagSet();
			PriceListStack priceListStack = priceListLookupService.getPriceListStack(catalogCode, currency, customerTagSet);
			customerSession.setPriceListStack(priceListStack);
		}
	}

	/**
	 * Set the {@link PriceListLookupService}.
	 * @param priceListLookupService instance to set.
	 */
	public void setPriceListLookupService(final PriceListLookupService priceListLookupService) {
		this.priceListLookupService = priceListLookupService;
	}
}