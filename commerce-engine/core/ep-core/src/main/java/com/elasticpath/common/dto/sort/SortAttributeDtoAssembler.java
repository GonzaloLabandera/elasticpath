/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.common.dto.sort;

import java.util.stream.Collectors;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.assembler.AbstractDtoAssembler;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortAttributeGroup;
import com.elasticpath.domain.search.SortAttributeType;
import com.elasticpath.domain.search.impl.SortLocalizedNameImpl;

/**
 * DTO Assembler for Sort Attributes.
 */
public class SortAttributeDtoAssembler extends AbstractDtoAssembler<SortAttributeDTO, SortAttribute> {

	private BeanFactory beanFactory;

	@Override
	public SortAttribute getDomainInstance() {
		return beanFactory.getPrototypeBean(ContextIdNames.SORT_ATTRIBUTE, SortAttribute.class);
	}

	@Override
	public SortAttributeDTO getDtoInstance() {
		return new SortAttributeDTO();
	}

	@Override
	public void assembleDto(final SortAttribute source, final SortAttributeDTO target) {
		target.setDisplayValues(source.getLocalizedNames().values().stream()
				.map(localizedName -> new DisplayValue(localizedName.getLocaleCode(), localizedName.getName())).collect(Collectors.toList()));
		target.setSortAttributeGuid(source.getGuid());
		target.setBusinessObjectId(source.getBusinessObjectId());
		target.setDescending(source.isDescending());
		target.setSortAttributeGroup(source.getSortAttributeGroup().getName());
		target.setStoreCode(source.getStoreCode());
		target.setDefaultAttribute(source.isDefaultAttribute());
		target.setSortAttributeType(source.getSortAttributeType().getName());
	}

	@Override
	public void assembleDomain(final SortAttributeDTO source, final SortAttribute target) {
		target.setLocalizedNames(source.getDisplayValues().stream().collect(Collectors.toMap(DisplayValue::getLanguage,  displayValue -> {
			final SortLocalizedNameImpl sortLocalizedName = new SortLocalizedNameImpl();
			sortLocalizedName.setName(displayValue.getValue());
			sortLocalizedName.setLocaleCode(displayValue.getLanguage());
			return sortLocalizedName;
		})));
		target.setGuid(source.getSortAttributeGuid());
		target.setBusinessObjectId(source.getBusinessObjectId());
		target.setDescending(source.isDescending());
		target.setSortAttributeGroup(SortAttributeGroup.valueOf(source.getSortAttributeGroup()));
		target.setStoreCode(source.getStoreCode());
		target.setSortAttributeType(SortAttributeType.valueOf(source.getSortAttributeType()));
		target.setDefaultAttribute(source.isDefaultAttribute());
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
