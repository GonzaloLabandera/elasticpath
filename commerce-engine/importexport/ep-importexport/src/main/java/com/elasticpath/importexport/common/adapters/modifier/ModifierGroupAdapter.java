/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.importexport.common.adapters.modifier;

import static com.elasticpath.importexport.common.comparators.ExportComparators.CART_ITEM_MODIFIER_FIELD_DTO;
import static com.elasticpath.importexport.common.comparators.ExportComparators.DISPLAY_VALUE_COMPARATOR;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierGroupLdf;
import com.elasticpath.importexport.common.adapters.AbstractDomainAdapterImpl;
import com.elasticpath.importexport.common.dto.modifier.ModifierFieldDTO;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupDTO;
import com.elasticpath.importexport.common.util.ImportExportUtil;
import com.elasticpath.service.modifier.ModifierService;

/**
 * The implementation of <code>DomainAdapter</code> interface.<br>
 * It is responsible for data transformation between <code>ModifierGroup</code> and
 * <code>ModifierGroupDTO</code> objects.
 */
public class ModifierGroupAdapter extends AbstractDomainAdapterImpl<ModifierGroup, ModifierGroupDTO> {

	private static final Logger LOG = LogManager.getLogger(ModifierGroupAdapter.class);

	private ModifierFieldAdapter modifierFieldAdapter;

	private ModifierService modifierService;

	@Override
	public void populateDomain(final ModifierGroupDTO dto, final ModifierGroup modifierGroup) {
		modifierGroup.setCode(dto.getCode());

		populateModifierGroupLdfDomain(dto, modifierGroup);

		populateModifierFieldDomain(dto, modifierGroup);
	}

	private void populateModifierGroupLdfDomain(final ModifierGroupDTO modifierGroupDTO, final ModifierGroup
			modifierGroup) {
		if (modifierGroupDTO.getValues() == null) {
			return;
		}

		for (DisplayValue displayValue : modifierGroupDTO.getValues()) {
			ModifierGroupLdf modifierGroupLdf = modifierGroup
					.getModifierGroupLdfByLocale(displayValue.getLanguage());

			if (modifierGroupLdf == null) {
				modifierGroupLdf = getBeanFactory()
						.getPrototypeBean(ContextIdNames.MODIFIER_GROUP_LDF, ModifierGroupLdf.class);
				ImportExportUtil.getInstance().validateLocale(displayValue.getLanguage());
				modifierGroupLdf.setLocale(displayValue.getLanguage());
				modifierGroupLdf.setDisplayName(displayValue.getValue());
				modifierGroup.addModifierGroupLdf(modifierGroupLdf);
			} else {
				modifierGroupLdf.setDisplayName(displayValue.getValue());
			}

			modifierGroupLdf.setDisplayName(displayValue.getValue());
		}
	}

	private void populateModifierFieldDomain(final ModifierGroupDTO dto, final ModifierGroup modifierGroup) {
		for (ModifierFieldDTO modifierFieldDTO : dto.getModifierFields()) {

			ModifierField modifierField = modifierGroup.getModifierFieldByCode(modifierFieldDTO.getCode());

			if (modifierField == null) {
				verifyModifierFieldDoesNotExist(dto.getCode(), modifierFieldDTO.getCode());
				LOG.info("Creating new modifierFieldDTO with code: " + modifierFieldDTO.getCode());
				modifierField = getBeanFactory().getPrototypeBean(ContextIdNames.MODIFIER_FIELD, ModifierField.class);
				modifierField.setCode(modifierFieldDTO.getCode());
			}

			modifierFieldAdapter.populateDomain(modifierFieldDTO, modifierField);
			modifierGroup.addModifierField(modifierField);
		}
	}

	private void verifyModifierFieldDoesNotExist(final String groupCode, final String modifierFieldCode) {
		ModifierField modifierFieldByCode = modifierService.findModifierFieldByCode(modifierFieldCode);
		if (modifierFieldByCode != null) {
			throw new IllegalArgumentException("A ModifierField cannot belong to more than one ModifierGroup ("
					+ groupCode + ", " + modifierFieldCode + ")");
		}
	}

	@Override
	public void populateDTO(final ModifierGroup modifierGroup, final ModifierGroupDTO dto) {
		dto.setCode(modifierGroup.getCode());
		dto.setValues(new ArrayList<>());
		dto.setModifierFields(new ArrayList<>());

		for (ModifierGroupLdf modifierGroupLdf : modifierGroup.getModifierGroupLdf()) {
			DisplayValue displayValue = new DisplayValue();
			displayValue.setValue(modifierGroupLdf.getDisplayName());
			displayValue.setLanguage(modifierGroupLdf.getLocale());
			dto.getValues().add(displayValue);
		}
		Collections.sort(dto.getValues(), DISPLAY_VALUE_COMPARATOR);

		for (ModifierField modifierField : modifierGroup.getModifierFields()) {
			ModifierFieldDTO modifierFieldDTO = new ModifierFieldDTO();
			dto.getModifierFields().add(modifierFieldDTO);
			modifierFieldAdapter.populateDTO(modifierField, modifierFieldDTO);
		}
		Collections.sort(dto.getModifierFields(), CART_ITEM_MODIFIER_FIELD_DTO);
	}

	@Override
	public ModifierGroup createDomainObject() {
		return getBeanFactory().getPrototypeBean(ContextIdNames.MODIFIER_GROUP, ModifierGroup.class);
	}

	@Override
	public ModifierGroupDTO createDtoObject() {
		return new ModifierGroupDTO();
	}

	public void setModifierFieldAdapter(final ModifierFieldAdapter modifierFieldAdapter) {
		this.modifierFieldAdapter = modifierFieldAdapter;
	}

	public void setModifierService(final ModifierService modifierService) {
		this.modifierService = modifierService;
	}
}
