/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.offer;

import static com.elasticpath.catalog.update.processor.connectivity.impl.ProductUpdateProcessorImpl.PRODUCTS;
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
 * Tests {@link OfferBulkUpdateEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OfferBulkUpdateEventHandlerTest {

	private static final String OFFER_CODE = "offerCode";

	@Mock
	private OfferBulkUpdateProcessor offerBulkUpdateProcessor;

	@InjectMocks
	private OfferBulkUpdateEventHandler offerBulkUpdateEventHandler;

	@Test
	public void testShouldCallUpdateOffersWithListOfOfferCodesFromExchangeData() {
		final List<String> offerCodes = Collections.singletonList(OFFER_CODE);

		Map<String, Object> productsData = new HashMap<>();
		productsData.put(PRODUCTS, offerCodes);

		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getData()).thenReturn(productsData);

		offerBulkUpdateEventHandler.handleBulkEvent(eventMessage);

		verify(offerBulkUpdateProcessor).updateOffers(offerCodes);
		verify(offerBulkUpdateProcessor).updateOffers(offerCodes);
	}

}
