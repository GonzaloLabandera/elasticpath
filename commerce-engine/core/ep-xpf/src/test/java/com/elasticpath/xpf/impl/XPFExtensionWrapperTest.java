/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.pf4j.ExtensionDescriptor;
import org.pf4j.ExtensionWrapper;

import com.elasticpath.xpf.impl.test_extension.StubbedExtension;

@RunWith(MockitoJUnitRunner.class)
public class XPFExtensionWrapperTest {

	private static final String EXTENSION_GUID = "extensionGuid";

	@Test
	@SuppressWarnings("unchecked")
	public void testCreateExtension() {
		StubbedExtension extension = new StubbedExtension();
		ExtensionWrapper<StubbedExtension> wrapper = mock(ExtensionWrapper.class);
		when(wrapper.getDescriptor()).thenReturn(new ExtensionDescriptor(0, StubbedExtension.class));
		XPFSingletonExtensionFactory extensionFactory = mock(XPFSingletonExtensionFactory.class);
		when(extensionFactory.create(StubbedExtension.class, EXTENSION_GUID)).thenReturn(extension);

		XPFExtensionWrapper<StubbedExtension> xpfExtensionWrapper = new XPFExtensionWrapper<>(EXTENSION_GUID, wrapper, extensionFactory);
		StubbedExtension actual = xpfExtensionWrapper.getExtension();
		xpfExtensionWrapper.getExtension();

		assertEquals(extension, actual);
		verify(extensionFactory, times(1)).create(StubbedExtension.class, EXTENSION_GUID);
	}
}
