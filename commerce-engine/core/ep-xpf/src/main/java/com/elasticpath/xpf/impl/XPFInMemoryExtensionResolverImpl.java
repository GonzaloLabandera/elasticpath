/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.pf4j.ExtensionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.xpf.XPFConfigurationLoader;
import com.elasticpath.xpf.XPFExtensionDefaultSelectorModeEnum;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.XPFExtensionResolver;
import com.elasticpath.xpf.XPFExtensionSelector;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPoint;
import com.elasticpath.xpf.dto.ExtensionPointConfigurationDTO;

/**
 * Determines which extensions are assigned for each extension point and selector.
 */
@SuppressWarnings({"PMD.GodClass"})
public class XPFInMemoryExtensionResolverImpl implements XPFExtensionResolver {

	private XPFPluginManager pluginManager;

	private XPFConfigurationLoader configurationLoader;

	private final ListMultimap<XPFExtensionPointEnum, ExtensionPointConfigurationDTO> extensionPointMap =
			Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

	private static final Logger LOGGER = LoggerFactory.getLogger(XPFInMemoryExtensionResolverImpl.class);

	/**
	 * Build extension points configuration at startup.
	 */
	public void init() {
		Arrays.stream(XPFExtensionPointEnum.values())
				.forEach(this::createAndAssignExtensionConfiguration);
	}

	@Override
	public <EXTENSIONPOINT extends ExtensionPoint> List<EXTENSIONPOINT> resolveExtensionPoints(final List<EXTENSIONPOINT> extensions,
																							   final XPFExtensionPointEnum extensionPointEnum,
																							   final XPFExtensionSelector selectionContext) {
		List<Pair<Integer, EXTENSIONPOINT>> filteredExtensionClasses = new ArrayList<>();

		extensions.forEach(extension ->
				getMatchingConfiguration(((XPFExtensionPoint) extension).getExtensionGuid(), extensionPointEnum, selectionContext)
						.ifPresent(conf -> filteredExtensionClasses.add(new ImmutablePair<>(conf.getPriority(), extension))));

		return filteredExtensionClasses.stream()
				.sorted(Comparator.comparing(Pair::getLeft))
				.map(Pair::getRight)
				.collect(Collectors.toList());
	}

	@Override
	public void assignPluginExtensions(final String pluginId) {
		final Map<XPFExtensionPointEnum, List<ExtensionPointConfigurationDTO>> pluginConfigurations =
				configurationLoader.getExtensionPointConfigurationMap()
						.asMap()
						.entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey,
								entry -> entry.getValue().stream().filter(value -> Objects.equals(value.getPluginId(), pluginId))
										.collect(Collectors.toList())));

		pluginConfigurations.forEach(this::addToExtensionPointMap);
	}

	private void createAndAssignExtensionConfiguration(final XPFExtensionPointEnum extensionPointEnum) {
		final List<ExtensionPointConfigurationDTO> configurations = configurationLoader.getExtensionPointConfigurationMap().get(extensionPointEnum);
		configurations.forEach(pluginManager::ensureExtensionCreated);
		configurations.forEach(configuration -> extensionPointMap.put(extensionPointEnum, configuration));
		configurations.forEach(extensionPointConfiguration -> LOGGER.debug("Extension class {} assigned to {} with priority {}.",
				extensionPointConfiguration.getExtensionClassName(),
				extensionPointEnum.getName(), extensionPointConfiguration.getPriority()));
	}

	@Override
	public List<String> getAllAvailableExtensionClassNames(final XPFExtensionPointEnum extensionPoint) {
		return pluginManager.getExtensionClasses(extensionPoint.getExtensionPointInterface()).stream()
				.map(Class::getName)
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getAssignedExtensionClassNames(final XPFExtensionPointEnum extensionPoint) {
		return new ArrayList<>(extensionPointMap.get(extensionPoint)).stream()
				.map(ExtensionPointConfigurationDTO::getExtensionClassName)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public List<ExtensionPointConfigurationDTO> getAssignedExtensionConfigurations(final Class<?> extensionClass) {
		return getExtensionPointEnum(extensionClass).stream()
				.map(extensionPoint -> new ArrayList<>(extensionPointMap.get(extensionPoint)))
				.flatMap(Collection::stream)
				.filter(config -> Objects.equals(config.getExtensionClassName(), extensionClass.getName()))
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getAssignedExtensionClassNames(final XPFExtensionPointEnum extensionPoint, final String pluginId) {
		return new ArrayList<>(extensionPointMap.get(extensionPoint))
				.stream()
				.filter(configuration -> Objects.equals(pluginId, configuration.getPluginId()))
				.map(ExtensionPointConfigurationDTO::getExtensionClassName)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	public void assignExtensionToSelector(final String extensionClassName, final String pluginId, final XPFExtensionPointEnum extensionPoint,
										  final XPFExtensionSelector selector, final int priority) {
		final ExtensionPointConfigurationDTO newExtensionPointConfiguration = new ExtensionPointConfigurationBuilder()
				.setExtensionGuid(UUID.randomUUID().toString())
				.setExtensionClassName(extensionClassName)
				.setDefaultSelectorMode(XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE)
				.setSelector(selector)
				.setPriority(priority)
				.setPluginId(pluginId)
				.build();
		addToExtensionPointMap(extensionPoint, Collections.singletonList(newExtensionPointConfiguration));
	}

	private void addToExtensionPointMap(final XPFExtensionPointEnum extensionPoint,
										final List<ExtensionPointConfigurationDTO> extensionPointConfigurations) {
		extensionPointConfigurations.forEach(pluginManager::ensureExtensionCreated);
		extensionPointConfigurations.forEach(extensionPointConfiguration -> extensionPointMap.put(extensionPoint, extensionPointConfiguration));
		extensionPointConfigurations.forEach(extensionPointConfiguration ->
				LOGGER.debug("Extension class {} assigned to {} with priority {}.", extensionPointConfiguration.getExtensionClassName(),
						extensionPoint.getName(), extensionPointConfiguration.getPriority()));

	}

	@Override
	public void removeExtensionFromSelector(final String extensionClassName, final String pluginId, final XPFExtensionPointEnum extensionPoint,
											final XPFExtensionSelector selector) {
		if (selector.getClass().isAssignableFrom(XPFExtensionSelectorAny.class)) { // remove all assignments
			extensionPointMap.get(extensionPoint).removeIf(ext ->
					Objects.equals(ext.getExtensionClassName(), extensionClassName)
							&& Objects.equals(ext.getPluginId(), pluginId));
		} else { // update selectors so the config will not match the given selector
			new ArrayList<>(extensionPointMap.get(extensionPoint))
					.stream()
					.filter(ext -> Objects.equals(ext.getExtensionClassName(), extensionClassName)
							&& Objects.equals(ext.getPluginId(), pluginId)
							&& selectorMatches(ext, selector))
					.forEach(ext -> {
						if (ext.getDefaultSelectorMode() == XPFExtensionDefaultSelectorModeEnum.DEFAULT_ALL) {
							ext.getSelectors().add(selector);
						} else if (ext.getDefaultSelectorMode() == XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE) {
							ext.getSelectors().removeIf(sel -> sel.matches(selector));
						}
					});
		}

		LOGGER.debug("Extension class {} unassigned from {}.", extensionClassName, extensionPoint.getName());
	}

	@Override
	public void updatePluginId(final String oldId, final String newId) {
		extensionPointMap.entries().stream()
				.map(Map.Entry::getValue)
				.filter(configuration -> Objects.equals(oldId, configuration.getPluginId()))
				.peek(configuration -> configuration.setPluginId(newId))
				.forEach(configuration -> pluginManager.ensureExtensionCreated(configuration));

		LOGGER.debug("XPF plugin {} updated to {}.", oldId, newId);
	}

	/**
	 * Retrieve the matching extension point configuration for the passed extension and selection context.
	 *
	 * @param guid               the extension GUID
	 * @param extensionPointEnum the extension point enum
	 * @param selectionContext   the selection context
	 * @return the extension point configuration
	 */
	protected Optional<ExtensionPointConfigurationDTO> getMatchingConfiguration(final String guid,
																				final XPFExtensionPointEnum extensionPointEnum,
																				final XPFExtensionSelector selectionContext) {
		return new ArrayList<>(extensionPointMap.get(extensionPointEnum))
				.stream()
				.filter(extension -> Objects.equals(extension.getExtensionGuid(), guid) && selectorMatches(extension, selectionContext))
				.findFirst();
	}

	private boolean selectorMatches(final ExtensionPointConfigurationDTO configuration, final XPFExtensionSelector selector) {
		final List<XPFExtensionSelector> configuredSelectors = configuration.getSelectors();
		if (configuration.getDefaultSelectorMode() == XPFExtensionDefaultSelectorModeEnum.DEFAULT_ALL) {
			// matching selectors DISABLE
			return configuredSelectors.stream().noneMatch(configSelector -> configSelector.matches(selector));
		} else if (configuration.getDefaultSelectorMode() == XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE) {
			//matching selectors ENABLE
			return configuredSelectors.stream().anyMatch(configSelector -> configSelector.matches(selector));
		}
		return false; // should never hit
	}

	private List<XPFExtensionPointEnum> getExtensionPointEnum(final Class<?> extensionClass) {
		return Stream.of(XPFExtensionPointEnum.values())
				.filter(extensionPointEnum -> extensionPointEnum.getExtensionPointInterface().isAssignableFrom(extensionClass))
				.collect(Collectors.toList());
	}

	protected ListMultimap<XPFExtensionPointEnum, ExtensionPointConfigurationDTO> getExtensionPointMap() {
		return extensionPointMap;
	}

	public void setPluginManager(final XPFPluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	protected XPFPluginManager getPluginManager() {
		return pluginManager;
	}

	public void setConfigurationLoader(final XPFConfigurationLoader configurationLoader) {
		this.configurationLoader = configurationLoader;
	}
}
