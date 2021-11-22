/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.pf4j.ExtensionPoint;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.XPFExtensionResolver;
import com.elasticpath.xpf.XPFPluginFactory;
import com.elasticpath.xpf.exception.InvalidExtensionException;
import com.elasticpath.xpf.exception.InvalidPluginException;
import com.elasticpath.xpf.exception.SchemaNotSupportedException;

/**
 * A factory for managing external plugins.
 */
public class XPFPluginFactoryImpl implements XPFPluginFactory {

	private BeanFactory beanFactory;

	/**
	 * Loads external plugins at startup.
	 */
	public void init() {
		final XPFPluginManager pluginManager = getPluginManager();

		pluginManager.loadPlugins();
		pluginManager.startPlugins();
	}

	@Override
	public void loadPlugin(final URI jarUri) {
		replacePlugin(null, jarUri);
	}

	@Override
	public void unloadPlugin(final String pluginId) {
		replacePlugin(pluginId, null);
	}

	@Override
	public void replacePlugin(final String pluginId, final URI newJarUri) {
		checkURI(newJarUri);

		//case when oldJarUri is present and newJarUri is present
		if (Objects.nonNull(pluginId) && Objects.nonNull(newJarUri)) {
			swapPlugins(pluginId, newJarUri);
		}

		//case when only newJarUri is present
		if (Objects.nonNull(newJarUri) && Objects.isNull(pluginId)) {
			loadAndStartPlugin(newJarUri);
		}

		//case when only oldJarUri is present
		if (Objects.nonNull(pluginId) && Objects.isNull(newJarUri)) {
			stopPlugin(pluginId);
		}
	}

	private void swapPlugins(final String pluginId, final URI newJarUri) {
		XPFExtensionResolver extensionPointResolver = getXpfExtensionsResolver();

		final String newPluginId = loadAndStartPlugin(newJarUri);
		try {
			validateNewExtensions(newPluginId, pluginId);
			validateIrreplaceableExtensions(newPluginId, pluginId);

			// reassign extensions to the new plugin id
			extensionPointResolver.updatePluginId(pluginId, newPluginId);

			//stop the old plugin
			stopAndUnloadPlugin(pluginId);
		} catch (InvalidExtensionException e) {
			XPFPluginManager pluginManager = getPluginManager();

			pluginManager.stopPlugin(newPluginId);
			pluginManager.unloadPlugin(newPluginId);
			throw e;
		}

	}

	@SuppressWarnings("PMD.PreserveStackTrace")
	private String loadAndStartPlugin(final URI newJarUri) {
		XPFPluginManager pluginManager = getPluginManager();
		final String pluginId = pluginManager.loadPlugin(Paths.get(newJarUri));

		try {
			pluginManager.startPlugin(pluginId);

			getXpfExtensionsResolver().assignPluginExtensions(pluginId);
			return pluginId;

		} catch (Exception e) {
			try {
				pluginManager.unloadPlugin(pluginId);
			} catch (Exception exception) {
				throw new InvalidPluginException("It's impossible to unload invalid plugin", e);
			}
			throw e;
		}
	}

	/**
	 * Unassign extensions, stops and unload the plugin.
	 */
	private void stopPlugin(final String pluginId) {
		XPFPluginManager pluginManager = getPluginManager();

		unassignAllExtensions(pluginId);

		pluginManager.stopPlugin(pluginId);
		pluginManager.unloadPlugin(pluginId);
	}

	/**
	 * Stops and unloads the plugin.
	 */
	private void stopAndUnloadPlugin(final String pluginId) {
		XPFPluginManager pluginManager = getPluginManager();

		pluginManager.stopPlugin(pluginId);
		pluginManager.unloadPlugin(pluginId);
	}

	private void unassignAllExtensions(final String pluginId) {
		XPFPluginManager pluginManager = getPluginManager();

		pluginManager.getExtensionClasses(pluginId).forEach(clazz ->
				getXpfExtensionsResolver().removeExtensionFromSelector(clazz.getName(),
						pluginId,
						getExtensionPointNameByClass(extractExtensionPointClass(clazz)),
						new XPFExtensionSelectorAny()));
	}

	/**
	 * Validates that all new extensions can assign to extension points from the old plugin.
	 */
	private void validateNewExtensions(final String newPluginId, final String oldPluginId) {
		XPFPluginManager pluginManager = getPluginManager();

		final List<Class<? extends ExtensionPoint>> oldExtensionPoints = pluginManager.getExtensionClasses(oldPluginId)
				.stream()
				.map(this::extractExtensionPointClass)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());

		final List<Class<?>> unassignedExtensions = pluginManager.getExtensionClasses(newPluginId)
				.stream()
				.filter(extension -> isExtensionAssignable(oldExtensionPoints, extension))
				.collect(Collectors.toList());

		if (!unassignedExtensions.isEmpty()) {
			throw new InvalidExtensionException(unassignedExtensions.stream()
					.map(Class::getName)
					.collect(Collectors.joining(" , ", "Not found extension points for ", " in old plugin")));
		}
	}

	/**
	 * Validates that all assigned extensions from the old plugin exist in the new plugin.
	 */
	private void validateIrreplaceableExtensions(final String newPluginId, final String oldPluginId) {
		XPFPluginManager pluginManager = getPluginManager();
		XPFExtensionResolver extensionPointResolver = getXpfExtensionsResolver();

		final List<String> unassignedNewExtensions = pluginManager.getExtensionClasses(newPluginId)
				.stream()
				.map(Class::getName)
				.collect(Collectors.toList());

		final List<String> assignedOldExtensions = Arrays.stream(XPFExtensionPointEnum.values())
				.map(extensionPoint -> extensionPointResolver.getAssignedExtensionClassNames(extensionPoint, oldPluginId))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		final List<String> irreplaceableExtensions = assignedOldExtensions.stream()
				.filter(extension -> !unassignedNewExtensions.contains(extension))
				.collect(Collectors.toList());

		if (!irreplaceableExtensions.isEmpty()) {
			throw new InvalidExtensionException(irreplaceableExtensions.stream()
					.collect(Collectors.joining(" , ", "Extension ", " was not found in the replacement plugin")));
		}
	}

	private boolean isExtensionAssignable(final List<Class<? extends ExtensionPoint>> oldExtensionPoints, final Class<?> extension) {
		return oldExtensionPoints.stream().noneMatch(extensionPoint -> extensionPoint.isAssignableFrom(extension));
	}

	private XPFExtensionPointEnum getExtensionPointNameByClass(final Class<? extends ExtensionPoint> extensionPoint) {
		return Arrays.stream(XPFExtensionPointEnum.values())
				.filter(extensionPointEnum -> extensionPoint == extensionPointEnum.getExtensionPointInterface())
				.findAny()
				.orElseThrow(() -> new InvalidExtensionException("Not found extension point for " + extensionPoint));
	}

	//extension can implement only one extension point

	private Class<? extends ExtensionPoint> extractExtensionPointClass(final Class<?> extensionClass) {
		return Arrays.stream(XPFExtensionPointEnum.values())
				.map(XPFExtensionPointEnum::getExtensionPointInterface)
				.filter(extensionPoint -> extensionPoint.isAssignableFrom(extensionClass))
				.findAny()
				.orElse(null);
	}

	private void checkURI(final URI jarUri) {
		if (Optional.ofNullable(jarUri).map(URI::getScheme).map(schema -> !"file".equals(schema)).orElse(false)) {
			throw new SchemaNotSupportedException(jarUri.getScheme() + " is not supported. URI \"" + jarUri + "\" must start with \"file:\".");
		}
	}

	private XPFExtensionResolver getXpfExtensionsResolver() {
		return beanFactory.getSingletonBean("xpfExtensionsResolver", XPFExtensionResolver.class);
	}

	private XPFPluginManager getPluginManager() {
		return beanFactory.getSingletonBean("pluginManager", XPFPluginManager.class);
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
