package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.xpf.connectivity.entity.XPFModifierGroup;
import com.elasticpath.xpf.connectivity.entity.XPFProductType;

@RunWith(MockitoJUnitRunner.class)
public class ProductTypeConverterTest {

	@Mock
	private ProductType productType;

	@Mock
	private ModifierGroup modifierGroup;

	@Mock
	private XPFModifierGroup xpfModifierGroup;

	@Mock
	private ModifierGroupConverter modifierGroupConverter;

	@InjectMocks
	private ProductTypeConverter shippingOptionConverter;

	@Test
	public void testConvertWithFullInputs() {
		when(modifierGroupConverter.convert(modifierGroup)).thenReturn(xpfModifierGroup);
		when(productType.getModifierGroups()).thenReturn(Collections.singleton(modifierGroup));

		XPFProductType xpfProductType = shippingOptionConverter.convert(productType);

		assertEquals(1, xpfProductType.getModifierGroups().size());
	}

	@Test
	public void testConvertWithMinInputs() {
		when(productType.getModifierGroups()).thenReturn(Collections.emptySet());

		XPFProductType xpfProductType = shippingOptionConverter.convert(productType);

		assertEquals(0, xpfProductType.getModifierGroups().size());
	}
}
