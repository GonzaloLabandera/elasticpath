/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.provider;

import java.io.Serializable;
import java.util.List;

/**
 * A DTO object of a list of payment provider plugins.
 */
public class PaymentProviderPluginsDTO implements Serializable {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;


	private List<PaymentProviderPluginDTO> paymentProviderPluginsDTOs;


	/**
	 * Gets payment provider plugin DTOs.
	 *
	 * @return the payment provider plugin DTOs.
	 */
	public List<PaymentProviderPluginDTO> getPaymentProviderPluginDTOs() {
		return paymentProviderPluginsDTOs;
	}

	/**
	 * Sets payment provider plugin DTOs.
	 *
	 * @param paymentProviderPluginsDTOs the payment provider plugin DTOs.
	 */
	public void setPaymentProviderPluginDTOs(final List<PaymentProviderPluginDTO> paymentProviderPluginsDTOs) {
		this.paymentProviderPluginsDTOs = paymentProviderPluginsDTOs;
	}

}
