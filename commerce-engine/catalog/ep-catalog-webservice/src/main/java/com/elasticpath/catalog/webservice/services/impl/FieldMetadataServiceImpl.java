/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.services.impl;

import java.util.List;
import java.util.Optional;

import com.elasticpath.catalog.entity.fieldmetadata.FieldMetaDataReaderCapability;
import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.impl.ModifiedSinceImpl;
import com.elasticpath.catalog.reader.impl.PaginationRequestImpl;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.webservice.exception.NoReaderCapabilityMatchedException;
import com.elasticpath.catalog.webservice.services.FieldMetadataService;
import com.elasticpath.service.misc.TimeService;

/**
 * An implementation of {@link FieldMetadata}.
 */
public class FieldMetadataServiceImpl extends ReaderServiceImpl implements FieldMetadataService {
	private final FieldMetaDataReaderCapability reader;

	/**
	 * Constructor.
	 *
	 * @param provider is provider of plugin capabilities.
	 * @param timeService the time service.
	 */
	public FieldMetadataServiceImpl(final CatalogProjectionPluginProvider provider, final TimeService timeService) {
		super(timeService);
		this.reader = provider.getCatalogProjectionPlugin()
				.getReaderCapability(FieldMetaDataReaderCapability.class)
				.orElseThrow(NoReaderCapabilityMatchedException::new);
	}

	@Override
	public Optional<FieldMetadata> get(final String store, final String code) {
		return reader.get(store, code);
	}

	@Override
	public FindAllResponse<FieldMetadata> getAllFieldMetadata(final String store, final String limit, final String startAfter,
															  final String modifiedSince,
															  final String modifiedSinceOffset) {
		validateLimit(limit);
		validateModifiedSince(modifiedSince, modifiedSinceOffset);

		return reader.findAll(store,
				new PaginationRequestImpl(limit, startAfter),
				new ModifiedSinceImpl(convertDate(modifiedSince), convertSinceOffset(modifiedSinceOffset)));
	}

	@Override
	public List<FieldMetadata> getLatestFieldMetadataWithCodes(final String store, final List<String> codes) {
		return reader.findAllWithCodes(store, codes);
	}

}
