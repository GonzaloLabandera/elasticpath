/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
/**
 * 
 */
package com.elasticpath.sellingchannel.presentation.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import com.elasticpath.common.dto.OrderItemDto;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.quantity.Quantity;
import com.elasticpath.domain.shoppingcart.FrequencyAndRecurringPrice;
import com.elasticpath.domain.shoppingcart.FrequencyAndRecurringPriceFactory;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.sellingchannel.director.OrderDirector;
import com.elasticpath.sellingchannel.presentation.OrderItemDtoListMapper;
import com.elasticpath.sellingchannel.presentation.OrderItemPresentationBean;
import com.elasticpath.sellingchannel.presentation.OrderPresentationHelper;
import com.elasticpath.service.shoppingcart.OrderSkuToPricingSnapshotFunction;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;

/**
 * Provides functions to help translate Orders into beans for presentation in Velocity templates
 * and other templates that can't deal with recursively traversing a tree of nested items.
 */
public class OrderPresentationHelperImpl implements OrderPresentationHelper {

	private OrderDirector orderDirector;

	private OrderItemDtoListMapper orderItemDtoListMapper;

	private PricingSnapshotService pricingSnapshotService;

	/**
	 * Creates a map of ShipmentNumber to OrderItemPresentationBean for the given Order.
	 * @param order an order.
	 * @return order item presentation bean map for an order.
	 */
	@Override
	public Map<Long, List<? extends OrderItemPresentationBean>> createOrderItemFormBeanMap(final Order order) {
		Map<Long, List<? extends OrderItemPresentationBean>> orderItemPresentationBeanMap =
			new HashMap<>();

		for (OrderShipment shipment : order.getAllShipments()) {
			orderItemPresentationBeanMap.put(shipment.getUidPk(), createOrderItemFormBeanList(shipment));
		}

		return orderItemPresentationBeanMap;
	}

	/**
	 * Create a list of OrderItemPresentationBeans for this shipment.
	 *
	 * @param shipment the shipment
	 * @return a list of OrderItemPresentationBeans for this shipment
	 */
	@Override
	public List<? extends OrderItemPresentationBean> createOrderItemFormBeanList(final OrderShipment shipment) {
		List<OrderItemDto> orderItemDtoList = orderDirector.createOrderItemDtoList(shipment);
		return orderItemDtoListMapper.mapFrom(orderItemDtoList);
	}

	/**
	 * @param orderDirector the orderDirector to set
	 */
	public void setOrderDirector(final OrderDirector orderDirector) {
		this.orderDirector = orderDirector;
	}

	/**
	 * @param orderItemDtoListMapper the orderItemDtoListMapper to set
	 */
	public void setOrderItemDtoListMapper(final OrderItemDtoListMapper orderItemDtoListMapper) {
		this.orderItemDtoListMapper = orderItemDtoListMapper;
	}
	
	
	/**
	 * Gets the frequency map for the order.
	 * @param order the order to use
	 * @return the frequency map for the order's shopping items.
	 */
	@Override
	public Map<Quantity, FrequencyAndRecurringPrice> getFrequencyMap(final Order order) {
		if (order == null) {
			return null;
		}

		final Iterable<OrderSku> orderSkus = Iterables.transform(order.getRootShoppingItems(), new Function<ShoppingItem, OrderSku>() {
			@Override
			public OrderSku apply(final ShoppingItem input) {
				return (OrderSku) input;
			}
		});

		final Map<OrderSku, ShoppingItemPricingSnapshot> itemPricingSnapshotMap =
				Maps.toMap(orderSkus, new OrderSkuToPricingSnapshotFunction(getPricingSnapshotService()));

		return createFrequencyAndRecurringPriceFactory().getFrequencyMap(itemPricingSnapshotMap);
	}

	/**
	 * Factory method to create a new FrequencyAndRecurringPriceFactory instance.
	 *
	 * @return a new FrequencyAndRecurringPriceFactory instance
	 */
	protected FrequencyAndRecurringPriceFactory createFrequencyAndRecurringPriceFactory() {
		return new FrequencyAndRecurringPriceFactory();
	}

	protected PricingSnapshotService getPricingSnapshotService() {
		return pricingSnapshotService;
	}

	public void setPricingSnapshotService(final PricingSnapshotService pricingSnapshotService) {
		this.pricingSnapshotService = pricingSnapshotService;
	}

}
