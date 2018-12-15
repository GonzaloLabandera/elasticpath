/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionsIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Repository for item definition to item definition options link.
 *
 * @param <I>  the identifier type
 * @param <LI> the linked identifier type
 */
@Component
public class ItemDefinitionOptionsLinksRepositoryImpl<I extends ItemDefinitionIdentifier, LI extends ItemDefinitionOptionsIdentifier>
		implements LinksRepository<ItemDefinitionIdentifier, ItemDefinitionOptionsIdentifier> {

	private ItemRepository itemRepository;

	@Override
	public Observable<ItemDefinitionOptionsIdentifier> getElements(final ItemDefinitionIdentifier identifier) {
		return doesItemContainOptions(identifier.getItemId().getValue())
				.flatMapObservable(containsOptions -> containsOptions ? buildItemDefinitionOptionsIdentifier(identifier) : Observable.empty());
	}

	/**
	 * Builds the ItemDefinitionOptionsIdentifier given the ItemDefinitionIdentifier.
	 *
	 * @param itemDefinitionIdentifier itemDefinitionIdentifier
	 * @return the ItemDefinitionOptionsIdentifier
	 */
	protected Observable<ItemDefinitionOptionsIdentifier> buildItemDefinitionOptionsIdentifier(final ItemDefinitionIdentifier itemDefinitionIdentifier) {
		return Observable.just(ItemDefinitionOptionsIdentifier.builder()
				.withItemDefinition(itemDefinitionIdentifier)
				.build());
	}

	/**
	 * Checks if item has options.
	 *
	 * @param itemIdMap itemIdMap
	 * @return true if any options are present for this item
	 */
	protected Single<Boolean> doesItemContainOptions(final Map<String, String> itemIdMap) {
		return itemRepository.getSkuOptionsForItemId(itemIdMap)
				.isEmpty()
				.map(isEmpty -> !isEmpty);
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}
}
