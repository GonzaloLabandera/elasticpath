/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer.impl;

import java.util.Collection;

import com.elasticpath.domain.store.Store;
import com.elasticpath.service.customer.CustomerProfileAttributeValueRestrictorContext;

/**
 * Implementation of <code>CustomerProfileAttributeValueRestrictorContext</code>.
 */
public class CustomerProfileAttributeValueRestrictorContextImpl implements CustomerProfileAttributeValueRestrictorContext {

	private final Collection<Store> sharedStores;

	/**
	 * Constructor.
	 * @param sharedStores the collection of shared stores
	 */
	public CustomerProfileAttributeValueRestrictorContextImpl(final Collection<Store> sharedStores) {
		this.sharedStores = sharedStores;
	}

	@Override
	public Collection<Store> getSharedStores() {
		return sharedStores;
	}
}
