/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service;

import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.shoppingcart.CheckoutService;

/**
 * Interface that provides some methods that deals with business operations during product ordering.
 */
public interface CustomerOrderingService {

	/**
	 * Handles changing shipping address for the shopping cart. Shipping service levels are reinitialized. If no shipping service level could be
	 * found for the shipping address then exception is thrown. If billing address wan't specified then the shipping address will be used as billing
	 * address.
	 * 
	 * @param shopper the shopper
	 * @param shippingAddress shipping address to be selected.
	 */
	void selectShippingAddress(Shopper shopper, CustomerAddress shippingAddress);

	/**
	 * Handles changing billing address for the shopping cart.
	 * 
	 * @param shopper shopper to be updated.
	 * @param billingAddress billing address to be selected.
	 */
	void selectBillingAddress(Shopper shopper, CustomerAddress billingAddress);

	/**
	 * Set the selectedShippingServiceLevelUid and update the shippingCost correspondingly. If there is no a shipping service level in the shopping
	 * cart with the specified uid, then EpDomainException will be thrown.
	 * 
	 * @param shoppingCart shopping cart to be updated.
	 * @param selectedSSLUid - the selected ShippingServiceLevel uid.
	 * @return updated shopping cart.
	 */
	ShoppingCart selectShippingServiceLevel(ShoppingCart shoppingCart, long selectedSSLUid);
	
	/**
	 * Set the checkout service.
	 * 
	 * @param checkoutService the customer service.
	 */
	void setCheckoutService(CheckoutService checkoutService);

	/**
	 * Set the customer service.
	 * 
	 * @param customerService the customer service.
	 */
	void setCustomerService(CustomerService customerService);

}