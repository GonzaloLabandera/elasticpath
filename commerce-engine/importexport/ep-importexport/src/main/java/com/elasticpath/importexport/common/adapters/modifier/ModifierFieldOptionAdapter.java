/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.importexport.common.adapters.modifier;

import static com.elasticpath.importexport.common.comparators.ExportComparators.DISPLAY_VALUE_COMPARATOR;

import java.util.ArrayList;
import java.util.Collections;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierFieldOptionLdf;
import com.elasticpath.importexport.common.adapters.AbstractDomainAdapterImpl;
import com.elasticpath.importexport.common.dto.modifier.ModifierFieldOptionDTO;
import com.elasticpath.importexport.common.util.ImportExportUtil;

/**
 * The implementation of <code>DomainAdapter</code> interface.<br>
 * It is responsible for data transformation between <code>ModifierField</code> and
 * <code>ModifierFieldDTO</code> objects.
 */
public class ModifierFieldOptionAdapter extends AbstractDomainAdapterImpl<ModifierFieldOption, ModifierFieldOptionDTO> {

	@Override
	public void populateDomain(final ModifierFieldOptionDTO cartItemModifierFieldOptionDTO,
			final ModifierFieldOption cartItemModifierFieldOption) {
		cartItemModifierFieldOption.setValue(cartItemModifierFieldOptionDTO.getValue());
		cartItemModifierFieldOption.setOrdering(cartItemModifierFieldOptionDTO.getOrdering());

		populateModifierFieldOptionLdfDomain(cartItemModifierFieldOptionDTO, cartItemModifierFieldOption);

	}

	private void populateModifierFieldOptionLdfDomain(final ModifierFieldOptionDTO cartItemModifierFieldOptionDTO,
			final ModifierFieldOption cartItemModifierFieldOption) {

		if (cartItemModifierFieldOptionDTO.getValues() == null) {
			return;
		}

		for (DisplayValue displayValue : cartItemModifierFieldOptionDTO.getValues()) {

			ModifierFieldOptionLdf cartItemModifierFieldOptionLdf = cartItemModifierFieldOption.getModifierFieldOptionsLdfByLocale(
					displayValue.getLanguage());
			if (cartItemModifierFieldOptionLdf == null) {
				cartItemModifierFieldOptionLdf = getBeanFactory().getPrototypeBean(ContextIdNames.MODIFIER_OPTION_LDF,
						ModifierFieldOptionLdf.class);
				ImportExportUtil.getInstance().validateLocale(displayValue.getLanguage());
				cartItemModifierFieldOptionLdf.setLocale(displayValue.getLanguage());
				cartItemModifierFieldOptionLdf.setDisplayName(displayValue.getValue());
				cartItemModifierFieldOption.addModifierFieldOptionLdf(cartItemModifierFieldOptionLdf);
			} else {
				cartItemModifierFieldOptionLdf.setDisplayName(displayValue.getValue());
			}
		}
	}

	@Override
	public void populateDTO(final ModifierFieldOption cartItemModifierFieldOption, final ModifierFieldOptionDTO dto) {
		dto.setValue(cartItemModifierFieldOption.getValue());
		dto.setOrdering(cartItemModifierFieldOption.getOrdering());
		dto.setValues(new ArrayList<>());

		for (ModifierFieldOptionLdf cartItemModifierFieldOptionLdf : cartItemModifierFieldOption.getModifierFieldOptionsLdf()) {
			DisplayValue displayValue = new DisplayValue();
			displayValue.setValue(cartItemModifierFieldOptionLdf.getDisplayName());
			displayValue.setLanguage(cartItemModifierFieldOptionLdf.getLocale());
			dto.getValues().add(displayValue);
		}
		Collections.sort(dto.getValues(), DISPLAY_VALUE_COMPARATOR);
	}

}
