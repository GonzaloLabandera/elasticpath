/*
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.contentspace.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;

import com.google.common.collect.ImmutableList;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.cache.SimpleTimeoutCache;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.impl.PriceTierImpl;
import com.elasticpath.domain.contentspace.ContentWrapper;
import com.elasticpath.domain.contentspace.ContentWrapperService;
import com.elasticpath.domain.contentspace.Parameter;
import com.elasticpath.domain.contentspace.ParameterValue;
import com.elasticpath.service.contentspace.ParameterResolvingException;
import com.elasticpath.service.contentspace.ScriptEngine;
import com.elasticpath.service.contentspace.ScriptEngineFactory;

/**
 * Test for ParameterValueResolverImpl.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParameterValueResolverImplTest  {

	private static final String DOUBLE_VAR2 = "doubleVar2";
	private static final String DOUBLE_VAR1 = "doubleVar1";
	private static final String CODE = "code";
	private static final String PARAM4 = "param4";
	private static final String PARAM3 = "param3";
	private static final String GROOVY = "groovy";
	private static final String PARAM1 = "param1";
	private static final String VALUE1 = "value1";

	private static final String PRODUCT_CODE = "123456789";
	private static final int INT_NUMBER_20 = 20;
	private static final int INT_NUMBER_123 = 123;
	private static final int INT_NUMBER_3 = 3;
	private static final float FLOAT_NUMBER_2 = 2f;
	private static final float FLOAT_NUMBER_5 = 5f;

	private static final List<Parameter> NULL_PARAMETERS = null;

	private List<ParameterValue> parameterValues;
	private List<Parameter> userParameters;
	private List<Parameter> tempParameters;

	private ContentWrapper contentWrapper;
	private ScriptEngineFactory scriptEngineFactory;
	private ParameterValueResolverImpl resolver;

	@Mock
	private SimpleTimeoutCache<String, FutureTask<Script>> mockSimpleTimeoutCache;

	/**
	 * Set up the test case.
	 */
	@Before
	public void setUp() {
		resolver = new ParameterValueResolverImpl();
		
		scriptEngineFactory = new ScriptEngineFactory();

		final FutureTaskGroovyScriptEngineImpl futureTaskGroovyScriptEngineImp = new FutureTaskGroovyScriptEngineImpl();
		futureTaskGroovyScriptEngineImp.setGroovyScriptTimeoutCache(mockSimpleTimeoutCache);

		Map<String, ScriptEngine> engines = new HashMap<>();
		engines.put(GROOVY, futureTaskGroovyScriptEngineImp);
		scriptEngineFactory.setValues(engines);
	}
	
	/**
	 * test that template parameters and user settings are merging into 
	 * one list of parameters correctly.
	 * Test null template parameters and null user input parameters result in an empty list
	 */
	@Test
	public void testGetAllContentWrapperParametersAllNull() {

		final ContentWrapper contentWrapperNoParams = mock(ContentWrapper.class, "contentWrapperNoParams");
		when(contentWrapperNoParams.getTemplateParameters()).thenReturn(NULL_PARAMETERS);
		when(contentWrapperNoParams.getUserInputSettings()).thenReturn(NULL_PARAMETERS);
		List<Parameter> resultNullNull;

		resultNullNull = resolver.getAllContentWrapperParameters(contentWrapperNoParams);
		assertThat(resultNullNull).isEmpty();
	}
	
	/**
	 * test that template parameters and user settings are merging into 
	 * one list of parameters correctly.
	 * Test template parameters and null user input parameters result in list with template parameters
	 */
	@Test
	public void testGetAllContentWrapperParametersTemplateOnly() {
		final List<Parameter> withParameters = new ArrayList<>();
		final Parameter parameter = mock(Parameter.class);
		withParameters.add(parameter);

		final ContentWrapper contentWrapperTemplateOnlyParams = mock(ContentWrapper.class, "contentWrapperTemplateOnlyParams");
		when(contentWrapperTemplateOnlyParams.getTemplateParameters()).thenReturn(withParameters);
		when(contentWrapperTemplateOnlyParams.getUserInputSettings()).thenReturn(NULL_PARAMETERS);
		List<Parameter> resultTemplateOnly;

		resultTemplateOnly = resolver.getAllContentWrapperParameters(contentWrapperTemplateOnlyParams);
		assertThat(resultTemplateOnly).hasSize(1);
	}
	
	/**
	 * test that template parameters and user settings are merging into 
	 * one list of parameters correctly.
	 * Test null template parameters and user input parameters result in list with user input parameters 
	 */
	@Test
	public void testGetAllContentWrapperParametersUserInputOnly() {
		final List<Parameter> withParameters = new ArrayList<>();
		final Parameter parameter = mock(Parameter.class);
		withParameters.add(parameter);

		final ContentWrapper contentWrapperUserInputOnlyParams = mock(ContentWrapper.class, "contentWrapperUserInputOnlyParams");
		when(contentWrapperUserInputOnlyParams.getTemplateParameters()).thenReturn(NULL_PARAMETERS);
		when(contentWrapperUserInputOnlyParams.getUserInputSettings()).thenReturn(withParameters);
		List<Parameter> resultUserInputOnly;

		resultUserInputOnly = resolver.getAllContentWrapperParameters(contentWrapperUserInputOnlyParams);
		assertThat(resultUserInputOnly).hasSize(1);
	}
	
	/**
	 * test that template parameters and user settings are merging into 
	 * one list of parameters correctly.
	 * Test template parameters and user input parameters result in list with template and user input parameters 
	 */
	@Test
	public void testGetAllContentWrapperParametersBothOnly() {
		final List<Parameter> withParameters = new ArrayList<>();
		final Parameter parameter = mock(Parameter.class);
		withParameters.add(parameter);

		final ContentWrapper contentWrapperUserInputOnlyParams = mock(ContentWrapper.class, "contentWrapperUserInputOnlyParams");
		when(contentWrapperUserInputOnlyParams.getTemplateParameters()).thenReturn(withParameters);
		when(contentWrapperUserInputOnlyParams.getUserInputSettings()).thenReturn(withParameters);
		List<Parameter> resultUserInputOnly;

		resultUserInputOnly = resolver.getAllContentWrapperParameters(contentWrapperUserInputOnlyParams);
		assertThat(resultUserInputOnly).hasSize(2);
	}
	
	/**
	 * Test that if no parameters are supplied the result 
	 * will be an empty resolved parameter values map.
	 * 
	 * @throws ParameterResolvingException must not be raised
	 */
	@Test
	public void testResolveValuesNullArguments() throws ParameterResolvingException {
		final List<Parameter> parameters = null;
		final ContentWrapper contentWrapper = mock(ContentWrapper.class);
		when(contentWrapper.getTemplateParameters()).thenReturn(parameters);
		when(contentWrapper.getUserInputSettings()).thenReturn(parameters);
		Map<String, Object> result;

		result = resolver.resolveValues(contentWrapper, parameterValues, new HashMap<>(), null);
		assertThat(result).isEmpty();
	}

	/**
	 * Tests that if no lang and parameter values are provided but there is a parameter definition
	 * there will be a parameter with a null value.
	 * 
	 * @throws ParameterResolvingException should be thrown because parameters are required by default.
	 */
	@Test(expected = ParameterResolvingException.class)
	public void testResolveValuesNoLocaleNoParameterValues() throws ParameterResolvingException {
		String lang = null;

		final List<Parameter> withParameters = new ArrayList<>();
		final Parameter parameter = mock(Parameter.class);
		withParameters.add(parameter);

		final ContentWrapper contentWrapper = mock(ContentWrapper.class);
		when(parameter.getParameterId()).thenReturn(PARAM1);
		when(parameter.getScriptExpression()).thenReturn(null);
		when(parameter.isRequired()).thenReturn(true);

		when(contentWrapper.getTemplateParameters()).thenReturn(NULL_PARAMETERS);
		when(contentWrapper.getUserInputSettings()).thenReturn(withParameters);

		resolver.resolveValues(contentWrapper, parameterValues, new HashMap<>(), lang);

	}

	/**
	 * Tests that if parameter definition is not provided and parameters values exists 
	 * the result will be empty parameters value map. 
	 * @throws ParameterResolvingException should not be raised
	 */
	@Test
	public void testResolveValuesNoParameterDefinition() throws ParameterResolvingException {

		final String lang = "en";
		final ParameterValue parameterValue = mock(ParameterValue.class);

		final ContentWrapper contentWrapper = mock(ContentWrapper.class);

		when(contentWrapper.getTemplateParameters()).thenReturn(NULL_PARAMETERS);
		when(contentWrapper.getUserInputSettings()).thenReturn(NULL_PARAMETERS);

		Map<String, Object> result;

		List<ParameterValue> parameterValues = ImmutableList.of(parameterValue);

		result = resolver.resolveValues(contentWrapper, parameterValues, new HashMap<>(), lang);
		assertThat(result).isEmpty();
	}
	
	/**
	 * Test for resolve not localized parameters. I.e. provided lang is null 
	 * and result - resolved values for parameters.
	 * @throws ParameterResolvingException should not be thrown since parameters are not localizable by default.
	 */
	@Test
	public void testResolveValuesNoLocale() throws ParameterResolvingException {

		final String lang = null;
		final ParameterValue parameterValue = mock(ParameterValue.class);

		final Parameter parameter = mock(Parameter.class);
		List<ParameterValue> parameterValues = ImmutableList.of(parameterValue);
		final List<Parameter> parameters = ImmutableList.of(parameter);

		final ContentWrapper contentWrapper = mock(ContentWrapper.class);
		when(parameterValue.getValue(lang)).thenReturn(VALUE1);
		when(parameterValue.getParameterName()).thenReturn(PARAM1);
		when(parameter.getParameterId()).thenReturn(PARAM1);
		when(parameter.getScriptExpression()).thenReturn(null);
		when(parameter.isPassToTemplate()).thenReturn(true);

		when(contentWrapper.getTemplateParameters()).thenReturn(NULL_PARAMETERS);
		when(contentWrapper.getUserInputSettings()).thenReturn(parameters);
		Map<String, Object> result;

		result = resolver.resolveValues(contentWrapper, parameterValues, new HashMap<>(), lang);
		assertThat(result).containsOnly(entry(PARAM1, VALUE1));

	}
	
	/**
	 * Test for resolve localized parameters. I.e. provided lang is not null 
	 * and result - resolved values for parameters.
	 * @throws ParameterResolvingException should not be thrown
	 */
	@Test
	public void testResolveValuesWithLocale() throws ParameterResolvingException {

		final String lang = "en";
		final ParameterValue parameterValue = mock(ParameterValue.class);

		final Parameter parameter = mock(Parameter.class);
		final List<ParameterValue> parameterValues = ImmutableList.of(parameterValue);
		final List<Parameter> parameters = ImmutableList.of(parameter);

		final ContentWrapper contentWrapper = mock(ContentWrapper.class);
		when(parameterValue.getValue(lang)).thenReturn(VALUE1);
		when(parameterValue.getParameterName()).thenReturn(PARAM1);
		when(parameter.getParameterId()).thenReturn(PARAM1);
		when(parameter.getScriptExpression()).thenReturn(null);
		when(parameter.isPassToTemplate()).thenReturn(true);

		when(contentWrapper.getTemplateParameters()).thenReturn(NULL_PARAMETERS);
		when(contentWrapper.getUserInputSettings()).thenReturn(parameters);
		Map<String, Object> result;

		result = resolver.resolveValues(contentWrapper, parameterValues, new HashMap<>(), lang);
		assertThat(result).containsOnly(entry(PARAM1, VALUE1));
	}
	
	/**
	 * Test for resolve localized parameters with wrong lang. 
	 * I.e. values provided for one lang and not resolved for another lang. 
	 * Result - resolved values for parameters is null.
	 * @throws ParameterResolvingException should be thrown since no parameter vlaue for this locale is set
	 */
	@Test(expected = ParameterResolvingException.class)
	public void testResolveValuesWithWrongLocale() throws ParameterResolvingException {

		final String wrongLang = "pl";
		final ParameterValue parameterValue = mock(ParameterValue.class);

		final Parameter parameter = mock(Parameter.class);
		final List<ParameterValue> parameterValues = ImmutableList.of(parameterValue);
		final List<Parameter> parameters = ImmutableList.of(parameter);

		final ContentWrapper contentWrapper = mock(ContentWrapper.class);
		when(parameterValue.getValue(wrongLang)).thenReturn(null);
		when(parameterValue.getParameterName()).thenReturn(PARAM1);
		when(parameter.getParameterId()).thenReturn(PARAM1);
		when(parameter.getScriptExpression()).thenReturn(null);
		when(parameter.isRequired()).thenReturn(true);

		when(contentWrapper.getTemplateParameters()).thenReturn(NULL_PARAMETERS);
		when(contentWrapper.getUserInputSettings()).thenReturn(parameters);

		resolver.resolveValues(contentWrapper, parameterValues, new HashMap<>(), wrongLang);

	}

	/**
	 * Tests that the result empty map of resolved parameters if the arguments are null.
	 * @throws ParameterResolvingException should not be thrown since no parameters are set
	 */
	@Test
	public void testIsParametersResolvedWithEitherArgumentNull() throws ParameterResolvingException {
		final ContentWrapper contentWrapper = mock(ContentWrapper.class);
		when(contentWrapper.getTemplateParameters()).thenReturn(NULL_PARAMETERS);
		when(contentWrapper.getUserInputSettings()).thenReturn(NULL_PARAMETERS);

		assertThat(resolver.resolveValues(contentWrapper, new ArrayList<>(), new HashMap<>(), "pl")).isNotNull();
		assertThat(resolver.resolveValues(contentWrapper, null, new HashMap<>(), "pl")).isNotNull();

	}

	/**
	 * Tests to ensure that if the parameter definitions is empty, that the method returns
	 * true to indicate that the parameters have been resolved.
	 * @throws ParameterResolvingException should not be thrown since no parametrs are set
	 */
	@Test
	public void testParametersResolvedEmptyDefinitionsAndValues() throws ParameterResolvingException {
		final ContentWrapper contentWrapper = mock(ContentWrapper.class);
		when(contentWrapper.getTemplateParameters()).thenReturn(new ArrayList<>());
		when(contentWrapper.getUserInputSettings()).thenReturn(NULL_PARAMETERS);

		assertThat(resolver.resolveValues(contentWrapper, new ArrayList<>(), new HashMap<>(), "pl")).isNotNull();

	}
	
	/**
	 * Tests to ensure that if the parameter value is null for one of the matching parameter
	 * definitions for a content wrapper, that the method will return a false to indicate
	 * that all parameters have not been resolved. 
	 * @throws ParameterResolvingException must be thrown since value for one required parameter is null
	 */
	@Test(expected = ParameterResolvingException.class)
	public void testParametersResolvedWithNullParameterValue() throws ParameterResolvingException {

		final String lang = "pl";

		// create CW with two required parameters
		final Parameter param3 = mock(Parameter.class, PARAM3);
		final Parameter param4 = mock(Parameter.class, PARAM4);

		final List<Parameter> paramDefs = new ArrayList<>();
		paramDefs.add(param3);
		paramDefs.add(param4);


		// set one's value to null
		final ParameterValue paramValue3 = mock(ParameterValue.class, "paramValue3");
		// set one's value to a value		
		final ParameterValue paramValue4 = mock(ParameterValue.class, "paramValue4");

		List<ParameterValue> resolvedParameters = ImmutableList.of(paramValue3, paramValue4);
		final ContentWrapper contentWrapper = mock(ContentWrapper.class);
		when(contentWrapper.getTemplateParameters()).thenReturn(paramDefs);
		when(contentWrapper.getUserInputSettings()).thenReturn(NULL_PARAMETERS);

		when(paramValue3.getValue(lang)).thenReturn(null);
		when(paramValue3.getParameterName()).thenReturn(PARAM3);
		when(param3.getParameterId()).thenReturn(PARAM3);
		when(param3.getScriptExpression()).thenReturn(null);
		when(param3.isRequired()).thenReturn(true);

		when(paramValue4.getValue(lang)).thenReturn("value4");
		when(paramValue4.getParameterName()).thenReturn(PARAM4);
		when(param4.getParameterId()).thenReturn(PARAM4);
		when(param4.getScriptExpression()).thenReturn(null);

		resolver.resolveValues(contentWrapper, resolvedParameters, new HashMap<>(), lang);

	}
	
	/**
	 * Tests to ensure that if the parameter value has excess parameter values, and at least one
	 * parameter value for each parameter definition, that the method will return true indicating
	 * that all parameters have been resolved.
	 * @throws ParameterResolvingException must not be thrown since all parameters of CW are satisfied
	 */
	@Test
	public void testIsParameterResolvedWithExcessParameterValues() throws ParameterResolvingException {

		final String lang = "pl";

		// create CW with two required parameters
		final Parameter param3 = mock(Parameter.class, PARAM3);
		final Parameter param4 = mock(Parameter.class, PARAM4);

		// set one's value to null
		final ParameterValue paramValue3 = mock(ParameterValue.class, "paramValue3");
		// set one's value to a value		
		final ParameterValue paramValue4 = mock(ParameterValue.class, "paramValue4");
		final ParameterValue paramValue5 = mock(ParameterValue.class, "paramValue5");

		final List<Parameter> paramDefs = new ArrayList<>();
		paramDefs.add(param3);
		paramDefs.add(param4);

		final List<ParameterValue> resolvedParameters = ImmutableList.of(paramValue3, paramValue4, paramValue5);

		final ContentWrapper contentWrapper = mock(ContentWrapper.class);
		when(contentWrapper.getTemplateParameters()).thenReturn(paramDefs);
		when(contentWrapper.getUserInputSettings()).thenReturn(NULL_PARAMETERS);

		when(paramValue3.getValue(lang)).thenReturn("value3");
		when(paramValue3.getParameterName()).thenReturn(PARAM3);
		when(param3.getParameterId()).thenReturn(PARAM3);
		when(param3.getScriptExpression()).thenReturn(null);
		when(param3.isPassToTemplate()).thenReturn(true);

		when(paramValue4.getValue(lang)).thenReturn("value4");
		when(paramValue4.getParameterName()).thenReturn(PARAM4);
		when(param4.getParameterId()).thenReturn(PARAM4);
		when(param4.getScriptExpression()).thenReturn(null);
		when(param4.isPassToTemplate()).thenReturn(true);

		Map<String, Object> resolved = resolver.resolveValues(contentWrapper, resolvedParameters, new HashMap<>(), lang);

		assertThat(resolved).hasSize(2);
	}
	

	/**
	 * Fill parameter - value list.
	 */
	private void fillParameterValues(final String lang) {

		final Parameter parameterName = mock(Parameter.class, "name");
		final Parameter parameterImage = mock(Parameter.class, "image");
		final Parameter parameterPrice = mock(Parameter.class, "price");

		final ParameterValue parameterValueCode = mock(ParameterValue.class, "codeVal");
		final Parameter parameterCode = mock(Parameter.class, CODE);

		parameterValues = ImmutableList.of(parameterValueCode);
		userParameters = ImmutableList.of(parameterCode);
		tempParameters = ImmutableList.of(parameterName, parameterImage, parameterPrice);

		contentWrapper = mock(ContentWrapper.class);

		when(parameterName.getScriptExpression()).thenReturn("product.getDisplayName(locale)");

		when(parameterValueCode.getValue(lang)).thenReturn(PRODUCT_CODE);
		when(parameterValueCode.getParameterName()).thenReturn(CODE);
		when(parameterCode.getScriptExpression()).thenReturn(null);

	}
	
	/**
	 * Minimal groovy test.
	 */
	@Test
	public void testGroovy() {
		Binding binding = new Binding();
		binding.setVariable("foo", 2);
		GroovyShell shell = new GroovyShell(binding);

		Object value = shell.evaluate("x = 123; return foo * 10");
		assertThat(value).isEqualTo(INT_NUMBER_20);
		assertThat(binding.getVariable("x")).isEqualTo(INT_NUMBER_123);
	}

	/**
	 * Test for detect dynamic parameter values in list.
	 */
	@Test
	public void testIsDynamicParameterValuesPresent() {
		fillParameterValues("en");
		
		ParameterValueResolverImpl parameterValueResolverImpl = new ParameterValueResolverImpl();
		
		List<Parameter> all = new ArrayList<>();
		all.addAll(tempParameters);
		all.addAll(userParameters);
		
		assertThat(parameterValueResolverImpl.isDynamicParameterPresent(all)).isTrue();
		assertThat(parameterValueResolverImpl.isDynamicParameterPresent(tempParameters)).isTrue();
		assertThat(parameterValueResolverImpl.isDynamicParameterPresent(userParameters)).isFalse();
		
	}
	
	/**
	 * Test load static resolved values into the dynamic context.
	 */
	@Test
	public void testLoadStaticParameterValues() {
		fillParameterValues(null);
		
		ParameterValueResolverImpl parameterValueResolverImpl = new ParameterValueResolverImpl();
		
		Map<String, Object> binding = new HashMap<>();
		
		parameterValueResolverImpl.loadStaticParameterValues(parameterValues, null, binding);
		
		assertThat(binding.get(CODE)).isNotNull();

		
	}
	
	/**
	 * Test for load spring bean into groovy context.
	 */
	@Test
	public void testLoadSpringBeansIntoGroovyBinding() {

		Map<String, Object> binding = new HashMap<>();

		final ContentWrapperService service1 = mock(ContentWrapperService.class, "service1");

		final ContentWrapper contentWrapper = mock(ContentWrapper.class);

		when(contentWrapper.getServiceDefinitions()).thenReturn(ImmutableList.of(service1));
		when(service1.getName()).thenReturn("sName");
		when(service1.getValue()).thenReturn(ContextIdNames.PRICE_TIER);

		ParameterValueResolverImpl parameterValueResolverImpl = new ParameterValueResolverImpl() {
			@Override
			protected Object getServiceDefinitionBean(final String serviceBeanName) {
				return new PriceTierImpl();
			}
		};

		parameterValueResolverImpl.loadServiceDefinitions(contentWrapper, binding);

		assertThat(binding.get(service1.getName())).isNotNull();


	}
	
	/**
	 * Test for that init section in content wrapper is evaluated correctly and
	 * variables are preserved and accessible for use in template parameters.
	 * The below code is equivalent of the following content wrapper:
	 * 
	 * <pre>{@code
	 * <content-wrapper>
	 * 	<template-name>initSectionTest.vm</template-name>
	 * <wrapper-id>1003</wrapper-id>
	 * 	<wrapper-name>Init Sectin test</wrapper-name>
	 * 	<init>
	 * 		doubleVar1 = 1+1+1;
	 * 		doubleVar2 = Math.sqrt(4);
	 * 	</init>
	 * 	<template-parameters>
	 * 		<parameter-definition name="doubleVar1">
	 * 			<resolver>
	 * 				doubleVar1
	 * 			</resolver>
	 * 		</parameter-definition>
	 * 		<parameter-definition name="doubleVar2">
	 * 			<resolver>
	 * 				doubleVar2				
	 * 			</resolver>
	 * 		</parameter-definition>
	 * 		<parameter-definition name="rez">
	 * 			<resolver>
	 * 				doubleVar1 + doubleVar2
	 * 			</resolver>
	 * 		</parameter-definition>
	 * 	</template-parameters>
	 * </content-wrapper>
	 * }</pre>
	 * 
	 * @throws ParameterResolvingException should not be thrown
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testInitSectionAndDynamicValues() throws ParameterResolvingException {


		final Parameter parameterVar1 = mock(Parameter.class, DOUBLE_VAR1);
		final Parameter parameterVar2 = mock(Parameter.class, DOUBLE_VAR2);
		final Parameter parameterVar3 = mock(Parameter.class, "rez");

		final List<Parameter> parameters = ImmutableList.of(parameterVar1, parameterVar2, parameterVar3);

		final ContentWrapper contentWrapper = mock(ContentWrapper.class);
		final String script = "doubleVar1 = 1+1+1;\ndoubleVar2 = Math.sqrt(4);";
		final String script2 = "doubleVar1 + doubleVar2";

		when(contentWrapper.getTemplateParameters()).thenReturn(parameters);
		when(contentWrapper.getUserInputSettings()).thenReturn(NULL_PARAMETERS);
		when(contentWrapper.getInitSection()).thenReturn(script);
		when(contentWrapper.getScriptLanguage()).thenReturn(GROOVY);
		when(contentWrapper.getServiceDefinitions()).thenReturn(null);

		when(parameterVar1.getParameterId()).thenReturn(DOUBLE_VAR1);
		when(parameterVar1.getScriptExpression()).thenReturn(DOUBLE_VAR1);
		when(parameterVar1.isPassToTemplate()).thenReturn(true);

		when(parameterVar2.getParameterId()).thenReturn(DOUBLE_VAR2);
		when(parameterVar2.getScriptExpression()).thenReturn(DOUBLE_VAR2);
		when(parameterVar2.isPassToTemplate()).thenReturn(true);

		when(parameterVar3.getParameterId()).thenReturn("rez");
		when(parameterVar3.getScriptExpression()).thenReturn(script2);
		when(parameterVar3.isPassToTemplate()).thenReturn(true);

		when(mockSimpleTimeoutCache.get(script)).thenReturn(null);
		when(mockSimpleTimeoutCache.get(DOUBLE_VAR1)).thenReturn(null);
		when(mockSimpleTimeoutCache.get(DOUBLE_VAR2)).thenReturn(null);
		when(mockSimpleTimeoutCache.get(script2)).thenReturn(null);

		ParameterValueResolverImpl parameterValueResolverImpl = new ParameterValueResolverImpl();

		parameterValueResolverImpl.setScriptEngineFactory(scriptEngineFactory);

		Map<String, Object> resolvedValues;

		resolvedValues = parameterValueResolverImpl.resolveValues(
			contentWrapper,
			new ArrayList<>(),
			new HashMap<>(), null);

		assertThat(resolvedValues).containsOnly(
			entry(DOUBLE_VAR1, String.valueOf(INT_NUMBER_3)),
			entry(DOUBLE_VAR2, String.valueOf(FLOAT_NUMBER_2)),
			entry("rez", String.valueOf(FLOAT_NUMBER_5)));

		verify(mockSimpleTimeoutCache).get(script);
		verify(mockSimpleTimeoutCache).put(eq(script), any(FutureTask.class));
		verify(mockSimpleTimeoutCache).get(DOUBLE_VAR1);
		verify(mockSimpleTimeoutCache).put(eq(DOUBLE_VAR1), any(FutureTask.class));
		verify(mockSimpleTimeoutCache).get(DOUBLE_VAR2);
		verify(mockSimpleTimeoutCache).put(eq(DOUBLE_VAR2), any(FutureTask.class));
		verify(mockSimpleTimeoutCache).get(script2);
		verify(mockSimpleTimeoutCache).put(eq(script2), any(FutureTask.class));

	}
	
	/**
	 * Test for that user input values provided for content wrapper is evaluated correctly and
	 * variables are preserved and accessible for use in template parameters.
	 * Two variable x ("hi") and y ("groovy") are user inputs will produce a concatenated string ("higroovy") 
	 * and store it in z variable for content wrapper template to use. The test also test that x and y
	 * variables are inaccessible to template since they both have passToTemplate == false.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testDynamicValues() throws ParameterResolvingException {

		final String lang = "en";

		ParameterValueResolverImpl parameterValueResolverImpl = new ParameterValueResolverImpl();
		parameterValueResolverImpl.setScriptEngineFactory(scriptEngineFactory);

		final Parameter paramX = mock(Parameter.class, "paramX");
		final Parameter paramY = mock(Parameter.class, "paramY");
		final Parameter paramZ = mock(Parameter.class, "paramZ");

		userParameters = ImmutableList.of(paramX, paramY);
		tempParameters = ImmutableList.of(paramZ);

		final ParameterValue parameterValueX = mock(ParameterValue.class, "x");
		final ParameterValue parameterValueY = mock(ParameterValue.class, "y");

		parameterValues = ImmutableList.of(parameterValueX, parameterValueY);

		contentWrapper = mock(ContentWrapper.class);

		final String script = "x+y";

		when(contentWrapper.getTemplateParameters()).thenReturn(tempParameters);
		when(contentWrapper.getUserInputSettings()).thenReturn(userParameters);
		when(contentWrapper.getInitSection()).thenReturn(null);
		when(contentWrapper.getScriptLanguage()).thenReturn(GROOVY);
		when(contentWrapper.getServiceDefinitions()).thenReturn(null);

		when(parameterValueX.getValue(lang)).thenReturn("hi");
		when(parameterValueX.getParameterName()).thenReturn("x");
		when(paramX.getParameterId()).thenReturn("x");
		when(paramX.isPassToTemplate()).thenReturn(false);

		when(parameterValueY.getValue(lang)).thenReturn(GROOVY);
		when(parameterValueY.getParameterName()).thenReturn("y");
		when(paramY.getParameterId()).thenReturn("y");
		when(paramY.isPassToTemplate()).thenReturn(false);

		when(paramZ.getParameterId()).thenReturn("z");
		when(paramZ.getScriptExpression()).thenReturn(script);
		when(paramZ.isPassToTemplate()).thenReturn(true);

		when(mockSimpleTimeoutCache.get(script)).thenReturn(null);

		Map<String, Object> resolvedValues = parameterValueResolverImpl.resolveValues(
			contentWrapper,
			parameterValues,
			new HashMap<>(), lang);
		assertThat(resolvedValues).containsOnly(entry("z", "higroovy"));

		verify(mockSimpleTimeoutCache).get(script);
		verify(mockSimpleTimeoutCache).put(eq(script), any(FutureTask.class));
	}

}
