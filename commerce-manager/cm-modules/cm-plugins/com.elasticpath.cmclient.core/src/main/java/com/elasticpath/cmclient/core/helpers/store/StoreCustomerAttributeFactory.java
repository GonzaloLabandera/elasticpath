/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.core.helpers.store;

import com.elasticpath.domain.customer.StoreCustomerAttribute;

/**
 * Store customer attribute factory.
 */
public class StoreCustomerAttributeFactory {

	/**
	 * Creates a store customer attribute.
	 *
	 * @param storeCustomerAttribute store customer attribute
	 * @return store customer attribute
	 */
	public StoreCustomerAttributeModel createAttribute(final StoreCustomerAttribute storeCustomerAttribute) {
		return new StoreCustomerAttributeModel(storeCustomerAttribute.getGuid(),
				storeCustomerAttribute.getAttributeKey(), storeCustomerAttribute.getPolicyKey(), storeCustomerAttribute.getStoreCode());
	}
}
