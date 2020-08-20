/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.impl.shipments;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.elasticpath.batch.jobs.AbstractBatchProcessor;
import com.elasticpath.domain.DatabaseLastModifiedDate;
import com.elasticpath.domain.dtos.PhysicalOrderShipmentDTO;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.event.OrderEventHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderEvent;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.store.StoreService;

/**
 * A single transactional unit for processing a batch of shipments.
 */
public class ReleaseShipmentsBatchProcessor extends AbstractBatchProcessor<PhysicalOrderShipmentDTO> {
	private TimeService timeService;
	private StoreService storeService;
	private EventOriginatorHelper eventOriginatorHelper;
	private OrderEventHelper orderEventHelper;

	@Override
	protected int preProcessBatch(final List<PhysicalOrderShipmentDTO> batch) {
		int numOfSkippedRecords = 0;

		for (Iterator<PhysicalOrderShipmentDTO> dtoIterator = batch.iterator(); dtoIterator.hasNext();) {
			final OrderShipment shipment = dtoIterator.next().getShipment();

			Date warehousePickDelayTimestamp = storeService.calculateCurrentPickDelayTimestamp(shipment.getOrder().getStoreCode());

			if (shipment.getCreatedDate().before(warehousePickDelayTimestamp)) {
				shipment.getOrder().setModifiedBy(eventOriginatorHelper.getSystemOriginator());
			} else {
				numOfSkippedRecords++;
				dtoIterator.remove();
			}
		}

		return numOfSkippedRecords;
	}

	@Override
	protected void executeBulkOperations(final List<PhysicalOrderShipmentDTO> batch) {
		if (batch.isEmpty()) {
			return;
		}

		List<Long> shipmentUidPKs = new ArrayList<>();
		List<Long> orderUidPks = new ArrayList<>();

		batch.forEach(shipmentDTO -> {
			OrderShipment shipment = shipmentDTO.getShipment();
			shipmentUidPKs.add(shipment.getUidPk());

			Order order = shipment.getOrder();
			orderUidPks.add(order.getUidPk());

			OrderEvent shipmentReleasedOrderEvent = orderEventHelper.createShipmentReleasedOrderEvent(order, shipment);
			shipmentReleasedOrderEvent.setOrderUidPk(order.getUidPk());
			((DatabaseLastModifiedDate) shipmentReleasedOrderEvent).setLastModifiedDate(timeService.getCurrentTime());

			//insert statements will be batched by OpenJPA
			getPersistenceEngine().save(shipmentReleasedOrderEvent);
		});

		final Date dbCurrentTime = timeService.getCurrentTime();

		getPersistenceEngine().executeNamedQueryWithList("UPDATE_SHIPMENTS_WITH_RELEASED_STATUS", LIST_PARAMETER_NAME, shipmentUidPKs, dbCurrentTime);
		getPersistenceEngine().executeNamedQueryWithList("UPDATE_ORDERS_LAST_MODIFIED_DATE", LIST_PARAMETER_NAME, orderUidPks, dbCurrentTime);
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	public void setEventOriginatorHelper(final EventOriginatorHelper eventOriginatorHelper) {
		this.eventOriginatorHelper = eventOriginatorHelper;
	}

	public void setOrderEventHelper(final OrderEventHelper orderEventHelper) {
		this.orderEventHelper = orderEventHelper;
	}

	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}
}
