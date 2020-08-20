/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.elasticpath.common.dto.sort.SortAttributeDTO;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.importexport.exporter.search.ImportExportSearcher;
import com.elasticpath.service.search.SortAttributeService;

/**
 * Exporter for {@link SortAttribute}.
 */
public class SortAttributeExporter extends AbstractExporterImpl<SortAttribute, SortAttributeDTO, String> {

	private static final Logger LOG = Logger.getLogger(SortAttributeExporter.class);

	private List<String> sortAttributeGuids;

	private ImportExportSearcher importExportSearcher;

	private SortAttributeService sortAttributeService;

	private DomainAdapter<SortAttribute, SortAttributeDTO> sortAttributeAdapter;

	@Override
	protected void initializeExporter(final ExportContext context) throws ConfigurationException {
		sortAttributeGuids = new ArrayList<>();
		sortAttributeGuids.addAll(sortAttributeService.findAllGuids());

		LOG.info("The list for " + sortAttributeGuids.size() + " sortAttribute(s) are retrieved from the database.");
	}

	@Override
	protected Class<? extends SortAttributeDTO> getDtoClass() {
		return SortAttributeDTO.class;
	}

	@Override
	protected List<String> getListExportableIDs() {
		if (getContext().getDependencyRegistry().supportsDependency(SortAttribute.class)) {
			sortAttributeGuids.addAll(getContext().getDependencyRegistry().getDependentGuids(SortAttribute.class));
		}

		return sortAttributeGuids;
	}

	@Override
	protected List<SortAttribute> findByIDs(final List<String> subList) {
		return sortAttributeService.findByGuids(subList);
	}

	@Override
	protected DomainAdapter<SortAttribute, SortAttributeDTO> getDomainAdapter() {
		return sortAttributeAdapter;
	}


	public ImportExportSearcher getImportExportSearcher() {
		return importExportSearcher;
	}

	public void setImportExportSearcher(final ImportExportSearcher importExportSearcher) {
		this.importExportSearcher = importExportSearcher;
	}

	public SortAttributeService getSortAttributeService() {
		return sortAttributeService;
	}

	public void setSortAttributeService(final SortAttributeService sortAttributeService) {
		this.sortAttributeService = sortAttributeService;
	}

	public DomainAdapter<SortAttribute, SortAttributeDTO> getSortAttributeAdapter() {
		return sortAttributeAdapter;
	}

	public void setSortAttributeAdapter(final DomainAdapter<SortAttribute, SortAttributeDTO> sortAttributeAdapter) {
		this.sortAttributeAdapter = sortAttributeAdapter;
	}

	@Override
	public JobType getJobType() {
		return JobType.SORTATTRIBUTE;
	}

	@Override
	public Class<?>[] getDependentClasses() {
		return new Class<?>[]{SortAttribute.class};
	}
}
