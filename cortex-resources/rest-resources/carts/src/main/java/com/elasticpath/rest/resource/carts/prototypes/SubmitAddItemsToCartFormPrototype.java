/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.prototypes;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormEntity;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormIdentifier;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormResource;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.ItemEntity;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Add Items to Cart Form prototype for Submit operation.
 */
public class SubmitAddItemsToCartFormPrototype implements AddItemsToCartFormResource.SubmitWithResult {

	private static final int MAX_ITEMS = 2000;
	
	private final AddItemsToCartFormEntity addItemsToCartFormEntity;

	private final AddItemsToCartFormIdentifier addItemsToCartFormIdentifier;

	private final Repository<AddItemsToCartFormEntity, CartIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param addItemsToCartFormEntity     addItemsToCartFormEntity
	 * @param addItemsToCartFormIdentifier addItemsToCartFormIdentifier
	 * @param repository                   repository
	 */
	@Inject
	public SubmitAddItemsToCartFormPrototype(@RequestForm final AddItemsToCartFormEntity addItemsToCartFormEntity,
											 @RequestIdentifier final AddItemsToCartFormIdentifier addItemsToCartFormIdentifier,
											 @ResourceRepository final Repository<AddItemsToCartFormEntity, CartIdentifier> repository) {
		this.addItemsToCartFormEntity = addItemsToCartFormEntity;
		this.addItemsToCartFormIdentifier = addItemsToCartFormIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<SubmitResult<CartIdentifier>> onSubmitWithResult() {
		List<ItemEntity> items = addItemsToCartFormEntity.getItems();
		if (items.size() > MAX_ITEMS) {
			String exceptionMessage = String.format("The request is too large. The maximum number of items that can be added to a cart in a single " 
					+ "request is %s. To add more items, create another request.", MAX_ITEMS);
			return Single.error(new ResourceOperationFailure(
					exceptionMessage,
					null, 
					ResourceStatus.REQUEST_ENTITY_TOO_LARGE,
					Arrays.asList(Message.builder()
							.withType(StructuredMessageTypes.ERROR)
							.withId("cart.request.too.large")
							.withData(ImmutableMap.of("maximum-number-of-items", String.valueOf(MAX_ITEMS)))
							.withDebugMessage(exceptionMessage)
							.build()
					))
			);
		}
		return repository.submit(addItemsToCartFormEntity, addItemsToCartFormIdentifier.getCart().getCarts().getScope());
	}
}
