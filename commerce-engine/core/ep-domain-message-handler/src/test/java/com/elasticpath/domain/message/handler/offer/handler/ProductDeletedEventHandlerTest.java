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
import com.elasticpath.messaging.EventMessage;

/**
 * Tests {@link ProductDeletedEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductDeletedEventHandlerTest {

	private static final String GUID = "guid";

	@Mock
	private ProductUpdateProcessor productUpdateProcessor;

	@InjectMocks
	private ProductDeletedEventHandler productDeletedEventHandler;

	private EventMessage eventMessage;

	@Before
	public void setUp() {
		eventMessage = mock(EventMessage.class);

		when(eventMessage.getGuid()).thenReturn(GUID);
	}

	@Test
	public void shouldCallProcessProductDeletedForGuid() {
		productDeletedEventHandler.handleMessage(eventMessage);

		verify(productUpdateProcessor).processProductDeleted(GUID);
	}

}
