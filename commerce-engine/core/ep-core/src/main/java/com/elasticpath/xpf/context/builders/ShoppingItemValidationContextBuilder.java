/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.context.builders;

import java.util.stream.Stream;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.context.XPFShoppingItemValidationContext;
import com.elasticpath.xpf.connectivity.entity.XPFOperationEnum;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;

/**
 * Builder for {@code com.elasticpath.xpf.connectivity.context.ShoppingItemValidationContext}.
 */
public interface ShoppingItemValidationContextBuilder {
	/**
	 * Builds XPFShoppingItemValidationContext using the inputs provided.
	 *
	 * @param xpfShoppingCart the XPF shopping cart
	 * @param shoppingItemDto the shopping item dto object
	 * @param parentShoppingItem the parent shopping item
	 * @param operation specifies the operation type
	 * @param shopper the shopper
	 * @param store the store
	 *
	 * @return XPFShoppingItemValidationContext built using inputs provided
	 */
	XPFShoppingItemValidationContext build(
			XPFShoppingCart xpfShoppingCart,
			ShoppingItemDto shoppingItemDto,
			ShoppingItem parentShoppingItem,
			XPFOperationEnum operation,
			Shopper shopper,
			Store store);

	/**
	 * Builds XPFShoppingItemValidationContext using the inputs provided.
	 *
	 * @param shoppingCart the shopping cart
	 * @param shoppingItemDto the shopping item dto object
	 * @param parentShoppingItem the parent shopping item
	 * @param operation specifies the operation type
	 *
	 * @return XPFShoppingItemValidationContext built using inputs provided
	 */
	XPFShoppingItemValidationContext build(
			ShoppingCart shoppingCart,
			ShoppingItemDto shoppingItemDto,
			ShoppingItem parentShoppingItem,
			XPFOperationEnum operation);

	/**
	 * Builds XPFShoppingItemValidationContext using the inputs provided.
	 *
	 * @param xpfShoppingCart the XPF shopping cart
	 * @param shoppingItem the shopping item
	 * @param parentShoppingItem the parent shopping item
	 * @param operation specifies the operation type
	 * @param shopper the shopper
	 * @param store the store
	 *
	 * @return XPFShoppingItemValidationContext built using inputs provided
	 */
	XPFShoppingItemValidationContext build(
			XPFShoppingCart xpfShoppingCart,
			ShoppingItem shoppingItem,
			ShoppingItem parentShoppingItem,
			XPFOperationEnum operation,
			Shopper shopper,
			Store store);

	/**
	 * Builds XPFShoppingItemValidationContext using the inputs provided.
	 *
	 * @param shoppingCart the shopping cart
	 * @param shoppingItem the shopping item
	 * @param parentShoppingItem the parent shopping item
	 * @param operation specifies the operation type
	 *
	 * @return XPFShoppingItemValidationContext built using inputs provided
	 */
	XPFShoppingItemValidationContext build(
			ShoppingCart shoppingCart,
			ShoppingItem shoppingItem,
			ShoppingItem parentShoppingItem,
			XPFOperationEnum operation);

	/**
	 * Return a stream containing the passed context along with contexts for it's children at all levels of the tree.
	 *
	 * @param context the parent XPFShoppingItemValidationContext
	 * @return a stream of XPFShoppingItemValidationContext objects from all levels of the tree
	 */
	Stream<XPFShoppingItemValidationContext> getAllContextsStream(XPFShoppingItemValidationContext context);

}
