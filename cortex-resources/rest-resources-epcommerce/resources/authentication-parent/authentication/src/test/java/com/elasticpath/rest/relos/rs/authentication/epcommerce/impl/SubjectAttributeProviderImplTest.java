/*
 * Copyright © 2015 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.identity.attribute.CurrencySubjectAttribute;
import com.elasticpath.rest.identity.attribute.LocaleSubjectAttribute;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.identity.attribute.UserTraitSubjectAttribute;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.connectivity.extensionpoint.HttpRequestTagSetPopulator;
import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;
import com.elasticpath.xpf.context.builders.HttpTagSetContextBuilder;

/**
 * Test class for {@link SubjectAttributeProviderImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public final class SubjectAttributeProviderImplTest {

	private static final String ATTRIBUTE_VALUE_1 = "anyValue1";
	private static final String ATTRIBUTE_VALUE_2 = "anyValue2";
	private static final String ATTRIBUTE_KEY_1 = "1";
	private static final String ATTRIBUTE_KEY_2 = "2";
	private static final int EXPECTED = 3;

	@Mock
	private XPFExtensionLookup extensionLookup;

	@Mock
	private HttpTagSetContextBuilder httpTagSetContextBuilder;

	@Mock
	private HttpRequestTagSetPopulator populator;

	@Mock
	private HttpRequestTagSetPopulator populator2;

	@InjectMocks
	private SubjectAttributeProviderImpl classUnderTest;

	@Mock
	private HttpServletRequest mockRequest;

	@Mock
	private XPFHttpTagSetContext httpTagSetContext;

	private List<HttpRequestTagSetPopulator> populators;

	private Map<String, String> subjectAttributes;

	private Map<String, String> subjectAttributes2;

	/**
	 * Set up.
	 */
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {

		when(httpTagSetContextBuilder.build(mockRequest)).thenReturn(httpTagSetContext);
		// default no populator strategies.
		populators = new ArrayList<>();

		// default no subject attributes
		subjectAttributes = new HashMap<>();
		subjectAttributes2 = new HashMap<>();

		when(extensionLookup.getMultipleExtensions(eq(HttpRequestTagSetPopulator.class), any(), any())).thenReturn(populators);

		when(populator.collectTagValues(any(XPFHttpTagSetContext.class)))
				.thenReturn(subjectAttributes);
		when(populator2.collectTagValues(any(XPFHttpTagSetContext.class))).thenReturn(subjectAttributes2);
	}

	private void setUpOnePopulatorWithNoAttributes() {
		populators.add(populator);
	}

	private void setUpOnePopulatorWithOneAttribute() {
		when(populator.collectTagValues(httpTagSetContext)).thenReturn(Collections.singletonMap(ATTRIBUTE_KEY_1, ATTRIBUTE_VALUE_1));
		populators.add(populator);
	}

	private void setUpOnePopulatorWithMultipleAttributes() {
		final Map<String, String> attributes = new HashMap<>();
		attributes.put(ATTRIBUTE_KEY_1, ATTRIBUTE_VALUE_1);
		attributes.put(ATTRIBUTE_KEY_2, ATTRIBUTE_VALUE_2);

		when(populator.collectTagValues(httpTagSetContext)).thenReturn(attributes);
		populators.add(populator);
	}

	private void setUpMultiplePopulatorWithSameAttributeKeyAndDifferentValuesEach() {
		when(populator.collectTagValues(httpTagSetContext)).thenReturn(Collections.singletonMap(ATTRIBUTE_KEY_1, ATTRIBUTE_VALUE_1));
		when(populator2.collectTagValues(httpTagSetContext)).thenReturn(Collections.singletonMap(ATTRIBUTE_KEY_1, ATTRIBUTE_VALUE_2));

		populators.add(populator);
		populators.add(populator2);
	}

	private void setUpMultiplePopulatorWithSameAttributeKeyAndValuesEach() {
		when(populator.collectTagValues(httpTagSetContext)).thenReturn(Collections.singletonMap(ATTRIBUTE_KEY_1, ATTRIBUTE_VALUE_1));
		when(populator2.collectTagValues(httpTagSetContext)).thenReturn(Collections.singletonMap(ATTRIBUTE_KEY_1, ATTRIBUTE_VALUE_1));

		populators.add(populator);
		populators.add(populator2);
	}

	private void setUpMultiplePopulators() {
		final Map<String, String> attributes = new HashMap<>();
		attributes.put(ATTRIBUTE_KEY_1, ATTRIBUTE_VALUE_1);
		attributes.put("LOCALE", "CA");
		attributes.put("CURRENCY", "CAD");

		when(populator.collectTagValues(httpTagSetContext)).thenReturn(attributes);
		populators.add(populator);
	}

	private void setUpMultiplePopulatorWhenFirstThrowException() {
		when(populator.collectTagValues(httpTagSetContext)).thenThrow(new RuntimeException());

		populators.add(populator);
		populators.add(populator2);
	}

	@Test
	public void testGetSubjectAttributesWithSingleExtensionMinResponse() {
		setUpOnePopulatorWithNoAttributes();

		Collection<SubjectAttribute> result = classUnderTest.getSubjectAttributes(mockRequest);

		assertTrue("No subject attributes should be created if there are no attributes provided by the look up.", result.isEmpty());
	}

	@Test
	public void testGetSubjectAttributesWithSingleExtensionFullResponse() {

		setUpOnePopulatorWithOneAttribute();

		Collection<SubjectAttribute> result = classUnderTest.getSubjectAttributes(mockRequest);

		assertEquals("Populator provides one attribute.", 1, result.size());
		assertTrue("Returned subject attribute should be one provided by look up.",
				result.stream().map(SubjectAttribute::getKey).anyMatch(ATTRIBUTE_KEY_1::equals));
	}

	@Test
	public void testExistingAttributeIsOverridenBySubsequentAdditionWithMultipleExtensions() {
		setUpMultiplePopulatorWithSameAttributeKeyAndDifferentValuesEach();

		Collection<SubjectAttribute> result = classUnderTest.getSubjectAttributes(mockRequest);

		assertEquals("Populator provides one attribute.", 1, result.size());


		assertTrue("Second attribute overrides first attribute.",
				result.stream().map(SubjectAttribute::getValue).anyMatch(ATTRIBUTE_VALUE_1::equals));
	}

	@Test
	public void testGetSubjectAttributesWithMultipleExtensionsExceptionResponse() {
		setUpMultiplePopulatorWhenFirstThrowException();

		assertThatThrownBy(() -> classUnderTest.getSubjectAttributes(mockRequest)).isInstanceOf(RuntimeException.class);
		verify(populator2, never()).collectTagValues(any());
	}

	@Test
	public void testNoPopulatorStrategiesCreatesEmptyCollectionOfSubjectAttributes() {
		Collection<SubjectAttribute> result = classUnderTest.getSubjectAttributes(mockRequest);

		assertTrue("No subject attributes should be created if there are no look up strategies.", result.isEmpty());
	}

	@Test
	public void testOnePopulatorStrategyWithMultipleAttributesCreatesSubjectAttributeCollectionWithProvidedAttributes() {
		setUpOnePopulatorWithMultipleAttributes();

		Collection<SubjectAttribute> result = classUnderTest.getSubjectAttributes(mockRequest);

		assertEquals("Populator provides two attributes.", 2, result.size());
		assertThat(result).extracting(SubjectAttribute::getKey).containsOnly(ATTRIBUTE_KEY_1, ATTRIBUTE_KEY_2);
	}

	@Test
	public void testFilterDuplicates() {
		setUpMultiplePopulatorWithSameAttributeKeyAndValuesEach();

		Collection<SubjectAttribute> result = classUnderTest.getSubjectAttributes(mockRequest);

		assertEquals("Populator provides two attributes which should be returned.", 1, result.size());
		assertTrue("Returned subject attribute should contain attribute provided by look up.",
				result.stream().map(SubjectAttribute::getValue).anyMatch(ATTRIBUTE_VALUE_1::equals));
	}

	@Test
	public void testProviderReturnsDifferentAttributes() {
		setUpMultiplePopulators();

		Collection<SubjectAttribute> result = classUnderTest.getSubjectAttributes(mockRequest);

		assertEquals("Populator provides two attributes which should be returned.", EXPECTED, result.size());
		assertThat(result).hasOnlyElementsOfTypes(CurrencySubjectAttribute.class, UserTraitSubjectAttribute.class, LocaleSubjectAttribute.class);
	}
}
