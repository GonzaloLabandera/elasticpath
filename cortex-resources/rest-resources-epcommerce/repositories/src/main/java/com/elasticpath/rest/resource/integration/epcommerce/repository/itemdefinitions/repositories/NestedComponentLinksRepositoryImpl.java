/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import java.util.Iterator;
import java.util.Map;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionNestedComponentsIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Repository for item definition component links in item definition nested components list.
 *
 * @param <I>  the identifier type
 * @param <LI> the linked identifier type
 */
@Component
public class NestedComponentLinksRepositoryImpl<I extends ItemDefinitionNestedComponentsIdentifier, LI extends ItemDefinitionComponentIdentifier>
		implements LinksRepository<ItemDefinitionNestedComponentsIdentifier, ItemDefinitionComponentIdentifier> {

	private ItemRepository itemRepository;

	@Override
	public Observable<ItemDefinitionComponentIdentifier> getElements(final ItemDefinitionNestedComponentsIdentifier identifier) {
		final ItemDefinitionComponentIdentifier itemDefinitionComponent = identifier.getItemDefinitionComponent();
		final Map<String, String> itemIdMap = itemDefinitionComponent.getItemDefinitionComponents().getItemDefinition().getItemId().getValue();
		final Iterator<String> guidPathFromRootItem = itemDefinitionComponent.getComponentId().getValue().iterator();

		return itemRepository.findBundleConstituentAtPathEnd(itemIdMap, guidPathFromRootItem)
				.flatMap(bundleConstituent -> itemRepository.getProductBundleFromConstituent(bundleConstituent))
				.map(ProductBundle::getConstituents)
				.flatMapObservable(Observable::fromIterable)
				.map(bundleConstituent -> buildItemDefinitionComponentIdentifier(identifier, bundleConstituent))
				.onErrorResumeNext(Observable.empty());
	}

	/**
	 * Build the ItemDefinitionComponentIdentifier.
	 *
	 * @param identifier        itemDefinitionNestedComponentsIdentifier
	 * @param bundleConstituent bundleConstituent
	 * @return ItemDefinitionComponentIdentifier
	 */
	protected ItemDefinitionComponentIdentifier buildItemDefinitionComponentIdentifier(
			final ItemDefinitionNestedComponentsIdentifier identifier,
			final BundleConstituent bundleConstituent) {
		return ItemDefinitionComponentIdentifier.builder()
				.withItemDefinitionComponents(identifier.getItemDefinitionComponent().getItemDefinitionComponents())
				.withComponentId(PathIdentifier.of(identifier.getItemDefinitionComponent().getComponentId(), bundleConstituent.getGuid()))
				.build();
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}
}
