/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.shipmentdetails.destinationinfo.advisors;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.purchases.CreatePurchaseFormIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.DestinationInfoIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.PurchaseFormDestinationInfoAdvisor;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.destinationinfo.DestinationInfoValidationService;

/**
 * Blocks create purchase form when destination info is not resolved.
 */
public class DestinationInfoAdvisorForPurchaseForm implements PurchaseFormDestinationInfoAdvisor.LinkedFormAdvisor {

	private final CreatePurchaseFormIdentifier createPurchaseFormIdentifier;
	private final DestinationInfoValidationService destinationInfoValidationService;

	/**
	 * Constructor.
	 *
	 * @param createPurchaseFormIdentifier		identifier
	 * @param destinationInfoValidationService	service
	 */
	@Inject
	public DestinationInfoAdvisorForPurchaseForm(@RequestIdentifier final CreatePurchaseFormIdentifier createPurchaseFormIdentifier,
										  @ResourceService final DestinationInfoValidationService destinationInfoValidationService) {
		this.createPurchaseFormIdentifier = createPurchaseFormIdentifier;
		this.destinationInfoValidationService = destinationInfoValidationService;
	}

	@Override
	public Observable<LinkedMessage<DestinationInfoIdentifier>> onLinkedAdvise() {
		return destinationInfoValidationService.validateDestinationInfo(createPurchaseFormIdentifier.getOrder());
	}
}
