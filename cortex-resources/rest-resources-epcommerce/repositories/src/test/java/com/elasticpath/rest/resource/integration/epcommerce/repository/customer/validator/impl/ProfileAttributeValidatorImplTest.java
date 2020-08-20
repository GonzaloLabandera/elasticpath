/**
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.validator.impl;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.base.ScopeIdentifierPart;
import com.elasticpath.rest.definition.profiles.ProfileEntity;
import com.elasticpath.rest.definition.profiles.ProfileIdIdentifierPart;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorMessageTransformer;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.customer.CustomerProfileAttributeService;

/**
 * Test class for {@link ProfileAttributeValidatorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileAttributeValidatorImplTest {

	private static final String PROFILE_KEYS_NOT_EDITABLE = "Profile keys not editable.";

	private static final String PROFILE_KEYS_MISSING = "Missing profile keys.";
	private static final String PROFILE_VALUES_NOT_VALID = "One or more profile values were invalid.";

	private static final String PROFILE_ID = "PROFILE_ID";
	private static final String PROFILE_KEY = "PROFILE_KEY";
	private static final String MISSING_PROFILE_KEY = "MISSING_PROFILE_KEY";
	private static final String MISSING_ATTR_KEY = "MISSING_ATTR_KEY";
	private static final String ATTR_KEY = "ATTR_KEY";
	private static final String VALUE = "VALUE";
	private static final String STORE = "STORE";
	private static final String ATTR_KEY_2 = "ATTR_KEY_2";
	private static final String ERROR = "error";

	private final Map<String, Attribute> attributes = Maps.newHashMap();

	@Mock
	private CustomerProfileAttributeService customerProfileAttributeService;

	@Mock
	private ProfileAttributeFieldTransformer profileAttributeFieldTransformer;

	@Mock
	private StructuredErrorMessageTransformer structuredErrorMessageTransformer;

	@Mock
	private AttributeService attributeService;

	@InjectMocks
	private ProfileAttributeValidatorImpl validator;

	private ProfileEntity profileEntity;

	private ProfileIdentifier profileIdentifier;

	@Before
	public void setup() {
		profileEntity = ProfileEntity.builder()
				.addingProperty(PROFILE_KEY, VALUE)
				.build();
		profileIdentifier = ProfileIdentifier.builder()
				.withProfileId(ProfileIdIdentifierPart.of((PROFILE_ID)))
				.withScope(ScopeIdentifierPart.of(STORE))
				.build();

		when(profileAttributeFieldTransformer.transformToAttributeKey(PROFILE_KEY))
				.thenReturn(ATTR_KEY);
		when(profileAttributeFieldTransformer.transformToFieldName(ATTR_KEY))
				.thenReturn(PROFILE_KEY);
		when(profileAttributeFieldTransformer.transformToFieldName(MISSING_ATTR_KEY))
				.thenReturn(MISSING_PROFILE_KEY);

		Attribute attribute = mock(Attribute.class);
		when(attribute.isRequired()).thenReturn(false);

		attributes.put(ATTR_KEY, attribute);

		when(attributeService.getCustomerProfileAttributesMap(AttributeUsageImpl.USER_PROFILE_USAGE))
				.thenReturn(attributes);
	}

	@Test
	public void testValidationOfDisallowedKeys() {
		when(customerProfileAttributeService.getCustomerEditableAttributeKeys(profileIdentifier.getScope().getValue()))
				.thenReturn(Sets.newHashSet(ATTR_KEY_2));
		validator.validate(profileEntity, profileIdentifier)
				.test()
				.assertError(ResourceOperationFailure.class)
				.assertErrorMessage(PROFILE_KEYS_NOT_EDITABLE);
	}

	@Test
	public void testValidationOfMissingKeys() {
		when(customerProfileAttributeService.getCustomerEditableAttributeKeys(profileIdentifier.getScope().getValue()))
				.thenReturn(Sets.newHashSet(ATTR_KEY, MISSING_ATTR_KEY));

		Attribute attribute = mock(Attribute.class);
		when(attribute.isRequired()).thenReturn(true);

		attributes.put(MISSING_ATTR_KEY, attribute);

		validator.validate(profileEntity, profileIdentifier)
				.test()
				.assertError(ResourceOperationFailure.class)
				.assertErrorMessage(PROFILE_KEYS_MISSING);
	}

	@Test
	public void testValidationNoErrorMessages() {
		when(customerProfileAttributeService.getCustomerEditableAttributeKeys(profileIdentifier.getScope().getValue()))
				.thenReturn(Sets.newHashSet(ATTR_KEY));
		when(customerProfileAttributeService.validateAttributes(anyMap(), anyString(), eq(AttributeUsageImpl.USER_PROFILE_USAGE)))
				.thenReturn(Collections.emptyList());
		when(structuredErrorMessageTransformer.transform(anyCollection(), anyString()))
				.thenReturn(Collections.emptyList());

		validator.validate(profileEntity, profileIdentifier)
				.test()
				.assertNoErrors();
	}

	@Test
	public void testValidationErrorMessages() {
		when(customerProfileAttributeService.getCustomerEditableAttributeKeys(profileIdentifier.getScope().getValue()))
				.thenReturn(Sets.newHashSet(ATTR_KEY));
		List<StructuredErrorMessage> messages = Lists.newArrayList();
		messages.add(new StructuredErrorMessage(ERROR, ERROR, null));

		when(customerProfileAttributeService.validateAttributes(anyMap(), anyString(), eq(AttributeUsageImpl.USER_PROFILE_USAGE)))
				.thenReturn(messages);
		when(structuredErrorMessageTransformer.transform(messages, PROFILE_ID))
				.thenReturn(Lists.newArrayList(Message.builder().build()));

		validator.validate(profileEntity, profileIdentifier)
				.test()
				.assertError(ResourceOperationFailure.class)
				.assertErrorMessage(PROFILE_VALUES_NOT_VALID);
	}
}
