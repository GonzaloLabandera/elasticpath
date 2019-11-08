/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer.impl;

import java.util.Collection;
import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

import com.elasticpath.domain.store.Store;
import com.elasticpath.service.customer.CustomerProfileAttributeValueRestrictor;
import com.elasticpath.service.customer.CustomerProfileAttributeValueRestrictorContext;

/**
 * Restricts values to the set of supported currencies for shared stores.
 */
public class CurrencyValueRestrictorImpl implements CustomerProfileAttributeValueRestrictor {

	@Override
	public Set<String> getRestrictedValues(final CustomerProfileAttributeValueRestrictorContext context) {
		return context.getSharedStores().stream()
				.map(Store::getSupportedCurrencies)
				.flatMap(Collection::stream)
				.map(Currency::getCurrencyCode)
				.collect(Collectors.toSet());
	}
}
