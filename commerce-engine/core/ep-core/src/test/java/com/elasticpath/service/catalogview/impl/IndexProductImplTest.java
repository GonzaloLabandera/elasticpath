/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.catalogview.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.impl.CategoryImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;

/**
 * Test for IndexProductImpl class.
 */
public class IndexProductImplTest {

	/**
	 * Test that method getProductCategory was called by product.
	 */
	@Test
	public void testThatProductCategoryWasCalledByCorrectProduct() {
		final Product product = mock(ProductImpl.class);
		final Category category = mock(CategoryImpl.class);
		final IndexProductImpl indexProduct = new IndexProductImpl(product);
		indexProduct.getProductCategory(category);

		verify(product).getProductCategory(category);
	}
}
