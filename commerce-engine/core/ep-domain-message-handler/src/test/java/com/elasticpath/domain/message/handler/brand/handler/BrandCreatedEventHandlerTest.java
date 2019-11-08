/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.brand.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.update.processor.capabilities.BrandUpdateProcessor;
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;

/**
 * Tests {@link BrandCreatedEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BrandCreatedEventHandlerTest {
	private static final String GUID = "guid";

	@Mock
	private EventMessageHandlerHelper<Brand> eventMessageHandlerHelper;

	@Mock
	private BrandUpdateProcessor brandUpdateProcessor;

	@InjectMocks
	private BrandCreatedEventHandler brandCreatedEventHandler;

	private EventMessage eventMessage;
	private Brand brand;

	@Before
	public void setUp() {
		eventMessage = mock(EventMessage.class);
		brand = mock(Brand.class);

		when(brand.getGuid()).thenReturn(GUID);
		when(eventMessageHandlerHelper.getExchangedEntity(eventMessage)).thenReturn(brand);
	}

	@Test
	public void shouldCallGetExchangedEntityForEventMessage() {
		brandCreatedEventHandler.handleMessage(eventMessage);

		verify(eventMessageHandlerHelper).getExchangedEntity(eventMessage);
	}

	@Test
	public void shouldCallProcessBrandCreatedForBrand() {
		brandCreatedEventHandler.handleMessage(eventMessage);

		verify(brandUpdateProcessor).processBrandCreated(brand);
	}

}
