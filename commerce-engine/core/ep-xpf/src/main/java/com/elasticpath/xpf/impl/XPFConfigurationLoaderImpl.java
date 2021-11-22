/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import static com.google.common.collect.Multimaps.toMultimap;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.pf4j.ExtensionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.xpf.XPFConfigurationLoader;
import com.elasticpath.xpf.XPFExtensionDefaultSelectorModeEnum;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.XPFExtensionSelector;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.dto.ExtensionPointConfigurationDTO;
import com.elasticpath.xpf.dto.PluginSettingDTO;
import com.elasticpath.xpf.dto.PluginSettingValueDTO;
import com.elasticpath.xpf.dto.SettingCollectionTypeDTO;
import com.elasticpath.xpf.dto.SettingDataTypeDTO;
import com.elasticpath.xpf.dto.XPFPluginConfigurationDTO;
import com.elasticpath.xpf.json.ExtensionConfigurations;
import com.elasticpath.xpf.json.PluginConfiguration;
import com.elasticpath.xpf.json.PluginConfigurations;
import com.elasticpath.xpf.json.Selector;
import com.elasticpath.xpf.json.Setting;
import com.elasticpath.xpf.json.XPFExtensionPointConfiguration;
import com.elasticpath.xpf.util.XPFHashMap;
import com.elasticpath.xpf.util.XPFUtils;

/**
 * Loads configurations of extensions and plugins.
 */
public class XPFConfigurationLoaderImpl implements XPFConfigurationLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(XPFConfigurationLoaderImpl.class);
	private static final String EXTENSIONS_CONFIGURATION_FILE_NAME = "extensions.json";
	private static final String PLUGINS_CONFIGURATION_FILE_NAME = "plugins.json";

	private final ObjectMapper objectMapper;
	private final XPFPluginManager pluginManager;
	private final XPFSettingValueParser xpfSettingValueParser;

	private final ListMultimap<XPFExtensionPointEnum, XPFExtensionPointConfiguration> extensions;
	private final Map<String, XPFPluginConfigurationDTO> plugins;

	/**
	 * Constructor.
	 *
	 * @param objectMapper  object mapper for conversion
	 * @param pluginManager plugin manager
	 * @param configFolder  config folder
	 * @param xpfSettingValueParser setting value parser
	 */
	public XPFConfigurationLoaderImpl(final ObjectMapper objectMapper, final XPFPluginManager pluginManager, final String configFolder,
									  final XPFSettingValueParser xpfSettingValueParser) {
		this.objectMapper = objectMapper;
		this.pluginManager = pluginManager;
		this.xpfSettingValueParser = xpfSettingValueParser;
		plugins = loadPluginConfiguration(configFolder);
		extensions = loadExtensionAssignmentConfiguration(configFolder);
	}

	@Override
	public ListMultimap<XPFExtensionPointEnum, ExtensionPointConfigurationDTO> getExtensionPointConfigurationMap() {
		final ListMultimap<XPFExtensionPointEnum, ExtensionPointConfigurationDTO> multimap = ArrayListMultimap.create();

		Arrays.stream(XPFExtensionPointEnum.values())
				.forEach(extensionEnum ->
						multimap.putAll(extensionEnum,
								createConfiguration(pluginManager.getUnfilteredExtensionClasses(extensionEnum.getExtensionPointInterface()),
										extensionEnum)));

		return multimap;
	}

	@Override
	public Map<String, XPFPluginConfigurationDTO> getPluginConfigurationMap() {
		return plugins;
	}

	private List<ExtensionPointConfigurationDTO> createConfiguration(final List<? extends Class<? extends ExtensionPoint>> extensionClasses,
																	 final XPFExtensionPointEnum extensionEnum) {
		return extensionClasses.stream()
				.map(this::getAssignments)
				.flatMap(Collection::stream)
				.filter(assignment -> assignment.getLeft().extensionPoint() == extensionEnum)
				.map(this::createExtensionConfiguration)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private List<Pair<XPFAssignment, Class<? extends ExtensionPoint>>> getAssignments(final Class<? extends ExtensionPoint> extensionPoint) {
		return Arrays.stream((extensionPoint.getAnnotationsByType(XPFAssignment.class)))
				.map(xpfAssignment -> new ImmutablePair<XPFAssignment, Class<? extends ExtensionPoint>>(xpfAssignment, extensionPoint))
				.collect(Collectors.toList());
	}

	/**
	 * Creates extension configurations.
	 *
	 * @param assignment the extension assignment
	 * @return list of extension configurations
	 */
	protected List<ExtensionPointConfigurationDTO> createExtensionConfiguration(final Pair<XPFAssignment,
			Class<? extends ExtensionPoint>> assignment) {
		final String pluginId = XPFUtils.getPluginIdForExtension(pluginManager, assignment.getRight());
		final List<XPFExtensionPointConfiguration> configurations = extensions.get(assignment.getLeft().extensionPoint())
				.stream()
				.filter(extension -> extension.getIdentifier().getExtensionClass().equals(assignment.getRight().getName()))
				.collect(Collectors.toList());

		return configurations.isEmpty()
				? createSingleAssignment(assignment, pluginId)
				: configurations.stream()
				.filter(XPFExtensionPointConfiguration::isEnabled)
				.map(config -> mergeConfiguration(config, assignment, pluginId))
				.collect(Collectors.toList());
	}

	private List<ExtensionPointConfigurationDTO> createSingleAssignment(final Pair<XPFAssignment, Class<? extends ExtensionPoint>> assignment,
																		final String pluginId) {
		return Collections.singletonList(new ExtensionPointConfigurationBuilder()
				.setExtensionClassName(assignment.getRight().getName())
				.setDefaultSelectorMode(XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE)
				.setPriority(assignment.getLeft().priority())
				.setPluginId(pluginId)
				.setExtensionGuid(UUID.randomUUID().toString())
				.setSelector(new XPFExtensionSelectorAny())
				.build());
	}

	/**
	 * Merges configuration from setting file with assignment configuration. Configuration from setting file has higher priority.
	 *
	 * @param configFromFile the configuration from file
	 * @param assignment     the extension assignment
	 * @param pluginId       the plugin id
	 * @return merged configuration
	 */
	protected ExtensionPointConfigurationDTO mergeConfiguration(final XPFExtensionPointConfiguration configFromFile,
																final Pair<XPFAssignment, Class<? extends ExtensionPoint>> assignment,
																final String pluginId) {
		return new ExtensionPointConfigurationBuilder()
				.setExtensionClassName(assignment.getRight().getName())
				.setDefaultSelectorMode(ObjectUtils.firstNonNull(Optional.ofNullable(configFromFile.getDefaultSelectorMode())
								.map(XPFExtensionDefaultSelectorModeEnum::valueOf)
								.orElse(null),
						XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE, assignment.getLeft().defaultSelectorMode()))
				.setPriority(ObjectUtils.firstNonNull(configFromFile.getPriority(), assignment.getLeft().priority()))
				.setPluginId(pluginId)
				.setExtensionSettings(convertToPluginSettingDTOs(configFromFile))
				.setExtensionGuid(UUID.randomUUID().toString())
				.setSelectors(ObjectUtils.firstNonNull(convertSelectors(configFromFile.getSelectors()),
						Collections.singletonList(new XPFExtensionSelectorAny())))
				.build();
	}

	private ListMultimap<XPFExtensionPointEnum, XPFExtensionPointConfiguration> loadExtensionAssignmentConfiguration(final String configPath) {
		final File configuration = Paths.get(configPath, EXTENSIONS_CONFIGURATION_FILE_NAME).toFile();
		try {
			return configuration.exists()
					? objectMapper.readValue(configuration, ExtensionConfigurations.class)
					.getExtensions()
					.stream()
					.peek(config ->
							config.getSettings().forEach(setting -> setting.getSettingValues().forEach(xpfSettingValueParser::resolvePlaceholder)))
					.collect(toMultimap(this::getKey, Function.identity(), ArrayListMultimap::create))
					: ArrayListMultimap.create();
		} catch (Exception e) {
			LOGGER.error("Exception during converting data from " + EXTENSIONS_CONFIGURATION_FILE_NAME + " file", e);
			return ArrayListMultimap.create();
		}
	}

	private Map<String, XPFPluginConfigurationDTO> loadPluginConfiguration(final String configPath) {
		final File configuration = Paths.get(configPath, PLUGINS_CONFIGURATION_FILE_NAME).toFile();
		try {
			return configuration.exists()
					? objectMapper.readValue(configuration, PluginConfigurations.class)
					.getPlugins()
					.stream()
					.peek(config ->
							config.getSettings().forEach(setting -> setting.getSettingValues().forEach(xpfSettingValueParser::resolvePlaceholder)))
					.collect(XPFHashMap::new,
							(map, config) -> map.put(config.getIdentifier().getPluginId(), convertToPluginConfiguration(config)),
							XPFHashMap::putAll)
					: Collections.emptyMap();
		} catch (EpSystemException e) {
			LOGGER.error("Invalid configuration of " + PLUGINS_CONFIGURATION_FILE_NAME + " file", e);
			throw e;
		} catch (Exception e) {
			LOGGER.error("Invalid configuration of " + PLUGINS_CONFIGURATION_FILE_NAME + " file", e);
			return Collections.emptyMap();
		}
	}

	private Set<PluginSettingDTO> convertToPluginSettingDTOs(final XPFExtensionPointConfiguration extension) {
		final AtomicInteger atomicInteger = new AtomicInteger(0);

		return Objects.isNull(extension) || Objects.isNull(extension.getSettings())
				? Collections.emptySet()
				: extension.getSettings()
				.stream()
				.map(setting -> convertPluginSettingDTO(atomicInteger, setting))
				.collect(Collectors.toSet());
	}

	/**
	 * Converts list of {@link Selector} to list of {@link XPFExtensionSelector}.
	 *
	 * @param selectors the list of {@link Selector}
	 * @return converted selectors
	 */
	protected List<XPFExtensionSelector> convertSelectors(final List<Selector> selectors) {
		final List<Selector> notNullSelectors = Objects.isNull(selectors)
				? Collections.emptyList()
				: selectors;

		final List<XPFExtensionSelectorAny> selectorsAny = notNullSelectors
				.stream()
				.filter(selector -> XPFExtensionSelectorAny.class.getSimpleName().equals(selector.getType()))
				.findFirst()
				.map(selector -> Collections.singletonList(new XPFExtensionSelectorAny()))
				.orElse(Collections.emptyList());

		final List<XPFExtensionSelector> selectorsByStoreCode = notNullSelectors
				.stream()
				.filter(selector -> XPFExtensionSelectorByStoreCode.class.getSimpleName().equals(selector.getType()))
				.map(Selector::getValue)
				.filter(StringUtils::isNoneEmpty)
				.map(XPFExtensionSelectorByStoreCode::new)
				.collect(Collectors.toList());

		return Stream.of(selectorsAny, selectorsByStoreCode).flatMap(Collection::stream).collect(Collectors.toList());
	}

	private XPFExtensionPointEnum getKey(final XPFExtensionPointConfiguration configuration) {
		return XPFExtensionPointEnum.valueOf(configuration.getIdentifier().getExtensionPointKey());
	}

	/**
	 * Converts {@link PluginConfiguration} to {@link XPFPluginConfigurationDTO}.
	 *
	 * @param configuration the {@link PluginConfiguration} configuration
	 * @return converted plugin configuration
	 */
	protected XPFPluginConfigurationDTO convertToPluginConfiguration(final PluginConfiguration configuration) {
		AtomicInteger atomicInteger = new AtomicInteger(0);
		return new XPFPluginConfigurationDTO(configuration.getSettings()
				.stream()
				.map(setting -> convertPluginSettingDTO(atomicInteger, setting))
				.collect(Collectors.toList()));
	}

	private PluginSettingDTO convertPluginSettingDTO(final AtomicInteger atomicInteger, final Setting setting) {
		return new PluginSettingDTO(setting.getSettingKey(),
				SettingDataTypeDTO.valueOf(setting.getDataType()),
				SettingCollectionTypeDTO.valueOf(setting.getCollectionType()),
				setting.getSettingValues()
						.stream()
						.map(value -> new PluginSettingValueDTO(atomicInteger.getAndIncrement(), value.getKey(), value.getValue(),
								SettingDataTypeDTO.valueOf(setting.getDataType())))
						.collect(Collectors.toList())
		);
	}
}
