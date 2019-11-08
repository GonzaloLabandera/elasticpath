/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.bulk.DomainBulkEventType.BRAND_BULK_UPDATE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.BrandWriterRepository;
import com.elasticpath.catalog.update.processor.capabilities.BrandUpdateProcessor;
import com.elasticpath.catalog.update.processor.connectivity.impl.exception.NoCapabilityMatchedException;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.service.query.CriteriaBuilder;
import com.elasticpath.service.query.QueryService;
import com.elasticpath.service.query.ResultType;
import com.elasticpath.service.query.relations.BrandRelation;

/**
 * Implementation of {@link BrandUpdateProcessor}.
 */
public class BrandUpdateProcessorImpl implements BrandUpdateProcessor {

	/**
	 * Name of field in Brand bulk update event which contains list of products required for update.
	 */
	public static final String PRODUCTS = "products";

	private static final Logger LOGGER = Logger.getLogger(BrandUpdateProcessorImpl.class);

	private final ProjectionService<Brand, com.elasticpath.catalog.entity.brand.Brand> projectionService;
	private final BrandWriterRepository repository;
	private final EventMessagePublisher eventMessagePublisher;
	private final QueryService<Product> productQueryService;
	private final EventMessageFactory eventMessageFactory;
	private final int bulkChangeMaxEventSize;

	/**
	 * BrandUpdateProcessorImpl constructor.
	 *
	 * @param projectionService      {@link ProjectionService} for projections building.
	 * @param provider               {@link CatalogProjectionPluginProvider}.
	 * @param eventMessagePublisher  publisher of  eventMessagePublisher.
	 * @param productQueryService    query service for a Product domain entities.
	 * @param eventMessageFactory    event message factory.
	 * @param bulkChangeMaxEventSize number of products codes in one bulk event.
	 */
	public BrandUpdateProcessorImpl(final ProjectionService<Brand, com.elasticpath.catalog.entity.brand.Brand> projectionService,
									final CatalogProjectionPluginProvider provider,
									final EventMessagePublisher eventMessagePublisher,
									final QueryService<Product> productQueryService,
									final EventMessageFactory eventMessageFactory,
									final int bulkChangeMaxEventSize) {
		this.projectionService = projectionService;
		this.repository = provider.getCatalogProjectionPlugin()
				.getWriterCapability(BrandWriterRepository.class)
				.orElseThrow(NoCapabilityMatchedException::new);
		this.eventMessagePublisher = eventMessagePublisher;
		this.productQueryService = productQueryService;
		this.eventMessageFactory = eventMessageFactory;
		this.bulkChangeMaxEventSize = bulkChangeMaxEventSize;
	}

	@Override
	public void processBrandCreated(final Brand brand) {
		LOGGER.debug("Brand created: " + brand.getGuid());

		final List<com.elasticpath.catalog.entity.brand.Brand> brands = projectionService.buildAllStoresProjections(brand);
		brands.forEach(repository::write);
	}

	@Override
	public void processBrandUpdated(final Brand brand) {
		LOGGER.debug("Brand updated: " + brand.getGuid());

		final List<com.elasticpath.catalog.entity.brand.Brand> brands = projectionService.buildAllStoresProjections(brand);

		final List<com.elasticpath.catalog.entity.brand.Brand> updatedBrands = new ArrayList<>();
		for (com.elasticpath.catalog.entity.brand.Brand brandProjection : brands) {
			boolean updated = repository.write(brandProjection);
			if (updated) {
				updatedBrands.add(brandProjection);
			}
		}

		if (!updatedBrands.isEmpty()) {
			final String code = brand.getCode();

			final List<Product> products = productQueryService.<Product>query(CriteriaBuilder.criteriaFor(Product.class)
					.with(BrandRelation.having().codes(code))
					.returning(ResultType.ENTITY))
					.getResults();

			if (!products.isEmpty()) {

				final List<String> productCodes = products.stream()
						.map(Product::getCode)
						.sorted()
						.collect(Collectors.toList());

				Lists.partition(productCodes, bulkChangeMaxEventSize).stream()
						.map(codes -> eventMessageFactory.createEventMessage(BRAND_BULK_UPDATE, code, Collections.singletonMap(PRODUCTS, codes)))
						.forEach(eventMessagePublisher::publish);
			}
		}
	}

	@Override
	public void processBrandDeleted(final String guid) {
		LOGGER.debug("Brand deleted: " + guid);

		repository.delete(guid);
	}
}