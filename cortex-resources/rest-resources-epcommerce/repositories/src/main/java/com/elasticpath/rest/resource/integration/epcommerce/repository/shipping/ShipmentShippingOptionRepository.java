/*
 * Copyright © 2014 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.shipping;

import io.reactivex.Single;

import com.elasticpath.shipping.connectivity.dto.ShippingOption;

/**
 * Repository for accessing {@link ShippingOption}s directly, as is necessary for a shipment.<br/>
 * This differs from the {@link ShippingOptionRepository}, which contains logic that is specific
 * to finding {@link ShippingOption}s that are valid for a given cart.
 */
public interface ShipmentShippingOptionRepository {

	/**
	 * Find a {@link ShippingOption} by its shipping option code.
	 *
	 * @param shippingOptionCode the shipping option code.
	 * @param storeCode          the store code
	 * @return result of {@link ShippingOption} lookup
	 */
	Single<ShippingOption> findByCode(String shippingOptionCode, String storeCode);

}
