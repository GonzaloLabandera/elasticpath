/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.batch.job;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.batch.item.ItemWriter;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * Projection writer. Does a projection bulk write.
 */
public class ProjectionWriter implements ItemWriter<List<Projection>> {

	private final CatalogService catalogService;

	/**
	 * Constructor.
	 *
	 * @param catalogService catalog service.
	 */
	public ProjectionWriter(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@Override
	public void write(final List<? extends List<Projection>> projectionsLists) {
		final List<Projection> projections =
				projectionsLists.stream().flatMap((Function<List<Projection>, Stream<? extends Projection>>) Collection::stream)
						.collect(Collectors.toList());

		catalogService.saveOrUpdateAll(projections);
	}

}
