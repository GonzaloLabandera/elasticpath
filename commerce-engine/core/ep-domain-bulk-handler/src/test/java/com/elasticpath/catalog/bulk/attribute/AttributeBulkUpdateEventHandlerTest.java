/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.attribute;

import static com.elasticpath.catalog.bulk.attribute.AttributeBulkEventHandler.PRODUCTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.messaging.EventMessage;

/**
 * Tests {@link AttributeBulkEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributeBulkUpdateEventHandlerTest {
	private static final String ATTRIBUTE_GUID = "attributeGuid";
	private static final String PRODUCT_CODE = "productCode";
	private static final String CATEGORY_CODE = "categoryCode";

	@Mock
	private AttributeBulkUpdateProcessor attributeBulkUpdateProcessor;

	@Mock
	private AttributeSkuBulkUpdateProcessor attributeSkuBulkUpdateProcessor;

	@Mock
	private AttributeCategoryBulkUpdateProcessor attributeCategoryBulkUpdateProcessor;

	@InjectMocks
	private AttributeBulkEventHandler attributeBulkEventHandler;

	@InjectMocks
	private AttributeCategoryBulkEventHandler attributeCategoryBulkEventHandler;

	@InjectMocks
	private AttributeSkuBulkEventHandler attributeSkuBulkEventHandler;

	@Test
	public void testShouldCallUpdateAttributeDisplayNameInOffersWithProductsCodesAndAttributeGuidFromEventMessage() {
		final List<String> products = Collections.singletonList(PRODUCT_CODE);

		Map<String, Object> productsData = new HashMap<>();
		productsData.put(PRODUCTS, products);

		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getGuid()).thenReturn(ATTRIBUTE_GUID);
		when(eventMessage.getData()).thenReturn(productsData);

		attributeBulkEventHandler.handleBulkEvent(eventMessage);

		verify(attributeBulkUpdateProcessor).updateAttributeDisplayNameInOffers(products, ATTRIBUTE_GUID);
	}

	@Test
	public void testShouldCallUpdateAttributeDisplayNameInCategoriesWithCategoriesCodesAndAttributeGuidFromEventMessage() {
		final List<String> categories = Collections.singletonList(CATEGORY_CODE);

		Map<String, Object> categoriesData = new HashMap<>();
		categoriesData.put(PRODUCTS, categories);

		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getGuid()).thenReturn(ATTRIBUTE_GUID);
		when(eventMessage.getData()).thenReturn(categoriesData);

		attributeCategoryBulkEventHandler.handleBulkEvent(eventMessage);

		verify(attributeCategoryBulkUpdateProcessor).updateCategoryAttributeDisplayNameInCategories(categories, ATTRIBUTE_GUID);
	}

	@Test
	public void testShouldCallUpdateSkuAttributeDisplayNameInOffersWithProductsCodesAndAttributeGuidFromEventMessage() {
		final List<String> products = Collections.singletonList(PRODUCT_CODE);

		Map<String, Object> productsData = new HashMap<>();
		productsData.put(PRODUCTS, products);

		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getGuid()).thenReturn(ATTRIBUTE_GUID);
		when(eventMessage.getData()).thenReturn(productsData);

		attributeSkuBulkEventHandler.handleBulkEvent(eventMessage);

		verify(attributeSkuBulkUpdateProcessor).updateSkuAttributeDisplayNameInOffers(products, ATTRIBUTE_GUID);
	}

}
