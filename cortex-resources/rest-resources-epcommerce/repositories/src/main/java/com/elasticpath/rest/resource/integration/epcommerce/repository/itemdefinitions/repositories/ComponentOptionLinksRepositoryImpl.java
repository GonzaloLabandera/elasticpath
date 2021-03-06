/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import java.util.Iterator;
import java.util.Map;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;

/**
 * Repository for item definition component option links in item definition component options list.
 *
 * @param <I>  the identifier type
 * @param <LI> the linked identifier type
 */
@Component
public class ComponentOptionLinksRepositoryImpl
		<I extends ItemDefinitionComponentOptionsIdentifier, LI extends ItemDefinitionComponentOptionIdentifier>
		implements LinksRepository<ItemDefinitionComponentOptionsIdentifier, ItemDefinitionComponentOptionIdentifier> {

	private ItemRepository itemRepository;
	private ProductSkuRepository productSkuRepository;
	private static final Logger LOG = LoggerFactory.getLogger(ComponentOptionLinksRepositoryImpl.class);

	@Override
	public Observable<ItemDefinitionComponentOptionIdentifier> getElements(final ItemDefinitionComponentOptionsIdentifier identifier) {
		final ItemDefinitionComponentIdentifier itemDefinitionComponent = identifier.getItemDefinitionComponent();
		final Map<String, String> itemIdMap = itemDefinitionComponent.getItemDefinitionComponents().getItemDefinition().getItemId().getValue();
		Iterator<String> guidPathFromRootItem = itemDefinitionComponent.getComponentId().getValue().iterator();

		return itemRepository.findBundleConstituentAtPathEnd(itemIdMap, guidPathFromRootItem)
				.doOnError(throwable -> LOG.info("No bundle constituent found for item id '{}'.", itemIdMap))
				.map(bundleConstituent -> bundleConstituent.getConstituent().getProductSku().getSkuCode())
				.flatMapObservable(skuCode -> productSkuRepository.getProductSkuOptionsByCode(skuCode)
						.doOnError(throwable -> LOG.info("No sku option found for sku code '{}'.", skuCode)))
				.flatMap(skuOption -> buildItemDefinitionComponentOptionIdentifier(identifier, skuOption))
				.onErrorResumeNext(Observable.empty());
	}

	/**
	 * Build an ItemDefinitionComponentOptionIdentifier from an ItemDefinitionComponentOptionsIdentifier and a sku option.
	 *
	 * @param identifier ItemDefinitionComponentOptionsIdentifier
	 * @param skuOption the sku option
	 * @return ItemDefinitionComponentOptionIdentifier
	 */
	protected Observable<ItemDefinitionComponentOptionIdentifier> buildItemDefinitionComponentOptionIdentifier(
			final ItemDefinitionComponentOptionsIdentifier identifier, final SkuOption skuOption) {
		return Observable.just(ItemDefinitionComponentOptionIdentifier.builder()
				.withItemDefinitionComponentOptions(identifier)
				.withOptionId(StringIdentifier.of(skuOption.getGuid()))
				.build());
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
