/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.context.builders.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.service.shipping.ShippingOptionResult;
import com.elasticpath.service.shipping.ShippingOptionService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.entity.XPFShippingOption;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.converters.ShippingOptionConverter;
import com.elasticpath.xpf.converters.ShoppingCartConverter;

@RunWith(MockitoJUnitRunner.class)
public class ShoppingCartValidationContextBuilderImplTest {
	@Mock
	private ShoppingCart shoppingCart;
	@Mock
	private XPFShoppingCart xpfShoppingCart;
	@Mock
	private Shopper shopper;
	@Mock
	private CustomerSession customerSession;
	@Mock
	private ShoppingCartConverter shoppingCartConverter;
	@Mock
	private PricingSnapshotService pricingSnapshotService;
	@Mock
	private ShippingOptionService shippingOptionService;
	@Mock
	private ShippingOptionConverter xpfShippingOptionConverter;
	@Mock
	private ShoppingCartPricingSnapshot pricingSnapshot;
	@Mock
	private ShippingOptionResult shippingOptionsResult;
	@Mock
	private ShippingOption shippingOption1, shippingOption2;
	@Mock
	private XPFShippingOption xpfShippingOption1, xpfShippingOption2;

	@InjectMocks
	private ShoppingCartValidationContextBuilderImpl shoppingCartValidationContextBuilder;

	@Rule
	public ExpectedException exceptionThrown = ExpectedException.none();

	@Test
	public void testBuildWithFullInputs() {
		// CustomerSession as null
		when(shoppingCartConverter.convert(shoppingCart)).thenReturn(xpfShoppingCart);
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(shopper.getCustomerSession()).thenReturn(null);

		XPFShoppingCartValidationContext xpfShoppingCartValidationContext = shoppingCartValidationContextBuilder.build(shoppingCart);

		assertEquals(xpfShoppingCart, xpfShoppingCartValidationContext.getShoppingCart());
		assertFalse(xpfShoppingCartValidationContext.isPaymentRequired());
		assertTrue(xpfShoppingCartValidationContext.getAvailableShippingOptions().isEmpty());

		// CustomerSession as valid value
		when(shopper.getCustomerSession()).thenReturn(customerSession);
		when(pricingSnapshotService.getPricingSnapshotForCart(shoppingCart)).thenReturn(pricingSnapshot);
		when(pricingSnapshot.getSubtotal()).thenReturn(BigDecimal.ONE);
		when(shippingOptionService.getShippingOptions(shoppingCart)).thenReturn(shippingOptionsResult);
		when(shippingOptionsResult.isSuccessful()).thenReturn(true);
		when(shippingOptionsResult.getAvailableShippingOptions()).thenReturn(Arrays.asList(shippingOption1, shippingOption2));
		when(xpfShippingOptionConverter.convert(shippingOption1)).thenReturn(xpfShippingOption1);
		when(xpfShippingOptionConverter.convert(shippingOption2)).thenReturn(xpfShippingOption2);

		XPFShoppingCartValidationContext xpfShoppingCartValidationContextWithCustomerSession =
				shoppingCartValidationContextBuilder.build(shoppingCart);

		assertEquals(xpfShoppingCart, xpfShoppingCartValidationContextWithCustomerSession.getShoppingCart());
		assertTrue(xpfShoppingCartValidationContextWithCustomerSession.isPaymentRequired());
		assertEquals(2, xpfShoppingCartValidationContextWithCustomerSession.getAvailableShippingOptions().size());
	}

	@Test
	public void testBuildWithMissingShoppingCart() {
		exceptionThrown.expect(NullPointerException.class);
		exceptionThrown.expectMessage("Required field shoppingCart for building XPFShoppingCartValidationContext is missing.");

		shoppingCartValidationContextBuilder.build(null);
	}
}