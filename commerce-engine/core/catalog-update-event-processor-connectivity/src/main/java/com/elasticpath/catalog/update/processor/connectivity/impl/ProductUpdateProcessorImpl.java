/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.bulk.DomainBulkEventType.OFFER_BULK_UPDATE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.OfferWriterRepository;
import com.elasticpath.catalog.update.processor.capabilities.ProductUpdateProcessor;
import com.elasticpath.catalog.update.processor.connectivity.impl.exception.NoCapabilityMatchedException;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;

/**
 * Implementation of {@link ProductUpdateProcessor}.
 */
public class ProductUpdateProcessorImpl implements ProductUpdateProcessor {

	/**
	 * Name of field in Brand bulk update event which contains list of products required for update.
	 */
	public static final String PRODUCTS = "products";

	private static final Logger LOGGER = LogManager.getLogger(ProductUpdateProcessorImpl.class);

	private final ProjectionService<Product, Offer> projectionService;
	private final OfferWriterRepository repository;
	private final EventMessagePublisher eventMessagePublisher;
	private final EventMessageFactory eventMessageFactory;
	private final int bulkChangeMaxEventSize;

	/**
	 * Constructor.
	 *
	 * @param projectionService      {@link ProjectionService} for projections building.
	 * @param provider               {@link CatalogProjectionPluginProvider}.
	 * @param eventMessagePublisher  publisher of  eventMessagePublisher.
	 * @param eventMessageFactory    event message factory.
	 * @param bulkChangeMaxEventSize number of products codes in one bulk event.
	 */
	public ProductUpdateProcessorImpl(final ProjectionService<Product, Offer> projectionService,
									  final CatalogProjectionPluginProvider provider,
									  final EventMessagePublisher eventMessagePublisher,
									  final EventMessageFactory eventMessageFactory,
									  final int bulkChangeMaxEventSize) {
		this.projectionService = projectionService;
		this.repository = provider.getCatalogProjectionPlugin()
				.getWriterCapability(OfferWriterRepository.class)
				.orElseThrow(NoCapabilityMatchedException::new);
		this.eventMessagePublisher = eventMessagePublisher;
		this.eventMessageFactory = eventMessageFactory;
		this.bulkChangeMaxEventSize = bulkChangeMaxEventSize;
	}


	@Override
	public void processProductCreated(final Product product, final ProductBundle... bundlesContainingProduct) {
		LOGGER.debug("Product created: {}", product.getGuid());

		final List<Offer> offers = product.getCatalogs().stream()
				.flatMap(catalog -> projectionService.buildProjections(product, catalog).stream())
				.collect(Collectors.toList());

		offers.forEach(repository::write);
	}

	@Override
	public void processProductUpdated(final Product product, final String... bundlesContainingProductCodes) {
		LOGGER.debug("Product updated: {}", product.getGuid());

		final List<Offer> offers = product.getCatalogs().stream()
				.flatMap(catalog -> projectionService.buildProjections(product, catalog).stream())
				.collect(Collectors.toList());

		boolean offerIsUpdated = false;
		for (Offer offer : offers) {
			final boolean updated = repository.write(offer);
			if (updated) {
				offerIsUpdated = true;
			}
		}

		if (offerIsUpdated) {
			final String productCode = product.getCode();

			final List<String> productBundleCodes = Arrays.stream(bundlesContainingProductCodes).sorted().collect(Collectors.toList());

			Lists.partition(productBundleCodes, bulkChangeMaxEventSize).stream()
					.map(codes -> eventMessageFactory.createEventMessage(OFFER_BULK_UPDATE, productCode, Collections.singletonMap(PRODUCTS, codes)))
					.forEach(eventMessagePublisher::publish);
		}

	}

	@Override
	public void processProductDeleted(final String guid, final ProductBundle... bundlesContainingProduct) {
		LOGGER.debug("Product deleted: {}", guid);

		repository.delete(guid);
	}
}
