/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.impl;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.CustomerOrderingService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.shoppingcart.CheckoutService;

/**
 * Provides a high-level service for customer ordering controllers (used only in FIT tests).
 */
public class CustomerOrderingServiceImpl implements CustomerOrderingService {

	private CheckoutService checkoutService;

	private CustomerService customerService;

	@Override
	public void selectShippingAddress(final Shopper shopper, final CustomerAddress shippingAddress) {

		final ShoppingCart shoppingCart = shopper.getCurrentShoppingCart();

		// Update the shopping cart with shipping address
		shoppingCart.setShippingAddress(shippingAddress);

		// Throw the exception if no shipping service is provided for the selected address
		checkoutService.retrieveShippingOption(shoppingCart);
		if (shoppingCart.getShippingServiceLevelList().isEmpty()) {
			throw new EpServiceException("No shipping service level could be found for the shipping address " + shippingAddress);
		}

		final Customer customer = shopper.getCustomer();
		// Address will actually be added just if it's not currently contained in the customer's addresses list. Safe operation.
		customer.addAddress(shippingAddress);

		// Set the billing address to the shipping address by default
		// if no billing address is available
		if (shoppingCart.getBillingAddress() == null) {
			if (customer.getPreferredBillingAddress() == null) {
				shoppingCart.setBillingAddress(shippingAddress);
				customer.setPreferredBillingAddress(shippingAddress);
			} else {
				shoppingCart.setBillingAddress(customer.getPreferredBillingAddress());
			}
		}

		// Update the customer
		customer.setPreferredShippingAddress(shippingAddress);
		if (customer.isPersisted()) {
			customerService.verifyCustomer(customer);
			Customer updatedCustomer = customerService.update(customer);
			shopper.setCustomer(updatedCustomer);
		}
	}

	@Override
	public void selectBillingAddress(final Shopper shopper, final CustomerAddress billingAddress) {
		Customer customer = shopper.getCustomer();

		// Address will actually be added just if it's not currently contained in the customer's addresses list. Safe operation.
		customer.addAddress(billingAddress);
		customer.setPreferredBillingAddress(billingAddress);

		if (customer.isPersisted()) {
			customerService.verifyCustomer(customer);
		}

		// Update the shopping cart
		shopper.getCurrentShoppingCart().setBillingAddress(billingAddress);
	}

	@Override
	public ShoppingCart selectShippingServiceLevel(final ShoppingCart shoppingCart, final long selectedSSLUid) {
		shoppingCart.setSelectedShippingServiceLevelUid(selectedSSLUid);
		return shoppingCart;
	}

	@Override
	public void setCheckoutService(final CheckoutService checkoutService) {
		this.checkoutService = checkoutService;
	}

	@Override
	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}

}
