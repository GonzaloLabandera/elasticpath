/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.option;

import static com.elasticpath.catalog.update.processor.connectivity.impl.SkuOptionUpdateProcessorImpl.PRODUCTS;
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
 * Tests {@link SkuOptionBulkEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OptionBulkUpdateEventHandlerTest {
	private static final String OPTION_GUID = "optionGuid";
	private static final String PRODUCT_CODE = "productCode";

	@Mock
	private SkuOptionBulkUpdateProcessor skuOptionBulkUpdateProcessor;

	@InjectMocks
	private SkuOptionBulkEventHandler skuOptionBulkEventHandler;

	@Test
	public void testShouldCallUpdateOptionDisplayNamesInOffersWithProductsCodesAndOptionGuidFromEventMessage() {
		final List<String> products = Collections.singletonList(PRODUCT_CODE);

		Map<String, Object> productsData = new HashMap<>();
		productsData.put(PRODUCTS, products);

		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getGuid()).thenReturn(OPTION_GUID);
		when(eventMessage.getData()).thenReturn(productsData);

		skuOptionBulkEventHandler.handleBulkEvent(eventMessage);

		verify(skuOptionBulkUpdateProcessor).updateSkuOptionDisplayNamesInOffers(products, OPTION_GUID);
	}
}
