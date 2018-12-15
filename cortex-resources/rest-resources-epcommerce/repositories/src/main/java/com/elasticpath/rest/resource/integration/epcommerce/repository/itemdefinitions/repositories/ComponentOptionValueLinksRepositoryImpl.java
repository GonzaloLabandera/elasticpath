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

import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionValueIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;

/**
 * Repository for item definition component option to item definition component option value link.
 *
 * @param <I>  the identifier type
 * @param <LI> the linked identifier type
 */
@Component
public class ComponentOptionValueLinksRepositoryImpl
		<I extends ItemDefinitionComponentOptionIdentifier, LI extends ItemDefinitionComponentOptionValueIdentifier>
		implements LinksRepository<ItemDefinitionComponentOptionIdentifier, ItemDefinitionComponentOptionValueIdentifier> {

	private static final String VALUE_NOT_FOUND = "Option value not found for optionId.";
	private ItemRepository itemRepository;
	private ReactiveAdapter reactiveAdapter;

	@Override
	public Observable<ItemDefinitionComponentOptionValueIdentifier> getElements(final ItemDefinitionComponentOptionIdentifier identifier) {
		final ItemDefinitionComponentIdentifier componentIdentifier = identifier.getItemDefinitionComponentOptions().getItemDefinitionComponent();
		final Map<String, String> itemIdMap = componentIdentifier.getItemDefinitionComponents().getItemDefinition().getItemId().getValue();
		final String optionId = identifier.getOptionId().getValue();

		Iterator<String> guidPathFromRootItem = componentIdentifier.getComponentId().getValue().iterator();

		return itemRepository.findBundleConstituentAtPathEnd(itemIdMap, guidPathFromRootItem)
				.flatMap(bundleConstituent -> getSkuOptionValue(bundleConstituent, optionId))
				.flatMapObservable(skuOptionValue -> buildItemDefinitionOptionValueIdentifier(skuOptionValue, identifier));
	}

	/**
	 * Get the sku option value for the optionId.
	 *
	 * @param bundleConstituent bundleConstituent
	 * @param optionId          optionId
	 * @return sku option value
	 */
	protected Single<SkuOptionValue> getSkuOptionValue(final BundleConstituent bundleConstituent, final String optionId) {
		return reactiveAdapter.fromNullableAsSingle(() ->
				bundleConstituent.getConstituent().getProductSku().getOptionValueMap().get(optionId), VALUE_NOT_FOUND);
	}

	/**
	 * Build a ItemDefinitionComponentOptionValueIdentifier.
	 *
	 * @param skuOptionValue skuOptionValue
	 * @param identifier     ItemDefinitionComponentOptionIdentifier
	 * @return a ItemDefinitionComponentOptionValueIdentifier
	 */
	protected Observable<ItemDefinitionComponentOptionValueIdentifier> buildItemDefinitionOptionValueIdentifier(
			final SkuOptionValue skuOptionValue, final ItemDefinitionComponentOptionIdentifier identifier) {
		return Observable.just(ItemDefinitionComponentOptionValueIdentifier.builder()
				.withItemDefinitionComponentOption(identifier)
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
