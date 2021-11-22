/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.lang.management.ManagementFactory;
import java.util.List;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.XPFExtensionResolver;
import com.elasticpath.xpf.XPFExtensionResolverMBean;
import com.elasticpath.xpf.XPFExtensionSelector;

/**
 * MBean wrapper for the extensions resolver.
 */
public class XPFExtensionResolverMBeanWrapperImpl implements XPFExtensionResolverMBean {
	private static final Logger LOGGER = LogManager.getLogger(XPFExtensionResolverMBeanWrapperImpl.class);

	private XPFExtensionResolver extensionsResolver;

	/**
	 * Register MBean at startup.
	 */
	public void init() {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = new ObjectName("com.elasticpath.xpf:name=XPFExtensionResolver");
			final StandardMBean smbean = new StandardMBean(this, XPFExtensionResolverMBean.class);
			mbs.registerMBean(smbean, name);
		} catch (InstanceAlreadyExistsException exception) {
			LOGGER.warn("Unable to register XPFExtensionResolver MBean because it already exists.");
		} catch (MBeanRegistrationException | NotCompliantMBeanException | MalformedObjectNameException exception) {
			LOGGER.error("Exception during creating MBean", exception);
		}
	}

	@Override
	public List<String> getAllAvailableExtensionClassNames(final String extensionPoint) {
		XPFExtensionPointEnum extensionPointEnum = XPFExtensionPointEnum.valueOf(extensionPoint);
		return extensionsResolver.getAllAvailableExtensionClassNames(extensionPointEnum);
	}

	@Override
	public List<String> getAssignedExtensionClassNames(final String extensionPoint) {
		XPFExtensionPointEnum extensionPointEnum = XPFExtensionPointEnum.valueOf(extensionPoint);
		return extensionsResolver.getAssignedExtensionClassNames(extensionPointEnum);
	}

	@Override
	public void assignToStore(final String extensionClass, final String pluginId, final String extensionPoint, final String storeCode,
							  final int priority) {
		String resolvedPluginId = pluginId.isEmpty() ? null : pluginId;
		XPFExtensionPointEnum extensionPointEnum = XPFExtensionPointEnum.valueOf(extensionPoint);
		XPFExtensionSelector selector = generateSelectorForStoreCode(storeCode);
		extensionsResolver.assignExtensionToSelector(extensionClass, resolvedPluginId, extensionPointEnum, selector, priority);
	}

	@Override
	public void removeFromStore(final String extensionClass, final String pluginId, final String extensionPoint, final String storeCode) {
		String resolvedPluginId = pluginId.isEmpty() ? null : pluginId;
		XPFExtensionPointEnum extensionPointEnum = XPFExtensionPointEnum.valueOf(extensionPoint);
		XPFExtensionSelector selector = generateSelectorForStoreCode(storeCode);
		extensionsResolver.removeExtensionFromSelector(extensionClass, resolvedPluginId, extensionPointEnum, selector);
	}

	private XPFExtensionSelector generateSelectorForStoreCode(final String storeCode) {
		if (StringUtils.isNotEmpty(storeCode)) {
			return new XPFExtensionSelectorByStoreCode(storeCode);
		} else {
			return new XPFExtensionSelectorAny();
		}
	}

	protected XPFExtensionResolver getExtensionsResolver() {
		return extensionsResolver;
	}

	public void setExtensionsResolver(final XPFExtensionResolver extensionsResolver) {
		this.extensionsResolver = extensionsResolver;
	}

}
