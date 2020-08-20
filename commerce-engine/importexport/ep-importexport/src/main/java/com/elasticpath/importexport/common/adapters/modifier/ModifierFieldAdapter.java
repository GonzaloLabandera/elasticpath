/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.importexport.common.adapters.modifier;

import static com.elasticpath.importexport.common.comparators.ExportComparators.CART_ITEM_MODIFIER_FIELD_OPTION_DTO;
import static com.elasticpath.importexport.common.comparators.ExportComparators.DISPLAY_VALUE_COMPARATOR;

import java.util.ArrayList;
import java.util.Collections;

import com.google.common.base.Preconditions;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldLdf;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierType;
import com.elasticpath.importexport.common.adapters.AbstractDomainAdapterImpl;
import com.elasticpath.importexport.common.dto.modifier.ModifierFieldDTO;
import com.elasticpath.importexport.common.dto.modifier.ModifierFieldOptionDTO;
import com.elasticpath.importexport.common.util.ImportExportUtil;

/**
 * The implementation of <code>DomainAdapter</code> interface.<br>
 * It is responsible for data transformation between <code>ModifierField</code> and
 * <code>ModifierFieldDTO</code> objects.
 */
public class ModifierFieldAdapter extends AbstractDomainAdapterImpl<ModifierField, ModifierFieldDTO> {

	private ModifierFieldOptionAdapter cartItemModifierFieldOptionAdapter;

	@Override
	public void populateDomain(final ModifierFieldDTO cartItemModifierFieldDTO, final ModifierField cartItemModifierField) {
		cartItemModifierField.setCode(cartItemModifierFieldDTO.getCode());
		cartItemModifierField.setOrdering(cartItemModifierFieldDTO.getOrdering());
		cartItemModifierField.setFieldType(ModifierType.valueOfCamelCase(cartItemModifierFieldDTO.getType()));
		cartItemModifierField.setRequired(cartItemModifierFieldDTO.isRequired());
		cartItemModifierField.setMaxSize(cartItemModifierFieldDTO.getMaxSize());
		cartItemModifierField.setDefaultCartValue(cartItemModifierFieldDTO.getDefaultCartValue());

		if (cartItemModifierFieldDTO.getMaxSize() != null && cartItemModifierFieldDTO.getMaxSize().intValue() == 0) {
			cartItemModifierField.setMaxSize(null);
		}

		Preconditions.checkNotNull(cartItemModifierField.getFieldType(), "Cannot accept ModifierField with null type");

		validateShortTextRules(cartItemModifierField);

		populateModifierFieldLdfDomain(cartItemModifierFieldDTO, cartItemModifierField);
		populateModifierFieldOptionDomain(cartItemModifierFieldDTO, cartItemModifierField);
	}

	private void validateShortTextRules(final ModifierField cartItemModifierField) {
		if (cartItemModifierField.getFieldType().getOrdinal() == ModifierType.SHORT_TEXT_ORDINAL
				&& cartItemModifierField.getMaxSize() == null) {
			throw new IllegalArgumentException("Cannot accept ModifierField with type SHORT_TEXT and null MaxSize");
		}

		if (cartItemModifierField.getFieldType().getOrdinal() != ModifierType.SHORT_TEXT_ORDINAL
				&& cartItemModifierField.getMaxSize() != null) {
			throw new IllegalArgumentException("Cannot accept ModifierField with type different than SHORT_TEXT and with a maxSize defined");
		}
	}

	private void populateModifierFieldLdfDomain(final ModifierFieldDTO cartItemModifierFieldDTO,
			final ModifierField cartItemModifierField) {
		if (cartItemModifierFieldDTO.getValues() == null) {
			return;
		}

		for (DisplayValue displayValue : cartItemModifierFieldDTO.getValues()) {
			ModifierFieldLdf cartItemModifierFieldLdf = cartItemModifierField
					.findModifierFieldLdfByLocale(displayValue.getLanguage());

			if (cartItemModifierFieldLdf == null) {
				cartItemModifierFieldLdf = getBeanFactory()
						.getPrototypeBean(ContextIdNames.MODIFIER_FIELD_LDF, ModifierFieldLdf.class);
				ImportExportUtil.getInstance().validateLocale(displayValue.getLanguage());
				cartItemModifierFieldLdf.setLocale(displayValue.getLanguage());
				cartItemModifierFieldLdf.setDisplayName(displayValue.getValue());
				cartItemModifierField.addModifierFieldLdf(cartItemModifierFieldLdf);
			} else {
				cartItemModifierFieldLdf.setDisplayName(displayValue.getValue());
			}
		}
	}

	private void populateModifierFieldOptionDomain(final ModifierFieldDTO cartItemModifierFieldDTO, final ModifierField
			cartItemModifierField) {

		if (cartItemModifierField.getFieldType().isPickType() && !doesModifierFieldDTOHasOptions(cartItemModifierFieldDTO)) {
			throw new IllegalArgumentException("When cartItemModifierField.type is either PICK_SINGLE_OPTION or PICK_MULTI_OPTION then you need to"
					+ " specify options");
		}

		if (!cartItemModifierField.getFieldType().isPickType() && doesModifierFieldDTOHasOptions(cartItemModifierFieldDTO)) {
			throw new IllegalArgumentException("When cartItemModifierField.type is neither PICK_SINGLE_OPTION or PICK_MULTI_OPTION then you cannot"
					+ " specify options");
		}

		if (!doesModifierFieldDTOHasOptions(cartItemModifierFieldDTO)) {
			return;
		}

		for (ModifierFieldOptionDTO cartItemModifierFieldOptionDTO : cartItemModifierFieldDTO.getModifierFieldOptions()) {

			ModifierFieldOption cartItemModifierFieldOption = cartItemModifierField.findModifierFieldOptionByValue(
					cartItemModifierFieldOptionDTO.getValue());

			if (cartItemModifierFieldOption == null) {
				cartItemModifierFieldOption = getBeanFactory().getPrototypeBean(ContextIdNames.MODIFIER_FIELD_OPTION,
						ModifierFieldOption.class);
				cartItemModifierFieldOptionAdapter.populateDomain(cartItemModifierFieldOptionDTO, cartItemModifierFieldOption);
				cartItemModifierField.addModifierFieldOption(cartItemModifierFieldOption);
			} else {
				cartItemModifierFieldOptionAdapter.populateDomain(cartItemModifierFieldOptionDTO, cartItemModifierFieldOption);
			}
		}
	}

	private boolean doesModifierFieldDTOHasOptions(final ModifierFieldDTO cartItemModifierFieldDTO) {
		return !(cartItemModifierFieldDTO.getModifierFieldOptions() == null
			|| cartItemModifierFieldDTO.getModifierFieldOptions().isEmpty());
	}

	@Override
	public void populateDTO(final ModifierField cartItemModifierField, final ModifierFieldDTO dto) {
		dto.setCode(cartItemModifierField.getCode());
		dto.setMaxSize(cartItemModifierField.getMaxSize());
		dto.setOrdering(cartItemModifierField.getOrdering());
		dto.setRequired(cartItemModifierField.isRequired());
		dto.setType(cartItemModifierField.getFieldType().getCamelName());
		dto.setValues(new ArrayList<>());
		dto.setModifierFieldOptions(new ArrayList<>());
		dto.setDefaultCartValue(cartItemModifierField.getDefaultCartValue());

		for (ModifierFieldLdf cartItemModifierFieldLdf : cartItemModifierField.getModifierFieldsLdf()) {
			DisplayValue displayValue = new DisplayValue();
			displayValue.setValue(cartItemModifierFieldLdf.getDisplayName());
			displayValue.setLanguage(cartItemModifierFieldLdf.getLocale());
			dto.getValues().add(displayValue);
		}
		Collections.sort(dto.getValues(), DISPLAY_VALUE_COMPARATOR);

		for (ModifierFieldOption cartItemModifierFieldOption : cartItemModifierField.getModifierFieldOptions()) {
			ModifierFieldOptionDTO cartItemModifierFieldOptionDTO = new ModifierFieldOptionDTO();
			dto.getModifierFieldOptions().add(cartItemModifierFieldOptionDTO);
			cartItemModifierFieldOptionAdapter.populateDTO(cartItemModifierFieldOption, cartItemModifierFieldOptionDTO);
		}
		Collections.sort(dto.getModifierFieldOptions(), CART_ITEM_MODIFIER_FIELD_OPTION_DTO);
	}

	public void setModifierFieldOptionAdapter(final ModifierFieldOptionAdapter cartItemModifierFieldOptionAdapter) {
		this.cartItemModifierFieldOptionAdapter = cartItemModifierFieldOptionAdapter;
	}
}
