/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.batch.job;

import java.util.List;

import org.springframework.batch.item.ItemProcessor;

import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.service.catalog.CategoryLookup;

/**
 * Represents an implementation of {@link ItemProcessor} to create a list of Projections for Category entity.
 */
public class CategoryProcessor implements ItemProcessor<com.elasticpath.domain.catalog.Category, List<Category>> {

	private final ProjectionService<com.elasticpath.domain.catalog.Category, Category> projectionService;
	private final CategoryLookup categoryLookup;

	/**
	 * Constructor.
	 *
	 * @param projectionService Category projection service.
	 * @param categoryLookup    {@link CategoryLookup} data service.
	 */
	public CategoryProcessor(final ProjectionService<com.elasticpath.domain.catalog.Category, Category> projectionService,
							 final CategoryLookup categoryLookup) {
		this.projectionService = projectionService;
		this.categoryLookup = categoryLookup;
	}

	@Override
	public List<Category> process(final com.elasticpath.domain.catalog.Category entity) {
		final com.elasticpath.domain.catalog.Category category = categoryLookup.findByGuid(entity.getGuid());

		return projectionService.buildProjections(category, category.getCatalog());
	}

}
