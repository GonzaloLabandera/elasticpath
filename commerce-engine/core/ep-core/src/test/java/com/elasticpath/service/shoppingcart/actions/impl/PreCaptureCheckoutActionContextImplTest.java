/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContextImpl;

/**
 * Unit test for {@link PreCaptureCheckoutActionContextImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PreCaptureCheckoutActionContextImplTest {

	@Test
	public void testOrderHoldContext() {
		OrderHold mockOrderHold = mock(OrderHold.class);
		PreCaptureCheckoutActionContextImpl checkoutActionContext = new PreCaptureCheckoutActionContextImpl(mockOrderHold);

		assertThat(checkoutActionContext.getOrderHolds())
				.asList()
				.contains(mockOrderHold);
	}

	@Test
	public void testAddOrderHoldContext() {
		PreCaptureCheckoutActionContextImpl checkoutActionContext = new PreCaptureCheckoutActionContextImpl();

		OrderHold mockOrderHold = mock(OrderHold.class);
		checkoutActionContext.addOrderHold(mockOrderHold);

		assertThat(checkoutActionContext.getOrderHolds())
				.asList()
				.contains(mockOrderHold);
	}

	@Test
	public void testFullConstructor() {
		ShoppingCart shoppingCart = mock(ShoppingCart.class);
		ShoppingCartTaxSnapshot shoppingCartTaxSnapshot = mock(ShoppingCartTaxSnapshot.class);
		CustomerSession customerSession = mock(CustomerSession.class);
		OrderReturn orderReturn = mock(OrderReturn.class);

		PreCaptureCheckoutActionContextImpl checkoutActionContext = new PreCaptureCheckoutActionContextImpl(
				shoppingCart,
				shoppingCartTaxSnapshot,
				customerSession,
				false,
				false,
				orderReturn,
				null
		);

		assertThat(checkoutActionContext.getShoppingCart()).isEqualTo(shoppingCart);
		assertThat(checkoutActionContext.getShoppingCartTaxSnapshot()).isEqualTo(shoppingCartTaxSnapshot);
		assertThat(checkoutActionContext.getCustomerSession()).isEqualTo(customerSession);
		assertThat(checkoutActionContext.isOrderExchange()).isFalse();
		assertThat(checkoutActionContext.isAwaitExchangeCompletion()).isFalse();
		assertThat(checkoutActionContext.getOrderHolds()).asList().isEmpty();
	}
}
