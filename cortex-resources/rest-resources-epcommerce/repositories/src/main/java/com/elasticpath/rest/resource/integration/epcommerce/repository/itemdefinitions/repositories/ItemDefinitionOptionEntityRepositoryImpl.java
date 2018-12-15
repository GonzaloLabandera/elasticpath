/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import java.util.Map;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;

/**
 * Item Definition Option Entity Repository.
 *
 * @param <E> Item Definition Option entity
 * @param <I> Item Definition Option identifier
 */
@Component
public class ItemDefinitionOptionEntityRepositoryImpl<E extends ItemDefinitionOptionEntity, I extends ItemDefinitionOptionIdentifier>
		implements Repository<ItemDefinitionOptionEntity, ItemDefinitionOptionIdentifier> {

	private static final String VALUE_NOT_FOUND = "Option value not found for optionId.";
	private ItemRepository itemRepository;
	private ConversionService conversionService;
	private ReactiveAdapter reactiveAdapter;

	@Override
	public Single<ItemDefinitionOptionEntity> findOne(final ItemDefinitionOptionIdentifier identifier) {
		final Map<String, String> itemIdMap = identifier.getItemDefinitionOptions().getItemDefinition().getItemId().getValue();
		final String optionId = identifier.getOptionId().getValue();

		return itemRepository.getSkuForItemId(itemIdMap)
				.flatMap(productSku -> getSkuOptionValue(productSku, optionId))
				.map(skuOptionValue -> buildItemDefinitionOptionEntity(skuOptionValue, itemIdMap));
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
	 * Build a ItemDefinitionEntity.
	 *
	 * @param skuOptionValue skuOptionValue
	 * @param itemIdMap      itemIdMap
	 * @return a ItemDefinitionEntity
	 */
	protected ItemDefinitionOptionEntity buildItemDefinitionOptionEntity(
			final SkuOptionValue skuOptionValue, final Map<String, String> itemIdMap) {
		return ItemDefinitionOptionEntity.builderFrom(conversionService.convert(skuOptionValue, ItemDefinitionOptionEntity.class))
				.withItemId(CompositeIdUtil.encodeCompositeId(itemIdMap))
				.build();
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@Reference
	public void setConversionService(final ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}
}
