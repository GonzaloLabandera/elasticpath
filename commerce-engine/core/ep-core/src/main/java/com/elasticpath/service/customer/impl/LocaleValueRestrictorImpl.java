/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer.impl;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.elasticpath.domain.store.Store;
import com.elasticpath.service.customer.CustomerProfileAttributeValueRestrictor;
import com.elasticpath.service.customer.CustomerProfileAttributeValueRestrictorContext;

/**
 * Restricts values to the set of supported locales for shared stores.
 */
public class LocaleValueRestrictorImpl implements CustomerProfileAttributeValueRestrictor {

	@Override
	public Set<String> getRestrictedValues(final CustomerProfileAttributeValueRestrictorContext context) {
		return context.getSharedStores().stream()
				.map(Store::getSupportedLocales)
				.flatMap(Collection::stream)
				.map(Locale::toString)
				.collect(Collectors.toSet());
	}
}
