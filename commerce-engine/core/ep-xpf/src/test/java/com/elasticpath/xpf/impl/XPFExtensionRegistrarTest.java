/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;

@RunWith(MockitoJUnitRunner.class)
public class XPFExtensionRegistrarTest {

	@Mock
	private PluginManager pluginManager;

	@Mock
	private BeanDefinitionRegistry beanDefinitionRegistry;

	@InjectMocks
	private XPFExtensionRegistrar registrar;

	private List<Class<?>> extensions;

	@Before
	public void init() {
		extensions = new ArrayList<>(2);
		extensions.add(EmbeddedStubbdedExtension.class);
		extensions.add(StubbdedExtension.class);

		when(pluginManager.getExtensionClasses((String) null)).thenReturn(extensions);
	}

	@Test
	public void testPostProcessBeanDefinitionRegistry() {
		registrar.postProcessBeanDefinitionRegistry(beanDefinitionRegistry);
		BeanDefinition beanDefinition = createBeanDefinition();

		// only the embedded extension should be registered as a bean definition in Spring
		verify(beanDefinitionRegistry).registerBeanDefinition(eq(EmbeddedStubbdedExtension.class.getName()), eq(beanDefinition));
		verifyNoMoreInteractions(beanDefinitionRegistry);
	}

	private BeanDefinition createBeanDefinition() {
		BeanDefinition beanDefinition = new RootBeanDefinition(EmbeddedStubbdedExtension.class.getName());
		beanDefinition.setScope("prototype");
		return beanDefinition;
	}

	@XPFEmbedded
	@Extension
	public class EmbeddedStubbdedExtension extends XPFExtensionPointImpl implements ExtensionPoint {
	}

	@Extension
	public class StubbdedExtension extends XPFExtensionPointImpl implements ExtensionPoint {

	}
}
