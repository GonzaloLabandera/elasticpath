/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities;

import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.OrderContext;

/**
 * The Payment plugin request.
 */
public interface PaymentCapabilityRequest {

	/**
	 * Gets order context.
	 *
	 * @return the order context
	 */
	OrderContext getOrderContext();

	/**
	 * Sets order context.
	 *
	 * @param orderContext the order context
	 */
	void setOrderContext(OrderContext orderContext);

	/**
	 * Gets payment instrument data for payment operation.
	 *
	 * @return the payment instrument data
	 */
	Map<String, String> getPaymentInstrumentData();

	/**
	 * Sets payment instrument data for payment operation.
	 *
	 * @param paymentInstrumentData the payment instrument data
	 */
	void setPaymentInstrumentData(Map<String, String> paymentInstrumentData);

	/**
	 * Gets reserved request data.
	 *
	 * @return the reserved request data
	 */
	Map<String, String> getCustomRequestData();

	/**
	 * Sets reserved request data.
	 *
	 * @param customRequestData the reserved request data
	 */
	void setCustomRequestData(Map<String, String> customRequestData);

	/**
	 * Gets plugin config data.
	 *
	 * @return the plugin config data
	 */
	Map<String, String> getPluginConfigData();

	/**
	 * Sets plugin config data.
	 *
	 * @param pluginConfigData the plugin config data
	 */
	void setPluginConfigData(Map<String, String> pluginConfigData);

}
