/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.advise;

import java.util.Map;
import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.carts.AddToSpecificCartFormAdvisorAdvisor;
import com.elasticpath.rest.definition.carts.AddToSpecificCartFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.AddToCartAdvisorService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Add to specific cart form advisor.
 */
public final class AddToSpecificCartFormAdvisorImpl implements AddToSpecificCartFormAdvisorAdvisor.FormAdvisor {

	private final String itemId;
	private final AddToCartAdvisorService addToCartAdvisorService;
	private final AddToSpecificCartFormIdentifier identifier;

	/**
	 * Constructor.
	 *
	 * @param identifier addToSpecificCartFormAdvisor identifier.
	 * @param addToCartAdvisorService        addToCartAdvisorService
	 */
	@Inject
	public AddToSpecificCartFormAdvisorImpl(@RequestIdentifier final AddToSpecificCartFormIdentifier identifier,
											@ResourceService final AddToCartAdvisorService addToCartAdvisorService) {
		this.identifier = identifier;
		Map<String, String> itemIdentifier = identifier.getItem().getItemId().getValue();
		this.itemId = itemIdentifier.get(ItemRepository.SKU_CODE_KEY);
		this.addToCartAdvisorService = addToCartAdvisorService;
	}

	@Override
	public Observable<Message> onAdvise() {
		return addToCartAdvisorService.validateItemPurchasable(identifier.getItem().getScope().getValue(),
				itemId);
	}
}
