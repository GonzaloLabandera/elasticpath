/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.index.loader.impl;

import java.util.Collection;

import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.service.shipping.ShippingServiceLevelService;

/**
 * Fetches a batch of {@link ShippingServiceLevel}s.
 */
public class ShippingServiceLevelLoader extends AbstractEntityLoader<ShippingServiceLevel> {

	private ShippingServiceLevelService shippingServiceLevelService;

	/**
	 * Loads the {@link ShippingServiceLevel}s for the batched ids and loads each batch in bulk.
	 * 
	 * @return the loaded {@link ShippingServiceLevel}s
	 */
	@Override
	public Collection<ShippingServiceLevel> loadBatch() {
		return getShippingServiceLevelService().findByUids(getUidsToLoad());
	}

	/**
	 * @param shippingServiceLevelService the shippingServiceLevelService to set
	 */
	public void setShippingServiceLevelService(final ShippingServiceLevelService shippingServiceLevelService) {
		this.shippingServiceLevelService = shippingServiceLevelService;
	}

	/**
	 * @return the shippingServiceLevelService
	 */
	public ShippingServiceLevelService getShippingServiceLevelService() {
		return shippingServiceLevelService;
	}

}
