/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Repository for item definition option links in item definition options list.
 *
 * @param <I>  the identifier type
 * @param <LI> the linked identifier type
 */
@Component
public class ItemDefinitionOptionLinksRepositoryImpl<I extends ItemDefinitionOptionsIdentifier, LI extends ItemDefinitionOptionIdentifier>
		implements LinksRepository<ItemDefinitionOptionsIdentifier, ItemDefinitionOptionIdentifier> {

	private ItemRepository itemRepository;

	@Override
	public Observable<ItemDefinitionOptionIdentifier> getElements(final ItemDefinitionOptionsIdentifier itemDefinitionOptionsIdentifier) {
		return itemRepository.getSkuOptionsForItemId(itemDefinitionOptionsIdentifier.getItemDefinition().getItemId().getValue())
				.map(skuOption -> ItemDefinitionOptionIdentifier.builder()
						.withItemDefinitionOptions(itemDefinitionOptionsIdentifier)
						.withOptionId(StringIdentifier.of(skuOption.getGuid()))
						.build());
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}
}
