/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.cartorder.dao;

import java.util.List;

import com.elasticpath.domain.cartorder.CartOrder;

/**
 * The CartOrder DAO interface, CartOrder should not be used in versions of EP prior to 6.4.
 */
public interface CartOrderDao {

	/**
	 * @param uid The CartOrder uidpk.
	 * @return The CartOrder.
	 */
	CartOrder get(long uid);

	/**
	 * @param guid The CartOrder GUID.
	 * @return The CartOrder.
	 */
	CartOrder findByGuid(String guid);

	/**
	 * Finds a {@link CartOrder} given the shopping cart guid.
	 *
	 * @param guid the guid
	 * @return the cart order
	 */
	CartOrder findByShoppingCartGuid(String guid);

	/**
	 * @param cartOrder The CartOrder to save or update.
	 * @return The saved/updated CartOrder.
	 */
	CartOrder saveOrUpdate(CartOrder cartOrder);

	/**
	 * Find the GUIDs of all the cart orders owned by a customer in a certain store, given customer's GUID and store code.
	 * @param storeCode the store code
	 * @param customerGuid the customer GUID
	 *
	 * @return the list of cart order GUIDs
	 */
	List<String> findCartOrderGuidsByCustomerGuid(String storeCode, String customerGuid);

	/**
	 * Find the GUIDs of all the cart orders owned by an account in a certain store, given customer's GUID and store code.
	 * @param storeCode the store code
	 * @param accountGuid the account GUID
	 *
	 * @return the list of cart order GUIDs
	 */
	List<String> findCartOrderGuidsByAccountGuid(String storeCode, String accountGuid);
}
