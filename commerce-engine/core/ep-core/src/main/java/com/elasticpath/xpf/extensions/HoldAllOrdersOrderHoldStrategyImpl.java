/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.extensions;

import java.util.Optional;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.settings.provider.SettingValueProvider;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFOrderHoldStrategyContext;
import com.elasticpath.xpf.connectivity.entity.XPFOrderHold;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.OrderHoldStrategy;

/**
 * Extension for holding all orders based on value of a setting.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.ORDER_HOLD_STRATEGY, priority = 1050)
public class HoldAllOrdersOrderHoldStrategyImpl extends XPFExtensionPointImpl implements OrderHoldStrategy {

	/**
	 * The TORDERHOLD.DESCRIPTION to use for why the orders held by this strategy are being put on hold.
	 */
	public static final String ALL_ORDERS_ARE_CONFIGURED_FOR_HOLD_PROCESSING = "All orders are configured for hold processing";

	@Autowired
	private BeanFactory beanFactory;

	@Override
	public Optional<XPFOrderHold> evaluate(final XPFOrderHoldStrategyContext context) {

		if (!isOnHold(context)) {
			return Optional.empty();
		}

		return Optional.of(new XPFOrderHold(getPermission(context), ALL_ORDERS_ARE_CONFIGURED_FOR_HOLD_PROCESSING));
	}

	/**
	 * Determines should or should not on hold order for current store.
	 *
	 * @param context the context
	 * @return true if configured as OnHold.
	 */
	private Boolean isOnHold(final XPFOrderHoldStrategyContext context) {
		return getHoldStrategyProvider().get(context.getShoppingCart().getShopper().getStore().getCode());
	}

	/**
	 * Determines the permission to resolve on hold for current store.
	 *
	 * @param context the context.
	 * @return the permission string.
	 */
	private String getPermission(final XPFOrderHoldStrategyContext context) {
		return getHoldPermissionProvider().get(context.getShoppingCart().getShopper().getStore().getCode());
	}

	@SuppressWarnings("unchecked")
	protected SettingValueProvider<Boolean> getHoldStrategyProvider() {
		return beanFactory.getSingletonBean("holdStrategyProvider", SettingValueProvider.class);
	}

	@SuppressWarnings("unchecked")
	protected SettingValueProvider<String> getHoldPermissionProvider() {
		return beanFactory.getSingletonBean("holdPermissionProvider", SettingValueProvider.class);
	}
}
