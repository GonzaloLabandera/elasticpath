/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.bridges;

import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.bridges.impl.OrderHoldStrategyXPFBridgeImpl;
import com.elasticpath.xpf.connectivity.context.XPFOrderHoldStrategyContext;
import com.elasticpath.xpf.connectivity.entity.XPFOrderHold;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFStore;
import com.elasticpath.xpf.connectivity.extensionpoint.OrderHoldStrategy;
import com.elasticpath.xpf.converters.OrderHoldConverter;
import com.elasticpath.xpf.converters.ShoppingCartConverter;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

/**
 * Test or {@link OrderHoldStrategyXPFBridge}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderHoldStrategyXPFBridgeTest {
	private static final String STORE_CODE = "storeCode";
	@Mock
	private ShoppingCartConverter shoppingCartConverter;
	@Mock
	private OrderHoldConverter orderHoldConverter;
	@Mock
	private XPFExtensionLookup xpfExtensionLookup;
	@Mock
	private PreCaptureCheckoutActionContext actionContext;
	@Mock
	private ShoppingCart shoppingCart;
	@Mock
	private XPFShoppingCart xpfShoppingCart;
	@Mock
	private OrderHoldStrategy firstOrderHoldStrategy;
	@Mock
	private OrderHoldStrategy secondOrderHoldStrategy;
	@Mock
	private XPFStore xpfStore;
	@Mock
	private XPFOrderHold xpfOrderHold;
	@Mock
	private XPFShopper xpfShopper;
	@Mock
	private OrderHold orderHold;
	@Mock
	private Shopper shopper;

	@InjectMocks
	private OrderHoldStrategyXPFBridgeImpl bridge;

	@Before
	public void setUp() {
		when(xpfShopper.getStore()).thenReturn(xpfStore);
		when(xpfStore.getCode()).thenReturn(STORE_CODE);
		when(actionContext.getShoppingCart()).thenReturn(shoppingCart);
		when(shoppingCartConverter.convert(shoppingCart)).thenReturn(xpfShoppingCart);
		when(xpfShoppingCart.getShopper()).thenReturn(xpfShopper);
		when(xpfExtensionLookup.getMultipleExtensions(OrderHoldStrategy.class, XPFExtensionPointEnum.ORDER_HOLD_STRATEGY,
				new XPFExtensionSelectorByStoreCode(xpfShoppingCart.getShopper().getStore().getCode())))
				.thenReturn(Collections.singletonList(firstOrderHoldStrategy));
		when(firstOrderHoldStrategy.evaluate(new XPFOrderHoldStrategyContext(xpfShoppingCart))).thenReturn(Optional.of(xpfOrderHold));
		when(secondOrderHoldStrategy.evaluate(new XPFOrderHoldStrategyContext(xpfShoppingCart))).thenReturn(Optional.of(xpfOrderHold));
		when(orderHoldConverter.convert(xpfOrderHold)).thenReturn(orderHold);
		when(actionContext.getShopper()).thenReturn(shopper);
		when(shopper.getStoreCode()).thenReturn(STORE_CODE);
	}

	@Test
	public void testEvaluateOrderHoldsWithSingleExtensionFullResponse() {
		List<OrderHold> result = bridge.evaluateOrderHolds(actionContext);

		assertEquals(1, result.size());
		assertEquals(orderHold, result.get(0));

		verify(shoppingCartConverter).convert(shoppingCart);
		verify(xpfExtensionLookup).getMultipleExtensions(OrderHoldStrategy.class, XPFExtensionPointEnum.ORDER_HOLD_STRATEGY,
				new XPFExtensionSelectorByStoreCode(xpfShoppingCart.getShopper().getStore().getCode()));
		verify(firstOrderHoldStrategy).evaluate(new XPFOrderHoldStrategyContext(xpfShoppingCart));
		verify(orderHoldConverter).convert(xpfOrderHold);
	}

	@Test
	public void testEvaluateOrderHoldsWithMultipleExtensions() {
		when(xpfExtensionLookup.getMultipleExtensions(OrderHoldStrategy.class, XPFExtensionPointEnum.ORDER_HOLD_STRATEGY,
				new XPFExtensionSelectorByStoreCode(xpfShoppingCart.getShopper().getStore().getCode())))
				.thenReturn(Arrays.asList(firstOrderHoldStrategy, secondOrderHoldStrategy));

		List<OrderHold> result = bridge.evaluateOrderHolds(actionContext);

		assertEquals(2, result.size());
		assertEquals(result.get(0), result.get(1));
	}

	@Test
	public void testEvaluateOrderHoldsWithMultipleExtensionsExceptionResponse() {
		when(xpfExtensionLookup.getMultipleExtensions(OrderHoldStrategy.class, XPFExtensionPointEnum.ORDER_HOLD_STRATEGY,
				new XPFExtensionSelectorByStoreCode(xpfShoppingCart.getShopper().getStore().getCode())))
				.thenReturn(Arrays.asList(firstOrderHoldStrategy, secondOrderHoldStrategy));
		when(firstOrderHoldStrategy.evaluate(new XPFOrderHoldStrategyContext(xpfShoppingCart))).thenThrow(new RuntimeException());

		assertThatThrownBy(() -> bridge.evaluateOrderHolds(actionContext)).isInstanceOf(RuntimeException.class);

		verify(secondOrderHoldStrategy, never()).evaluate(new XPFOrderHoldStrategyContext(xpfShoppingCart));
	}
}
