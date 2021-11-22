/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.impl;

import static com.elasticpath.rest.identity.attribute.KeyValueSubjectAttribute.KEY_VALUE_DELIM;
import static com.google.common.collect.Maps.immutableEntry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.relos.rs.subject.SubjectHeaderConstants;

@RunWith(MockitoJUnitRunner.class)
public final class EditableTraitsFromRequestTest {

	private static final String TRAIT_SEPARATOR = ",";
	private static final String TEST_HEADER_KEY = "HEADER_KEY";
	private static final String TEST_HEADER_VALUE = "TEST_HEADER";
	private static final String TRAIT_KEY = "TRAIT_KEY";
	private static final String TRAIT_KEY_2 = "TRAIT_KEY_2";
	private static final String TRAIT_VALUE = "trait value";
	private static final String TRAIT_URL_VALUE = "http://elasticpath.com/search?q=search+term&tq=other,otter";
	private static final String TRAIT_HEADER_VALUE = TRAIT_KEY + KEY_VALUE_DELIM + TRAIT_VALUE;
	private static final String TRAIT_HEADER_URL_VALUE = StringUtils.wrap(TRAIT_KEY_2 + KEY_VALUE_DELIM + TRAIT_URL_VALUE, '"');

	private final SubjectAttributeProviderImpl classUnderTest = new SubjectAttributeProviderImpl();

	@Mock
	private HttpServletRequest mockRequest;


	@Test
	public void testSingleAttributeNoAttributeHeader() {
		addTraitHeader(TRAIT_HEADER_VALUE);

		Map<String, String> actual = classUnderTest.getUserTraitsFromRequest(mockRequest);

		assertThat(actual)
				.containsOnly(immutableEntry(TRAIT_KEY, TRAIT_VALUE));
	}

	@Test
	public void testSingleAttributeWithURLValue() {
		addTraitHeader(TRAIT_HEADER_URL_VALUE);

		Map<String, String> actual = classUnderTest.getUserTraitsFromRequest(mockRequest);

		assertThat(actual)
				.containsOnly(immutableEntry(TRAIT_KEY_2, TRAIT_URL_VALUE));
	}

	@Test
	public void testWithMoreThanOneTraitHeader() {
		addTraitHeader(Joiner.on(TRAIT_SEPARATOR).join(TRAIT_HEADER_VALUE, TRAIT_HEADER_URL_VALUE));

		Map<String, String> actual = classUnderTest.getUserTraitsFromRequest(mockRequest);

		assertThat(actual)
				.containsOnly(
						immutableEntry(TRAIT_KEY, TRAIT_VALUE),
						immutableEntry(TRAIT_KEY_2, TRAIT_URL_VALUE));
	}

	@Test
	public void testWithTrailingAndLeadingSpaces() {
		String traitHeaderWithSpaces = TRAIT_KEY + KEY_VALUE_DELIM + "  " + TRAIT_VALUE + "   ";
		addTraitHeader(traitHeaderWithSpaces);

		Map<String, String> actual = classUnderTest.getUserTraitsFromRequest(mockRequest);

		assertThat(actual)
				.containsOnly(immutableEntry(TRAIT_KEY, TRAIT_VALUE));
	}

	@Test
	public void testWithTraitHeaderAndOtherHeader() {
		addTraitHeader(TRAIT_HEADER_VALUE);
		addHeaders(TEST_HEADER_KEY, TEST_HEADER_VALUE);

		Map<String, String> actual = classUnderTest.getUserTraitsFromRequest(mockRequest);

		assertThat(actual)
				.containsOnly(immutableEntry(TRAIT_KEY, TRAIT_VALUE));
	}

	@Test
	public void testWithNoHeaders() {
		Map<String, String> actual = classUnderTest.getUserTraitsFromRequest(mockRequest);

		assertThat(actual)
				.isEmpty();
	}

	@Test
	public void testWrongTraitFormat() {
		addTraitHeader("testtraitwithbadformat");

		Map<String, String> actual = classUnderTest.getUserTraitsFromRequest(mockRequest);

		assertThat(actual)
				.isEmpty();
	}

	@Test
	public void testTraitWithNoValue() {
		addTraitHeader("traitkey=");

		Map<String, String> actual = classUnderTest.getUserTraitsFromRequest(mockRequest);

		assertThat(actual)
				.isEmpty();
	}

	@Test
	public void testTraitWitNoKey() {
		addTraitHeader("=traitvalue");

		Map<String, String> actual = classUnderTest.getUserTraitsFromRequest(mockRequest);

		assertThat(actual)
				.isEmpty();
	}

	private void addHeaders(final String headerName, final String... headerValue) {
		when(mockRequest.getHeaders(headerName))
				.thenReturn(Iterators.asEnumeration(Iterators.forArray(headerValue)));
	}

	private void addTraitHeader(final String traitHeaderValue) {
		addHeaders(SubjectHeaderConstants.USER_TRAITS, traitHeaderValue);
	}
}
