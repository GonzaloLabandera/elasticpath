/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.batch.job;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * Expired projection writer. Does tombstone from expired projections.
 */
public class ExpiredProjectionWriter implements ItemWriter<Projection> {
	private final CatalogService catalogService;

	/**
	 * Constructor.
	 *
	 * @param catalogService catalog service.
	 */
	public ExpiredProjectionWriter(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@Override
	public void write(final List<? extends Projection> items) {
		catalogService.delete(items);
	}
}
