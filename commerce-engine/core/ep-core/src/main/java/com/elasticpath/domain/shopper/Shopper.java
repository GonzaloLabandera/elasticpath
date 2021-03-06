/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.domain.shopper;

import com.elasticpath.base.GloballyIdentifiable;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shoppingcart.ShopperBrowsingActivity;
import com.elasticpath.persistence.api.Persistable;

/**
 * A shopper in the system: this is the key object for finding items
 * related to the shopper, e.g. shopping carts, wish lists, etc.
 */
public interface Shopper extends ShoppingRequisiteData, CustomerAccessor, 
		ShoppingCartAccessor, WishListAccessor, Persistable, GloballyIdentifiable {

	/**
	 * Gets the {@link ShopperMemento} for this Shopper.
	 * @return the {@link ShopperMemento} for this Shopper.
	 */
	ShopperMemento getShopperMemento();

	/**
	 * Sets the {@link ShopperMemento} for this Shopper.
	 * @param shopperMomento the 
	 */
	void setShopperMemento(ShopperMemento shopperMomento);

	/**
	 * Gets the {@link ShopperBrowsingActivity} for this Shopper.
	 *
	 * @return this shopper's browsing activity
	 */
	ShopperBrowsingActivity getBrowsingActivity();

	/**
	 * Updates transient data on {@link Shopper} that comes from {@link CustomerSession}.
	 *
	 * @param customerSession {@link CustomerSession} which contains the transient data that {@link Shopper} requires.
	 */
	void setCustomerSession(CustomerSession customerSession);

	/**
	 * Returns the {@link CustomerSession} stored on the Shopper. Should only be used by ShoppingCart.
	 *
	 * @return {@link CustomerSession}
	 */
	CustomerSession getCustomerSession();
}
