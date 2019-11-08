/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.importexport.importer.importers.impl;

import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupDTO;
import com.elasticpath.service.modifier.ModifierService;

/**
 * DTO for {@link ModifierGroup}.
 */
public class ModifierGroupImporterImpl extends AbstractImporterImpl<ModifierGroup, ModifierGroupDTO> {

	private DomainAdapter<ModifierGroup, ModifierGroupDTO> modifierGroupAdapter;

	private ModifierService modifierService;

	@Override
	protected String getDtoGuid(final ModifierGroupDTO dto) {
		return dto.getCode();
	}

	@Override
	protected DomainAdapter<ModifierGroup, ModifierGroupDTO> getDomainAdapter() {
		return modifierGroupAdapter;
	}

	@Override
	protected ModifierGroup findPersistentObject(final ModifierGroupDTO dto) {
		return modifierService.findModifierGroupByCode(dto.getCode());
	}

	@Override
	protected void setImportStatus(final ModifierGroupDTO object) {
		getStatusHolder().setImportStatus("(" + object.getCode() + ")");
	}

	@Override
	public Class<? extends ModifierGroupDTO> getDtoClass() {
		return ModifierGroupDTO.class;
	}

	@Override
	public String getImportedObjectName() {
		return ModifierGroupDTO.ROOT_ELEMENT;
	}

	public void setModifierGroupAdapter(final DomainAdapter<ModifierGroup, ModifierGroupDTO> modifierGroupAdapter) {
		this.modifierGroupAdapter = modifierGroupAdapter;
	}

	public void setModifierService(final ModifierService modifierService) {
		this.modifierService = modifierService;
	}
}
