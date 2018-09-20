/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.shoppingcart;

/**
 * Internal interface for ShoppingCart Implementations.  Should not be called by external clients.
 */
public interface ShoppingCartMementoHolder {
	/**
	 * Gets the persistent shopping cart object.
	 *
	 * @return the shoppingCartPersistent
	 */
	ShoppingCartMemento getShoppingCartMemento();

	/**
	 * @param shoppingCartMemento the shoppingCartPersistent to set
	 */
	void setShoppingCartMemento(ShoppingCartMemento shoppingCartMemento);
}
