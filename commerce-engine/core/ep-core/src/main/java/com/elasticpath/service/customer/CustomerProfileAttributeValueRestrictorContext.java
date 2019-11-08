/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer;

import java.util.Collection;

import com.elasticpath.domain.store.Store;

/**
 * Context for restrictor evaluation.
 */
public interface CustomerProfileAttributeValueRestrictorContext {

	/**
	 * The collection of shared stores.
	 * @return the shared stores
	 */
	Collection<Store> getSharedStores();
}
