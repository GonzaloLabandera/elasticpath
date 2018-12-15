/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import java.util.Iterator;
import java.util.Map;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Repository for item definition component to item link.
 *
 * @param <I>  the identifier type
 * @param <LI> the linked identifier type
 */
@Component
public class ItemDefinitionComponentToItemLinksRepositoryImpl<I extends ItemDefinitionComponentIdentifier, LI extends ItemIdentifier>
		implements LinksRepository<ItemDefinitionComponentIdentifier, ItemIdentifier> {

	private ItemRepository itemRepository;

	@Override
	public Observable<ItemIdentifier> getElements(final ItemDefinitionComponentIdentifier identifier) {
		final IdentifierPart<String> scope = identifier.getItemDefinitionComponents().getItemDefinition().getScope();
		final Map<String, String> itemIdMap = identifier.getItemDefinitionComponents().getItemDefinition().getItemId().getValue();

		Iterator<String> guidPathFromRootItem = identifier.getComponentId().getValue().iterator();

		return itemRepository.findBundleConstituentAtPathEnd(itemIdMap, guidPathFromRootItem)
				.map(bundleConstituent -> bundleConstituent.getConstituent().getProductSku().getSkuCode())
				.map(skuCode -> itemRepository.getItemIdMap(skuCode))
				.flatMapObservable(itemId -> buildItemIdentifier(itemId, scope))
				.onErrorResumeNext(Observable.empty());
	}

	/**
	 * Build an ItemIdentifier.
	 *
	 * @param itemId itemId
	 * @param scope  scope
	 * @return an ItemIdentifier
	 */
	protected Observable<ItemIdentifier> buildItemIdentifier(final IdentifierPart<Map<String, String>> itemId,
															 final IdentifierPart<String> scope) {
		return Observable.just(ItemIdentifier.builder()
				.withScope(scope)
				.withItemId(itemId)
				.build());
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}
}
