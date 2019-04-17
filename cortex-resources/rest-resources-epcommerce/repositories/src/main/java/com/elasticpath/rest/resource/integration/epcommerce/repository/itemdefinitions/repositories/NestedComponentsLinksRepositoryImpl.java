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
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionNestedComponentsIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Repository for item definition component to item definition nested components link.
 *
 * @param <I>  the identifier type
 * @param <LI> the linked identifier type
 */
@Component
public class NestedComponentsLinksRepositoryImpl<I extends ItemDefinitionComponentIdentifier, LI extends ItemDefinitionNestedComponentsIdentifier>
		implements LinksRepository<ItemDefinitionComponentIdentifier, ItemDefinitionNestedComponentsIdentifier> {

	private ItemRepository itemRepository;

	@Override
	public Observable<ItemDefinitionNestedComponentsIdentifier> getElements(final ItemDefinitionComponentIdentifier identifier) {
		final Map<String, String> itemIdMap = identifier.getItemDefinitionComponents().getItemDefinition().getItemId().getValue();
		final Iterator<String> guidPathFromRootItem = identifier.getComponentId().getValue().iterator();

		return itemRepository.findBundleConstituentAtPathEnd(itemIdMap, guidPathFromRootItem)
				.map(bundleConstituent -> bundleConstituent.getConstituent().isBundle())
				.flatMapObservable(isBundle -> isBundle ? buildNestedComponentsIdentifier(identifier) : Observable.empty())
				.onErrorResumeNext(Observable.empty());
	}

	/**
	 * Build the ItemDefinitionNestedComponentsIdentifier.
	 *
	 * @param identifier itemDefinitionComponentIdentifier
	 * @return a ItemDefinitionNestedComponentsIdentifier
	 */
	protected Observable<ItemDefinitionNestedComponentsIdentifier> buildNestedComponentsIdentifier(
			final ItemDefinitionComponentIdentifier identifier) {
		return Observable.just(ItemDefinitionNestedComponentsIdentifier.builder()
				.withItemDefinitionComponent(identifier)
				.build());
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}
}
