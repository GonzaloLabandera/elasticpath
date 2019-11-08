/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.skuoption.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.update.processor.capabilities.SkuOptionUpdateProcessor;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.messaging.EventMessage;

/**
 * Tests {@link SkuOptionUpdatedEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SkuOptionUpdatedEventHandlerTest {
	private static final String GUID = "guid";

	@Mock
	private EventMessageHandlerHelper<SkuOption> eventMessageHandlerHelper;

	@Mock
	private SkuOptionUpdateProcessor skuOptionUpdateProcessor;

	@InjectMocks
	private SkuOptionUpdatedEventHandler skuOptionUpdatedEventHandler;

	private EventMessage eventMessage;
	private SkuOption skuOption;

	@Before
	public void setUp() {
		eventMessage = mock(EventMessage.class);
		skuOption = mock(SkuOption.class);

		when(skuOption.getGuid()).thenReturn(GUID);
		when(eventMessageHandlerHelper.getExchangedEntity(eventMessage)).thenReturn(skuOption);
	}

	@Test
	public void shouldCallGetExchangedEntityForEventMessage() {
		skuOptionUpdatedEventHandler.handleMessage(eventMessage);

		verify(eventMessageHandlerHelper).getExchangedEntity(eventMessage);
	}

	@Test
	public void shouldCallProcessSkuOptionUpdatedForSkuOption() {
		skuOptionUpdatedEventHandler.handleMessage(eventMessage);

		verify(skuOptionUpdateProcessor).processSkuOptionUpdated(skuOption);
	}

}