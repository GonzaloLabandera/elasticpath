/*
 * Copyright © 2014 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.converter.ConversionMalformedValueException;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.identity.attribute.UserTraitSubjectAttribute;
import com.elasticpath.rest.traits.Trait;
import com.elasticpath.settings.provider.SettingValueProvider;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;
import com.elasticpath.tags.service.TagFactory;

/**
 * Unit test.
 */
@RunWith(MockitoJUnitRunner.class)
public class SubjectBasedUserTraitsToTagSetTransformerTest {

	private SubjectBasedUserTraitsToTagSetTransformer transformer;

	@Mock
	private TagFactory tagFactory;

	@Mock
	private SettingValueProvider<Boolean> trustedTraitSetting;

	private Boolean settingValue = true;

	private static final String SHOPPING_CONTEXT_DATE_OVERRIDE = "SHOPPING_CONTEXT_DATE_OVERRIDE";

	private final List<Object> controlledTraits = Collections.singletonList(SHOPPING_CONTEXT_DATE_OVERRIDE);

	@Before
	public void setUp() {
		when(trustedTraitSetting.get()).thenReturn(settingValue);

		when(tagFactory.createTagFromTagName(anyString(), anyString()))
			.thenAnswer(invocationOnMock -> {
				Object[] arguments = invocationOnMock.getArguments();
				return new Tag(arguments[1]);
			});

		transformer = new SubjectBasedUserTraitsToTagSetTransformer(tagFactory, trustedTraitSetting, controlledTraits);
	}

	@Test
	public void testSetupCustomerWithNoTags() {
		List<SubjectAttribute> traits = givenZeroTraits();
		Subject subject = givenSubjectWithTraits(traits);

		TagSet tagSet = transformer.transformUserTraitsToTagSet(subject);

		verifyTagsMatchTraits(traits, tagSet);
	}

	@Test
	public void testSetupCustomerWithOneTag() {
		List<SubjectAttribute> traits = givenOneTrait();
		Subject subject = givenSubjectWithTraits(traits);

		TagSet tagSet = transformer.transformUserTraitsToTagSet(subject);

		verifyTagsMatchTraits(traits, tagSet);
	}

	@Test
	public void testSetupCustomerSessionWithManyTags() {
		List<SubjectAttribute> traits = givenManyUserTraits();
		Subject subject = givenSubjectWithTraits(traits);

		TagSet tagSet = transformer.transformUserTraitsToTagSet(subject);

		verifyTagsMatchTraits(traits, tagSet);
	}

	@Test
	public void testSkipTraitOnTypeConversionError() {
		List<SubjectAttribute> traits = givenOneTrait();
		Subject subject = givenSubjectWithTraits(traits);

		when(tagFactory.createTagFromTagName(anyString(), anyString()))
				.thenThrow(new ConversionMalformedValueException("Test failure"));
		TagSet tagSet = transformer.transformUserTraitsToTagSet(subject);
		assertTrue("Invalid tag should have been skipped", tagSet.isEmpty());
	}

	@Test
	public void testControlledTraitsAreInvalidated() {
		settingValue = false;

		Trait trait = new Trait(SHOPPING_CONTEXT_DATE_OVERRIDE, "value");

		Optional<StructuredErrorMessage> error = transformer.verifyTrustedTraitIsAllowed(trait, settingValue);

		assertTrue(error.isPresent());
		assertTrue(error.get().getMessageId().equals("trusted.traits.not.enabled"));
	}

	@Test
	public void testControlledTraitsCanBeEnabled() {
		settingValue = true;

		List<SubjectAttribute> traits = givenControlledUserTraits();
		Subject subject = givenSubjectWithTraits(traits);

		TagSet tagSet = transformer.transformUserTraitsToTagSet(subject);

		verifyTagsMatchTraits(traits, tagSet);
	}

	private Subject givenSubjectWithTraits(final List<SubjectAttribute> traits) {
		Subject subject = mock(Subject.class);
		when(subject.getAttributes()).thenReturn(traits);
		return subject;
	}

	private List<SubjectAttribute> givenZeroTraits() {
		return Collections.emptyList();
	}

	private List<SubjectAttribute> givenOneTrait() {
		return Collections.singletonList(new UserTraitSubjectAttribute("aKey", "aValue"));
	}

	private List<SubjectAttribute> givenManyUserTraits() {
		return Arrays.asList(
			new UserTraitSubjectAttribute("aKey1", "aValue1"),
			new UserTraitSubjectAttribute("aKey2", "aValue2"),
			new UserTraitSubjectAttribute("aKey3", "aValue3"),
			new UserTraitSubjectAttribute("aKey4", "aValue4"));
	}

	private List<SubjectAttribute> givenControlledUserTraits() {
		return Arrays.asList(
			new UserTraitSubjectAttribute("SHOPPING_CONTEXT_DATE_OVERRIDE", "value"),
			new UserTraitSubjectAttribute("aKey1", "aValue1"),
			new UserTraitSubjectAttribute("aKey2", "aValue2")
		);
	}

	private void verifyTagsMatchTraits(final List<SubjectAttribute> traits, final TagSet tagSet) {
		assertEquals("Unexpected number of tags",
				traits.size(), tagSet.getTags().size());

		for (SubjectAttribute attribute: traits) {
			Trait trait = ((UserTraitSubjectAttribute) attribute).getTrait();
			assertEquals("Expected tag was not added to the customer session",
					new Tag(trait.getValue()), tagSet.getTagValue(trait.getName()));
		}
	}
}
