/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.shoppingcart.CartType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFCartType;
import com.elasticpath.xpf.connectivity.entity.XPFCatalog;
import com.elasticpath.xpf.connectivity.entity.XPFStore;

@RunWith(MockitoJUnitRunner.class)
public class StoreConverterTest {
	@Mock
	private Store store;
	@Mock
	private Catalog catalog;
	@Mock
	private CartType cartType;
	@Mock
	private XPFCatalog contextCatalog;
	@Mock
	private XPFCartType xpfCartType;
	@Mock
	private CatalogConverter catalogConverter;
	@Mock
	private CartTypeConverter cartTypeConverter;

	@InjectMocks
	private StoreConverter storeConverter;

	@Test
	public void testConvert() {
		String name = "name";
		String code = "code";
		TimeZone timeZone = TimeZone.getDefault();

		when(store.getName()).thenReturn(name);
		when(store.getCode()).thenReturn(code);
		when(store.getTimeZone()).thenReturn(timeZone);
		when(store.getCatalog()).thenReturn(catalog);
		when(store.getShoppingCartTypes()).thenReturn(Collections.singletonList(cartType));
		when(catalogConverter.convert(catalog)).thenReturn(contextCatalog);
		when(cartTypeConverter.convert(cartType)).thenReturn(xpfCartType);

		XPFStore contextStore = storeConverter.convert(store);
		assertEquals(code, contextStore.getCode());
		assertEquals(name, contextStore.getName());
		assertEquals(timeZone, contextStore.getTimeZone());
		assertEquals(contextCatalog, contextStore.getCatalog());
	}
}