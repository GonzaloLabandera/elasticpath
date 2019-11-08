/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.fieldmetadata.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Tests {@link ModifierGroupEventMessageHandlerHelper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierGroupEventMessageHandlerHelperTest {
	private static final String GUID = "guid";

	@Mock
	private ModifierService cartItemModifierService;

	@InjectMocks
	private ModifierGroupEventMessageHandlerHelper helper;

	@Test
	public void shouldCallModifierFieldByCodeWithGuidFromEventMessage() {
		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getGuid()).thenReturn(GUID);

		helper.getExchangedEntity(eventMessage);

		verify(cartItemModifierService).findModifierGroupByCode(GUID);
	}

}