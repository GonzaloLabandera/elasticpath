/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemselections.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.repository.GenericSelectorRepository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionsIdentifier;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.option.SkuOptionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.rest.selector.Choice;
import com.elasticpath.rest.selector.ChoiceStatus;
import com.elasticpath.rest.selector.SelectResult;
import com.elasticpath.rest.selector.SelectStatus;
import com.elasticpath.rest.selector.SelectorChoice;
import com.elasticpath.service.catalog.MultiSkuProductConfigurationService;

/**
 * Implementation of a repository for selecting a item option value.
 *
 * @param <SI> the selector identifier type
 * @param <CI> the choice identifier type
 * @param <RI> the result identifier type
 */
@Component
public class ItemOptionSelectorRepositoryImpl
		<SI extends ItemOptionSelectorIdentifier, CI extends ItemOptionSelectorChoiceIdentifier, RI extends ItemIdentifier>
		implements GenericSelectorRepository<ItemOptionSelectorIdentifier, ItemOptionSelectorChoiceIdentifier, ItemIdentifier> {

	private static final String VALUE_NOT_FOUND = "Option value not found for optionId.";
	private static final String OPTION_VALUE_SELECTION_NOT_FOUND = "Cannot find matching selection.";
	private ItemRepository itemRepository;
	private StoreProductRepository storeProductRepository;
	private MultiSkuProductConfigurationService multiSkuProductConfigurationService;
	private ProductSkuRepository productSkuRepository;
	private SkuOptionRepository skuOptionRepository;
	private ReactiveAdapter reactiveAdapter;

	@Override
	public Observable<SelectorChoice> getChoices(final ItemOptionSelectorIdentifier itemOptionSelectorIdentifier) {
		final Map<String, String> itemIdMap = itemOptionSelectorIdentifier.getItemId().getValue();
		final String optionId = itemOptionSelectorIdentifier.getOptionId().getValue();
		return itemRepository.getSkuForItemId(itemIdMap)
				.flatMapObservable(productSku -> getSelectedOptionValue(productSku, optionId)
						.flatMapObservable(selectedOptionValue ->
								getSelectorChoices(itemOptionSelectorIdentifier, productSku, selectedOptionValue)));
	}

	/**
	 * Get the selector choices.
	 *
	 * @param itemOptionSelectorIdentifier itemOptionSelectorIdentifier
	 * @param productSku                   productSku
	 * @param selectedOptionValue          selectedOptionValue
	 * @return selector choices
	 */
	protected Observable<SelectorChoice> getSelectorChoices(final ItemOptionSelectorIdentifier itemOptionSelectorIdentifier,
															final ProductSku productSku,
															final SkuOptionValue selectedOptionValue) {
		final String optionId = itemOptionSelectorIdentifier.getOptionId().getValue();
		final String scope = itemOptionSelectorIdentifier.getScope().getValue();

		return getStoreProduct(scope, productSku)
				.flatMapObservable(storeProduct -> getChoicesOptionValues(storeProduct, productSku, optionId, selectedOptionValue))
				.map(skuOptionValue -> buildSelectorChoice(itemOptionSelectorIdentifier, skuOptionValue.getGuid(), selectedOptionValue.getGuid()));
	}


	/**
	 * Get the sku option value for the optionId.
	 *
	 * @param productSku productSku
	 * @param optionId   optionId
	 * @return sku option value
	 */
	protected Single<SkuOptionValue> getSelectedOptionValue(final ProductSku productSku, final String optionId) {
		return reactiveAdapter.fromNullableAsSingle(() -> productSku.getOptionValueMap().get(optionId), VALUE_NOT_FOUND);
	}

	/**
	 * Get the store product for the given product and store scope.
	 *
	 * @param scope      scope
	 * @param productSku productSku
	 * @return store product
	 */
	protected Single<StoreProduct> getStoreProduct(final String scope, final ProductSku productSku) {
		return storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(scope, productSku.getProduct().getGuid());
	}

	/**
	 * Get the selectable and selected option values.
	 *
	 * @param storeProduct        storeProduct
	 * @param productSku          productSku
	 * @param optionId            optionId
	 * @param selectedOptionValue selectedOptionValue
	 * @return choices option values
	 */
	protected Observable<SkuOptionValue> getChoicesOptionValues(final StoreProduct storeProduct,
																final ProductSku productSku,
																final String optionId,
																final SkuOptionValue selectedOptionValue) {

		return reactiveAdapter.fromService(() -> getSelectableOptionValues(storeProduct, productSku, optionId))
				.map(choicesOptionValues -> addSelectedOptionValue(choicesOptionValues, selectedOptionValue))
				.flatMap(Observable::fromIterable);
	}

	/**
	 * Get the option values that are available to be selected.
	 *
	 * @param storeProduct storeProduct
	 * @param productSku   productSku
	 * @param optionId     optionId
	 * @return selectable option values
	 */
	protected Collection<SkuOptionValue> getSelectableOptionValues(final StoreProduct storeProduct,
																   final ProductSku productSku,
																   final String optionId) {
		final Collection<SkuOptionValue> selectedValuesForOtherOptions = getSelectedValuesForOtherOptions(productSku, optionId);
		return multiSkuProductConfigurationService.getAvailableOptionValuesForOption(storeProduct, optionId, selectedValuesForOtherOptions);
	}

	/**
	 * If the currently selected option is not available, add it to the list of choices option values.
	 *
	 * @param choicesOptionValues choicesOptionValues
	 * @param selectedOptionValue selectedOptionValue
	 * @return choicesOptionValues
	 */
	protected Collection<SkuOptionValue> addSelectedOptionValue(final Collection<SkuOptionValue> choicesOptionValues,
																final SkuOptionValue selectedOptionValue) {

		// PB-1829 It is possible the currently selected option is not available, so add it to the list
		// to avoid a 500 error.
		if (selectedOptionValue != null && !choicesOptionValues.contains(selectedOptionValue)) {
			choicesOptionValues.add(selectedOptionValue);
		}

		return choicesOptionValues;
	}

	/**
	 * Get the selected values for sku options that doesn't match the given sku option key.
	 *
	 * @param productSku   productSku
	 * @param skuOptionKey skuOptionKey
	 * @return selected values for other sku options
	 */
	protected Collection<SkuOptionValue> getSelectedValuesForOtherOptions(final ProductSku productSku, final String skuOptionKey) {
		return productSku.getOptionValueMap().values().stream()
				.filter(skuOptionValue -> !skuOptionValue.getSkuOption().getOptionKey().equals(skuOptionKey))
				.collect(Collectors.toList());
	}

	private ChoiceStatus getChoiceStatus(final String skuOptionValueId, final String selectedSkuOptionValueId) {
		return selectedSkuOptionValueId.equals(skuOptionValueId) ? ChoiceStatus.CHOSEN : ChoiceStatus.CHOOSABLE;
	}

	/**
	 * Build the selector choice.
	 *
	 * @param itemOptionSelectorIdentifier itemOptionSelectorIdentifier
	 * @param skuOptionValueId             skuOptionValueId
	 * @param selectedOptionValueId        selectedOptionValueId
	 * @return the selector choice
	 */
	protected SelectorChoice buildSelectorChoice(final ItemOptionSelectorIdentifier itemOptionSelectorIdentifier,
												 final String skuOptionValueId,
												 final String selectedOptionValueId) {
		return SelectorChoice.builder()
				.withChoice(buildItemSelectionsOptionValueSelectorChoiceIdentifier(itemOptionSelectorIdentifier, skuOptionValueId))
				.withStatus(getChoiceStatus(skuOptionValueId, selectedOptionValueId))
				.build();
	}

	/**
	 * Build the option selector choice identifier.
	 *
	 * @param itemOptionSelectorIdentifier itemOptionSelectorIdentifier
	 * @param skuOptionValueId             skuOptionValueId
	 * @return the option value selector choice identifier
	 */
	protected ItemOptionSelectorChoiceIdentifier buildItemSelectionsOptionValueSelectorChoiceIdentifier(
			final ItemOptionSelectorIdentifier itemOptionSelectorIdentifier,
			final String skuOptionValueId) {
		return ItemOptionSelectorChoiceIdentifier.builder()
				.withItemOptionSelector(itemOptionSelectorIdentifier)
				.withOptionValueId(StringIdentifier.of(skuOptionValueId))
				.build();
	}

	@Override
	public Single<Choice> getChoice(final ItemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier) {
		Map<String, String> itemIdMap = itemOptionSelectorChoiceIdentifier.getItemOptionSelector().getItemId().getValue();
		String optionId = itemOptionSelectorChoiceIdentifier.getItemOptionSelector().getOptionId().getValue();
		String optionValueId = itemOptionSelectorChoiceIdentifier.getOptionValueId().getValue();

		return itemRepository.getSkuForItemId(itemIdMap)
				.flatMap(productSku -> getSelectedOptionValue(productSku, optionId))
				.map(selectedOptionValue -> buildChoice(itemOptionSelectorChoiceIdentifier, optionValueId, selectedOptionValue.getGuid()));
	}

	/**
	 * Build the choice.
	 *
	 * @param itemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier
	 * @param optionValueId                      optionValueId
	 * @param selectedOptionValueId              selectedOptionValueId
	 * @return the choice
	 */
	protected Choice buildChoice(final ItemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier,
								 final String optionValueId,
								 final String selectedOptionValueId) {
		return Choice.builder()
				.withDescription(buildItemDefinitionOptionValueIdentifier(itemOptionSelectorChoiceIdentifier))
				.withAction(itemOptionSelectorChoiceIdentifier)
				.withStatus(getChoiceStatus(optionValueId, selectedOptionValueId))
				.build();
	}

	/**
	 * Build a ItemDefinitionOptionValueIdentifier.
	 *
	 * @param itemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier
	 * @return a ItemDefinitionOptionValueIdentifier
	 */
	protected ItemDefinitionOptionValueIdentifier buildItemDefinitionOptionValueIdentifier(
			final ItemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier) {
		IdentifierPart<Map<String, String>> itemId = itemOptionSelectorChoiceIdentifier.getItemOptionSelector().getItemId();
		IdentifierPart<String> scope = itemOptionSelectorChoiceIdentifier.getItemOptionSelector().getScope();
		IdentifierPart<String> optionId = itemOptionSelectorChoiceIdentifier.getItemOptionSelector().getOptionId();
		IdentifierPart<String> optionValueId = itemOptionSelectorChoiceIdentifier.getOptionValueId();

		return ItemDefinitionOptionValueIdentifier.builder()
				.withItemDefinitionOption(buildItemDefinitionOptionIdentifier(itemId, scope, optionId))
				.withOptionValueId(optionValueId)
				.build();
	}

	/**
	 * Build a ItemDefinitionOptionIdentifier.
	 *
	 * @param itemId   itemId
	 * @param scope    scope
	 * @param optionId optionId
	 * @return a ItemDefinitionOptionIdentifier
	 */
	protected ItemDefinitionOptionIdentifier buildItemDefinitionOptionIdentifier(final IdentifierPart<Map<String, String>> itemId,
																				 final IdentifierPart<String> scope,
																				 final IdentifierPart<String> optionId) {
		return ItemDefinitionOptionIdentifier.builder()
				.withItemDefinitionOptions(buildItemDefinitionOptionsIdentifier(itemId, scope))
				.withOptionId(optionId)
				.build();
	}

	/**
	 * Build a ItemDefinitionOptionsIdentifier.
	 *
	 * @param itemId itemId
	 * @param scope  scope
	 * @return a ItemDefinitionOptionsIdentifier
	 */
	protected ItemDefinitionOptionsIdentifier buildItemDefinitionOptionsIdentifier(final IdentifierPart<Map<String, String>> itemId,
																				   final IdentifierPart<String> scope) {
		return ItemDefinitionOptionsIdentifier.builder()
				.withItemDefinition(ItemDefinitionIdentifier.builder()
						.withScope(scope)
						.withItemId(itemId)
						.build())
				.build();
	}

	@Override
	public Single<SelectResult<ItemIdentifier>> selectChoice(
			final ItemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier) {
		final Map<String, String> itemIdMap = itemOptionSelectorChoiceIdentifier.getItemOptionSelector().getItemId().getValue();
		final IdentifierPart<String> scope = itemOptionSelectorChoiceIdentifier.getItemOptionSelector().getScope();
		final String selectorOptionId = itemOptionSelectorChoiceIdentifier.getItemOptionSelector().getOptionId().getValue();
		final String selectedOptionValueId = itemOptionSelectorChoiceIdentifier.getOptionValueId().getValue();

		return itemRepository.getSkuForItemId(itemIdMap)
				.flatMap(productSku -> getSelectedItemId(scope, selectorOptionId, selectedOptionValueId, productSku))
				.map(itemId -> buildSelectResult(scope, itemId));
	}

	/**
	 * Get the item id of the selected option choice.
	 *
	 * @param scope                 scope
	 * @param selectorOptionId      selectorOptionId
	 * @param selectedOptionValueId selectedOptionValueId
	 * @param productSku            productSku
	 * @return item id
	 */
	protected Single<IdentifierPart<Map<String, String>>> getSelectedItemId(final IdentifierPart<String> scope,
																			final String selectorOptionId,
																			final String selectedOptionValueId,
																			final ProductSku productSku) {
		return getStoreProduct(scope.getValue(), productSku)
				.flatMap(storeProduct -> getSkuOptionValues(selectorOptionId, selectedOptionValueId, productSku)
						.flatMap(skuOptionValues -> findNewItemId(storeProduct, skuOptionValues)));
	}

	/**
	 * Get the option values of the newly selected item.
	 *
	 * @param selectorOptionId      selectorOptionId
	 * @param selectedOptionValueId selectedOptionValueId
	 * @param productSku            productSku
	 * @return option values
	 */
	protected Single<Collection<SkuOptionValue>> getSkuOptionValues(final String selectorOptionId,
																	final String selectedOptionValueId,
																	final ProductSku productSku) {
		List<SkuOptionValue> unchangedOptionValues = productSku.getOptionValues().stream()
				.filter(skuOptionValue -> !skuOptionValue.getSkuOption().getOptionKey().equals(selectorOptionId))
				.collect(Collectors.toList());

		return skuOptionRepository.findSkuOptionValueByKey(selectorOptionId, selectedOptionValueId)
				.map(selectedOptionValue -> addSelectedOptionValueToAllOptionValues(unchangedOptionValues, selectedOptionValue));
	}

	/**
	 * Add the selected option value to the collection of unchanged option values
	 * to get a full collection of option values that we want the newly selected item to have.
	 *
	 * @param optionValueSelection optionValueSelection
	 * @param selectedOptionValue  selectedOptionValue
	 * @return collection of option values
	 */
	protected Collection<SkuOptionValue> addSelectedOptionValueToAllOptionValues(final Collection<SkuOptionValue> optionValueSelection,
																				 final SkuOptionValue selectedOptionValue) {
		optionValueSelection.add(selectedOptionValue);
		return optionValueSelection;
	}

	/**
	 * Find the item id of product sku that matches the collection of option values.
	 *
	 * @param storeProduct         storeProduct
	 * @param optionValueSelection optionValueSelection
	 * @return item id
	 */
	protected Single<IdentifierPart<Map<String, String>>> findNewItemId(final StoreProduct storeProduct,
																		final Collection<SkuOptionValue> optionValueSelection) {

		return findSkuGuidMatchingOptionValueSelection(storeProduct, optionValueSelection)
				.flatMap(selectedSkuGuid -> productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(selectedSkuGuid))
				.map(productSku -> itemRepository.getItemIdForProductSku(productSku));
	}

	/**
	 * Find the sku guid that matches collection of option values.
	 *
	 * @param storeProduct         storeProduct
	 * @param optionValueSelection optionValueSelection
	 * @return sku guid
	 */
	protected Single<String> findSkuGuidMatchingOptionValueSelection(final StoreProduct storeProduct,
																	 final Collection<SkuOptionValue> optionValueSelection) {
		return reactiveAdapter.fromServiceAsSingle(() ->
				findSkuGuidsMatchingSelectedOptions(storeProduct, optionValueSelection), OPTION_VALUE_SELECTION_NOT_FOUND)
				.filter(matchingSelectionSkuGuids -> matchingSelectionSkuGuids.size() == 1)
				.switchIfEmpty(Maybe.error(ResourceOperationFailure.notFound(OPTION_VALUE_SELECTION_NOT_FOUND)))
				.flatMapObservable(Observable::fromIterable)
				.firstOrError();
	}

	private Collection<String> findSkuGuidsMatchingSelectedOptions(final StoreProduct storeProduct,
																   final Collection<SkuOptionValue> optionValueSelection) {
		return multiSkuProductConfigurationService.findSkuGuidsMatchingSelectedOptions(storeProduct, optionValueSelection);
	}

	/**
	 * Build a SelectResult.
	 *
	 * @param scope  scope
	 * @param itemId itemId
	 * @return SelectResult
	 */
	protected SelectResult<ItemIdentifier> buildSelectResult(final IdentifierPart<String> scope,
															 final IdentifierPart<Map<String, String>> itemId) {
		return SelectResult.<ItemIdentifier>builder()
				.withIdentifier(ItemIdentifier.builder()
						.withItemId(itemId)
						.withScope(scope)
						.build())
				.withStatus(SelectStatus.SELECTED)
				.build();
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@Reference
	public void setStoreProductRepository(final StoreProductRepository storeProductRepository) {
		this.storeProductRepository = storeProductRepository;
	}

	@Reference
	public void setMultiSkuProductConfigurationService(final MultiSkuProductConfigurationService multiSkuProductConfigurationService) {
		this.multiSkuProductConfigurationService = multiSkuProductConfigurationService;
	}

	@Reference
	public void setProductSkuRepository(final ProductSkuRepository productSkuRepository) {
		this.productSkuRepository = productSkuRepository;
	}

	@Reference
	public void setSkuOptionRepository(final SkuOptionRepository skuOptionRepository) {
		this.skuOptionRepository = skuOptionRepository;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}
}
