/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.bulk.DomainBulkEventType.ATTRIBUTE_BULK_UPDATE;
import static com.elasticpath.catalog.bulk.DomainBulkEventType.ATTRIBUTE_CATEGORY_BULK_UPDATE;
import static com.elasticpath.catalog.bulk.DomainBulkEventType.ATTRIBUTE_SKU_BULK_UPDATE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.AttributeWriterRepository;
import com.elasticpath.catalog.update.processor.capabilities.AttributeUpdateProcessor;
import com.elasticpath.catalog.update.processor.connectivity.impl.exception.NoCapabilityMatchedException;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.dao.ProductDao;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.catalog.ProductSkuService;

/**
 * Implementation of {@link AttributeUpdateProcessor}.
 */
public class AttributeUpdateProcessorImpl implements AttributeUpdateProcessor {

	private static final Logger LOGGER = Logger.getLogger(AttributeUpdateProcessorImpl.class);
	private static final String PRODUCTS = "products";
	private final ProjectionService<com.elasticpath.domain.attribute.Attribute, Attribute> projectionService;
	private final AttributeWriterRepository repository;
	private final EventMessagePublisher eventMessagePublisher;
	private final EventMessageFactory eventMessageFactory;
	private final ProductDao productDao;
	private final CategoryService categoryService;
	private final ProductSkuService productSkuService;
	private final int bulkChangeMaxEventSize;

	/**
	 * Constructor for AttributeUpdateProcessorImpl.
	 *
	 * @param projectionService      {@link ProjectionService} for projections building.
	 * @param provider               {@link  CatalogProjectionPluginProvider}.
	 * @param eventMessagePublisher  publisher of  eventMessagePublisher.
	 * @param eventMessageFactory    event message factory.
	 * @param productDao             product Dao.
	 * @param categoryService        category Service.
	 * @param productSkuService      product Sku Service.
	 * @param bulkChangeMaxEventSize number of products codes in one bulk event.
	 */
	@SuppressWarnings({"PMD.ExcessiveParameterList", "checkstyle:parameternumber"})
	public AttributeUpdateProcessorImpl(final ProjectionService<com.elasticpath.domain.attribute.Attribute, Attribute> projectionService,
										final CatalogProjectionPluginProvider provider,
										final EventMessagePublisher eventMessagePublisher,
										final EventMessageFactory eventMessageFactory,
										final ProductDao productDao,
										final CategoryService categoryService,
										final ProductSkuService productSkuService,
										final int bulkChangeMaxEventSize) {
		this.projectionService = projectionService;
		this.repository = provider.getCatalogProjectionPlugin()
				.getWriterCapability(AttributeWriterRepository.class)
				.orElseThrow(NoCapabilityMatchedException::new);
		this.eventMessagePublisher = eventMessagePublisher;
		this.eventMessageFactory = eventMessageFactory;
		this.productDao = productDao;
		this.categoryService = categoryService;
		this.productSkuService = productSkuService;
		this.bulkChangeMaxEventSize = bulkChangeMaxEventSize;
	}

	@Override
	public void processAttributeCreated(final com.elasticpath.domain.attribute.Attribute attribute) {
		LOGGER.debug("Attribute created: " + attribute.getGuid());

		final List<Attribute> attributes = projectionService.buildAllStoresProjections(attribute);
		attributes.forEach(repository::write);
	}

	@Override
	public void processAttributeUpdated(final com.elasticpath.domain.attribute.Attribute attribute) {
		LOGGER.debug("Attribute updated: " + attribute.getGuid());
		boolean attributeIsUpdated = false;
		final List<Attribute> attributes = projectionService.buildAllStoresProjections(attribute);
		for (Attribute attributeProjections : attributes) {
			final boolean updated = repository.write(attributeProjections);
			if (updated) {
				attributeIsUpdated = true;
			}
		}
		if (attributeIsUpdated) {
			messagePublishForProduct(attribute);
			messagePublishForCategory(attribute);
			messagePublishForSku(attribute);
		}
	}

	@Override
	public void processAttributeDeleted(final String guid) {
		LOGGER.debug("Attribute deleted: " + guid);

		repository.delete(guid);
	}

	private void messagePublishForProduct(final com.elasticpath.domain.attribute.Attribute attribute) {
		final List<Long> uids = productDao.findUidsByAttribute(attribute);
		final Map<Long, String> map = productDao.findCodesByUids(uids);
		final List<String> productCodes = map.values()
				.stream()
				.sorted()
				.collect(Collectors.toList());

		Lists.partition(productCodes, bulkChangeMaxEventSize).stream()
				.map(codes -> eventMessageFactory.createEventMessage(ATTRIBUTE_BULK_UPDATE, attribute.getGuid(),
						Collections.singletonMap(PRODUCTS, codes)))
				.forEach(eventMessagePublisher::publish);
	}

	private void messagePublishForCategory(final com.elasticpath.domain.attribute.Attribute attribute) {
		final List<Long> uids = categoryService.findUidsByAttribute(attribute);
		final List<String> categoryCodes = categoryService.findCodesByUids(uids)
				.stream()
				.sorted()
				.collect(Collectors.toList());

		Lists.partition(categoryCodes, bulkChangeMaxEventSize).
				stream().map(codes -> eventMessageFactory.createEventMessage(ATTRIBUTE_CATEGORY_BULK_UPDATE, attribute.getGuid(),
				Collections.singletonMap(PRODUCTS, codes)))
				.forEach(eventMessagePublisher::publish);
	}

	private void messagePublishForSku(final com.elasticpath.domain.attribute.Attribute attribute) {
		final List<String> productCodes = productSkuService.findCodesByAttribute(attribute)
				.stream()
				.sorted()
				.collect(Collectors.toList());

		Lists.partition(productCodes, bulkChangeMaxEventSize).
				stream().map(codes -> eventMessageFactory.createEventMessage(ATTRIBUTE_SKU_BULK_UPDATE, attribute.getGuid(),
				Collections.singletonMap(PRODUCTS, codes)))
				.forEach(eventMessagePublisher::publish);
	}
}
