/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.test.integration.orderpaymentapi;

import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT_INSTRUMENT;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.impl.OrderPaymentImpl;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.impl.OrderPaymentInstrumentServiceImpl;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.util.Utils;

/**
 * Test for {@link OrderPaymentInstrumentServiceImpl}.
 */
public class OrderPaymentInstrumentServiceImplTest extends DbTestCase {

	private static String paymentInstrumentGuid;

	@Autowired
	private OrderPaymentInstrumentService orderPaymentInstrumentService;

	@Autowired
	private OrderService orderService;

	@Before
	public void setUp() {
		paymentInstrumentGuid = Utils.uniqueCode("PAYMENTINSTRUMENT");
	}

	@Test
	@DirtiesDatabase
	public void ensureFindByGuidFindsOrderPaymentInstrument() {
        OrderPaymentInstrument entity = createTestOrderPaymentInstrument();
        Order order = createOrder();
        entity.setOrderNumber(order.getOrderNumber());

        orderPaymentInstrumentService.saveOrUpdate(entity);

        OrderPaymentInstrument persistedInstrument = orderPaymentInstrumentService.findByGuid(entity.getGuid());
        assertEquals("Wrong OrderPaymentInstrument found by GUID", entity, persistedInstrument);
    }

	@Test
	@DirtiesDatabase
	public void ensureFindByOrderUidFindsOrderPaymentInstrument() {
        OrderPaymentInstrument entity = createTestOrderPaymentInstrument();
        Order order = createOrder();
        entity.setOrderNumber(order.getOrderNumber());

        orderPaymentInstrumentService.saveOrUpdate(entity);

        final Collection<OrderPaymentInstrument> instruments = orderPaymentInstrumentService.findByOrder(order);
        assertFalse("No OrderPaymentInstrument entities were found for this Order", instruments.isEmpty());
        assertThat("Wrong OrderPaymentInstrument associated with the Order", instruments, hasItem(entity));
    }

	@Test
	@DirtiesDatabase
	public void ensureFindByOrderAndPaymentInstrumentGuidFindsOneOrderPaymentInstrument() {
        Order order = createOrder();

        OrderPaymentInstrument entity = createTestOrderPaymentInstrument();
        entity.setOrderNumber(order.getOrderNumber());
        orderPaymentInstrumentService.saveOrUpdate(entity);

        OrderPaymentInstrument entityAssociatedWithAnotherPaymentInstrument = createTestOrderPaymentInstrument();
        entityAssociatedWithAnotherPaymentInstrument.setOrderNumber(order.getOrderNumber());
        entityAssociatedWithAnotherPaymentInstrument.setPaymentInstrumentGuid("another-payment-instrument-guid");
        orderPaymentInstrumentService.saveOrUpdate(entityAssociatedWithAnotherPaymentInstrument);

        OrderPayment dummyOrderPayment = new OrderPaymentImpl();
        dummyOrderPayment.setOrderNumber(order.getOrderNumber());
        dummyOrderPayment.setPaymentInstrumentGuid(paymentInstrumentGuid);

        OrderPaymentInstrument instrument = orderPaymentInstrumentService.findByOrderPayment(dummyOrderPayment);
        assertNotNull("No OrderPaymentInstrument entities were found for this Order and PaymentInstrument GUID", instrument);
        assertEquals("Wrong OrderPaymentInstrument associated with the Order and PaymentInstrument", entity, instrument);
    }

	@Test
	@DirtiesDatabase
	public void removingOrderPaymentInstrumentDoesNotRemoveOrder() {
        Order order = createOrder();

        OrderPaymentInstrument entity = createTestOrderPaymentInstrument();
        entity.setOrderNumber(order.getOrderNumber());

        orderPaymentInstrumentService.saveOrUpdate(entity);
        orderPaymentInstrumentService.remove(entity);

        final Order persistedOrder = orderService.get(order.getUidPk());
        assertNotNull("Order was unexpectedly removed", persistedOrder);
    }

	private OrderPaymentInstrument createTestOrderPaymentInstrument() {
		OrderPaymentInstrument orderPaymentInstrument = getBeanFactory().getPrototypeBean(ORDER_PAYMENT_INSTRUMENT, OrderPaymentInstrument.class);
		orderPaymentInstrument.setPaymentInstrumentGuid(paymentInstrumentGuid);
		orderPaymentInstrument.setLimitAmount(BigDecimal.ONE);
		return orderPaymentInstrument;
	}

	private Order createOrder() {
		Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(
				scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse());

		return persisterFactory.getOrderTestPersister().createOrderWithSkus(
				scenario.getStore(), product.getDefaultSku());
	}

}
