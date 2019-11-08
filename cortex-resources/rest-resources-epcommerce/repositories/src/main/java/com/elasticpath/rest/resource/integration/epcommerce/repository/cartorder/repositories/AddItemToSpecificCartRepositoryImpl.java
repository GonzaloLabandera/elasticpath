/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import java.util.Map;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.AddToSpecificCartFormIdentifier;
import com.elasticpath.rest.definition.carts.LineItemConfigurationEntity;
import com.elasticpath.rest.definition.carts.LineItemEntity;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ModifiersRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Repository for cart.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AddItemToSpecificCartRepositoryImpl<E extends LineItemEntity,
		I extends AddToSpecificCartFormIdentifier>
implements Repository<LineItemEntity, AddToSpecificCartFormIdentifier>  {

	private ModifiersRepository modifiersRepository;


	@Override
	public Single<LineItemEntity> findOne(final AddToSpecificCartFormIdentifier identifier) {

		Single<LineItemConfigurationEntity> lineItemConfigurationEntity = getLineItemConfigurationEntity(identifier);
		String itemId = identifier.getItem().getItemId().getValue().get(ItemRepository.SKU_CODE_KEY);

		return lineItemConfigurationEntity.map(liConfig -> LineItemEntity
				.builder()
				.withQuantity(1)
				.withItemId(itemId)
				.withConfiguration(liConfig)
				.build()
		);
	}


	private Single<LineItemConfigurationEntity> getLineItemConfigurationEntity(final AddToSpecificCartFormIdentifier identifier) {
		IdentifierPart<Map<String, String>> itemId = identifier.getItem().getItemId();
		return modifiersRepository.getConfiguration(itemId.getValue().get(ItemRepository.SKU_CODE_KEY));

	}

	@Reference
	public void setModifiersRepository(final ModifiersRepository modifiersRepository) {
		this.modifiersRepository = modifiersRepository;
	}
}
