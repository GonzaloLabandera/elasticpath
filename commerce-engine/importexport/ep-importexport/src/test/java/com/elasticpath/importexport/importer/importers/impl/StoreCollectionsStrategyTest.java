/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.store.StoreDTO;
import com.elasticpath.domain.store.Store;
import com.elasticpath.importexport.importer.configuration.ImporterConfiguration;
import com.elasticpath.importexport.importer.types.CollectionStrategyType;
import com.elasticpath.importexport.importer.types.DependentElementType;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;

/**
 * Represents tests for StoreCollectionsStrategy.
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreCollectionsStrategyTest {

	@Mock
	private StorePaymentProviderConfigService storePaymentProviderConfigService;

	@Mock
	private ImporterConfiguration importerConfiguration;

	private StoreCollectionsStrategy storeCollectionsStrategy;

	@Before
	public void setUp() {
		when(importerConfiguration.getCollectionStrategyType(any(DependentElementType.class))).thenReturn(CollectionStrategyType.CLEAR_COLLECTION);

		storeCollectionsStrategy = new StoreCollectionsStrategy(storePaymentProviderConfigService, importerConfiguration);
	}

	@Test
	public void shouldCallStorePaymentProviderConfigServiceDeleteByStoreWhenStoreIsPersisted() {
		final Store store = mockStore();
		when(store.isPersisted()).thenReturn(true);

		final StoreDTO storeDTO = new StoreDTO();

		storeCollectionsStrategy.prepareCollections(store, storeDTO);

		verify(storePaymentProviderConfigService).deleteByStore(store);
	}

	@Test
	public void shouldNotCallStorePaymentProviderConfigServiceDeleteByStoreWhenStoreIsNotPersisted() {
		final Store store = mockStore();
		when(store.isPersisted()).thenReturn(false);

		final StoreDTO storeDTO = new StoreDTO();

		storeCollectionsStrategy.prepareCollections(store, storeDTO);

		verify(storePaymentProviderConfigService, never()).deleteByStore(any());
	}

	private Store mockStore() {
		final Store store = mock(Store.class);
		when(store.getTaxCodes()).thenReturn(Collections.emptySet());
		when(store.getTaxJurisdictions()).thenReturn(Collections.emptySet());
		when(store.getCreditCardTypes()).thenReturn(Collections.emptySet());

		return store;
	}

}