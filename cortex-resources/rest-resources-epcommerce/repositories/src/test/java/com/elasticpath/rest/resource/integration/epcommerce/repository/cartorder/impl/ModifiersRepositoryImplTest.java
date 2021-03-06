/**
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
/*

 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ErrorCheckPredicate.createErrorCheckPredicate;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.impl.ModifierFieldImpl;
import com.elasticpath.domain.modifier.impl.ModifierFieldOptionImpl;
import com.elasticpath.domain.modifier.impl.ModifierGroupImpl;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.impl.OrderRepositoryImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Tests for {@link ModifiersRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifiersRepositoryImplTest {

	private static final String GROUP_CODE = "groupCode";
	private static final String FIELD_CODE1 = "fieldCode1";
	private static final String FIELD_CODE2 = "fieldCode2";
	private static final String FIELD_CODE3 = "fieldCode3";
	private static final String FIELD_CODE4 = "fieldCode4";
	private static final String FIELD_CODE5 = "fieldCode5";
	private static final int FIELD_ORDERING_1 = 1;
	private static final int FIELD_ORDERING_2 = 2;
	private static final int FIELD_ORDERING_3 = 3;
	private static final int FIELD_ORDERING_4 = 4;
	private static final int FIELD_ORDERING_5 = 5;
	private static final String OPTION_VALUE1 = "option1";
	private static final String OPTION_VALUE2 = "option2";
	private static final int REQUIRED_FIELDS_COUNT = 3;
	private static final String SKU_GUID = "testSkuGuid";
	private static final String FIELD_VALUE_1 = "testFieldValue1";
	private static final String SHOPPING_ITEM_GUID = "testShoppingItemGuid";

	private static final String STORE_CODE = "testStoreCode";

	private static final String PURCHASE_GUID = "testPurchaseGuid";

	private static final String PURCHASE_LINE_ITEM_GUID = "testPurchaseLineItemGuid";

	private static final String CART_GUID = "testcartGUID";

	private ModifierGroup modifierGroup;

	private ModifierField field1;

	private ModifierField field2;

	@Mock
	private ModifierService modifierService;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;

	@Mock
	private ProductSkuRepository productSkuRepository;

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@InjectMocks
	private ModifiersRepositoryImpl modifiersRepository;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ShoppingItem shoppingItem;

	@Mock
	private ProductSku productSku;

	@Mock
	private Product product;

	@Mock
	private ProductType productType;

	@Mock
	private Order order;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private OrderSku orderSku;

	private final List<ModifierField> modifierFields = buildFields();

	private final Map<String, String> shoppingItemData = new HashMap<>();

	@Before
	public void setUp() {
		modifiersRepository = new ModifiersRepositoryImpl(modifierService, shoppingCartRepository, productSkuRepository,
				orderRepository, reactiveAdapter);
		//given
		modifierGroup = buildModifierGroup();
		when(modifierService.findModifierGroupByCode(GROUP_CODE)).thenReturn(modifierGroup);

		when(product.getProductType()).thenReturn(productType);
		when(modifierService.findModifierFieldsByProductType(productType)).thenReturn(modifierFields);

		given(productSkuRepository.getProductSkuWithAttributesByGuid(SKU_GUID)).willReturn(Single.just(productSku));
		given(productSku.getProduct()).willReturn(product);
		given(product.getProductType()).willReturn(productType);
		given(productType.getModifierGroups()).willReturn(ImmutableSet.of(modifierGroup));
	}

	@Test(expected = NullPointerException.class)
	public void testFindModifierGroupByCodeWhenCodeIsNull() {
		// when
		modifiersRepository.findModifierGroupByCode(null);
	}

	@Test
	public void testFindModifierGroupByCodeWhenValidCode() {
		modifiersRepository.findModifierGroupByCode(GROUP_CODE)
				.test()
				.assertNoErrors()
				.assertValue(modifierGroup);
	}

	@Test
	public void testFindModifierGroupByCodeWhenInvalidCode() {

		//given
		String invalidCode = "invalidCode";
		modifiersRepository.findModifierGroupByCode(invalidCode)
				.test()
				.assertError(ResourceOperationFailure.notFound("ModifierGroup not found"));
	}

	@Test(expected = NullPointerException.class)
	public void testFindModifierFieldByWhenAnyParamIsNull() {
		//given
		String modifierFieldCode = null;
		String modifierGroupCode = "code";
		// when
		modifiersRepository.findModifierFieldBy(modifierFieldCode, modifierGroupCode);
	}

	@Test
	public void testFindModifierFieldBy() {
		modifiersRepository.findModifierFieldBy(FIELD_CODE1, GROUP_CODE)
				.test()
				.assertNoErrors()
				.assertValue(field -> StringUtils.equals(FIELD_CODE1, field.getCode()));
	}

	@Test
	public void testFindModifierFieldByWithInvalidFieldCode() {

		String invalidCode = "invalidCode";
		modifiersRepository.findModifierFieldBy(invalidCode, GROUP_CODE)
				.test()
				.assertError(ResourceOperationFailure.notFound("ModifierField not found"))
				.assertNoValues();
	}

	@Test(expected = NullPointerException.class)
	public void testFindModifierFieldOptionByWhenAnyParamIsNull() {
		//given
		String modifierFieldCode = null;
		String modifierGroupCode = "code";
		// when
		modifiersRepository.findModifierFieldOptionBy(OPTION_VALUE1, modifierFieldCode, modifierGroupCode);
	}

	@Test
	public void testFindModifierFieldOptionBy() {
		modifiersRepository.findModifierFieldOptionBy(OPTION_VALUE1, FIELD_CODE1, GROUP_CODE)
				.test()
				.assertNoErrors()
				.assertValue(option -> StringUtils.equals(OPTION_VALUE1, option.getValue()));
	}

	@Test
	public void testFindModifierFieldOptionByWithInvalidOptionValue() {
		String invalidOptionValue = "invalidValue";
		modifiersRepository.findModifierFieldOptionBy(invalidOptionValue, FIELD_CODE1, GROUP_CODE)
				.test()
				.assertError(ResourceOperationFailure.notFound("ModifierFieldOption not found"))
				.assertNoValues();
	}

	private ModifierGroup buildModifierGroup() {
		ModifierGroup group = new ModifierGroupImpl();
		group.initialize();
		group.setCode(GROUP_CODE);
		field1 = new ModifierFieldImpl();
		field1.initialize();
		field1.setCode(FIELD_CODE1);
		field1.setOrdering(0);
		field2 = new ModifierFieldImpl();
		field2.initialize();
		field2.setCode(FIELD_CODE2);
		field1.setOrdering(1);
		group.addModifierField(field1);
		group.addModifierField(field2);

		ModifierFieldOption option1 = new ModifierFieldOptionImpl();
		option1.setValue(OPTION_VALUE1);
		option1.setOrdering(0);
		ModifierFieldOption option2 = new ModifierFieldOptionImpl();
		option2.setValue(OPTION_VALUE2);
		option2.setOrdering(1);

		field1.addModifierFieldOption(option1);
		field1.addModifierFieldOption(option2);

		return group;
	}

	@Test
	public void testFindModifierValuesContainsOnlyValidModifiers() {
		given(shoppingCartRepository.getShoppingCart(CART_GUID))
				.willReturn(Single.just(shoppingCart));
		given(shoppingCart.getCartItemByGuid(SHOPPING_ITEM_GUID))
				.willReturn(shoppingItem);
		given(shoppingItem.getSkuGuid())
				.willReturn(SKU_GUID);
		given(shoppingItem.getModifierFields().getMap())
				.willReturn(shoppingItemData);

		given(productSkuRepository.getProductSkuWithAttributesByGuid(SKU_GUID))
				.willReturn(Single.just(productSku));
		given(productSku.getProduct())
				.willReturn(product);
		given(product.getProductType())
				.willReturn(productType);
		given(productType.getModifierGroups())
				.willReturn(ImmutableSet.of(modifierGroup));

		shoppingItemData.put(FIELD_CODE1, FIELD_VALUE_1);

		modifiersRepository.findModifierValues(CART_GUID, SHOPPING_ITEM_GUID)
				.test()
				.assertNoErrors()
				.assertValue(shoppingData -> shoppingData.get(field1).equals(FIELD_VALUE_1) && shoppingData.get(field2).equals(StringUtils.EMPTY));
	}

	@Test
	public void testFindModifierValuesWithNoCartFoundFailure() {
		given(shoppingCartRepository.getShoppingCart(CART_GUID))
				.willReturn(Single.error(ResourceOperationFailure.notFound(ShoppingCartResourceConstants.DEFAULT_CART_NOT_FOUND)));

		modifiersRepository.findModifierValues(CART_GUID, SHOPPING_ITEM_GUID)
				.test()
				.assertError(createErrorCheckPredicate(ShoppingCartResourceConstants.DEFAULT_CART_NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void testFindPurchaseItemModifierValuesContainsOnlyValidModifiers() {
		given(orderRepository.findByGuid(STORE_CODE, PURCHASE_GUID))
				.willReturn(Single.just(order));
		given(order.getOrderSkuByGuid(PURCHASE_LINE_ITEM_GUID))
				.willReturn(orderSku);
		given(orderSku.getSkuGuid())
				.willReturn(SKU_GUID);
		given(orderSku.getModifierFields().getMap())
				.willReturn(shoppingItemData);

		given(productSkuRepository.getProductSkuWithAttributesByGuid(SKU_GUID))
				.willReturn(Single.just(productSku));
		given(productSku.getProduct())
				.willReturn(product);
		given(product.getProductType())
				.willReturn(productType);
		given(productType.getModifierGroups())
				.willReturn(ImmutableSet.of(modifierGroup));
		shoppingItemData.put(FIELD_CODE1, FIELD_VALUE_1);

		modifiersRepository.findPurchaseItemModifierValues(STORE_CODE, PURCHASE_GUID, PURCHASE_LINE_ITEM_GUID)
				.test()
				.assertNoErrors()
				.assertValue(shoppingData -> shoppingData.get(field1).equals(FIELD_VALUE_1) && shoppingData.get(field2).equals(StringUtils.EMPTY));
	}

	@Test
	public void testFindPurchaseItemModifierValuesWithNoOrderFoundFailure() {
		given(orderRepository.findByGuid(STORE_CODE, PURCHASE_GUID))
				.willReturn(Single.error(ResourceOperationFailure.notFound(OrderRepositoryImpl.PURCHASE_NOT_FOUND)));

		modifiersRepository.findPurchaseItemModifierValues(STORE_CODE, PURCHASE_GUID, PURCHASE_LINE_ITEM_GUID)
				.test()
				.assertError(createErrorCheckPredicate(OrderRepositoryImpl.PURCHASE_NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void testFindModifiersByProduct() {
		modifiersRepository.findModifiersByProduct(product)
				.test()
				.assertNoErrors()
				.assertValue(modifierFields);
	}

	@Test
	public void testFindMissingRequiredFieldCodesByShoppingItem() {

		// given
		final ShoppingItem mockShoppingItem = mock(ShoppingItem.class);
		final Map<String, String> itemMap = new HashMap<>();
		itemMap.put(FIELD_CODE1, FIELD_VALUE_1);
		itemMap.put(FIELD_CODE3, StringUtils.EMPTY);
		itemMap.put(FIELD_CODE4, null);
		when(mockShoppingItem.getSkuGuid()).thenReturn(SKU_GUID);

		ModifierFieldsMapWrapper modifierFields = new ModifierFieldsMapWrapper();
		modifierFields.putAll(itemMap);
		when(mockShoppingItem.getModifierFields()).thenReturn(modifierFields);

		// when
		modifiersRepository.findMissingRequiredFieldCodesByShoppingItem(mockShoppingItem)
				.test()
				.assertNoErrors()
				.assertValueCount(REQUIRED_FIELDS_COUNT)
				.assertValueSet(Arrays.asList(FIELD_CODE3, FIELD_CODE4, FIELD_CODE5));
	}

	private ModifierField buildField(final String fieldCode, final int ordering, final boolean required) {

		ModifierField field = new ModifierFieldImpl();

		field.initialize();
		field.setCode(fieldCode);
		field.setOrdering(ordering);
		field.setRequired(required);

		return field;

	}

	private List<ModifierField> buildFields() {

		List<ModifierField> fields = new ArrayList<>();

		fields.add(buildField(FIELD_CODE1, FIELD_ORDERING_1, true));
		fields.add(buildField(FIELD_CODE2, FIELD_ORDERING_2, false));
		fields.add(buildField(FIELD_CODE3, FIELD_ORDERING_3, true));
		fields.add(buildField(FIELD_CODE4, FIELD_ORDERING_4, true));
		fields.add(buildField(FIELD_CODE5, FIELD_ORDERING_5, true));

		return fields;
	}
}
