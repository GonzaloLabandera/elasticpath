/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.shoppingcart.actions;

import java.util.Optional;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;
import com.elasticpath.settings.provider.SettingValueProvider;

/**
 * Implements {@link OrderHoldStrategy} to hold all orders.
 */
public class HoldAllOrdersOrderHoldStrategyImpl implements OrderHoldStrategy {

	/**
	 * The TORDERHOLD.DESCRIPTION to use for why the orders held by this strategy are being put on hold.
	 */
	public static final String ALL_ORDERS_ARE_CONFIGURED_FOR_HOLD_PROCESSING = "All orders are configured for hold processing";

	private BeanFactory beanFactory;
	private SettingValueProvider<Boolean> holdStrategyProvider;
	private SettingValueProvider<String> holdPermissionProvider;

	@Override
	public Optional<OrderHold> evaluate(final PreCaptureCheckoutActionContext context) {

		if (!isOnHold(context)) {
			return Optional.empty();
		}

		final OrderHold orderHold = createOrderHold(context);

		return Optional.of(orderHold);
	}

	/**
	 * Determines should or should not on hold order for current store.
	 *
	 * @param context the context
	 * @return true if configured as OnHold.
	 */
	private Boolean isOnHold(final PreCaptureCheckoutActionContext context) {
		return getHoldStrategyProvider().get(context.getShopper().getStoreCode());
	}

	/**
	 * Determines the permission to resolve on hold for current store.
	 *
	 * @param context the context.
	 * @return the permission string.
	 */
	private String getPermission(final PreCaptureCheckoutActionContext context) {
		return getHoldPermissionProvider().get(context.getShopper().getStoreCode());
	}

	/**
	 * Creates the order hold object with given context.
	 *
	 * @param context the context.
	 * @return the created order hold object.
	 */
	private OrderHold createOrderHold(final PreCaptureCheckoutActionContext context) {
		final OrderHold orderHold = beanFactory.getPrototypeBean(ContextIdNames.ORDER_HOLD, OrderHold.class);

		orderHold.setStatus(OrderHoldStatus.ACTIVE);
		orderHold.setHoldDescription(ALL_ORDERS_ARE_CONFIGURED_FOR_HOLD_PROCESSING);
		orderHold.setPermission(getPermission(context));

		return orderHold;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public SettingValueProvider<String> getHoldPermissionProvider() {
		return holdPermissionProvider;
	}

	public void setHoldPermissionProvider(final SettingValueProvider<String> holdPermissionProvider) {
		this.holdPermissionProvider = holdPermissionProvider;
	}

	public SettingValueProvider<Boolean> getHoldStrategyProvider() {
		return holdStrategyProvider;
	}

	public void setHoldStrategyProvider(final SettingValueProvider<Boolean> holdStrategyProvider) {
		this.holdStrategyProvider = holdStrategyProvider;
	}
}
