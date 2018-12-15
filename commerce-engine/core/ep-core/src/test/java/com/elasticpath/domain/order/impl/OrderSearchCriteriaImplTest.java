/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.order.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.order.OrderStatus;

/**
 * Test cases for <code>OrderSearchCriteriaImpl</code>.
 */
public class OrderSearchCriteriaImplTest {
	private static final OrderStatus ORDER_STATUS = OrderStatus.COMPLETED;

	private static final OrderShipmentStatus SHIPMENT_STATUS = OrderShipmentStatus.INVENTORY_ASSIGNED;

	private AdvancedOrderSearchCriteriaImpl orderSearchCriteriaImpl;

	@Before
	public void setUp() throws Exception {
		orderSearchCriteriaImpl = new AdvancedOrderSearchCriteriaImpl();
	}

	/**
	 * Test getter/setter's of a OrderSearchCriteria.
	 */
	@Test
	public void testGetSet() {
		assertThat(orderSearchCriteriaImpl.getCustomerCriteria()).isNull();
		final Map<String, String> customerCriteria = new HashMap<>();
		orderSearchCriteriaImpl.setCustomerCriteria(customerCriteria);
		assertThat(orderSearchCriteriaImpl.getCustomerCriteria()).isEqualTo(customerCriteria);

		assertThat(orderSearchCriteriaImpl.getOrderFromDate()).isNull();
		final Date testDate = new Date();
		orderSearchCriteriaImpl.setOrderFromDate(testDate);
		assertThat(orderSearchCriteriaImpl.getOrderFromDate()).isEqualTo(testDate);

		assertThat(orderSearchCriteriaImpl.getOrderToDate()).isNull();
		orderSearchCriteriaImpl.setOrderToDate(testDate);
		assertThat(orderSearchCriteriaImpl.getOrderToDate()).isEqualTo(testDate);

		assertThat(orderSearchCriteriaImpl.getOrderStatus()).isNull();
		orderSearchCriteriaImpl.setOrderStatus(ORDER_STATUS);
		assertThat(orderSearchCriteriaImpl.getOrderStatus()).isEqualTo(ORDER_STATUS);

		assertThat(orderSearchCriteriaImpl.getShipmentAddressCriteria()).isNull();
		final Map<String, String> shipmentAddressCriteria = new HashMap<>();
		orderSearchCriteriaImpl.setShipmentAddressCriteria(shipmentAddressCriteria);
		assertThat(orderSearchCriteriaImpl.getShipmentAddressCriteria()).isEqualTo(shipmentAddressCriteria);

		assertThat(orderSearchCriteriaImpl.getShipmentStatus()).isNull();
		orderSearchCriteriaImpl.setShipmentStatus(SHIPMENT_STATUS);
		assertThat(orderSearchCriteriaImpl.getShipmentStatus()).isEqualTo(SHIPMENT_STATUS);



	}



}

