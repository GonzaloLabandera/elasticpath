/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.bulk.DomainBulkEventType.OPTION_BULK_UPDATE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.OptionWriterRepository;
import com.elasticpath.catalog.update.processor.capabilities.SkuOptionUpdateProcessor;
import com.elasticpath.catalog.update.processor.connectivity.impl.exception.NoCapabilityMatchedException;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.dao.ProductDao;

/**
 * Implementation of {@link SkuOptionUpdateProcessor}.
 */
public class SkuOptionUpdateProcessorImpl implements SkuOptionUpdateProcessor {

	/**
	 * Name of field in Brand bulk update event which contains list of products required for update.
	 */
	public static final String PRODUCTS = "products";

	private static final Logger LOGGER = Logger.getLogger(SkuOptionUpdateProcessorImpl.class);

	private final ProjectionService<SkuOption, Option> projectionService;
	private final OptionWriterRepository repository;
	private final EventMessagePublisher eventMessagePublisher;
	private final EventMessageFactory eventMessageFactory;
	private final ProductDao productDao;
	private final int bulkChangeMaxEventSize;

	/**
	 * SkuOptionUpdateProcessorImpl constructor.
	 *
	 * @param projectionService      {@link ProjectionService} for projections building.
	 * @param provider               {@link CatalogProjectionPluginProvider}.
	 * @param eventMessagePublisher  publisher of  eventMessagePublisher.
	 * @param eventMessageFactory    event message factory.
	 * @param productDao             productDao.
	 * @param bulkChangeMaxEventSize number of products codes in one bulk event.
	 */
	public SkuOptionUpdateProcessorImpl(final ProjectionService<SkuOption, Option> projectionService,
										final CatalogProjectionPluginProvider provider,
										final EventMessagePublisher eventMessagePublisher,
										final EventMessageFactory eventMessageFactory,
										final ProductDao productDao,
										final int bulkChangeMaxEventSize) {
		this.projectionService = projectionService;
		this.repository = provider.getCatalogProjectionPlugin()
				.getWriterCapability(OptionWriterRepository.class)
				.orElseThrow(NoCapabilityMatchedException::new);
		this.eventMessagePublisher = eventMessagePublisher;
		this.eventMessageFactory = eventMessageFactory;
		this.productDao = productDao;
		this.bulkChangeMaxEventSize = bulkChangeMaxEventSize;
	}

	@Override
	public void processSkuOptionCreated(final SkuOption skuOption) {
		LOGGER.debug("SkuOption created: " + skuOption.getGuid());

		final List<Option> options = projectionService.buildAllStoresProjections(skuOption);
		options.forEach(repository::write);
	}

	@Override
	public void processSkuOptionUpdated(final SkuOption skuOption) {
		LOGGER.debug("SkuOption updated: " + skuOption.getGuid());
		boolean skuOptionIsUpdated = false;
		final List<Option> options = projectionService.buildAllStoresProjections(skuOption);
		for (Option optionProjection : options) {
			final boolean updated = repository.write(optionProjection);
			if (updated) {
				skuOptionIsUpdated = true;
			}
		}

		if (skuOptionIsUpdated) {
			messagePublish(skuOption);
		}
	}

	private void messagePublish(final SkuOption skuOption) {
		final List<Long> uids = productDao.findUidsBySkuOption(skuOption);
		final Map<Long, String> map = productDao.findCodesByUids(uids);
		final List<String> productCodes = map.values()
				.stream()
				.sorted()
				.collect(Collectors.toList());

		Lists.partition(productCodes, bulkChangeMaxEventSize).stream()
				.map(codes -> eventMessageFactory.createEventMessage(OPTION_BULK_UPDATE, skuOption.getGuid(),
						Collections.singletonMap(PRODUCTS, codes)))
				.forEach(eventMessagePublisher::publish);
	}

	@Override
	public void processSkuOptionDeleted(final String guid) {
		LOGGER.debug("SkuOption deleted: " + guid);

		repository.delete(guid);
	}
}
