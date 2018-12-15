/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shipping.impl;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import com.elasticpath.service.shipping.PhysicalOrderShipmentShippingCostCalculationService;

/**
 * Tests for {@link PhysicalOrderShipmentShippingCostRefresherImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PhysicalOrderShipmentShippingCostRefresherImplTest {

	private static final BigDecimal SHIPPING_COST_AMOUNT = BigDecimal.ONE;

	@InjectMocks
	private PhysicalOrderShipmentShippingCostRefresherImpl target;

	@Mock
	private PhysicalOrderShipmentShippingCostCalculationService calculationService;

	@Mock
	private PhysicalOrderShipment physicalOrderShipment;

	private final Money shippingCostMoney = Money.valueOf(SHIPPING_COST_AMOUNT, Currency.getInstance("USD"));

	@Before
	public void setUp() {

		when(calculationService.calculateCost(physicalOrderShipment)).thenReturn(Optional.of(shippingCostMoney));

	}

	@Test
	public void testRefresh() {

		// given
		target.refresh(physicalOrderShipment);

		// verify
		verify(physicalOrderShipment, times(1)).setShippingCost(shippingCostMoney.getAmount());

	}

	@Test
	public void testRefreshWithNoValidShippingCost() {

		// given
		when(calculationService.calculateCost(physicalOrderShipment)).thenReturn(Optional.empty());

		// when
		target.refresh(physicalOrderShipment);

		// verify
		verify(physicalOrderShipment, times(1)).setShippingCost(null);

	}


}
