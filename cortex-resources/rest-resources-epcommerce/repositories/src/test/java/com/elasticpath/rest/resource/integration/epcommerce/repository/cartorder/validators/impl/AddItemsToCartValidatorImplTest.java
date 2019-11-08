/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.validators.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormEntity;
import com.elasticpath.rest.definition.carts.ItemEntity;
import com.elasticpath.rest.definitions.validator.constants.ValidationMessages;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * Test class for AddItemsToCartValidator.
 */
@RunWith(MockitoJUnitRunner.class)
public class AddItemsToCartValidatorImplTest {

	private static final int TOTAL_ITEMS = 5000;
	@Mock
	private ProductSkuLookup productSkuLookup;

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private AddItemsToCartValidatorImpl addItemsToCartValidator;

	private static final String EP_VALIDATION_EXCEPTION_MESSAGE = "There is something wrong with the constructed JSON payload";

	private static final String ITEMS_FIELD_NAME = "items";

	private static final String MESSAGE_ID = "item.not.available";

	private static final String ITEM_IS_CONFIGURABLE = "item.is.configurable";

	private static final String SCOPE = "scope";

	private static final String SKUCODE = "SKUCODE";

	private static final String MISSING_CODE_IN_REQUEST_BODY = "Code field is either missing or missing a value";

	private static final String ITEM_CODE_FIELD_NAME = "code";

	private static final String QUANTITY_FIELD_NAME = "quantity";

	private static final String INVALID_QUANTITY_IN_REQUEST_BODY = "'quantity' value must be greater than or equal to '1'.";

	private static final String INVALID_QUANTITY_MESSAGE_ID = "field.invalid.minimum.value";
	
	private static final String DUPLICATE_ITEM_MESSAGE = "Item 'SKUCODE' has multiple entries. Please combine duplicate items into one entry.";

	private static final String ITEM_IS_DUPLICATE_ID = "item.is.duplicate";

	private static final String CATALOG_CODE = "catalog_code";

	private static final String FIELD_NAME = "field-name";

	private static final String FIELD_VALUE = "field-value";

	@Test
	public void testNullFormEntity() {
		Message errorMessage = buildErrorMessage(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY, MESSAGE_ID, ITEMS_FIELD_NAME);
		addItemsToCartValidator.validate(null, SCOPE)
				.test()
				.assertError(throwable -> throwable.getMessage().equals(EP_VALIDATION_EXCEPTION_MESSAGE))
				.assertError(assertErrorMessages(Collections.singletonList(errorMessage)))
				.assertNotComplete();
	}

	@Test
	public void testEmptyFormEntity() {
		// Given
		AddItemsToCartFormEntity addItemsToCartFormEntity = AddItemsToCartFormEntity.builder()
				.build();

		Message errorMessage = buildErrorMessage(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY, MESSAGE_ID, ITEMS_FIELD_NAME);
		addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE)
				.test()
				.assertError(throwable -> throwable.getMessage().equals(EP_VALIDATION_EXCEPTION_MESSAGE))
				.assertError(assertErrorMessages(Collections.singletonList(errorMessage)))
				.assertNotComplete();
	}

	@Test
	public void testEmptyItemCode() {
		// given
		ItemEntity itemEntity = ItemEntity.builder()
				.withCode(StringUtils.EMPTY)
				.withQuantity(1)
				.build();
		AddItemsToCartFormEntity addItemsToCartFormEntity = AddItemsToCartFormEntity.builder()
				.withItems(Collections.singleton(itemEntity))
				.build();

		// Then
		Message errorMessage = buildErrorMessage(MISSING_CODE_IN_REQUEST_BODY, MESSAGE_ID, ITEM_CODE_FIELD_NAME);
		addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE)
				.test()
				.assertError(throwable -> throwable.getMessage().equals(EP_VALIDATION_EXCEPTION_MESSAGE))
				.assertError(assertErrorMessages(Collections.singletonList(errorMessage)))
				.assertNotComplete();
	}

	@Test
	public void testZeroQuantity() {
		// given
		ItemEntity itemEntity = ItemEntity.builder()
				.withCode(SKUCODE)
				.withQuantity(0)
				.build();

		AddItemsToCartFormEntity addItemsToCartFormEntity = AddItemsToCartFormEntity.builder()
				.withItems(Collections.singleton(itemEntity))
				.build();

		Product product = mockItemCodeExisting();
		mockItemIsConfigurable(product, false);

		// Then
		Message errorMessage = buildErrorMessage(INVALID_QUANTITY_IN_REQUEST_BODY, INVALID_QUANTITY_MESSAGE_ID, QUANTITY_FIELD_NAME);
		addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE)
				.test()
				.assertError(throwable -> throwable.getMessage().equals(EP_VALIDATION_EXCEPTION_MESSAGE))
				.assertError(assertErrorMessages(Collections.singletonList(errorMessage)))
				.assertNotComplete();
	}

	@Test
	public void testInvalidItemCode() {
		// given
		ItemEntity itemEntity = ItemEntity.builder()
				.withCode(SKUCODE)
				.withQuantity(1)
				.build();

		AddItemsToCartFormEntity addItemsToCartFormEntity = AddItemsToCartFormEntity.builder()
				.withItems(Collections.singleton(itemEntity))
				.build();

		// Then
		String debugMessage = "Item with code '" + SKUCODE + "' does not exist.";
		Message errorMessage = buildErrorMessage(debugMessage, MESSAGE_ID, ITEM_CODE_FIELD_NAME, SKUCODE);
		addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE)
				.test()
				.assertError(throwable -> throwable.getMessage().equals(EP_VALIDATION_EXCEPTION_MESSAGE))
				.assertError(assertErrorMessages(Collections.singletonList(errorMessage)))
				.assertNotComplete();
	}

	@Test
	public void testInvalidItemCodeAndInvalidQuantity() {
		// given
		ItemEntity itemEntity = ItemEntity.builder()
				.withCode(SKUCODE)
				.withQuantity(0)
				.build();

		AddItemsToCartFormEntity addItemsToCartFormEntity = AddItemsToCartFormEntity.builder()
				.withItems(Collections.singleton(itemEntity))
				.build();

		// Then
		String debugMessage = "Item with code '" + SKUCODE + "' does not exist.";
		Message invalidCodeErrorMessage = buildErrorMessage(debugMessage, MESSAGE_ID, ITEM_CODE_FIELD_NAME, SKUCODE);
		Message invalidQtyErrorMessage = buildErrorMessage(INVALID_QUANTITY_IN_REQUEST_BODY, INVALID_QUANTITY_MESSAGE_ID, QUANTITY_FIELD_NAME);

		addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE)
				.test()
				.assertError(throwable -> throwable.getMessage().equals(EP_VALIDATION_EXCEPTION_MESSAGE))
				.assertError(assertErrorMessages(Arrays.asList(invalidCodeErrorMessage, invalidQtyErrorMessage)))
				.assertNotComplete();
	}

	@Test
	public void testConfigurableItem() {
		// given.
		Product product = mockItemCodeExisting();
		mockItemIsConfigurable(product, true);

		// given
		ItemEntity itemEntity = ItemEntity.builder()
				.withCode(SKUCODE)
				.withQuantity(1)
				.build();

		AddItemsToCartFormEntity addItemsToCartFormEntity = AddItemsToCartFormEntity.builder()
				.withItems(Collections.singleton(itemEntity))
				.build();

		// Then
		String debugMessage = "Item '" + SKUCODE + "' is a configurable product. Please add it individually using 'additemtocart' form.";
		Message errorMessage = buildErrorMessage(debugMessage, ITEM_IS_CONFIGURABLE, ITEM_CODE_FIELD_NAME, SKUCODE);
		addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE)
				.test()
				.assertError(throwable -> throwable.getMessage().equals(EP_VALIDATION_EXCEPTION_MESSAGE))
				.assertError(assertErrorMessages(Collections.singletonList(errorMessage)))
				.assertNotComplete();

	}

	@Test
	public void testDuplicateItems() {
		// given
		ItemEntity itemEntity = ItemEntity.builder()
				.withCode(SKUCODE)
				.withQuantity(1)
				.build();

		AddItemsToCartFormEntity addItemsToCartFormEntity = AddItemsToCartFormEntity.builder()
				.withItems(Arrays.asList(itemEntity, itemEntity))
				.build();

		Product product = mockItemCodeExisting();
		mockItemIsConfigurable(product, false);

		// Then
		Message errorMessage = buildErrorMessage(DUPLICATE_ITEM_MESSAGE, ITEM_IS_DUPLICATE_ID, ITEM_CODE_FIELD_NAME, SKUCODE);
		addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE)
				.test()
				.assertError(throwable -> throwable.getMessage().equals(EP_VALIDATION_EXCEPTION_MESSAGE))
				.assertError(assertErrorMessages(Collections.singletonList(errorMessage)))
				.assertNotComplete();
	}

	@Test
	public void testValidatingLargeRequestWithDuplicateItems() {
		// given
		ItemEntity itemEntity = ItemEntity.builder()
				.withCode(SKUCODE)
				.withQuantity(1)
				.build();

		List<ItemEntity> itemEntities = new ArrayList<>();
		IntStream.range(1, TOTAL_ITEMS) // about 2500 items cause a stackoverflow error
				.forEach(value -> itemEntities.add(itemEntity));
		AddItemsToCartFormEntity addItemsToCartFormEntity = AddItemsToCartFormEntity.builder()
				.withItems(itemEntities)
				.build();

		Product product = mockItemCodeExisting();
		mockItemIsConfigurable(product, false);

		// Then
		Message errorMessage = buildErrorMessage(DUPLICATE_ITEM_MESSAGE, ITEM_IS_DUPLICATE_ID, ITEM_CODE_FIELD_NAME, SKUCODE);
		addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE)
				.test()
				.assertError(throwable -> throwable.getMessage().equals(EP_VALIDATION_EXCEPTION_MESSAGE))
				.assertError(assertErrorMessages(Collections.singletonList(errorMessage)))
				.assertNotComplete();
	}

	private void mockItemIsConfigurable(final Product product, final boolean isConfigurable) {
		ProductType productType = createMockProductType();
		when(product.getProductType()).thenReturn(productType);
		when(productType.isConfigurable()).thenReturn(isConfigurable);
	}

	private Product mockItemCodeExisting() {
		Catalog catalog = createMockCatalog();
		Store store = createMockStore(catalog);
		Product product = createMockProduct();

		when(product.isInCatalog(catalog)).thenReturn(true);

		ProductSku productSku = createProductSku(SKUCODE, product);

		when(productSkuLookup.findBySkuCode(SKUCODE)).thenReturn(productSku);
		when(storeRepository.findStoreAsSingle(SCOPE)).thenReturn(Single.just(store));
		
		return product;
	}

	private Product createMockProduct() {
		return mock(Product.class);
	}

	private ProductType createMockProductType() {
		return mock(ProductType.class);
	}

	private Catalog createMockCatalog() {
		final Catalog catalog = mock(Catalog.class);
		catalog.setCode(CATALOG_CODE);
		return catalog;
	}

	private ProductSku createProductSku(final String skuCode) {
		ProductSku productSku = new ProductSkuImpl();
		productSku.setSkuCode(skuCode);
		return productSku;
	}

	private ProductSku createProductSku(final String skuCode, final Product product) {
		ProductSku productSku = createProductSku(skuCode);
		productSku.setProduct(product);
		return productSku;
	}

	private Store createMockStore(final Catalog catalog) {
		final Store store = mock(Store.class);
		when(store.getCatalog()).thenReturn(catalog);

		return store;
	}

	private Message buildErrorMessage(final String debugMessage, final String messageId, final String fieldName) {
		Map<String, String> errorData = new HashMap<>();
		errorData.put(FIELD_NAME, fieldName);

		return Message.builder()
				.withType(StructuredMessageTypes.ERROR)
				.withId(messageId)
				.withDebugMessage(debugMessage)
				.withData(errorData)
				.build();
	}

	private Message buildErrorMessage(final String debugMessage, final String messageId, final String fieldName, final String fieldValue) {
		Map<String, String> errorData = new HashMap<>();
		errorData.put(FIELD_NAME, fieldName);
		errorData.put(FIELD_VALUE, fieldValue);

		return Message.builder()
				.withType(StructuredMessageTypes.ERROR)
				.withId(messageId)
				.withDebugMessage(debugMessage)
				.withData(errorData)
				.build();
	}

	private Predicate<Throwable> assertErrorMessages(final List<Message> expectedMessages) {
		return throwable -> {
			ResourceOperationFailure failure = (ResourceOperationFailure) throwable;

			if (failure.getMessages().size() != expectedMessages.size()) {
				return false;
			}

			for (Message expectedMsg : expectedMessages) {
				boolean noneMatch = failure.getMessages().stream()
						.noneMatch(message ->
								message.getData().equals(expectedMsg.getData())
										&& message.getDebugMessage().equals(expectedMsg.getDebugMessage())
										&& message.getId().equals(expectedMsg.getId()));

				if (noneMatch) {
					return false;
				}
			}
			return true;
		};
	}
}
