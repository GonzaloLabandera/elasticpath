/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.util.List;

import org.pf4j.PluginManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

import com.elasticpath.xpf.annotations.XPFEmbedded;

/**
 * Scans all extension point implementations looking for the @XPFEmbeded annotation indicating that the extension point
 * implementation is provided by ep-commerce and should be allowed access to the spring context. Registers bean
 * definitions for all embedded extensions.
 */
public class XPFExtensionRegistrar implements BeanDefinitionRegistryPostProcessor {

	private PluginManager pluginManager;

	@Override
	public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
		List<Class<?>> extensions = pluginManager.getExtensionClasses((String) null);
		for (Class<?> extensionClass : extensions) {
			if (extensionClass.isAnnotationPresent(XPFEmbedded.class)) {
				BeanDefinition def = createBeanDefinition(extensionClass);
				def.setScope("prototype");

				beanDefinitionRegistry.registerBeanDefinition(extensionClass.getName(), def);
			}
		}
	}

	/**
	 * Creates a bean definition for the given class.
	 *
	 * @param extensionClass the class to create a bean definition for.
	 * @return the Spring bean definition
	 */
	protected BeanDefinition createBeanDefinition(final Class<?> extensionClass) {
		return new RootBeanDefinition(extensionClass);
	}

	@Override
	public void postProcessBeanFactory(final ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
		// nothing to do
	}

	public void setPluginManager(final PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

}
