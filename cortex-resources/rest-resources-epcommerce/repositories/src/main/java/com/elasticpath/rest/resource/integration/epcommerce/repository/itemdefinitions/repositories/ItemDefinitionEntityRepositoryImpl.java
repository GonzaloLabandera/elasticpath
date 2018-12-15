/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import java.util.Map;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Item Definition Entity Repository.
 *
 * @param <E> Item Definition entity
 * @param <I> Item Definition identifier
 */
@Component
public class ItemDefinitionEntityRepositoryImpl<E extends ItemDefinitionEntity, I extends ItemDefinitionIdentifier>
		implements Repository<ItemDefinitionEntity, ItemDefinitionIdentifier> {

	private ItemRepository itemRepository;
	private ConversionService conversionService;

	@Override
	public Single<ItemDefinitionEntity> findOne(final ItemDefinitionIdentifier identifier) {
		final Map<String, String> itemIdMap = identifier.getItemId().getValue();
		return itemRepository.getSkuForItemId(itemIdMap)
				.map(productSku -> conversionService.convert(productSku, ItemDefinitionEntity.class))
				.map(itemDefinitionEntity -> ItemDefinitionEntity.builderFrom(itemDefinitionEntity)
						.withItemId(CompositeIdUtil.encodeCompositeId(itemIdMap))
						.build());
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@Reference
	public void setConversionService(final ConversionService conversionService) {
		this.conversionService = conversionService;
	}
}
