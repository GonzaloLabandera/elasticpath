/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.offer.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.update.processor.capabilities.ProductUpdateProcessor;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;

/**
 * Tests {@link ProductUpdatedEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductUpdatedEventHandlerTest {

	private static final String GUID = "guid";
	private static final String PRODUCT_BUNDLE_CODE = "bundle";


	@Mock
	private EventMessageHandlerHelper<Product> eventMessageHandlerHelper;

	@Mock
	private EventMessageHandlerHelper<Set<String>> productBundleEventMessageHandlerHelper;

	@Mock
	private ProductUpdateProcessor productUpdateProcessor;

	private ProductUpdatedEventHandler productUpdatedEventHandler;

	private EventMessage eventMessage;
	private Product product;

	@Before
	public void setUp() {
		eventMessage = mock(EventMessage.class);
		product = mock(Product.class);

		when(product.getGuid()).thenReturn(GUID);
		when(eventMessageHandlerHelper.getExchangedEntity(eventMessage)).thenReturn(product);
		when(productBundleEventMessageHandlerHelper.getExchangedEntity(eventMessage)).thenReturn(Collections.singleton(PRODUCT_BUNDLE_CODE));

		productUpdatedEventHandler = new ProductUpdatedEventHandler(eventMessageHandlerHelper, productBundleEventMessageHandlerHelper,
				productUpdateProcessor);
	}

	@Test
	public void shouldCallGetExchangedEntityForEventMessage() {
		productUpdatedEventHandler.handleMessage(eventMessage);

		verify(eventMessageHandlerHelper).getExchangedEntity(eventMessage);
	}

	@Test
	public void shouldCallProcessProductUpdatedForProduct() {
		productUpdatedEventHandler.handleMessage(eventMessage);

		verify(productUpdateProcessor).processProductUpdated(product, PRODUCT_BUNDLE_CODE);
	}


	@Test
	public void shouldCallProductBundleEventMessageHandlerHelperGetExchangedEntityForEventMessage() {
		productUpdatedEventHandler.handleMessage(eventMessage);

		verify(productBundleEventMessageHandlerHelper).getExchangedEntity(eventMessage);
	}

}