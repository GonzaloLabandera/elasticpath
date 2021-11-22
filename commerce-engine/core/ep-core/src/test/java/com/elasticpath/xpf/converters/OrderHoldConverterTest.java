/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;
import com.elasticpath.domain.order.impl.OrderHoldImpl;
import com.elasticpath.xpf.connectivity.entity.XPFOrderHold;

@RunWith(MockitoJUnitRunner.class)
public class OrderHoldConverterTest {

	private static final String PERMISSION = "permission";
	private static final String DESCRIPTION = "description";
	private static final String GUID = "guid";

	@Mock
	private BeanFactory beanFactory;

	@InjectMocks
	private OrderHoldConverter orderHoldConverter;


	@Test
	public void testConvert() {
		final OrderHold orderHold = new OrderHoldImpl();
		orderHold.setGuid(GUID);
		when(beanFactory.getPrototypeBean(ContextIdNames.ORDER_HOLD, OrderHold.class)).thenReturn(orderHold);
		XPFOrderHold xpfOrderHold = new XPFOrderHold(PERMISSION, DESCRIPTION);

		OrderHold result = orderHoldConverter.convert(xpfOrderHold);

		assertSame(orderHold, result);
		assertEquals(GUID, result.getGuid());
		assertEquals(PERMISSION, result.getPermission());
		assertEquals(DESCRIPTION, result.getHoldDescription());
		assertEquals(OrderHoldStatus.ACTIVE, result.getStatus());
	}
}
