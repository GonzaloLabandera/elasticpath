/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.shopper;

import com.elasticpath.domain.customer.CustomerSession;

/**
 * An interface to update any of the transient data on {@link Shopper}.
 */
public interface UpdateShopperTransientData {

	/**
	 * Updates transient data on {@link Shopper} that comes from {@link CustomerSession}.
	 *
	 * @param customerSession {@link CustomerSession} which contains the transient data that {@link Shopper} requires.
	 */
	void updateTransientDataWith(CustomerSession customerSession);
	
}
