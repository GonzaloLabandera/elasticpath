/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.common.dto.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.impl.FacetImpl;

/**
 * Test for {@link FacetDtoAssembler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FacetDtoAssemblerTest {

	private static final int FIELD_KEY_TYPE = 0;
	private static final int FACET_GROUP = 1;
	private static final int FACET_TYPE = 2;

	private static final boolean SEARCHABLE_OPTION = true;

	private static final String FACET_GUID = "facetGuid";
	private static final String BUSINESS_ID = "businessId";
	private static final String FACET_NAME = "facetName";
	private static final String STORE_CODE = "storeCode";
	private static final String DISPLAY_NAME = "{\"en\":\"Brand\",\"fr\":\"BrandFR\"}";
	private static final String RANGE_FACET_VALUES = "[{\"start\":0,\"end\":100,\"displayNameMap\":{\"fr_CA\":\"0 - 100\",\"en\":\"0 - 100\"}},"
			+ "{\"start\":100,\"end\":200,\"displayNameMap\":{\"fr_CA\":\"100 - 200\",\"en\":\"100 - 200\"}}]";
	private static final String EN_LOCALE = "en";
	private static final String BRAND = "Brand";
	private static final String FR_LOCALE = "fr";
	private static final String BRAND_FR = "BrandFR";
	private static final String FR_CA = "fr_CA";
	private static final String ZERO_TO_ONE_HUNDRED = "0 - 100";
	private static final String ONE_HUNDRED_TO_TWO_HUNDRED = "100 - 200";

	private static final BigDecimal ONE_HUNDRED_BIG_DECIMAL = new BigDecimal("100");
	private static final BigDecimal TWO_HUNDRED_BIG_DECIMAL = new BigDecimal("200");

	private final FacetDtoAssembler facetDtoAssembler = new FacetDtoAssembler();

	@Test
	public void verifyFacetDTOAssemblesCorrectlyFromAFacet() throws Exception {
		Facet facet = new FacetImpl();
		facet.setFacetGuid(FACET_GUID);
		facet.setBusinessObjectId(BUSINESS_ID);
		facet.setFacetName(FACET_NAME);
		facet.setFieldKeyType(FIELD_KEY_TYPE);
		facet.setStoreCode(STORE_CODE);
		facet.setDisplayName(DISPLAY_NAME);
		facet.setFacetType(FACET_TYPE);
		facet.setSearchableOption(SEARCHABLE_OPTION);
		facet.setRangeFacetValues(RANGE_FACET_VALUES);
		facet.setFacetGroup(FACET_GROUP);

		FacetDTO facetDTO = new FacetDTO();
		facetDtoAssembler.assembleDto(facet, facetDTO);

		assertThat(facetDTO.getFacetGuid()).isEqualTo(FACET_GUID);
		assertThat(facetDTO.getBusinessObjectId()).isEqualTo(BUSINESS_ID);
		assertThat(facetDTO.getFacetName()).isEqualTo(FACET_NAME);
		assertThat(facetDTO.getFieldKeyType()).isEqualTo(FIELD_KEY_TYPE);
		assertThat(facetDTO.getStoreCode()).isEqualTo(STORE_CODE);

		List<DisplayValue> displayValues = facetDTO.getDisplayValues();
		verifyDisplayValue(EN_LOCALE, BRAND, displayValues.get(0));
		verifyDisplayValue(FR_LOCALE, BRAND_FR, displayValues.get(1));

		List<RangeFacetDTO> rangeFacetDTOs = facetDTO.getRangeFacetValues();
		verifyRangeFacetDTO(BigDecimal.ZERO, ONE_HUNDRED_BIG_DECIMAL, rangeFacetDTOs.get(0),
				ImmutableList.of(new DisplayValue(FR_CA, ZERO_TO_ONE_HUNDRED), new DisplayValue(EN_LOCALE, ZERO_TO_ONE_HUNDRED)));
		verifyRangeFacetDTO(ONE_HUNDRED_BIG_DECIMAL, TWO_HUNDRED_BIG_DECIMAL, rangeFacetDTOs.get(1),
				ImmutableList.of(new DisplayValue(FR_CA, ONE_HUNDRED_TO_TWO_HUNDRED), new DisplayValue(EN_LOCALE, ONE_HUNDRED_TO_TWO_HUNDRED)));

		assertThat(facetDTO.getFacetType()).isEqualTo(FACET_TYPE);
		assertThat(facetDTO.getSearchableOption()).isEqualTo(SEARCHABLE_OPTION);
		assertThat(facetDTO.getFacetGroup()).isEqualTo(FACET_GROUP);
	}

	private void verifyDisplayValue(final String language, final String value, final DisplayValue displayValue) {
		assertThat(displayValue.getLanguage()).isEqualTo(language);
		assertThat(displayValue.getValue()).isEqualTo(value);
	}

	private void verifyRangeFacetDTO(final BigDecimal start, final BigDecimal end, final RangeFacetDTO rangeFacetDTO,
									 final List<DisplayValue> expectedDisplayValues) {
		assertThat(rangeFacetDTO.getStart()).isEqualTo(start);
		assertThat(rangeFacetDTO.getEnd()).isEqualTo(end);
		List<DisplayValue> actualDisplayValues = rangeFacetDTO.getDisplayValues();
		for (int i = 0; i < actualDisplayValues.size(); i++) {
			DisplayValue actualDisplayValue = actualDisplayValues.get(i);
			DisplayValue expectedDisplayValue = expectedDisplayValues.get(i);

			assertThat(actualDisplayValue.getLanguage()).isEqualTo(expectedDisplayValue.getLanguage());
			assertThat(actualDisplayValue.getValue()).isEqualTo(expectedDisplayValue.getValue());
		}
	}

	@Test
	public void verifyFacetAssemblesCorrectlyFromFacetDTO() throws Exception {
		FacetDTO facetDTO = new FacetDTO();
		facetDTO.setFacetGuid(FACET_GUID);
		facetDTO.setBusinessObjectId(BUSINESS_ID);
		facetDTO.setFacetName(FACET_NAME);
		facetDTO.setFieldKeyType(FIELD_KEY_TYPE);
		facetDTO.setStoreCode(STORE_CODE);
		facetDTO.setDisplayValues(ImmutableList.of(new DisplayValue(EN_LOCALE, BRAND), new DisplayValue(FR_LOCALE, BRAND_FR)));

		facetDTO.setFacetType(FACET_TYPE);
		facetDTO.setSearchableOption(SEARCHABLE_OPTION);

		RangeFacetDTO rangeFacetZeroToOneHundred = new RangeFacetDTO();
		rangeFacetZeroToOneHundred.setStart(BigDecimal.ZERO);
		rangeFacetZeroToOneHundred.setEnd(ONE_HUNDRED_BIG_DECIMAL);
		rangeFacetZeroToOneHundred.setDisplayValues(
				ImmutableList.of(new DisplayValue(FR_CA, ZERO_TO_ONE_HUNDRED), new DisplayValue(EN_LOCALE, ZERO_TO_ONE_HUNDRED)));

		RangeFacetDTO rangeFacetOneHundredToTwoHundred = new RangeFacetDTO();
		rangeFacetOneHundredToTwoHundred.setStart(ONE_HUNDRED_BIG_DECIMAL);
		rangeFacetOneHundredToTwoHundred.setEnd(TWO_HUNDRED_BIG_DECIMAL);
		rangeFacetOneHundredToTwoHundred.setDisplayValues(
				ImmutableList.of(new DisplayValue(FR_CA, ONE_HUNDRED_TO_TWO_HUNDRED), new DisplayValue(EN_LOCALE, ONE_HUNDRED_TO_TWO_HUNDRED)));

		facetDTO.setRangeFacetValues(ImmutableList.of(rangeFacetZeroToOneHundred, rangeFacetOneHundredToTwoHundred));
		facetDTO.setFacetGroup(FACET_GROUP);

		Facet facet = new FacetImpl();

		facetDtoAssembler.assembleDomain(facetDTO, facet);

		assertThat(facet.getFacetGuid()).isEqualTo(FACET_GUID);
		assertThat(facet.getBusinessObjectId()).isEqualTo(BUSINESS_ID);
		assertThat(facet.getFacetName()).isEqualTo(FACET_NAME);
		assertThat(facet.getFieldKeyType()).isEqualTo(FIELD_KEY_TYPE);
		assertThat(facet.getStoreCode()).isEqualTo(STORE_CODE);
		assertThat(facet.getDisplayName()).isEqualTo(DISPLAY_NAME);
		assertThat(facet.getFacetType()).isEqualTo(FACET_TYPE);
		assertThat(facet.getSearchableOption()).isEqualTo(SEARCHABLE_OPTION);
		assertThat(facet.getRangeFacetValues()).isEqualTo(RANGE_FACET_VALUES);
		assertThat(facet.getFacetGroup()).isEqualTo(FACET_GROUP);
	}

}