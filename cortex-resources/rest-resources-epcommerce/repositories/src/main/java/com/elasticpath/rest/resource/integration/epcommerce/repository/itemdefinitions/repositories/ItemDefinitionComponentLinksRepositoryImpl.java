/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentsIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Repository for item definition component links in item definition components list.
 *
 * @param <I>  the identifier type
 * @param <LI> the linked identifier type
 */
@Component
public class ItemDefinitionComponentLinksRepositoryImpl<I extends ItemDefinitionComponentsIdentifier, LI extends ItemDefinitionComponentIdentifier>
		implements LinksRepository<ItemDefinitionComponentsIdentifier, ItemDefinitionComponentIdentifier> {

	private ItemRepository itemRepository;

	@Override
	public Observable<ItemDefinitionComponentIdentifier> getElements(final ItemDefinitionComponentsIdentifier identifier) {
		return itemRepository.getSkuForItemId(identifier.getItemDefinition().getItemId().getValue())
				.map(ProductSku::getProduct)
				.flatMap(product -> itemRepository.asProductBundle(product))
				.map(ProductBundle::getConstituents)
				.flatMapObservable(Observable::fromIterable)
				.map(bundleConstituent -> buildItemDefinitionComponentIdentifier(bundleConstituent, identifier));
	}

	/**
	 * Build the ItemDefinitionComponentIdentifier.
	 *
	 * @param bundleConstituent                  bundleConstituent
	 * @param itemDefinitionComponentsIdentifier itemDefinitionComponentsIdentifier
	 * @return ItemDefinitionComponentIdentifier
	 */
	protected ItemDefinitionComponentIdentifier buildItemDefinitionComponentIdentifier(
			final BundleConstituent bundleConstituent,
			final ItemDefinitionComponentsIdentifier itemDefinitionComponentsIdentifier) {
		return ItemDefinitionComponentIdentifier.builder()
				.withItemDefinitionComponents(itemDefinitionComponentsIdentifier)
				.withComponentId(PathIdentifier.of(bundleConstituent.getGuid()))
				.build();
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}
}
