/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.test.integration.shoppingcart.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingItemValidator;
import com.elasticpath.xpf.connectivity.extensionpoint.SystemInformation;
import com.elasticpath.xpf.impl.XPFExtensionSelectorAny;

;

/**
 * Integration tests for verifying spring wiring for extension lookup.
 */
public class XPFExtensionLookupTest extends BasicSpringContextTest {

	@Autowired
	private XPFExtensionLookup extensionLookup;

	@Test
	public void testLookupExtensionsForShoppingItemValidator() {
		List<ShoppingItemValidator> extensions = extensionLookup.getMultipleExtensions(ShoppingItemValidator.class,
				XPFExtensionPointEnum.VALIDATE_SHOPPING_ITEM_AT_ADD_TO_CART, new XPFExtensionSelectorAny());
		assertThat(extensions).isNotEmpty();
	}

	@Test
	public void testLookupExtensionsForProductSkuValidator() {
		List<ProductSkuValidator> extensions = extensionLookup.getMultipleExtensions(ProductSkuValidator.class,
				XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART_READ,  new XPFExtensionSelectorAny());
		assertThat(extensions).isNotEmpty();
	}

	@Test
	public void testLookupExtensionsForSystemInformation() {
		List<SystemInformation> extensions = extensionLookup.getMultipleExtensions(SystemInformation.class,
				XPFExtensionPointEnum.SYSTEM_INFORMATION, new XPFExtensionSelectorAny());
		assertThat(extensions).isNotEmpty();
	}
}