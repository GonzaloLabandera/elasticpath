/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider;

import java.util.List;

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
