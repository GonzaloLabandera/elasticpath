/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Repository for item definition to item definition components link.
 *
 * @param <I>  the identifier type
 * @param <LI> the linked identifier type
 */
@Component
public class ItemDefinitionComponentsLinksRepositoryImpl<I extends ItemDefinitionIdentifier, LI extends ItemDefinitionComponentsIdentifier>
		implements LinksRepository<ItemDefinitionIdentifier, ItemDefinitionComponentsIdentifier> {

	private ItemRepository itemRepository;

	@Override
	public Observable<ItemDefinitionComponentsIdentifier> getElements(final ItemDefinitionIdentifier identifier) {
		return itemRepository.isItemBundle(identifier.getItemId().getValue())
				.flatMapObservable(isItemBundle -> isItemBundle ? buildItemDefinitionComponentsIdentifier(identifier) : Observable.empty());
	}

	/**
	 * Build the ItemDefinitionComponentsIdentifier.
	 *
	 * @param identifier itemDefinitionIdentifier
	 * @return a ItemDefinitionComponentsIdentifier
	 */
	protected Observable<ItemDefinitionComponentsIdentifier> buildItemDefinitionComponentsIdentifier(final ItemDefinitionIdentifier identifier) {
		return Observable.just(ItemDefinitionComponentsIdentifier.builder()
				.withItemDefinition(identifier)
				.build());
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

}
