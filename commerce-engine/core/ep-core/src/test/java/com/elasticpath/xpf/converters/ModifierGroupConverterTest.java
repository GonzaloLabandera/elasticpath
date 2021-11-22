package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.xpf.connectivity.entity.XPFModifierField;
import com.elasticpath.xpf.connectivity.entity.XPFModifierGroup;

@RunWith(MockitoJUnitRunner.class)
public class ModifierGroupConverterTest {

	@Mock
	private ModifierGroup modifierGroup;

	@Mock
	private ModifierField modifierField;

	@Mock
	private XPFModifierField xpfModifierField;

	@Mock
	private ModifierFieldConverter modifierFieldConverter;

	@InjectMocks
	private ModifierGroupConverter modifierGroupConverter;

	@Before
	public void setup() {
		when(modifierFieldConverter.convert(modifierField)).thenReturn(xpfModifierField);
	}

	@Test
	public void testConvertWithFullInputs() {
		when(modifierGroup.getModifierFields()).thenReturn(Collections.singleton(modifierField));

		XPFModifierGroup xpfModifierGroup = modifierGroupConverter.convert(modifierGroup);

		assertEquals(1, xpfModifierGroup.getModifierFields().size());
	}

	@Test
	public void testConvertWithMinInputs() {
		when(modifierGroup.getModifierFields()).thenReturn(null);

		XPFModifierGroup xpfModifierGroup = modifierGroupConverter.convert(modifierGroup);

		assertEquals(0, xpfModifierGroup.getModifierFields().size());
	}
}
