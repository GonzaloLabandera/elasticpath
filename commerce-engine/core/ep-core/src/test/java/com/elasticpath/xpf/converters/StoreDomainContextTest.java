/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.store.Store;

@RunWith(MockitoJUnitRunner.class)
public class StoreDomainContextTest {

	@Mock
	private Store store;

	@Mock
	private Object object;

	private StoreDomainContext storeDomainContext;

	@Test
	public void getStoreTest() {
		storeDomainContext = new StoreDomainContext<>(object, store);
		assertTrue(storeDomainContext.getStore().isPresent());
		assertEquals(store, storeDomainContext.getStore().get());
	}

	@Test
	public void getStoreTestWithOptionalParameter() {
		storeDomainContext = new StoreDomainContext<>(object, store);
		assertTrue(storeDomainContext.getStore().isPresent());
		assertEquals(store, storeDomainContext.getStore().get());
	}

	@Test
	public void getEmptyStoreTestWithNoStore() {
		storeDomainContext = new StoreDomainContext<>(object, Optional.empty());
		assertFalse(storeDomainContext.getStore().isPresent());
	}

	@Test
	public void getEmptyStoreTestWithNullStoreParameter() {
		storeDomainContext = new StoreDomainContext<>(object, (Store) null);
		assertFalse(storeDomainContext.getStore().isPresent());
	}

	@Test
	public void getDomainTest() {
		storeDomainContext = new StoreDomainContext<>(object, store);
		assertEquals(object, storeDomainContext.getDomain());
	}
}
