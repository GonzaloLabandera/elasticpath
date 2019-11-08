/*
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.commons.beanframework;


/**
 * Implementations can provide class instances based on an abstract name, which
 * allows BeanFactory clients to forego knowing the specific implementation of
 * they are dealing with - dealing with then through an interface or similar.
 */
public interface BeanFactory {

	/**
	 * Obtain a new instance of a Prototype Bean.
	 *
	 * @param name Bean name.
	 * @param clazz Bean interface.
	 * @param <T> Expected bean type.
	 * @return Bean instance.
	 */
	<T> T getPrototypeBean(String name, Class<T> clazz);

	/**
	 * Obtain the instance of a Singleton Bean.
	 *
	 * @param name Bean name.
	 * @param clazz Bean interface.
	 * @param <T> Expected bean type.
	 * @return Bean instance.
	 */
	<T> T getSingletonBean(String name, Class<T> clazz);

	/**
	 * Return an instance of the given bean name.
	 *
	 * @param <T> the type of the bean to return.
	 * @param name the 'friendly' name of the bean to return an instance of.
	 * @return an instance of the bean, or null if the bean name cannot be found.
	 * @deprecated This method does not provide enough safety for the consumer. Please use either {@link #getPrototypeBean(String, Class)}
	 *   or {@link #getSingletonBean(String, Class)} to fetch a Spring bean.
	 */
	@Deprecated
	<T> T getBean(String name);


	/**
	 * Return the <code>Class</code> object currently registered with the
	 * specified <code>beanName</code>.
	 *
	 * @param <T> the type of the bean to return.
	 * @param beanName the name of the bean to get the class for.
	 * @return the class object if the bean is registered, null otherwise.
	 */
	<T> Class<T> getBeanImplClass(String beanName);

}
