/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.sf.ehcache.CacheManager;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.DefaultExtensionFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.xpf.XPFConfigurationLoader;
import com.elasticpath.xpf.connectivity.context.XPFPluginInitializationContext;
import com.elasticpath.xpf.connectivity.entity.XPFPluginSetting;
import com.elasticpath.xpf.connectivity.plugin.XPFExternalPlugin;
import com.elasticpath.xpf.converters.XPFPluginSettingConverter;
import com.elasticpath.xpf.dto.ExtensionPointConfigurationDTO;
import com.elasticpath.xpf.dto.XPFPluginConfigurationDTO;
import com.elasticpath.xpf.exception.InvalidExtensionException;
import com.elasticpath.xpf.exception.InvalidPluginException;

/**
 * Extended implementation of org.pf4j.DefaultPluginManager that wires other custom components in to the framework.
 */
public class XPFPluginManager extends DefaultPluginManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(XPFPluginManager.class);

	private final XPFSingletonExtensionFactory singletonExtensionFactory;
	private final BeanFactory beanFactory;
	private final XPFPluginSettingConverter settingConverter;

	/**
	 * Constructor.
	 *
	 * @param singletonExtensionFactory extension factory
	 * @param beanFactory               bean factory
	 * @param settingConverter          setting converter
	 * @param pluginsFolder             plugins folder
	 */
	public XPFPluginManager(final XPFSingletonExtensionFactory singletonExtensionFactory, final BeanFactory beanFactory,
							final XPFPluginSettingConverter settingConverter, final String pluginsFolder) {

		super(Paths.get(pluginsFolder));
		this.singletonExtensionFactory = singletonExtensionFactory;
		this.beanFactory = beanFactory;
		this.settingConverter = settingConverter;

		addFinder(singletonExtensionFactory, beanFactory);
	}

	@Override
	protected PluginWrapper loadPluginFromPath(final Path pluginPath) {
		PluginWrapper pluginWrapper = super.loadPluginFromPath(pluginPath);

		LOGGER.info("XPF plugin {} loaded.", pluginWrapper.getDescriptor().getPluginId());

		return pluginWrapper;
	}

	/**
	 * Start all active plugins.
	 */
	@Override
	public void startPlugins() {
		getResolvedPlugins().stream()
				.filter(pluginWrapper -> pluginWrapper.getPluginState() != PluginState.DISABLED
						&& pluginWrapper.getPluginState() != PluginState.STARTED)
				.forEach(this::setSettings);

		super.startPlugins();
		resolvedPlugins.forEach(resolvedPlugin -> logPluginStartEvent(resolvedPlugin.getPluginId(), resolvedPlugin.getPluginState()));
	}

	/**
	 * Start the specified plugin and its dependencies.
	 */
	@Override
	public PluginState startPlugin(final String pluginId) {
		final PluginWrapper plugin = getPlugin(pluginId);
		if (!tryEnablePlugin(pluginId, plugin)) {
			return PluginState.DISABLED;
		}

		if (plugin.getPluginState() != PluginState.STARTED) {
			setSettings(plugin);
		}

		PluginState result = super.startPlugin(pluginId);

		logPluginStartEvent(pluginId, result);

		return result;
	}

	/**
	 * Log plugin start event.
	 * @param pluginId the plugin Id
	 * @param pluginState the plugin state of plugin start operation
	 */
	protected void logPluginStartEvent(final String pluginId, final PluginState pluginState) {
		if (pluginState == PluginState.STARTED) {
			LOGGER.info("XPF plugin {} started.", pluginId);
		} else {
			LOGGER.warn("XPF plugin {} could not be started; current plugin state is {}.", pluginId, pluginState);
		}
	}

	@Override
	public PluginState stopPlugin(final String pluginId) {
		PluginState pluginState = super.stopPlugin(pluginId);

		logPluginStopEvent(pluginId, pluginState);
		return pluginState;
	}

	@Override
	public void stopPlugins() {
		// Plugins that are stopped get removed form startedPlugins list in super.stopPlugins() so create a copy for logging purposes.
		List<PluginWrapper> startedPluginsList = new ArrayList<>(startedPlugins);
		super.stopPlugins();
		startedPluginsList.forEach(startedPlugin -> logPluginStopEvent(startedPlugin.getPluginId(), startedPlugin.getPluginState()));
	}

	/**
	 * Log plugin stop event.
	 * @param pluginId the plugin Id
	 * @param pluginState the plugin state of plugin stop operation
	 */
	protected void logPluginStopEvent(final String pluginId, final PluginState pluginState) {
		if (pluginState == PluginState.STOPPED) {
			LOGGER.info("XPF plugin {} stopped.", pluginId);
		} else {
			LOGGER.warn("XPF plugin {} could not be stopped; current plugin state is {}.", pluginId, pluginState);
		}
	}

	@Override
	public boolean unloadPlugin(final String pluginId) {
		boolean isUnloaded = super.unloadPlugin(pluginId);

		if (isUnloaded) {
			LOGGER.info("XPF plugin {} unloaded.", pluginId);
		} else {
			LOGGER.warn("XPF plugin {} could not be unloaded.", pluginId);
		}

		return isUnloaded;
	}

	private boolean tryEnablePlugin(final String pluginId, final PluginWrapper plugin) {
		return plugin.getPluginState() != PluginState.DISABLED || enablePlugin(pluginId);
	}

	private void addFinder(final XPFSingletonExtensionFactory singletonExtensionFactory, final BeanFactory beanFactory) {
		final XPFExtensionFinder extensionFinder = new XPFExtensionFinder(this, beanFactory, singletonExtensionFactory);
		super.extensionFinder = extensionFinder;
		// It needs to update collection of extensions after loading external plugins.
		addPluginStateListener(extensionFinder);
	}

	private void setSettings(final PluginWrapper pluginWrapper) {
		validatePlugin(pluginWrapper);

		final CacheManager cacheManager = beanFactory.getSingletonBean("epCoreEhcacheManager", CacheManager.class);

		final Optional<XPFPluginConfigurationDTO> configuration = Optional.ofNullable(getConfigurationLoader()
				.getPluginConfigurationMap()
				.get(pluginWrapper.getPluginId()));

		Map<String, XPFPluginSetting> xpfPluginSettings = configuration
				.map(XPFPluginConfigurationDTO::getPluginSettings)
				.map(settings ->
						settings.stream()
								.map(settingConverter::convert)
								.collect(Collectors.toMap(XPFPluginSetting::getSettingKey, Function.identity(),
										(first, second) -> first, CaseInsensitiveMap::new)))
				.orElse(new CaseInsensitiveMap<>());

		((XPFExternalPlugin) pluginWrapper.getPlugin())
				.setContext(new XPFPluginInitializationContext(LoggerFactory.getLogger(pluginWrapper.getPlugin().getClass()),
						xpfPluginSettings,
						pluginWrapper.getPluginId(),
						cacheManager));
	}

	private void validatePlugin(final PluginWrapper pluginWrapper) {
		if (!(pluginWrapper.getPlugin() instanceof XPFExternalPlugin)) {
			final String errorMessage = String.format("Plugin with id %s is not instance of XPFExternalPlugin", pluginWrapper.getPluginId());
			LOGGER.error(errorMessage);

			throw new InvalidPluginException(errorMessage);
		}
	}

	/**
	 * Find the correct ClassLoader for the extension and load the class.
	 *
	 * @param extensionConfig extension configuration
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void ensureExtensionCreated(final ExtensionPointConfigurationDTO extensionConfig) {
		try {
			final Class extensionClass = getPluginClassLoader(extensionConfig.getPluginId()).loadClass(extensionConfig.getExtensionClassName());

			singletonExtensionFactory.initializeAndCacheExtension(extensionClass,
					extensionConfig.getExtensionGuid(),
					extensionConfig.getExtensionSettings());

		} catch (final Exception e) {
			throw new InvalidExtensionException("Exception during extension initialisation", e);
		}
	}

	/**
	 * Get the {@link ClassLoader} for plugin.
	 *
	 * @param pluginId the plugin id
	 * @return plugin class loader
	 */
	@Override
	public ClassLoader getPluginClassLoader(final String pluginId) {
		return StringUtils.isEmpty(pluginId)
				? getClass().getClassLoader()
				: super.getPluginClassLoader(pluginId);
	}

	/**
	 * Get Unfiltered Extension Classes.
	 *
	 * @param type the type
	 * @param <T>  the type
	 * @return the extension classes
	 */
	public <T> List<Class<? extends T>> getUnfilteredExtensionClasses(final Class<T> type) {
		return getExtensionClasses(new DefaultExtensionFinder(this).find(type));
	}

	/**
	 * Get Unfiltered Extension Classes.
	 *
	 * @param type     the type
	 * @param pluginId the plugin Id
	 * @param <T>      the type
	 * @return the extension classes
	 */
	public <T> List<Class<? extends T>> getUnfilteredExtensionClasses(final Class<T> type, final String pluginId) {
		return getExtensionClasses(new DefaultExtensionFinder(this).find(type, pluginId));
	}

	private XPFConfigurationLoader getConfigurationLoader() {
		return beanFactory.getSingletonBean("xpfConfigurationLoader", XPFConfigurationLoader.class);
	}

	protected XPFSingletonExtensionFactory getSingletonExtensionFactory() {
		return singletonExtensionFactory;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	protected XPFPluginSettingConverter getSettingConverter() {
		return settingConverter;
	}
}
