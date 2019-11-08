/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.importexport.common.adapters.modifier;

import static groovy.util.GroovyTestCase.assertEquals;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierFieldOptionLdf;
import com.elasticpath.domain.modifier.impl.ModifierFieldOptionImpl;
import com.elasticpath.domain.modifier.impl.ModifierFieldOptionLdfImpl;
import com.elasticpath.importexport.common.dto.modifier.ModifierFieldOptionDTO;

/**
 * Tests ModifierFieldOptionAdapter.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierFieldOptionAdapterTest {

	private static final String OPTION_VALUE = "OPTION_VALUE1";
	private static final int ORDERING = 10;
	private static final String DISPLAY_NAME_1 = "DISPLAY_NAME_1";

	@InjectMocks
	private final ModifierFieldOptionAdapter adapter = new ModifierFieldOptionAdapter();

	@Mock
	private BeanFactory beanFactory;

	@Before
	public void setUp() {
		given(beanFactory.getPrototypeBean(ContextIdNames.MODIFIER_OPTION_LDF, ModifierFieldOptionLdf.class))
				.willAnswer((Answer<ModifierFieldOptionLdfImpl>) invocationOnMock -> new ModifierFieldOptionLdfImpl());
	}

	@Test
	public void testPopulateDomainSimple() {
		ModifierFieldOption cartItemModifierFieldOption = new ModifierFieldOptionImpl();

		ModifierFieldOptionDTO dto = createDTO();
		dto.setOrdering(ORDERING);
		dto.setValue(OPTION_VALUE);

		adapter.populateDomain(dto, cartItemModifierFieldOption);

		assertEquals(dto.getValue(), cartItemModifierFieldOption.getValue());
		assertEquals(dto.getOrdering(), cartItemModifierFieldOption.getOrdering());
	}

	@Test
	public void testPopulateDomainWithLdf() {
		ModifierFieldOption cartItemModifierFieldOption = new ModifierFieldOptionImpl();

		ModifierFieldOptionDTO dto = createDTO();
		dto.setOrdering(ORDERING);
		dto.setValue(OPTION_VALUE);
		dto.setValues(new ArrayList<>());
		DisplayValue dv1 = new DisplayValue();
		dv1.setValue(DISPLAY_NAME_1);
		dv1.setLanguage(Locale.CANADA.toString());
		dto.getValues().add(dv1);

		adapter.populateDomain(dto, cartItemModifierFieldOption);

		assertEquals(1, cartItemModifierFieldOption.getModifierFieldOptionsLdf().size());
		ModifierFieldOptionLdf ldf1 = cartItemModifierFieldOption.getModifierFieldOptionsLdf().iterator().next();

		assertEquals(dv1.getValue(), ldf1.getDisplayName());
		assertEquals(dv1.getLanguage(), ldf1.getLocale());
	}

	@Test
	public void testPopulateDTOSimple() {
		ModifierFieldOption cartItemModifierFieldOption = createModifierFieldOption();

		ModifierFieldOptionDTO dto = createDTO();

		adapter.populateDTO(cartItemModifierFieldOption, dto);

		assertEquals(cartItemModifierFieldOption.getValue(), dto.getValue());
		assertEquals(cartItemModifierFieldOption.getOrdering(), dto.getOrdering());
	}

	@Test
	public void testPopulateDTOWithLdf() {
		ModifierFieldOption cartItemModifierFieldOption = createModifierFieldOption();
		ModifierFieldOptionLdf ldf1 = new ModifierFieldOptionLdfImpl();
		ldf1.setDisplayName(DISPLAY_NAME_1);
		ldf1.setLocale(Locale.CANADA.toString());
		cartItemModifierFieldOption.addModifierFieldOptionLdf(ldf1);

		ModifierFieldOptionDTO dto = createDTO();

		adapter.populateDTO(cartItemModifierFieldOption, dto);

		assertEquals(1, dto.getValues().size());
		DisplayValue dv1 = dto.getValues().get(0);

		assertEquals(ldf1.getDisplayName(), dv1.getValue());
		assertEquals(ldf1.getLocale(), dv1.getLanguage());
	}

	private ModifierFieldOption createModifierFieldOption() {
		ModifierFieldOption cartItemModifierFieldOption = new ModifierFieldOptionImpl();

		cartItemModifierFieldOption.setValue(OPTION_VALUE);
		cartItemModifierFieldOption.setOrdering(ORDERING);
		return cartItemModifierFieldOption;
	}

	private ModifierFieldOptionDTO createDTO() {
		ModifierFieldOptionDTO dto = new ModifierFieldOptionDTO();
		dto.setValues(new ArrayList<>());
		return dto;
	}

}
