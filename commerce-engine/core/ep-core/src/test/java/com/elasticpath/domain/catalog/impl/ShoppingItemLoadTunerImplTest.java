/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.catalog.impl;

import static com.elasticpath.persistence.support.FetchFieldConstants.DEFAULT_ASSOCIATION_QUANTITY;
import static com.elasticpath.persistence.support.FetchFieldConstants.DEFAULT_SKU;
import static com.elasticpath.persistence.support.FetchFieldConstants.DEPENDENT_ITEMS_INTERNAL;
import static com.elasticpath.persistence.support.FetchFieldConstants.GUID;
import static com.elasticpath.persistence.support.FetchFieldConstants.PARENT_ITEM;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRICE;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRODUCT_SKU;
import static com.elasticpath.persistence.support.FetchFieldConstants.PRODUCT_SKUS_INTERNAL;
import static com.elasticpath.persistence.support.FetchFieldConstants.QUANTITY_INTERNAL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import org.apache.openjpa.persistence.FetchPlan;

import com.elasticpath.domain.catalog.ShoppingItemLoadTuner;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;
import com.elasticpath.persistence.support.FetchGroupConstants;

/**
 * Test <code>ProductLoadTunerImpl</code>.
 */
public class ShoppingItemLoadTunerImplTest {

	@Test
	public void shouldConfigureWithLazyFieldsAndGroup() {

		FetchPlan mockFetchPlan = mock(FetchPlan.class);
		final ShoppingItemLoadTuner loadTuner = new ShoppingItemLoadTunerImpl();

		loadTuner.setLoadingDefaultAssociationQuantity(true);
		loadTuner.setLoadingDependentItems(true);
		loadTuner.setLoadingParentItem(true);
		loadTuner.setLoadingPrice(true);
		loadTuner.setLoadingProductSku(true);
		loadTuner.setLoadingQuantity(true);
		loadTuner.setLoadingRecursiveDependentItems(true);

		loadTuner.configure(mockFetchPlan);

		verify(mockFetchPlan).addField(GUID);
		verify(mockFetchPlan).addField(ShoppingItemImpl.class, DEFAULT_ASSOCIATION_QUANTITY);
		verify(mockFetchPlan).addField(ShoppingItemImpl.class, QUANTITY_INTERNAL);
		verify(mockFetchPlan).addField(ShoppingItemImpl.class, DEPENDENT_ITEMS_INTERNAL);
		verify(mockFetchPlan).addField(ShoppingItemImpl.class, PARENT_ITEM);
		verify(mockFetchPlan).addField(ShoppingItemImpl.class, PRODUCT_SKU);
		verify(mockFetchPlan).addField(ShoppingItemImpl.class, PRICE);
		verify(mockFetchPlan).addField(ProductImpl.class, DEFAULT_SKU);
		verify(mockFetchPlan).addField(ProductImpl.class, PRODUCT_SKUS_INTERNAL);
		verify(mockFetchPlan).addFetchGroup(FetchGroupConstants.SHOPPING_ITEM_CHILD_ITEMS);
	}
}
