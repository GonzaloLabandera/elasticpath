/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import com.elasticpath.common.dto.sort.SortAttributeDTO;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.importexport.importer.configuration.ImporterConfiguration;
import com.elasticpath.importexport.importer.importers.CollectionsStrategy;
import com.elasticpath.importexport.importer.types.CollectionStrategyType;
import com.elasticpath.importexport.importer.types.DependentElementType;
import com.elasticpath.service.search.SortAttributeService;

/**
 * Strategy to handle the collection of sort attribute.
 */
public class SortCollectionStrategy implements CollectionsStrategy<SortAttribute, SortAttributeDTO> {

	private final SortAttributeService sortAttributeService;
	private final boolean clearSortLocalizedNames;

	/**
	 * Constructor.
	 * @param importerConfiguration importer configuration
	 * @param sortAttributeService the sort attribute service.
	 */
	public SortCollectionStrategy(final ImporterConfiguration importerConfiguration, final SortAttributeService sortAttributeService) {
		this.sortAttributeService = sortAttributeService;

		clearSortLocalizedNames = importerConfiguration.getCollectionStrategyType(DependentElementType.SORT_LOCALIZED_NAMES).equals(
				CollectionStrategyType.CLEAR_COLLECTION);

	}

	@Override
	public void prepareCollections(final SortAttribute sortAttribute, final SortAttributeDTO sortAttributeDTO) {

		if (clearSortLocalizedNames) {
			sortAttributeService.removeAllLocalizedName(sortAttribute);
		}

	}

	@Override
	public boolean isForPersistentObjectsOnly() {
		return false;
	}
}
