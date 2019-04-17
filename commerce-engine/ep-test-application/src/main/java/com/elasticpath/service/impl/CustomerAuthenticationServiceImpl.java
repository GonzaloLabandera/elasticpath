/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.impl;

import java.util.Date;
import java.util.UUID;

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
	public void loginStore(final Store store, final String userId) {
		loginStoreInternal(store, userId, true);
	}

	private void loginStoreInternal(final Store store, final String userId, final boolean primaryCustomerSession) {
		validate(store, userId);

		final Customer customer = findCustomer(store, userId);
		if (customer == null) {
			throw new TestApplicationException("Couldn't log into store " + store.getCode() + " with user id " + userId);
		}

		handleSignIn(store, customer, primaryCustomerSession);
	}

	private void handleSignIn(final Store store, final Customer customer, final boolean primaryCustomerSession) {
		handleCustomerSignIn(store, customer, primaryCustomerSession);
		ShoppingTestData.getInstance().setStore(store);
	}

	private void validate(final Store store, final String userId) {
		if (store == null) {
			throw new TestApplicationException("Store cannot be null");
		}

		if (userId == null) {
			throw new TestApplicationException("User id cannot be null");
		}
	}

	private Customer findCustomer(final Store store, final String userId) {
		final Customer customer = customerService.findByUserId(userId, store.getCode());
		if (customer != null) {
			customer.setPreferredLocale(store.getDefaultLocale());
		}
		return customer;
	}

	private void createAndAddAnonymousCustomerSession(
			final Store store, final String customerSessionGuid, final Customer customer, final boolean primaryCustomerSession) {
		if (customer == null) {
			//don't persist the customer unless you talk with Ivan, Mike, Edison or Matt (or all).
			throw new IllegalArgumentException("Customer cannot be null. If your customer is null, consider to use createAnonymousSession.");
		}

		final Shopper shopper = shopperService.findOrCreateShopper(customer, store.getCode());

		//setting the anonymous customer to the shopper
		//it get ripped of from the shopper service
		shopper.setCustomer(customer);

		CustomerSession customerSession = customerSessionService.createWithShopper(shopper);
		customerSession.setCreationDate(new Date());
		customerSession.setLastAccessedDate(new Date());
		customerSession.setLocale(store.getDefaultLocale());
		customerSession.setCurrency(store.getDefaultCurrency());
		customerSession.setGuid(customerSessionGuid);
		customerSession = customerSessionService.initializeCustomerSessionForPricing(customerSession, store.getCode(), store.getDefaultCurrency());

		customerSessionService.add(customerSession);
		createOrLoadShoppingCart(customerSession, store, customer);

		shopperService.save(shopper);

		if (primaryCustomerSession) {
			ShoppingTestData.getInstance().setCustomerSession(customerSession);
		} else {
			ShoppingTestData.getInstance().setSecondaryCustomerSession(customerSession);
		}
		ShoppingTestData.getInstance().setStore(store);
	}

	private void handleCustomerSignIn(final Store store, final Customer customer, final boolean primaryCustomerSession) {
		createAndAddAnonymousCustomerSession(store, obtainCustomerSessionGuid(), customer, primaryCustomerSession);
		if (primaryCustomerSession) {
			handleCustomerSignIn(store.getCode(), customer, ShoppingTestData.getInstance().getCustomerSession());
		} else {
			handleCustomerSignIn(store.getCode(), customer, ShoppingTestData.getInstance().getSecondaryCustomerSession());
		}
		ShoppingTestData.getInstance().setStore(store);
	}

	private void handleCustomerSignIn(final String storeCode, final Customer customer, final CustomerSession customerSession) {
		customerSession.getShopper().setCustomer(customer);
		customerSession.setSignedIn(true);

		customerSessionService.handleShopperChangeAndUpdate(customerSession, storeCode);
		customerSession.setPriceListStackValid(false);
	}

	private void createOrLoadShoppingCart(final CustomerSession customerSession, final Store store) {
		ShoppingCart shoppingCart = getShoppingCartService().findOrCreateByShopper(customerSession.getShopper());
		customerSession.setShoppingCart(shoppingCart);
		customerSession.setCurrency(store.getDefaultCurrency());
		shoppingCart.setCustomerSession(customerSession);
		shoppingCart.setStore(store);

		shoppingCart = getShoppingCartService().saveIfNotPersisted(shoppingCart);
		customerSession.getShopper().setCurrentShoppingCart(shoppingCart);
	}

	private ShoppingCart createOrLoadShoppingCart(final CustomerSession customerSession, final Store store, final Customer customer) {
		createOrLoadShoppingCart(customerSession, store);
		final ShoppingCart shoppingCart = customerSession.getShopper().getCurrentShoppingCart();
		shoppingCart.setShopper(customerSession.getShopper());
		if (customer != null) {
			shoppingCart.setBillingAddress(customer.getPreferredBillingAddress());
			shoppingCart.setShippingAddress(customer.getPreferredShippingAddress());
		}

		return shoppingCart;
	}

	private String obtainCustomerSessionGuid() {
		return UUID.randomUUID().toString();
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
