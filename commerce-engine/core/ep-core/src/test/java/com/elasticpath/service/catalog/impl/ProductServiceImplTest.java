/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.service.catalog.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.exception.EpProductInUseException;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.persistence.dao.ProductDao;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.IndexType;

/**
 * Tests for the ProductServiceImpl class.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductServiceImplTest {

	@Mock
	private ProductDao productDao;

	@Mock
	private ProductLookup productLookup;

	@Mock
	private IndexNotificationService indexNotificationService;

	/**
	 * Tests if #removeProductTree throws exception when product is in a bundle.
	 */
	@Test(expected = EpProductInUseException.class)
	public void testRemoveProductTreeThrowsExceptionWhenProductIsInBundle() {
		final long aProductUid = 1L;
		final Product product = new ProductImpl();
		
		final ArrayList<Product> productsInBundleList = new ArrayList<>();
		productsInBundleList.add(product);
		
		ProductServiceImpl service = new ProductServiceImpl() {
			@Override
			public boolean canDelete(final Product product) {
				return false;
			}
		};
		
		service.setProductLookup(productLookup);
		when(productLookup.findByUid(aProductUid)).thenReturn(product);

		service.removeProductTree(aProductUid);
	}

	@Test
	public void testFindProductUidsByCategoryUids() {

		final List<Long> categoryUids = Arrays.asList(1L, 2L, 3L);
		final List<Long> expectedProductUids = Arrays.asList(4L, 5L, 6L);

		ProductServiceImpl service = new ProductServiceImpl();
		service.setProductDao(productDao);

		when(productDao.findUidsByCategoryUids(categoryUids)).thenReturn(expectedProductUids);

		final List<Long> actualProductUids = service.findUidsByCategoryUids(categoryUids);

		assertThat(actualProductUids).isEqualTo(expectedProductUids);
	}
	
	/**
	 * Tests that index notification update is posted when a product is added or updated.
	 */
	@Test
	public void testAddOrUpdateProductAddsIndexNotification() {
		final Product product = new ProductImpl();
		final ProductServiceImpl productService = new ProductServiceImpl() {
			@Override
			protected void validate(final Product product) {
				// Skip validation
			}
		};
		productService.setProductDao(productDao);
		productService.setIndexNotificationService(indexNotificationService);

		when(productDao.saveOrUpdate(product)).thenReturn(product);

		productService.saveOrUpdate(product);
		verify(indexNotificationService).addNotificationForEntityIndexUpdate(IndexType.PRODUCT, product.getUidPk());
	}
	
}
