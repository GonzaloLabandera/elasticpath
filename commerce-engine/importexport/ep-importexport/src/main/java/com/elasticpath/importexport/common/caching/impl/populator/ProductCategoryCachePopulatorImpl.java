/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.caching.impl.populator;

import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.caching.core.MutableCachingService;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductLoadTuner;
import com.elasticpath.importexport.common.caching.CachePopulator;
import com.elasticpath.importexport.common.dto.productcategory.ProductCategoriesDTO;
import com.elasticpath.service.query.CriteriaBuilder;
import com.elasticpath.service.query.QueryResult;
import com.elasticpath.service.query.QueryService;
import com.elasticpath.service.query.ResultType;
import com.elasticpath.service.query.relations.ProductRelation;

/**
 * Product category cache populator.
 */
public class ProductCategoryCachePopulatorImpl implements CachePopulator<ProductCategoriesDTO> {

	private QueryService<Product> queryService;
	private ProductLoadTuner productLoadTuner;
	private MutableCachingService<Product> productMutableCachingService;

	@Override
	public void populate(final List<ProductCategoriesDTO> dtos) {
		final List<String> productCodes = dtos.stream()
				.map(ProductCategoriesDTO::getProductCode)
				.collect(Collectors.toList());
		final List<Product> products = findProducts(productCodes);
		products.forEach(this::cacheProduct);
	}

	private List<Product> findProducts(final List<String> productCodes) {
		final QueryResult<Product> queryResult = queryService.query(CriteriaBuilder.criteriaFor(Product.class)
				.with(ProductRelation.having().codes(productCodes))
				.usingLoadTuner(productLoadTuner)
				.returning(ResultType.ENTITY));
		return queryResult.getResults();
	}

	private void cacheProduct(final Product product) {
		productMutableCachingService.cache(product);
	}

	public void setQueryService(final QueryService<Product> queryService) {
		this.queryService = queryService;
	}

	public void setProductLoadTuner(final ProductLoadTuner productLoadTuner) {
		this.productLoadTuner = productLoadTuner;
	}

	public void setProductMutableCachingService(final MutableCachingService<Product> productMutableCachingService) {
		this.productMutableCachingService = productMutableCachingService;
	}
}
