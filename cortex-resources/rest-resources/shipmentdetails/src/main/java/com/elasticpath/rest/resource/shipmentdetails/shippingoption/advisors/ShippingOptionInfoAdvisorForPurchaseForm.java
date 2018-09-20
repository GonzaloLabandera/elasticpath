/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.shipmentdetails.shippingoption.advisors;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.purchases.CreatePurchaseFormIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.PurchaseFormShippingOptionInfoAdvisor;
import com.elasticpath.rest.definition.shipmentdetails.ShippingOptionInfoIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.shippinginfo.ShippingOptionInfoValidationService;

/**
 * Shipping option info advisor for a purchase form.
 */
public class ShippingOptionInfoAdvisorForPurchaseForm implements PurchaseFormShippingOptionInfoAdvisor.LinkedFormAdvisor {

	private final CreatePurchaseFormIdentifier createPurchaseFormIdentifier;
	private final ShippingOptionInfoValidationService shippingOptionInfoValidationService;

	/**
	 * Constructor.
	 *
	 * @param createPurchaseFormIdentifier				identifier
	 * @param shippingOptionInfoValidationService		service
	 */
	@Inject
	public ShippingOptionInfoAdvisorForPurchaseForm(@RequestIdentifier final CreatePurchaseFormIdentifier createPurchaseFormIdentifier,
											 @ResourceService final ShippingOptionInfoValidationService shippingOptionInfoValidationService) {
		this.createPurchaseFormIdentifier = createPurchaseFormIdentifier;
		this.shippingOptionInfoValidationService = shippingOptionInfoValidationService;
	}

	@Override
	public Observable<LinkedMessage<ShippingOptionInfoIdentifier>> onLinkedAdvise() {
		return shippingOptionInfoValidationService.validateShippingOptionInfo(createPurchaseFormIdentifier.getOrder());
	}
}
