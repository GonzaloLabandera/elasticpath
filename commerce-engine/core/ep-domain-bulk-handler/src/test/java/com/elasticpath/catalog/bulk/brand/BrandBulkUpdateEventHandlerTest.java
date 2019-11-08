/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.brand;

import static com.elasticpath.catalog.update.processor.connectivity.impl.BrandUpdateProcessorImpl.PRODUCTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.messaging.EventMessage;

/**
 * Tests {@link BrandBulkUpdateEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BrandBulkUpdateEventHandlerTest {

	private static final String BRAND_GUID = "brandGuid";
	private static final String PRODUCT_CODE = "productCode";

	@Mock
	private BrandBulkUpdateProcessor brandBulkUpdateProcessor;

	@InjectMocks
	private BrandBulkUpdateEventHandler brandBulkUpdateEventHandler;

	/**
	 * Method BrandBulkUpdateEventHandler#handleBulkEvent should calls BrandBulkUpdateProcessor#updateBrandDisplayNamesInOffers.
	 */
	@Test
	public void testShouldCallUpdateBrandDisplayNamesInOffersWithProductsCodesAndBrandGuidFromEventMessage() {
		final List<String> products = Collections.singletonList(PRODUCT_CODE);

		Map<String, Object> productsData = new HashMap<>();
		productsData.put(PRODUCTS, products);

		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getGuid()).thenReturn(BRAND_GUID);
		when(eventMessage.getData()).thenReturn(productsData);

		brandBulkUpdateEventHandler.handleBulkEvent(eventMessage);

		verify(brandBulkUpdateProcessor).updateBrandDisplayNamesInOffers(products, BRAND_GUID);
	}

}