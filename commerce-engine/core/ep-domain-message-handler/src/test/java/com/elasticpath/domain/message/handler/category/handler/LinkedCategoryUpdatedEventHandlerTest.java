/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.message.handler.category.handler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.update.processor.capabilities.CategoryUpdateProcessor;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.message.handler.EventMessageHandlerHelper;
import com.elasticpath.domain.message.handler.category.helper.LinkedCategoryEventMessageHelper;
import com.elasticpath.domain.message.handler.exception.EventMessageProcessingException;
import com.elasticpath.messaging.EventMessage;

/**
 * Test for {@link LinkedCategoryUpdatedEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LinkedCategoryUpdatedEventHandlerTest {
	private static final String GUID = "guid";
	private static final String STORE_CODE = "store_code";

	@Mock
	private EventMessageHandlerHelper<Category> eventMessageHandlerHelper;

	@Mock
	private CategoryUpdateProcessor categoryUpdateProcessor;

	@Mock
	private LinkedCategoryEventMessageHelper linkedCategoryEventMessageHelper;

	@InjectMocks
	private LinkedCategoryUpdatedEventHandler linkedCategoryUpdatedEventHandler;

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
		linkedCategoryUpdatedEventHandler.handleMessage(eventMessage);
		verify(eventMessageHandlerHelper).getExchangedEntity(eventMessage);
	}

	@Test
	public void shouldCallProcessCategoryUpdatedForCategory() {
		final List<String> stores = Collections.singletonList(STORE_CODE);

		when(linkedCategoryEventMessageHelper.getUnlinkedCategoryStores(eventMessage)).thenReturn(stores);

		linkedCategoryUpdatedEventHandler.handleMessage(eventMessage);
		verify(categoryUpdateProcessor).processCategoryIncludedExcluded(category, stores);
	}

	@Test
	public void shouldThrowsEventMessageProcessingExceptionWhenCategoryNotExist() {
		when(eventMessageHandlerHelper.getExchangedEntity(eventMessage)).thenReturn(null);
		when(eventMessage.getGuid()).thenReturn(GUID);

		assertThatExceptionOfType(EventMessageProcessingException.class)
				.isThrownBy(() -> linkedCategoryUpdatedEventHandler.handleMessage(eventMessage))
				.withMessage("Linked Category does not exist with guid: " + GUID);
	}
}
