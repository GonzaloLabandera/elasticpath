/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import org.pf4j.Plugin;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginFactory;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.xpf.XPFConfigurationLoader;
import com.elasticpath.xpf.connectivity.plugin.XPFExternalPlugin;
import com.elasticpath.xpf.converters.XPFPluginSettingConverter;
import com.elasticpath.xpf.dto.PluginSettingDTO;
import com.elasticpath.xpf.dto.XPFPluginConfigurationDTO;
import com.elasticpath.xpf.dto.ExtensionPointConfigurationDTO;
import com.elasticpath.xpf.exception.InvalidPluginException;
import com.elasticpath.xpf.impl.test_extension.StubbedEmbeddedExtension;
import com.elasticpath.xpf.impl.test_extension.StubbedExtension;

@RunWith(MockitoJUnitRunner.class)
public class XPFPluginManagerTest {
	private static final String EXTENSION_GUID = "extensionGuid";
	private static final String PLUGIN_ID = "pluginId";
	@Mock
	private XPFSingletonExtensionFactory singletonExtensionFactory;
	@Mock
	private BeanFactory beanFactory;
	@Mock
	private ClassLoader classLoader;
	@Mock
	private PluginSettingDTO setting;

	@Mock
	private XPFConfigurationLoader configurationLoader;

	@Mock
	private XPFPluginSettingConverter settingConverter;

	@Mock
	private Map<String, XPFPluginConfigurationDTO> configurationMap;

	private final Map<String, PluginWrapper> externalPlugins = new HashMap<>();

	private XPFPluginManager xpfPluginManager;

	@Before
	public void setUp() {
		xpfPluginManager = new XPFPluginManager(singletonExtensionFactory, beanFactory, settingConverter, "") {
			@Override
			protected void initialize() {
				super.initialize();
				super.pluginClassLoaders = Collections.singletonMap(PLUGIN_ID, classLoader);
				super.plugins = externalPlugins;
			}

			@Override
			public boolean enablePlugin(final String pluginId) {
				return false;
			}
		};

		when(beanFactory.getSingletonBean("xpfConfigurationLoader", XPFConfigurationLoader.class)).thenReturn(configurationLoader);

		when(configurationLoader.getPluginConfigurationMap())
				.thenReturn(configurationMap);
		when(configurationMap.get(PLUGIN_ID))
				.thenReturn(mock(XPFPluginConfigurationDTO.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testLoadingEmbeddedExtension() {
		ExtensionPointConfigurationDTO configuration = new ExtensionPointConfigurationBuilder()
				.setExtensionClassName(StubbedEmbeddedExtension.class.getName())
				.setSelectors(Collections.singletonList(new XPFExtensionSelectorAny()))
				.setPriority(0)
				.setPluginId(null)
				.setExtensionGuid("extensionGuid")
				.setExtensionSettings(Collections.emptySet())
				.build();

		xpfPluginManager.ensureExtensionCreated(configuration);

		verify(singletonExtensionFactory).initializeAndCacheExtension(StubbedEmbeddedExtension.class, EXTENSION_GUID, Collections.emptySet());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testLoadingExternalExtension() throws ClassNotFoundException {
		ExtensionPointConfigurationDTO configuration = new ExtensionPointConfigurationBuilder()
				.setExtensionClassName(StubbedExtension.class.getName())
				.setSelectors(Collections.singletonList(new XPFExtensionSelectorAny()))
				.setPriority(0).setPluginId("pluginId")
				.setExtensionGuid("extensionGuid")
				.setExtensionSettings(Collections.singleton(setting))
				.build();
		((OngoingStubbing) when(classLoader.loadClass(anyString()))).thenReturn(StubbedExtension.class);

		xpfPluginManager.ensureExtensionCreated(configuration);

		verify(singletonExtensionFactory).initializeAndCacheExtension(StubbedExtension.class, EXTENSION_GUID, Collections.singleton(setting));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testStartPlugins() {
		PluginWrapper pluginWrapper = createPluginWrapper(XPFExternalPlugin::new);

		xpfPluginManager.getResolvedPlugins().add(pluginWrapper);
		xpfPluginManager.startPlugins();

		assertThat(pluginWrapper.getPluginState())
				.isEqualTo(PluginState.STARTED);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testInvalidPluginType() {
		PluginWrapper pluginWrapper = createPluginWrapper(Plugin::new);

		xpfPluginManager.getResolvedPlugins().add(pluginWrapper);

		assertThatThrownBy(() -> xpfPluginManager.startPlugins())
				.isInstanceOf(InvalidPluginException.class)
				.hasMessage("Plugin with id pluginId is not instance of XPFExternalPlugin");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testStartedPluginsSkipped() {
		PluginWrapper pluginWrapper = createPluginWrapper(XPFExternalPlugin::new);

		pluginWrapper.setPluginState(PluginState.STARTED);

		xpfPluginManager.getResolvedPlugins().add(pluginWrapper);
		xpfPluginManager.startPlugins();

		assertThat(((XPFExternalPlugin) pluginWrapper.getPlugin()).getContext()).isNull();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDisabledPluginsSkipped() {
		PluginWrapper pluginWrapper = createPluginWrapper(XPFExternalPlugin::new);

		pluginWrapper.setPluginState(PluginState.DISABLED);

		xpfPluginManager.getResolvedPlugins().add(pluginWrapper);
		xpfPluginManager.startPlugins();

		assertThat(((XPFExternalPlugin) pluginWrapper.getPlugin()).getContext()).isNull();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testStartPlugin() {
		PluginWrapper pluginWrapper = createPluginWrapper(XPFExternalPlugin::new);
		externalPlugins.put(PLUGIN_ID, pluginWrapper);

		xpfPluginManager.startPlugin(PLUGIN_ID);

		assertThat(XPFExternalPlugin.getInstance().getContext().getLogger().getName())
				.isEqualTo(XPFExternalPlugin.class.getName());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testStartedPluginSkipped() {
		PluginWrapper pluginWrapper = createPluginWrapper(XPFExternalPlugin::new);
		externalPlugins.put(PLUGIN_ID, pluginWrapper);
		pluginWrapper.setPluginState(PluginState.STARTED);

		xpfPluginManager.startPlugin(PLUGIN_ID);

		assertThat(((XPFExternalPlugin) pluginWrapper.getPlugin()).getContext()).isNull();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDisabledPluginSkipped() {
		PluginWrapper pluginWrapper = createPluginWrapper(XPFExternalPlugin::new);
		externalPlugins.put(PLUGIN_ID, pluginWrapper);
		pluginWrapper.setPluginState(PluginState.DISABLED);

		xpfPluginManager.startPlugin(PLUGIN_ID);

		assertThat(((XPFExternalPlugin) pluginWrapper.getPlugin()).getContext()).isNull();
	}

	private PluginWrapper createPluginWrapper(final Function<PluginWrapper, Plugin> pluginConstructor) {
		PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);
		when(pluginDescriptor.getPluginId()).thenReturn(PLUGIN_ID);

		PluginWrapper pluginWrapper = new PluginWrapper(mock(PluginManager.class), pluginDescriptor, null, null);
		pluginWrapper.setPluginState(PluginState.CREATED);
		PluginFactory pluginFactory = mock(PluginFactory.class);
		pluginWrapper.setPluginFactory(pluginFactory);

		Plugin externalPlugin = pluginConstructor.apply(pluginWrapper);

		when(pluginFactory.create(pluginWrapper)).thenReturn(externalPlugin);
		return pluginWrapper;
	}
}
