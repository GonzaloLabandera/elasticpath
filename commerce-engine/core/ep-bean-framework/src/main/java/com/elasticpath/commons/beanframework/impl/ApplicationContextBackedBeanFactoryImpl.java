/*
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.commons.beanframework.impl;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.SimpleAliasRegistry;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.commons.beanframework.BeanFactory;

/**
 * The default <code>BeanFactory</code> for ElasticPath which provides a wrapper around a
 * into a Spring <code>ApplicationContext</code> that makes the generics more convenient.
 */
public class ApplicationContextBackedBeanFactoryImpl implements BeanFactory, ApplicationContextAware {

	private ConfigurableApplicationContext applicationContext;

	@Override
	public <T> T getPrototypeBean(final String name, final Class<T> clazz) {
		
		// The getBeanDefinition method does not handle aliases, it simply looks up bean definitions by name. We must resolve the alias down 
		// to the aliased bean definition name. Note that this does not impact the 'getBean' lookup, just our validation of the bean definition as a 
		// singleton or prototype.
		final String beanName = transformedBeanName(name);
		
		if (applicationContext.getBeanFactory().getBeanDefinition(beanName).isPrototype()) {
			return applicationContext.getBean(name, clazz);
		} else {
			throw new IllegalArgumentException("Bean '" + name + "' is not a prototype.");
		}
	}

	@Override
	public <T> T getSingletonBean(final String name, final Class<T> clazz) {
		
		final String beanName = transformedBeanName(name);
		
		if (applicationContext.getBeanFactory().getBeanDefinition(beanName).isSingleton()) {
			return applicationContext.getBean(name, clazz);
		} else {
			throw new IllegalArgumentException("Bean '" + name + "' is not a singleton.");
		}
	}

	/**
	 * Get the bean with the given name from the Spring
	 * <code>ApplicationContext</code>.
	 *
	 * @param <T>  the expected Java type of the bean
	 * @param name the abstract name of the bean to find.
	 * @return instance of the bean with the given id, or null if the bean name
	 * @deprecated please use {@link ApplicationContextBackedBeanFactoryImpl#getPrototypeBean(String, Class)}
	 * or {@link ApplicationContextBackedBeanFactoryImpl#getSingletonBean(String, Class)}
	 * is not registered with Spring.
	 */
	@Override
	@Deprecated
	@SuppressWarnings("unchecked")
	public <T> T getBean(final String name) {
		return (T) applicationContext.getBean(name);
	}

	/**
	 * Returns the class registered with the specified <code>beanName</code>.
	 *
	 * @param <T>      the expected Java type of the bean corresponding to the given name
	 * @param beanName the bean name to get the associated class for.
	 * @return the implementation class registered against the
	 * <code>beanName</code>.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> Class<T> getBeanImplClass(final String beanName) {
		return (Class<T>) applicationContext.getType(beanName);
	}


	/**
	 * Set the web application context.
	 *
	 * @param context the web application context to set.
	 * @throws EpSystemException in case of error
	 */
	@Override
	public void setApplicationContext(final ApplicationContext context) {
		if (context == null) {
			throw new EpSystemException("ApplicationContext cannot be set to null.");
		}
		applicationContext = (ConfigurableApplicationContext) context;
	}
	
	/**
	 * Return the bean name, stripping out the factory dereference prefix if necessary, and resolving aliases to canonical names. 
	 * Note that this functionality is based upon {@link AbstractBeanFactory.transformedBeanName}.
	 * 
	 * @param name the user-specified name
	 * @return the transformed bean name
	 */
	private String transformedBeanName(final String name) {
		
		final SimpleAliasRegistry simpleAliasRegistry = (SimpleAliasRegistry) applicationContext.getBeanFactory();
		
		return simpleAliasRegistry.canonicalName(BeanFactoryUtils.transformedBeanName(name));
	}
	
}
