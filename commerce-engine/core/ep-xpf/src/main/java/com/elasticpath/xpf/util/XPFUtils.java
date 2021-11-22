/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.util;

import java.lang.reflect.Proxy;
import java.util.Optional;

import org.pf4j.ExtensionPoint;
import org.pf4j.PluginWrapper;

import com.elasticpath.xpf.impl.XPFPluginManager;
import com.elasticpath.xpf.impl.XPFTimingInvocationHandler;

/**
 * A util class to operate with extensions.
 */
public final class XPFUtils {

	private XPFUtils() {
	}

	/**
	 * Gets plugin id for given extension.
	 *
	 * @param pluginManager the plugin manager
	 * @param extension     the extension class
	 * @return plugin id.
	 */
	public static String getPluginIdForExtension(final XPFPluginManager pluginManager, final Class<? extends ExtensionPoint> extension) {

		return Optional.ofNullable(pluginManager.whichPlugin(extension)).map(PluginWrapper::getPluginId).orElse(null);
	}

	/**
	 * Helper method to obtain class of the proxied extension point.
	 *
	 * @param extensionPoint the extension point
	 * @param <EXTENSIONPOINT> The extension point type
	 *
	 * @return class of the proxied extension point.
	 */
	@SuppressWarnings("unchecked")
	public static <EXTENSIONPOINT extends ExtensionPoint> Class<EXTENSIONPOINT> getProxiedExtensionClass(final EXTENSIONPOINT extensionPoint) {
		return (Class<EXTENSIONPOINT>) ((XPFTimingInvocationHandler<EXTENSIONPOINT>) Proxy.getInvocationHandler(extensionPoint)).getProxiedClass();
	}
}
