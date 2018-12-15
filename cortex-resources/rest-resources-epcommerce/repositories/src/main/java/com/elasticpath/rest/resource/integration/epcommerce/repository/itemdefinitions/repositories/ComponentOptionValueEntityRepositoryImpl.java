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
import org.springframework.core.convert.ConversionService;

import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionValueIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Item Definition Option Value Entity Repository for Components.
 *
 * @param <E> Item Definition Component Option Value entity
 * @param <I> Item Definition Component Option Value identifier
 */
@Component
public class ComponentOptionValueEntityRepositoryImpl
		<E extends ItemDefinitionOptionValueEntity, I extends ItemDefinitionComponentOptionValueIdentifier>
		implements Repository<ItemDefinitionOptionValueEntity, ItemDefinitionComponentOptionValueIdentifier> {

	private static final String OPTION_NOT_FOUND_FOR_ITEM = "Option not found for item.";
	private ItemRepository itemRepository;
	private ConversionService conversionService;

	@Override
	public Single<ItemDefinitionOptionValueEntity> findOne(final ItemDefinitionComponentOptionValueIdentifier identifier) {
		final ItemDefinitionComponentOptionIdentifier optionIdentifier = identifier.getItemDefinitionComponentOption();
		final ItemDefinitionComponentIdentifier componentIdentifier =
				optionIdentifier.getItemDefinitionComponentOptions().getItemDefinitionComponent();
		final Map<String, String> itemIdMap = componentIdentifier.getItemDefinitionComponents().getItemDefinition().getItemId().getValue();
		final String optionId = optionIdentifier.getOptionId().getValue();
		final String optionValueId = identifier.getOptionValueId().getValue();

		Iterator<String> guidPathFromRootItem = componentIdentifier.getComponentId().getValue().iterator();

		return getSkuOptions(itemIdMap, guidPathFromRootItem)
				.filter(skuOption -> containsOptionValue(skuOption, optionId, optionValueId))
				.firstOrError()
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(OPTION_NOT_FOUND_FOR_ITEM)))
				.map(skuOption -> skuOption.getOptionValue(optionValueId))
				.map(skuOptionValue -> conversionService.convert(skuOptionValue, ItemDefinitionOptionValueEntity.class));
	}

	/**
	 * Get the SkuOption for the component at the end of the guid path.
	 *
	 * @param itemIdMap            itemIdMap
	 * @param guidPathFromRootItem guidPathFromRootItem
	 * @return SkuOption
	 */
	protected Observable<SkuOption> getSkuOptions(final Map<String, String> itemIdMap, final Iterator<String> guidPathFromRootItem) {
		return itemRepository.findBundleConstituentAtPathEnd(itemIdMap, guidPathFromRootItem)
				.map(bundleConstituent -> bundleConstituent.getConstituent().getProduct().getProductType().getSkuOptions())
				.flatMapObservable(Observable::fromIterable);
	}

	/**
	 * Check if SkuOption key matches the given optionKey and if SkuOption contains the valueCode.
	 *
	 * @param skuOption skuOption
	 * @param optionKey optionKey
	 * @param valueCode valueCode
	 * @return if SkuOption key matches the given optionKey and if SkuOption contains the valueCode
	 */
	protected boolean containsOptionValue(final SkuOption skuOption, final String optionKey, final String valueCode) {
		return skuOption.getOptionKey().equals(optionKey) && skuOption.contains(valueCode);
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
