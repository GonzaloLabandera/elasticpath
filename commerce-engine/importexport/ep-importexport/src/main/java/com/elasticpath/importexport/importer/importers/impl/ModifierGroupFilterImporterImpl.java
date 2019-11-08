/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.importexport.importer.importers.impl;

import com.elasticpath.domain.modifier.ModifierGroupFilter;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupFilterDTO;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Imports {@link ModifierGroupFilter}.
 */
public class ModifierGroupFilterImporterImpl extends AbstractImporterImpl<ModifierGroupFilter, ModifierGroupFilterDTO> {

	private DomainAdapter<ModifierGroupFilter, ModifierGroupFilterDTO> domainAdapter;

	private ModifierService modifierService;

	@Override
	protected String getDtoGuid(final ModifierGroupFilterDTO dto) {
		return getGuid(dto);
	}

	private String getGuid(final ModifierGroupFilterDTO dto) {
		return dto.getType()
				+ "-" + dto.getModifierCode() + "-" + dto.getReferenceGuid();
	}

	@Override
	protected DomainAdapter<ModifierGroupFilter, ModifierGroupFilterDTO> getDomainAdapter() {
		return domainAdapter;
	}

	@Override
	protected ModifierGroupFilter findPersistentObject(final ModifierGroupFilterDTO dto) {
		return modifierService.findModifierGroupFilter(dto.getReferenceGuid(), dto.getModifierCode(), dto.getType());
	}

	@Override
	protected void setImportStatus(final ModifierGroupFilterDTO object) {
		getStatusHolder().setImportStatus("(" + getGuid(object) + ")");
	}

	@Override
	public Class<? extends ModifierGroupFilterDTO> getDtoClass() {
		return ModifierGroupFilterDTO.class;
	}

	@Override
	public String getImportedObjectName() {
		return ModifierGroupFilterDTO.ROOT_ELEMENT;
	}

	public void setDomainAdapter(final DomainAdapter<ModifierGroupFilter, ModifierGroupFilterDTO> sortAttributeAdapter) {
		this.domainAdapter = sortAttributeAdapter;
	}

	public void setModifierService(final ModifierService modifierService) {
		this.modifierService = modifierService;
	}
}
