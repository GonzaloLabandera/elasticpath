/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.message.handler.category.handler;

import static com.elasticpath.domain.catalog.Category.CATEGORY_LEGACY_GUID_DELIMITER;
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
import com.elasticpath.domain.message.handler.category.helper.LinkedCategoryEventMessageHelper;
import com.elasticpath.messaging.EventMessage;

/**
 * Tests {@link CategoryUnlinkedEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CategoryUnlinkedEventHandlerTest {
	private static final String GUID = "guid";
	private static final String CATALOG = "catalog";
	private static final String STORE_CODE = "store";
	private static final String COMPOUND_GUID = GUID + CATEGORY_LEGACY_GUID_DELIMITER + CATALOG;

	@Mock
	private CategoryUpdateProcessor categoryUpdateProcessor;

	@Mock
	private LinkedCategoryEventMessageHelper linkedCategoryEventMessageHelper;

	@InjectMocks
	private CategoryUnlinkedEventHandler categoryUnlinkedEventHandler;

	private EventMessage eventMessage;

	@Before
	public void setUp() {
		eventMessage = mock(EventMessage.class);
		when(eventMessage.getGuid()).thenReturn(COMPOUND_GUID);
	}

	@Test
	public void shouldCallProcessCategoryUnlinkedForCategory() {
		final List<String> stores = Collections.singletonList(STORE_CODE);

		when(linkedCategoryEventMessageHelper.getUnlinkedCategoryCode(eventMessage)).thenReturn(GUID);
		when(linkedCategoryEventMessageHelper.getUnlinkedCategoryStores(eventMessage)).thenReturn(stores);

		categoryUnlinkedEventHandler.handleMessage(eventMessage);
		verify(categoryUpdateProcessor).processCategoryUnlinked(GUID, stores);
	}
}
