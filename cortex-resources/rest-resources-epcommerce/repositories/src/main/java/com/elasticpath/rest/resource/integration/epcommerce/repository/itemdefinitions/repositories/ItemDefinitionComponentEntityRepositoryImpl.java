/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import java.util.Iterator;
import java.util.Map;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.id.util.Base32Util;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Item Definition Component Entity Repository.
 *
 * @param <E> Item Definition Component entity
 * @param <I> Item Definition Component identifier
 */
@Component
public class ItemDefinitionComponentEntityRepositoryImpl<E extends ItemDefinitionComponentEntity, I extends ItemDefinitionComponentIdentifier>
		implements Repository<ItemDefinitionComponentEntity, ItemDefinitionComponentIdentifier> {

	private ItemRepository itemRepository;
	private ConversionService conversionService;

	@Override
	public Single<ItemDefinitionComponentEntity> findOne(final ItemDefinitionComponentIdentifier identifier) {
		final Map<String, String> itemIdMap = identifier.getItemDefinitionComponents().getItemDefinition().getItemId().getValue();
		final Iterator<String> guidPathFromRootItem = identifier.getComponentId().getValue().iterator();

		return itemRepository.findBundleConstituentAtPathEnd(itemIdMap, guidPathFromRootItem)
				.flatMap(bundleConstituent -> buildItemDefinitionComponentEntity(bundleConstituent, itemIdMap));
	}

	/**
	 * Build an ItemDefinitionComponentEntity.
	 *
	 * @param bundleConstituent bundleConstituent
	 * @param itemIdMap         itemIdMap
	 * @return an ItemDefinitionComponentEntity
	 */
	protected Single<ItemDefinitionComponentEntity> buildItemDefinitionComponentEntity(
			final BundleConstituent bundleConstituent, final Map<String, String> itemIdMap) {
		return Single.just(ItemDefinitionComponentEntity
				.builderFrom(conversionService.convert(bundleConstituent, ItemDefinitionComponentEntity.class))
				.withStandaloneItemId(itemRepository.getItemIdForSku(bundleConstituent.getConstituent().getProductSku()))
				.withItemId(CompositeIdUtil.encodeCompositeId(itemIdMap))
				.withComponentId(Base32Util.encode(bundleConstituent.getGuid()))
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
