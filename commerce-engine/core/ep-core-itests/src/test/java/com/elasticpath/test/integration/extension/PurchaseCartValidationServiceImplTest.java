package com.elasticpath.test.integration.extension;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.customer.impl.CustomerSessionImpl;
import com.elasticpath.domain.factory.TestShopperFactoryForTestApplication;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.shoppingcart.validation.impl.PurchaseCartValidationServiceImpl;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

public class PurchaseCartValidationServiceImplTest extends BasicSpringContextTest {

	@Autowired
	private PurchaseCartValidationServiceImpl purchaseCartValidationService;

	private SimpleStoreScenario scenario;
	private ShoppingCart shoppingCart;
	private Store store;
	private Shopper shopper;

	@Before
	public void setUp() throws Exception {
		scenario = getTac().useScenario(SimpleStoreScenario.class);

		Catalog catalog = scenario.getCatalog();

		store = createStore(catalog);

		Customer customer = createCustomer();

		shopper = createShopper(customer, store);

		shoppingCart = createShoppingCart(shopper, store);
	}

	private Store createStore(Catalog catalog) {
		store = scenario.getStore();
		store.setCatalog(catalog);

		return store;
	}

	private ShoppingCart createShoppingCart(final Shopper shopper, final Store store) {
		final ShoppingCart shoppingCart = getBeanFactory().getPrototypeBean(ContextIdNames.SHOPPING_CART, ShoppingCart.class);
		shoppingCart.setStore(store);
		shoppingCart.setShopper(shopper);
		shoppingCart.setDefault(true);
		return shoppingCart;
	}

	private Shopper createShopper(final Customer customer, final Store store) {
		final Shopper shopper = TestShopperFactoryForTestApplication.getInstance().createNewShopperWithMemento();
		shopper.setCustomer(customer);
		shopper.setStoreCode(store.getCode());

		CustomerSession customerSession = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER_SESSION, CustomerSession.class);
		customerSession.setCurrency(Currency.getInstance("USD"));
		customerSession.setLocale(Locale.CANADA);
		shopper.setCustomerSession(customerSession);

		return shopper;
	}

	private Customer createCustomer() {
		CustomerImpl customer = new CustomerImpl();
		customer.setCustomerType(CustomerType.SINGLE_SESSION_USER);
		customer.setGuid(UUID.randomUUID().toString());
		customer.setSharedId("shared-id-" + customer.getGuid());
		return customer;
	}

	@Test
	@DirtiesDatabase
	public void testPurchaseCartValidation() {
		Collection<StructuredErrorMessage> validateMessages = purchaseCartValidationService.validate(shoppingCart, shopper, store);

		assertEquals(3, validateMessages.size());
	}
}
