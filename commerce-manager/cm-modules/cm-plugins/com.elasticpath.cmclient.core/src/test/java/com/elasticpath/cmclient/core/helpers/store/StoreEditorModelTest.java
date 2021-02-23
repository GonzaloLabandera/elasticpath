/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.core.helpers.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

	private static final String B2C_ROLE = "role";
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

	@Test
	public void testThatEditorModelUsesStoreToGetB2CAuthenticatedRole() {
		when(store.getB2CAuthenticatedRole()).thenReturn(B2C_ROLE);

		assertThat(editorModel.getB2CAuthenticatedRole()).isEqualTo(B2C_ROLE);
		verify(store).getB2CAuthenticatedRole();
	}

	@Test
	public void testThatEditorModelSetB2CAuthenticatedRoleForStore() {
		editorModel.setB2CAuthenticatedRole(B2C_ROLE);
		verify(store).setB2CAuthenticatedRole(B2C_ROLE);
	}

	@Test
	public void testThatEditorModelUsesStoreToGetB2CSingleSessionRole() {
		when(store.getB2CSingleSessionRole()).thenReturn(B2C_ROLE);

		assertThat(editorModel.getB2CSingleSessionRole()).isEqualTo(B2C_ROLE);
		verify(store).getB2CSingleSessionRole();
	}

	@Test
	public void testThatEditorModelSetB2CSingleSessionRole() {
		editorModel.setB2CSingleSessionRole(B2C_ROLE);
		verify(store).setB2CSingleSessionRole(B2C_ROLE);
	}
}
