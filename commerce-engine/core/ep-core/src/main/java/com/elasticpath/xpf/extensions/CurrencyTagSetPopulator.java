/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.xpf.extensions;

import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;
import com.elasticpath.xpf.connectivity.entity.XPFStore;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.HttpRequestTagSetPopulator;

/**
 * Populator for currency subject attribute.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.HTTP_TAG_SET_POPULATOR, priority = 1010)
public class CurrencyTagSetPopulator extends XPFExtensionPointImpl implements HttpRequestTagSetPopulator {

	private static final Logger LOG = LoggerFactory.getLogger(CurrencyTagSetPopulator.class);
	private static final String TRAIT_HEADER = "CURRENCY";
	private static final String SUBJECT_ATTRIBUTE_KEY = "CURRENCY";

	@Override
	public Map<String, String> collectTagValues(final XPFHttpTagSetContext context) {
		final XPFStore store = context.getStore();
		if (store == null) {
			LOG.error("Unable to retrieve store record for {}", store);
			return Collections.emptyMap();
		}

		Currency currency;
		// NOTE: User trait key is case-sensitive
		String currencyString = context.getUserTraitValues().get(TRAIT_HEADER);
		if (StringUtils.isNotEmpty(currencyString)) {
			currency = findBestSupportedCurrency(createCurrency(currencyString), store);
		} else {
			currency = store.getDefaultCurrency();
		}

		return Collections.singletonMap(SUBJECT_ATTRIBUTE_KEY, currency.getCurrencyCode());
	}

	private Currency createCurrency(final String currencyString) {
		try {
			return Currency.getInstance(currencyString);
		} catch (Exception e) {
			LOG.error("Unrecognized currency code: {}", currencyString);
		}
		return null;
	}

	/**
	 * Find the best currency for this session given the request headers and Store supported currencies.
	 *
	 * @param currency the currency from the request
	 * @param store    the store
	 * @return the appropriate currency
	 */
	Currency findBestSupportedCurrency(final Currency currency, final XPFStore store) {

		if (currency != null) {
			Collection<Currency> supportedCurrencies = store.getSupportedCurrencies();

			if (supportedCurrencies.contains(currency)) {
				return currency;
			}
		}
		return store.getDefaultCurrency();
	}
}
