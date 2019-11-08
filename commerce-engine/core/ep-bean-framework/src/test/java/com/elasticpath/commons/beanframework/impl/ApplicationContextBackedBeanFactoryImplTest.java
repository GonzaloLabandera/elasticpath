/*
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.commons.beanframework.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * Test for the ApplicationContextBackedBeanFactoryImpl that backs the ElasticPathImpl object.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationContextBackedBeanFactoryImplTest {

	private static final String TEST_BEAN_NAME = "bean";
	private static final Integer TEST_INSTANCE = 1;
	private static final Class<Integer> TEST_INSTANCE_CLASS = Integer.class;

	@Mock
	private ConfigurableApplicationContext mockApplicationContext;
	@Mock
	private ConfigurableListableBeanFactory mockFactory;
	@Mock
	private BeanDefinition mockBeanDefinition;

	/**
	 * Object under test.
	 */
	private ApplicationContextBackedBeanFactoryImpl factory;

	/**
	 * Set up the mock domain bean factory to help testing.
	 */
	@Before
	public void setUp() {
		factory = new ApplicationContextBackedBeanFactoryImpl();
		factory.setApplicationContext(mockApplicationContext);
		when(mockApplicationContext.getBeanFactory()).thenReturn(mockFactory);
		when(mockFactory.getBeanDefinition(TEST_BEAN_NAME)).thenReturn(mockBeanDefinition);
	}

	/**
	 * Test that a bean is retrieved correctly from the delegate.
	 */
	@Test
	@Deprecated
	public void testGetBeanForExistingBean() {
		when(mockApplicationContext.getBean(TEST_BEAN_NAME)).thenReturn(TEST_INSTANCE);
		Integer expected = factory.getBean(TEST_BEAN_NAME);
		assertThat(expected)
				.isSameAs(TEST_INSTANCE);
	}

	/**
	 * Test that a bean is retrieved correctly from the delegate.
	 */
	@Test
	public void testGetSingletonBeanForExistingBean() {
		when(mockApplicationContext.getBean(TEST_BEAN_NAME, Integer.class)).thenReturn(TEST_INSTANCE);
		when(mockBeanDefinition.isSingleton()).thenReturn(true);

		assertThat(factory.getSingletonBean(TEST_BEAN_NAME, Integer.class))
				.isSameAs(TEST_INSTANCE);
	}

	@Test
	public void testGetSingletonAssertsForPrototypeBean() {
		when(mockBeanDefinition.isSingleton()).thenReturn(false);
		assertThatThrownBy(() -> factory.getSingletonBean(TEST_BEAN_NAME, Integer.class))
				.isInstanceOf(AssertionError.class);
	}

	/**
	 * Test that a bean is retrieved correctly from the delegate.
	 */
	@Test
	public void testGetPrototypeBeanForExistingBean() {
		when(mockApplicationContext.getBean(TEST_BEAN_NAME, Integer.class)).thenReturn(TEST_INSTANCE);
		when(mockBeanDefinition.isPrototype()).thenReturn(true);

		assertThat(factory.getPrototypeBean(TEST_BEAN_NAME, Integer.class))
				.isSameAs(TEST_INSTANCE);
	}

	@Test
	public void testGetPrototypeAssertsForSingletonBean() {
		when(mockBeanDefinition.isPrototype()).thenReturn(false);
		assertThatThrownBy(() -> factory.getPrototypeBean(TEST_BEAN_NAME, Integer.class))
				.isInstanceOf(AssertionError.class);
	}

	/**
	 * Make sure we get 'null' for non-registered bean.
	 */
	@Test
	@Deprecated
	public void testGetBeanNonRegisteredBean() {
		when(mockApplicationContext.getBean(TEST_BEAN_NAME)).thenReturn(null);
		Integer expected = factory.getBean(TEST_BEAN_NAME);
		assertThat(expected)
				.isNull();
	}

	/**
	 * Test that getting an implementation class for a non-registered bean
	 * returns 'null'.
	 */
	@Test
	public void testGetBeanImplClassNonRegisteredBean() {
		when(mockApplicationContext.getType(TEST_BEAN_NAME)).thenReturn(null);
		assertThat(factory.getBeanImplClass(TEST_BEAN_NAME))
				.as("Should get null for a non-registered object.")
				.isNull();
	}

	/**
	 * Test that a non-prototype bean has it's class returned correctly.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testGetBeanImplClassRegisteredNonPrototypeBean() {
		when((Class<Integer>) mockApplicationContext.getType(TEST_BEAN_NAME)).thenReturn(TEST_INSTANCE_CLASS);
		assertThat(factory.getBeanImplClass(TEST_BEAN_NAME))
				.isEqualTo(Integer.class);
	}
}
