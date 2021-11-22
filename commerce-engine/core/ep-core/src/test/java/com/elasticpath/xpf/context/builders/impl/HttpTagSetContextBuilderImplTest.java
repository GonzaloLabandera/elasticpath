/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.context.builders.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.store.Store;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;
import com.elasticpath.xpf.connectivity.entity.XPFStore;
import com.elasticpath.xpf.converters.StoreConverter;

/**
 * Test or {@link HttpTagSetContextBuilderImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpTagSetContextBuilderImplTest {

	private static final String TEST_STORE = "store";
	private static final String X_EP_USER_SCOPE = "x-ep-user-scope";
	private static final String X_EP_USER_TRAITS = "x-ep-user-traits";
	private static final String CUSTOMER_SEGMENT_USER_TRAIT = "CUSTOMER_SEGMENT=TEST_SEGMENT";
	private static final String CUSTOMER_SEGMENT_USER_TRAIT_VALUE = "TEST_SEGMENT";

	@Mock
	private HttpServletRequest request;
	@Mock
	private StoreService storeService;
	@Mock
	private StoreConverter storeConverter;
	@Mock
	private Store store;
	@Mock
	private XPFStore xpfStore;

	@InjectMocks
	private HttpTagSetContextBuilderImpl httpTagSetContextBuilder;

	@Test
	public void testBuildWithFullInputs() {
		when(request.getHeaders(X_EP_USER_SCOPE)).thenReturn(Collections.enumeration(Collections.singletonList(TEST_STORE)));
		when(storeService.findStoreWithCode(TEST_STORE)).thenReturn(store);
		when(storeConverter.convert(store)).thenReturn(xpfStore);

		when(request.getHeaders(X_EP_USER_TRAITS)).thenReturn(Collections.enumeration(Collections.singletonList(CUSTOMER_SEGMENT_USER_TRAIT)));

		XPFHttpTagSetContext xpfHttpTagSetContext = httpTagSetContextBuilder.build(request);
		assertEquals(xpfStore, xpfHttpTagSetContext.getStore());
		assertEquals(CUSTOMER_SEGMENT_USER_TRAIT_VALUE, xpfHttpTagSetContext.getUserTraitValues().get("CUSTOMER_SEGMENT"));
	}

	@Test
	public void testBuildWithMinInputs() {
		when(request.getHeaders(X_EP_USER_SCOPE)).thenReturn(Collections.enumeration(Collections.emptyList()));
		when(request.getHeaders(X_EP_USER_TRAITS)).thenReturn(Collections.enumeration(Collections.emptyList()));

		XPFHttpTagSetContext xpfHttpTagSetContext = httpTagSetContextBuilder.build(request);
		assertNull(xpfHttpTagSetContext.getStore());
		assertTrue(xpfHttpTagSetContext.getUserTraitValues().isEmpty());
	}
}