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

import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.shoppingcart.CartType;
import com.elasticpath.xpf.connectivity.entity.XPFCartType;
import com.elasticpath.xpf.connectivity.entity.XPFModifierGroup;

@RunWith(MockitoJUnitRunner.class)
public class CartTypeConverterTest {

	private static final String CART_TYPE_NAME = "CART_TYPE_NAME";

	@Mock
	private CartType cartType;

	@Mock
	private ModifierGroup modifierGroup;

	@Mock
	private XPFModifierGroup xpfModifierGroup;

	@Mock
	private ModifierGroupConverter xpfModifierGroupConverter;

	@InjectMocks
	private CartTypeConverter cartTypeConverter;

	@Before
	public void setup() {
		when(xpfModifierGroupConverter.convert(modifierGroup)).thenReturn(xpfModifierGroup);
	}

	@Test
	public void testConvertWithFullInputs() {

		when(cartType.getName()).thenReturn(CART_TYPE_NAME);
		when(cartType.getModifiers()).thenReturn(Collections.singletonList(modifierGroup));

		XPFCartType xpfCartType = cartTypeConverter.convert(cartType);

		assertEquals(xpfCartType.getModifierGroups().size(), cartType.getModifiers().size());
		assertEquals(xpfCartType.getName(), cartType.getName());
	}

	@Test
	public void testConvertWithMinInputs() {
		when(cartType.getName()).thenReturn(null);
		when(cartType.getModifiers()).thenReturn(null);

		XPFCartType xpfCartType = cartTypeConverter.convert(cartType);

		assertNull(xpfCartType.getName());
		assertEquals(0, xpfCartType.getModifierGroups().size());
	}
}
