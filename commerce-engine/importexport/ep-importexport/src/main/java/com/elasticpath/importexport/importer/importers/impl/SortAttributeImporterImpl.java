/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.importexport.importer.importers.impl;

import com.elasticpath.common.dto.sort.SortAttributeDTO;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.exporters.impl.SortCollectionStrategy;
import com.elasticpath.importexport.importer.importers.CollectionsStrategy;
import com.elasticpath.service.search.SortAttributeService;

/**
 * Imports {@link SortAttribute}.
 */
public class SortAttributeImporterImpl extends AbstractImporterImpl<SortAttribute, SortAttributeDTO> {

	private DomainAdapter<SortAttribute, SortAttributeDTO> sortAttributeAdapter;

	private SortAttributeService sortAttributeService;

	@Override
	protected String getDtoGuid(final SortAttributeDTO dto) {
		return dto.getSortAttributeGuid();
	}

	@Override
	protected DomainAdapter<SortAttribute, SortAttributeDTO> getDomainAdapter() {
		return sortAttributeAdapter;
	}

	@Override
	protected SortAttribute findPersistentObject(final SortAttributeDTO dto) {
		return sortAttributeService.findByGuid(dto.getSortAttributeGuid());
	}

	@Override
	protected void setImportStatus(final SortAttributeDTO object) {
		getStatusHolder().setImportStatus("(" + object.getSortAttributeGuid() + ")");
	}

	@Override
	public Class<? extends SortAttributeDTO> getDtoClass() {
		return SortAttributeDTO.class;
	}

	@Override
	public String getImportedObjectName() {
		return SortAttributeDTO.ROOT_ELEMENT;
	}

	public void setSortAttributeAdapter(final DomainAdapter<SortAttribute, SortAttributeDTO> sortAttributeAdapter) {
		this.sortAttributeAdapter = sortAttributeAdapter;
	}

	public void setSortAttributeService(final SortAttributeService sortAttributeService) {
		this.sortAttributeService = sortAttributeService;
	}

	@Override
	protected CollectionsStrategy<SortAttribute, SortAttributeDTO> getCollectionsStrategy() {
		return new SortCollectionStrategy(getContext().getImportConfiguration().getImporterConfiguration(JobType.SORTATTRIBUTE),
				sortAttributeService);
	}
}
