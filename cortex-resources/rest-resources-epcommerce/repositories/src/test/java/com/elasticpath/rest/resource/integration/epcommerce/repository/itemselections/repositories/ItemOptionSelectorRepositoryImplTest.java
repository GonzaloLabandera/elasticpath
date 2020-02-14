/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemselections.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.option.SkuOptionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.rest.selector.ChoiceStatus;
import com.elasticpath.rest.selector.SelectResult;
import com.elasticpath.rest.selector.SelectStatus;
import com.elasticpath.service.catalog.MultiSkuProductConfigurationService;

/**
 * Test for {@link ItemOptionSelectorRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemOptionSelectorRepositoryImplTest {

	private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found.";
	private static final String VALUE_NOT_FOUND = "Option value not found for optionId.";
	private static final String STORE_PRODUCT_NOT_FOUND = "Store product not found";
	private static final String CANNOT_FIND_OPTION_VALUE = "Cannot find option value.";
	private static final String OPTION_VALUE_SELECTION_NOT_FOUND = "Cannot find matching selection.";
	private static final String PRODUCT_SKU_NOT_FOUND = "Product sku not found.";
	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final String SELECTOR_OPTION_ID = "selectorOptionId";
	private static final String OTHER_OPTION_ID = "otherOptionId";
	private static final String SELECTED_OPTION_VALUE_GUID = "selectedOptionValueGuid";
	private static final String OTHER_OPTION_VALUE_GUID = "otherOptionValueGuid";
	private static final String PRODUCT_GUID = "productGuid";
	private static final String PRODUCT_SKU_GUID = "productSkuGuid";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);
	private static final IdentifierPart<Map<String, String>> SELECTED_ITEM_ID =
			CompositeIdentifier.of(ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE));
	private ItemOptionSelectorIdentifier itemOptionSelectorIdentifier;
	private ItemOptionSelectorChoiceIdentifier itemOptionSelectorChoiceIdentifier;

	@Mock
	private ItemRepository itemRepository;
	@Mock
	private StoreProductRepository storeProductRepository;
	@Mock
	private MultiSkuProductConfigurationService multiSkuProductConfigurationService;
	@Mock
	private ProductSkuRepository productSkuRepository;
	@Mock
	private SkuOptionRepository skuOptionRepository;
	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;
	@InjectMocks
	private ItemOptionSelectorRepositoryImpl<ItemOptionSelectorIdentifier, ItemOptionSelectorChoiceIdentifier, ItemIdentifier> repository;

	@Mock
	private ProductSku productSku;
	@Mock
	private ProductSku selectedProductSku;
	@Mock
	private Product product;
	@Mock
	private StoreProduct storeProduct;
	@Mock
	private Map<String, SkuOptionValue> optionValueMap;
	@Mock
	private SkuOptionValue selectedSkuOptionValue;
	@Mock
	private SkuOptionValue otherSkuOptionValue;
	@Mock
	private SkuOption selectorSkuOption;
	@Mock
	private SkuOption otherSkuOption;

	@Before
	public void setUp() {
		repository.setReactiveAdapter(reactiveAdapter);
		itemOptionSelectorIdentifier = ItemSelectionsTestFactory.buildItemOptionSelectorIdentifier(SCOPE, SKU_CODE, SELECTOR_OPTION_ID);
		itemOptionSelectorChoiceIdentifier =
				ItemSelectionsTestFactory.buildItemOptionSelectorChoiceIdentifier(SCOPE, SKU_CODE, SELECTOR_OPTION_ID, SELECTED_OPTION_VALUE_GUID);

		when(productSku.getOptionValueMap()).thenReturn(optionValueMap);
		when(productSku.getProduct()).thenReturn(product);
		when(product.getGuid()).thenReturn(PRODUCT_GUID);
		when(optionValueMap.get(SELECTOR_OPTION_ID)).thenReturn(selectedSkuOptionValue);
		when(optionValueMap.values()).thenReturn(Arrays.asList(selectedSkuOptionValue, otherSkuOptionValue));
		when(selectedSkuOptionValue.getGuid()).thenReturn(SELECTED_OPTION_VALUE_GUID);
		when(selectedSkuOptionValue.getSkuOption()).thenReturn(selectorSkuOption);
		when(otherSkuOptionValue.getGuid()).thenReturn(OTHER_OPTION_VALUE_GUID);
		when(otherSkuOptionValue.getSkuOption()).thenReturn(otherSkuOption);
		when(selectorSkuOption.getOptionKey()).thenReturn(SELECTOR_OPTION_ID);
		when(otherSkuOption.getOptionKey()).thenReturn(OTHER_OPTION_ID);

		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.just(productSku));
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, productSku.getProduct().getGuid()))
				.thenReturn(Single.just(storeProduct));
		when(multiSkuProductConfigurationService.getAvailableOptionValuesForOption(eq(storeProduct), eq(SELECTOR_OPTION_ID), any()))
				.thenReturn(Lists.newArrayList(otherSkuOptionValue));
		when(skuOptionRepository.findSkuOptionValueByKey(SELECTOR_OPTION_ID, SELECTED_OPTION_VALUE_GUID))
				.thenReturn(Single.just(selectedSkuOptionValue));
		when(multiSkuProductConfigurationService.findSkuGuidsMatchingSelectedOptions(eq(storeProduct), any()))
				.thenReturn(Collections.singletonList(PRODUCT_SKU_GUID));
		when(productSkuRepository.getProductSkuWithAttributesByGuid(PRODUCT_SKU_GUID)).thenReturn(Single.just(selectedProductSku));
		when(itemRepository.getItemIdForProductSku(selectedProductSku)).thenReturn(SELECTED_ITEM_ID);
	}

	@Test
	public void getChoiceShouldReturnChoiceWithChosenStatus() {

		repository.getChoice(itemOptionSelectorChoiceIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choice -> choice.getStatus() == ChoiceStatus.CHOSEN);
	}

	@Test
	public void getChoiceShouldReturnChoiceWithChoosableStatus() {
		when(optionValueMap.get(SELECTOR_OPTION_ID)).thenReturn(otherSkuOptionValue);
		repository.getChoice(itemOptionSelectorChoiceIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choice -> choice.getStatus() == ChoiceStatus.CHOOSABLE);
	}

	@Test
	public void getChoiceShouldReturnErrorWhenItemNotFound() {
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.error(ResourceOperationFailure.notFound(ITEM_NOT_FOUND_MESSAGE)));
		repository.getChoice(itemOptionSelectorChoiceIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(ITEM_NOT_FOUND_MESSAGE))
				.assertNoValues();
	}

	@Test
	public void getChoiceShouldReturnErrorWhenSelectedSkuOptionValueNotFound() {
		when(optionValueMap.get(SELECTOR_OPTION_ID)).thenReturn(null);
		repository.getChoice(itemOptionSelectorChoiceIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(VALUE_NOT_FOUND))
				.assertNoValues();
	}

	@Test
	public void getChoicesShouldReturnListOfChoicesWhenSelectedSkuOptionValueExists() {
		repository.getChoices(itemOptionSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertValueCount(2)
				.assertValueAt(0, selectorChoice -> selectorChoice.getStatus() == ChoiceStatus.CHOOSABLE)
				.assertValueAt(1, selectorChoice -> selectorChoice.getStatus() == ChoiceStatus.CHOSEN);
	}

	@Test
	public void getChoicesShouldReturnErrorWhenItemNotFound() {
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.error(ResourceOperationFailure.notFound(ITEM_NOT_FOUND_MESSAGE)));

		repository.getChoices(itemOptionSelectorIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(ITEM_NOT_FOUND_MESSAGE))
				.assertNoValues();
	}

	@Test
	public void getChoicesShouldReturnErrorWhenSelectedSkuOptionValueNotFound() {
		when(optionValueMap.get(SELECTOR_OPTION_ID)).thenReturn(null);
		repository.getChoices(itemOptionSelectorIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(VALUE_NOT_FOUND))
				.assertNoValues();
	}

	@Test
	public void getChoicesShouldReturnErrorWhenStoreProductNotFound() {
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, productSku.getProduct().getGuid()))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(STORE_PRODUCT_NOT_FOUND)));
		repository.getChoices(itemOptionSelectorIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(STORE_PRODUCT_NOT_FOUND))
				.assertNoValues();
	}

	@Test
	public void selectChoiceShouldComplete() {
		repository.selectChoice(itemOptionSelectorChoiceIdentifier)
				.test()
				.assertComplete()
				.assertNoErrors()
				.assertValue(SelectResult.<ItemIdentifier>builder()
						.withIdentifier(ItemIdentifier.builder()
								.withItemId(SELECTED_ITEM_ID)
								.withScope(StringIdentifier.of(SCOPE))
								.build())
						.withStatus(SelectStatus.SELECTED)
						.build());
	}

	@Test
	public void selectChoiceShouldReturnErrorWhenStoreProductNotFound() {
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, productSku.getProduct().getGuid()))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(STORE_PRODUCT_NOT_FOUND)));
		repository.selectChoice(itemOptionSelectorChoiceIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(STORE_PRODUCT_NOT_FOUND))
				.assertNoValues();
	}

	@Test
	public void selectChoiceShouldReturnErrorWhenSktOptionValueNotFound() {
		when(skuOptionRepository.findSkuOptionValueByKey(SELECTOR_OPTION_ID, SELECTED_OPTION_VALUE_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(CANNOT_FIND_OPTION_VALUE)));
		repository.selectChoice(itemOptionSelectorChoiceIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(CANNOT_FIND_OPTION_VALUE))
				.assertNoValues();
	}

	@Test
	public void selectChoiceShouldReturnErrorWhenItemNotFound() {
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.error(ResourceOperationFailure.notFound(ITEM_NOT_FOUND_MESSAGE)));
		repository.selectChoice(itemOptionSelectorChoiceIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(ITEM_NOT_FOUND_MESSAGE))
				.assertNoValues();
	}

	@Test
	public void selectChoiceShouldReturnErrorWhenNoSkuGuidsMatchSelectedOptions() {
		when(multiSkuProductConfigurationService.findSkuGuidsMatchingSelectedOptions(eq(storeProduct), any()))
				.thenReturn(Collections.emptyList());
		repository.selectChoice(itemOptionSelectorChoiceIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(OPTION_VALUE_SELECTION_NOT_FOUND))
				.assertNoValues();
	}

	@Test
	public void selectChoiceShouldReturnErrorWhenNoProductSkuFoundForGuid() {
		when(productSkuRepository.getProductSkuWithAttributesByGuid(PRODUCT_SKU_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(PRODUCT_SKU_NOT_FOUND)));
		repository.selectChoice(itemOptionSelectorChoiceIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(PRODUCT_SKU_NOT_FOUND))
				.assertNoValues();
	}


	@Test
	public void getSelectedValuesForOtherOptionsShouldReturnCollectionWithoutSelectedOptionValue() {
		Map<String, SkuOptionValue> optionValueMap = new HashMap<>();
		optionValueMap.put(SELECTED_OPTION_VALUE_GUID, selectedSkuOptionValue);
		optionValueMap.put(OTHER_OPTION_VALUE_GUID, otherSkuOptionValue);

		when(productSku.getOptionValueMap()).thenReturn(optionValueMap);

		assertThat(repository.getSelectedValuesForOtherOptions(productSku, SELECTOR_OPTION_ID)).containsOnly(otherSkuOptionValue);
	}
}