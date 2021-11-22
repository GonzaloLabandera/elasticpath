/**
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.xpf.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.settings.provider.SettingValueProvider;
import com.elasticpath.xpf.connectivity.context.XPFOrderHoldStrategyContext;
import com.elasticpath.xpf.connectivity.entity.XPFOrderHold;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFStore;

@RunWith(MockitoJUnitRunner.class)
public class OrderHoldStrategyTest {
	private static final String STORE_CODE = "storeCode";
	private static final String PERMISSION = "permission";
	@Mock
	private SettingValueProvider<Boolean> holdStrategyProvider;
	@Mock
	private SettingValueProvider<String> holdPermissionProvider;
	@Mock
	private BeanFactory beanFactory;
	@Mock
	private XPFOrderHoldStrategyContext orderHoldStrategyContext;
	@Mock
	private XPFShoppingCart xpfShoppingCart;
	@Mock
	private XPFShopper xpfShopper;
	@Mock
	private XPFStore xpfStore;

	@InjectMocks
	private HoldAllOrdersOrderHoldStrategyImpl strategy;

	@Before
	public void setUp() {
		when(beanFactory.getSingletonBean("holdStrategyProvider", SettingValueProvider.class)).thenReturn(holdStrategyProvider);
		when(beanFactory.getSingletonBean("holdPermissionProvider", SettingValueProvider.class)).thenReturn(holdPermissionProvider);
		when(orderHoldStrategyContext.getShoppingCart()).thenReturn(xpfShoppingCart);
		when(xpfShoppingCart.getShopper()).thenReturn(xpfShopper);
		when(xpfShopper.getStore()).thenReturn(xpfStore);
		when(xpfStore.getCode()).thenReturn(STORE_CODE);
		when(holdPermissionProvider.get(STORE_CODE)).thenReturn(PERMISSION);
	}


	@Test
	public void evaluateNotOnHoldTest() {
		when(holdStrategyProvider.get(STORE_CODE)).thenReturn(false);

		Optional<XPFOrderHold> orderHold = strategy.evaluate(orderHoldStrategyContext);

		assertFalse(orderHold.isPresent());
		verify(holdStrategyProvider).get(STORE_CODE);
		verify(holdPermissionProvider, never()).get(any());
	}

	@Test
	public void evaluateOnHoldTest() {
		when(holdStrategyProvider.get(STORE_CODE)).thenReturn(true);

		Optional<XPFOrderHold> orderHold = strategy.evaluate(orderHoldStrategyContext);

		assertTrue(orderHold.isPresent());
		assertEquals(HoldAllOrdersOrderHoldStrategyImpl.ALL_ORDERS_ARE_CONFIGURED_FOR_HOLD_PROCESSING, orderHold.get().getHoldDescription());
		assertEquals(PERMISSION, orderHold.get().getPermissionToRelease());

		verify(holdPermissionProvider).get(STORE_CODE);
		verify(holdStrategyProvider).get(STORE_CODE);
	}

}
