/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.contentspace.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.contentspace.Parameter;
import com.elasticpath.domain.contentspace.ParameterLocaleDependantValue;

/**
 * A test case for {@link ParameterValueImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParameterValueImplTest {
	
	private static final String TEST_VALUE = "test2";
	private ParameterValueImpl parameterValue;

	@Mock
	private Parameter userInputParameter;

	@Mock
	private BeanFactory beanFactory;

	/**
	 * Sets up the test case.
	 * 
	 * @throws Exception on error
	 */
	@Before
	public void setUp() throws Exception {

		userInputParameter = mock(Parameter.class, "userInputParameter");
		
		parameterValue = new ParameterValueImpl() {
			private static final long serialVersionUID = 8108527056961199725L;

			@Override
			protected ParameterLocaleDependantValue getNewParameterLocaleDependantValue() {
				return new ParameterLocaleDependantValueImpl();
			}

			@Override
			public <T> T getBean(final String beanName) {
				return beanFactory.getBean(beanName);
			}
		};
	}

	/**
	 * Tests that even if the parameter value does not have associated 
	 * Parameter instance the result could still be retrieved.
	 */
	@Test
	public void testSetParameterValueNoParameter() {

		when(beanFactory.getBean(ContextIdNames.DYNAMIC_CONTENT_WRAPPER_USER_INPUT_PARAMETER)).thenReturn(userInputParameter);
		when(userInputParameter.isLocalizable()).thenReturn(false);

		parameterValue.setValue("test123", "en");
		assertThat(parameterValue.getValue("en")).isEqualTo("test123");
		verify(beanFactory).getBean(ContextIdNames.DYNAMIC_CONTENT_WRAPPER_USER_INPUT_PARAMETER);
		verify(userInputParameter).setParameterId(null);
		verify(userInputParameter).setDescription(null);
		verify(userInputParameter).setLocalizable(false);
	}
	
	/**
	 * Test that if a localized value exists then 
	 * it should be able to be retrieved. 
	 */
	@Test
	public void testGetSetValueWithSpecifiedLanguage() {
		when(userInputParameter.isLocalizable()).thenReturn(true);

		parameterValue.setParameter(userInputParameter);

		String locale = "en";
		String value = "testvalue";
		parameterValue.setValue(value, locale);

		assertThat(parameterValue.getValue(locale)).isEqualTo(value);
	}

	/**
	 * Test that even when setting the value with a null language
	 * the value could be retrieved with the same value of the language.
	 */
	@Test
	public void testGetSetValueNullLanguage() {
		when(userInputParameter.isLocalizable()).thenReturn(true);

		parameterValue.setParameter(userInputParameter);

		String locale = null;
		String value = "testvalue";
		parameterValue.setValue(value, locale);

		assertThat(parameterValue.getValue(locale)).isEqualTo(value);
	}


	/**
	 * Test that when setting the value of a non-localized parameter
	 * the value could be retrieved properly.
	 */
	@Test
	public void testGetSetValueNullLanguageNonLocalizedParameter() {
		when(userInputParameter.isLocalizable()).thenReturn(false);

		parameterValue.setParameter(userInputParameter);
		
		String locale = null;
		String value = "test";
		parameterValue.setValue(value, locale);
		
		assertThat(parameterValue.getValue(locale)).isEqualTo(value);
	}

	/**
	 * Test that non required and non localizable parameter without value, return empty string
	 * instead null.
	 */
	@Test
	public void testGetEmptyValueFromNonRequeredNonLocalizableParameter() {
		when(userInputParameter.isLocalizable()).thenReturn(false);
		when(userInputParameter.isRequired()).thenReturn(false);

		parameterValue.setParameter(userInputParameter);
		
		assertThat(parameterValue.getValue(null)).isBlank();
		
	}
	
	/**
	 * Test that required and non localizable parameter without value, return null 
	 * instead empty string.
	 */
	@Test
	public void testGetNullFromNonRequeredNonLocalizableParameter() {
		
		when(userInputParameter.isLocalizable()).thenReturn(false);
		when(userInputParameter.isRequired()).thenReturn(true);

		parameterValue.setParameter(userInputParameter);
		assertThat(parameterValue.getValue(null)).isNull();
		
	}
	
	
	/**
	 * Test that non required and localizable parameter without value, return empty string
	 * instead null.
	 */
	@Test
	public void testGetEmptyValueFromNonRequeredLocalizableParameter() {
		
		when(userInputParameter.isLocalizable()).thenReturn(true);
		when(userInputParameter.isRequired()).thenReturn(false);
		parameterValue.setParameter(userInputParameter);
		
		assertThat(parameterValue.getValue(null)).isEmpty();
		
	}
	
	
	/**
	 * Test that non required and non localizable parameter with value, 
	 * return value  instead empty string. 
	 */
	@Test
	public void testGetValueFromNonRequeredNonLocalizableParameter() {
		
		when(userInputParameter.isLocalizable()).thenReturn(false);

		parameterValue.setParameter(userInputParameter);
		
		parameterValue.setValue("value", null);
		assertThat(parameterValue.getValue(null)).isEqualTo("value");
	}
	
	/**
	 * Test that non required and localizable parameter with value, 
	 * return value, depends from locale,  instead empty string. 
	 */
	@Test
	public void testGetValueFromNonRequeredLocalizableParameter() {
		
		when(userInputParameter.isLocalizable()).thenReturn(true);

		parameterValue.setParameter(userInputParameter);
		
		parameterValue.setValue("value_en", "en");
		parameterValue.setValue("value_de", "de");
		assertThat(parameterValue.getValue("en")).isEqualTo("value_en");
		assertThat(parameterValue.getValue("de")).isEqualTo("value_de");
		
	}
	
	

	
	/**
	 * Test that when setting the value of a non-localized parameter
	 * multiple times with different languages the value still 
	 * could be retrieved properly and should equal to the last time
	 * it was invoked.
	 */
	@Test
	public void testGetSetValueNonLocalizedParameter() {
		
		when(userInputParameter.isLocalizable()).thenReturn(false);

		parameterValue.setParameter(userInputParameter);
		
		String locale = "de";
		String value = "test";
		parameterValue.setValue(value, locale);
		
		assertThat(parameterValue.getValue(locale)).isEqualTo(value);
		
		parameterValue.setValue(TEST_VALUE, "en");
		
		assertThat(parameterValue.getValue(null)).isEqualTo(TEST_VALUE);
		assertThat(parameterValue.getValue("en")).isEqualTo(TEST_VALUE);
		assertThat(parameterValue.getValue("de")).isEqualTo(TEST_VALUE);
	}

	/**
	 * Tests that toString() works as expected.
	 * Calling toString() without a parameter assigned should be error prone.
	 */
	@Test
	public void testToString() {

		when(beanFactory.getBean(ContextIdNames.DYNAMIC_CONTENT_WRAPPER_USER_INPUT_PARAMETER)).thenReturn(userInputParameter);

		parameterValue.setParameter(null);

		assertThat(parameterValue.toString()).isNotNull();

		parameterValue.setParameter(new TemplateParameterImpl());

		assertThat(parameterValue.toString()).isNotNull();

		verify(beanFactory).getBean(ContextIdNames.DYNAMIC_CONTENT_WRAPPER_USER_INPUT_PARAMETER);
		verify(userInputParameter).setParameterId(null);
		verify(userInputParameter).setDescription(null);
		verify(userInputParameter).setLocalizable(false);

	}
}
