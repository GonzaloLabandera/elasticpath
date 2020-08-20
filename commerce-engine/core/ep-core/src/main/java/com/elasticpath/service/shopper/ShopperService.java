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
	 * @param customer Customer to use.
	 * @param storeCode the storeCode
	 * @return Shopper
	 */
	Shopper findOrCreateShopper(Customer customer, String storeCode);

	/**
	 * Finds a Shopper for this given customer and account.  If none found, then create one.
	 *
	 * @param customer Customer to use.
	 * @param account Account to use.
	 * @param storeCode the storeCode
	 * @return Shopper
	 */
	Shopper findOrCreateShopper(Customer customer, Customer account, String storeCode);

	/**
	 * Finds a Shopper for this given customerGuid.
	 *
	 * @param customerGuid Customer to use.
	 * @return Shopper
	 */
	Shopper findByCustomerGuid(String customerGuid);

	/**
	 * Finds a Shopper for this given customerGuid and account shared ID.
	 *
	 * @param customerGuid Customer to use.
	 * @param accountSharedId Account shared ID.
	 * @param storeCode The store code.
	 * @return Shopper
	 */
	Shopper findByCustomerGuidAndAccountSharedIdAndStore(String customerGuid, String accountSharedId, String storeCode);

	/**
	 * Finds a Shopper for given customer guid and store code.
	 *
	 * @param customerGuid Customer GUID.
	 * @param storeCode Store code.
	 * @return Shopper
	 */
	Shopper findByCustomerGuidAndStoreCode(String customerGuid, String storeCode);
	/**
	 * Finds a Shopper for this given customerSharedId.
	 *
	 * @param customerSharedId Customer SharedId to use.
	 * @param storeCode the storeCode
	 * @return Shopper
	 */
	Shopper findByCustomerSharedIdAndStoreCode(String customerSharedId, String storeCode);

	/**
	 * Finds a Shopper for this given customerSharedId and accountSharedId.
	 *
	 * @param customerSharedId Customer ID to use.
	 * @param accountSharedId the account shared ID to use.
	 * @param storeCode The store code.
	 * @return Shopper
	 */
	Shopper findByCustomerSharedIdAndAccountSharedIdAndStore(String customerSharedId, String accountSharedId, String storeCode);

	/**
     * Create and save the {@Shopper}.
     *
	 * @param storeCode the storeCode.
     * @return the {@link Shopper} created
     */
	Shopper createAndSaveShopper(String storeCode);

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
