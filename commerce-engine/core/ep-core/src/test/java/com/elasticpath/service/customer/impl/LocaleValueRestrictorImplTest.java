/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Locale;

import org.junit.Test;

import org.assertj.core.util.Lists;

import com.elasticpath.domain.catalog.DefaultValueRemovalForbiddenException;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.service.customer.CustomerProfileAttributeValueRestrictorContext;

/**
 * Test class for <code>LocaleValueRestrictorImpl</code>.
 */
public class LocaleValueRestrictorImplTest {

	private static final String STORE_1 = "STORE_1";
	private static final String STORE_2 = "STORE_2";
	private static final int EXPECTED_SIZE = 3;

	private final LocaleValueRestrictorImpl restrictor = new LocaleValueRestrictorImpl();

	@Test
	public void testGetRestrictedValues() throws DefaultValueRemovalForbiddenException {
		Collection<Store> stores = Lists.newArrayList();
		stores.add(addStore(STORE_1, Lists.list(Locale.CANADA, Locale.CANADA_FRENCH)));
		stores.add(addStore(STORE_2, Lists.list(Locale.US)));
		CustomerProfileAttributeValueRestrictorContext context =
				new CustomerProfileAttributeValueRestrictorContextImpl(stores);

		assertThat(restrictor.getRestrictedValues(context))
				.as("Mismatched set of restricted locales.")
				.hasSize(EXPECTED_SIZE);
	}

	private Store addStore(final String code, final Collection<Locale> locales) throws DefaultValueRemovalForbiddenException {
		Store store = new StoreImpl();
		store.setCode(code);
		store.setSupportedLocales(locales);
		return store;
	}
}
