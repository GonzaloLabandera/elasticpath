/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.XPFExtensionResolver;
import com.elasticpath.xpf.XPFExtensionSelector;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;
import com.elasticpath.xpf.connectivity.extensionpoint.SystemInformation;
import com.elasticpath.xpf.exception.InvalidExtensionException;
import com.elasticpath.xpf.exception.InvalidPluginException;
import com.elasticpath.xpf.exception.SchemaNotSupportedException;
import com.elasticpath.xpf.impl.test_extension.SomeExtensionClass;

@RunWith(MockitoJUnitRunner.class)
public class XPFPluginFactoryTest {
	private static final String PLUGIN_ID = "test-id";
	private static final String REPLACED_PLUGIN_ID = "test-replaced-id";

	private static final String PLUGIN_PATH = "file:///test-plugin.jar";
	private static final String REPLACED_PLUGIN_PATH = "file:///test-replaced-plugin.jar";

	@Mock
	private XPFPluginManager pluginManager;
	@Mock
	private XPFExtensionResolver extensionPointResolver;
	@Mock
	private SystemInformation firstExtension;
	@Mock
	private ProductSkuValidator secondExtension;
	@Mock
	private SystemInformation replaced;
	@Mock
	private BeanFactory beanFactory;

	@InjectMocks
	private XPFPluginFactoryImpl xpfPluginFactory;

	@Before
	public void setUp() {
		when(beanFactory.getSingletonBean("pluginManager", XPFPluginManager.class)).thenReturn(pluginManager);
		when(beanFactory.getSingletonBean("xpfExtensionsResolver", XPFExtensionResolver.class)).thenReturn(extensionPointResolver);
	}

	@Test
	public void testInit() {
		xpfPluginFactory.init();

		verify(pluginManager).startPlugins();
		verify(pluginManager).loadPlugins();
	}

	@Test
	public void testThatFactoryLoadNewPluginAndNotAssignsExtensions() throws URISyntaxException {
		URI uri = new URI(PLUGIN_PATH);
		when(pluginManager.loadPlugin(Paths.get(uri))).thenReturn(PLUGIN_ID);

		xpfPluginFactory.loadPlugin(uri);

		verify(pluginManager).startPlugin(PLUGIN_ID);
		verify(pluginManager).loadPlugin(Paths.get(uri));
		verify(extensionPointResolver, never()).assignExtensionToSelector(anyString(), anyString(), any(XPFExtensionPointEnum.class),
				any(XPFExtensionSelector.class), anyInt());
	}

	@Test
	public void testThatStopAndUnloadPluginInCaseExceptionWhenPluginStarts() throws URISyntaxException {
		URI uri = new URI(PLUGIN_PATH);
		when(pluginManager.loadPlugin(Paths.get(uri))).thenReturn(PLUGIN_ID);
		when(pluginManager.startPlugin(PLUGIN_ID)).thenThrow(new RuntimeException());

		assertThatThrownBy(() -> xpfPluginFactory.loadPlugin(uri)).isInstanceOf(RuntimeException.class);

		verify(pluginManager).unloadPlugin(PLUGIN_ID);
	}

	@Test
	public void testThatStopAndUnloadPluginInCaseExceptionWhenPluginUnload() throws URISyntaxException {
		URI uri = new URI(PLUGIN_PATH);
		when(pluginManager.loadPlugin(Paths.get(uri))).thenReturn(PLUGIN_ID);
		when(pluginManager.startPlugin(PLUGIN_ID)).thenThrow(new RuntimeException());
		when(pluginManager.unloadPlugin(PLUGIN_ID)).thenThrow(new RuntimeException());

		assertThatThrownBy(() -> xpfPluginFactory.loadPlugin(uri)).isInstanceOf(InvalidPluginException.class)
				.hasMessageContaining("It's impossible to unload invalid plugin");

		verify(pluginManager).unloadPlugin(PLUGIN_ID);
	}

	@Test
	public void testThatFactoryUnloadOldPluginAndUnassignsExtensions() throws URISyntaxException {
		URI uri = new URI(PLUGIN_PATH);
		when(pluginManager.loadPlugin(Paths.get(uri))).thenReturn(PLUGIN_ID);
		when(pluginManager.getExtensionClasses(PLUGIN_ID)).thenReturn(Collections.singletonList(firstExtension.getClass()));
		xpfPluginFactory.loadPlugin(uri);
		xpfPluginFactory.unloadPlugin(PLUGIN_ID);

		verify(extensionPointResolver).removeExtensionFromSelector(eq(firstExtension.getClass().getName()), eq(PLUGIN_ID),
				eq(XPFExtensionPointEnum.SYSTEM_INFORMATION), any(XPFExtensionSelectorAny.class));
		verify(pluginManager).stopPlugin(PLUGIN_ID);
		verify(pluginManager).unloadPlugin(PLUGIN_ID);
	}

	@Test
	public void testThatFactorySupportOnlyFileSchema() throws URISyntaxException {
		URI unsupportedURI = new URI("http://somehost:80");
		String errorMessage = "http is not supported. URI \"http://somehost:80\" must start with \"file:\".";

		assertThatThrownBy(() -> xpfPluginFactory.loadPlugin(unsupportedURI)).isInstanceOf(SchemaNotSupportedException.class)
				.hasMessageContaining(errorMessage);
		assertThatThrownBy(() -> xpfPluginFactory.replacePlugin(null, unsupportedURI)).isInstanceOf(SchemaNotSupportedException.class)
				.hasMessageContaining(errorMessage);
	}

	@Test
	public void testThatFactoryStopsNewPluginInCaseExceptionAndKeepsOnlyOldExtensions() throws URISyntaxException {
		URI uri = new URI(PLUGIN_PATH);
		URI replacedURI = new URI(REPLACED_PLUGIN_PATH);
		SomeExtensionClass replaced = new SomeExtensionClass();

		when(pluginManager.loadPlugin(Paths.get(uri))).thenReturn(PLUGIN_ID);
		when(pluginManager.loadPlugin(Paths.get(replacedURI))).thenReturn(REPLACED_PLUGIN_ID);

		when(pluginManager.getExtensionClasses(PLUGIN_ID)).thenReturn(Collections.singletonList(firstExtension.getClass()));
		when(pluginManager.getExtensionClasses(REPLACED_PLUGIN_ID)).thenReturn(Collections.singletonList(replaced.getClass()));
		//load old plugin
		xpfPluginFactory.loadPlugin(uri);

		assertThatThrownBy(() -> xpfPluginFactory.replacePlugin(PLUGIN_ID, replacedURI)).isInstanceOf(InvalidExtensionException.class)
				.hasMessageContaining("Not found extension points for com.elasticpath.xpf.impl.test_extension.SomeExtensionClass in old plugin");

		//check that old plugin loaded
		verify(pluginManager).startPlugin(PLUGIN_ID);
		verify(pluginManager).loadPlugin(Paths.get(uri));

		//check replace

		//stop and unload new plugin
		verify(pluginManager).stopPlugin(REPLACED_PLUGIN_ID);
		verify(pluginManager).unloadPlugin(REPLACED_PLUGIN_ID);

		//old plugin was not unload and stop
		verify(pluginManager, never()).stopPlugin(PLUGIN_ID);
		verify(pluginManager, never()).unloadPlugin(PLUGIN_ID);

		//extensions were not unassigned
		verify(extensionPointResolver, never()).removeExtensionFromSelector(anyString(), anyString(), any(XPFExtensionPointEnum.class),
				any(XPFExtensionSelector.class));
	}

	@Test
	public void testThatFactoryReplacePlugins() throws URISyntaxException {
		URI uri = new URI(PLUGIN_PATH);
		URI replacedURI = new URI(REPLACED_PLUGIN_PATH);

		when(pluginManager.loadPlugin(Paths.get(uri))).thenReturn(PLUGIN_ID);
		when(pluginManager.loadPlugin(Paths.get(replacedURI))).thenReturn(REPLACED_PLUGIN_ID);

		when(pluginManager.getExtensionClasses(PLUGIN_ID)).thenReturn(Collections.singletonList(firstExtension.getClass()));
		when(pluginManager.getExtensionClasses(REPLACED_PLUGIN_ID)).thenReturn(Collections.singletonList(replaced.getClass()));
		//load old plugin
		xpfPluginFactory.loadPlugin(uri);

		xpfPluginFactory.replacePlugin(PLUGIN_ID, replacedURI);

		//check that old plugin loaded
		verify(pluginManager).startPlugin(PLUGIN_ID);
		verify(pluginManager).loadPlugin(Paths.get(uri));

		//check replace

		//start new plugin
		verify(pluginManager).startPlugin(REPLACED_PLUGIN_ID);
		verify(pluginManager).loadPlugin(Paths.get(replacedURI));

		extensionPointResolver.updatePluginId(PLUGIN_ID, REPLACED_PLUGIN_ID);

		//stop and unload old plugin
		verify(pluginManager).stopPlugin(PLUGIN_ID);
		verify(pluginManager).unloadPlugin(PLUGIN_ID);
	}

	@Test
	public void testThatFactoryStopsNewPluginWhenNewPluginHasNoExtensionsThatWereAlreadyAssignedInOldPlugin() throws URISyntaxException {
		URI uri = new URI(PLUGIN_PATH);
		URI replacedURI = new URI(REPLACED_PLUGIN_PATH);

		when(pluginManager.loadPlugin(Paths.get(uri))).thenReturn(PLUGIN_ID);
		when(pluginManager.loadPlugin(Paths.get(replacedURI))).thenReturn(REPLACED_PLUGIN_ID);

		when(pluginManager.getExtensionClasses(PLUGIN_ID)).thenReturn(Arrays.asList(firstExtension.getClass(), secondExtension.getClass()));
		when(pluginManager.getExtensionClasses(REPLACED_PLUGIN_ID)).thenReturn(Collections.singletonList(replaced.getClass()));
		when(extensionPointResolver.getAssignedExtensionClassNames(XPFExtensionPointEnum.SYSTEM_INFORMATION, PLUGIN_ID))
				.thenReturn(Collections.emptyList());
		when(extensionPointResolver.getAssignedExtensionClassNames(XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_CHECKOUT, PLUGIN_ID))
				.thenReturn(Collections.singletonList(secondExtension.getClass().getName()));
		//load old plugin
		xpfPluginFactory.loadPlugin(uri);

		assertThatThrownBy(() -> xpfPluginFactory.replacePlugin(PLUGIN_ID, replacedURI)).isInstanceOf(InvalidExtensionException.class)
				.hasMessageStartingWith("Extension com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator$MockitoMock")
				.hasMessageEndingWith("was not found in the replacement plugin");

		//check that old plugin loaded
		verify(pluginManager).startPlugin(PLUGIN_ID);
		verify(pluginManager).loadPlugin(Paths.get(uri));

		//check replace

		//stop and unload new plugin
		verify(pluginManager).stopPlugin(REPLACED_PLUGIN_ID);
		verify(pluginManager).unloadPlugin(REPLACED_PLUGIN_ID);

		//old plugin was not unload and stop
		verify(pluginManager, never()).stopPlugin(PLUGIN_ID);
		verify(pluginManager, never()).unloadPlugin(PLUGIN_ID);

		//extensions were not unassigned
		verify(extensionPointResolver, never()).removeExtensionFromSelector(anyString(), anyString(), any(XPFExtensionPointEnum.class),
				any(XPFExtensionSelector.class));
	}
}
