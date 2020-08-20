/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.caching.impl.populator;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.caching.core.MutableCachingService;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductLoadTuner;
import com.elasticpath.importexport.common.dto.productcategory.ProductCategoriesDTO;
import com.elasticpath.service.query.CriteriaBuilder;
import com.elasticpath.service.query.QueryResult;
import com.elasticpath.service.query.QueryService;
import com.elasticpath.service.query.ResultType;
import com.elasticpath.service.query.relations.ProductRelation;

/**
 * Tests {@link ProductCategoryCachePopulatorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductCategoryCachePopulatorImplTest {

	private static final String PRODUCT_CODE1 = "productCode1";
	private static final String PRODUCT_CODE2 = "productCode2";

	@InjectMocks
	private ProductCategoryCachePopulatorImpl populator;

	@Mock
	private QueryService<Product> queryService;
	@Mock
	private ProductLoadTuner productLoadTuner;
	@Mock
	private MutableCachingService<Product> productMutableCachingService;
	@Mock
	private Product product1;
	@Mock
	private Product product2;
	@Mock
	private QueryResult<Product> productQueryResult;

	private ProductCategoriesDTO productCategoriesDTO1;
	private ProductCategoriesDTO productCategoriesDTO2;

	@Before
	public void setUp() {
		productCategoriesDTO1 = new ProductCategoriesDTO();
		productCategoriesDTO1.setProductCode(PRODUCT_CODE1);

		productCategoriesDTO2 = new ProductCategoriesDTO();
		productCategoriesDTO2.setProductCode(PRODUCT_CODE2);

		given(productQueryResult.getResults()).willReturn(Arrays.asList(product1, product2));
		willReturn(productQueryResult).given(queryService).query(CriteriaBuilder.criteriaFor(Product.class)
				.with(ProductRelation.having().codes(Arrays.asList(PRODUCT_CODE1, PRODUCT_CODE2)))
				.usingLoadTuner(productLoadTuner)
				.returning(ResultType.ENTITY));
	}

	@Test
	public void testPopulateWithProducts() {
		populator.populate(Arrays.asList(productCategoriesDTO1, productCategoriesDTO2));

		verify(productMutableCachingService).cache(product1);
		verify(productMutableCachingService).cache(product2);
	}
}
