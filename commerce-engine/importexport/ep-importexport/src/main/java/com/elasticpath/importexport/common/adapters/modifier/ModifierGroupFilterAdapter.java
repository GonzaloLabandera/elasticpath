/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.importexport.common.adapters.modifier;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.modifier.ModifierGroupFilter;
import com.elasticpath.importexport.common.adapters.AbstractDomainAdapterImpl;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupFilterDTO;

/**
 * The implementation of <code>DomainAdapter</code> interface.<br>
 * It is responsible for data transformation between <code>ModifierGroupFilter</code> and
 * <code>ModifierGroupFilterDTO</code> objects.
 */
public class ModifierGroupFilterAdapter extends AbstractDomainAdapterImpl<ModifierGroupFilter, ModifierGroupFilterDTO> {

	@Override
	public void populateDomain(final ModifierGroupFilterDTO dto, final ModifierGroupFilter modifierGroupFilter) {
		modifierGroupFilter.setModifierCode(dto.getModifierCode());
		modifierGroupFilter.setType(dto.getType());
		modifierGroupFilter.setReferenceGuid(dto.getReferenceGuid());

	}


	@Override
	public void populateDTO(final ModifierGroupFilter modifierGroupFilter, final ModifierGroupFilterDTO dto) {
		dto.setModifierCode(modifierGroupFilter.getModifierCode());
		dto.setReferenceGuid(modifierGroupFilter.getReferenceGuid());
		dto.setType(modifierGroupFilter.getType());
	}

	@Override
	public ModifierGroupFilter createDomainObject() {
		return getBeanFactory().getPrototypeBean(ContextIdNames.MODIFIER_GROUP_FILTER, ModifierGroupFilter.class);
	}

	@Override
	public ModifierGroupFilterDTO createDtoObject() {
		return new ModifierGroupFilterDTO();
	}
}
