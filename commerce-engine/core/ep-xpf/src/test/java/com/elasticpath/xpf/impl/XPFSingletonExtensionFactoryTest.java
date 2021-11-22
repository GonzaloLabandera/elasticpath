/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.xpf.connectivity.entity.XPFPluginSetting;
import com.elasticpath.xpf.converters.XPFPluginSettingConverter;
import com.elasticpath.xpf.dto.PluginSettingDTO;
import com.elasticpath.xpf.impl.test_extension.StubbedEmbeddedExtension;
import com.elasticpath.xpf.impl.test_extension.StubbedExtension;

@RunWith(MockitoJUnitRunner.class)
public class XPFSingletonExtensionFactoryTest {

	private static final String EXTENSION_GUID = "extensionGuid";
	@Mock
	private BeanFactory beanFactory;

	@Mock
	private XPFPluginSettingConverter settingConverter;

	@InjectMocks
	private XPFSingletonExtensionFactory extensionFactory;

	@Test
	public void testCreateExtension() {
		extensionFactory.getSingletonCache().put(EXTENSION_GUID, new StubbedEmbeddedExtension());

		StubbedEmbeddedExtension createdExtension = extensionFactory.create(StubbedEmbeddedExtension.class, EXTENSION_GUID);
		assertEquals(StubbedEmbeddedExtension.class, createdExtension.getClass());
	}

	@Test
	public void testInitializeAndCacheEmbeddedExtension() {
		final StubbedEmbeddedExtension extension = mockEmbeddedExtension();

		extensionFactory.initializeAndCacheExtension(StubbedEmbeddedExtension.class, EXTENSION_GUID,
				Collections.emptySet());

		assertEquals(extension, extensionFactory.getSingletonCache().get(EXTENSION_GUID));
		assertEquals(EXTENSION_GUID, ((StubbedEmbeddedExtension) extensionFactory.getSingletonCache().get(EXTENSION_GUID)).getExtensionGuid());
	}

	@Test
	public void testInitializeAndCacheExtension() {
		final String settingKey = "settingKey";
		final PluginSettingDTO setting = mock(PluginSettingDTO.class);
		final XPFPluginSetting xpfSetting = mock(XPFPluginSetting.class);
		when(settingConverter.convert(setting)).thenReturn(xpfSetting);
		when(xpfSetting.getSettingKey()).thenReturn(settingKey);

		extensionFactory.initializeAndCacheExtension(StubbedExtension.class, EXTENSION_GUID,
				Collections.singleton(setting));

		assertEquals(StubbedExtension.class, extensionFactory.getSingletonCache().get(EXTENSION_GUID).getClass());
		assertEquals(EXTENSION_GUID, ((StubbedExtension) extensionFactory.getSingletonCache().get(EXTENSION_GUID)).getExtensionGuid());
		assertEquals(Collections.singletonMap(xpfSetting.getSettingKey(), xpfSetting),
				((StubbedExtension) extensionFactory.getSingletonCache().get(EXTENSION_GUID)).getSettings());
	}

	@Test
	public void testCreateWithInvalidExtension() {
		NotExtension result = extensionFactory.create(NotExtension.class, EXTENSION_GUID);
		assertNull("Nothing should be instantiated for a class that is not an extention.", result);
	}

	private StubbedEmbeddedExtension mockEmbeddedExtension() {
		final StubbedEmbeddedExtension extension = new StubbedEmbeddedExtension();
		when(beanFactory.getPrototypeBean(StubbedEmbeddedExtension.class.getName(), StubbedEmbeddedExtension.class))
				.thenReturn(extension);
		return extension;
	}


	public class NotExtension {
	}
}
