package com.elasticpath.xpf.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pf4j.PluginWrapper;

import com.elasticpath.xpf.XPFExtensionDefaultSelectorModeEnum;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.XPFExtensionSelector;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.SystemInformation;
import com.elasticpath.xpf.dto.ExtensionPointConfigurationDTO;
import com.elasticpath.xpf.dto.PluginSettingDTO;
import com.elasticpath.xpf.dto.SettingCollectionTypeDTO;
import com.elasticpath.xpf.dto.SettingDataTypeDTO;
import com.elasticpath.xpf.dto.XPFPluginConfigurationDTO;
import com.elasticpath.xpf.json.ExtensionConfigurations;
import com.elasticpath.xpf.json.Identifier;
import com.elasticpath.xpf.json.PluginConfiguration;
import com.elasticpath.xpf.json.PluginConfigurations;
import com.elasticpath.xpf.json.Selector;
import com.elasticpath.xpf.json.Setting;
import com.elasticpath.xpf.json.SettingValue;
import com.elasticpath.xpf.json.XPFExtensionPointConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class XPFPluginConfigurationLoaderTest {

	private static final int ASSIGNMENT_PRIORITY = 1040;
	private static final String PLUGIN_ID = "pluginId";
	private static final String SETTING_KEY = "key";
	private static final String MAP_KEY = "mapKey";
	private static final String MOBEE_STORE = "MOBEE";
	private static final String PATH = "target/plugins";
	private static final int CONFIG_PRIORITY = 1;
	private static final String EXTENSIONS_JSON_CONFIG = "extensions.json";
	@Mock
	private XPFPluginManager pluginManager;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private XPFSettingValueParser xpfSettingValueParser;

	private XPFConfigurationLoaderImpl configurationLoader;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws IOException {
		PluginConfiguration pluginConfiguration = createPluginConfiguration();
		XPFExtensionPointConfiguration extensionConfiguration = createExtensionConfiguration(true,
				new Selector(XPFExtensionSelectorByStoreCode.class.getSimpleName(), MOBEE_STORE));

		when(objectMapper.readValue(Paths.get(PATH, "plugins.json").toFile(), PluginConfigurations.class))
				.thenReturn(new PluginConfigurations(Collections.singletonList(pluginConfiguration)));

		when(objectMapper.readValue(Paths.get(PATH, EXTENSIONS_JSON_CONFIG).toFile(), ExtensionConfigurations.class))
				.thenReturn(new ExtensionConfigurations(Collections.singletonList(extensionConfiguration)));

		configurationLoader = new XPFConfigurationLoaderImpl(objectMapper, pluginManager, PATH, xpfSettingValueParser);

		PluginWrapper pluginWrapper = mock(PluginWrapper.class);
		when(pluginWrapper.getPluginId()).thenReturn(PLUGIN_ID);

		when(pluginManager.whichPlugin(TestXPFExtensionPoint.class)).thenReturn(pluginWrapper);

		when(pluginManager.getUnfilteredExtensionClasses(
				(Class<SystemInformation>) XPFExtensionPointEnum.SYSTEM_INFORMATION.getExtensionPointInterface()))
				.thenReturn(Collections.singletonList(TestXPFExtensionPoint.class));
	}

	@Test
	public void testLoadPluginConfiguration() {
		List<PluginSettingDTO> pluginSettings = configurationLoader.getPluginConfigurationMap().get(PLUGIN_ID).getPluginSettings();

		assertEquals(1, pluginSettings.size());
		PluginSettingDTO dto = pluginSettings.get(0);

		checkSetting(dto);
	}

	@Test
	public void testLoadExtensionConfigurationAndSetFromFile() {
		List<ExtensionPointConfigurationDTO> extensionSettings = configurationLoader.getExtensionPointConfigurationMap()
				.get(XPFExtensionPointEnum.SYSTEM_INFORMATION);

		assertEquals(1, extensionSettings.size());
		ExtensionPointConfigurationDTO dto = extensionSettings.get(0);

		assertEquals(1, dto.getPriority().intValue());
		assertEquals(PLUGIN_ID, dto.getPluginId());
		assertEquals(TestXPFExtensionPoint.class.getName(), dto.getExtensionClassName());
		assertEquals(XPFExtensionDefaultSelectorModeEnum.DEFAULT_ALL, dto.getDefaultSelectorMode());
		assertEquals(MOBEE_STORE, dto.getSelectors().get(0).getValue());
		assertNotNull(dto.getExtensionGuid());
		checkSetting(dto.getExtensionSettings().stream().findFirst().get());
	}

	@Test
	public void testLoadExtensionConfigurationDisabled() throws IOException {
		XPFExtensionPointConfiguration extensionConfiguration = createExtensionConfiguration(false,
				new Selector(XPFExtensionSelectorByStoreCode.class.getSimpleName(), MOBEE_STORE));

		when(objectMapper.readValue(Paths.get(PATH, EXTENSIONS_JSON_CONFIG).toFile(), ExtensionConfigurations.class))
				.thenReturn(new ExtensionConfigurations(Collections.singletonList(extensionConfiguration)));

		configurationLoader = new XPFConfigurationLoaderImpl(objectMapper, pluginManager, PATH, xpfSettingValueParser);

		List<ExtensionPointConfigurationDTO> extensionSettings = configurationLoader.getExtensionPointConfigurationMap()
				.get(XPFExtensionPointEnum.SYSTEM_INFORMATION);

		assertEquals(0, extensionSettings.size());
	}

	@Test
	public void testLoadExtensionConfigurationAndSetFromAnnotation() throws IOException {
		when(objectMapper.readValue(Paths.get(PATH, EXTENSIONS_JSON_CONFIG).toFile(), ExtensionConfigurations.class))
				.thenReturn(new ExtensionConfigurations(Collections.emptyList()));
		configurationLoader = new XPFConfigurationLoaderImpl(objectMapper, pluginManager, PATH, xpfSettingValueParser);


		List<ExtensionPointConfigurationDTO> extensionSettings = configurationLoader.getExtensionPointConfigurationMap()
				.get(XPFExtensionPointEnum.SYSTEM_INFORMATION);

		assertEquals(1, extensionSettings.size());
		ExtensionPointConfigurationDTO dto = extensionSettings.get(0);

		assertEquals(ASSIGNMENT_PRIORITY, dto.getPriority().intValue());
		assertEquals(PLUGIN_ID, dto.getPluginId());
		assertEquals(TestXPFExtensionPoint.class.getName(), dto.getExtensionClassName());
		assertEquals(XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE, dto.getDefaultSelectorMode());
	}

	@Test
	public void testMergeConfigurationPrioritizeFileConfiguration() {
		XPFExtensionPointConfiguration configuration = createExtensionConfiguration(true,
				new Selector(XPFExtensionSelectorByStoreCode.class.getSimpleName(), MOBEE_STORE));
		final XPFAssignment assignment = createAssignment(XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE, ASSIGNMENT_PRIORITY);
		ExtensionPointConfigurationDTO dto = configurationLoader.mergeConfiguration(configuration,
				new ImmutablePair<>(assignment, TestXPFExtensionPoint.class), PLUGIN_ID);

		//replaced data from configuration file
		assertEquals(TestXPFExtensionPoint.class.getName(), dto.getExtensionClassName());
		assertEquals(XPFExtensionDefaultSelectorModeEnum.DEFAULT_ALL, dto.getDefaultSelectorMode());
		assertEquals(CONFIG_PRIORITY, dto.getPriority().intValue());


		//other configuration from file
		assertNotNull(dto.getExtensionGuid());
		assertEquals(PLUGIN_ID, dto.getPluginId());
		assertEquals(MOBEE_STORE, dto.getSelectors().get(0).getValue());

		checkSetting(dto.getExtensionSettings().stream().findFirst().get());
	}

	@Test
	public void testMergeConfigurationProvideAssignmentConfiguration() {
		XPFExtensionPointConfiguration configuration = new XPFExtensionPointConfiguration(null, true, null, null, null, null);
		XPFAssignment assignment = createAssignment(XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE, ASSIGNMENT_PRIORITY);
		ExtensionPointConfigurationDTO dto = configurationLoader.mergeConfiguration(configuration,
				new ImmutablePair<>(assignment, TestXPFExtensionPoint.class), PLUGIN_ID);

		assertEquals(TestXPFExtensionPoint.class.getName(), dto.getExtensionClassName());
		assertEquals(XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE, dto.getDefaultSelectorMode());
		assertEquals(ASSIGNMENT_PRIORITY, dto.getPriority().intValue());
	}

	@Test
	public void testCreateExtensionConfiguration() {
		XPFAssignment assignment = createAssignment(XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE, ASSIGNMENT_PRIORITY);
		List<ExtensionPointConfigurationDTO> dtos = configurationLoader.createExtensionConfiguration(new ImmutablePair<>(assignment,
				TestXPFExtensionPoint.class));

		assertEquals(1, dtos.size());
		ExtensionPointConfigurationDTO dto = dtos.get(0);

		assertEquals(1, dto.getPriority().intValue());
		assertEquals(PLUGIN_ID, dto.getPluginId());
		assertEquals(TestXPFExtensionPoint.class.getName(), dto.getExtensionClassName());
		assertEquals(XPFExtensionDefaultSelectorModeEnum.DEFAULT_ALL, dto.getDefaultSelectorMode());
		assertEquals(MOBEE_STORE, dto.getSelectors().get(0).getValue());
		assertNotNull(dto.getExtensionGuid());
		assertEquals(1, dto.getExtensionSettings().size());
	}

	@Test
	public void testCreateExtensionConfigurationSkipNotEnabled() throws IOException {
		XPFExtensionPointConfiguration extensionConfiguration = createExtensionConfiguration(false,
				new Selector(XPFExtensionSelectorByStoreCode.class.getSimpleName(), MOBEE_STORE));
		when(objectMapper.readValue(Paths.get(PATH, EXTENSIONS_JSON_CONFIG).toFile(), ExtensionConfigurations.class))
				.thenReturn(new ExtensionConfigurations(Collections.singletonList(extensionConfiguration)));
		configurationLoader = new XPFConfigurationLoaderImpl(objectMapper, pluginManager, PATH, xpfSettingValueParser);

		XPFAssignment assignment = createAssignment(XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE, ASSIGNMENT_PRIORITY);
		List<ExtensionPointConfigurationDTO> dtos = configurationLoader.createExtensionConfiguration(new ImmutablePair<>(assignment,
				TestXPFExtensionPoint.class));

		assertEquals(0, dtos.size());
	}

	@Test
	public void testConvertSelectors() {
		List<XPFExtensionSelector> selectors =
				configurationLoader.convertSelectors(Arrays.asList(new Selector(XPFExtensionSelectorByStoreCode.class.getSimpleName(), MOBEE_STORE),
						new Selector(XPFExtensionSelectorAny.class.getSimpleName(), null)));

		XPFExtensionSelector any = selectors.stream().filter(selector -> Objects.isNull(selector.getValue())).findAny().orElse(null);
		XPFExtensionSelector storeSelector = selectors.stream().filter(selector -> Objects.nonNull(selector.getValue())).findAny().orElse(null);

		//check XPFExtensionSelectorAny
		assertThat(any, instanceOf(XPFExtensionSelectorAny.class));
		assertThat(storeSelector, instanceOf(XPFExtensionSelectorByStoreCode.class));

		//check XPFExtensionSelectorByStoreCode
		assertEquals(MOBEE_STORE, storeSelector.getValue());
	}

	@Test
	public void testConvertToPluginConfiguration() {
		final PluginConfiguration configuration = createPluginConfigurationWithMultiSettingValues();
		XPFPluginConfigurationDTO dto = configurationLoader.convertToPluginConfiguration(configuration);

		PluginSettingDTO setting = dto.getPluginSettings().get(0);
		assertEquals(SETTING_KEY, setting.getSettingKey());
		assertEquals(0, setting.getSettingValues().get(0).getSequence());
		assertEquals(1, setting.getSettingValues().get(1).getSequence());
		assertEquals(2, setting.getSettingValues().get(2).getSequence());

		assertEquals(SettingCollectionTypeDTO.MAP, setting.getCollectionType());
		assertEquals(SettingDataTypeDTO.INTEGER, setting.getDataType());
	}

	private XPFAssignment createAssignment(final XPFExtensionDefaultSelectorModeEnum selectorMode, final int priority) {
		final XPFAssignment xpfAssignment = mock(XPFAssignment.class);
		when(xpfAssignment.defaultSelectorMode()).thenReturn(selectorMode);
		when(xpfAssignment.extensionPoint()).thenReturn(XPFExtensionPointEnum.SYSTEM_INFORMATION);
		when(xpfAssignment.priority()).thenReturn(priority);
		return xpfAssignment;
	}

	private XPFExtensionPointConfiguration createExtensionConfiguration(final boolean enabled, final Selector selector) {
		final Set<Setting> settings = Collections.singleton(new Setting(SETTING_KEY, SettingDataTypeDTO.INTEGER.toString(),
				SettingCollectionTypeDTO.MAP.toString(),
				Collections.singletonList(new SettingValue(MAP_KEY, 1))));
		return new XPFExtensionPointConfiguration(new Identifier(PLUGIN_ID, TestXPFExtensionPoint.class.getName(),
				XPFExtensionPointEnum.SYSTEM_INFORMATION.toString()),
				enabled,
				CONFIG_PRIORITY,
				"DEFAULT_ALL",
				Collections.singletonList(selector),
				settings
		);
	}

	private void checkSetting(final PluginSettingDTO dto) {
		assertEquals(SETTING_KEY, dto.getSettingKey());
		assertEquals(SettingCollectionTypeDTO.MAP, dto.getCollectionType());
		assertEquals(SettingDataTypeDTO.INTEGER, dto.getDataType());
		assertEquals(1, ((Map) dto.getValues()).get(MAP_KEY));
	}

	private PluginConfiguration createPluginConfiguration() {
		final List<Setting> settings = Collections.singletonList(new Setting(SETTING_KEY, SettingDataTypeDTO.INTEGER.toString(),
				SettingCollectionTypeDTO.MAP.toString(),
				Collections.singletonList(new SettingValue(MAP_KEY, 1))));
		return new PluginConfiguration(new Identifier(PLUGIN_ID, null, null), settings);
	}

	private PluginConfiguration createPluginConfigurationWithMultiSettingValues() {
		final List<Setting> settings = Collections.singletonList(new Setting(SETTING_KEY, SettingDataTypeDTO.INTEGER.toString(),
				SettingCollectionTypeDTO.MAP.toString(),
				Arrays.asList(new SettingValue(MAP_KEY, 0), new SettingValue(MAP_KEY, 1), new SettingValue(MAP_KEY, 2))));

		return new PluginConfiguration(new Identifier(PLUGIN_ID, null, null), settings);
	}

	@XPFAssignment(extensionPoint = XPFExtensionPointEnum.SYSTEM_INFORMATION, priority = ASSIGNMENT_PRIORITY,
			defaultSelectorMode = XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE)
	private class TestXPFExtensionPoint extends XPFExtensionPointImpl implements SystemInformation {

		@Override
		public String getName() {
			return null;
		}

		@Override
		public String getSimpleValue() {
			return null;
		}
	}
}
