/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.services.impl;

import java.util.List;
import java.util.Optional;

import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.entity.option.OptionReaderCapability;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.impl.ModifiedSinceImpl;
import com.elasticpath.catalog.reader.impl.PaginationRequestImpl;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.webservice.exception.NoReaderCapabilityMatchedException;
import com.elasticpath.catalog.webservice.services.OptionService;
import com.elasticpath.service.misc.TimeService;

/**
 * An implementation of {@link OptionService}.
 */
public class OptionServiceImpl extends ReaderServiceImpl implements OptionService {

	private final OptionReaderCapability reader;

	/**
	 * Constructor.
	 *
	 * @param provider    is provider of plugin capabilities.
	 * @param timeService is time service.
	 */
	public OptionServiceImpl(final CatalogProjectionPluginProvider provider, final TimeService timeService) {
		super(timeService);
		this.reader = provider.getCatalogProjectionPlugin()
				.getReaderCapability(OptionReaderCapability.class)
				.orElseThrow(NoReaderCapabilityMatchedException::new);
	}

	@Override
	public Optional<Option> get(final String store, final String code) {
		return reader.get(store, code);
	}

	@Override
	public FindAllResponse<Option> getAllOptions(final String store, final String limit, final String startAfter, final String modifiedSince,
												 final String modifiedSinceOffset) {
		validateLimit(limit);
		validateModifiedSince(modifiedSince, modifiedSinceOffset);

		return reader.findAll(store,
				new PaginationRequestImpl(limit, startAfter),
				new ModifiedSinceImpl(convertDate(modifiedSince), convertSinceOffset(modifiedSinceOffset)));
	}

	@Override
	public List<Option> getLatestOptionsWithCodes(final String store, final List<String> codes) {
		return reader.findAllWithCodes(store, codes);
	}
}
