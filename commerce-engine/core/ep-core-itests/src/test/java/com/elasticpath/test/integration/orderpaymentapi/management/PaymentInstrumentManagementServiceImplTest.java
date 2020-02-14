/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.test.integration.orderpaymentapi.management;

import static com.elasticpath.test.persister.PaymentInstrumentPersister.PAYMENT_INSTRUMENT_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;
import com.elasticpath.test.db.DbTestCase;

public class PaymentInstrumentManagementServiceImplTest extends DbTestCase {

	@Autowired
	private PaymentInstrumentManagementService testee;

	@Autowired
	private OrderPaymentInstrumentService orderPaymentInstrumentService;

	private Order order;

	@Before
	public void setUp() {
		order = persistOrderWithInstrument();
	}

	@Test
	public void testFindOrderInstruments() {
		final List<PaymentInstrumentDTO> orderInstruments = testee.findOrderInstruments(order);

		makeAllOrderInstrumentsLimited();

		assertThat(testee.findOrderInstruments(order))
				.usingFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(orderInstruments);
		assertThat(orderInstruments).extracting(PaymentInstrumentDTO::getName).containsExactly(PAYMENT_INSTRUMENT_NAME);
	}

	@Test
	public void testFindUnlimitedOrderInstruments() {
		final List<PaymentInstrumentDTO> orderInstruments = testee.findUnlimitedOrderInstruments(order);
		assertThat(orderInstruments).extracting(PaymentInstrumentDTO::getName).containsExactly(PAYMENT_INSTRUMENT_NAME);

		makeAllOrderInstrumentsLimited();

		assertThat(testee.findUnlimitedOrderInstruments(order)).isEmpty();
	}

	@Test
	public void testFindByOrderPaymentInstrumentGuid() {
		final Collection<OrderPaymentInstrument> orderPaymentInstruments = orderPaymentInstrumentService.findByOrder(order);
		final String[] paymentInstrumentGuids = orderPaymentInstruments.stream()
				.map(OrderPaymentInstrument::getPaymentInstrumentGuid)
				.toArray(String[]::new);

		assertThat(orderPaymentInstruments).extracting(OrderPaymentInstrument::getGuid)
				.extracting(orderPaymentInstrumentGuid -> testee.findByOrderPaymentInstrumentGuid(orderPaymentInstrumentGuid))
				.extracting(PaymentInstrumentDTO::getGUID)
				.contains(paymentInstrumentGuids);
	}

	private void makeAllOrderInstrumentsLimited() {
		final Collection<OrderPaymentInstrument> orderPaymentInstruments = orderPaymentInstrumentService.findByOrder(order);
		for (OrderPaymentInstrument orderPaymentInstrument : orderPaymentInstruments) {
			orderPaymentInstrument.setLimitAmount(BigDecimal.ONE);
			orderPaymentInstrumentService.saveOrUpdate(orderPaymentInstrument);
		}
	}

	private Order persistOrderWithInstrument() {
		Product product = persisterFactory.getCatalogTestPersister()
				.createDefaultProductWithSkuAndInventory(scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse());
		return persisterFactory.getOrderTestPersister()
				.createOrderWithSkus(scenario.getStore(), product.getDefaultSku());
	}

}
