/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.validators.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormEntity;
import com.elasticpath.rest.definition.carts.ItemEntity;
import com.elasticpath.rest.definitions.validator.constants.ValidationMessages;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * Test class for AddItemsToCartValidator.
 */
@RunWith(MockitoJUnitRunner.class)
public class AddItemsToCartValidatorImplTest {

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

	private static final String CATALOG_CODE = "catalog_code";

	private static final String FIELD_NAME = "field-name";

	private static final String FIELD_VALUE = "field-value";

	@Test
	public void testNullFormEntity() {
		assertThatThrownBy(() -> addItemsToCartValidator.validate(null, SCOPE))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining(EP_VALIDATION_EXCEPTION_MESSAGE)
				.extracting(exception -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getDebugMessage()
				)
				.isEqualTo(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY);

		assertThatThrownBy(() -> addItemsToCartValidator.validate(null, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getMessageId()
				)
				.isEqualTo(MESSAGE_ID);

		assertThatThrownBy(() -> addItemsToCartValidator.validate(null, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getData()
				)
				.isEqualTo(ImmutableMap.of(
						FIELD_NAME, ITEMS_FIELD_NAME
				));
	}

	@Test
	public void testEmptyFormEntity() {
		// Given
		AddItemsToCartFormEntity addItemsToCartFormEntity = AddItemsToCartFormEntity.builder()
				.build();

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining(EP_VALIDATION_EXCEPTION_MESSAGE)
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getDebugMessage()
				)
				.isEqualTo(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY);

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getMessageId()
				)
				.isEqualTo(MESSAGE_ID);
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
		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining(EP_VALIDATION_EXCEPTION_MESSAGE)
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getDebugMessage()
				)
				.isEqualTo(MISSING_CODE_IN_REQUEST_BODY);

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getMessageId()
				)
				.isEqualTo(MESSAGE_ID);

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getData()
				)
				.isEqualTo(ImmutableMap.of(
						FIELD_NAME, ITEM_CODE_FIELD_NAME
				));
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
		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining(EP_VALIDATION_EXCEPTION_MESSAGE)
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getDebugMessage()
				)
				.isEqualTo(INVALID_QUANTITY_IN_REQUEST_BODY);

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getMessageId()
				)
				.isEqualTo(INVALID_QUANTITY_MESSAGE_ID);

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getData()
				)
				.isEqualTo(ImmutableMap.of(
						FIELD_NAME, QUANTITY_FIELD_NAME
				));
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
		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining(EP_VALIDATION_EXCEPTION_MESSAGE)
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getDebugMessage()
				)
				.isEqualTo("Item with code '" + SKUCODE + "' does not exist.");

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getMessageId()
				)
				.isEqualTo(MESSAGE_ID);

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getData()
				)
				.isEqualTo(ImmutableMap.of(
						FIELD_NAME, ITEM_CODE_FIELD_NAME,
						FIELD_VALUE, SKUCODE
				));
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
		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining(EP_VALIDATION_EXCEPTION_MESSAGE)
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getDebugMessage()
				)
				.isEqualTo("Item with code '" + SKUCODE + "' does not exist.");

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getMessageId()
				)
				.isEqualTo(MESSAGE_ID);

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getData()
				)
				.isEqualTo(ImmutableMap.of(
						FIELD_NAME, ITEM_CODE_FIELD_NAME,
						FIELD_VALUE, SKUCODE
				));

		// Then
		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining(EP_VALIDATION_EXCEPTION_MESSAGE)
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.stream().skip(1)
						.findFirst()
						.map(StructuredErrorMessage::getDebugMessage)
						.orElse(null)
				)
				.isEqualTo(INVALID_QUANTITY_IN_REQUEST_BODY);

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.stream().skip(1)
						.findFirst()
						.map(StructuredErrorMessage::getMessageId)
						.orElse(null)
				)
				.isEqualTo(INVALID_QUANTITY_MESSAGE_ID);

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.stream().skip(1)
						.findFirst()
						.map(StructuredErrorMessage::getData)
						.orElse(null)
				)
				.isEqualTo(ImmutableMap.of(
						FIELD_NAME, QUANTITY_FIELD_NAME
				));
	}

	@Test
	public void testConfigurableItem() {
		// given.
		Product product = mockItemCodeExisting();
		mockItemIsConfigurable(product, true);

		// given
		ItemEntity itemEntity = ItemEntity.builder()
				.withCode(SKUCODE)
				.withQuantity(0)
				.build();

		AddItemsToCartFormEntity addItemsToCartFormEntity = AddItemsToCartFormEntity.builder()
				.withItems(Collections.singleton(itemEntity))
				.build();

		// Then
		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining(EP_VALIDATION_EXCEPTION_MESSAGE)
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getDebugMessage()
				)
				.isEqualTo("Item '" + SKUCODE + "' is a configurable product. Please add it individually using 'additemtocart' form.");

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getMessageId()
				)
				.isEqualTo(ITEM_IS_CONFIGURABLE);

		assertThatThrownBy(() -> addItemsToCartValidator.validate(addItemsToCartFormEntity, SCOPE))
				.extracting((exception) -> ((EpValidationException) exception)
						.getStructuredErrorMessages()
						.iterator().next()
						.getData()
				)
				.isEqualTo(ImmutableMap.of(
						FIELD_NAME, ITEM_CODE_FIELD_NAME,
						FIELD_VALUE, SKUCODE
				));

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

		when(product.isInCatalog(catalog, true)).thenReturn(true);

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

}
