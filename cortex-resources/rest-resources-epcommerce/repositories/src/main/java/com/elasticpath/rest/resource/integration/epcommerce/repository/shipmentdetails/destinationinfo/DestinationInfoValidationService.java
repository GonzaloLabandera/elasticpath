/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.destinationinfo;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.DestinationInfoIdentifier;

/**
 * Service that creates an advisor when destination info is needed.
 */
public interface DestinationInfoValidationService {

	/**
	 * Creates an advisor for destination info.
	 * @param orderIdentifier	identifier
	 * @return an advisor
	 */
	Observable<LinkedMessage<DestinationInfoIdentifier>> validateDestinationInfo(OrderIdentifier orderIdentifier);
}
