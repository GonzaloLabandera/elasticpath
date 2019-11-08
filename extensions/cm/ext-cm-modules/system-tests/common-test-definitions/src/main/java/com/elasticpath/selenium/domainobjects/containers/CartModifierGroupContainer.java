/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.elasticpath.selenium.domainobjects.CartItemModifierGroup;

/**
 * Cart Modifier Group container class.
 */
public class CartModifierGroupContainer {
	private final List<CartItemModifierGroup> cartItemModifierGroups = new ArrayList<>();

	public List<CartItemModifierGroup> getCartItemModifierGroups() {
		return cartItemModifierGroups;
	}

	public void addCartItemModifierGroups(final CartItemModifierGroup cartItemModifierGroup) {
		Optional.ofNullable(cartItemModifierGroup).ifPresent(cartItemModifierGroups::add);
	}
}
