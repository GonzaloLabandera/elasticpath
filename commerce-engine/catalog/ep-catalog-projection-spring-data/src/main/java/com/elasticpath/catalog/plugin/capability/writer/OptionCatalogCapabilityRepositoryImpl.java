/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.capability.writer;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OPTION_IDENTITY_TYPE;

import java.util.List;

import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.spi.capabilities.OptionWriterRepository;
import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * An implementation of {@link OptionWriterRepository}.
 */
public class OptionCatalogCapabilityRepositoryImpl implements OptionWriterRepository {

	private final CatalogService catalogService;

	/**
	 * Constructor.
	 *
	 * @param catalogService {@link CatalogService}.
	 */
	public OptionCatalogCapabilityRepositoryImpl(final CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@Override
	public boolean write(final Option option) {
		return catalogService.saveOrUpdate(option);
	}

	@Override
	public void delete(final String code) {
		catalogService.delete(OPTION_IDENTITY_TYPE, code);
	}

	@Override
	public void delete(final String code, final String store) {
		catalogService.delete(OPTION_IDENTITY_TYPE, store, code);
	}

	@Override
	public List<Option> writeAll(final List<Option> projections) {
		return catalogService.saveOrUpdateAll(projections);
	}

}
