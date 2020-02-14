/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.core;

import com.elasticpath.commons.beanframework.BeanFactory;

/**
 * Thread-safe class for getting services from the Spring context.
 */
public final class BeanLocator {

	private static BeanFactory beanFactory;

	private BeanLocator() {
		//private constructor
	}


	/**
	 * Set Spring bean factory.
	 *
	 * @param springBeanFactory Spring bean factory.
	 */
	public static void setBeanFactory(final BeanFactory springBeanFactory) {
		beanFactory = springBeanFactory;
	}

	/**
	 * Get a service for given service name.
	 *
	 * @param serviceName the service name
	 * @param <T>         the type
	 * @return Spring bean
	 * @deprecated use getPrototypeBean or getSingletonBean methods
	 */
	@Deprecated
	public static <T> T getService(final String serviceName) {
		return (T) beanFactory.getBean(serviceName);
	}

	/**
	 * Get a prototype bean for given name.
	 *
	 * @param <T>   the type
	 * @param name  the bean name
	 * @param clazz the class
	 * @return Spring bean
	 */
	public static <T> T getPrototypeBean(final String name, final Class<T> clazz) {
		return beanFactory.getPrototypeBean(name, clazz);
	}

	/**
	 * Get a singleton bean for given name.
	 *
	 * @param <T>   the type
	 * @param name  the bean name
	 * @param clazz the class
	 * @return Spring bean
	 */
	public static <T> T getSingletonBean(final String name, final Class<T> clazz) {
		return beanFactory.getSingletonBean(name, clazz);
	}
}
