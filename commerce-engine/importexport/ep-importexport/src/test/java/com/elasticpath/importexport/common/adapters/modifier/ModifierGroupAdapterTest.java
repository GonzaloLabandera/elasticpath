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
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierGroupLdf;
import com.elasticpath.domain.modifier.impl.ModifierFieldImpl;
import com.elasticpath.domain.modifier.impl.ModifierGroupImpl;
import com.elasticpath.domain.modifier.impl.ModifierGroupLdfImpl;
import com.elasticpath.importexport.common.dto.modifier.ModifierFieldDTO;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupDTO;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Tests ModifierGroupAdapter.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierGroupAdapterTest {

	private static final String CODE1 = "CODE1";
	private static final String DISPLAY_NAME_1 = "DISPLAY_NAME_1";
	private static final String CART_ITEM_MODIFIER_CODE = "CART_ITEM_MODIFIER_CODE";

	@InjectMocks
	private final ModifierGroupAdapter adapter = new ModifierGroupAdapter();

	@Mock
	private BeanFactory beanFactory;
	@Mock
	private ModifierService cartItemModifierService;

	@Before
	public void setUp() {
		given(beanFactory.getPrototypeBean(ContextIdNames.MODIFIER_GROUP_LDF, ModifierGroupLdf.class))
				.willAnswer((Answer<ModifierGroupLdfImpl>) invocationOnMock -> new ModifierGroupLdfImpl());
		given(cartItemModifierService.findModifierFieldByCode(CART_ITEM_MODIFIER_CODE))
				.willAnswer((Answer<ModifierField>) invocationOnMock -> new ModifierFieldImpl());
	}

	@Test
	public void testPopulateDomainSimple() {
		ModifierGroup cartItemModifierGroup = new ModifierGroupImpl();

		ModifierGroupDTO dto = createDTO();
		dto.setCode(CODE1);

		adapter.populateDomain(dto, cartItemModifierGroup);

		assertEquals(cartItemModifierGroup.getCode(), dto.getCode());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPopulateDomainWithCartItemModifier() {
		ModifierGroup cartItemModifierGroup = new ModifierGroupImpl();

		ModifierGroupDTO dto = createDTO();
		dto.setCode(CODE1);
		dto.setModifierFields(new ArrayList<>());
		ModifierFieldDTO cartItemModifierFieldDTO = new ModifierFieldDTO();
		cartItemModifierFieldDTO.setCode(CART_ITEM_MODIFIER_CODE);
		dto.getModifierFields().add(cartItemModifierFieldDTO);

		adapter.populateDomain(dto, cartItemModifierGroup);
	}

	@Test
	public void testPopulateDomainWithLdf() {
		ModifierGroup cartItemModifierGroup = new ModifierGroupImpl();

		ModifierGroupDTO dto = createDTO();
		dto.setCode(CODE1);
		dto.setValues(new ArrayList<>());
		DisplayValue dv1 = new DisplayValue();
		dv1.setValue(DISPLAY_NAME_1);
		dv1.setLanguage(Locale.CANADA.toString());
		dto.getValues().add(dv1);

		adapter.populateDomain(dto, cartItemModifierGroup);

		assertEquals(1, cartItemModifierGroup.getModifierGroupLdf().size());
		ModifierGroupLdf ldf1 = cartItemModifierGroup.getModifierGroupLdf().iterator().next();

		assertEquals(cartItemModifierGroup.getCode(), dto.getCode());
		assertEquals(ldf1.getDisplayName(), dv1.getValue());
		assertEquals(ldf1.getLocale(), dv1.getLanguage());
	}

	@Test
	public void testPopulateDTOSimple() {
		ModifierGroup cartItemModifierGroup = createModifierGroup();

		ModifierGroupDTO dto = createDTO();

		adapter.populateDTO(cartItemModifierGroup, dto);

		assertEquals(cartItemModifierGroup.getCode(), dto.getCode());
	}

	@Test
	public void testPopulateDTOWithLdf() {
		ModifierGroup cartItemModifierGroup = createModifierGroup();

		ModifierGroupLdf ldf1 = new ModifierGroupLdfImpl();
		ldf1.setDisplayName(DISPLAY_NAME_1);
		ldf1.setLocale(Locale.CANADA.toString());
		cartItemModifierGroup.addModifierGroupLdf(ldf1);

		ModifierGroupDTO dto = createDTO();

		adapter.populateDTO(cartItemModifierGroup, dto);

		assertEquals(1, dto.getValues().size());
		DisplayValue displayValue = dto.getValues().get(0);

		assertEquals(ldf1.getDisplayName(), displayValue.getValue());
		assertEquals(ldf1.getLocale(), displayValue.getLanguage());
	}

	private ModifierGroup createModifierGroup() {
		ModifierGroupImpl cartItemModifierGroup = new ModifierGroupImpl();
		cartItemModifierGroup.setCode(CODE1);
		return cartItemModifierGroup;
	}

	private ModifierGroupDTO createDTO() {
		ModifierGroupDTO dto = new ModifierGroupDTO();
		dto.setModifierFields(new ArrayList<>());
		return dto;
	}

}
