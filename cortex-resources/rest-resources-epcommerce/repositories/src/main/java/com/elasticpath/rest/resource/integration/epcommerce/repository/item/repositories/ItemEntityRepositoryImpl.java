/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.item.repositories;

import java.util.Map;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.items.ItemEntity;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Repository that implements retrieving an item.
 *
 * @param <E> extends ItemEntity
 * @param <I> extends ItemIdentifier
 */
@Component
public class ItemEntityRepositoryImpl<E extends ItemEntity, I extends ItemIdentifier>
		implements Repository<ItemEntity, ItemIdentifier> {

	private static final String ITEM_NOT_FOUND = "Item not found.";
	private ItemRepository itemRepository;

	@Override
	public Single<ItemEntity> findOne(final ItemIdentifier identifier) {
		final Map<String, String> itemIdMap = identifier.getItemId().getValue();
		final String itemId = itemIdMap.get(ItemRepository.SKU_CODE_KEY);

		return itemRepository.isProductSkuExistForItemId(itemIdMap)
				.flatMap(productSkuExists ->
						(productSkuExists) ? buildItemEntity(itemId) : Single.error(ResourceOperationFailure.notFound(ITEM_NOT_FOUND)));
	}

	/**
	 * Build an ItemEntity given the item ID.
	 *
	 * @param itemId item ID
	 * @return an ItemEntity
	 */
	protected Single<ItemEntity> buildItemEntity(final String itemId) {
		return Single.just(ItemEntity.builder()
				.withItemId(itemId)
				.build());
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}
}
