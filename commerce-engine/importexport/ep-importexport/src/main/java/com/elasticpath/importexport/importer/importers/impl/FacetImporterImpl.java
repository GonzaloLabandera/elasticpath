/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.importexport.importer.importers.impl;

import com.elasticpath.common.dto.search.FacetDTO;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.service.search.FacetService;

/**
 * DTO for {@link Facet}.
 */
public class FacetImporterImpl extends AbstractImporterImpl<Facet, FacetDTO> {

	private DomainAdapter<Facet, FacetDTO> facetAdapter;

	private FacetService facetService;

	@Override
	protected String getDtoGuid(final FacetDTO dto) {
		return dto.getFacetGuid();
	}

	@Override
	protected DomainAdapter<Facet, FacetDTO> getDomainAdapter() {
		return facetAdapter;
	}

	@Override
	protected Facet findPersistentObject(final FacetDTO dto) {
		return facetService.findByGuid(dto.getFacetGuid());
	}

	@Override
	protected void setImportStatus(final FacetDTO object) {
		getStatusHolder().setImportStatus("(" + object.getFacetGuid() + ")");
	}

	@Override
	public Class<? extends FacetDTO> getDtoClass() {
		return FacetDTO.class;
	}

	@Override
	public String getImportedObjectName() {
		return FacetDTO.ROOT_ELEMENT;
	}

	public void setFacetAdapter(final DomainAdapter<Facet, FacetDTO> facetAdapter) {
		this.facetAdapter = facetAdapter;
	}

	public void setFacetService(final FacetService facetService) {
		this.facetService = facetService;
	}
}
