/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.impl.shipments;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.batch.jobs.util.FailingJpaPersistenceEngine;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderEvent;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.order.impl.OrderImpl;
import com.elasticpath.domain.order.impl.PhysicalOrderShipmentImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl;
import com.elasticpath.settings.provider.TestSettingValueProvider;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;

public class ReleasePhysicalShipmentsJobTest extends DbTestCase {
	private static final long NOW_MILLIS = new Date().getTime();
	private static final Date EXPIRED_CREATION_DATE = new Date(NOW_MILLIS - 10 * 60 * 1000);

	private static final String SHIPMENT_NUMBER_1 = "100000-1";
	private static final String SHIPMENT_NUMBER_2 = "100000-2";
	private static final String SHIPMENT_NUMBER_3 = "100000-3";

	private static final int THREE_SHIPMENTS_BATCH = 3;

	@Autowired
	private JpaPersistenceEngineImpl batchPersistenceEngineTarget;
	@Autowired
	private ReleaseShipmentsBatchProcessor releaseShipmentsBatchProcessor;
	@Autowired
	private ReleasePhysicalShipmentsJob releasePhysicalShipmentsJob;

	private Order order;

	@Before
	public void init() {
		order = persistOrder();
		releasePhysicalShipmentsJob.setConfigBatchSize(new TestSettingValueProvider(1));
	}

	/**
	 * Testing the case when all shipments are eligible for release.
	 * A shipment is eligible if:
	 * 1. its status is INVENTORY_ASSIGNED
	 * 2. its order status is one of IN_PROGRESS and PARTIALLY_SHIPPED
	 * 3. its old than CURRENT_DATE - WAREHOUSE_PICK_DELAY
	 */
	@Test
	@DirtiesDatabase
	@SuppressWarnings("PMD.PrematureDeclaration")
	public void shouldReleaseTwoBatchesOfShipmentsOutOfTwoWhenAllAreEligible() {
		OrderShipment shipmentOne = createOrderShipment(order, EXPIRED_CREATION_DATE, SHIPMENT_NUMBER_1);
		OrderShipment shipmentTwo = createOrderShipment(order, EXPIRED_CREATION_DATE, SHIPMENT_NUMBER_2);

		//verify the number of shipments and events before running the job
		preJobAssertations(2L);

		//run the job
		releasePhysicalShipmentsJob.execute();

		// assert that order release shipment events are created and order and shipments correctly updated
		postJobAssertions(shipmentOne, shipmentTwo);
	}

	/**
	 * Testing the case when one shipment is not ready for release because its created date is after (CURRENT_DATE - WAREHOUSE_PICK_DELAY).
	 * It will appear in all retrieve db operations, but it should not be processed.
	 */
	@Test
	@DirtiesDatabase
	@SuppressWarnings("PMD.PrematureDeclaration")
	public void shouldReleaseTwoBatchesOfShipmentsOutOfThreeWhenOneIsNotEligible() {
		final int expectedNumberOfCreatedShipments = 3;

		OrderShipment shipmentOne = createOrderShipment(order, EXPIRED_CREATION_DATE, SHIPMENT_NUMBER_1);
		OrderShipment shipmentTwo = createOrderShipment(order, new Date(), SHIPMENT_NUMBER_2);
		OrderShipment shipmentThree = createOrderShipment(order, EXPIRED_CREATION_DATE, SHIPMENT_NUMBER_3);

		preJobAssertations(expectedNumberOfCreatedShipments);

		releasePhysicalShipmentsJob.execute();

		//verify that first 1. & 3. shipments are correctly released
		postJobAssertions(shipmentOne, shipmentThree);

		//the 2nd one shouldn't be because it's still not ready for pick-up
		assertShipmentNotProcessed(shipmentTwo);
	}

	/**
	 * Testing cases when non-expired shipments are skipped and failures occurred. The job should always correctly skip the records and
	 * fetch a next batch.
	 */
	@Test
	@DirtiesDatabase
	@SuppressWarnings("PMD.PrematureDeclaration")
	public void shouldHandleSkippedRecordsAndFailuresAndReleaseOnlyExpiredShipments() {
		releasePhysicalShipmentsJob.setConfigBatchSize(new TestSettingValueProvider(THREE_SHIPMENTS_BATCH));
		
		final int expectedNumberOfCreatedShipments = 12;

		//all expired shipments must be processed
		OrderShipment expiredShipment11 = createOrderShipment(order, EXPIRED_CREATION_DATE, "expiredShipment11");
		OrderShipment expiredShipment12 = createOrderShipment(order, EXPIRED_CREATION_DATE, "expiredShipment12");
		OrderShipment expiredShipment13 = createOrderShipment(order, EXPIRED_CREATION_DATE, "expiredShipment13");

		//1 expired shipment will be processed, 2 non-expired will be skipped
		OrderShipment nonExpiredShipment21 = createOrderShipment(order, new Date(), "nonExpiredShipment21");
		OrderShipment nonExpiredShipment22 = createOrderShipment(order, new Date(), "nonExpiredShipment22");
		OrderShipment expiredShipment23 = createOrderShipment(order, EXPIRED_CREATION_DATE, "expiredShipment23");

		//this batch has 1 shipment for which exception will be thrown - the whole batch will be skipped - expired shipment will not be processed
		OrderShipment nonExpiredShipment31 = createOrderShipment(order, new Date(), "nonExpiredShipment31");
		OrderShipment expiredShipment32 = createOrderShipment(order, EXPIRED_CREATION_DATE, "expiredShipment32");
		//throw an exception for this one
		OrderShipment exceptionalShipment33 = createOrderShipment(order, EXPIRED_CREATION_DATE, "exceptionalShipment33");

		//final batch - only 1 shipment must be processed
		OrderShipment expiredShipment41 = createOrderShipment(order, EXPIRED_CREATION_DATE, "expiredShipment41");
		OrderShipment nonExpiredShipment42 = createOrderShipment(order, new Date(), "nonExpiredShipment42");
		OrderShipment nonExpiredShipment43 = createOrderShipment(order, new Date(), "nonExpiredShipment43");

		preJobAssertations(expectedNumberOfCreatedShipments);

		//create a persistence engine that will fail on updating shipment ONE
		initJobWithPersistenceEngineThrowingException(exceptionalShipment33.getUidPk());

		releasePhysicalShipmentsJob.execute();

		//verify that all expired shipments are processed
		postJobAssertions(expiredShipment11, expiredShipment12, expiredShipment13, expiredShipment23, expiredShipment41);

		//the following, non-expired, shipments must not be processed; the last must not be processed because an exception is thrown causing
		//changes to rollback
		assertShipmentNotProcessed(nonExpiredShipment21, nonExpiredShipment22, nonExpiredShipment31, nonExpiredShipment42, nonExpiredShipment43,
				exceptionalShipment33, expiredShipment32);
	}

	private Object[] getOrderAndShipmentLastModifiedDatesByShipmentNumber(final String shipmentNumber) {
		StringBuilder query = new StringBuilder("SELECT o.lastModifiedDate, s.lastModifiedDate, s.status FROM PhysicalOrderShipmentImpl s")
				.append(" JOIN s.orderInternal o")
				.append(" WHERE s.shipmentNumber = '")
				.append(shipmentNumber)
				.append("'");

		return getPersistenceEngine().<Object[]>retrieve(query.toString()).get(0);
	}

	private boolean isShipmentReleaseEventCreatedForShipment(final String shipmentNumber) {
		String eventDetail = String.format("Order shipment #%s is released.", shipmentNumber);

		return getPersistenceEngine().<Long>retrieve("SELECT count(e.uidPk) FROM OrderEventImpl e where e.note = '" + eventDetail + "'")
				.get(0) == 1L;
	}

	private List<OrderEvent> getActualOrderEvents() {
		return getPersistenceEngine().retrieve("SELECT e FROM OrderEventImpl e");
	}

	private Long getActualNumberOfOrderShipments() {
		return getPersistenceEngine().<Long>retrieve("SELECT count(pos.uidPk) FROM PhysicalOrderShipmentImpl pos")
				.get(0);
	}

	private Order persistOrder() {
		Catalog catalog = persisterFactory.getCatalogTestPersister().persistDefaultMasterCatalog();
		Warehouse warehouse = persisterFactory.getStoreTestPersister().persistDefaultWarehouse(); //pick delay = 10min
		Store store = persisterFactory.getStoreTestPersister().persistDefaultStore(catalog, warehouse);

		return createOrder(store);
	}

	private Order createOrder(final Store store) {
		Order order = new OrderImpl();
		order.setLastModifiedDate(new Date());
		order.setCreatedDate(new Date());
		order.setLocale(Locale.ENGLISH);
		order.setStoreCode(store.getCode());

		doInTransaction(status -> saveOrUpdate(order));
		//set correct status
		doInTransaction(status -> getPersistenceEngine().executeQuery("UPDATE OrderImpl o "
				+ "SET o.status = com.elasticpath.domain.order.OrderStatus.IN_PROGRESS  WHERE o.uidPk = " + order.getUidPk()));

		return order;
	}

	private OrderShipment createOrderShipment(final Order order, final Date createdDate, final String shipmentNumber) {
		final PhysicalOrderShipmentImpl orderShipment = new PhysicalOrderShipmentImpl();

		//must set the FAILED_ORDER status to avoid recalculations before update/persists and after load JPA events
		orderShipment.setStatus(OrderShipmentStatus.FAILED_ORDER);
		orderShipment.setLastModifiedDate(new Date());
		orderShipment.setCreatedDate(createdDate);
		orderShipment.setShipmentNumber(shipmentNumber);
		//must disable recalculations - they are not even relevant for the job
		orderShipment.disableRecalculation();
		orderShipment.setOrder(order);

		doInTransaction(status -> saveOrUpdate(orderShipment));

		//set correct status
		doInTransaction(status -> getPersistenceEngine().executeQuery("UPDATE PhysicalOrderShipmentImpl pos "
				+ "SET pos.status = com.elasticpath.domain.order.OrderShipmentStatus.INVENTORY_ASSIGNED  WHERE pos.uidPk = "
				+ orderShipment.getUidPk()));

		return orderShipment;
	}

	private <T extends Persistable> T saveOrUpdate(final T entity) {
		return getPersistenceEngine().saveOrUpdate(entity);
	}

	private void preJobAssertations(final long expectedNumberOfCreatedShipments) {
		assertThat(getActualNumberOfOrderShipments())
				.as("The list of created shipments must be 2")
				.isEqualTo(expectedNumberOfCreatedShipments);

		assertThat(getActualOrderEvents())
				.as("The list of events must be empty")
				.isEmpty();
	}

	private void postJobAssertions(final OrderShipment... shipments) {
		for (OrderShipment shipment : shipments) {
			assertThat(isShipmentReleaseEventCreatedForShipment(shipment.getShipmentNumber()))
					.as(String.format("Missing shipment release event for shipment %s", shipment.getShipmentNumber()))
					.isTrue();

			Object[] actualStatusAndDates = getOrderAndShipmentLastModifiedDatesByShipmentNumber(shipment.getShipmentNumber());

			assertThat(actualStatusAndDates)
					.as("The array of last-modified dates can't be null")
					.isNotNull();

			//verify that order's LMD is updated
			assertThat((Date) actualStatusAndDates[0])
					.as("Order last modified date is not updated")
					.isAfter(order.getLastModifiedDate());

			//verify that shipment's LMD is updated
			assertThat((Date) actualStatusAndDates[1])
					.as(String.format("Shipment's [%s] last modified date is not updated", shipment.getShipmentNumber()))
					.isAfter(shipment.getLastModifiedDate());

			//verify that shipment's status is RELEASED
			assertThat((OrderShipmentStatus) actualStatusAndDates[2])
					.as("Shipment status must be RELEASED")
					.isEqualTo(OrderShipmentStatus.RELEASED);

		}
	}

	//create a PersistenceEngine with a method that will throw exception under given condition
	private void initJobWithPersistenceEngineThrowingException(final long shipmentUidToFailFor) {
		try {
			PersistenceEngine failingJpaPersistenceEngine = new FailingJpaPersistenceEngine(batchPersistenceEngineTarget, shipmentUidToFailFor,
					"UPDATE_SHIPMENTS_WITH_RELEASED_STATUS");

			releaseShipmentsBatchProcessor.setPersistenceEngine(failingJpaPersistenceEngine);

		} catch (Exception e) {
			throw new EpSystemException("Error occurred", e);
		}
	}

	private void assertShipmentNotProcessed(final OrderShipment... nonProcessedShipments) {
		for (OrderShipment nonProcessedShipment : nonProcessedShipments) {
			String shipmentNumber = nonProcessedShipment.getShipmentNumber();

			assertThat(isShipmentReleaseEventCreatedForShipment(shipmentNumber))
					.as(String.format("Shipment release event shouldn't exist for shipment %s", shipmentNumber))
					.isFalse();

			Object[] actualStatusAndDates = getOrderAndShipmentLastModifiedDatesByShipmentNumber(shipmentNumber);

			//verify that shipment's LMD is NOT updated
			assertThat((Date) actualStatusAndDates[1])
					.as(String.format("Shipment's [%s] last modified date must not be changed", shipmentNumber))
					.isEqualTo(nonProcessedShipment.getLastModifiedDate());

			//verify that shipment's status is INVENTORY_ASSIGNED
			assertThat((OrderShipmentStatus) actualStatusAndDates[2])
					.as("Shipment's status must be INVENTORY_ASSIGNED")
					.isEqualTo(OrderShipmentStatus.INVENTORY_ASSIGNED);
		}
	}
}
