/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.provider;

import com.elasticpath.provider.payment.service.provider.impl.ExternalPluginLoaderImpl;

/**
 * Extension of {@link ExternalPluginLoaderImpl} using test-classes folder as base plugins location.
 */
public class ExternalPluginLoaderExtensionForIntegrationTesting extends ExternalPluginLoaderImpl {

	@Override
	protected String getPluginsLocation(final Class<?> pluginInterface) {
		return getSourceLocation(getClass()).toExternalForm() + "plugins";
	}

}
