/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFStore;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;
import com.elasticpath.xpf.context.builders.ProductSkuValidationContextBuilder;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

@RunWith(MockitoJUnitRunner.class)
public class ProductSkuDelegateFromShoppingItemValidatorTest {

	private static final String ERROR_ID = "ERROR_ID";

	private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

	private static final String SKU_CODE_KEY = "SKU_CODE_KEY";

	private static final String SKU_CODE = "SKU_CODE";

	private static final String STORE_CODE_KEY = "STORE_CODE_KEY";

	private static final String STORE_CODE = "STORE_CODE";

	@InjectMocks
	private ProductSkuDelegateFromShoppingItemValidatorImpl validator;

	@Mock
	private XPFExtensionLookup extensionLookup;

	@Mock
	private ProductSkuValidationContextBuilder productSkuValidationContextBuilder;

	@Mock
	private XPFShoppingItemValidationContext context;

	@Mock
	private XPFProductSkuValidationContext productSkuValidationContext;

	@Mock
	private XPFShopper shopper;

	@Mock
	private XPFStore store;

	@Mock
	private XPFProductSku productSku;

	@Mock
	private XPFShoppingCart xpfShoppingCart;

	@Mock
	private BeanFactory beanFactory;

	@Before
	public void setUp() {
		given(beanFactory.getSingletonBean(
				ContextIdNames.PRODUCT_SKU_VALIDATION_CONTEXT_BUILDER, ProductSkuValidationContextBuilder.class))
				.willReturn(productSkuValidationContextBuilder);
		given(context.getShoppingCart()).willReturn(xpfShoppingCart);
		given(xpfShoppingCart.getShopper()).willReturn(shopper);
		given(shopper.getStore()).willReturn(store);
		given(store.getCode()).willReturn(STORE_CODE);
		given(productSku.getCode()).willReturn(SKU_CODE);
		given(productSkuValidationContextBuilder.build(context)).willReturn(productSkuValidationContext);
		given(productSkuValidationContext.getProductSku()).willReturn(productSku);
	}

	@Test
	public void unsuccessfulValidationTest() {
		// Given
		given(extensionLookup.getMultipleExtensions(eq(ProductSkuValidator.class),
				eq(XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART),
				any(XPFExtensionSelectorByStoreCode.class)))
				.willReturn(Collections.singletonList(context -> ImmutableList.of(
						new XPFStructuredErrorMessage(ERROR_ID, ERROR_MESSAGE, ImmutableMap.of(
								SKU_CODE_KEY, context.getProductSku().getCode(),
								STORE_CODE_KEY, STORE_CODE)))));

		// When
		Collection<XPFStructuredErrorMessage> errorMessages = validator.validate(context);

		// Then
		assertThat(errorMessages)
				.extracting(XPFStructuredErrorMessage::getData)
				.extracting(SKU_CODE_KEY, STORE_CODE_KEY)
				.containsExactlyInAnyOrder(
						tuple(SKU_CODE, STORE_CODE));
	}

	@Test
	public void successfulValidationTest() {
		// Given
		given(extensionLookup.getMultipleExtensions(eq(ProductSkuValidator.class),
				eq(XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART),
				any(XPFExtensionSelectorByStoreCode.class)))
				.willReturn(Collections.emptyList());

		// When
		Collection<XPFStructuredErrorMessage> errorMessages = validator.validate(context);

		// Then
		assertThat(errorMessages).isEmpty();
	}
}
