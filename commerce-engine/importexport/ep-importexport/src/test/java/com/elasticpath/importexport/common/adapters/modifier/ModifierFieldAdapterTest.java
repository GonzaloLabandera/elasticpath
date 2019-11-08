/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.importexport.common.adapters.modifier;

import static groovy.util.GroovyTestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldLdf;
import com.elasticpath.domain.modifier.ModifierType;
import com.elasticpath.domain.modifier.impl.ModifierFieldImpl;
import com.elasticpath.domain.modifier.impl.ModifierFieldLdfImpl;
import com.elasticpath.importexport.common.dto.modifier.ModifierFieldDTO;

/**
 * Tests ModifierFieldAdapter.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierFieldAdapterTest {

	private static final String CODE1 = "CODE1";
	private static final String DISPLAY_NAME_1 = "DISPLAY_NAME_1";
	private static final int ORDERING = 10;
	private static final int MAX_SIZE = 5;

	@Mock
	private BeanFactory beanFactory;

	@InjectMocks
	private final ModifierFieldAdapter adapter = new ModifierFieldAdapter();

	@Test
	public void testPopulateDomainSimple() {
		ModifierField cartItemModifierField = new ModifierFieldImpl();

		ModifierFieldDTO dto = createDTO();
		dto.setCode(CODE1);
		dto.setOrdering(ORDERING);
		dto.setType(ModifierType.SHORT_TEXT.getCamelName());
		dto.setRequired(true);
		dto.setMaxSize(MAX_SIZE);

		adapter.populateDomain(dto, cartItemModifierField);

		assertEquals(dto.getCode(), cartItemModifierField.getCode());
		assertEquals(dto.getOrdering(), cartItemModifierField.getOrdering());
		assertEquals(dto.getType(), cartItemModifierField.getFieldType().getCamelName());
		assertEquals(dto.isRequired(), cartItemModifierField.isRequired());
		assertEquals(dto.getMaxSize(), cartItemModifierField.getMaxSize());
	}

	@Test
	public void testPopulateDomainWithNullMaxSize() {
		ModifierField cartItemModifierField = new ModifierFieldImpl();

		ModifierFieldDTO dto = createDTO();
		dto.setCode(CODE1);
		dto.setOrdering(ORDERING);
		dto.setType(ModifierType.BOOLEAN.getCamelName());
		dto.setRequired(true);
		dto.setMaxSize(null);

		adapter.populateDomain(dto, cartItemModifierField);

		assertEquals(dto.getCode(), cartItemModifierField.getCode());
		assertEquals(dto.getOrdering(), cartItemModifierField.getOrdering());
		assertEquals(dto.getType(), cartItemModifierField.getFieldType().getCamelName());
		assertEquals(dto.isRequired(), cartItemModifierField.isRequired());
		assertNull(cartItemModifierField.getMaxSize());
	}

	@Test
	public void testPopulateDomainWithZeroMaxSize() {
		ModifierField cartItemModifierField = new ModifierFieldImpl();

		ModifierFieldDTO dto = createDTO();
		dto.setCode(CODE1);
		dto.setOrdering(ORDERING);
		dto.setType(ModifierType.BOOLEAN.getCamelName());
		dto.setRequired(true);
		dto.setMaxSize(0);

		adapter.populateDomain(dto, cartItemModifierField);

		assertEquals(dto.getCode(), cartItemModifierField.getCode());
		assertEquals(dto.getOrdering(), cartItemModifierField.getOrdering());
		assertEquals(dto.getType(), cartItemModifierField.getFieldType().getCamelName());
		assertEquals(dto.isRequired(), cartItemModifierField.isRequired());
		assertNull(cartItemModifierField.getMaxSize());
	}


	@Test
	public void testPopulateDomainWithLdf() {
		when(beanFactory.getPrototypeBean(ContextIdNames.MODIFIER_FIELD_LDF, ModifierFieldLdf.class))
				.thenAnswer(invocation -> new ModifierFieldLdfImpl());

		ModifierField cartItemModifierField = new ModifierFieldImpl();

		ModifierFieldDTO dto = createDTO();
		dto.setCode(CODE1);
		dto.setOrdering(ORDERING);
		dto.setType(ModifierType.SHORT_TEXT.getCamelName());
		dto.setRequired(true);
		dto.setMaxSize(MAX_SIZE);
		dto.setValues(new ArrayList<>());
		DisplayValue dv1 = new DisplayValue();
		dv1.setLanguage(Locale.CANADA.toString());
		dv1.setValue(DISPLAY_NAME_1);
		dto.getValues().add(dv1);

		adapter.populateDomain(dto, cartItemModifierField);

		assertEquals(1, cartItemModifierField.getModifierFieldsLdf().size());
		ModifierFieldLdf ldf1 = cartItemModifierField.getModifierFieldsLdf().iterator().next();

		assertEquals(dv1.getValue(), ldf1.getDisplayName());
		assertEquals(dv1.getLanguage(), ldf1.getLocale());
	}

	@Test
	public void testPopulateDTOSimple() {
		ModifierField cartItemModifierField = createModifierField();

		ModifierFieldDTO dto = createDTO();

		adapter.populateDTO(cartItemModifierField, dto);

		assertEquals(cartItemModifierField.getCode(), dto.getCode());
		assertEquals(cartItemModifierField.getOrdering(), dto.getOrdering());
		assertEquals(cartItemModifierField.getFieldType().getCamelName(), dto.getType());
		assertEquals(cartItemModifierField.isRequired(), dto.isRequired());
		assertEquals(cartItemModifierField.getMaxSize(), dto.getMaxSize());
	}

	@Test
	public void testPopulateDTOWithNullMaxSize() {
		ModifierField cartItemModifierField = createModifierField();
		cartItemModifierField.setMaxSize(null);

		ModifierFieldDTO dto = createDTO();

		adapter.populateDTO(cartItemModifierField, dto);

		assertEquals(cartItemModifierField.getCode(), dto.getCode());
		assertEquals(cartItemModifierField.getOrdering(), dto.getOrdering());
		assertEquals(cartItemModifierField.getFieldType().getCamelName(), dto.getType());
		assertEquals(cartItemModifierField.isRequired(), dto.isRequired());
		assertNull(dto.getMaxSize());
	}

	@Test
	public void testPopulateDTOWithLdf() {
		ModifierField cartItemModifierField = createModifierField();
		ModifierFieldLdf ldf1 = new ModifierFieldLdfImpl();
		ldf1.setLocale(Locale.CANADA.toString());
		ldf1.setDisplayName(DISPLAY_NAME_1);
		cartItemModifierField.addModifierFieldLdf(ldf1);

		ModifierFieldDTO dto = createDTO();

		adapter.populateDTO(cartItemModifierField, dto);

		assertEquals(1, dto.getValues().size());
		DisplayValue dv1 = dto.getValues().get(0);

		assertEquals(ldf1.getDisplayName(), dv1.getValue());
		assertEquals(ldf1.getLocale(), dv1.getLanguage());
	}

	private ModifierField createModifierField() {
		ModifierField cartItemModifierGroup = new ModifierFieldImpl();
		cartItemModifierGroup.setCode(CODE1);
		cartItemModifierGroup.setOrdering(ORDERING);
		cartItemModifierGroup.setFieldType(ModifierType.BOOLEAN);
		cartItemModifierGroup.setRequired(true);
		cartItemModifierGroup.setMaxSize(MAX_SIZE);

		return cartItemModifierGroup;
	}

	private ModifierFieldDTO createDTO() {
		ModifierFieldDTO dto = new ModifierFieldDTO();
		dto.setModifierFieldOptions(new ArrayList<>());
		return dto;
	}

}
