/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.webservice.services.impl;

import static java.util.Comparator.comparingInt;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryReaderCapability;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.impl.ModifiedSinceImpl;
import com.elasticpath.catalog.reader.impl.PaginationRequestImpl;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.webservice.exception.NoReaderCapabilityMatchedException;
import com.elasticpath.catalog.webservice.services.CategoryService;
import com.elasticpath.service.misc.TimeService;

/**
 * An implementation of {@link CategoryService}.
 */
public class CategoryServiceImpl extends ReaderServiceImpl implements CategoryService {

	private final CategoryReaderCapability reader;

	/**
	 * Constructor.
	 *
	 * @param provider    is provider of plugin capabilities.
	 * @param timeService is time service.
	 */
	public CategoryServiceImpl(final CatalogProjectionPluginProvider provider, final TimeService timeService) {
		super(timeService);
		this.reader = provider.getCatalogProjectionPlugin()
				.getReaderCapability(CategoryReaderCapability.class)
				.orElseThrow(NoReaderCapabilityMatchedException::new);
	}

	@Override
	public Optional<Category> get(final String store, final String code) {
		return reader.get(store, code);
	}

	@Override
	public List<Category> getChildren(final String store, final String code) {
		final Optional<Category> optionalCategory = reader.get(store, code);

		if (optionalCategory.isPresent()) {
			final List<String> childrenCodes = optionalCategory.get().getChildren();
			return getLatestCategoriesWithCodes(store, childrenCodes).stream()
					.sorted(comparingInt(category -> childrenCodes.indexOf(category.getIdentity().getCode())))
					.collect(Collectors.toList());
		}

		return null;
	}

	@Override
	public FindAllResponse<Category> getAllCategories(final String store, final String limit, final String startAfter, final String modifiedSince,
													  final String modifiedSinceOffset) {
		validateLimit(limit);
		validateModifiedSince(modifiedSince, modifiedSinceOffset);

		return reader.findAll(store,
				new PaginationRequestImpl(limit, startAfter),
				new ModifiedSinceImpl(convertDate(modifiedSince), convertSinceOffset(modifiedSinceOffset)));
	}

	@Override
	public List<Category> getLatestCategoriesWithCodes(final String store, final List<String> codes) {
		return reader.findAllWithCodes(store, codes);
	}

}
