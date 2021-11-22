/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.shopper;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.Shopper;

/**
 * Provides a way to find or store the Shopper.
 */
public interface ShopperService {

	/**
	 * Finds a Shopper for this given customer.  If none found, then create one.
	 *
	 * @param customer the customer
	 * @param storeCode the store code
	 * @return Shopper
	 */
	Shopper findOrCreateShopper(Customer customer, String storeCode);

	/**
	 * Finds a Shopper for this given customer and account.  If none found, then create one.
	 *
	 * @param customer the customer
	 * @param account the account
	 * @param storeCode the store code
	 * @return Shopper
	 */
	Shopper findOrCreateShopper(Customer customer, Customer account, String storeCode);

	/**
	 * Finds a Shopper for this given customer.  If none found, then create one.
	 *
	 * @param customerGuid the customer GUID
	 * @param storeCode the storeCode
	 * @return Shopper
	 */
	Shopper findOrCreateShopper(String customerGuid, String storeCode);

	/**
	 * Finds a Shopper for this given customer and account.  If none found, then create one.
	 *
	 * @param customerGuid the customer GUID
	 * @param accountSharedId the account shared ID
	 * @param storeCode the store code
	 * @return Shopper
	 */
	Shopper findOrCreateShopper(String customerGuid, String accountSharedId, String storeCode);

	/**
	 * Get the shopper by uidPk.
	 *
	 * @param uid the uidPk
	 * @return the shopper found
	 */
	Shopper get(long uid);

	/**
	 * Save the shopper.
	 *
	 * @param shopper the shopper to be saved
	 * @return the saved shopper
	 */
	Shopper save(Shopper shopper);

	/**
	 * Remove the shopper.
	 *
	 * @param shopper the shopper to be removed
	 */
	void remove(Shopper shopper);
}
