/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.customer;

import com.elasticpath.domain.shopper.Shopper;

/**
 * The customer session listener interface. 
 */
public interface CustomerSessionShopperUpdateHandler {
	
	/**
	 * The method to be triggered after {@link Shopper} is updated.
	 * 
	 * @param oldShopper the old {@link Shopper}.
	 * @param newShopper the new {@link Shopper}.
	 */
	void invalidateShopper(Shopper oldShopper, Shopper newShopper);
}
