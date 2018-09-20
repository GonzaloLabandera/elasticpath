/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.advisors;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.purchases.CreatePurchaseFormIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseFormIsCartEmptyAdvisor;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.service.CartHasItemsService;

/**
 * Advisor that blocks Purchase form when no items are in the cart.
 */
public class PurchaseFormIsCartEmptyAdvisorImpl implements PurchaseFormIsCartEmptyAdvisor.FormAdvisor {

	private final CreatePurchaseFormIdentifier createPurchaseFormIdentifier;
	private final CartHasItemsService service;

	/**
	 * Constructor.
	 *
	 * @param createPurchaseFormIdentifier create purchase form identifier
	 * @param service                      order purchasable service
	 */
	@Inject
	public PurchaseFormIsCartEmptyAdvisorImpl(
			@RequestIdentifier final CreatePurchaseFormIdentifier createPurchaseFormIdentifier,
			@ResourceService final CartHasItemsService service) {

		this.createPurchaseFormIdentifier = createPurchaseFormIdentifier;
		this.service = service;
	}

	@Override
	public Observable<Message> onAdvise() {
		return service.validateCartHasItems(createPurchaseFormIdentifier.getOrder());
	}
}
