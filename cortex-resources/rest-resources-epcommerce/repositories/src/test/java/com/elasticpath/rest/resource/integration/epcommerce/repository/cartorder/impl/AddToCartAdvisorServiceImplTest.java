/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SKU_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.when;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.carts.LineItemEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ShopperRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorMessageTransformer;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;
import com.elasticpath.xpf.context.builders.impl.ProductSkuValidationContextBuilderImpl;
import com.elasticpath.xpf.converters.StructuredErrorMessageConverter;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

/**
 * Tests {@link AddToCartAdvisorServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AddToCartAdvisorServiceImplTest {

	@InjectMocks
	private AddToCartAdvisorServiceImpl advisorService;

	@Mock
	private ProductSkuRepository productSkuRepository;

	@Mock
	private StructuredErrorMessageTransformer structuredErrorMessageTransformer;

	@Mock
	private ShopperRepository shopperRepository;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private ProductSku productSku;

	@Mock
	private Shopper shopper;

	@Mock
	private Store store;

	@Mock
	private XPFExtensionLookup extensionLookup;

	@Mock
	private ProductSkuValidationContextBuilderImpl productSkuValidationContextBuilder;

	@Mock
	private XPFProductSkuValidationContext context;

	@Mock
	private StructuredErrorMessageConverter structuredErrorMessageConverter;

	@Before
	public void setup() {
		when(shopperRepository.findOrCreateShopper()).thenReturn(Single.just(shopper));
		when(storeRepository.findStoreAsSingle(SCOPE)).thenReturn(Single.just(store));
		when(productSkuRepository.getProductSkuWithAttributesByCode(SKU_CODE)).thenReturn(Single.just(productSku));
		when(productSku.getSkuCode()).thenReturn(SKU_CODE);
		when(productSkuValidationContextBuilder.build(productSku, null, shopper, store)).thenReturn(context);
		when(store.getCode()).thenReturn("store_code");
	}

	/**
	 * Test item is purchasable.
	 */
	@Test
	public void shouldBeTrueWhenItemIsPurchasable() {
		when(extensionLookup.getMultipleExtensions(eq(ProductSkuValidator.class),
				eq(XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART_READ),
				any(XPFExtensionSelectorByStoreCode.class)))
				.thenReturn(Collections.singletonList(context -> Collections.emptyList()));

		when(structuredErrorMessageTransformer.transform(Collections.emptyList(), SKU_CODE))
				.thenReturn(Collections.emptyList());

		advisorService.validateItemPurchasable(SCOPE, SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValueCount(0);
	}

	/**
	 * Test item is not purchasable.
	 */
	@Test
	public void shouldBeFalseWhenItemIsNotPurchasable() {

		XPFStructuredErrorMessage xpfStructuredErrorMessage = new XPFStructuredErrorMessage("error", "message", Collections.emptyMap());
		StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage("error", "message", Collections.emptyMap());

		when(extensionLookup.getMultipleExtensions(eq(ProductSkuValidator.class),
				eq(XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART_READ),
				any(XPFExtensionSelectorByStoreCode.class)))
				.thenReturn(Collections.singletonList(context ->
						ImmutableList.of(xpfStructuredErrorMessage)));

		ImmutableList<StructuredErrorMessage> errorList = ImmutableList.of(structuredErrorMessage);

		when(structuredErrorMessageConverter.convert(xpfStructuredErrorMessage)).thenReturn(structuredErrorMessage);

		when(structuredErrorMessageTransformer.transform(errorList, SKU_CODE))
				.thenReturn(ImmutableList.of(Message.builder()
						.build()));

		advisorService.validateItemPurchasable(SCOPE, SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValueCount(1);
	}

	@Test
	public void testValidateQuantitySuccess() {
		LineItemEntity lineItemEntity = mock(LineItemEntity.class);
		when(lineItemEntity.getQuantity()).thenReturn(1);

		advisorService.validateLineItemEntity(lineItemEntity)
				.test()
				.assertNoErrors()
				.assertValueCount(0);
	}

	@Test
	public void testValidateQuantityFailureWhenNull() {
		LineItemEntity lineItemEntity = mock(LineItemEntity.class);
		when(lineItemEntity.getQuantity()).thenReturn(null);

		advisorService.validateLineItemEntity(lineItemEntity)
				.test()
				.assertError(ResourceOperationFailure.class);
	}
}