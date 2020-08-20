package com.elasticpath.cucumber.cleanup;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.cucumber.ScenarioContextValueHolder;
import com.elasticpath.cucumber.category.CategoryStepDefinitionsHelper;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.jobs.impl.FailedOrdersCleanupJob;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.CartOrderPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.settings.test.support.SimpleSettingValueProvider;

/**
 * Clean up test step definitions class.
 */
public class CleanupStepDefinitions {

	@Autowired
	@Qualifier("cleanupFailedOrdersJob")
	private FailedOrdersCleanupJob failedOrdersCleanupJob;

	@Inject
	@Named("shoppingCartHolder")
	private ScenarioContextValueHolder<ShoppingCart> shoppingCartHolder;

	@Inject
	@Named("customerHolder")
	private ScenarioContextValueHolder<Customer> customerHolder;

	@Inject
	@Named("orderHolder")
	private ScenarioContextValueHolder<Order> orderHolder;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ShopperService shopperService;

	@Autowired
	private ShoppingCartService shoppingCartService;

	@Autowired
	private OrderPaymentInstrumentService orderPaymentInstrumentService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService;

	@Autowired
	private CategoryStepDefinitionsHelper categoryStepDefinitionsHelper;

	@Autowired
	private CleanupStepDefinitionsHelper cleanupStepDefinitionsHelper;

	private static String cartOrderPaymentInstrumentGuid;

	/**
	 * Sets up products for the current test environment.
	 *
	 * @param dataTable the data of products.
	 */
	@Given("^products$")
	public void setUpProducts(final DataTable dataTable) {
		categoryStepDefinitionsHelper.setUpProducts(dataTable.asMaps(String.class, String.class));
	}

	/**
	 * Sets the number of days of failed order history to keep before a Quartz job clears it.
	 *
	 * @param maxHistory the number of days of failed order history.
	 */
	@Given("^the FAILEDORDERCLEANUP maxHistory is (\\d+) day$")
	public void setOrderMaxHistory(final int maxHistory) {
		failedOrdersCleanupJob.setMaxDaysHistoryProvider(new SimpleSettingValueProvider<>(maxHistory));
	}

	/**
	 * Sets up an order in FAILED state for the current test environment.
	 *
	 * @param dataTable the shopping items
	 */
	@And("^the order is in FAILED state$")
	public void setUpFailedOrder(final DataTable dataTable) {
		cleanupStepDefinitionsHelper.setUpFailedOrder(dataTable);
	}

	/**
	 * Adds the payment instrument to the order of the current test context.
	 */
	@And("^the anonymous customer created a payment instrument in order$")
	public void createPaymentInstrument() {
		cartOrderPaymentInstrumentGuid = cleanupStepDefinitionsHelper.createPaymentInstrument();
	}

	/**
	 * Runs the failed orders cleanup job.
	 */
	@When("^the cleanupFailedOrdersJob processes$")
	public void runFailedOrdersCleanupJobProcessor() {
		failedOrdersCleanupJob.removeFailedOrders();
	}

	/**
	 * Verifies that the customer was removed from TCUSTOMER and TSHOPPER table.
	 */
	@Then("^the anonymous customer should be removed from TCUSTOMER, TSHOPPER$")
	public void isCustomerAndShopperRemoved() {
		String customerGuid = customerHolder.get().getGuid();
		Customer customer = customerService.findByGuid(customerGuid);
		Shopper shopper = shopperService.findByCustomerGuid(customerGuid);
		assertNull("The customer should have been null after clean up job processed", customer);
		assertNull("The shopper should have been null after clean up job processed", shopper);
	}

	/**
	 * Verifies that the shopping cart was removed from TSHOPPINGCART table.
	 */
	@Then("^the record from TSHOPPINGCART should be removed$")
	public void isShoppingCartRemoved() {
		ShoppingCart shoppingCart = shoppingCartService.findByGuid(shoppingCartHolder.get().getGuid());
		assertNull("The shopping cart should have been null after clean up job processed", shoppingCart);
	}

	/**
	 * Verifies that the failed order was removed from TORDER table.
	 */
	@Then("^the FAILED orders should be removed from TORDER$")
	public void isOrderRemoved() {
		Order order = orderService.get(orderHolder.get().getUidPk());
		assertNull("The order should have been null after clean up job processed", order);
	}

	/**
	 * Verifies that the payment instrument was removed from TCARTORDERPAYMENTINSTRUMENT table.
	 */
	@And("^the associated payment instrument should be removed from TCARTORDERPAYMENTINSTRUMENT$")
	public void isPaymentInstrumentRemoved() {
		CartOrderPaymentInstrument paymentInstrument = cartOrderPaymentInstrumentService.findByGuid(cartOrderPaymentInstrumentGuid);
		assertNull("The payment instrument should have been null after clean up job processed", paymentInstrument);
	}

	/**
	 * Verifies that the payment instrument was removed from TORDERPAYMENTINSTRUMENT table.
	 */
	@And("^there is no PI saved in TORDERPAYMENTINSTRUMENT for failed orders$")
	public void isOrderPaymentInstrumentRemoved() {
		Collection<OrderPaymentInstrument> orderPaymentInstruments = orderPaymentInstrumentService.findByOrder(orderHolder.get());
		assertTrue("There should be no payment instrument after clean up job processed", orderPaymentInstruments.isEmpty());
	}
}
