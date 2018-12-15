package com.elasticpath.service.catalog.impl;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.store.Store;

/**
 * Unit tests for {@link DependentItemLookupImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DependentItemLookupImplTest {

	@Mock
	private Store store;

	@Mock
	private ProductSku parentSku;

	@InjectMocks
	private DependentItemLookupImpl objectUnderTest;

	@Test
	public void verifyFindDependentItemsForSkuRequiresStore() {
		final String expectedMessage = "Store is required";

		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> objectUnderTest.findDependentItemsForSku(null, parentSku))
				.withMessage(expectedMessage);

		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> objectUnderTest.findDependentItemsForSku(null, null))
				.withMessage(expectedMessage);
	}

	@Test
	public void verifyFindDependentItemsForSkuRequiresParentSku() {
		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> objectUnderTest.findDependentItemsForSku(store, null))
				.withMessage("Parent SKU is required");
	}



}