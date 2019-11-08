/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.FieldMetadataWriterRepository;
import com.elasticpath.catalog.update.processor.capabilities.ModifierGroupUpdateProcessor;
import com.elasticpath.catalog.update.processor.connectivity.impl.exception.NoCapabilityMatchedException;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.modifier.ModifierGroup;

/**
 * Implementation of {@link ModifierGroupUpdateProcessor}.
 */
public class ModifierGroupUpdateProcessorImpl implements ModifierGroupUpdateProcessor {

	private static final Logger LOGGER = Logger.getLogger(ModifierGroupUpdateProcessorImpl.class);

	private final ProjectionService<ModifierGroup, FieldMetadata> projectionService;
	private final FieldMetadataWriterRepository repository;

	/**
	 * Constructor.
	 *
	 * @param projectionService {@link ProjectionService} for projections building.
	 * @param provider          {@link CatalogProjectionPluginProvider}.
	 */
	public ModifierGroupUpdateProcessorImpl(final ProjectionService<ModifierGroup, FieldMetadata> projectionService,
													final CatalogProjectionPluginProvider provider) {
		this.projectionService = projectionService;
		this.repository = provider.getCatalogProjectionPlugin()
				.getWriterCapability(FieldMetadataWriterRepository.class)
				.orElseThrow(NoCapabilityMatchedException::new);
	}

	@Override
	public void processModifierGroupCreated(final ModifierGroup modifierGroup) {
		LOGGER.debug("ModifierGroup created: " + modifierGroup.getGuid());

		final List<FieldMetadata> fieldMetadata = projectionService.buildAllStoresProjections(modifierGroup);
		fieldMetadata.forEach(repository::write);
	}

	@Override
	public void processModifierGroupUpdated(final ModifierGroup modifierGroup) {
		LOGGER.debug("ModifierGroup updated: " + modifierGroup.getGuid());

		final List<FieldMetadata> fieldMetadata = projectionService.buildAllStoresProjections(modifierGroup);
		fieldMetadata.forEach(repository::write);
	}

	@Override
	public void processModifierGroupDeleted(final String guid) {
		LOGGER.debug("ModifierGroup deleted: " + guid);

		repository.delete(guid);
	}
}
