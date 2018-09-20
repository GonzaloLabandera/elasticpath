/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static java.util.Arrays.asList;

import java.util.Locale;

import com.google.common.collect.ImmutableMap;

import com.elasticpath.common.dto.StructuredErrorMessage;
import com.elasticpath.commons.beanframework.MessageSource;
import com.elasticpath.domain.catalog.AvailabilityException;
import com.elasticpath.domain.catalog.InsufficientInventoryException;
import com.elasticpath.domain.catalog.MinOrderQtyException;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.order.OrderMessageIds;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Warehouse;
import com.elasticpath.sellingchannel.inventory.ProductInventoryShoppingService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.order.AllocationService;
import com.elasticpath.service.shoppingcart.ShoppingCartEmptyException;
import com.elasticpath.service.shoppingcart.actions.CheckoutAction;
import com.elasticpath.service.shoppingcart.actions.CheckoutActionContext;

/**
 * CheckoutAction to validate whether there is sufficient stock available for physical products.
 */
public class StockCheckerCheckoutAction implements CheckoutAction {

	private static final String ITEM_CODE = "item-code";
	private AllocationService allocationService;

	private ProductInventoryShoppingService productInventoryShoppingService;

	private MessageSource messageSource;
	private ProductSkuLookup productSkuLookup;
	/**
	 * Enumeration representing text IDs for error messages.
	 */
	public enum ErrorMessage {
		/** The inventory message Key. */
		INVENTORY("globals.cart.insufficientinventory"),
		/** The unavailable message Key. */
		UNAVAILABLE("globals.cart.unavailable");

		private final String messageKey;

		/**
		 * Constructs this enumeration.
		 *
		 * @param message the message key to be used for getting the real text
		 */
		ErrorMessage(final String message) {
			this.messageKey = message;
		}

		/**
		 * Gets the message key.
		 *
		 * @return the message key
		 */
		public String message() {
			return this.messageKey;
		}

		/**
		 * @param messageSource - the messageSource
		 * @param locale - the locale to retrieve from
		 * @return - the attached message from the properties file.
		 */
		public String getTextForMessage(final MessageSource messageSource, final Locale locale) {
			return messageSource.getMessage(this.messageKey, null, this.messageKey, locale);
		}
	}

	@Override
	public void execute(final CheckoutActionContext context) {
		final ShoppingCart shoppingCart = context.getShoppingCart();

		if (shoppingCart.getNumItems() <= 0) {
			String errorMessage = "Shopping cart must not be empty during checkout.";
			throw new ShoppingCartEmptyException(
					errorMessage,
					asList(
							new StructuredErrorMessage(
									OrderMessageIds.CART_IS_EMPTY,
									errorMessage,
									null
							)
					)
			);

		}

		final Warehouse fulfillingWarehouse = getFulfillingWarehouse(shoppingCart);
		final TemporaryInventory inventory = new TemporaryInventory(fulfillingWarehouse, productInventoryShoppingService);

		Locale locale = context.getCustomerSession().getLocale();
		for (final ShoppingItem shoppingItem : shoppingCart.getLeafShoppingItems()) {
			final ProductSku itemSku = getProductSkuLookup().findByGuid(shoppingItem.getSkuGuid());
			this.verifyInventory(shoppingItem, itemSku, fulfillingWarehouse, locale);
			this.verifyAvailability(shoppingItem, itemSku, locale);

			this.verifyCartItemInventory(shoppingItem, itemSku, inventory, locale);
		}
		for (final ShoppingItem shoppingItem : shoppingCart.getRootShoppingItems()) {
			if (shoppingItem.isBundle(getProductSkuLookup())) {
				continue;
			}
			this.verifyMinOrderQty(shoppingItem);
		}
	}

	/**
	 * Verifies that the cart item is still available for order.
	 *
	 * @param shoppingItem the cartItem to be checked
	 * @param itemSku      the product sku that the cart item refers to
	 * @param locale       of error if there is one
	 * @throws AvailabilityException if any of the items in the cart are no longer available
	 */
	protected void verifyAvailability(final ShoppingItem shoppingItem, final ProductSku itemSku, final Locale locale) {
		if (itemSku != null && !itemSku.isWithinDateRange()) {
			this.setCartItemErrorMessage(shoppingItem, ErrorMessage.UNAVAILABLE, locale);
			String errorMessage = "Item is not available for purchase: " + itemSku.getSkuCode();
			throw new AvailabilityException(
					errorMessage,
					asList(
							new StructuredErrorMessage(
									OrderMessageIds.ITEM_NOT_AVAILABLE,
									errorMessage,
									ImmutableMap.of(ITEM_CODE, itemSku.getSkuCode())
							)
					)
			);

		}
	}

	/**
	 * Checks for each cart item.
	 *
	 * @param shoppingItem a shopping item
	 * @param itemSku      the product sku that the cart item refers to
	 * @param inventory    a temporary inventory object
	 * @param locale       the locale
	 * @throws InsufficientInventoryException if there is not sufficient inventory
	 */
	protected void verifyCartItemInventory(
			final ShoppingItem shoppingItem, final ProductSku itemSku, final TemporaryInventory inventory, final Locale locale) {
		if (!inventory.hasSufficient(shoppingItem, itemSku)) {
			setCartItemErrorMessage(shoppingItem, ErrorMessage.INVENTORY, locale);
			String errorMessage = "Insufficient inventory available for SKU: "	+ itemSku.getSkuCode();
			throw new InsufficientInventoryException(
					errorMessage,
					asList(
							new StructuredErrorMessage(
									OrderMessageIds.INSUFFICIENT_INVENTORY,
									errorMessage,
									ImmutableMap.of(ITEM_CODE, itemSku.getSkuCode())
							)
					)
			);

		}
	}

	/**
	 * Verifies that there is sufficient inventory available to satisfy the order.
	 *
	 * @param shoppingItem the cart item to be checked
	 * @param itemSku      the product sku that the cart item is using
	 * @param warehouse    the warehouse to check inventory
	 * @param locale       the locale to be used
	 * @throws InsufficientInventoryException if there is not enough stock available
	 */
	protected void verifyInventory(final ShoppingItem shoppingItem, final ProductSku itemSku, final Warehouse warehouse, final Locale locale) {
		if (!allocationService.hasSufficientUnallocatedQty(itemSku, warehouse.getUidPk(), shoppingItem.getQuantity())) {
			this.setCartItemErrorMessage(shoppingItem, ErrorMessage.INVENTORY, locale);
			String errorMessage = "Insufficient inventory available for SKU: "	+ itemSku.getSkuCode();
			throw new InsufficientInventoryException(
					errorMessage,
					asList(
							new StructuredErrorMessage(
									OrderMessageIds.INSUFFICIENT_INVENTORY,
									errorMessage,
									ImmutableMap.of(ITEM_CODE, itemSku.getSkuCode())
							)
					)
			);
		}
	}

	/**
	 * Verifies that the quantity of items in the cart item are not below the minimum order quantity.
	 *
	 * @param shoppingItem the cartItem to be checked
	 * @throws MinOrderQtyException if any of the items in the cart are no longer available
	 */
	protected void verifyMinOrderQty(final ShoppingItem shoppingItem) {
		ProductSku itemSku = getProductSkuLookup().findByGuid(shoppingItem.getSkuGuid());
		if (shoppingItem.getQuantity() < itemSku.getProduct().getMinOrderQty()) {
			String errorMessage = "SKU: " + itemSku.getSkuCode();
			throw new MinOrderQtyException(
					errorMessage,
					asList(
							new StructuredErrorMessage(
									OrderMessageIds.MINIMUM_QUANTITY_REQUIRED,
									errorMessage,
									ImmutableMap.of(
											ITEM_CODE, itemSku.getSkuCode(),
											"minimum-quantity", String.valueOf(itemSku.getProduct().getMinOrderQty())
									)
							)
					)
			);
		}
	}

	private Warehouse getFulfillingWarehouse(final ShoppingCart shoppingCart) {
		return shoppingCart.getStore().getWarehouse();
	}

	/**
	 * Sets the error message to the cart item.
	 *
	 * @param shoppingItem the cart item
	 * @param error the error message
	 * @param locale of the message
	 */
	protected void setCartItemErrorMessage(final ShoppingItem shoppingItem, final ErrorMessage error, final Locale locale) {
		String cartErrorMessage = "";
		if (shoppingItem.getErrorMessage() != null) {
			cartErrorMessage = shoppingItem.getErrorMessage();
		}
		final String errorMessage = cartErrorMessage
		+ error.getTextForMessage(messageSource, locale);
		shoppingItem.setErrorMessage(errorMessage);
	}

	protected AllocationService getAllocationService() {
		return allocationService;
	}

	public void setAllocationService(final AllocationService allocationService) {
		this.allocationService = allocationService;
	}

	protected ProductInventoryShoppingService getProductInventoryShoppingService() {
		return productInventoryShoppingService;
	}

	public void setProductInventoryShoppingService(final ProductInventoryShoppingService productInventoryShoppingService) {
		this.productInventoryShoppingService = productInventoryShoppingService;
	}

	protected MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(final MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	protected ProductSkuLookup getProductSkuLookup() {
		return productSkuLookup;
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}
}

