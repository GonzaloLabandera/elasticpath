/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.validation.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.cache.Cache;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.validation.ConstraintViolationTransformer;
import com.elasticpath.validation.validators.util.DynamicAttributeValue;
import com.elasticpath.validation.validators.util.DynamicAttributeValueValidator;

/**
 * Test class for {@link AttributeValueValidationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributeValueValidationServiceImplTest {
	private static final String KEY = "key";
	private static final String VALUE = "value";
	private static final String ERROR = "error";

	@Mock
	private DynamicAttributeValueValidator dynamicAttributeValueValidator;

	@Mock
	private ConstraintViolationTransformer constraintViolationTransformer;

	@Mock
	private ConstraintViolation<DynamicAttributeValue> violation;

	@Captor
	private ArgumentCaptor<Set<ConstraintViolation<DynamicAttributeValue>>> errors;

	@InjectMocks
	@Spy
	private AttributeValueValidationServiceImpl service;

	private final List<StructuredErrorMessage> messages = Lists.newArrayList();

	@Before
	public void setup() {
		when(service.getDynamicAttributeValueValidator(any(Cache.class), any(Cache.class)))
				.thenReturn(dynamicAttributeValueValidator);
	}

	@Test
	public void testValidateEmptyValueList() {

		assertThat(service.validate(Collections.emptyMap(), Collections.emptyMap()))
				.as("unexpected non-empty violations")
				.isEmpty();
	}

	@Test(expected = IllegalStateException.class)
	public void testValidateEmptyReferenceList() {
		Map<String, String> values = Maps.newHashMap();
		values.put(KEY, VALUE);

		assertThat(service.validate(values, Collections.emptyMap()))
				.as("unexpected non-empty violations")
				.isEmpty();
	}

	@Test
	public void testWithNoViolations() {
		when(dynamicAttributeValueValidator.validate(any(DynamicAttributeValue.class)))
				.thenReturn(Collections.emptySet());
		when(constraintViolationTransformer.transform(anySet()))
				.thenReturn(messages);

		Map<String, String> values = Maps.newHashMap();
		values.put(KEY, VALUE);

		Map<Attribute, Set<String>> references = Maps.newHashMap();
		references.put(new AttributeImpl(), Collections.emptySet());

		assertThat(service.validate(values, references))
				.as("unexpected non-empty violations")
				.isEmpty();

		verify(constraintViolationTransformer).transform(errors.capture());
		assertThat(errors.getValue()).isEmpty();
	}

	@Test
	public void testWithViolations() {
		messages.add(new StructuredErrorMessage(ERROR, ERROR, null));

		Set<ConstraintViolation<DynamicAttributeValue>> violations = Sets.newHashSet();
		violations.add(violation);

		when(dynamicAttributeValueValidator.validate(any(DynamicAttributeValue.class)))
				.thenReturn(violations);

		when(constraintViolationTransformer.transform(anySet()))
				.thenReturn(messages);

		Map<String, String> values = Maps.newHashMap();
		values.put(KEY, VALUE);

		Map<Attribute, Set<String>> references = Maps.newHashMap();
		references.put(new AttributeImpl(), Collections.emptySet());

		assertThat(service.validate(values, references))
				.as("expected empty violations")
				.hasSize(1);

		verify(constraintViolationTransformer).transform(errors.capture());
		assertThat(errors.getValue()).isNotEmpty();
	}
}
