/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.pf4j.ExtensionPoint;
import org.pf4j.PluginManager;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.XPFExtensionResolver;
import com.elasticpath.xpf.XPFExtensionSelector;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPoint;

/**
 * Fetches extension implementations for a given extension point.
 */
public class XPFExtensionLookupImpl implements XPFExtensionLookup {

	private PluginManager pluginManager;
	private XPFExtensionResolver extensionPointResolver;

	@Override
	public <EXTENSIONPOINT extends ExtensionPoint> EXTENSIONPOINT getSingleExtension(final Class<EXTENSIONPOINT> clazz,
																					 final XPFExtensionPointEnum extensionPoint,
																					 final XPFExtensionSelector selectionContext) {
		return getMultipleExtensions(clazz, extensionPoint, selectionContext).stream()
				.findFirst()
				.orElseThrow(() -> new EpServiceException("No valid extension found for extension point " + clazz.getSimpleName()));
	}

	@Override
	public <EXTENSIONPOINT extends ExtensionPoint> List<EXTENSIONPOINT> getMultipleExtensions(
			final Class<EXTENSIONPOINT> clazz,
			final XPFExtensionPointEnum extensionPoint,
			final XPFExtensionSelector selectionContext) {
		List<EXTENSIONPOINT> extensions = pluginManager.getExtensions(clazz);
		List<EXTENSIONPOINT> resolvedExtensions =
				extensionPointResolver.resolveExtensionPoints(extensions, extensionPoint, selectionContext);
		return wrapExtensionsInTimingProxy(resolvedExtensions);

	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <EXTENSIONPOINT extends ExtensionPoint> List<EXTENSIONPOINT> wrapExtensionsInTimingProxy(final List<EXTENSIONPOINT> extensions) {
		return extensions.stream()
				.map(ext -> (EXTENSIONPOINT) Proxy.newProxyInstance(ext.getClass().getClassLoader(),
						getInterfaces(ext),
						new XPFTimingInvocationHandler(ext)))
				.collect(Collectors.toList());
	}

	/**
	 * Extensions extend XPFExtensionPointImpl which inturn implements XPFExtensionPoint interface. Along with this, each extension implements
	 * corresponding extension point interface. The getInterfaces() method only returns the interfaces that the class implements directly and does
	 * not return XPFExtensionPoint interface. Since proxies are to be created for all interfaces in the hierarchy of the extension, this method
	 * adds XPFExtensionPoint.class to the list of interfaces for proxy generation.
	 *
	 * @param extensionpoint The extension point
	 * @param <EXTENSIONPOINT> The extension point type
	 * @return list of interfaces imlemented by the given extensionPoint along with XPFExtensionPoint.class
	 */
	protected <EXTENSIONPOINT extends ExtensionPoint> Class<?>[] getInterfaces(final EXTENSIONPOINT extensionpoint) {
		int extInterfaceArrLength = extensionpoint.getClass().getInterfaces().length;
		Class<?>[] interfaces = Arrays.copyOf(extensionpoint.getClass().getInterfaces(), extInterfaceArrLength + 1);
		interfaces[interfaces.length - 1] = XPFExtensionPoint.class;
		return interfaces;
	}

	public void setPluginManager(final PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public void setExtensionPointResolver(final XPFExtensionResolver extensionPointResolver) {
		this.extensionPointResolver = extensionPointResolver;
	}
}
