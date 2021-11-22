/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.common.dto.search.FacetDTO;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.importexport.exporter.search.ImportExportSearcher;
import com.elasticpath.service.search.FacetService;

/**
 * Exporter for {@link Facet}.
 */
public class FacetExporter extends AbstractExporterImpl<Facet, FacetDTO, String> {

	private static final Logger LOG = LogManager.getLogger(FacetExporter.class);

	private List<String> facetGuids;

	private ImportExportSearcher importExportSearcher;

	private FacetService facetService;

	private DomainAdapter<Facet, FacetDTO> facetAdapter;

	@Override
	protected void initializeExporter(final ExportContext context) throws ConfigurationException {
		facetGuids = new ArrayList<>();
		facetGuids.addAll(facetService.findAllGuids());

		LOG.info("The list for " + facetGuids.size() + " facet(s) are retrieved from the database.");
	}

	@Override
	protected Class<? extends FacetDTO> getDtoClass() {
		return FacetDTO.class;
	}

	@Override
	protected List<String> getListExportableIDs() {
		if (getContext().getDependencyRegistry().supportsDependency(Facet.class)) {
			facetGuids.addAll(getContext().getDependencyRegistry().getDependentGuids(Facet.class));
		}

		return facetGuids;
	}

	@Override
	protected List<Facet> findByIDs(final List<String> subList) {
		return facetService.findByGuids(subList);
	}

	@Override
	protected DomainAdapter<Facet, FacetDTO> getDomainAdapter() {
		return facetAdapter;
	}


	public ImportExportSearcher getImportExportSearcher() {
		return importExportSearcher;
	}

	public void setImportExportSearcher(final ImportExportSearcher importExportSearcher) {
		this.importExportSearcher = importExportSearcher;
	}

	public FacetService getFacetService() {
		return facetService;
	}

	public void setFacetService(final FacetService facetService) {
		this.facetService = facetService;
	}

	public DomainAdapter<Facet, FacetDTO> getFacetAdapter() {
		return facetAdapter;
	}

	public void setFacetAdapter(final DomainAdapter<Facet, FacetDTO> facetAdapter) {
		this.facetAdapter = facetAdapter;
	}

	@Override
	public JobType getJobType() {
		return JobType.FACET;
	}

	@Override
	public Class<?>[] getDependentClasses() {
		return new Class<?>[] { Facet.class };
	}
}
