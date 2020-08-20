/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.impl.shipments;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.elasticpath.batch.jobs.AbstractBatchJob;
import com.elasticpath.domain.dtos.PhysicalOrderShipmentDTO;
import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.order.OrderStatus;

/**
 * The job will release all eligible shipments - those in INVENTORY_ASSIGNED state and belonging to IN_PROGRESS or PARTIALLY_SHIPPED orders.
 * Also, a shipment's creation date must be BEFORE the warehouse pick date (CURRENT_DATE - WAREHOUSE_PICK_DELAY_MINUTES)
 */
public class ReleasePhysicalShipmentsJob extends AbstractBatchJob<PhysicalOrderShipmentDTO> {

	private static final List<OrderStatus> ORDER_STATUSES = Arrays.asList(OrderStatus.IN_PROGRESS, OrderStatus.PARTIALLY_SHIPPED);
	private static final Object[] SHIPMENT_STATUSES = new Object[]{OrderShipmentStatus.INVENTORY_ASSIGNED};

	@Override
	@SuppressWarnings("unchecked")
	protected Collection<OrderStatus> getListParameterValues() {
		return ORDER_STATUSES;
	}

	@Override
	@SuppressWarnings("PMD.MethodReturnsInternalArray")
	protected Object[] getParameters() {
		return SHIPMENT_STATUSES;
	}

	@Override
	protected String getBatchJPQLQuery() {
		return "RELEASABLE_PHYSICAL_SHIPMENTS";
	}

	@Override
	protected String getJobName() {
		return "Release Physical Shipments";
	}
}
