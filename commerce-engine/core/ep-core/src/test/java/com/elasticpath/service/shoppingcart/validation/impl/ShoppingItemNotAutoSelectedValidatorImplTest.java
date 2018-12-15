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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.ProductBundleImpl;

@RunWith(MockitoJUnitRunner.class)
public class ShoppingItemNotAutoSelectedValidatorImplTest {

	private static final String PRODUCT_SKU = "product_sku";
	private static final String MESSAGE_ID = "cart.item.auto.selected.in.bundle";

	private final ShoppingItemNotAutoSelectedValidatorImpl validator = new ShoppingItemNotAutoSelectedValidatorImpl();

	@InjectMocks
	private ShoppingItemValidationContextImpl context;

	@Mock(name = "parentProductSku")
	private ProductSku parentProductSku;

	@Mock(name = "productSku")
	private ProductSku productSku;

	@Mock
	private ProductBundleImpl productBundle;

	@Mock
	private BundleConstituent bundleConstituent;

	@Mock
	private ConstituentItem constituentItem;

	@Before
	public void setUp() {
		when(productSku.getSkuCode()).thenReturn(PRODUCT_SKU);
		when(parentProductSku.getProduct()).thenReturn(productBundle);
		when(productBundle.getConstituents()).thenReturn(Collections.singletonList(bundleConstituent));
		when(productBundle.isConstituentAutoSelectable(bundleConstituent)).thenReturn(true);
		when(bundleConstituent.getConstituent()).thenReturn(constituentItem);
		when(constituentItem.getProductSku()).thenReturn(productSku);
	}

	@Test
	public void testValidateWithError() {
		Collection<StructuredErrorMessage> validationMessages = validator.validate(context);
		assertThat(validationMessages).hasSize(1);
		StructuredErrorMessage validationMessage = validationMessages.iterator().next();
		assertThat(validationMessage.getMessageId()).isEqualTo(MESSAGE_ID);
		assertThat(validationMessage.getData().get("item-code")).isEqualTo(PRODUCT_SKU);
		assertThat(validationMessage.getDebugMessage()).isEqualTo("Item '" + PRODUCT_SKU + "' is a bundle constituent that was automatically "
				+ "selected.");
	}

	@Test
	public void testValidateNoError() {
		context.setParentProductSku(null);

		Collection<StructuredErrorMessage> validationMessages = validator.validate(context);
		assertThat(validationMessages).isEmpty();
	}

}