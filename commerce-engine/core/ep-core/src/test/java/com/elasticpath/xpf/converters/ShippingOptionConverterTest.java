package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.shipping.connectivity.dto.ShippingOption;
import com.elasticpath.xpf.connectivity.entity.XPFShippingOption;

@RunWith(MockitoJUnitRunner.class)
public class ShippingOptionConverterTest {

	private static final String SHIPPING_OPTION_CODE = "SHIPPING_OPTION_CODE";
	private static final String SHIPPING_OPTION_CARRIER_CODE = "SHIPPING_OPTION_CARRIER_CODE";

	private final ShippingOptionConverter shippingOptionConverter = new ShippingOptionConverter();

	@Mock
	private ShippingOption shippingOption;

	@Test
	public void testConvertWithFullInputs() {
		when(shippingOption.getCode()).thenReturn(SHIPPING_OPTION_CODE);
		when(shippingOption.getCarrierCode()).thenReturn(Optional.of(SHIPPING_OPTION_CARRIER_CODE));

		XPFShippingOption xpfShippingOption = shippingOptionConverter.convert(shippingOption);

		assertEquals(SHIPPING_OPTION_CODE, xpfShippingOption.getCode());
		assertEquals(SHIPPING_OPTION_CARRIER_CODE, xpfShippingOption.getCarrierCode());
	}
}
