package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierType;
import com.elasticpath.xpf.connectivity.entity.XPFModifierField;
import com.elasticpath.xpf.connectivity.entity.XPFModifierFieldOption;

@RunWith(MockitoJUnitRunner.class)
public class ModifierFieldConverterTest {

	private static final String MODIFIER_FIELD_CODE = "MODIFIER_FIELD_CODE";
	private static final Integer MODIFIER_FIELD_MAX_SIZE = 10;

	@Mock
	private ModifierField modifierField;

	@Mock
	private ModifierFieldOption modifierFieldOption;

	@Mock
	private XPFModifierFieldOption xpfModifierFieldOption;

	@Mock
	private ModifierFieldOptionConverter modifierFieldOptionConverter;

	@InjectMocks
	private ModifierFieldConverter modifierFieldConverter;

	@Before
	public void setup() {
		when(modifierField.getModifierFieldOptions()).thenReturn(Collections.singleton(modifierFieldOption));
		when(modifierFieldOptionConverter.convert(modifierFieldOption)).thenReturn(xpfModifierFieldOption);
	}

	@Test
	public void testConvertWithFullInputs() {
		when(modifierField.getMaxSize()).thenReturn(MODIFIER_FIELD_MAX_SIZE);
		when(modifierField.getCode()).thenReturn(MODIFIER_FIELD_CODE);
		when(modifierField.getFieldType()).thenReturn(ModifierType.SHORT_TEXT);

		XPFModifierField xpfModifierField = modifierFieldConverter.convert(modifierField);

		assertEquals(MODIFIER_FIELD_CODE, xpfModifierField.getCode());
	}

	@Test
	public void testConvertWithMinInputs() {
		when(modifierField.getCode()).thenReturn(null);
		when(modifierField.getMaxSize()).thenReturn(null);
		when(modifierField.getFieldType()).thenReturn(ModifierType.SHORT_TEXT);

		XPFModifierField xpfModifierField = modifierFieldConverter.convert(modifierField);

		assertNull(MODIFIER_FIELD_CODE, xpfModifierField.getCode());
	}
}
