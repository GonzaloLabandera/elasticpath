/*
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.commons.util;

import java.util.Comparator;

import com.elasticpath.domain.order.OrderReturnSku;

/**
 * The comparator factory which uses to create order return sku comparators.
 */
public final class OrderReturnSkuComparatorFactory {
	
	private OrderReturnSkuComparatorFactory() {
	}

	/**
	 * Compare by return sku code in ascending order.
	 * 
	 * @return comparator
	 */
	public static Comparator<OrderReturnSku> getOrderReturnSkuComparator() {
		return Comparator.comparing(arg0 -> arg0.getOrderSku().getSkuCode());
	}
}
