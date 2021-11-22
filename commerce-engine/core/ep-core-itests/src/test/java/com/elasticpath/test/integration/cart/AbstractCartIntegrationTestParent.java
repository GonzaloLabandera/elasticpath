/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
/**
 *
 */
package com.elasticpath.test.integration.cart;

import java.math.BigDecimal;
import java.util.Currency;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.pricing.service.PriceListHelperService;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.factory.TestCustomerSessionFactoryForTestApplication;
import com.elasticpath.domain.factory.TestShopperFactoryForTestApplication;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.persister.TaxTestPersister;

/**
 * Abstract class for Cart-related tests.
 */
public abstract class AbstractCartIntegrationTestParent extends DbTestCase {

	@Autowired
	protected ShopperService shopperService;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private BeanFactory beanFactory;


	protected Shopper createShopper() {
		return createShopper(null);
	}

	protected Shopper createShopper(final Customer customer) {
		final Shopper shopper = TestShopperFactoryForTestApplication.getInstance().createNewShopperWithMemento();
		shopper.setCustomer(customer);
		shopperService.save(shopper);

		final CustomerSession customerSession = TestCustomerSessionFactoryForTestApplication.getInstance().createNewCustomerSessionWithContext(
				shopper);

		final PriceListHelperService priceListHelperService = getBeanFactory().getSingletonBean(ContextIdNames.PRICE_LIST_HELPER_SERVICE,
				PriceListHelperService.class);
		final Currency currency = priceListHelperService.getDefaultCurrencyFor(getScenario().getCatalog());
		customerSession.setCurrency(currency);
		return shopper;
	}

	protected ShoppingCart createShoppingCart(final Shopper shopper) {
		final ShoppingCart shoppingCart = getBeanFactory().getPrototypeBean(ContextIdNames.SHOPPING_CART, ShoppingCart.class);
		shoppingCart.setStore(getScenario().getStore());
		shoppingCart.setShopper(shopper);
		shoppingCart.setDefault(true);
		return shoppingCart;
	}

	protected Product persistProductWithSku() {
		final TaxCode taxCode = getPersisterFactory().getTaxTestPersister().getTaxCode(TaxTestPersister.TAX_CODE_GOODS);
		final PriceListHelperService priceListHelperService = getBeanFactory().getSingletonBean(ContextIdNames.PRICE_LIST_HELPER_SERVICE,
				PriceListHelperService.class);
		final Currency currency = priceListHelperService.getDefaultCurrencyFor(getScenario().getCatalog());
		final int orderLimit = Integer.MAX_VALUE;
		return getPersisterFactory().getCatalogTestPersister().persistProductWithSku(
				getScenario().getCatalog(),
				getScenario().getCategory(),
				getScenario().getWarehouse(),
				BigDecimal.TEN,
				currency,
				"brandCode",
				"productCode",
				"productName",
				"skuCode",
				taxCode.getCode(),
				AvailabilityCriteria.ALWAYS_AVAILABLE,
				orderLimit);
	}

	protected Product persistNonShippableProductWithSku() {
		return getPersisterFactory().getCatalogTestPersister().persistNonShippablePersistedProductWithSku(
				getScenario().getCatalog(),
				getScenario().getCategory(),
				getScenario().getWarehouse(),
				BigDecimal.TEN,
				"productName",
				"skuCode");
	}

	protected Customer createPersistedCustomer() {
		return createPersistedCustomer("sharedID", "email", scenario.getStore());
	}

	protected Customer createPersistedCustomer(final String sharedId, final String email, final Store store) {
		final Customer customer = beanFactory.getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setSharedId(sharedId);
		customer.setCustomerType(CustomerType.REGISTERED_USER);
		customer.setEmail(email);
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setStoreCode(store.getCode());

		return customerService.add(customer);
	}

}