/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.carts.LineItemEntity;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.price.PriceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Tests {@link ShoppingItemValidationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShoppingItemValidationServiceImplTest {

	private static final String STORE_CODE = "store";
	private static final String SKU_CODE = "sku";

	@Mock
	private ProductSkuRepository productSkuRepository;

	@Mock
	private PriceRepository priceRepository;

	@Mock
	private StoreProductRepository storeProductRepository;

	@Mock
	private ShoppingItemDto shoppingItemDto;

	@InjectMocks
	private ShoppingItemValidationServiceImpl validationService;

	/**
	 * Test item is purchasable.
	 */
	@Test
	public void shouldBeTrueWhenItemIsPurchasable() {
		ProductSku productSku = prepareMocksForPurchasableItems(true, false, true);

		validationService.validateItemPurchasable(STORE_CODE, productSku)
				.test()
				.assertNoErrors()
				.assertValueCount(0);

		verify(storeProductRepository).findDisplayableStoreProductWithAttributesByProductGuidAsSingle(any(), any());
		verify(priceRepository).priceExists(any(), any());
	}

	/**
	 * Test item is not purchasable.
	 */
	@Test
	public void shouldBeFalseWhenItemIsNotPurchasable() {
		ProductSku productSku = prepareMocksForPurchasableItems(false, false, true);

		validationService.validateItemPurchasable(STORE_CODE, productSku)
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(this::hasValidStructuredErrorMessage);

		verify(storeProductRepository).findDisplayableStoreProductWithAttributesByProductGuidAsSingle(any(), any());
	}

	/**
	 * Test item is not purchasable because it is not sold separately.
	 */
	@Test
	public void shouldBeFalseWhenItemIsNotSoldSeparately() {
		ProductSku productSku = prepareMocksForPurchasableItems(true, true, true);

		validationService.validateItemPurchasable(STORE_CODE, productSku)
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(this::hasValidStructuredErrorMessage);

		verify(storeProductRepository).findDisplayableStoreProductWithAttributesByProductGuidAsSingle(any(), any());
	}

	/**
	 * Test item is not purchasable because the price does not exist.
	 */
	@Test
	public void shouldBeFalseWhenPriceDoesNotExist() {
		ProductSku productSku = prepareMocksForPurchasableItems(true, false, false);

		validationService.validateItemPurchasable(STORE_CODE, productSku)
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(this::hasValidStructuredErrorMessage);

		verify(storeProductRepository).findDisplayableStoreProductWithAttributesByProductGuidAsSingle(any(), any());
		verify(priceRepository).priceExists(any(), any());
	}

	private ProductSku prepareMocksForPurchasableItems(final boolean isProductSkuPurchasable,
													   final boolean isProductSoldSeparately, final boolean doesPriceExist) {
		ProductSku mockProductSku = mock(ProductSku.class, Answers.RETURNS_DEEP_STUBS.get());
		Product mockProduct = mock(Product.class, Answers.RETURNS_DEEP_STUBS.get());
		StoreProduct mockStoreProduct = mock(StoreProduct.class, Answers.RETURNS_DEEP_STUBS.get());

		when(productSkuRepository.getProductSkuWithAttributesByCodeAsSingle(SKU_CODE)).thenReturn(Single.just(mockProductSku));
		when(mockProductSku.getProduct()).thenReturn(mockProduct);
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuidAsSingle(STORE_CODE, mockProduct.getGuid()))
				.thenReturn(Single.just(mockStoreProduct));

		when(mockProductSku.getSkuCode()).thenReturn(SKU_CODE);

		when(mockStoreProduct.isSkuPurchasable(SKU_CODE)).thenReturn(isProductSkuPurchasable);
		when(mockStoreProduct.isNotSoldSeparately()).thenReturn(isProductSoldSeparately);
		when(priceRepository.priceExists(STORE_CODE, SKU_CODE)).thenReturn(Single.just(doesPriceExist));

		return mockProductSku;
	}

	@Test
	public void testValidateQuantitySuccess() {
		LineItemEntity lineItemEntity = mock(LineItemEntity.class);
		when(lineItemEntity.getQuantity()).thenReturn(1);

		validationService.validateQuantity(lineItemEntity, 1)
				.test()
				.assertNoErrors()
				.assertValueCount(0);
	}

	@Test
	public void testValidateQuantityFailureWhenNull() {
		LineItemEntity lineItemEntity = mock(LineItemEntity.class);
		when(lineItemEntity.getQuantity()).thenReturn(null);

		validationService.validateQuantity(lineItemEntity, 1)
				.test()
				.assertError(ResourceOperationFailure.class);
	}

	@Test
	public void testValidateQuantityFailureWhenInvalid() {
		LineItemEntity lineItemEntity = mock(LineItemEntity.class);
		when(lineItemEntity.getQuantity()).thenReturn(-1);

		validationService.validateQuantity(lineItemEntity, 1)
				.test()
				.assertError(ResourceOperationFailure.class);
	}

	@Test
	public void ensureShoppingItemDtoWithValidQuantitySucceedsValidation() {
		given(shoppingItemDto.getQuantity())
				.willReturn(1);

		validationService.validate(shoppingItemDto)
				.test()
				.assertNoErrors();
	}

	@Test
	public void ensureShoppingItemDtoWithInvalidQuantityReturnsStateFailure() {
		given(shoppingItemDto.getQuantity())
				.willReturn(-1);

		validationService.validate(shoppingItemDto)
				.test()
				.assertError(ResourceOperationFailure.class);
	}

	private boolean hasValidStructuredErrorMessage(final Message linkedMessage) {
		return linkedMessage.getType().equals(StructuredMessageTypes.ERROR)
				&& linkedMessage.getId().equals(StructuredErrorMessageIdConstants.CART_ITEM_NOT_AVAILABLE)
				&& linkedMessage.getDebugMessage().equals("Item '" + SKU_CODE + "' is not available for purchase.")
				&& linkedMessage.getData().equals(ImmutableMap.of("item-code", SKU_CODE));
	}
}