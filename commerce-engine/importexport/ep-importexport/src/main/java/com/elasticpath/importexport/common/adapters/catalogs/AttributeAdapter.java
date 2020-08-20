/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */

package com.elasticpath.importexport.common.adapters.catalogs;

import static com.elasticpath.importexport.common.comparators.ExportComparators.DISPLAY_VALUE_COMPARATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeMultiValueType;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.importexport.common.adapters.AbstractDomainAdapterImpl;
import com.elasticpath.importexport.common.dto.catalogs.AttributeDTO;
import com.elasticpath.importexport.common.dto.catalogs.AttributeMultiValueTypeType;
import com.elasticpath.importexport.common.dto.catalogs.AttributeTypeType;
import com.elasticpath.importexport.common.dto.catalogs.AttributeUsageType;
import com.elasticpath.importexport.common.exception.runtime.PopulationRollbackException;

/**
 * The implementation of <code>DomainAdapter</code> interface.<br>
 * It is responsible for data transformation between <code>Attribute</code> and
 * <code>AttributeDTO</code> objects.
 */
public class AttributeAdapter extends AbstractDomainAdapterImpl<Attribute, AttributeDTO> {

	@Override
	public void populateDTO(final Attribute attribute, final AttributeDTO attributeDTO) {
		attributeDTO.setKey(attribute.getKey());
		final List<DisplayValue> nameValues = new ArrayList<>();
		Collection<Locale> supportedLocales =
				attribute.getCatalog() == null ? Arrays.asList(Locale.getAvailableLocales()) : attribute.getCatalog().getSupportedLocales();
		for (Locale locale : supportedLocales) {
			String displayName = attribute.getDisplayName(locale);
			if (!displayName.isEmpty()) {
				nameValues.add(new DisplayValue(locale.toString(), displayName));
			}
		}
		Collections.sort(nameValues, DISPLAY_VALUE_COMPARATOR);
		attributeDTO.setNameValues(nameValues);

		attributeDTO.setUsage(AttributeUsageType.valueOf(attribute.getAttributeUsage()));
		attributeDTO.setType(AttributeTypeType.valueOf(attribute.getAttributeType()));
		attributeDTO.setMultiLanguage(attribute.isLocaleDependant());
		attributeDTO.setRequired(attribute.isRequired());

		attributeDTO.setMultivalue(AttributeMultiValueTypeType.valueOf(attribute.getMultiValueType()));

		attributeDTO.setGlobal(attribute.isGlobal());
	}

	@Override
	public void populateDomain(final AttributeDTO attributeDTO, final Attribute attribute) {
		checkAttributeTypeForMultilanguage(attributeDTO.getKey(), attributeDTO.getType(), attributeDTO.getMultiLanguage());
		checkAttributeTypeForMultivalue(attributeDTO.getKey(), attributeDTO.getType(), attributeDTO.getMultivalue());

		attribute.setKey(attributeDTO.getKey());
		populateDomainNamesValues(attributeDTO, attribute);

		attribute.setAttributeUsage(attributeDTO.getUsage().usage());
		attribute.setAttributeType(attributeDTO.getType().type());
		attribute.setLocaleDependant(attributeDTO.getMultiLanguage());
		attribute.setRequired(attributeDTO.getRequired());

		attribute.setMultiValueType(attributeDTO.getMultivalue().type());

		attribute.setGlobal(attributeDTO.getGlobal());
	}

	private void checkAttributeTypeForMultivalue(final String key, final AttributeTypeType type,
			final AttributeMultiValueTypeType multivalue) {
		if (!AttributeMultiValueType.SINGLE_VALUE.equals(multivalue.type())
				&& type != AttributeTypeType.ShortText) {
			throw new PopulationRollbackException("IE-10002", key, type.toString());
		}

	}

	private void checkAttributeTypeForMultilanguage(final String key, final AttributeTypeType type, final boolean multiLanguageEnabled) {
		if (multiLanguageEnabled) {
			switch (type) {
				case Image:
					break;
				case ShortText:
					break;
				case LongText:
					break;
				case File:
					break;
				default:
					throw new PopulationRollbackException("IE-10003", key, type.toString());
			}
		}
	}

	private void populateDomainNamesValues(final AttributeDTO attributeDTO, final Attribute attribute) {
		for (DisplayValue displayValue : attributeDTO.getNameValues()) {
			if (!"".equals(displayValue.getValue())) {
				try {
					Locale locale = LocaleUtils.toLocale(displayValue.getLanguage());
					if (!LocaleUtils.isAvailableLocale(locale)) {
						throw new PopulationRollbackException("IE-10011", displayValue.getLanguage());
					}
					checkLocaleSupportedByCatalog(locale, attribute);
					attribute.setDisplayName(displayValue.getValue(), locale);
				} catch (IllegalArgumentException exception) {
					throw new PopulationRollbackException("IE-10011", exception, displayValue.getLanguage());
				}
			}
		}
	}

	private void checkLocaleSupportedByCatalog(final Locale locale, final Attribute attribute) {
		Catalog catalog = attribute.getCatalog();
		if (catalog != null && !catalog.getSupportedLocales().contains(locale)) {
			throw new PopulationRollbackException("IE-10000", locale.toString(), catalog.getCode());
		}
	}

	@Override
	public Attribute createDomainObject() {
		return getBeanFactory().getPrototypeBean(ContextIdNames.ATTRIBUTE, Attribute.class);
	}

	@Override
	public AttributeDTO createDtoObject() {
		return new AttributeDTO();
	}
}
