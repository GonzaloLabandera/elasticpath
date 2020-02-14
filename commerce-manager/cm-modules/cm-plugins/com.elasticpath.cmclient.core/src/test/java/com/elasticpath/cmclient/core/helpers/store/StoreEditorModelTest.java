/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.core.helpers.store;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.StoreState;

/**
 * Unit test class for StoreEditorModel. Also see StoreEditorModelHelperTest.
 */
public class StoreEditorModelTest {

	@Rule
	public final MockitoRule rule = MockitoJUnit.rule();

	@Mock
	private Store store;

	private StoreEditorModel editorModel;

	/**
	 * Set up {@link StoreEditorModel} and mock store.
	 */
	@Before
	public void setUp() {
		editorModel = new StoreEditorModel(store);
	}

	@Test
	public void testIsStorePaymentConfigurationSavable() {
		StorePaymentConfigurationModel storePaymentConfigurationModel = new StorePaymentConfigurationModel(mock(StorePaymentProviderConfig.class),
				"Happy-Path-config", "Happy-Path-Config-Guid", "Provider", "method", true);
		editorModel.setStorePaymentConfigurations(Collections.singletonList(storePaymentConfigurationModel));
		when(store.getStoreState()).thenReturn(StoreState.OPEN);
		assertTrue(editorModel.isStorePaymentConfigurationSavable());

		storePaymentConfigurationModel.setSelected(false);
		assertFalse(editorModel.isStorePaymentConfigurationSavable());

		when(store.getStoreState()).thenReturn(StoreState.UNDER_CONSTRUCTION);
		assertTrue(editorModel.isStorePaymentConfigurationSavable());
	}
}
