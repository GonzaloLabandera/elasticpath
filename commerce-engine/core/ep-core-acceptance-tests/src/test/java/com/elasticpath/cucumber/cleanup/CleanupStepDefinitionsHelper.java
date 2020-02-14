package com.elasticpath.cucumber.cleanup;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import cucumber.api.DataTable;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.cucumber.ScenarioContextValueHolder;
import com.elasticpath.cucumber.shippingservicelevel.ShippingServiceLevelStepDefinitionsHelper;
import com.elasticpath.cucumber.shoppingcart.ShoppingCartStepDefinitionsHelper;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.test.persister.TestApplicationContext;

/**
 * Help class for {@link CleanupStepDefinitions}.
 */
public class CleanupStepDefinitionsHelper {

	@Inject
	@Named("storeHolder")
	private ScenarioContextValueHolder<Store> storeHolder;

	@Inject
	@Named("customerHolder")
	private ScenarioContextValueHolder<Customer> customerHolder;

	@Inject
	@Named("orderHolder")
	private ScenarioContextValueHolder<Order> orderHolder;

	@Autowired
	private ShippingServiceLevelStepDefinitionsHelper shippingServiceLevelStepDefinitionsHelper;

	@Autowired
	private ShoppingCartStepDefinitionsHelper shoppingCartStepDefinitionsHelper;

	@Autowired
	private TestApplicationContext tac;

	@Autowired
	private OrderService orderService;

	private static final String SHIPPING_SERVICE_LEVEL_CODE = "2-Business-Days";

	/**
	 * Adds the payment instrument to the order of the current test context.
	 *
	 * @return the payment instrument guid
	 */
	public String createPaymentInstrument() {
		ShoppingCart shoppingCart = shoppingCartStepDefinitionsHelper.getShoppingCart();
		CartOrderPaymentInstrument cartOrderPaymentInstrument =
				tac.getPersistersFactory().getPaymentInstrumentPersister().persistPaymentInstrument(shoppingCart);
		return cartOrderPaymentInstrument.getGuid();
	}

	/**
	 * Sets up an order in FAILED state for the current test environment.
	 *
	 * @param dataTable the shopping items
	 */
	public void setUpFailedOrder(final DataTable dataTable) {
		setUpShippingServiceLevels();
		customerHolder.set(tac.getPersistersFactory().getStoreTestPersister().createDefaultCustomer(storeHolder.get()));
		shoppingCartStepDefinitionsHelper.setShippingAddress(customerHolder.get().getAddresses().get(0));
		shoppingCartStepDefinitionsHelper.setDeliveryOption(SHIPPING_SERVICE_LEVEL_CODE);
		shoppingCartStepDefinitionsHelper.purchaseItems(shoppingCartStepDefinitionsHelper.convertDataTableToShoppingItemDtos(dataTable));
		Order order = orderHolder.get();
		order.failOrder();
		orderService.update(order);
	}

	/**
	 * Sets up shipping service levels for the current test environment.
	 */
	private void setUpShippingServiceLevels() {
		List<Map<String, String>> serviceLevels = ImmutableList.of(ImmutableMap.of(
				"region", "Canada",
				"shipping service level code", SHIPPING_SERVICE_LEVEL_CODE,
				"price", "10.00"
		));
		shippingServiceLevelStepDefinitionsHelper.setUpShippingServiceLevels(serviceLevels);
	}
}
