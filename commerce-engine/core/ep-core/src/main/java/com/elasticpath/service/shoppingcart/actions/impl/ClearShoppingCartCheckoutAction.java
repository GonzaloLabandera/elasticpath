/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.orderpaymentapi.CartOrderPaymentInstrumentService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.actions.FinalizeCheckoutAction;
import com.elasticpath.service.shoppingcart.actions.FinalizeCheckoutActionContext;

/**
 * CheckoutAction to remove all items from the shoppingCart after converting the cart to an order. It also removes the cart order.
 */
public class ClearShoppingCartCheckoutAction implements FinalizeCheckoutAction {

	private ShoppingCartService shoppingCartService;
	private CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService;

	@Override
	public void execute(final FinalizeCheckoutActionContext context) throws EpSystemException {
		ShoppingCart oldShoppingCart = context.getShoppingCart();
		if (!context.isOrderExchange()) {
			shoppingCartService.deactivateCart(oldShoppingCart);
		}

		//free some memory - doesn't incur new db calls because the cart is not saved nor referenced anywhere else
		oldShoppingCart.clearItems();
		/*
			all cart-related data (cart orders, CO payment instruments, CO coupons) etc will be completely removed by the
			InactiveCartsCleanupJob
		 */
	}

	protected ShoppingCartService getShoppingCartService() {
		return shoppingCartService;
	}

	public void setShoppingCartService(final ShoppingCartService shoppingCartService) {
		this.shoppingCartService = shoppingCartService;
	}

	public void setCartOrderPaymentInstrumentService(final CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService) {
		this.cartOrderPaymentInstrumentService = cartOrderPaymentInstrumentService;
	}

	protected CartOrderPaymentInstrumentService getCartOrderPaymentInstrumentService() {
		return cartOrderPaymentInstrumentService;
	}
}
