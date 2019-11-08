/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.batch.job;

import java.util.List;

import org.springframework.batch.item.ItemProcessor;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;

/**
 * Represents an implementation of {@link ItemProcessor} to create a list of Projections for domain entity.
 *
 * @param <S> type of domain entity.
 * @param <T> type of projection.
 */
public class DomainEntityProcessor<S, T extends Projection> implements ItemProcessor<S, List<T>> {

	private final ProjectionService<S, T> projectionService;

	/**
	 * Constructor.
	 *
	 * @param projectionService Catalog projection service.
	 */
	public DomainEntityProcessor(final ProjectionService<S, T> projectionService) {
		this.projectionService = projectionService;
	}

	@Override
	public List<T> process(final S entity) {
		return projectionService.buildAllStoresProjections(entity);
	}

}
