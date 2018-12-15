/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shipping.impl;

import static java.util.Collections.singletonList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.money.Money;
import com.elasticpath.service.shipping.transformers.PricedShippableItemContainerFromOrderShipmentTransformer;
import com.elasticpath.shipping.connectivity.dto.PricedShippableItem;
import com.elasticpath.shipping.connectivity.dto.PricedShippableItemContainer;
import com.elasticpath.shipping.connectivity.dto.ShippingCalculationResult;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;
import com.elasticpath.shipping.connectivity.service.ShippingCalculationService;

/**
 * Tests {@link PhysicalOrderShipmentShippingCostCalculationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PhysicalOrderShipmentShippingCostCalculationServiceImplTest {

	private static final String SHIPPING_OPTION_CODE = "testShippingOptionCode";
	private static final String OTHER_SHIPPING_OPTION_CODE = "otherShippingOptionCode";

	@Mock
	private PricedShippableItemContainerFromOrderShipmentTransformer<PricedShippableItem> transformer;

	@Mock
	private ShippingCalculationService shippingCalculationService;

	@InjectMocks
	private PhysicalOrderShipmentShippingCostCalculationServiceImpl target;

	@Mock
	private PhysicalOrderShipment physicalOrderShipment;

	@Mock
	private PricedShippableItemContainer<PricedShippableItem> pricedShippableItemContainer;

	@Mock
	private ShippingCalculationResult shippingCalculationResult;

	@Mock
	private ShippingOption shippingOption;

	private final Money shippingCost = Money.valueOf(BigDecimal.ONE, Currency.getInstance("USD"));

	@Before
	public void setUp() {

		when(transformer.apply(physicalOrderShipment)).thenReturn(pricedShippableItemContainer);
		when(shippingCalculationService.getPricedShippingOptions(pricedShippableItemContainer)).thenReturn(shippingCalculationResult);
		when(shippingCalculationResult.isSuccessful()).thenReturn(true);
		when(shippingCalculationResult.getAvailableShippingOptions()).thenReturn(singletonList(shippingOption));
		when(shippingOption.getShippingCost()).thenReturn(Optional.of(shippingCost));
		when(shippingOption.getCode()).thenReturn(SHIPPING_OPTION_CODE);
		when(physicalOrderShipment.getShippingOptionCode()).thenReturn(SHIPPING_OPTION_CODE);
	}

	@Test
	public void testCalculateCost() {

		// when
		final Optional<Money> shippingCostOpt = target.calculateCost(physicalOrderShipment);

		// verify
		assertThat(shippingCostOpt.isPresent()).isTrue();

	}

	@Test
	public void testCalculateCostWithoutMatchedShippingOptionCode() {

		// given
		when(shippingOption.getCode()).thenReturn(OTHER_SHIPPING_OPTION_CODE);

		// when
		final Optional<Money> shippingCostOpt = target.calculateCost(physicalOrderShipment);

		// verify
		assertThat(shippingCostOpt.isPresent()).isFalse();

	}

	@Test
	public void testCalculateCostWithFailedShippingCalculationResult() {

		// given
		when(shippingCalculationResult.isSuccessful()).thenReturn(false);

		// when
		final Optional<Money> shippingCostOpt = target.calculateCost(physicalOrderShipment);

		// verify
		assertThat(shippingCostOpt.isPresent()).isFalse();

	}

}
