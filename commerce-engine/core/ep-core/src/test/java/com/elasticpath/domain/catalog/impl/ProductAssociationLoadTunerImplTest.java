/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.catalog.impl;

import static com.elasticpath.persistence.support.FetchFieldConstants.CATALOG;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.openjpa.persistence.FetchPlan;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.ProductAssociationLoadTuner;
import com.elasticpath.domain.catalog.ProductLoadTuner;

/**
 * Test class for {@link CategoryLoadTunerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductAssociationLoadTunerImplTest {

	@Mock
	private FetchPlan mockFetchPlan;

	@Test
	public void shouldConfigureWithLazyField() {
		final ProductAssociationLoadTuner loadTuner = new ProductAssociationLoadTunerImpl();
		loadTuner.setLoadingCatalog(true);

		loadTuner.configure(mockFetchPlan);

		verify(mockFetchPlan).addField(ProductAssociationImpl.class, CATALOG);
	}

	@Test
	public void shouldConfigureWithProductLoadTuner() {
		final ProductAssociationLoadTuner loadTuner = new ProductAssociationLoadTunerImpl();
		ProductLoadTuner mockProductLoadTuner = mock(ProductLoadTuner.class);

		loadTuner.setProductLoadTuner(mockProductLoadTuner);

		loadTuner.configure(mockFetchPlan);

		verify(mockProductLoadTuner).configure(mockFetchPlan);
		verifyZeroInteractions(mockFetchPlan);
	}
}
