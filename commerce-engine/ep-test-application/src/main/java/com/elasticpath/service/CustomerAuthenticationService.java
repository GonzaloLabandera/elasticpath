/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service;

import com.elasticpath.commons.util.Utility;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;

/**
 * <code>CustomerAuthenticationService</code> provides services for managing <code>CustomerSession</code>s.
 */
public interface CustomerAuthenticationService {

	void loginStore(Store store, String userName);

	/**
	 * Set the customer session service.
	 *
	 * @param customerSessionService the customer session service to set.
	 */
	void setCustomerSessionService(CustomerSessionService customerSessionService);

	/**
	 * Sets the utility.
	 *
	 * @param utility the utility.
	 */
	void setUtility(Utility utility);

	/**
	 * Returns the utility.
	 *
	 * @return the utility.
	 */
	Utility getUtility();

	/**
	 * Set the shopping cart service.
	 *
	 * @param shoppingCartService the shopping cart service to set.
	 */
	void setShoppingCartService(ShoppingCartService shoppingCartService);

	/**
	 * Sets the ShopperService.
	 *
	 * @param shopperService the ShopperService
	 */
	void setShopperService(ShopperService shopperService);

}
