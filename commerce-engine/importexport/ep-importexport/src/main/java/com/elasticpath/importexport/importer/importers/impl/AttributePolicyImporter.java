/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import com.elasticpath.common.dto.customer.AttributePolicyDTO;
import com.elasticpath.domain.customer.AttributePolicy;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.service.customer.AttributePolicyService;

/**
 * An importer for {@link AttributePolicy}s.
 */
public class AttributePolicyImporter extends AbstractImporterImpl<AttributePolicy, AttributePolicyDTO> {

	private DomainAdapter<AttributePolicy, AttributePolicyDTO> domainAdapter;

	private AttributePolicyService attributePolicyService;

	@Override
	public String getImportedObjectName() {
		return AttributePolicyDTO.ROOT_ELEMENT;
	}

	@Override
	protected String getDtoGuid(final AttributePolicyDTO dto) {
		return dto.getGuid();
	}

	@Override
	protected DomainAdapter<AttributePolicy, AttributePolicyDTO> getDomainAdapter() {
		return domainAdapter;
	}

	@Override
	protected AttributePolicy findPersistentObject(final AttributePolicyDTO dto) {
		return attributePolicyService.findByGuid(dto.getGuid()).orElse(null);
	}

	@Override
	protected void setImportStatus(final AttributePolicyDTO object) {
		getStatusHolder().setImportStatus(object.getGuid());
	}

	@Override
	public Class<? extends AttributePolicyDTO> getDtoClass() {
		return AttributePolicyDTO.class;
	}

	public void setDomainAdapter(
			final DomainAdapter<AttributePolicy, AttributePolicyDTO> domainAdapter) {
		this.domainAdapter = domainAdapter;
	}

	public void setAttributePolicyService(final AttributePolicyService attributePolicyService) {
		this.attributePolicyService = attributePolicyService;
	}
}
