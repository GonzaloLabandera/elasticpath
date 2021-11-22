/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.shopper.dao;

import java.util.List;

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
	 * Finds the ShopperMemento based on customer guid and store code.
	 *
	 * @param customerGuid the customer guid that this Shopper belongs to.
	 * @param storeCode the storeCode of the Store this Shopper belongs to.
	 * @return ShopperMemento if found, otherwise null.
	 */
	ShopperMemento findByCustomerGuidAndStore(String customerGuid, String storeCode);

	/**
	 * Finds a ShopperMemento by Customer guid, account ID, and store code.
	 * @param customerGuid The Customer Guid.
	 * @param accountSharedId The Account Shared ID.
	 * @param storeCode The Store Code.
	 * @return The ShopperMemento or null if none found.
	 */
	ShopperMemento findByCustomerGuidAccountSharedIdAndStore(String customerGuid, String accountSharedId, String storeCode);

	/**
	 * Finds ShopperMementos based on customer guid.
	 *
	 * @param customerGuid the customer guid that the shoppers belongs to.
	 * @return list of ShopperMementos
	 */
	List<ShopperMemento> findByCustomerGuid(String customerGuid);

	/**
	 * Finds ShopperMementos based on account guid.
	 *
	 * @param accountGuid the account guid that the shoppers belongs to.
	 * @return list of ShopperMementos
	 */
	List<ShopperMemento> findByAccountGuid(String accountGuid);
}
