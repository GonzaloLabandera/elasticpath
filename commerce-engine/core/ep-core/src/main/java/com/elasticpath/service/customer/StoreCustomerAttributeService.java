/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elasticpath.domain.customer.StoreCustomerAttribute;

/**
 * Store customer attribute service.
 */
public interface StoreCustomerAttributeService {

	/**
	 * Finds store customer attribute by guid.
	 *
	 * @param guid the guid
	 * @return store customer attribute
	 */
	Optional<StoreCustomerAttribute> findByGuid(String guid);

	/**
	 * Finds store customer attribute by store code and attribute key.
	 *
	 * @param storeCode    store code
	 * @param attributeKey attribute key
	 * @return store customer attribute
	 */
	Optional<StoreCustomerAttribute> findByStoreCodeAndAttributeKey(String storeCode, String attributeKey);

	/**
	 * Find store attributes by guids.
	 *
	 * @param guids the guids
	 * @return list of store customer attributes
	 */
	List<StoreCustomerAttribute> findByGuids(List<String> guids);

	/**
	 * Finds store customer attributes by store.
	 *
	 * @param storeCode the store code
	 * @return list of store customer attributes
	 */
	List<StoreCustomerAttribute> findByStore(String storeCode);

	/**
	 * Find all store customer attributes.
	 *
	 * @return list of store customer attributes
	 */
	List<StoreCustomerAttribute> findAll();

	/**
	 * Remove store customer attribute.
	 *
	 * @param storeCustomerAttribute the store customer attribute to remove
	 */
	void remove(StoreCustomerAttribute storeCustomerAttribute);

	/**
	 * Add store customer attribute.
	 *
	 * @param storeCustomerAttribute the store customer attribute to add
	 */
	void add(StoreCustomerAttribute storeCustomerAttribute);

	/**
	 * Update store customer attribute.
	 *
	 * @param storeCustomerAttribute the store customer attribute to update
	 */
	void update(StoreCustomerAttribute storeCustomerAttribute);

	/**
	 * This method assumes that the provided map is a replacement of all StoreCustomerAttribute objects related to the store code.
	 * As such it will remove existing attributes that are not in the map, add attributes that new and update the remainder.
	 *
	 * @param storeCode the store code
	 * @param storeCustomerAttributes the updated attributes
	 */
	void updateAll(String storeCode, Map<String, StoreCustomerAttribute> storeCustomerAttributes);
}
