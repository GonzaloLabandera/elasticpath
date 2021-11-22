/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static com.elasticpath.commons.constants.ContextIdNames.CART_ORDER_SERVICE;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.xpf.connectivity.entity.XPFAddress;
import com.elasticpath.xpf.connectivity.entity.XPFContact;
import com.elasticpath.xpf.connectivity.entity.XPFShippingOption;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingItem;

/**
 * Converts {@code com.elasticpath.domain.shoppingcart.ShoppingCart} to {@code com.elasticpath.xpf.connectivity.context.ShoppingCart}.
 */
public class ShoppingCartConverter implements Converter<ShoppingCart, XPFShoppingCart> {

	private ShoppingItemConverter xpfShoppingItemConverter;
	private AddressConverter xpfAddressConverter;
	private ContactConverter xpfContactConverter;
	private ShopperConverter xpfShopperConverter;
	private ShippingOptionConverter xpfShippingOptionConverter;
	private BeanFactory beanFactory;

	@Override
	public XPFShoppingCart convert(final ShoppingCart shoppingCart) {

		final CartOrderService cartOrderService = beanFactory.getSingletonBean(CART_ORDER_SERVICE, CartOrderService.class);

		XPFAddress xpfShippingAddress = null;
		XPFContact xpfShippingContact = null;
		XPFAddress xpfBillingAddress = null;
		XPFContact xpfBillingContact = null;

		final List<XPFShoppingItem> shoppingItems = shoppingCart.getAllShoppingItems().stream()
				.map(shoppingItem -> xpfShoppingItemConverter.convert(new StoreDomainContext<>(shoppingItem, shoppingCart.getStore())))
				.collect(Collectors.toList());

		if (shoppingCart.getShippingAddress() != null) {
			xpfShippingAddress = xpfAddressConverter.convert(shoppingCart.getShippingAddress());
			xpfShippingContact = xpfContactConverter.convert(shoppingCart.getShippingAddress());
		}
		if (shoppingCart.getBillingAddress() != null) {
			xpfBillingAddress = xpfAddressConverter.convert(shoppingCart.getBillingAddress());
			xpfBillingContact = xpfContactConverter.convert(shoppingCart.getBillingAddress());
		}
		final Map<String, String> modifierFields = shoppingCart.getModifierFields().getMap();

		final XPFShopper xpfShopper = xpfShopperConverter.convert(shoppingCart.getShopper());

		final XPFShippingOption xpfShippingOption = shoppingCart.getSelectedShippingOption().map(xpfShippingOptionConverter::convert).orElse(null);

		String cartOrderGuid = cartOrderService.getCartOrderGuidByShoppingCartGuid(shoppingCart.getGuid());

		return new XPFShoppingCart(shoppingCart.getGuid(), cartOrderGuid, xpfShopper, shoppingItems,
				shoppingCart.getNumItems(), shoppingCart.requiresShipping(), xpfShippingAddress, xpfBillingAddress, xpfShippingContact,
				xpfBillingContact, modifierFields, xpfShippingOption);
	}

	public void setXpfShoppingItemConverter(final ShoppingItemConverter xpfShoppingItemConverter) {
		this.xpfShoppingItemConverter = xpfShoppingItemConverter;
	}

	public void setXpfAddressConverter(final AddressConverter xpfAddressConverter) {
		this.xpfAddressConverter = xpfAddressConverter;
	}

	public void setXpfContactConverter(final ContactConverter xpfContactConverter) {
		this.xpfContactConverter = xpfContactConverter;
	}

	public void setXpfShopperConverter(final ShopperConverter xpfShopperConverter) {
		this.xpfShopperConverter = xpfShopperConverter;
	}

	public void setXpfShippingOptionConverter(final ShippingOptionConverter xpfShippingOptionConverter) {
		this.xpfShippingOptionConverter = xpfShippingOptionConverter;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
