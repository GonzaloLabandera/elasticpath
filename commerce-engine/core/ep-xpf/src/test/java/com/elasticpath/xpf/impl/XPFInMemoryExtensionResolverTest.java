/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ListMultimap;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.xpf.XPFConfigurationLoader;
import com.elasticpath.xpf.XPFExtensionDefaultSelectorModeEnum;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.XPFExtensionSelector;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPoint;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;
import com.elasticpath.xpf.connectivity.extensionpoint.SystemInformation;
import com.elasticpath.xpf.dto.ExtensionPointConfigurationDTO;
import com.elasticpath.xpf.impl.test_extension.SomeExtensionClass;

@RunWith(MockitoJUnitRunner.class)
public class XPFInMemoryExtensionResolverTest {

	private static final String EXTENSION_GUID = "extensionGuid";
	private static final String SOME_PLUGIN_ID = "somePluginID";

	@Mock
	private XPFPluginManager pluginManager;
	@Mock
	private XPFConfigurationLoader configurationLoader;
	@Mock
	private ListMultimap<XPFExtensionPointEnum, ExtensionPointConfigurationDTO> multimap;

	@InjectMocks
	private XPFInMemoryExtensionResolverImpl xpfResolver;

	private final XPFExtensionPoint extensionPointDefaultALL = new TestXPFExtensionPointDefaultAll();

	@SuppressWarnings("checkstyle:magicnumber")
	@XPFAssignment(extensionPoint = XPFExtensionPointEnum.SYSTEM_INFORMATION, priority = 1040)
	private class TestXPFExtensionPointDefaultAll extends XPFExtensionPointImpl implements SystemInformation {

		@Override
		public String getName() {
			return null;
		}

		@Override
		public String getSimpleValue() {
			return null;
		}
	}

	@SuppressWarnings("checkstyle:magicnumber")
	@XPFAssignment(extensionPoint = XPFExtensionPointEnum.SYSTEM_INFORMATION, priority = 1040, defaultSelectorMode =
			XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE)
	private class TestXPFExtensionPointDefaultNone extends XPFExtensionPointImpl implements SystemInformation {

		@Override
		public String getName() {
			return null;
		}

		@Override
		public String getSimpleValue() {
			return null;
		}
	}

	private static final String EXTENSION_CLASS_NAME_A = SomeExtensionClass.class.getName();
	private static final String EXTENSION_CLASS_NAME_B = "com.elasticpath.xpf.impl.test_extension.SomeExtensionClassB";

	private static final String STORE_CODE_MOBEE = "MOBEE";
	private static final String STORE_CODE_KOBEE = "KOBEE";

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		doNothing().when(pluginManager).ensureExtensionCreated(any());
		when(configurationLoader.getExtensionPointConfigurationMap())
				.thenReturn(multimap);
		cleanConfigMap();
	}

	@Test
	public void testResolveExtensionPoints() {

		SystemInformation ext1 = new TestXPFExtensionPointDefaultAll();
		SystemInformation ext2 = new TestXPFExtensionPointDefaultNone();
		SystemInformation ext3 = new TestXPFExtensionPointDefaultAll();
		((XPFExtensionPoint) ext1).setExtensionGuid("1");
		((XPFExtensionPoint) ext2).setExtensionGuid("2");
		((XPFExtensionPoint) ext3).setExtensionGuid("3");

		addExtension(ext1.getClass().getName(), new XPFExtensionSelectorAny(), 0, XPFExtensionPointEnum.SYSTEM_INFORMATION, null, "1");
		addExtension(ext2.getClass().getName(), new XPFExtensionSelectorAny(), 2, XPFExtensionPointEnum.SYSTEM_INFORMATION, null, "2");
		addExtension(ext3.getClass().getName(), new XPFExtensionSelectorAny(), 1, XPFExtensionPointEnum.SYSTEM_INFORMATION, null, "3");

		List<SystemInformation> extensions = xpfResolver.resolveExtensionPoints(Arrays.asList(ext1, ext2, ext3),
				XPFExtensionPointEnum.SYSTEM_INFORMATION, new XPFExtensionSelectorAny());

		assertThat(extensions).containsExactly(ext1, ext3, ext2);
	}

	@Test
	public void testGetAssignedExtensionClassNames() {
		addExtension(EXTENSION_CLASS_NAME_A, new XPFExtensionSelectorAny(), XPFExtensionPointEnum.SYSTEM_INFORMATION, null);

		List<String> assignedExtensionClassNames = xpfResolver.getAssignedExtensionClassNames(XPFExtensionPointEnum.SYSTEM_INFORMATION, null);

		assertEquals(1, assignedExtensionClassNames.size());
	}

	@Test
	public void testAssignExtensionToSelector() {
		xpfResolver.assignExtensionToSelector(EXTENSION_CLASS_NAME_A, SOME_PLUGIN_ID, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorAny(), 0);

		List<ExtensionPointConfigurationDTO> configurations = xpfResolver.getExtensionPointMap()
				.get(XPFExtensionPointEnum.SYSTEM_INFORMATION);

		assertEquals(1, configurations.size());
		verify(pluginManager).ensureExtensionCreated(configurations.get(0));
		checkConfiguration(configurations.get(0));
	}

	@Test
	public void testRemoveExtensionFromSelectorAny() {
		xpfResolver.assignExtensionToSelector(EXTENSION_CLASS_NAME_A, SOME_PLUGIN_ID, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorAny(), 0);
		xpfResolver.assignExtensionToSelector(EXTENSION_CLASS_NAME_A, SOME_PLUGIN_ID, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorByStoreCode(STORE_CODE_KOBEE), 0);

		xpfResolver.removeExtensionFromSelector(EXTENSION_CLASS_NAME_A, SOME_PLUGIN_ID, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorAny());

		List<ExtensionPointConfigurationDTO> configurations = xpfResolver.getExtensionPointMap()
				.get(XPFExtensionPointEnum.SYSTEM_INFORMATION);

		assertEquals(0, configurations.size());
	}

	@Test
	public void testRemoveExtensionFromSelectorByStoreCodeDeleteSelectors() {
		xpfResolver.assignExtensionToSelector(EXTENSION_CLASS_NAME_A, SOME_PLUGIN_ID, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorByStoreCode(STORE_CODE_KOBEE), 0);

		xpfResolver.removeExtensionFromSelector(EXTENSION_CLASS_NAME_A, SOME_PLUGIN_ID, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorByStoreCode(STORE_CODE_KOBEE));

		List<ExtensionPointConfigurationDTO> configurations = xpfResolver.getExtensionPointMap()
				.get(XPFExtensionPointEnum.SYSTEM_INFORMATION);

		assertEquals(0, configurations.get(0).getSelectors().size());
	}

	@Test
	public void testRemoveExtensionFromSelectorByStoreCodeDoesNotDeleteSelectors() {
		xpfResolver.assignExtensionToSelector(EXTENSION_CLASS_NAME_A, SOME_PLUGIN_ID, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorByStoreCode(STORE_CODE_MOBEE), 0);

		xpfResolver.removeExtensionFromSelector(EXTENSION_CLASS_NAME_A, SOME_PLUGIN_ID, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorByStoreCode(STORE_CODE_KOBEE));

		List<ExtensionPointConfigurationDTO> configurations = xpfResolver.getExtensionPointMap()
				.get(XPFExtensionPointEnum.SYSTEM_INFORMATION);

		assertEquals(1, configurations.get(0).getSelectors().size());
	}

	@Test
	public void testRemoveExtensionFromSelectorByStoreCodeDeleteSelectorsDefaultAll() {
		addExtension(EXTENSION_CLASS_NAME_A, new XPFExtensionSelectorByStoreCode(STORE_CODE_KOBEE), XPFExtensionPointEnum.SYSTEM_INFORMATION);

		xpfResolver.removeExtensionFromSelector(EXTENSION_CLASS_NAME_A, null, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorByStoreCode(STORE_CODE_MOBEE));

		List<ExtensionPointConfigurationDTO> configurations = xpfResolver.getExtensionPointMap()
				.get(XPFExtensionPointEnum.SYSTEM_INFORMATION);

		assertEquals(2, configurations.get(0).getSelectors().size());
	}

	private void checkConfiguration(final ExtensionPointConfigurationDTO configuration) {
		assertEquals(0, configuration.getPriority().intValue());
		assertEquals(XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE, configuration.getDefaultSelectorMode());
		assertEquals(EXTENSION_CLASS_NAME_A, configuration.getExtensionClassName());
		assertNotNull(configuration.getExtensionGuid());
		assertEquals(0, configuration.getExtensionSettings().size());
		assertThat(configuration.getSelectors().get(0)).isInstanceOf(XPFExtensionSelectorAny.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveExtensionFromExtensionPoint() {
		when(multimap.get(XPFExtensionPointEnum.SYSTEM_INFORMATION)).thenReturn(Collections.singletonList(new ExtensionPointConfigurationBuilder()
				.setExtensionClassName(extensionPointDefaultALL.getClass().getName())
				.setSelectors(Collections.singletonList(new XPFExtensionSelectorAny()))
				.build()));

		xpfResolver.init();

		List<ExtensionPointConfigurationDTO> extensionPointConfigurations =
				xpfResolver.getExtensionPointMap().get(XPFExtensionPointEnum.SYSTEM_INFORMATION);
		assertEquals(1, extensionPointConfigurations.size());

		xpfResolver.removeExtensionFromSelector(extensionPointDefaultALL.getClass().getName(), null,
				XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorAny());

		assertEquals(0, extensionPointConfigurations.size());
	}

	@Test
	public void testUpdatePluginId() {
		final String initialId = "initialId";
		final String updatedId = "updatedId";

		xpfResolver.assignExtensionToSelector(EXTENSION_CLASS_NAME_A, null, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorAny(), 0);
		xpfResolver.assignExtensionToSelector(EXTENSION_CLASS_NAME_B, initialId, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorAny(), 0);

		xpfResolver.updatePluginId(initialId, updatedId);

		assertThat(xpfResolver.getExtensionPointMap().get(XPFExtensionPointEnum.SYSTEM_INFORMATION))
				.extracting(ExtensionPointConfigurationDTO::getPluginId)
				.containsOnly(null, updatedId);

		ExtensionPointConfigurationDTO configuration = xpfResolver.getExtensionPointMap().get(XPFExtensionPointEnum.SYSTEM_INFORMATION)
				.stream()
				.filter(config -> updatedId.equals(config.getPluginId()))
				.findAny()
				.get();
		verify(pluginManager, times(2)).ensureExtensionCreated(configuration);
	}

	@Test
	public void testExtensionsReturnForAllPlugins() {
		addExtension(EXTENSION_CLASS_NAME_A, new XPFExtensionSelectorAny(), XPFExtensionPointEnum.SYSTEM_INFORMATION, "somePluginID_1");
		addExtension(EXTENSION_CLASS_NAME_B, new XPFExtensionSelectorAny(), XPFExtensionPointEnum.SYSTEM_INFORMATION, "somePluginID_2");

		List<String> assignedExtensionClassNames = xpfResolver.getAssignedExtensionClassNames(XPFExtensionPointEnum.SYSTEM_INFORMATION);

		assertEquals(2, assignedExtensionClassNames.size());
	}

	@Test
	public void testAssignedExtensionConfigurations() {
		ProductSkuValidator extension = context -> null;
		addExtension(extension.getClass().getName(), new XPFExtensionSelectorAny(),
				XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART_READ, SOME_PLUGIN_ID);

		List<ExtensionPointConfigurationDTO> configurationList = xpfResolver.getAssignedExtensionConfigurations(extension.getClass());

		assertThat(configurationList)
				.hasSize(1)
				.extracting(ExtensionPointConfigurationDTO::getPluginId)
				.containsOnly(SOME_PLUGIN_ID);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testInit() {
		// when
		ExtensionPointConfigurationDTO configuration = mock(ExtensionPointConfigurationDTO.class);
		when(multimap.get(any()))
				.thenReturn(Collections.singletonList(configuration));
		xpfResolver.init();

		// then
		Arrays.stream(XPFExtensionPointEnum.values()).forEach(value -> verify(multimap).get(value));
		verify(pluginManager, times(XPFExtensionPointEnum.values().length)).ensureExtensionCreated(configuration);
		assertThat(xpfResolver.getExtensionPointMap().asMap().size()).isEqualTo(XPFExtensionPointEnum.values().length);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void assignPluginExtensionsTest() {
		final ExtensionPointConfigurationDTO configuration = new ExtensionPointConfigurationBuilder()
				.setExtensionClassName(extensionPointDefaultALL.getClass().getName())
				.setPluginId(SOME_PLUGIN_ID)
				.setSelectors(Collections.singletonList(new XPFExtensionSelectorAny()))
				.build();

		// when
		when(multimap.asMap())
				.thenReturn(Collections.singletonMap(XPFExtensionPointEnum.SYSTEM_INFORMATION, Collections.singletonList(configuration)));
		xpfResolver.assignPluginExtensions(SOME_PLUGIN_ID);

		// then
		verify(pluginManager).ensureExtensionCreated(configuration);
		assertThat(xpfResolver.getExtensionPointMap().asMap().size()).isEqualTo(1);
	}

	private void addExtension(final String extensionClassName, final XPFExtensionSelector selector, final int priority,
							  final XPFExtensionPointEnum extensionPointEnum, final String pluginID, final String extensionGuid) {
		List<XPFExtensionSelector> selectors = new ArrayList<>();
		selectors.add(selector);

		ExtensionPointConfigurationDTO extensionPointConfiguration =
				new ExtensionPointConfigurationBuilder()
						.setExtensionClassName(extensionClassName)
						.setSelectors(selectors)
						.setPriority(priority)
						.setPluginId(pluginID)
						.setExtensionGuid(extensionGuid)
						.setExtensionSettings(Collections.emptySet())
						.setDefaultSelectorMode(XPFExtensionDefaultSelectorModeEnum.DEFAULT_NONE)
						.build();
		xpfResolver.getExtensionPointMap().put(extensionPointEnum, extensionPointConfiguration);
	}

	private void addExtension(final String extensionClassName, final XPFExtensionSelector selector,
							  final XPFExtensionPointEnum extensionPointEnum, final String pluginID) {
		addExtension(extensionClassName, selector, 0, extensionPointEnum, pluginID, EXTENSION_GUID);
	}

	private void addExtension(final String extensionClassName, final XPFExtensionSelector selector,
							  final XPFExtensionPointEnum extensionPointEnum) {
		List<XPFExtensionSelector> selectors = new ArrayList<>();
		selectors.add(selector);

		ExtensionPointConfigurationDTO extensionPointConfiguration =
				new ExtensionPointConfigurationBuilder()
						.setExtensionClassName(extensionClassName)
						.setSelectors(selectors)
						.setExtensionGuid(EXTENSION_GUID)
						.setExtensionSettings(Collections.emptySet())
						.setDefaultSelectorMode(XPFExtensionDefaultSelectorModeEnum.DEFAULT_ALL)
						.build();
		xpfResolver.getExtensionPointMap().put(extensionPointEnum, extensionPointConfiguration);
	}

	private void cleanConfigMap() {
		xpfResolver.getExtensionPointMap().clear();
	}
}
