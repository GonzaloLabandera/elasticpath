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
 * Tests {@link CategoryLinkedEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CategoryLinkedEventHandlerTest {
	private static final String GUID = "guid";

	@Mock
	private EventMessageHandlerHelper<Category> eventMessageHandlerHelper;

	@Mock
	private CategoryUpdateProcessor categoryUpdateProcessor;

	@InjectMocks
	private CategoryCreatedEventHandler categoryCreatedEventHandler;

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
		categoryCreatedEventHandler.handleMessage(eventMessage);
		verify(eventMessageHandlerHelper).getExchangedEntity(eventMessage);
	}

	@Test
	public void shouldCallProcessCategoryCreatedForCategory() {
		categoryCreatedEventHandler.handleMessage(eventMessage);
		verify(categoryUpdateProcessor).processCategoryCreated(category);
	}
}
