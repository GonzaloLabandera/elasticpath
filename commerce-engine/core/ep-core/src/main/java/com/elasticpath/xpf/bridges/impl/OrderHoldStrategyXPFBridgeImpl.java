/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.bridges.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.bridges.OrderHoldStrategyXPFBridge;
import com.elasticpath.xpf.connectivity.context.XPFOrderHoldStrategyContext;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.extensionpoint.OrderHoldStrategy;
import com.elasticpath.xpf.converters.OrderHoldConverter;
import com.elasticpath.xpf.converters.ShoppingCartConverter;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

/**
 * Implementation of {@code com.elasticpath.xpf.bridges.OrderHoldStrategyXPFBridge}.
 */
public class OrderHoldStrategyXPFBridgeImpl implements OrderHoldStrategyXPFBridge {

	private ShoppingCartConverter shoppingCartConverter;
	private OrderHoldConverter orderHoldConverter;
	private XPFExtensionLookup xpfExtensionLookup;

	@Override
	public List<OrderHold> evaluateOrderHolds(final PreCaptureCheckoutActionContext context) {
		final XPFShoppingCart xpfShoppingCart = shoppingCartConverter.convert(context.getShoppingCart());
		final XPFOrderHoldStrategyContext strategyContext = new XPFOrderHoldStrategyContext(xpfShoppingCart);

		final List<OrderHoldStrategy> strategies = xpfExtensionLookup.getMultipleExtensions(OrderHoldStrategy.class,
				XPFExtensionPointEnum.ORDER_HOLD_STRATEGY, new XPFExtensionSelectorByStoreCode(context.getShopper().getStoreCode()));

		return strategies.stream()
				.map(strategy -> strategy.evaluate(strategyContext))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(orderHoldConverter::convert)
				.collect(Collectors.toList());

	}

	protected ShoppingCartConverter getShoppingCartConverter() {
		return shoppingCartConverter;
	}

	public void setShoppingCartConverter(final ShoppingCartConverter shoppingCartConverter) {
		this.shoppingCartConverter = shoppingCartConverter;
	}

	protected OrderHoldConverter getOrderHoldConverter() {
		return orderHoldConverter;
	}

	public void setOrderHoldConverter(final OrderHoldConverter orderHoldConverter) {
		this.orderHoldConverter = orderHoldConverter;
	}

	protected XPFExtensionLookup getXpfExtensionLookup() {
		return xpfExtensionLookup;
	}

	public void setXpfExtensionLookup(final XPFExtensionLookup xpfExtensionLookup) {
		this.xpfExtensionLookup = xpfExtensionLookup;
	}
}
