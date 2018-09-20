/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.shipping.connectivity.service.selector.impl;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.shipping.connectivity.dto.PricedShippableItem;
import com.elasticpath.shipping.connectivity.dto.ShippingAddress;
import com.elasticpath.shipping.connectivity.spi.ShippingCalculationPlugin;

/**
 * Tests {@link StaticPricedShippingCalculationPluginSelectorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StaticPricedShippingCalculationPluginSelectorImplTest {

	private static final String STORE_CODE = "storeCode";

	@Mock
	private PricedShippableItem mockPricedShippableItem;

	@Mock
	private ShippingAddress mockAddress;

	@Mock
	private ShippingCalculationPlugin mockShippingCalculationPlugin;

	@InjectMocks
	private StaticPricedShippingCalculationPluginSelectorImpl target;

	@Before
	public void setUp() {
		target.setShippingCalculationPluginList(singletonList(mockShippingCalculationPlugin));
	}

	@Test
	public void testGetUnpricedShippingCalculationPlugin() {
		final ShippingCalculationPlugin provider = target
				.getPricedShippingCalculationPlugin(singletonList(mockPricedShippableItem), mockAddress, STORE_CODE);

		assertThat(provider).isEqualTo(mockShippingCalculationPlugin);
	}
}
