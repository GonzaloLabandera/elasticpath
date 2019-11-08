/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.fieldmetadata.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.update.processor.capabilities.ModifierGroupUpdateProcessor;
import com.elasticpath.messaging.EventMessage;

/**
 * Tests {@link ModifierGroupDeletedEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierGroupDeletedEventHandlerTest {
	private static final String GUID = "guid";

	@Mock
	private ModifierGroupUpdateProcessor cartItemModifierGroupUpdateProcessor;

	@InjectMocks
	private ModifierGroupDeletedEventHandler cartItemModifierGroupDeletedEventHandler;

	private EventMessage eventMessage;

	@Before
	public void setUp() {
		eventMessage = mock(EventMessage.class);

		when(eventMessage.getGuid()).thenReturn(GUID);
	}

	@Test
	public void shouldCallProcessModifierGroupDeletedForGuid() {
		cartItemModifierGroupDeletedEventHandler.handleMessage(eventMessage);

		verify(cartItemModifierGroupUpdateProcessor).processModifierGroupDeleted(GUID);
	}


}