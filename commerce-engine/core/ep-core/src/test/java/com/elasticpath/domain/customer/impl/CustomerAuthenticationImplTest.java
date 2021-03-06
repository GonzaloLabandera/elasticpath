/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.customer.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.commons.lang3.StringUtils;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.PasswordGenerator;
import com.elasticpath.domain.customer.CustomerAuthentication;
import com.elasticpath.test.BeanFactoryExpectationsFactory;

/**
 * Test that {@link CustomerAuthenticationImpl} behaves as expected.
 */
public class CustomerAuthenticationImplTest {

	private static final String CLEAR_TEXT_PASSWORD = "password";
	private static final String ENCODED_PASSWORD = "957278bbd71a7f7c9dc9853df31f6675b6671d0945aa8689c7abfb9287b293d0";

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	private PasswordEncoder passwordEncoder;
	private BeanFactory beanFactory;
	private BeanFactoryExpectationsFactory beanExpectations;

	private final CustomerAuthentication customerAuthentication = new CustomerAuthenticationImpl();

	/**
	 * Setup required for each test.
	 *
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		beanFactory = context.mock(BeanFactory.class);
		beanExpectations = new BeanFactoryExpectationsFactory(context, beanFactory);
		passwordEncoder = context.mock(PasswordEncoder.class);
	}

	@After
	public void tearDown() {
		beanExpectations.close();
	}

	/**
	 * Test that setting a clear text password creates a salt and uses it in the encoding.
	 */
	@Test
	public void testSetClearPassword() {
		beanExpectations.oneBeanFactoryGetSingletonBean(ContextIdNames.PASSWORDENCODER, PasswordEncoder.class, passwordEncoder);
		context.checking(new Expectations() {
			{
				oneOf(passwordEncoder).encode(CLEAR_TEXT_PASSWORD); will(returnValue(ENCODED_PASSWORD));
			}
		});
		customerAuthentication.setClearTextPassword(CLEAR_TEXT_PASSWORD);
		assertEquals("The clear text password should be as set", CLEAR_TEXT_PASSWORD, customerAuthentication.getClearTextPassword());
		assertEquals("The password should be set to the value returned by the encoder", ENCODED_PASSWORD, customerAuthentication.getPassword());
	}

	/**
	 * Test that setting an empty clear text password sets the password to null and doesn't call the encoder.
	 */
	@Test
	public void testSetEmptyPassword() {
		customerAuthentication.setClearTextPassword(StringUtils.EMPTY);
		context.checking(new Expectations() {
			{
				never(passwordEncoder);
			}
		});
		assertNull("The password should be null", customerAuthentication.getPassword());
	}

	/**
	 * Test that resetting a password calls the generator.
	 */
	@Test
	public void testResetPassword() {
		final PasswordGenerator passwordGenerator = context.mock(PasswordGenerator.class);
		final String randomPassword = "MyNtgYZf3U";

		beanExpectations.allowingBeanFactoryGetSingletonBean(ContextIdNames.PASSWORD_GENERATOR, PasswordGenerator.class, passwordGenerator);
		beanExpectations.oneBeanFactoryGetSingletonBean(ContextIdNames.PASSWORDENCODER, PasswordEncoder.class, passwordEncoder);
		context.checking(new Expectations() {
			{
				oneOf(passwordGenerator).getPassword(); will(returnValue(randomPassword));
				oneOf(passwordEncoder).encode(randomPassword); will(returnValue(ENCODED_PASSWORD));
			}
		});
		customerAuthentication.resetPassword();
		assertEquals("The clear text password should be the one generated", randomPassword, customerAuthentication.getClearTextPassword());
	}

}
