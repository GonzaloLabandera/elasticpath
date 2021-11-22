/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.xpf.connectivity.entity.XPFCatalog;

@RunWith(MockitoJUnitRunner.class)
public class CatalogConverterTest {
	@Mock
	private Catalog catalog;

	@InjectMocks
	private CatalogConverter catalogConverter;

	@Test
	public void testConvert() {
		String testCatalogCode = "testCatalogCode";
		when(catalog.getCode()).thenReturn(testCatalogCode);

		XPFCatalog contextCatalog = catalogConverter.convert(catalog);

		assertEquals(testCatalogCode, contextCatalog.getCode());
	}
}