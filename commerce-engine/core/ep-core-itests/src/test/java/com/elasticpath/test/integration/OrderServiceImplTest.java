/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.test.integration;

import static com.elasticpath.commons.constants.ContextIdNames.EVENT_ORIGINATOR_HELPER;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_SEARCH_CRITERIA;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_SKU;
import static com.elasticpath.commons.constants.ContextIdNames.PERSISTENCE_ENGINE;
import static com.elasticpath.commons.constants.ContextIdNames.PRICE;
import static com.elasticpath.domain.catalog.AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK;
import static com.elasticpath.domain.order.OrderPaymentStatus.APPROVED;
import static com.elasticpath.domain.order.OrderShipmentStatus.INVENTORY_ASSIGNED;
import static com.elasticpath.domain.order.OrderShipmentStatus.RELEASED;
import static com.elasticpath.domain.order.OrderStatus.CANCELLED;
import static com.elasticpath.domain.order.OrderStatus.FAILED;
import static com.elasticpath.domain.order.OrderStatus.IN_PROGRESS;
import static com.elasticpath.domain.order.OrderStatus.ONHOLD;
import static com.elasticpath.domain.shipping.ShipmentType.PHYSICAL;
import static java.lang.System.currentTimeMillis;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.DataFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.core.messaging.order.OrderEventType;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.InventoryAudit;
import com.elasticpath.domain.catalog.InventoryEventType;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.InventoryAuditImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.domain.order.PurchaseHistorySearchCriteria;
import com.elasticpath.domain.order.impl.PhysicalOrderShipmentImpl;
import com.elasticpath.domain.order.impl.PurchaseHistorySearchCriteriaImpl;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.inventory.InventoryDto;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.camel.test.support.CamelContextMessagePurger;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.money.Money;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.catalog.ProductInventoryManagementService;
import com.elasticpath.service.catalog.ProductService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.order.OrderHoldService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.service.payment.provider.PaymentProviderPluginForIntegrationTesting;
import com.elasticpath.service.search.query.OrderSearchCriteria;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;
import com.elasticpath.test.persister.CatalogTestPersister;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.CheckoutHelper;

/**
 * Integration test for OrderService.
 */
@SuppressWarnings({"PMD.TooManyStaticImports", "deprecation", "PMD.GodClass", "PMD.CouplingBetweenObjects", "PMD.TooManyFields"})
public class OrderServiceImplTest extends BasicSpringContextTest {

	private static final String INVALID_GUID = "BAD GUID";

	private static final String NON_EXISTENT_CART_ORDER_GUID = "NON_EXISTENT_CART_ORDER_GUID";

	private static final String DIFFERENT_CART_ORDER_GUID = "DIFFERENT_CART_ORDER_GUID";

	private static final String CART_ORDER_GUID = "CART_ORDER_GUID";

	private static final String ORDER_MESSAGING_CAMEL_CONTEXT = "ep-order-messaging";

	@Autowired
	private EventMessageFactory eventMessageFactory;

	@Autowired
	@Qualifier(ORDER_MESSAGING_CAMEL_CONTEXT)
	private CamelContext camelContext;

	@Autowired
	private CheckoutService checkoutService;

	@Autowired
	private OrderPaymentService orderPaymentService;

	@Autowired
	private OrderPaymentApiService orderPaymentApiService;

	private Store store;

	private Product product;

	private Customer customer;

	private CustomerAddress address;

	private SimpleStoreScenario scenario;

	private TestDataPersisterFactory persisterFactory;

	private CheckoutHelper checkoutHelper;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderHoldService orderHoldService;

	private static final int MAX_RESULTS = 10;

	@Autowired
	private CartDirector cartDirector;

	private Shopper shopper;

	@Autowired
	private PersistenceEngine persistenceEngine;

	@Autowired
	private TimeService timeService;

	@EndpointInject(uri = "mock:orders/events", context = ORDER_MESSAGING_CAMEL_CONTEXT)
	private MockEndpoint mockOrderEventEndpoint;

	@EndpointInject(ref = "epOrderMessagingOrderEventExternalEndpoint")
	private Endpoint orderEventOutgoingEndpoint;

	@Autowired
	private DataFormat eventMessageDataFormat;

	private List<ReversibleCheckoutAction> reversibleCheckoutActions;
	private final ReversibleCheckoutAction failingAction = new ReversibleCheckoutAction() {

		@Override
		public void execute(final PreCaptureCheckoutActionContext context) throws EpSystemException {
			throw new EpSystemException("Causing previous ReversibleCheckoutActions to roll back.");

		}

		@Override
		public void rollback(final PreCaptureCheckoutActionContext context)
				throws EpSystemException {
			//do nothing
		}
	};

	@Autowired
	private ProductInventoryManagementService productInventoryManagementService;
	@Autowired
	private ProductService productService;
	@Autowired
	private CamelContextMessagePurger camelContextMessagePurger;
	@Autowired
	private CatalogTestPersister catalogTestPersister;
	@Autowired
	private ProductSkuLookup productSkuLookup;
	@Autowired
	private PricingSnapshotService pricingSnapshotService;
	@Autowired
	private TaxSnapshotService taxSnapshotService;
	@Autowired
	private EventOriginatorHelper eventOriginatorHelper;
	@Autowired
	private EntityManager entityManager;

	/**
	 * Get a reference to TestApplicationContext for use within the test. Setup scenarios.
	 */
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		scenario = getTac().useScenario(SimpleStoreScenario.class);
		persisterFactory = getTac().getPersistersFactory();
		store = scenario.getStore();
		product = catalogTestPersister.createDefaultProductWithSkuAndInventory(
				scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse());

		customer = persisterFactory.getStoreTestPersister().createDefaultCustomer(store);
		address = persisterFactory.getStoreTestPersister().createCustomerAddress("Bond", "James", "1234 Pine Street", "", "Vancouver", "CA", "BC",
				"V6J5G4", "891312345007");
		shopper = persisterFactory.getStoreTestPersister().persistShopperWithAssociatedEntities(customer);
		reversibleCheckoutActions = getBeanFactory().getSingletonBean("reversibleActions", List.class);

		checkoutHelper = new CheckoutHelper(getTac());
	}

	/**
	 * Messages will be purged from Camel before and after each individual test is run.
	 *
	 * @throws Exception if the {@link CamelContextMessagePurger} is unable to purge all messages from the Camel context.
	 */
	@After
	@Before
	public void drainCamelEndpoints() throws Exception {
		camelContextMessagePurger.purgeMessages(camelContext);
	}

	// ============================ TESTS ========================= \\

	/**
	 * Make sure that the failing action is removed after each test so that it doesn't break the suite.
	 */
	@After
	public void cleanUpFailingReversibleActionIfExists() {
		cleanUpReversibleCheckoutAction(failingAction);
	}

	/**
	 * Reset plugin capabilities back to "supports everything" state.
	 */
	@After
	public void resetPluginCapabilities() {
		PaymentProviderPluginForIntegrationTesting.resetCapabilities();
	}

	/**
	 * Test order inventory.
	 */
	@DirtiesDatabase
	@Test
	public void testOrderInventory() {
		//default product is ALWAYS_AVAILABLE which prevents querying of inventories
		product.setAvailabilityCriteria(AVAILABLE_WHEN_IN_STOCK);

		// construct and save new shopping cart
		ShoppingCart shoppingCart = givenASimpleShoppingCart();

		assertNotNull("Shopping cart should contain items", shoppingCart.getRootShoppingItems());
		assertThat(shoppingCart.getRootShoppingItems()).as("There should be 1 item in the shopping cart").hasSize(1);
		ProductSku productSku = product.getDefaultSku();

		// checkout
		assertCustomerEmailEqualsShopperCustomerEmail(shopper);

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);

		// only one order should have been created by the checkout service
		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		assertThat(ordersList).as("There should be 1 item in the order").hasSize(1);
		Order order = ordersList.iterator().next();

		assertNotNull("We should be able to find the warehouse for this order", order.getStore().getWarehouses().get(0));
		long warehouseUidPk = order.getStore().getWarehouses().get(0).getUidPk();
		assertThat(store.getWarehouses().get(0).getUidPk()).as("Order warehouse should be same as the original product").isEqualTo(warehouseUidPk);
		assertThat(order.getRootShoppingItems()).as("There should be 1 sku in the order").hasSize(1);
		ShoppingItem orderSku = order.getRootShoppingItems().iterator().next();
		assertThat(orderSku.getSkuGuid()).as("The order sku guid should match the original sku guid").isEqualTo(productSku.getGuid());

		InventoryDto orderSkuInventory = productInventoryManagementService.getInventory(productSku, scenario.getWarehouse().getUidPk());

		assertNotNull("There should be inventory for the order sku in the store's warehouse", orderSkuInventory);
	}

	/**
	 * Test that if the checkout process fails, checkout actions get reversed correctly and the order is saved with the failed status.
	 */
	@Test(expected = EpSystemException.class)
	@DirtiesDatabase
	public void testFailOrderOnReversingCheckoutProcess() {
		reversibleCheckoutActions.add(failingAction);

		// construct and save new shopping cart
		ShoppingCart shoppingCart = persisterFactory.getOrderTestPersister().persistEmptyShoppingCart(shopper, address, address,
				scenario.getShippingOption(), scenario.getStore());

		ProductSku productSku = product.getDefaultSku();
		ShoppingItemDto dto = new ShoppingItemDto(productSku.getSkuCode(), 1);
		cartDirector.addItemToCart(shoppingCart, dto);

		assertNotNull("Shopping cart should contain items", shoppingCart.getRootShoppingItems());
		assertThat(shoppingCart.getRootShoppingItems()).as("There should be 1 item in the shopping cart").hasSize(1);

		// checkout
		assertCustomerEmailEqualsShopperCustomerEmail(shopper);

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutService.checkout(shoppingCart, taxSnapshot, shopper.getCustomerSession());

		// only one order should have been created by the checkout service
		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		assertThat(ordersList).as("There should be only one order").hasSize(1);
		Order order = ordersList.iterator().next();

		assertThat(FAILED).as("The order should be in the failed state after checkout reversal").isEqualTo(order.getStatus());

		assertThat(order.getRootShoppingItems()).as("There should be 1 sku in the order").hasSize(1);
		ShoppingItem orderSku = order.getRootShoppingItems().iterator().next();
		assertThat(orderSku.getSkuGuid()).as("The order sku guid should match the original sku guid").isEqualTo(productSku.getGuid());
	}

	/**
	 * Test cancelling an order.
	 */
	@Test
	public void testCancelOrder() {
		// construct and save new shopping cart
		ShoppingCart shoppingCart = givenASimpleShoppingCart();
		assertCustomerEmailEqualsShopperCustomerEmail(shopper);

		// make new order payment
		whenCheckoutIsDone(shoppingCart);

		// only one order should have been created by the checkout service
		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		Order order = ordersList.iterator().next();

		// two shipments should have been created
		List<OrderShipment> shipments = order.getAllShipments();
		assertThat(shipments).hasSize(1);

		// check payments
		final Collection<OrderPayment> payments = orderPaymentService.findByOrder(order);
		assertThat(payments).hasSize(1);
		OrderPayment authPayment = payments.iterator().next();
		assertThat(authPayment.getAmount().doubleValue()).isEqualTo(order.getTotal().doubleValue());

		assertTrue(order.isCancellable());
		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		order = orderService.cancelOrder(order);

		assertThat(order.getStatus()).isEqualTo(CANCELLED);

		// 1 auth + 1 reverse auth payments should have been conducted
		final Collection<OrderPayment> updatedPayments = orderPaymentService.findByOrder(order);
		assertThat(updatedPayments).hasSize(2);

		OrderPayment reverseAuth = null;
		for (OrderPayment payment : updatedPayments) {
			if (payment.getTransactionType() == TransactionType.CANCEL_RESERVE) {
				assertNull(reverseAuth);
				reverseAuth = payment;
			}
		}
		assertNotNull(reverseAuth);
		assertThat(reverseAuth.getOrderPaymentStatus()).isEqualTo(APPROVED);

	}

	/**
	 * Test canceling a shipment.
	 */
	@Test
	public void testCancelShipment() {
		// construct and save new shopping cart
		ShoppingCart shoppingCart = givenASimpleShoppingCart();
		assertCustomerEmailEqualsShopperCustomerEmail(shopper);

		// checkout
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);

		// only one order should have been created by the checkout service
		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		assertThat(ordersList).hasSize(1);
		Order order = ordersList.iterator().next();

		// one shipment should have been created
		List<OrderShipment> shipments = order.getAllShipments();
		assertThat(shipments).hasSize(1);

		// check payments
		final Collection<OrderPayment> payments = orderPaymentService.findByOrder(order);
		assertThat(payments).hasSize(1);
		OrderPayment authPayment = payments.iterator().next();
		assertThat(authPayment.getAmount().doubleValue()).isEqualTo(order.getTotal().doubleValue());

		PhysicalOrderShipment phShipment = order.getPhysicalShipments().iterator().next();
		assertTrue(phShipment.isCancellable());

		phShipment = orderService.cancelOrderShipment(phShipment);

		assertThat(phShipment.getShipmentStatus()).isEqualTo(OrderShipmentStatus.CANCELLED);
		assertThat(phShipment.getOrder().getStatus()).isEqualTo(CANCELLED);

		// 1 auth + 1 reverse auth payments should have been conducted
		final Collection<OrderPayment> updatedPayments = orderPaymentService.findByOrder(order);
		assertThat(updatedPayments).hasSize(2);

		OrderPayment reverseAuth = null;
		for (OrderPayment payment : updatedPayments) {
			if (payment.getTransactionType() == TransactionType.CANCEL_RESERVE) {
				assertNull(reverseAuth);
				reverseAuth = payment;
			}
		}
		assertNotNull(reverseAuth);
		assertThat(reverseAuth.getOrderPaymentStatus()).isEqualTo(APPROVED);
	}

	/**
	 * Test augmenting the shipment total.
	 */
	@Test
	@DirtiesDatabase
	public void testAugmentShipmentTotal() {
		// construct and save new shopping cart
		ShoppingCart shoppingCart = givenASimpleShoppingCart();
		assertCustomerEmailEqualsShopperCustomerEmail(shopper);

		// checkout
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);

		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		assertThat(ordersList).as("only one order should have been created by the checkout service").hasSize(1);
		Order order = ordersList.iterator().next();

		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		assertNotNull("Order should have a modified by value", order.getModifiedBy());

		List<OrderShipment> shipments = order.getAllShipments();
		assertThat(shipments).as("one shipment should have been created").hasSize(1);

		final Collection<OrderPayment> payments = orderPaymentService.findByOrder(order);
		assertThat(payments).as("there should be one payment").hasSize(1);
		OrderPayment authPayment = payments.iterator().next();
		assertEquals("payment total should be for full amount of order", order.getTotal().doubleValue(), authPayment.getAmount().doubleValue(), 0);

		PhysicalOrderShipment phShipment = order.getPhysicalShipments().iterator().next();

		OrderSku newProductOrderSku = getNewProductOrderSku(order.getUidPk());

		BigDecimal previousTotal = phShipment.getTotal();
		phShipment.addShipmentOrderSku(newProductOrderSku);
		assertTrue("the previous total amount should be less than the new one", previousTotal.compareTo(phShipment.getTotal()) < 0);

		orderPaymentApiService.orderModified(order, Collections.emptyList(), Money.valueOf(phShipment.getTotal(), order.getCurrency()));

		order = orderService.update(order);
		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		phShipment = order.getPhysicalShipments().iterator().next();

		assertThat(phShipment.getShipmentStatus()).as("The shipment status should be inventory assigned").isEqualTo(INVENTORY_ASSIGNED);
		assertThat(phShipment.getOrderShipmentType()).as("The shipment type should be physical").isEqualTo(PHYSICAL);

		for (OrderSku sku : phShipment.getShipmentOrderSkus()) {
			assertSame("Skus in the shipment should know what shipment they are for", phShipment, sku.getShipment());
		}
		phShipment = (PhysicalOrderShipment) orderService.processReleaseShipment(phShipment);

		final Collection<OrderPayment> orderPayments = orderPaymentService.findByOrder(order);
		assertThat(orderPayments).as("there should be 2 payments : 1 initial auth + 1 modification auth (new amount)").hasSize(2);

		OrderPayment initialAuth = null;
		OrderPayment newAuth = null;
		for (OrderPayment payment : orderPayments) {
			if (payment.getTransactionType() == TransactionType.RESERVE) {
				assertNull("There should only be a single initial auth", initialAuth);
				initialAuth = payment;
			} else if (payment.getTransactionType() == TransactionType.MODIFY_RESERVE) {
				assertNull("There should only be a single modification auth", newAuth);
				newAuth = payment;
			}
		}
		assertNotNull("There should have been an initial auth", initialAuth);
		assertNotNull("There should be a new auth", newAuth);

		assertThat(newAuth.getAmount()).as("Auth should have been for the new shipment total").isEqualTo(phShipment.getTotal());
		assertThat(newAuth.getOrderPaymentStatus()).as("New auth status should be approved").isEqualTo(APPROVED);

	}

	/**
	 * Test creating a new shipment.
	 */
	@Test
	@DirtiesDatabase
	public void testCreateNewShipment() {
		// drop 'smart' modify capability
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ModifyCapability.class);

		// construct new shopping cart
		ShoppingCart shoppingCart = givenASimpleShoppingCart();
		assertCustomerEmailEqualsShopperCustomerEmail(shopper);

		// checkout
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);

		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		assertThat(ordersList).as("Only one order should have been created by the checkout service").hasSize(1);
		Order order = ordersList.iterator().next();

		List<OrderShipment> shipments = order.getAllShipments();
		assertThat(shipments).as("one shipment should have been created").hasSize(1);

		final Collection<OrderPayment> payments = orderPaymentService.findByOrder(order);
		assertThat(payments).as("there should be one payment").hasSize(1);

		final OrderPayment authPayment = payments.iterator().next();
		BigDecimal originalTotal = order.getTotal();
		assertEquals("Payment should be for the full amount of the order", originalTotal.doubleValue(), authPayment.getAmount().doubleValue(), 0);

		PhysicalOrderShipment phShipment = order.getPhysicalShipments().iterator().next();
		phShipment.getOrder().setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		assertThat(phShipment.getTotal()).as("Order shipment should have a total of the original order").isEqualTo(originalTotal);

		PhysicalOrderShipmentImpl newPhysicalShipment = new PhysicalOrderShipmentImpl();
		newPhysicalShipment.setCreatedDate(new Date());
		newPhysicalShipment.setLastModifiedDate(new Date());
		newPhysicalShipment.setOrder(order);
		newPhysicalShipment.setStatus(INVENTORY_ASSIGNED);
		newPhysicalShipment.initialize();

		OrderSku newProductOrderSku = getNewProductOrderSku(order.getUidPk());

		newPhysicalShipment.addShipmentOrderSku(newProductOrderSku);
		newPhysicalShipment.setShippingOptionCode(scenario.getShippingOption().getCode());
		newPhysicalShipment.setShippingCost(ONE);

		order.addShipment(newPhysicalShipment);
		assertTrue("the new shipment total should be > 0", ZERO.compareTo(newPhysicalShipment.getTotal()) < 0);
		assertThat(order.getPhysicalShipments()).as("the order should now have 2 physical shipments").hasSize(2);

		orderPaymentApiService.orderModified(order, Collections.emptyList(),
				Money.valueOf(originalTotal.add(newPhysicalShipment.getTotal()), order.getCurrency()));

		order = orderService.update(order);
		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		phShipment = order.getPhysicalShipments().iterator().next();
		orderService.processReleaseShipment(phShipment);

		final Collection<OrderPayment> orderPayments = orderPaymentService.findByOrder(order);
		assertThat(orderPayments).as("There should be 2 payments: 1 auth + 1 new auth (new amount)").hasSize(2);

		OrderPayment newAuth = null;
		for (OrderPayment payment : orderPayments) {
			if (payment.getTransactionType() == TransactionType.CANCEL_RESERVE) {
				fail("There should not be any reverse auths");
			} else if (payment.getTransactionType() == TransactionType.RESERVE) {
				if (newAuth == null) {
					newAuth = payment;
				} else if (payment.getCreatedDate().compareTo(newAuth.getCreatedDate()) > 0) {
					newAuth = payment;
				}
			}
		}
		assertNotNull("There should have been a new auth", newAuth);

		assertThat(newAuth.getAmount()).as("new auth total should be for physical shipment").isEqualTo(newPhysicalShipment.getTotal());
		assertThat(newAuth.getOrderPaymentStatus()).as("new auth status should be approved").isEqualTo(APPROVED);
	}

	/**
	 * Tests that you can find products codes for skus that have been purchased by a user, given a start and end date.
	 */
	@Test
	@DirtiesDatabase
	public void testFindProductCodesPurchasedByUserAndFromDate() {
		// construct and save new shopping cart
		Order order = createOrder();

		// two shipments should have been created
		List<OrderShipment> shipments = order.getAllShipments();
		assertThat(shipments).hasSize(1);

		// Make a list of productCodes that were in the shopping cart
		List<String> shoppingCartProductCodes = new ArrayList<>();
		for (ShoppingItem orderSku : shipments.get(0).getShipmentOrderSkus()) {
			ProductSku sku = productSkuLookup.findByGuid(orderSku.getSkuGuid());
			shoppingCartProductCodes.add(sku.getProduct().getCode());
		}

		// Make sure we get that list back
		final int fourMinutesInMillis = 240000;
		PurchaseHistorySearchCriteria criteria = createPurchaseHistorySearchCriteria(new Date(currentTimeMillis() - fourMinutesInMillis),
				// from 4 minutes ago
				new Date()); // now

		List<String> purchasedProductCodes = orderService.findProductCodesPurchasedByUser(criteria);
		assertNotNull(purchasedProductCodes);

		for (String code : shoppingCartProductCodes) {
			assertTrue("ProductCode " + code + " was not found in list of purchased codes", purchasedProductCodes.contains(code));
		}
	}

	/**
	 * Tests that while searching for the products bought by a user given a start and end date, failed orders are excluded.
	 */
	@Test
	@DirtiesDatabase
	public void testFindProductCodesPurchasedByUserAndFromDateExcludingFailedOrders() {
		// construct and save new shopping cart
		Order order = createOrder();
		order.failOrder();
		order = orderService.update(order);

		// two shipments should have been created
		List<OrderShipment> shipments = order.getAllShipments();
		assertThat(shipments).hasSize(1);

		// Make sure we get that list back
		final int fourMinutesInMillis = 240000;
		PurchaseHistorySearchCriteria criteria = createPurchaseHistorySearchCriteria(new Date(currentTimeMillis() - fourMinutesInMillis),
				// from 4 minutes ago
				new Date()); // now

		List<String> purchasedProductCodes = orderService.findProductCodesPurchasedByUser(criteria);
		assertNotNull(purchasedProductCodes);
		assertTrue("The product codes for a failed order should not be retrieved.", purchasedProductCodes.isEmpty());
	}

	/**
	 * Tests that you can find products codes for skus that have been purchased by a user.
	 */
	@Test
	@DirtiesDatabase
	public void testFindProductCodesPurchasedByUser() {
		// construct and save new shopping cart
		Order order = createOrder();

		// two shipments should have been created
		List<OrderShipment> shipments = order.getAllShipments();
		assertThat(shipments).hasSize(1);

		// Make a list of productCodes that were in the shopping cart
		List<String> orderProductCodes = new ArrayList<>();
		for (OrderSku orderSku : shipments.get(0).getShipmentOrderSkus()) {
			ProductSku sku = productSkuLookup.findByGuid(orderSku.getSkuGuid());
			orderProductCodes.add(sku.getProduct().getCode());
		}

		// Make sure we get that list back
		PurchaseHistorySearchCriteria criteria = createPurchaseHistorySearchCriteria(null, //no from date
				new Date()); // now

		List<String> purchasedProductCodes = orderService.findProductCodesPurchasedByUser(criteria);
		assertNotNull(purchasedProductCodes);

		for (String code : orderProductCodes) {
			assertTrue("ProductCode " + code + " was not found in list of purchased codes", purchasedProductCodes.contains(code));
		}
	}

	/**
	 * Tests that while searching for the products bought by a user, failed orders are excluded.
	 */
	@Test
	@DirtiesDatabase
	public void testFindProductCodesPurchasedByUserExcludeFailedOrder() {
		// construct and save new shopping cart
		Order order = createOrder();
		order.failOrder();
		orderService.update(order);

		// Make sure we get that list back
		PurchaseHistorySearchCriteria criteria = createPurchaseHistorySearchCriteria(null, //no from date
				new Date()); // now

		List<String> purchasedProductCodes = orderService.findProductCodesPurchasedByUser(criteria);
		assertNotNull(purchasedProductCodes);
		assertTrue("The product codes for a failed order should not be retrieved.", purchasedProductCodes.isEmpty());
	}

	/**
	 * Test that findOrderByCustomerGuid finds regular orders but excludes failed orders.
	 */
	@Test
	@DirtiesDatabase
	public void testFindOrderByCustomerGuid() {
		Order order = createOrder();

		String customerGuid = shopper.getCustomer().getGuid();
		List<Order> orderByCustomerGuid = orderService.findOrderByCustomerGuid(customerGuid, true);
		assertThat(orderByCustomerGuid).as("There should be 1 order found").hasSize(1);
		assertThat(orderByCustomerGuid.get(0)).as("The order should be the one we created").isEqualTo(order);
		List<Order> orderByOtherCustomer = orderService.findOrderByCustomerGuid(INVALID_GUID, true);
		assertTrue("There should be no orders found when customer guid does not match.", orderByOtherCustomer.isEmpty());

		order.failOrder();
		orderService.update(order);

		orderByCustomerGuid = orderService.findOrderByCustomerGuid(customerGuid, true);
		assertTrue("There should be no orders found", orderByCustomerGuid.isEmpty());
	}

	/**
	 * Test that findOrderByAccountGuid finds regular orders but excludes failed orders.
	 */
	@Test
	public void testFindOrderByAccountGuid() {
		Customer account = persisterFactory.getStoreTestPersister().persistAccount("accountGuid");
		shopper.setAccount(account);

		Order order = createOrder();

		String accountGuid = shopper.getAccount().getGuid();
		List<Order> orderByAccountGuid = orderService.findOrderByAccountGuid(accountGuid, true);
		assertThat(orderByAccountGuid).as("There should be 1 order found").hasSize(1);
		assertThat(orderByAccountGuid.get(0)).as("The order should be the one we created").isEqualTo(order);
		List<Order> orderByOtherCustomer = orderService.findOrderByAccountGuid(INVALID_GUID, true);
		assertTrue("There should be no orders found when customer guid does not match.", orderByOtherCustomer.isEmpty());

		order.failOrder();
		orderService.update(order);

		orderByAccountGuid = orderService.findOrderByAccountGuid(accountGuid, true);
		assertTrue("There should be no orders found", orderByAccountGuid.isEmpty());
	}

	/**
	 * Test that failOrder sets the cart order guid to null.
	 */
	@Test
	@DirtiesDatabase
	public void testFailedOrderShouldHaveNullCartOrderGuid() {
		Order order = createOrder();

		order.failOrder();
		orderService.update(order);
		assertNull("The Cart Order Guid for a failed Order should be null", order.getCartOrderGuid());
	}

	/**
	 * Test that findOrderByCustomerGuidAndStoreCode finds regular orders but excludes failed orders.
	 */
	@Test
	@DirtiesDatabase
	public void testFindOrderByCustomerGuidAndStoreCode() {
		Order order = createOrder();

		String customerGuid = shopper.getCustomer().getGuid();
		List<Order> orderByCustomerGuid = orderService.findOrdersByCustomerGuidAndStoreCode(customerGuid, store.getCode(), true);
		assertThat(orderByCustomerGuid).as("There should be 1 order found").hasSize(1);
		assertThat(orderByCustomerGuid.get(0)).as("The order should be the one we created").isEqualTo(order);
		List<Order> orderByOtherCustomer = orderService.findOrdersByCustomerGuidAndStoreCode(INVALID_GUID, store.getCode(), true);
		assertTrue("There should be no orders found when customer guid does not match.", orderByOtherCustomer.isEmpty());

		order.failOrder();
		orderService.update(order);

		orderByCustomerGuid = orderService.findOrdersByCustomerGuidAndStoreCode(customerGuid, store.getCode(), true);
		assertTrue("There should be no orders found", orderByCustomerGuid.isEmpty());
	}

	/**
	 * Test find order by customer guid and store code without full info.
	 */
	@Test
	@DirtiesDatabase
	public void testFindOrderByCustomerGuidAndStoreCodeWithoutFullInfo() {
		Order order = createOrder();

		String customerGuid = shopper.getCustomer().getGuid();
		List<Order> retrievedOrders = orderService.findOrdersByCustomerGuidAndStoreCode(customerGuid, store.getCode(), false);
		assertThat(retrievedOrders).as("There should be 1 order found").hasSize(1);

		Order retrievedOrder = retrievedOrders.get(0);
		assertThat(retrievedOrder.getOrderNumber()).as("The order number should be the one we created").isEqualTo(order.getOrderNumber());
		assertThat(retrievedOrder.getStore().getName()).as("The order store name should be the one we created").isEqualTo(order.getStore().getName());
		assertThat(retrievedOrder.getStore().getUrl()).as("The order store url should be the one we created").isEqualTo(order.getStore().getUrl());
		assertThat(retrievedOrder.getTotal()).as("The order total should be the one we created").isEqualTo(order.getTotal());
		assertThat(retrievedOrder.getCreatedDate()).as("The order created date should be the one we created").isEqualTo(order.getCreatedDate());
		assertThat(retrievedOrder.getStatus()).as("The order status should be the one we created").isEqualTo(order.getStatus());

		List<Order> orderByOtherCustomer = orderService.findOrdersByCustomerGuidAndStoreCode(INVALID_GUID, store.getCode(), false);
		assertTrue("There should be no orders found when customer guid does not match.", orderByOtherCustomer.isEmpty());
	}

	/**
	 * Test that getAccountGuidAssociatedWithOrderNumber finds the account guid for given order number.
	 */
	@Test
	public void testGetAccountGuidAssociatedWithOrderNumber() {
		Customer account = persisterFactory.getStoreTestPersister().persistAccount("accountGuid");
		shopper.setAccount(account);

		Order order = createOrder();
		String orderNumber = order.getOrderNumber();
		String accountGuid = shopper.getAccount().getGuid();

		assertThat(orderService.getAccountGuidAssociatedWithOrderNumber(orderNumber)).isEqualTo(accountGuid);
	}

	private Order createOrder() {
		ShoppingCart shoppingCart = persisterFactory.getOrderTestPersister().persistEmptyShoppingCart(shopper, address, address,
				scenario.getShippingOption(), scenario.getStore());
		ShoppingItemDto dto = new ShoppingItemDto(product.getDefaultSku().getSkuCode(), 1);
		cartDirector.addItemToCart(shoppingCart, dto);

		// checkout
		assertCustomerEmailEqualsShopperCustomerEmail(shopper);

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);

		List<Order> ordersList;
		// only one order should have been created by the checkout service
		if(Objects.nonNull(shopper.getAccount())){
			ordersList = orderService.findOrderByAccountGuid(shopper.getAccount().getGuid(), true);
		} else {
			ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		}
		assertThat(ordersList).hasSize(1);
		return ordersList.iterator().next();
	}

	@Test
	public void testHoldAndReleaseHoldOnOrder() {
		// construct and save new shopping cart
		ShoppingCart shoppingCart = givenASimpleShoppingCart();

		// checkout
		assertCustomerEmailEqualsShopperCustomerEmail(shopper);

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);

		// only one order should have been created by the checkout service
		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		assertThat(ordersList).hasSize(1);
		Order order = ordersList.iterator().next();

		// two shipments should have been created
		List<OrderShipment> shipments = order.getAllShipments();
		assertThat(shipments).hasSize(1);

		// check payments
		final Collection<OrderPayment> payments = orderPaymentService.findByOrder(order);
		assertThat(payments).hasSize(1);
		OrderPayment authPayment = payments.iterator().next();
		assertThat(authPayment.getAmount().doubleValue()).isEqualTo(order.getTotal().doubleValue());

		assertThat(order.getStatus()).isEqualTo(IN_PROGRESS);
		order = orderService.get(order.getUidPk());
		assertThat(order.getStatus()).isEqualTo(IN_PROGRESS);
	}

	/**
	 * Tests release shipment with product which is available only when in stock.
	 */
	@Test
	@DirtiesDatabase
	public void testReleaseOrderShipmentProductInStock() {
		releaseOrderShipmentWithProductAvailability(AvailabilityCriteria.AVAILABLE_WHEN_IN_STOCK);
	}

	/**
	 * Tests release of an order shipment.
	 * <p>
	 * Scenario:
	 * 1. Create shopping cart with one product
	 * 2. Checkout the shopping cart
	 * 3. ???
	 *
	 * @param availabilityCriteria availability criteria
	 */
	public void releaseOrderShipmentWithProductAvailability(final AvailabilityCriteria availabilityCriteria) {
		product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(), scenario.getCategory(),
				scenario.getWarehouse());
		product.setAvailabilityCriteria(availabilityCriteria);
		product = productService.saveOrUpdate(product);

		final int qty5 = 5;
		InventoryDto inventoryDto = productInventoryManagementService.getInventory(
				product.getDefaultSku(),
				scenario.getWarehouse().getUidPk());
		productInventoryManagementService.saveOrUpdate(inventoryDto);

		InventoryAudit inventoryAudit = buildInventoryAudit(qty5 - inventoryDto.getQuantityOnHand());
		productInventoryManagementService.processInventoryUpdate(inventoryDto, inventoryAudit);
		inventoryDto = productInventoryManagementService.getInventory(inventoryDto.getSkuCode(), inventoryDto.getWarehouseUid());

		assertNotNull(inventoryDto);
		assertThat(inventoryDto.getQuantityOnHand()).isEqualTo(qty5);
		assertThat(product.getAvailabilityCriteria()).isEqualTo(availabilityCriteria);

		ShoppingCart shoppingCart = persisterFactory.getOrderTestPersister().persistEmptyShoppingCart(shopper, address, address,
				scenario.getShippingOption(), scenario.getStore());
		ShoppingItemDto dto = new ShoppingItemDto(product.getDefaultSku().getSkuCode(), 2);
		cartDirector.addItemToCart(shoppingCart, dto);

		// checkout
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);

		inventoryDto = productInventoryManagementService.getInventory(
				inventoryDto.getSkuCode(), inventoryDto.getWarehouseUid());

		// check allocation status
		assertThat(inventoryDto.getAvailableQuantityInStock()).isEqualTo(qty5 - 2);
		assertThat(inventoryDto.getAllocatedQuantity()).isEqualTo(2);

		Order order = shoppingCart.getCompletedOrder();

		List<PhysicalOrderShipment> physicalShipments = order.getPhysicalShipments();
		assertThat(physicalShipments).hasSize(1);
		PhysicalOrderShipment shipment = physicalShipments.iterator().next();

		Set<OrderSku> shipmentOrderSkus = shipment.getShipmentOrderSkus();
		assertThat(shipmentOrderSkus).as("One product was checked out only").hasSize(1);
		OrderSku orderSku = shipmentOrderSkus.iterator().next();
		assertThat(orderSku.getAllocatedQuantity()).as("Quantity of two products was checked out").isEqualTo(2);
		inventoryDto = productInventoryManagementService.getInventory(
				orderSku.getSkuCode(),
				scenario.getWarehouse().getUidPk());

		// check allocation status
		assertThat(inventoryDto.getAllocatedQuantity()).as("Two allocated quantities expected").isEqualTo(2);

		order = orderService.get(order.getUidPk());

		assertThat(order.getAllShipments().iterator().next().getShipmentStatus()).isEqualTo(INVENTORY_ASSIGNED);

		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		shipment = (PhysicalOrderShipment) order.getAllShipments().iterator().next();

		shipment = (PhysicalOrderShipment) orderService.processReleaseShipment(shipment);

		assertThat(shipment.getShipmentStatus()).isEqualTo(RELEASED);

		orderSku = shipment.getShipmentOrderSkus().iterator().next();
		InventoryDto inventory2 = productInventoryManagementService.getInventory(
				orderSku.getSkuCode(),
				scenario.getWarehouse().getUidPk());
		inventory2 = productInventoryManagementService.getInventory(
				inventory2.getSkuCode(), inventory2.getWarehouseUid());

		assertThat(orderSku.getAllocatedQuantity()).isEqualTo(2);
		assertThat(inventory2.getAllocatedQuantity()).isEqualTo(2);

		inventory2 = productInventoryManagementService.getInventory(
				inventory2.getSkuCode(), inventory2.getWarehouseUid());

		// The allocated quantity and quantity on hand should not be changed in orderService.processReleaseShipment,
		// we only change them in orderService.completeShipment.
		assertThat(inventory2.getAllocatedQuantity()).isEqualTo(2);
		assertThat(inventory2.getQuantityOnHand()).isEqualTo(inventoryDto.getQuantityOnHand());
	}

	private InventoryAudit buildInventoryAudit(final int quantity) {
		InventoryAudit inventoryAudit = new InventoryAuditImpl();
		inventoryAudit.setEventType(InventoryEventType.STOCK_ADJUSTMENT);
		inventoryAudit.setQuantity(quantity);
		return inventoryAudit;
	}

	// =================== UTILITY METHODS ========================= \\

	private PurchaseHistorySearchCriteria createPurchaseHistorySearchCriteria(final Date fromDate, final Date toDate) {
		PurchaseHistorySearchCriteria criteria = new PurchaseHistorySearchCriteriaImpl();
		criteria.setUserId(shopper.getCustomer().getUserId());
		criteria.setStoreCode(store.getCode());
		criteria.setFromDate(fromDate); // from 4 minutes ago
		criteria.setToDate(toDate);
		return criteria;
	}

	private OrderSku getNewProductOrderSku(final long orderId) {
		Product newProduct = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(
				scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse());

		final OrderSku orderSku = getBeanFactory().getPrototypeBean(ORDER_SKU, OrderSku.class);

		final ProductSku productSku = newProduct.getDefaultSku();

		final Price price = getBeanFactory().getPrototypeBean(PRICE, Price.class);
		final Money amount = Money.valueOf(BigDecimal.ONE, Currency.getInstance("USD"));
		price.setListPrice(amount);
		orderSku.setPrice(1, price);
		orderSku.setUnitPrice(BigDecimal.ONE);
		final Date now = new Date();
		orderSku.setCreatedDate(now);
		final int qty3 = 3;
		orderSku.setQuantity(qty3);
		orderSku.setSkuCode(productSku.getSkuCode());
		orderSku.setSkuGuid(productSku.getGuid());
		orderSku.setDigitalAsset(productSku.getDigitalAsset());
		orderSku.setTaxCode(newProduct.getTaxCodeOverride().getCode());
		orderSku.setTaxAmount(BigDecimal.ONE);
		orderSku.setAllocatedQuantity(qty3);

		if (productSku.getImage() != null) {
			orderSku.setImage(productSku.getImage());
		}

		orderSku.setDisplayName("product_name2");
		orderSku.setOrderUidPk(orderId);
		return orderSku;
	}

	private void assertCustomerEmailEqualsShopperCustomerEmail(final Shopper shopper) {
		assertThat(shopper.getCustomer().getEmail()).isEqualTo(customer.getEmail());
	}

	/**
	 * getEventOriginatorHelper.
	 *
	 * @return EventOriginatorHelper
	 */
	public EventOriginatorHelper getEventOriginatorHelper() {
		return getBeanFactory().getSingletonBean(EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class);
	}

	/**
	 * Test that finding by criteria with a store code works as expected.
	 */
	@Test
	@DirtiesDatabase
	public void testFindBySearchCriteriaStoreCode() {
		Order order = persistOrder(product);
		OrderSearchCriteria criteria = getBeanFactory().getPrototypeBean(ORDER_SEARCH_CRITERIA, OrderSearchCriteria.class);
		Set<String> storeCodes = new HashSet<>();
		storeCodes.add(scenario.getStore().getCode());
		criteria.setStoreCodes(storeCodes);

		List<Order> results = orderService.findOrdersBySearchCriteria(criteria, 0, MAX_RESULTS);
		assertThat(results).as("One order should have been found").hasSize(1);
		assertThat(results.get(0)).as("The search result should be the expected order").isEqualTo(order);
	}

	/**
	 * Test that finding by criteria with a store code works as expected.
	 */
	@Test
	@DirtiesDatabase
	public void testFindBySearchCriteriaStoreCodeWithExcludedOrderStatusSetToFailedOrder() {
		createAndPersistFailedOrder();

		OrderSearchCriteria criteria = getBeanFactory().getPrototypeBean(ORDER_SEARCH_CRITERIA, OrderSearchCriteria.class);
		criteria.setExcludedOrderStatus(FAILED);
		Set<String> storeCodes = new HashSet<>();
		storeCodes.add(scenario.getStore().getCode());
		criteria.setStoreCodes(storeCodes);

		List<Order> results = orderService.findOrdersBySearchCriteria(criteria, 0, MAX_RESULTS);
		assertThat(results).as("No order should have been found").hasSize(0);
	}

	/**
	 * Test that finding by criteria with a store code works as expected.
	 */
	@Test
	@DirtiesDatabase
	public void testFindBySearchCriteriaStoreCodeWithoutExcludedOrderStatusSetToFailedOrder() {
		Order order = createAndPersistFailedOrder();

		OrderSearchCriteria criteria = getBeanFactory().getPrototypeBean(ORDER_SEARCH_CRITERIA, OrderSearchCriteria.class);
		Set<String> storeCodes = new HashSet<>();
		storeCodes.add(scenario.getStore().getCode());
		criteria.setStoreCodes(storeCodes);

		List<Order> results = orderService.findOrdersBySearchCriteria(criteria, 0, MAX_RESULTS);
		assertThat(results).as("One order should have been found").hasSize(1);
		assertThat(results.get(0)).as("The search result should be the expected order").isEqualTo(order);
	}

	private Order createAndPersistFailedOrder() {
		Order order = createOrder();

		order.failOrder();

		return orderService.update(order);
	}

	/**
	 * Test that getting the count by criteria with a store code works as expected.
	 */
	@Test
	@DirtiesDatabase
	public void testGetCountBySearchCriteriaStoreCode() {
		persistOrder(product);
		OrderSearchCriteria criteria = getBeanFactory().getPrototypeBean(ORDER_SEARCH_CRITERIA, OrderSearchCriteria.class);
		Set<String> storeCodes = new HashSet<>();
		storeCodes.add(scenario.getStore().getCode());
		criteria.setStoreCodes(storeCodes);

		long count = orderService.getOrderCountBySearchCriteria(criteria);
		assertThat(count).as("One order should have been found").isEqualTo(1);
	}

	private Order persistOrder(final Product product) {
		Order order = persisterFactory.getOrderTestPersister().createOrderWithSkus(scenario.getStore(), product.getDefaultSku());
		assertThat(order.getStatus()).as("Order status should be in progress").isEqualTo(IN_PROGRESS);
		return order;
	}

	private long getCount(final Class<?> entityClass) {
		PersistenceEngine persistenceEngine = getBeanFactory().getSingletonBean(PERSISTENCE_ENGINE, PersistenceEngine.class);
		String entityName = entityClass.getSimpleName();
		String query = "SELECT COUNT(o.uidPk) FROM " + entityName + " o";
		List<Long> retrieve = persistenceEngine.retrieve(query);
		return retrieve.get(0);
	}

	private void assertExpectedPersistedInstances(final long count, final Class<?> entityClass) {
		assertThat(getCount(entityClass)).as("The number of persisted instances of " + entityClass + " should be as expected").isEqualTo(count);
	}

	/**
	 * Test finding the latest order GUID by cart order GUID when no cart orders exist.
	 */
	@Test
	@DirtiesDatabase
	public void testFindLatestOrderGuidByCartOrderGuidWhenNoCartOrdersExist() {
		// create an order with no cart order GUID assigned
		persistOrder(product);
		String result = orderService.findLatestOrderGuidByCartOrderGuid(NON_EXISTENT_CART_ORDER_GUID);
		assertNull("There should be no order guid returned.", result);
	}

	/**
	 * Test finding an order by cart order GUID will not return an order that does not match the cart order GUID.
	 */
	@Test
	@DirtiesDatabase
	public void testFindLatestOrderGuidByCartOrderGuidWhenManyOrdersExistForACartOrderButDoesNotMatchCartOrderGuidArgument() {
		createOrderWithCartOrderGuid(product, CART_ORDER_GUID);
		String result = orderService.findLatestOrderGuidByCartOrderGuid(DIFFERENT_CART_ORDER_GUID);
		assertNull("There should be no order guid returned.", result);
	}

	/**
	 * Tests {@link OrderService#findOrderNumbersByCustomerGuid(String, String)} for the main flow.
	 */
	@Test
	@DirtiesDatabase
	public void testFindOrderNumbersByCustomerGuid() {
		String customerGuid = customer.getGuid();
		String storeCode = store.getCode();

		List<String> orderNumbersBefore = orderService.findOrderNumbersByCustomerGuid(storeCode, customerGuid);
		assertTrue("No orders should be found as none is created yet.", orderNumbersBefore.isEmpty());

		Order order = createOrder();

		List<String> orderNumbers = orderService.findOrderNumbersByCustomerGuid(storeCode, customerGuid);
		assertThat(orderNumbers).as("There should be exactly one order found.").hasSize(1);
		assertTrue("The order number should match the one we just created.", orderNumbers.contains(order.getOrderNumber()));
	}

	/**
	 * Tests {@link OrderService#findOrderNumbersByCustomerGuid(String, String)} for the main flow.
	 */
	@Test
	@DirtiesDatabase
	public void testFindOrderNumbersByCustomerGuidInvalidCustomerGuid() {
		String storeCode = store.getCode();
		createOrder();

		List<String> orderNumbers = orderService.findOrderNumbersByCustomerGuid(storeCode, "INVALID CUSTOMER GUID");
		assertTrue("No order should be found, since the customer guid does not match the GUID of the order's owner",
				orderNumbers.isEmpty());
	}

	/**
	 * Tests {@link OrderService#findOrderNumbersByCustomerGuid(String, String)} for the main flow.
	 */
	@Test
	@DirtiesDatabase
	public void testFindOrderNumbersByCustomerGuidInvalidStoreCode() {
		String customerGuid = customer.getGuid();
		createOrder();

		List<String> orderNumbers = orderService.findOrderNumbersByCustomerGuid("INVALID_STORE_CODE", customerGuid);
		assertTrue("No order should be found, since the store code does not match that of the order.",
				orderNumbers.isEmpty());
	}

	@DirtiesDatabase
	@Test
	public void verifyProcessOrderShipmentPaymentHappyPath() {
		// Given
		Order order = createOrder();
		OrderShipment orderShipment = order.getAllShipments().get(0);

		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		orderService.processReleaseShipment(orderShipment);

		// When
		orderService.processOrderShipmentPayment(orderShipment.getShipmentNumber());

		// Then
		final Set<OrderPayment> processedPayments = orderPaymentService.findByOrder(order)
				.stream()
				.filter(payment -> payment.getTransactionType() == TransactionType.CHARGE)
				.collect(Collectors.toSet());

		assertThat(processedPayments).as("One payments should have been processed").hasSize(1);
	}

	@DirtiesContext
	@Test(expected = EpSystemException.class)
	public void verifyExceptionThrownWhenCamelUnavailableOnOrderCreation() throws Exception {
		camelContext.stop();
		createOrder();
	}

	@Test
	@DirtiesDatabase
	public void messageDeliveredToJmsQueueOnCheckout() throws Exception {
		camelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(orderEventOutgoingEndpoint)
						.unmarshal(eventMessageDataFormat)
						.to("mock:orders/events");
			}
		});

		final NotifyBuilder notifyBuilder = new NotifyBuilder(camelContext)
				.whenDone(1)
				.wereSentTo("mock:orders/events")
				.create();

		final Order order = createOrder();

		assertTrue(notifyBuilder.matches(1, TimeUnit.SECONDS));

		mockOrderEventEndpoint.message(1)
				.body(EventMessage.class)
				.isEqualTo(eventMessageFactory.createEventMessage(OrderEventType.ORDER_CREATED, order.getGuid()));
		mockOrderEventEndpoint.message(0)
				.body(EventMessage.class)
				.isEqualTo(eventMessageFactory.createEventMessage(OrderEventType.ORDER_ACCEPTED, order.getGuid()));
		mockOrderEventEndpoint.assertIsSatisfied();
	}

	@Test
	@DirtiesDatabase
	public void heldOrderCreatedAndReleasedInDetachedState() {
		// Given
		ShoppingCart shoppingCart = givenASimpleShoppingCart();

		// When
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartWithHold(shoppingCart, taxSnapshot, shopper.getCustomerSession(), true);

		// Then
		// only one order should have been created by the checkout
		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		assertThat(ordersList).hasSize(1);
		Order order = ordersList.iterator().next();

		// one shipment should have been created
		List<OrderShipment> shipments = order.getAllShipments();
		assertThat(shipments).hasSize(1);

		// Order is on hold
		assertThat(order.getStatus()).isEqualTo(ONHOLD);

		// Detach and Release order
		entityManager.detach(order);

		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		orderService.triggerPostCaptureCheckout(order);
		order = checkoutHelper.finalizeOrder(order);


		assertThat(order.getStatus()).isEqualTo(IN_PROGRESS);

	}

	/**
	 * Tests that orders are properly placed on hold.
	 */
	@Test
	@DirtiesDatabase
	public void testAddOrderHold() {
		// construct and save new shopping cart
		final Order order = createOrder();
		final OrderHold orderHold = createOrderHold();
		orderHoldService.addHoldsToOrder(order, Collections.singleton(orderHold));

		final Order updatedOrder  = orderService.update(order);

		assertThat(updatedOrder.getAllShipments()).size().isGreaterThan(0);

		final List<OrderHold> orderHolds = orderHoldService.findOrderHoldsByOrderUid(order.getUidPk());
		assertThat(orderHolds).size().isEqualTo(1);
		final OrderHold addedOrderHold = orderHolds.iterator().next();
		assertThat(addedOrderHold.getCreationDate()).isNotNull();
		assertThat(addedOrderHold.getGuid()).isNotEmpty();
	}

	private void cleanUpReversibleCheckoutAction(final ReversibleCheckoutAction actionToRemove) {
		reversibleCheckoutActions.remove(actionToRemove);
	}

	private void whenCheckoutIsDone(final ShoppingCart shoppingCart) {
		// checkout
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);
	}

	private ShoppingCart givenASimpleShoppingCart() {
		ShoppingCart shoppingCart = persisterFactory.getOrderTestPersister().persistEmptyShoppingCart(shopper, address, address,
				scenario.getShippingOption(), scenario.getStore());
		ShoppingItemDto shoppingItemDto = new ShoppingItemDto(product.getDefaultSku().getSkuCode(), 1);
		cartDirector.addItemToCart(shoppingCart, shoppingItemDto);
		return shoppingCart;
	}

	private Order createOrderWithCartOrderGuid(final Product product, final String cartOrderGuid) {
		Order order = persistOrder(product);
		order.setCartOrderGuid(cartOrderGuid);
		return orderService.update(order);
	}

	private OrderHold createOrderHold() {

		final OrderHold orderHold = getBeanFactory().getPrototypeBean(ContextIdNames.ORDER_HOLD, OrderHold.class);
		orderHold.setResolvedDate(new Date());
		//orderHold.setCreateDate(new Date());
		orderHold.setReviewerNotes("testReviewNotes");
		orderHold.setResolvedBy(null);
		orderHold.setStatus(OrderHoldStatus.ACTIVE);
		orderHold.setHoldDescription("testHoldDesc");
		orderHold.setPermission("testPermission");
		return orderHold;
	}

}
