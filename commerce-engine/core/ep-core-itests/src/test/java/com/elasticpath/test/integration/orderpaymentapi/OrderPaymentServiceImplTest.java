/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.test.integration.orderpaymentapi;

import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT_DATA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderPaymentStatus;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentData;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.service.orderpaymentapi.impl.OrderPaymentServiceImpl;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.util.Utils;

/**
 * Test for {@link OrderPaymentServiceImpl}.
 */
public class OrderPaymentServiceImplTest extends DbTestCase {

	private static String paymentInstrumentGuid;

	@Autowired
	private OrderPaymentService orderPaymentService;

	@Autowired
	private OrderService orderService;

	@Before
	public void setUp() {
		paymentInstrumentGuid = Utils.uniqueCode("PAYMENTINSTRUMENT");
	}

	@Test
	@DirtiesDatabase
	public void verifySaveAndRetrieval() {
		final OrderPayment orderPayment1 = createTestOrderPayments();
		orderPaymentService.saveOrUpdate(orderPayment1);

		final OrderPayment orderPayment2 = createTestOrderPayments();
		orderPaymentService.saveOrUpdate(orderPayment2);

		OrderPayment persistedOrderPayment = orderPaymentService.findByGuid(orderPayment1.getGuid());
		assertEquals(orderPayment1.getTransactionType(), persistedOrderPayment.getTransactionType());
		assertEquals(orderPayment1.getOrderNumber(), persistedOrderPayment.getOrderNumber());
		assertEquals(orderPayment1.getOrderPaymentStatus(), persistedOrderPayment.getOrderPaymentStatus());
		assertEquals(0, persistedOrderPayment.getAmount().compareTo(orderPayment1.getAmount()));
		assertEquals(orderPayment1.getCurrency(), persistedOrderPayment.getCurrency());
		assertEquals(orderPayment1.getCreatedDate(), persistedOrderPayment.getCreatedDate());
		assertEquals(orderPayment1.getPaymentInstrumentGuid(), persistedOrderPayment.getPaymentInstrumentGuid());
		assertEquals(orderPayment1.getParentOrderPaymentGuid(), persistedOrderPayment.getParentOrderPaymentGuid());
		assertEquals(orderPayment1.isOriginalPI(), persistedOrderPayment.isOriginalPI());

		assertThat(orderPaymentService.findByPaymentInstrumentGuid(paymentInstrumentGuid))
				.containsExactlyInAnyOrder(orderPayment1, orderPayment2);
	}

	@Test
	@DirtiesDatabase
	public void verifyFieldsPersisted() {
		final OrderPayment entity = createTestOrderPayments();

		orderPaymentService.saveOrUpdate(entity);

		final OrderPayment persistedEntity = orderPaymentService.findByGuid(entity.getGuid());
		assertThat(persistedEntity.getOrderPaymentStatus()).isEqualTo(entity.getOrderPaymentStatus());
		assertThat(persistedEntity.getTransactionType()).isEqualTo(entity.getTransactionType());
		assertThat(persistedEntity.getCurrency()).isEqualTo(entity.getCurrency());
		assertThat(persistedEntity.getCreatedDate()).isEqualTo(entity.getCreatedDate());
		assertThat(persistedEntity.getOrderNumber()).isEqualTo(entity.getOrderNumber());
		assertThat(persistedEntity.getAmount()).isEqualByComparingTo(entity.getAmount());
	}

	@Test
	@DirtiesDatabase
	public void verifyFindByOrderResultIsSortedByCreatedDate() {
		final Order order = createOrder();
		final Instant now = Instant.now();

		final OrderPayment orderPaymentSecond = createTestOrderPayments();
		orderPaymentSecond.setOrderNumber(order.getOrderNumber());
		orderPaymentSecond.setCreatedDate(Date.from(now.plusSeconds(10)));
		orderPaymentService.saveOrUpdate(orderPaymentSecond);

		final OrderPayment orderPaymentFirst = createTestOrderPayments();
		orderPaymentFirst.setOrderNumber(order.getOrderNumber());
		orderPaymentSecond.setCreatedDate(Date.from(now));
		orderPaymentService.saveOrUpdate(orderPaymentFirst);

		assertThat(orderPaymentService.findByOrder(order)).containsSequence(orderPaymentFirst, orderPaymentSecond);
	}

	@Test
	@DirtiesDatabase
	public void verifyOrderPaymentDataGetsUpdated() {
		final OrderPayment orderPayment = createTestOrderPayments();

		final Set<OrderPaymentData> orderPaymentDataMap = new HashSet<>();
		orderPayment.setOrderPaymentData(orderPaymentDataMap);

		long uidPk = orderPaymentService.saveOrUpdate(orderPayment).getUidPk();

		final OrderPayment persistedOrderPayment = orderPaymentService.findByUid(uidPk);
		final Set<OrderPaymentData> orderPaymentData = persistedOrderPayment.getOrderPaymentData();
		assertTrue(orderPaymentData.isEmpty());

		String key = "key";
		String value = "value";

		orderPaymentData.add(createOrderPaymentData(key, value));
		orderPaymentService.saveOrUpdate(persistedOrderPayment);

		assertThat(orderPaymentService.findByUid(uidPk).getOrderPaymentData())
				.filteredOn(paymentData -> paymentData.getKey().equals(key))
				.extracting(OrderPaymentData::getValue)
				.containsExactly(value);
	}

	@Test
	@DirtiesDatabase
	public void verifyReservationOrderPaymentLinking() {
		final OrderPayment reservationOrderPayment = createTestOrderPayments();
		final String reservationOrderPaymentGuid = orderPaymentService.saveOrUpdate(reservationOrderPayment).getGuid();

		final OrderPayment orderPayment = createTestOrderPayments();
		orderPayment.setParentOrderPaymentGuid(reservationOrderPaymentGuid);
		final String guid = orderPaymentService.saveOrUpdate(orderPayment).getGuid();
		final OrderPayment persistedOrderPayment = orderPaymentService.findByGuid(guid);

		assertEquals(reservationOrderPaymentGuid, persistedOrderPayment.getParentOrderPaymentGuid());
	}

	@Test
	@DirtiesDatabase
	public void removingOrderPaymentsDoesNotRemoveOrder() {
		final Order order = createOrder();

		final OrderPayment entity = createTestOrderPayments();
		entity.setOrderNumber(order.getOrderNumber());

		orderPaymentService.saveOrUpdate(entity);
		orderPaymentService.remove(entity);

		final Order persistedOrder = orderService.get(order.getUidPk());
		assertNotNull("Order was unexpectedly removed", persistedOrder);
	}

	@Test
	@DirtiesDatabase
	public void findByOrderUidFindsPayments() {
		Order order = createOrder();

		OrderPayment orderPayment1 = createTestOrderPayments();
		orderPayment1.setPaymentInstrumentGuid(paymentInstrumentGuid);
		orderPayment1.setOrderNumber(order.getOrderNumber());
		orderPaymentService.saveOrUpdate(orderPayment1);

		OrderPayment orderPayment2 = createTestOrderPayments();
		orderPayment2.setPaymentInstrumentGuid(paymentInstrumentGuid);
		orderPayment2.setOrderNumber(order.getOrderNumber());
		orderPaymentService.saveOrUpdate(orderPayment2);

		assertThat(orderPaymentService.findByOrder(order)).contains(orderPayment1, orderPayment2);
	}

	private OrderPayment createTestOrderPayments() {
		final OrderPayment entity = getBeanFactory().getPrototypeBean(ORDER_PAYMENT, OrderPayment.class);
		entity.setPaymentInstrumentGuid(paymentInstrumentGuid);
		entity.setCreatedDate(new Date());
		entity.setTransactionType(TransactionType.RESERVE);
		entity.setOrderPaymentStatus(OrderPaymentStatus.APPROVED);
		entity.setAmount(BigDecimal.ONE);
		entity.setCurrency(Currency.getInstance("CAD"));
		entity.setParentOrderPaymentGuid(entity.getGuid());
		entity.setOriginalPI(true);
		return entity;
	}

	private OrderPaymentData createOrderPaymentData(final String key, final String value) {
		final OrderPaymentData orderPaymentData = getBeanFactory().getPrototypeBean(ORDER_PAYMENT_DATA, OrderPaymentData.class);
		orderPaymentData.setKey(key);
		orderPaymentData.setValue(value);
		return orderPaymentData;
	}

	private Order createOrder() {
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(
				scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse());

		return persisterFactory.getOrderTestPersister().createOrderWithSkus(
				scenario.getStore(), product.getDefaultSku());
	}

}
