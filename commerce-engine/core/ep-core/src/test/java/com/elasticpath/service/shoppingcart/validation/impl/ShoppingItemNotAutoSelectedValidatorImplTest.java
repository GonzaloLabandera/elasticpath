/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFBundleConstituent;
import com.elasticpath.xpf.connectivity.entity.XPFProductBundle;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;

@RunWith(MockitoJUnitRunner.class)
public class ShoppingItemNotAutoSelectedValidatorImplTest {

	private static final String PRODUCT_SKU = "product_sku";
	private static final String MESSAGE_ID = "cart.item.auto.selected.in.bundle";

	private final ShoppingItemNotAutoSelectedValidatorImpl validator = new ShoppingItemNotAutoSelectedValidatorImpl();

	@Mock
	private XPFShoppingItemValidationContext context;

	@Mock
	private XPFProductSku parentProductSku;

	@Mock
	private XPFProductSku productSku;

	@Mock
	private XPFProductBundle productBundle;

	@Mock
	private XPFBundleConstituent bundleConstituent;

	@Mock
	private XPFShoppingItem xpfShoppingItem;

	@Mock
	private XPFShoppingItem xpfParentShoppingItem;

	@Before
	public void setUp() {
		when(context.getShoppingItem()).thenReturn(xpfShoppingItem);
		when(productSku.getCode()).thenReturn(PRODUCT_SKU);
		when(xpfShoppingItem.getProductSku()).thenReturn(productSku);
		when(parentProductSku.getProduct()).thenReturn(productBundle);
		when(productBundle.getConstituents()).thenReturn(Collections.singletonList(bundleConstituent));
		when(bundleConstituent.getProductSku()).thenReturn(productSku);
	}

	@Test
	public void testValidateWithError() {
		when(context.getParentShoppingItem()).thenReturn(xpfParentShoppingItem);
		when(xpfParentShoppingItem.getProductSku()).thenReturn(parentProductSku);

		Collection<XPFStructuredErrorMessage> validationMessages = validator.validate(context);
		assertThat(validationMessages).hasSize(1);
		XPFStructuredErrorMessage validationMessage = validationMessages.iterator().next();
		assertThat(validationMessage.getMessageId()).isEqualTo(MESSAGE_ID);
		assertThat(validationMessage.getData().get("item-code")).isEqualTo(PRODUCT_SKU);
		assertThat(validationMessage.getDebugMessage()).isEqualTo("Item '" + PRODUCT_SKU + "' is a bundle constituent that was automatically "
				+ "selected.");
	}

	@Test
	public void testValidateNoError() {
		when(context.getParentShoppingItem()).thenReturn(xpfParentShoppingItem);
		when(xpfParentShoppingItem.getProductSku()).thenReturn(null);

		Collection<XPFStructuredErrorMessage> validationMessages = validator.validate(context);
		assertThat(validationMessages).isEmpty();
	}

}