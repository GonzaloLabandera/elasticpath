/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.xpf.XPFPluginFactory;
import com.elasticpath.xpf.XPFPluginFactoryMBean;
import com.elasticpath.xpf.exception.InvalidPluginException;

/**
 * MBean wrapper for the plugin factory.
 */
public class XPFPluginFactoryMBeanWrapperImpl implements XPFPluginFactoryMBean {
	private static final Logger LOGGER = LogManager.getLogger(XPFPluginFactoryMBeanWrapperImpl.class);

	private XPFPluginFactory xpfPluginFactory;

	/**
	 * At startup it should register itself as an MBean.
	 */
	public void init() {
		try {
			final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			final ObjectName beanName = new ObjectName("com.elasticpath.xpf:name=XPFPluginFactory");
			final StandardMBean mBean = new StandardMBean(this, XPFPluginFactoryMBean.class);
			server.registerMBean(mBean, beanName);
		} catch (InstanceAlreadyExistsException exception) {
			LOGGER.warn("Unable to register XPFPluginFactory MBean because it already exists.");
		} catch (MBeanRegistrationException | NotCompliantMBeanException | MalformedObjectNameException exception) {
			LOGGER.error("Exception during creating MBean XPFPluginFactory", exception);
		}
	}

	@SuppressWarnings("PMD.PreserveStackTrace")
	@Override
	public void loadPlugin(final String jarUri) {
		try {
			xpfPluginFactory.loadPlugin(stringToURI(jarUri));
		} catch (InvalidPluginException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	@SuppressWarnings("PMD.PreserveStackTrace")
	@Override
	public void unloadPlugin(final String pluginId) {
		try {
			xpfPluginFactory.unloadPlugin(pluginId);
		} catch (InvalidPluginException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	@SuppressWarnings("PMD.PreserveStackTrace")
	@Override
	public void replacePlugin(final String oldPluginId, final String newJarUri) {
		try {
			xpfPluginFactory.replacePlugin(oldPluginId, stringToURI(newJarUri));
		} catch (InvalidPluginException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	private URI stringToURI(final String jarUri) {
		try {
			return new URI(jarUri);
		} catch (URISyntaxException e) {
			throw new InvalidPluginException("URI \"" + jarUri + "\" is invalid.", e);
		}
	}

	protected XPFPluginFactory getXpfPluginFactory() {
		return xpfPluginFactory;
	}

	public void setXpfPluginFactory(final XPFPluginFactory xpfPluginFactory) {
		this.xpfPluginFactory = xpfPluginFactory;
	}
}
