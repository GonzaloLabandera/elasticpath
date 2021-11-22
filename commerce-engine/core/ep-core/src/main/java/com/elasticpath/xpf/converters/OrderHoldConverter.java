/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;
import com.elasticpath.xpf.connectivity.entity.XPFOrderHold;

/**
 * Converts {@code com.elasticpath.xpf.connectivity.entity.XPFOrderHold} to {@code com.elasticpath.domain.order.OrderHold}.
 */
public class OrderHoldConverter implements Converter<XPFOrderHold, OrderHold> {

	private BeanFactory beanFactory;

	@Override
	public OrderHold convert(final XPFOrderHold xpfOrderHold) {
		final OrderHold orderHold = beanFactory.getPrototypeBean(ContextIdNames.ORDER_HOLD, OrderHold.class);
		orderHold.setHoldDescription(xpfOrderHold.getHoldDescription());
		orderHold.setPermission(xpfOrderHold.getPermissionToRelease());
		orderHold.setStatus(OrderHoldStatus.ACTIVE);
		return orderHold;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
