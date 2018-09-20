/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.availabilities.AvailabilityForCartLineItemIdentifier;
import com.elasticpath.rest.definition.orders.OrderIdentifier;

/**
 * Validation service for order item availability.
 */
public interface ItemAvailabilityValidationService {

	/**
	 * Check if the order has unavailable items.
	 *
	 * @param orderIdentifier orderIdentifier
	 * @return the messages of unavailable items
	 */
	Observable<LinkedMessage<AvailabilityForCartLineItemIdentifier>> validateItemUnavailable(OrderIdentifier orderIdentifier);
}
