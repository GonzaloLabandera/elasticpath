/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.shippinginfo;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.ShippingOptionInfoIdentifier;

/**
 * Service that validates shipping option info for advisors.
 */
public interface ShippingOptionInfoValidationService {

	/**
	 * Creates an advisor when there is no shipping option info selected.
	 *
	 * @param orderIdentifier	identifier
	 * @return LinkedMessage ShippingOptionInfoIdentifier that contains the advisor.
	 */
	Observable<LinkedMessage<ShippingOptionInfoIdentifier>> validateShippingOptionInfo(OrderIdentifier orderIdentifier);
}
