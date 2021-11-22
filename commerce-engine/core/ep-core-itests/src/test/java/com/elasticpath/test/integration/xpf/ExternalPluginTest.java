/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.test.integration.xpf;

import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.xpf.XPFConfigurationLoader;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.XPFPluginFactory;
import com.elasticpath.xpf.connectivity.extensionpoint.SystemInformation;
import com.elasticpath.xpf.dto.ExtensionPointConfigurationDTO;
import com.elasticpath.xpf.dto.PluginSettingDTO;
import com.elasticpath.xpf.dto.SettingCollectionTypeDTO;
import com.elasticpath.xpf.dto.SettingDataTypeDTO;
import com.elasticpath.xpf.dto.XPFPluginConfigurationDTO;
import com.elasticpath.xpf.exception.InvalidPluginException;
import com.elasticpath.xpf.impl.XPFExtensionSelectorAny;
import com.elasticpath.xpf.impl.XPFInMemoryExtensionResolverImpl;
import com.elasticpath.xpf.util.XPFUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;
import static org.junit.Assert.assertEquals;

public class ExternalPluginTest extends BasicSpringContextTest {
	private static final String EXTENSION_CLASS = "com.elasticpath.plugins.ExternalTestSystemInformationImpl$TestSystemInformation";
	private static final String PLUGIN_VERSION = "ep-testPlugin-0.0.1";
	private static final String PLUGIN_VERSION_2 = "ep-testPlugin-0.0.2";
	private static final int PRIORITY = 0;
	private static final String PLUGIN_MESSAGE = "External test plugin!";
	private static final String STARTUP_PLUGIN_MESSAGE = "External startup test plugin!";

	private static final String COMPLETE = "complete";
	private static final String REPLACED_PLUGIN_MESSAGE = "External replaced test plugin!";

	private static String EXTERNAL_PLUGIN = "xpf-external-plugin-%s.jar";
	private static String REPLACED_EXTERNAL_PLUGIN = "xpf-external-replaced-plugin-%s.jar";

	@Autowired
	private XPFPluginFactory xpfPluginFactory;

	@Autowired
	private XPFInMemoryExtensionResolverImpl resolver;

	@Autowired
	private XPFExtensionLookup extensionLookup;

	@Autowired
	private XPFConfigurationLoader configurationLoader;

	@Before
	public void setUp() throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("properties-from-pom.properties");
		Properties p = new Properties();
		p.load(is);
		EXTERNAL_PLUGIN = String.format(EXTERNAL_PLUGIN, p.getProperty("version"));
		REPLACED_EXTERNAL_PLUGIN = String.format(REPLACED_EXTERNAL_PLUGIN, p.getProperty("version"));
	}

	@Test
	@SuppressWarnings("PMD.PrematureDeclaration")
	public void testThatExternalPluginIsLoadedAndExtensionIsAssigned() throws URISyntaxException {
		Callable<String> extensionAssigner = () -> {
			loadExternalPlugin();

			return COMPLETE;
		};

		Callable<String> extensionChecker = () -> {
			List<SystemInformation> systemInfoExtensions;
			do {
				systemInfoExtensions = extractSystemInformation();
			} while (systemInfoExtensions.stream().map(SystemInformation::getSimpleValue).noneMatch(PLUGIN_MESSAGE::equals));

			return COMPLETE;
		};

		startAndCheck(extensionAssigner, extensionChecker);
		xpfPluginFactory.unloadPlugin(PLUGIN_VERSION);
	}

	@Test
	@SuppressWarnings("PMD.PrematureDeclaration")
	public void testThatExternalPluginIsReplacedAndExtensionIsReassigned() throws URISyntaxException {
		loadExternalPlugin();

		Callable<String> extensionChecker = () -> {
			List<SystemInformation> systemInfoExtensions;
			do {
				systemInfoExtensions = extractSystemInformation();
				checkThatExternalExtensionAssignedAllTime(systemInfoExtensions);
			}
			while (systemInfoExtensions.stream().map(SystemInformation::getSimpleValue).noneMatch(REPLACED_PLUGIN_MESSAGE::equals));

			return COMPLETE;
		};

		Callable<String> extensionAssigner = () -> {
			xpfPluginFactory.replacePlugin(PLUGIN_VERSION, getPluginURI(REPLACED_EXTERNAL_PLUGIN));

			return COMPLETE;
		};

		startAndCheck(extensionChecker, extensionAssigner);
		xpfPluginFactory.unloadPlugin(PLUGIN_VERSION_2);
	}

	@Test
	@SuppressWarnings("PMD.PrematureDeclaration")
	public void testThatExternalPluginIsUnloaded() throws URISyntaxException {
		loadExternalPlugin();

		Callable<String> extensionChecker = () -> {
			List<SystemInformation> systemInfoExtensions;
			do {
				systemInfoExtensions = extractSystemInformation();

			} while (systemInfoExtensions.stream().map(SystemInformation::getSimpleValue).anyMatch(PLUGIN_MESSAGE::equals));

			return COMPLETE;
		};


		Callable<String> extensionUnloader = () -> {
			xpfPluginFactory.unloadPlugin(PLUGIN_VERSION);

			return COMPLETE;
		};

		startAndCheck(extensionChecker, extensionUnloader);

	}

	@Test
	@SuppressWarnings("PMD.PrematureDeclaration")
	public void testThatExternalPluginIsLoadedOnStartup() {

		Callable<String> extensionChecker = () -> {
			List<SystemInformation> systemInfoExtensions;
			do {
				systemInfoExtensions = extractSystemInformation();
			} while (systemInfoExtensions.stream().map(SystemInformation::getSimpleValue).noneMatch(STARTUP_PLUGIN_MESSAGE::equals));

			return COMPLETE;
		};

		startAndCheck(extensionChecker, () -> COMPLETE);
	}

	@Test
	@SuppressWarnings("PMD.PrematureDeclaration")
	public void testThatExternalPluginSettingsAreLoadedOnStartup() throws URISyntaxException {
		XPFPluginConfigurationDTO pluginConfiguration =
				configurationLoader.getPluginConfigurationMap().get("ep-externalTestPlugin-" + EXTERNAL_PLUGIN);

		PluginSettingDTO dto = pluginConfiguration.getPluginSettings().get(0);
		assertEquals("SAMPLE_MAP", dto.getSettingKey());
		assertEquals(SettingCollectionTypeDTO.MAP, dto.getCollectionType());
		assertEquals(SettingDataTypeDTO.INTEGER, dto.getDataType());
		assertEquals(99, ((Map) dto.getValues()).get("NUMBER_OF_PUPPIES"));
	}

	@Test
	@SuppressWarnings("PMD.PrematureDeclaration")
	public void testThatExternalExtensionSettingsAreLoadedOnStartup() {
		Set<PluginSettingDTO> settings = configurationLoader.getExtensionPointConfigurationMap()
				.get(XPFExtensionPointEnum.SYSTEM_INFORMATION)
				.stream()
				.filter(config -> config.getExtensionClassName()
						.equals("com.elasticpath.plugins.ExternalDefaultTestSystemInformationImpl$TestSystemInformation"))
				.map(ExtensionPointConfigurationDTO::getExtensionSettings)
				.findFirst()
				.orElse(Collections.emptySet());

		assertEquals(1, settings.size());
		PluginSettingDTO dto = settings.stream().findFirst().orElse(null);

		assertEquals("SAMPLE_MAP", dto.getSettingKey());
		assertEquals(SettingDataTypeDTO.INTEGER, dto.getDataType());
		assertEquals(SettingCollectionTypeDTO.MAP, dto.getCollectionType());
		assertEquals(2345, ((Map) dto.getValues()).get("mobee"));

	}

	private void loadExternalPlugin() throws URISyntaxException {
		xpfPluginFactory.loadPlugin(getPluginURI(EXTERNAL_PLUGIN));
		resolver.assignExtensionToSelector(EXTENSION_CLASS, PLUGIN_VERSION, XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorAny(), PRIORITY);

		//make sure that external extension was assigned
		await().atMost(TEN_SECONDS)
				.until(() -> extractSystemInformation().stream()
						.map(SystemInformation::getSimpleValue).anyMatch(PLUGIN_MESSAGE::equals));
	}

	private void checkThatExternalExtensionAssignedAllTime(List<SystemInformation> systemInfoExtensions) {
		if (systemInfoExtensions.stream()
				.map(systemInfoExtension -> XPFUtils.getProxiedExtensionClass(systemInfoExtension).getName())
				.noneMatch(EXTENSION_CLASS::equals)) {
			throw new InvalidPluginException("No external extension");
		}
	}

	private URI getPluginURI(final String plugin) throws URISyntaxException {
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		return loader.getResource(plugin).toURI();
	}

	private void startAndCheck(final Callable<String> first, final Callable<String> second) {
		ExecutorService executor = Executors.newFixedThreadPool(2);

		Future<String> futureFirst = executor.submit(first);
		Future<String> futureSecond = executor.submit(second);

		await().atMost(TEN_SECONDS).until(futureFirst::isDone);
		await().atMost(TEN_SECONDS).until(futureSecond::isDone);

		Assertions.assertThatCode(futureSecond::get).doesNotThrowAnyException();
		Assertions.assertThatCode(futureFirst::get).doesNotThrowAnyException();
	}

	private List<SystemInformation> extractSystemInformation() {
		return extensionLookup.getMultipleExtensions(SystemInformation.class,
				XPFExtensionPointEnum.SYSTEM_INFORMATION,
				new XPFExtensionSelectorAny());
	}
}


