/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.offer.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.update.processor.capabilities.ProductUpdateProcessor;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;

/**
 * Tests {@link ProductCreatedEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductCreatedEventHandlerTest {
	private static final String GUID = "guid";

	@Mock
	private EventMessageHandlerHelper<Product> eventMessageHandlerHelper;

	@Mock
	private ProductUpdateProcessor productUpdateProcessor;

	@InjectMocks
	private ProductCreatedEventHandler productCreatedEventHandler;

	private EventMessage eventMessage;
	private Product product;

	@Before
	public void setUp() {
		eventMessage = mock(EventMessage.class);
		product = mock(Product.class);

		when(product.getGuid()).thenReturn(GUID);
		when(eventMessageHandlerHelper.getExchangedEntity(eventMessage)).thenReturn(product);
	}

	@Test
	public void shouldCallGetExchangedEntityForEventMessage() {
		productCreatedEventHandler.handleMessage(eventMessage);

		verify(eventMessageHandlerHelper).getExchangedEntity(eventMessage);
	}

	@Test
	public void shouldCallProcessProductCreatedForProduct() {
		productCreatedEventHandler.handleMessage(eventMessage);

		verify(productUpdateProcessor).processProductCreated(product);
	}

}
