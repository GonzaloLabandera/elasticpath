/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.uat.stepdefs;

import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT_INSTRUMENT;
import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_INSTRUMENT;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.apache.camel.builder.NotifyBuilder;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.builder.OrderBuilder;
import com.elasticpath.domain.builder.checkout.CheckoutTestCartBuilder;
import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.builder.shopper.ShoppingContextBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.domain.shipping.ShipmentType;
import com.elasticpath.email.test.support.EmailSendingMockInterceptor;
import com.elasticpath.persister.ShoppingContextPersister;
import com.elasticpath.provider.payment.domain.PaymentInstrument;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.impl.OrderPaymentApiServiceImpl;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.uat.ScenarioContextValueHolder;

/**
 * Step Definitions for Order-based functionality.
 */
public class OrderStepDefinitions {

	private static final long MAX_SECONDS_TO_WAIT_FOR_EMAIL = 20;
	private static final String INSTRUMENT_NAME = "Instrument name";
	private static final BigDecimal ZERO = BigDecimal.ZERO;
	private static final Currency CURRENCY_USD = Currency.getInstance("USD");

	@Autowired
	private CustomerService customerService;

	@Autowired
	private OrderBuilder orderBuilder;

	@Autowired
	private OrderService orderService;

	@Autowired
	@Qualifier("customerBuilderHolder")
	private ScenarioContextValueHolder<CustomerBuilder> customerBuilderHolder;

	@Autowired
	@Qualifier("customerHolder")
	private ScenarioContextValueHolder<Customer> customerHolder;

	@Autowired
	@Qualifier("storeScenarioHolder")
	private ScenarioContextValueHolder<SimpleStoreScenario> scenarioHolder;

	@Autowired
	@Qualifier("orderHolder")
	private ScenarioContextValueHolder<Order> orderHolder;

	@Autowired
	@Qualifier("checkoutTestCartBuilderHolder")
	private ScenarioContextValueHolder<CheckoutTestCartBuilder> checkoutTestCartBuilderHolder;

	@Autowired
	private EventOriginatorHelper eventOriginatorHelper;

	@Autowired
	private EmailSendingMockInterceptor emailSendingMockInterceptor;

	@Autowired
	private ScenarioContextValueHolder<Runnable> emailSendingCommandHolder;

	@Autowired
	private ShoppingContextBuilder shoppingContextBuilder;

	@Autowired
	private ShoppingContextPersister shoppingContextPersister;

	@Autowired
	private OrderPaymentInstrumentService orderPaymentInstrumentService;

	@Autowired
	private PaymentInstrumentService paymentInstrumentService;

	@Autowired
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	@Autowired
	private OrderPaymentApiServiceImpl orderPaymentApiService;

	@Autowired
	@Qualifier("coreBeanFactory")
	private BeanFactory beanFactory;

	/**
	 * Build and checkout a cart.
	 *
	 * @throws Exception
	 */
	@When("^I successfully purchase my shopping cart contents$")
	public void createOrder() throws Exception {
		// Defer execution until we are ready to check for the email
		emailSendingCommandHolder.set(() -> {
			final Customer customer = buildAndAddCustomer();

			final ShoppingContext shoppingContext = shoppingContextBuilder
					.withCustomer(customer)
					.withStoreCode(customer.getStoreCode())
					.build();
			shoppingContextPersister.persist(shoppingContext);

			final CheckoutTestCartBuilder checkoutTestCartBuilder = checkoutTestCartBuilderHolder.get()
					.withScenario(scenarioHolder.get())
					.withShopper(shoppingContext.getShopper());

				orderHolder.set(orderBuilder.withCheckoutTestCartBuilder(checkoutTestCartBuilder)
										.withShoppingContext(shoppingContext)
										.checkout());
		});
	}

	/**
	 * Build and checkout a cart that will trigger a pending order hold.
	 */
	@When("^I successfully purchase my shopping cart contents when order hold is enabled$")
	public void createHeldOrder() {
		final Customer customer = buildAndAddCustomer();

		final ShoppingContext shoppingContext = shoppingContextBuilder
				.withCustomer(customer)
				.withStoreCode(customer.getStoreCode())
				.build();
		shoppingContextPersister.persist(shoppingContext);

		final CheckoutTestCartBuilder checkoutTestCartBuilder = checkoutTestCartBuilderHolder.get()
				.withScenario(scenarioHolder.get())
				.withShopper(shoppingContext.getShopper());

		orderHolder.set(orderBuilder.withCheckoutTestCartBuilder(checkoutTestCartBuilder)
				.withShoppingContext(shoppingContext)
				.checkoutWithHold());
	}

	/**
	 * Mark the hold as unresolvable on the current order.
	 */
	@And("^the order hold is unresolvable$")
	public void theOrderHoldIsUnresolvable() {
		emailSendingCommandHolder.set(() -> orderService.cancelOrder(orderHolder.get()));
	}

	/**
	 * Resend the order confirmation email.
	 */
	@When("^the CSR resends the order confirmation email$")
	public void resendOrderConfirmationEmail() {
		// We need to ensure the previous command is executed for a resend
		emailSendingCommandHolder.get().run();
		// Defer execution until we are ready to check for the email
		emailSendingCommandHolder.set(() -> {
			final String orderNumber = orderHolder.get().getOrderNumber();
			orderService.resendOrderConfirmationEvent(orderNumber);
		});
	}

	/**
	 * Build a cart with physical goods and checkout.
	 */
	@Given("^(?:I have|a customer has) previously made a purchase$")
	public void createInProgressOrder() {
		final Customer customer = buildAndAddCustomer();

		final ShoppingContext shoppingContext = shoppingContextBuilder
				.withCustomer(customer)
				.withStoreCode(customer.getStoreCode())
				.build();
		shoppingContextPersister.persist(shoppingContext);

		final CheckoutTestCartBuilder checkoutTestCartBuilder = checkoutTestCartBuilderHolder.get()
				.withScenario(scenarioHolder.get())
				.withShopper(shoppingContext.getShopper());

		final Order order = orderBuilder.withCheckoutTestCartBuilder(checkoutTestCartBuilder)
				.withNonZeroPhysicalShipment()
				.withShoppingContext(shoppingContext)
				.build();

		orderHolder.set(order);

		final PaymentInstrument paymentInstrument = createPaymentInstrument();
		createOrderPaymentInstrument(order, paymentInstrument.getGuid());

		orderPaymentApiService.orderCreated(order);
	}

	/**
	 * Create a cart and checkout an order.  The order will be placed on hold.
	 */
	@Given("^(?:I have|the customer) made a purchase when order hold enabled$")
	public void createDefaultHeldOrder() {
		final Customer customer = buildAndAddCustomer();

		final ShoppingContext shoppingContext = shoppingContextBuilder
				.withCustomer(customer)
				.withStoreCode(customer.getStoreCode())
				.build();
		shoppingContextPersister.persist(shoppingContext);

		final CheckoutTestCartBuilder checkoutTestCartBuilder = checkoutTestCartBuilderHolder.get()
				.withScenario(scenarioHolder.get())
				.withShopper(shoppingContext.getShopper());

		final Order order = orderBuilder.withCheckoutTestCartBuilder(checkoutTestCartBuilder)
				.withNonZeroPhysicalShipment()
				.withShoppingContext(shoppingContext)
				.checkoutWithHold();
		orderHolder.set(order);
	}

	/**
	 * Complete all the shipments on the order.
	 * @throws Throwable
	 */
	@And("^the purchase has been completed and delivered$")
	public void completeAllOrderShipments() throws Throwable {
		Order order = orderHolder.get();

		final int numberOfPhysicalShipments = getPhysicalShipmentCount(order);
		final NotifyBuilder notifyBuilder = emailSendingMockInterceptor.createNotifyBuilderForEmailSendingMockInterceptor()
				.whenDone(numberOfPhysicalShipments)
				.create();

		final List<OrderShipment> shipments = order.getAllShipments();
		for (final OrderShipment shipment : shipments) {
			if (!shipment.getShipmentStatus().equals(OrderShipmentStatus.SHIPPED)) {
				order = orderService.completeShipment(shipment.getShipmentNumber(),
													  null,
													  true,
													  null,
													  false,
													  eventOriginatorHelper.getSystemOriginator());
			}
		}

		orderHolder.set(order);

		// wait for the order shipment confirmation email(s) to be sent, then clear them
		notifyBuilder.matches(MAX_SECONDS_TO_WAIT_FOR_EMAIL, TimeUnit.SECONDS);

		// Clear the shipment confirmation emails
		Mailbox.clearAll();
	}

	private int getPhysicalShipmentCount(final Order order) {
		int count = 0;

		for (final OrderShipment shipment : order.getAllShipments()) {
			if (shipment.getOrderShipmentType().equals(ShipmentType.PHYSICAL)) {
				count++;
			}
		}

		return count;
	}

	private Customer buildAndAddCustomer() {
		final Customer customer = customerBuilderHolder.get().build();
		final Customer addedCustomer = customerService.add(customer);
		customerHolder.set(addedCustomer);
		return addedCustomer;
	}

	private PaymentInstrument createPaymentInstrument() {
		final PaymentInstrument paymentInstrument = beanFactory.getPrototypeBean(PAYMENT_INSTRUMENT, PaymentInstrument.class);
		paymentInstrument.setName(INSTRUMENT_NAME);
		paymentInstrument.setPaymentProviderConfiguration(paymentProviderConfigurationService.findAll()
				.stream()
				.filter(config -> "paymentProviderPluginForIntegrationTesting".equals(config.getPaymentProviderPluginId()))
				.findAny()
				.orElse(paymentProviderConfigurationService.findAll().get(0)));
		paymentInstrument.setSingleReservePerPI(false);
		paymentInstrument.setPaymentInstrumentData(Collections.emptySet());

		paymentInstrumentService.saveOrUpdate(paymentInstrument);

		return paymentInstrument;
	}

	private void createOrderPaymentInstrument(final Order order, final String giud) {
        final OrderPaymentInstrument orderPaymentInstrument = beanFactory.getPrototypeBean(ORDER_PAYMENT_INSTRUMENT, OrderPaymentInstrument.class);
        orderPaymentInstrument.setPaymentInstrumentGuid(giud);
        orderPaymentInstrument.setLimitAmount(ZERO);
        orderPaymentInstrument.setOrderNumber(order.getOrderNumber());
        orderPaymentInstrument.setCurrency(CURRENCY_USD);
        orderPaymentInstrumentService.saveOrUpdate(orderPaymentInstrument);
    }
}
