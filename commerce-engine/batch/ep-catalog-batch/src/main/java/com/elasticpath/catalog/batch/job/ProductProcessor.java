/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.batch.job;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.item.ItemProcessor;

import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.service.catalog.ProductLookup;

/**
 * Represents an implementation of {@link ItemProcessor} to create a list of Projections for Product entity.
 */
public class ProductProcessor implements ItemProcessor<Product, List<Offer>> {

	private final ProjectionService<Product, Offer> projectionService;
	private final ProductLookup productLookup;

	/**
	 * Constructor.
	 *
	 * @param projectionService Product projection service.
	 * @param productLookup     {@link ProductLookup} data service.
	 */
	public ProductProcessor(final ProjectionService<Product, Offer> projectionService, final ProductLookup productLookup) {
		this.projectionService = projectionService;
		this.productLookup = productLookup;
	}

	@Override
	public List<Offer> process(final Product entity) {
		final Product product = productLookup.findByGuid(entity.getGuid());

		return product.getCatalogs().stream()
				.flatMap(catalog -> projectionService.buildProjections(product, catalog).stream())
				.collect(Collectors.toList());
	}

}
