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
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionEntity;
import com.elasticpath.rest.id.util.Base32Util;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;

/**
 * Item Definition Option Entity Repository for Components.
 *
 * @param <E> Item Definition Option entity
 * @param <I> Item Definition Component Option identifier
 */
@Component
public class ComponentOptionEntityRepositoryImpl<E extends ItemDefinitionOptionEntity, I extends ItemDefinitionComponentOptionIdentifier>
		implements Repository<ItemDefinitionOptionEntity, ItemDefinitionComponentOptionIdentifier> {

	private static final String VALUE_NOT_FOUND = "Option value not found for optionId.";
	private ItemRepository itemRepository;
	private ConversionService conversionService;
	private ReactiveAdapter reactiveAdapter;

	@Override
	public Single<ItemDefinitionOptionEntity> findOne(final ItemDefinitionComponentOptionIdentifier identifier) {
		final ItemDefinitionComponentIdentifier componentIdentifier = identifier.getItemDefinitionComponentOptions().getItemDefinitionComponent();
		final Map<String, String> itemIdMap = componentIdentifier.getItemDefinitionComponents().getItemDefinition().getItemId().getValue();
		final String optionId = identifier.getOptionId().getValue();

		Iterator<String> guidPathFromRootItem = componentIdentifier.getComponentId().getValue().iterator();

		return itemRepository.findBundleConstituentAtPathEnd(itemIdMap, guidPathFromRootItem)
				.flatMap(bundleConstituent -> getSkuOptionValue(bundleConstituent, optionId)
						.map(skuOptionValue -> buildItemDefinitionOptionEntity(skuOptionValue, bundleConstituent, itemIdMap)));
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
	 * Build a ItemDefinitionEntity.
	 *
	 * @param skuOptionValue    skuOptionValue
	 * @param bundleConstituent bundleConstituent
	 * @param itemIdMap         itemIdMap
	 * @return a ItemDefinitionEntity
	 */
	protected ItemDefinitionOptionEntity buildItemDefinitionOptionEntity(
			final SkuOptionValue skuOptionValue, final BundleConstituent bundleConstituent, final Map<String, String> itemIdMap) {
		return ItemDefinitionOptionEntity.builderFrom(conversionService.convert(skuOptionValue, ItemDefinitionOptionEntity.class))
				.withItemId(CompositeIdUtil.encodeCompositeId(itemIdMap))
				.withComponentId(Base32Util.encode(bundleConstituent.getGuid()))
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
