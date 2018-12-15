/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import java.util.Iterator;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionsIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;

/**
 * Repository for item definition component to item definition component options link.
 *
 * @param <I>  the identifier type
 * @param <LI> the linked identifier type
 */
@Component
public class ComponentOptionsLinksRepositoryImpl<I extends ItemDefinitionComponentIdentifier, LI extends ItemDefinitionComponentOptionsIdentifier>
		implements LinksRepository<ItemDefinitionComponentIdentifier, ItemDefinitionComponentOptionsIdentifier> {

	private ItemRepository itemRepository;
	private ProductSkuRepository productSkuRepository;

	@Override
	public Observable<ItemDefinitionComponentOptionsIdentifier> getElements(final ItemDefinitionComponentIdentifier identifier) {
		final Map<String, String> itemIdMap = identifier.getItemDefinitionComponents().getItemDefinition().getItemId().getValue();
		Iterator<String> guidPathFromRootItem = identifier.getComponentId().getValue().iterator();

		return itemRepository.findBundleConstituentAtPathEnd(itemIdMap, guidPathFromRootItem)
				.map(bundleConstituent -> bundleConstituent.getConstituent().getProductSku().getSkuCode())
				.flatMap(this::doesSkuContainOptions)
				.flatMapObservable(containsOptions ->
						containsOptions ? buildItemDefinitionComponentOptionsIdentifier(identifier) : Observable.empty())
				.onErrorResumeNext(Observable.empty());
	}

	/**
	 * Build an ItemDefinitionComponentOptionsIdentifier.
	 *
	 * @param identifier ItemDefinitionComponentIdentifier
	 * @return ItemDefinitionComponentOptionsIdentifier
	 */
	protected Observable<ItemDefinitionComponentOptionsIdentifier> buildItemDefinitionComponentOptionsIdentifier(
			final ItemDefinitionComponentIdentifier identifier) {
		return Observable.just(ItemDefinitionComponentOptionsIdentifier.builder()
				.withItemDefinitionComponent(identifier)
				.build());
	}

	/**
	 * Checks if sku has options.
	 *
	 * @param skuCode skuCode
	 * @return true any options are present for this sku
	 */
	protected Single<Boolean> doesSkuContainOptions(final String skuCode) {
		return productSkuRepository.getProductSkuOptionsByCode(skuCode)
				.isEmpty()
				.map(isEmpty -> !isEmpty);
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@Reference
	public void setProductSkuRepository(final ProductSkuRepository productSkuRepository) {
		this.productSkuRepository = productSkuRepository;
	}
}
