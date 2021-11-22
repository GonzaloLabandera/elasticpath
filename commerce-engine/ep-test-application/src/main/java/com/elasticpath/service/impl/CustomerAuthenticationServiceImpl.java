/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.impl;

import com.elasticpath.commons.util.Utility;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.testcontext.ShoppingTestData;
import com.elasticpath.service.CustomerAuthenticationService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.test.common.exception.TestApplicationException;

/**
 * <code>CustomerAuthenticationServiceImpl</code> provides services for managing <code>CustomerSession</code>s.
 */
public class CustomerAuthenticationServiceImpl implements CustomerAuthenticationService {

	private CustomerSessionService customerSessionService;

	private ShoppingCartService shoppingCartService;

	private ShopperService shopperService;

	private CustomerService customerService;

	private Utility utility;

	@Override
	public void loginStore(final Store store, final String userName) {
		loginStoreInternal(store, userName, true);
	}

	private void loginStoreInternal(final Store store, final String userName, final boolean primaryCustomerSession) {
		validate(store, userName);

		final Customer customer = findCustomer(store, userName);
		if (customer == null) {
			throw new TestApplicationException("Couldn't log into store " + store.getCode() + " with user name " + userName);
		}

		handleSignIn(store, customer, primaryCustomerSession);
	}

	private void handleSignIn(final Store store, final Customer customer, final boolean primaryCustomerSession) {
		handleCustomerSignIn(store, customer, primaryCustomerSession);
		ShoppingTestData.getInstance().setStore(store);
	}

	private void validate(final Store store, final String userName) {
		if (store == null) {
			throw new TestApplicationException("Store cannot be null");
		}

		if (userName == null) {
			throw new TestApplicationException("User name cannot be null");
		}
	}

	private Customer findCustomer(final Store store, final String userName) {
		final Customer customer = customerService.findCustomerByUserName(userName, store.getCode());
		if (customer != null) {
			customer.setPreferredLocale(store.getDefaultLocale());
		}
		return customer;
	}

	private void createAndAddAnonymousShopper(final Store store, final Customer customer, final boolean primaryCustomerSession) {
		if (customer == null) {
			//don't persist the customer unless you talk with Ivan, Mike, Edison or Matt (or all).
			throw new IllegalArgumentException("Customer cannot be null. If your customer is null, consider to use createAnonymousSession.");
		}

		final Shopper shopper = shopperService.findOrCreateShopper(customer, store.getCode());

		//setting the anonymous customer to the shopper
		//it get ripped of from the shopper service
		shopper.setCustomer(customer);

		CustomerSession customerSession = customerSessionService.createWithShopper(shopper);
		customerSession.setLocale(store.getDefaultLocale());
		customerSession.setCurrency(store.getDefaultCurrency());
		customerSessionService.initializeCustomerSessionForPricing(customerSession, store.getCode(), store.getDefaultCurrency());

		createOrLoadShoppingCart(shopper, customerSession, store, customer);

		shopperService.save(shopper);

		if (primaryCustomerSession) {
			ShoppingTestData.getInstance().setShopper(shopper);
		} else {
			ShoppingTestData.getInstance().setSecondaryShopper(shopper);
		}
		ShoppingTestData.getInstance().setStore(store);
	}

	private void handleCustomerSignIn(final Store store, final Customer customer, final boolean primaryCustomerSession) {
		createAndAddAnonymousShopper(store, customer, primaryCustomerSession);
		ShoppingTestData shoppingTestData = ShoppingTestData.getInstance();
		if (primaryCustomerSession) {
			handleCustomerSignIn(customer, shoppingTestData.getShopper());
		} else {
			handleCustomerSignIn(customer, shoppingTestData.getSecondaryShopper());
		}
		ShoppingTestData.getInstance().setStore(store);
	}

	private void handleCustomerSignIn(final Customer customer, final Shopper shopper) {
		shopper.setCustomer(customer);
		shopper.getCustomerSession().setPriceListStackValid(false);
	}

	private void createOrLoadShoppingCart(final Shopper shopper, final CustomerSession customerSession, final Store store) {
		ShoppingCart shoppingCart = getShoppingCartService().findOrCreateDefaultCartByShopper(shopper);
		customerSession.setCurrency(store.getDefaultCurrency());
		shoppingCart.setStore(store);
		shoppingCart.setDefault(true);

		shoppingCart = getShoppingCartService().saveIfNotPersisted(shoppingCart);
		shopper.setCurrentShoppingCart(shoppingCart);
	}

	private ShoppingCart createOrLoadShoppingCart(final Shopper shopper, final CustomerSession customerSession, final Store store,
												  final Customer customer) {
		createOrLoadShoppingCart(shopper, customerSession, store);
		final ShoppingCart shoppingCart = shopper.getCurrentShoppingCart();
		shoppingCart.setShopper(shopper);
		if (customer != null) {
			shoppingCart.setBillingAddress(customer.getPreferredBillingAddress());
			shoppingCart.setShippingAddress(customer.getPreferredShippingAddress());
		}

		return shoppingCart;
	}

	@Override
	public void setCustomerSessionService(final CustomerSessionService customerSessionService) {
		this.customerSessionService = customerSessionService;
	}

	@Override
	public void setUtility(final Utility utility) {
		this.utility = utility;
	}

	@Override
	public Utility getUtility() {
		return utility;
	}

	@Override
	public void setShoppingCartService(final ShoppingCartService shoppingCartService) {
		this.shoppingCartService = shoppingCartService;
	}

	protected ShoppingCartService getShoppingCartService() {
		return shoppingCartService;
	}

	/**
	 * @param customerService the customerService to set
	 */
	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}

	@Override
	public void setShopperService(final ShopperService shopperService) {
		this.shopperService = shopperService;
	}

}
