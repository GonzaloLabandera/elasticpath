/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.message.handler.category.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.catalog.CategoryLookup;

/**
 * Tests {@link CategoryEventMessageHandlerHelper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CategoryEventMessageHandlerHelperTest {
	private static final String GUID = "guid";

	@Mock
	private CategoryLookup categoryLookup;

	@InjectMocks
	private CategoryEventMessageHandlerHelper helper;

	@Test
	public void shouldCallFindByKeyWithGuidFromEventMessage() {
		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getGuid()).thenReturn(GUID);
		helper.getExchangedEntity(eventMessage);
		verify(categoryLookup).findByCompoundCategoryAndCatalogCodes(GUID);
	}
}
