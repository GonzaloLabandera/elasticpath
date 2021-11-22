/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.connectivity.context.XPFOrderDataPopulatorContext;
import com.elasticpath.xpf.connectivity.extensionpoint.OrderDataPopulator;
import com.elasticpath.xpf.converters.ShoppingCartConverter;
import com.elasticpath.xpf.impl.XPFExtensionSelectorByStoreCode;

//CHECKSTYLE:OFF

/**
 * <p>Checkout Action which populates the {@link com.elasticpath.domain.order.Order}'s {@link com.elasticpath.domain.order.OrderData} with data
 * from the {@link PreCaptureCheckoutActionContext}.</p>
 *
 * <p>The values that are populated by this action are supplied by com.elasticpath.xpf.connectivity.order.OrderDataPopulator implementations
 * returned by XPFExtensionLookup</p>
 * </pre>
 */
//CHECKSTYLE:ON
public class PopulateOrderDataCheckoutAction implements ReversibleCheckoutAction {
	private XPFExtensionLookup xpfExtensionLookup;
	private ShoppingCartConverter xpfShoppingCartConverter;

	private static final Logger LOG = LogManager.getLogger(PopulateOrderDataCheckoutAction.class);

	@Override
	public void execute(final PreCaptureCheckoutActionContext context) throws EpSystemException {
		List<OrderDataPopulator> orderDataPopulators = getOrderDataPopulators(context);
		XPFOrderDataPopulatorContext orderDataPopulationContext = new XPFOrderDataPopulatorContext(
				xpfShoppingCartConverter.convert(context.getShoppingCart()));

		orderDataPopulators.forEach(orderDataPopulator -> {
			try {
				Map<String, String> orderDataMap = orderDataPopulator.collectOrderData(orderDataPopulationContext);
				orderDataMap.keySet().forEach(key ->
						context.getOrder().getModifierFields().putIfAbsent(key, orderDataMap.get(key)));
			} catch (Exception e) {
				LOG.error("Exception thrown by " + orderDataPopulator.getClass().getName() + ".  ", e);
			}
		});
	}

	@Override
	public void rollback(final PreCaptureCheckoutActionContext context) throws EpSystemException {
		Map<String, String> orderData = new HashMap<>(context.getOrder().getFieldValues());
		for (String dataKey : orderData.keySet()) {
			context.getOrder().removeFieldValue(dataKey);
		}
	}

	/**
	 * Creates the key that will be used to store the given input map element in the OrderData map.
	 *
	 * @param baseKey       the base key for the input map
	 * @param mapElementKey the name of the key that the element is stored in in the input map
	 * @return the key that will be used to store the element in the OrderData map
	 */
	protected String getSubMapKey(final String baseKey, final Object mapElementKey) {
		StringBuilder stringBuilder = new StringBuilder();
		if (StringUtils.isNotEmpty(baseKey)) {
			stringBuilder.append(baseKey);
			stringBuilder.append('.');
		}

		stringBuilder.append(mapElementKey);
		return stringBuilder.toString();
	}

	private List<OrderDataPopulator> getOrderDataPopulators(final PreCaptureCheckoutActionContext context) {
		XPFExtensionSelectorByStoreCode selector = new XPFExtensionSelectorByStoreCode(context.getShoppingCart().getStore().getCode());
		return xpfExtensionLookup.getMultipleExtensions(OrderDataPopulator.class,
				XPFExtensionPointEnum.ORDER_DATA_POPULATOR, selector);
	}

	public void setXpfExtensionLookup(final XPFExtensionLookup xpfExtensionLookup) {
		this.xpfExtensionLookup = xpfExtensionLookup;
	}

	public void setXpfShoppingCartConverter(final ShoppingCartConverter xpfShoppingCartConverter) {
		this.xpfShoppingCartConverter = xpfShoppingCartConverter;
	}
}
