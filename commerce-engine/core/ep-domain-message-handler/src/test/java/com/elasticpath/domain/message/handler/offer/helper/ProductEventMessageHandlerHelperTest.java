/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.offer.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.catalog.ProductLookup;

/**
 * Tests {@link ProductEventMessageHandlerHelper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductEventMessageHandlerHelperTest {
	private static final String GUID = "guid";

	@Mock
	private ProductLookup productLookup;

	@InjectMocks
	private ProductEventMessageHandlerHelper helper;

	@Test
	public void shouldCallFindByKeyWithGuidFromEventMessage() {
		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getGuid()).thenReturn(GUID);

		helper.getExchangedEntity(eventMessage);

		verify(productLookup).findByGuid(GUID);
	}

}
