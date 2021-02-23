/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.test.integration.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.elasticpath.core.messaging.order.OrderEventType;
import com.elasticpath.domain.builder.checkout.CheckoutTestCartBuilder;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.builder.shopper.ShoppingContextBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.order.OrderReturnSku;
import com.elasticpath.domain.order.OrderReturnType;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentData;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.money.Money;
import com.elasticpath.persister.ShoppingContextPersister;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.order.CompleteShipmentFailedException;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.order.ReturnAndExchangeService;
import com.elasticpath.service.order.ReturnExchangeRefundTypeEnum;
import com.elasticpath.service.order.impl.OrderServiceImpl;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;
import com.elasticpath.service.payment.provider.PaymentProviderPluginForIntegrationTesting;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.StoreTestPersister;
import com.elasticpath.test.util.CheckoutHelper;

@ContextConfiguration
public class OrderPaymentDataPropagationTest extends DbTestCase {

	private static final String RESERVE_DATA_KEY = "reserve-data";
	private static final String CHARGE_DATA_KEY = "charge-data";

	@Autowired
	private ShoppingContextBuilder shoppingContextBuilder;

	@Autowired
	private ShoppingContextPersister shoppingContextPersister;

	@Autowired
	private CheckoutTestCartBuilder checkoutTestCartBuilder;

	@Autowired
	private PricingSnapshotService pricingSnapshotService;

	@Autowired
	private TaxSnapshotService taxSnapshotService;

	@Autowired
	private CheckoutService checkoutService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderServiceImpl orderServiceNoProxy;

	@Autowired
	private EventOriginatorHelper eventOriginatorHelper;

	@Autowired
	private ReturnAndExchangeService returnAndExchangeService;

	@Autowired
	private OrderPaymentApiService orderPaymentApiService;

	@Autowired
	private PaymentInstrumentManagementService paymentInstrumentManagementService;

	@Autowired
	private OrderPaymentService orderPaymentService;

	private ShoppingContext shoppingContext;
	private ShoppingCart shoppingCart;
	private EventOriginator originator;
	private CheckoutHelper checkoutHelper;

	@Before
	public void initialize() {
		final Customer customer = createCustomer();
		shoppingContext = shoppingContextBuilder
				.withStoreCode(scenario.getStore().getCode())
				.withCustomer(customer)
				.build();
		shoppingContextPersister.persist(shoppingContext);

		shoppingCart = checkoutTestCartBuilder.withScenario(scenario)
				.withCustomer(customer)
				.withCustomerSession(shoppingContext.getCustomerSession())
				.withPhysicalProduct()
				.build();

		originator = eventOriginatorHelper.getCustomerOriginator(customer);

		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReserveCapability.class,
				request -> createResponse(RESERVE_DATA_KEY));
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ChargeCapability.class, request -> {
			if (!request.getReservationData().containsKey(RESERVE_DATA_KEY)) {
				throw new PaymentCapabilityRequestFailedException(RESERVE_DATA_KEY + " missing", "", false);
			}
			return createResponse(CHARGE_DATA_KEY);
		});
		checkoutHelper = new CheckoutHelper(getTac());
	}

	private PaymentCapabilityResponse createResponse(final String dataKey) {
		PaymentCapabilityResponse response = new PaymentCapabilityResponse();
		response.setData(ImmutableMap.of(dataKey, ""));
		response.setProcessedDateTime(LocalDateTime.now());
		response.setRequestHold(false);
		return response;
	}

	@After
	public void tearDown() {
		PaymentProviderPluginForIntegrationTesting.resetCapabilities();
	}

	@DirtiesDatabase
	@Test
	public void reserveDataPropagation() {
		final Order order = checkout();

		assertThat(orderPaymentService.findByOrder(order))
				.filteredOn(orderPayment -> orderPayment.getTransactionType() == TransactionType.RESERVE)
				.flatExtracting(OrderPayment::getOrderPaymentData)
				.extracting(OrderPaymentData::getKey)
				.containsExactly(RESERVE_DATA_KEY);
	}

	@DirtiesDatabase
	@Test
	public void chargeDataPropagation() {
		final Order order = checkout();
		final OrderShipment orderShipment = releaseAndCompleteShipment(order, 0);

		assertThat(orderPaymentService.findByOrder(orderShipment.getOrder()))
				.filteredOn(orderPayment -> orderPayment.getTransactionType() == TransactionType.CHARGE)
				.flatExtracting(OrderPayment::getOrderPaymentData)
				.extracting(OrderPaymentData::getKey)
				.containsExactly(CHARGE_DATA_KEY);
	}

	@DirtiesDatabase
	@Test
	public void creditDataPropagation() {
		final String creditDataKey = "credit-data";
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), CreditCapability.class, request -> {
			if (!request.getChargeData().containsKey(CHARGE_DATA_KEY)) {
				throw new PaymentCapabilityRequestFailedException(CHARGE_DATA_KEY + " missing", "", false);
			}
			return createResponse(creditDataKey);
		});
		final Order order = checkout();
		final OrderShipment orderShipment = releaseAndCompleteShipment(order, 0);
		final OrderReturn orderReturn = returnShipment(orderShipment);

		assertThat(orderPaymentService.findByOrder(orderReturn.getOrder()))
				.filteredOn(orderPayment -> orderPayment.getTransactionType() == TransactionType.CREDIT)
				.flatExtracting(OrderPayment::getOrderPaymentData)
				.extracting(OrderPaymentData::getKey)
				.containsExactly(creditDataKey);
	}

	@DirtiesDatabase
	@Test
	public void modifyAndCancelDataPropagation() {
		final String modifyDataKey = "modify-data";
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ModifyCapability.class, request -> {
			if (!request.getReservationData().containsKey(RESERVE_DATA_KEY)) {
				throw new PaymentCapabilityRequestFailedException(RESERVE_DATA_KEY + " missing", "", false);
			}
			return createResponse(modifyDataKey);
		});
		final String cancelDataKey = "cancel-data";
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), CancelCapability.class, request -> {
			// note that modify is yet another reservation, which replaces previous one
			if (!request.getReservationData().containsKey(modifyDataKey)) {
				throw new PaymentCapabilityRequestFailedException(modifyDataKey + " missing", "", false);
			}
			return createResponse(cancelDataKey);
		});
		final Order order = checkout();
		final List<PaymentInstrumentDTO> instruments = paymentInstrumentManagementService.findOrderInstruments(order);
		final Money newAmount = Money.valueOf(order.getTotal().subtract(BigDecimal.TEN), order.getCurrency());

		orderPaymentApiService.orderModified(order, instruments, newAmount);
		final Order updatedOrder = orderService.cancelOrder(order);

		assertThat(orderPaymentService.findByOrder(updatedOrder))
				.filteredOn(orderPayment -> orderPayment.getTransactionType() == TransactionType.MODIFY_RESERVE)
				.flatExtracting(OrderPayment::getOrderPaymentData)
				.extracting(OrderPaymentData::getKey)
				.containsExactly(modifyDataKey);
		assertThat(orderPaymentService.findByOrder(updatedOrder))
				.filteredOn(orderPayment -> orderPayment.getTransactionType() == TransactionType.CANCEL_RESERVE)
				.flatExtracting(OrderPayment::getOrderPaymentData)
				.extracting(OrderPaymentData::getKey)
				.containsExactly(cancelDataKey);
	}

	@DirtiesDatabase
	@Test
	public void reverseChargeDataPropagation() {
		final String reverseChargeDataKey = "reverse-charge-data";
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReverseChargeCapability.class, request -> {
			if (!request.getChargeData().containsKey(CHARGE_DATA_KEY)) {
				throw new PaymentCapabilityRequestFailedException(CHARGE_DATA_KEY + " missing", "", false);
			}
			return createResponse(reverseChargeDataKey);
		});
		orderServiceNoProxy.setEventMessagePublisher(eventMessage -> {
			if (eventMessage.getEventType() == OrderEventType.ORDER_SHIPMENT_SHIPPED) {
				throw new IllegalStateException("Shipping failed by design");
			}
		});

		final Order order = checkout();
		try {
			releaseAndCompleteShipment(order, 0);
			fail("Shipment completion must fail");
		} catch (CompleteShipmentFailedException expected) {
			// ok
		}

		assertThat(orderPaymentService.findByOrder(order))
				.filteredOn(orderPayment -> orderPayment.getTransactionType() == TransactionType.REVERSE_CHARGE)
				.flatExtracting(OrderPayment::getOrderPaymentData)
				.extracting(OrderPaymentData::getKey)
				.containsExactly(reverseChargeDataKey);
	}

	private Customer createCustomer() {
		final StoreTestPersister storeTestPersister = getTac().getPersistersFactory().getStoreTestPersister();
		return storeTestPersister.createDefaultCustomer(scenario.getStore());
	}

	private Order checkout() {
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);
		return checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, shoppingContext.getCustomerSession(), true);
	}

	private OrderShipment releaseAndCompleteShipment(final Order order, int shipmentIndex) {
		final OrderShipment orderShipment = orderService.processReleaseShipment(order.getPhysicalShipments().get(shipmentIndex));
		final Order updatedOrder = orderService.completeShipment(orderShipment.getShipmentNumber(), "", true, new Date(), false, originator);
		return updatedOrder.getPhysicalShipments().get(shipmentIndex);
	}

	private OrderReturn returnShipment(final OrderShipment orderShipment) {
		final OrderReturn orderReturn = returnAndExchangeService.getOrderReturnPrototype(orderShipment, OrderReturnType.RETURN);
		for (OrderReturnSku orderReturnSku : orderReturn.getOrderReturnSkus()) {
			orderReturnSku.setQuantity(orderReturnSku.getOrderSku().getQuantity());
			orderReturnSku.setReturnReason("Faulty");
			orderReturnSku.setReturnAmount(orderReturnSku.getAmountMoney().getAmount());
		}
		orderReturn.recalculateOrderReturn();
		return returnAndExchangeService.completeReturn(orderReturn, ReturnExchangeRefundTypeEnum.REFUND_TO_ORIGINAL);
	}

}