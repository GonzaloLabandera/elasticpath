/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.common.dto.sort;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortAttributeGroup;
import com.elasticpath.domain.search.SortAttributeType;
import com.elasticpath.domain.search.SortLocalizedName;
import com.elasticpath.domain.search.impl.SortAttributeImpl;
import com.elasticpath.domain.search.impl.SortLocalizedNameImpl;

/**
 * Test for {@link SortAttributeDtoAssembler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SortAttributeDtoAssemblerTest {

	private static final boolean DESCENDING = true;
	private static final boolean DEFAULT = true;
	private static final SortAttributeGroup SORT_ATTRIBUTE_GROUP = SortAttributeGroup.ATTRIBUTE_TYPE;
	private static final SortAttributeType SORT_ATTRIBUTE_TYPE = SortAttributeType.STRING_TYPE;
	private static final String SORT_ATTRIBUTE_GUID = "sortAttributeGuid";
	private static final String FR_LOCALE = "fr";
	private static final String BUSINESS_ID = "businessId";
	private static final String STORE_CODE = "storeCode";
	private static final String DISPLAY_NAME = "BrandFR";
	private final SortAttributeDtoAssembler sortAttributeDtoAssembler = new SortAttributeDtoAssembler();

	@Test
	public void verifySortAttributeDTOAssemblesCorrectlyFromASortAttribute() {
		SortAttribute sortAttribute = new SortAttributeImpl();
		final SortLocalizedName sortLocalizedName = new SortLocalizedNameImpl();
		sortLocalizedName.setName(DISPLAY_NAME);
		sortLocalizedName.setLocaleCode(FR_LOCALE);
		sortAttribute.setLocalizedNames(ImmutableMap.of(FR_LOCALE, sortLocalizedName));
		sortAttribute.setDescending(DESCENDING);
		sortAttribute.setSortAttributeGroup(SORT_ATTRIBUTE_GROUP);
		sortAttribute.setGuid(SORT_ATTRIBUTE_GUID);
		sortAttribute.setBusinessObjectId(BUSINESS_ID);
		sortAttribute.setStoreCode(STORE_CODE);
		sortAttribute.setDefaultAttribute(DEFAULT);
		sortAttribute.setSortAttributeType(SORT_ATTRIBUTE_TYPE);

		SortAttributeDTO sortAttributeDTO = new SortAttributeDTO();
		sortAttributeDtoAssembler.assembleDto(sortAttribute, sortAttributeDTO);
		assertThat(sortAttributeDTO.getSortAttributeGuid()).isEqualTo(SORT_ATTRIBUTE_GUID);
		assertThat(sortAttributeDTO.getBusinessObjectId()).isEqualTo(BUSINESS_ID);
		assertThat(sortAttributeDTO.getDisplayValues().get(0).getLanguage()).isEqualTo(FR_LOCALE);
		assertThat(sortAttributeDTO.getDisplayValues().get(0).getValue()).isEqualTo(DISPLAY_NAME);
		assertThat(sortAttributeDTO.getSortAttributeGroup()).isEqualTo(SORT_ATTRIBUTE_GROUP.getName());
		assertThat(sortAttributeDTO.getStoreCode()).isEqualTo(STORE_CODE);
		assertThat(sortAttributeDTO.getSortAttributeType()).isEqualTo(SORT_ATTRIBUTE_TYPE.getName());
		assertThat(sortAttributeDTO.isDefaultAttribute()).isEqualTo(DEFAULT);
	}

	@Test
	public void verifySortAttributeAssemblesCorrectlyFromSortAttributeDTO() {
		SortAttributeDTO sortAttributeDTO = new SortAttributeDTO();
		sortAttributeDTO.setSortAttributeGuid(SORT_ATTRIBUTE_GUID);
		sortAttributeDTO.setBusinessObjectId(BUSINESS_ID);
		sortAttributeDTO.setStoreCode(STORE_CODE);
		sortAttributeDTO.setSortAttributeGroup(SORT_ATTRIBUTE_GROUP.getName());
		sortAttributeDTO.setDescending(DESCENDING);
		sortAttributeDTO.setSortAttributeType(SORT_ATTRIBUTE_TYPE.getName());
		sortAttributeDTO.setDefaultAttribute(DEFAULT);
		final DisplayValue displayValue = new DisplayValue(FR_LOCALE, DISPLAY_NAME);
		sortAttributeDTO.setDisplayValues(Collections.singletonList(displayValue));
		SortAttribute sortAttribute = new SortAttributeImpl();
		sortAttributeDtoAssembler.assembleDomain(sortAttributeDTO, sortAttribute);
		assertThat(sortAttribute.getBusinessObjectId()).isEqualTo(BUSINESS_ID);
		assertThat(sortAttribute.getGuid()).isEqualTo(SORT_ATTRIBUTE_GUID);
		assertThat(sortAttribute.getLocalizedNames().get(FR_LOCALE).getLocaleCode()).isEqualTo(FR_LOCALE);
		assertThat(sortAttribute.getLocalizedNames().get(FR_LOCALE).getName()).isEqualTo(DISPLAY_NAME);
		assertThat(sortAttribute.getSortAttributeGroup()).isEqualTo(SORT_ATTRIBUTE_GROUP);
		assertThat(sortAttribute.getStoreCode()).isEqualTo(STORE_CODE);
		assertThat(sortAttribute.getSortAttributeType()).isEqualTo(SORT_ATTRIBUTE_TYPE);
		assertThat(sortAttribute.isDefaultAttribute()).isEqualTo(DEFAULT);
	}

}