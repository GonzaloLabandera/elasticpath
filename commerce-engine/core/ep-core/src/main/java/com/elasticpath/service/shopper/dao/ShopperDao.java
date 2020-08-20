/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.shopper.dao;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.ShopperMemento;


/**
 * The {@link ShopperMemento} dao interface.
 */
public interface ShopperDao {

	/**
	 * Get ShopperMemento by uidpk.
	 *
	 * @param uid the uidpk
	 * @return the wish list found
	 */
	ShopperMemento get(long uid);

	/**
	 * save or update {@link ShopperMemento}.
	 *
	 * @param shopperMemento the {@link ShopperMemento} to be saved or updated.
	 * @return the saved/updated ShopperMemento
	 */
	ShopperMemento saveOrUpdate(ShopperMemento shopperMemento);

	/**
	 * Remove the {@link ShopperMemento}.
	 *
	 * @param shopperMemento the {@link ShopperMemento}.
	 */
	void remove(ShopperMemento shopperMemento);

	/**
	 * Finds the ShopperMemento based on customer and store code.
	 *
	 * @param customer the {@link Customer} that this Shopper belongs to.
	 * @param storeCode the storeCode of the Store this Shopper belongs to.
	 * @return ShopperMemento if found, otherwise null.
	 */
	ShopperMemento findByCustomerAndStoreCode(Customer customer, String storeCode);
	
	/**
	 * Finds the ShopperMemento based on customer guid and store code.
	 *
	 * @param customerGuid the customer guid that this Shopper belongs to.
	 * @param storeCode the storeCode of the Store this Shopper belongs to.
	 * @return ShopperMemento if found, otherwise null.
	 */
	ShopperMemento findByCustomerGuidAndStoreCode(String customerGuid, String storeCode);

	/**
	 * Finds the ShopperMemento by customer, account, and store.
	 * @param customer The customer.
	 * @param account The account.
	 * @param storeCode The store code.
	 * @return The ShopperMememnto or null if none found.
	 */
	ShopperMemento findByCustomerAccountAndStore(Customer customer, Customer account, String storeCode);

	/**
	 * Finds a ShopperMemento by Customer guid, account ID, and store code.
	 * @param customerGuid The Customer Guid.
	 * @param accountSharedId The Account Shared ID.
	 * @param storeCode The Store Code.
	 * @return The ShopperMemento or null if none found.
	 */
	ShopperMemento findByCustomerGuidAccountSharedIdAndStore(String customerGuid, String accountSharedId, String storeCode);

	/**
	 * Finds a ShopperMemento by customer user ID, Account Shared ID, and store code.
	 * @param customerSharedId The customer ID.
	 * @param accountSharedId The Account Shared ID.
	 * @param storeCode The store code.
	 * @return ShopperMemento or null if none found.
	 */
	ShopperMemento findByCustomerSharedIdAndAccountSharedIdAndStore(String customerSharedId, String accountSharedId, String storeCode);

	/**
	 * Finds the ShopperMemento based on customer guid.
	 *
	 * @param customerGuid the customer guid  that this Shopper belongs to.
	 * @return ShopperMemento if found, otherwise null.
	 */
	ShopperMemento findByCustomerGuid(String customerGuid);

	/**
	 * Finds the ShopperMemento based on customer id and store code.
	 *
	 * @param customerSharedId the customer id that this Shopper belongs to.
	 * @param storeCode the storeCode of the Store this Shopper belongs to.
	 * @return ShopperMemento if found, otherwise null.
	 */
	ShopperMemento findByCustomerSharedIdAndStore(String customerSharedId, String storeCode);
}
