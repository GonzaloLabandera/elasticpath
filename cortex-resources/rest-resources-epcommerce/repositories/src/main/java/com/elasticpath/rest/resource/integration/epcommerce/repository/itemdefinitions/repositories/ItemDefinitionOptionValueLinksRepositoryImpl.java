/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;

/**
 * Repository for item definition option to item definition option value link.
 *
 * @param <I>  the identifier type
 * @param <LI> the linked identifier type
 */
@Component
public class ItemDefinitionOptionValueLinksRepositoryImpl<I extends ItemDefinitionOptionIdentifier, LI extends ItemDefinitionOptionValueIdentifier>
		implements LinksRepository<ItemDefinitionOptionIdentifier, ItemDefinitionOptionValueIdentifier> {

	private static final String VALUE_NOT_FOUND = "Option value not found for optionId.";
	private ItemRepository itemRepository;
	private ReactiveAdapter reactiveAdapter;

	@Override
	public Observable<ItemDefinitionOptionValueIdentifier> getElements(final ItemDefinitionOptionIdentifier identifier) {
		final Map<String, String> itemIdMap = identifier.getItemDefinitionOptions().getItemDefinition().getItemId().getValue();
		final String optionId = identifier.getOptionId().getValue();

		return itemRepository.getSkuForItemId(itemIdMap)
				.flatMap(productSku -> getSkuOptionValue(productSku, optionId))
				.flatMapObservable(skuOptionValue -> buildItemDefinitionOptionValueIdentifier(skuOptionValue, identifier));
	}

	/**
	 * Get the sku option value for the optionId.
	 *
	 * @param productSku productSku
	 * @param optionId   optionId
	 * @return sku option value
	 */
	protected Single<SkuOptionValue> getSkuOptionValue(final ProductSku productSku, final String optionId) {
		return reactiveAdapter.fromNullableAsSingle(() -> productSku.getOptionValueMap().get(optionId), VALUE_NOT_FOUND);
	}

	/**
	 * Build a ItemDefinitionOptionValueIdentifier.
	 *
	 * @param skuOptionValue skuOptionValue
	 * @param identifier     ItemDefinitionOptionIdentifier
	 * @return a ItemDefinitionOptionValueIdentifier
	 */
	protected Observable<ItemDefinitionOptionValueIdentifier> buildItemDefinitionOptionValueIdentifier(
			final SkuOptionValue skuOptionValue, final ItemDefinitionOptionIdentifier identifier) {
		return Observable.just(ItemDefinitionOptionValueIdentifier.builder()
				.withItemDefinitionOption(identifier)
				.withOptionValueId(StringIdentifier.of(skuOptionValue.getOptionValueKey()))
				.build());
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}
}
