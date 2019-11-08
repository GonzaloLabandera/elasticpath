/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.message.handler.category.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.update.processor.capabilities.CategoryUpdateProcessor;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.messaging.EventMessage;

/**
 * Tests {@link CategoryCreatedEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CategoryCreatedEventHandlerTest {
	private static final String GUID = "guid";

	@Mock
	private EventMessageHandlerHelper<Category> eventMessageHandlerHelper;

	@Mock
	private CategoryUpdateProcessor categoryUpdateProcessor;

	@InjectMocks
	private CategoryLinkedEventHandler categoryLinkedEventHandler;

	private EventMessage eventMessage;
	private Category category;

	@Before
	public void setUp() {
		eventMessage = mock(EventMessage.class);
		category = mock(Category.class);
		when(category.getGuid()).thenReturn(GUID);
		when(eventMessageHandlerHelper.getExchangedEntity(eventMessage)).thenReturn(category);
	}

	@Test
	public void shouldCallGetExchangedEntityForEventMessage() {
		categoryLinkedEventHandler.handleMessage(eventMessage);
		verify(eventMessageHandlerHelper).getExchangedEntity(eventMessage);
	}

	@Test
	public void shouldCallProcessCategoryLinkedForCategory() {
		categoryLinkedEventHandler.handleMessage(eventMessage);
		verify(categoryUpdateProcessor).processCategoryLinked(category);
	}
}
