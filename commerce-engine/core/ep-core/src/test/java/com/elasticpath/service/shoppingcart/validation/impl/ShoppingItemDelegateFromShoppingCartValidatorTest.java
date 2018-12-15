/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.common.pricing.service.PriceLookupFacade;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;

/**
 * Unit tests for {@link ShoppingItemDelegateFromShoppingCartValidatorImpl}.
 */

@RunWith(MockitoJUnitRunner.class)
public class ShoppingItemDelegateFromShoppingCartValidatorTest {

	private static final String ERROR_ID = "error.id";

	private static final String ERROR_MESSAGE = "error.message";

	private static final String SKU_GUID = "skuGuid";

	private static final String SKU_CODE_KEY = "skuCode";

	private static final String PARENT_SKU_CODE_KEY = "parentSkuCode";

	private static final String STORE_CODE_KEY = "storeCode";

	private static final String SHOPPER_GUID_KEY = "shopperGuid";

	private static final String STORE_CODE = "STORE_CODE";

	private static final String SHOPPER_GUID = "SHOPPER_GUID";

	private static final String SKU_GUID_ROOT_1 = "skuGuidRoot1";

	private static final String SKU_GUID_ROOT_2 = "skuGuidRoot2";

	private static final String SKU_GUID_LEVEL_1_A = "skuGuidLevel1a";

	private static final String SKU_GUID_LEVEL_1_B = "skuGuidLevel1b";

	private static final String SKU_GUID_LEVEL_2 = "skuGuidLevel2";

	private static final String CODE = "Code";


	@InjectMocks
	private ShoppingItemDelegateFromShoppingCartValidatorImpl delegateValidator;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private Shopper shopper;

	@Mock
	private Store store;

	@Mock
	private BeanFactory beanFactory;

	@SuppressWarnings("PMD.UnusedPrivateField")
	@Mock
	private PriceLookupFacade priceLookupFacade;

	private ShoppingItem level2ShoppingItem;

	private ShoppingItem level1aShoppingItem;

	private ShoppingItem level1bShoppingItem;

	private ShoppingItem rootShoppingItem1;

	private ShoppingItem rootShoppingItem2;

	@Before
	public void setup() {
		given(beanFactory.getBean(ContextIdNames.SHOPPING_ITEM_VALIDATION_CONTEXT))
				.willAnswer(invocation -> new ShoppingItemValidationContextImpl());
		given(store.getCode()).willReturn(STORE_CODE);
		given(shoppingCart.getStore()).willReturn(store);
		given(shoppingCart.getShopper()).willReturn(shopper);
		given(shopper.getGuid()).willReturn(SHOPPER_GUID);

		delegateValidator.setShoppingItemValidators(ImmutableList.of(itemValidationContext -> {
			Map<String, String> data = new HashMap<>();
			data.put(SKU_GUID, itemValidationContext.getShoppingItem().getSkuGuid());
			data.put(SKU_CODE_KEY, itemValidationContext.getProductSku().getSkuCode());
			if (itemValidationContext.getParentProductSku() != null) {
				data.put(PARENT_SKU_CODE_KEY, itemValidationContext.getParentProductSku().getSkuCode());
			}
			data.put(STORE_CODE_KEY, itemValidationContext.getStore().getCode());
			data.put(SHOPPER_GUID_KEY, itemValidationContext.getShopper().getGuid());
			return Collections.singletonList(new StructuredErrorMessage(ERROR_ID, ERROR_MESSAGE, data));
		}));
	}

	@Test
	public void testValidateWithTwoStandardShoppingItems() {
		// Given
		ShoppingItem shoppingItem1 = getShoppingItem(SKU_GUID_ROOT_1);
		ShoppingItem shoppingItem2 = getShoppingItem(SKU_GUID_ROOT_2);
		Map<ShoppingItem, ProductSku> shoppingItemProductSkuMap = ImmutableList.of(shoppingItem1, shoppingItem2).stream()
				.collect(Collectors.toMap(shoppingItem -> shoppingItem, shoppingItem -> getMockProductSku(shoppingItem.getSkuGuid())));
		when(shoppingCart.getShoppingItemProductSkuMap()).thenReturn(shoppingItemProductSkuMap);

		// When
		final ShoppingCartValidationContext context = new ShoppingCartValidationContextImpl();
		context.setShoppingCart(shoppingCart);

		Collection<StructuredErrorMessage> errors = delegateValidator.validate(context);

		// Then
		assertThat(errors)
				.extracting(StructuredErrorMessage::getMessageId)
				.containsExactlyInAnyOrder(ERROR_ID, ERROR_ID);

		assertThat(errors)
				.extracting(StructuredErrorMessage::getData)
				.extracting(SKU_GUID, SKU_CODE_KEY, PARENT_SKU_CODE_KEY, STORE_CODE_KEY, SHOPPER_GUID_KEY)
				.containsExactlyInAnyOrder(
						tuple(SKU_GUID_ROOT_1, SKU_GUID_ROOT_1 + CODE, null, STORE_CODE, SHOPPER_GUID),
						tuple(SKU_GUID_ROOT_2, SKU_GUID_ROOT_2 + CODE, null, STORE_CODE, SHOPPER_GUID));
	}

	@Test
	public void testValidateWithDeeplyNestedShoppingCart() {
		// Given
		Map<ShoppingItem, ProductSku> shoppingItemProductSkuMap = getDeeplyNestedShoppingItems().stream()
				.collect(Collectors.toMap(shoppingItem -> shoppingItem, shoppingItem -> getMockProductSku(shoppingItem.getSkuGuid())));
		when(shoppingCart.getShoppingItemProductSkuMap()).thenReturn(shoppingItemProductSkuMap);

		when(shoppingCart.getParentProductSku(level1aShoppingItem)).thenReturn(new ProductSkuImpl(SKU_GUID_ROOT_1 + CODE, AvailabilityCriteria
				.ALWAYS_AVAILABLE));
		when(shoppingCart.getParentProductSku(level1bShoppingItem)).thenReturn(new ProductSkuImpl(SKU_GUID_ROOT_2 + CODE, AvailabilityCriteria
				.ALWAYS_AVAILABLE));
		when(shoppingCart.getParentProductSku(level2ShoppingItem)).thenReturn(new ProductSkuImpl(SKU_GUID_LEVEL_1_A + CODE, AvailabilityCriteria
				.ALWAYS_AVAILABLE));


		// When
		final ShoppingCartValidationContext context = new ShoppingCartValidationContextImpl();
		context.setShoppingCart(shoppingCart);

		Collection<StructuredErrorMessage> errors = delegateValidator.validate(context);

		// Then
		assertThat(errors)
				.extracting(StructuredErrorMessage::getData)
				.extracting(SKU_GUID, SKU_CODE_KEY, PARENT_SKU_CODE_KEY, STORE_CODE_KEY, SHOPPER_GUID_KEY)
				.containsExactlyInAnyOrder(
						tuple(SKU_GUID_ROOT_1, SKU_GUID_ROOT_1 + CODE, null, STORE_CODE, SHOPPER_GUID),
						tuple(SKU_GUID_ROOT_2, SKU_GUID_ROOT_2 + CODE, null, STORE_CODE, SHOPPER_GUID),
						tuple(SKU_GUID_LEVEL_1_A, SKU_GUID_LEVEL_1_A + CODE, SKU_GUID_ROOT_1 + CODE, STORE_CODE, SHOPPER_GUID),
						tuple(SKU_GUID_LEVEL_1_B, SKU_GUID_LEVEL_1_B + CODE, SKU_GUID_ROOT_2 + CODE, STORE_CODE, SHOPPER_GUID),
						tuple(SKU_GUID_LEVEL_2, SKU_GUID_LEVEL_2 + CODE, SKU_GUID_LEVEL_1_A + CODE, STORE_CODE, SHOPPER_GUID));
	}

	private List<ShoppingItem> getDeeplyNestedShoppingItems() {

		level2ShoppingItem = getShoppingItem(SKU_GUID_LEVEL_2);
		List<ShoppingItem> level2List = Arrays.asList(level2ShoppingItem);
		level1aShoppingItem = getShoppingItem(SKU_GUID_LEVEL_1_A);
		List<ShoppingItem> level1aList = Arrays.asList(level1aShoppingItem);
		level1bShoppingItem = getShoppingItem(SKU_GUID_LEVEL_1_B);
		List<ShoppingItem> level1bList = Arrays.asList(level1bShoppingItem);
		rootShoppingItem1 = getShoppingItem(SKU_GUID_ROOT_1);
		rootShoppingItem2 = getShoppingItem(SKU_GUID_ROOT_2);

		List<ShoppingItem> allShoppingItems = new ArrayList<>();
		allShoppingItems.add(rootShoppingItem1);
		allShoppingItems.add(rootShoppingItem2);
		allShoppingItems.addAll(level1aList);
		allShoppingItems.addAll(level1bList);
		allShoppingItems.addAll(level2List);
		return allShoppingItems;
	}

	private ShoppingItem getShoppingItem(final String skuGuid) {
		final ShoppingItem shoppingItem = mock(ShoppingItem.class);
		when(shoppingItem.getSkuGuid()).thenReturn(skuGuid);
		return shoppingItem;
	}

	private ProductSku getMockProductSku(final String skuGuid) {
		final ProductSku productSku = mock(ProductSku.class);
		when(productSku.getSkuCode()).thenReturn(skuGuid + CODE);
		return productSku;
	}

}
