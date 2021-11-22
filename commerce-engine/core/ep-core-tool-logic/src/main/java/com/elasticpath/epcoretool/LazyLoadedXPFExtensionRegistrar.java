/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.epcoretool;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

import com.elasticpath.xpf.impl.XPFExtensionRegistrar;

/**
 * Lazy BeanDefinitionRegistryPostProcessor used to override the xpfExtensionRegistrar, preventing the addition of
 * eagerly-loaded bean definitions for plugin classes.
 * 
 * This is necessary due to peculiarities of the EP Core Tool specifically - some manual steps are taken in
 * EmbeddedEpCore.initializeBeanFactory to set most of the beans in the spring context to lazy init. Eager
 * initialization can interfere with the reconfiguration done in EmbeddedEpCore.wireDataSource.
 */
public class LazyLoadedXPFExtensionRegistrar extends XPFExtensionRegistrar {

	@Override
	protected BeanDefinition createBeanDefinition(final Class<?> extensionClass) {
		BeanDefinition beanDef = new RootBeanDefinition(extensionClass);
		beanDef.setLazyInit(true);
		
		return beanDef;
	}
}
