/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.payment.provider;

import java.util.List;

import com.elasticpath.plugin.payment.provider.AbstractPaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;

/**
 * Dummy plugin for Spring test.
 */
public class DummyPaymentProviderPluginImpl extends AbstractPaymentProviderPlugin {

	@Override
	public String getPaymentVendorId() {
		return null;
	}

	@Override
	public String getPaymentMethodId() {
		return null;
	}

	@Override
	public List<PluginConfigurationKey> getConfigurationKeys() {
		return null;
	}
}
