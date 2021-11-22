/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static com.elasticpath.commons.constants.ContextIdNames.CART_ORDER_SERVICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;
import com.elasticpath.xpf.connectivity.entity.XPFAddress;
import com.elasticpath.xpf.connectivity.entity.XPFContact;
import com.elasticpath.xpf.connectivity.entity.XPFShippingOption;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({ "PMD.TooManyFields" })
public class ShoppingCartConverterTest {

	private static final String SHOPPING_CART_GUID = "shoppingCartGuid";
	private static final String CART_ORDER_GUID = "cartOrderGuid";

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ShoppingCart shoppingCart;
	@Mock
	private Address shoppingCartAddress;
	@Mock
	private XPFAddress addressContext;
	@Mock
	private XPFContact contactContext;
	@Mock
	private Shopper shopper;
	@Mock
	private XPFShopper shopperContext;
	@Mock
	private Map<String, String> modifierFields;
	@Mock
	private ShoppingItem shoppingItem1, shoppingItem2;
	@Mock
	private XPFShoppingItem contextShoppingItem1, contextShoppingItem2;
	@Mock
	private ShippingOption shippingOption;
	@Mock
	private XPFShippingOption xpfShippingOption;
	@Mock
	private ShoppingItemConverter shoppingItemConverter;
	@Mock
	private AddressConverter xpfAddressConverter;
	@Mock
	private ContactConverter xpfContactConverter;
	@Mock
	private ShippingOptionConverter xpfShippingOptionConverter;
	@Mock
	private ShopperConverter xpfShopperConverter;
	@Mock
	private CartOrderService cartOrderService;
	@Mock
	private BeanFactory beanFactory;
	@InjectMocks
	private ShoppingCartConverter shoppingCartConverter;

	List<XPFShoppingItem> contextShoppingItemList;

	@Before
	public void setUp() {
		contextShoppingItemList = Lists.newArrayList(contextShoppingItem1, contextShoppingItem2);
		when(beanFactory.getSingletonBean(CART_ORDER_SERVICE, CartOrderService.class)).thenReturn(cartOrderService);
		when(shoppingItemConverter.convert(new StoreDomainContext<>(shoppingItem1, shoppingCart.getStore()))).thenReturn(contextShoppingItem1);
		when(shoppingItemConverter.convert(new StoreDomainContext<>(shoppingItem2, shoppingCart.getStore()))).thenReturn(contextShoppingItem2);
		when(xpfShippingOptionConverter.convert(shippingOption)).thenReturn(xpfShippingOption);
		when(shoppingCart.getGuid()).thenReturn(SHOPPING_CART_GUID);
		when(cartOrderService.getCartOrderGuidByShoppingCartGuid(SHOPPING_CART_GUID)).thenReturn(CART_ORDER_GUID);
		when(shoppingCart.getShopper()).thenReturn(shopper);
	}

	@Test
	public void testConvertWithFullInputs() {
		when(shoppingCart.getAllShoppingItems()).thenReturn(Lists.newArrayList(shoppingItem1, shoppingItem2));
		when(shoppingCart.getNumItems()).thenReturn(2);
		when(shoppingCart.getShippingAddress()).thenReturn(shoppingCartAddress);
		when(shoppingCart.getBillingAddress()).thenReturn(shoppingCartAddress);
		when(shoppingCart.getModifierFields().getMap()).thenReturn(modifierFields);
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(shoppingCart.getSelectedShippingOption()).thenReturn(Optional.of(shippingOption));
		when(xpfAddressConverter.convert(shoppingCartAddress)).thenReturn(addressContext);
		when(xpfContactConverter.convert(shoppingCartAddress)).thenReturn(contactContext);
		when(xpfShopperConverter.convert(shopper)).thenReturn(shopperContext);

		XPFShoppingCart contextShoppingCart = shoppingCartConverter.convert(shoppingCart);
		assertEquals(contextShoppingItemList, contextShoppingCart.getLineItems());
		assertEquals(2, contextShoppingCart.getQuantity());
	}

	@Test
	public void testConvertWithMinInputs() {
		when(shoppingCart.getAllShoppingItems()).thenReturn(Lists.newArrayList());
		when(shoppingCart.getNumItems()).thenReturn(0);
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(xpfShopperConverter.convert(shopper)).thenReturn(shopperContext);
		when(shoppingCart.getShippingAddress()).thenReturn(null);
		when(shoppingCart.getBillingAddress()).thenReturn(null);
		when(shoppingCart.getModifierFields().getMap()).thenReturn(Collections.emptyMap());
		when(shoppingCart.getSelectedShippingOption()).thenReturn(Optional.empty());

		XPFShoppingCart contextShoppingCart = shoppingCartConverter.convert(shoppingCart);
		assertTrue(contextShoppingCart.getLineItems().isEmpty());
		assertEquals(0, contextShoppingCart.getQuantity());
	}
}
