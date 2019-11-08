/*
 * Copyright © 2014 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.converter.ConversionMalformedValueException;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.attribute.UserTraitSubjectAttribute;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.UserTraitsToTagSetTransformer;
import com.elasticpath.rest.traits.Trait;
import com.elasticpath.settings.provider.SettingValueProvider;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;
import com.elasticpath.tags.service.TagFactory;

/**
 * Transforms user traits from a Subject into a CE TagSet.
 */
@Singleton
@Named("userTraitsToTagSetTransformer")
public class SubjectBasedUserTraitsToTagSetTransformer implements UserTraitsToTagSetTransformer {

	private static final Logger LOG = LoggerFactory.getLogger(SubjectBasedUserTraitsToTagSetTransformer.class);

	private final TagFactory tagFactory;

	private final SettingValueProvider<Boolean> trustedTraitSettingProvider;

	private final Set<Object> controlledTraits;

	/**
	 * Creates a SubjectBasedUserTraitsToTagSetTransformer.
	 *
	 * @param tagFactory the tag factory.
	 * @param trustedTraitSettingProvider Settings provider for the trusted trait setting.
	 * @param controlledTraits list of controlled trait names.
	 */
	@Inject
	public SubjectBasedUserTraitsToTagSetTransformer(
			@Named("tagFactory")
			final TagFactory tagFactory,
			@Named("trustedTraitSettingProvider")
			final SettingValueProvider<Boolean> trustedTraitSettingProvider,
			@Named("controlledTraits") @Value("#{controlledTraits}")
			final List<Object> controlledTraits
			) {
		this.tagFactory = tagFactory;
		this.trustedTraitSettingProvider = trustedTraitSettingProvider;

		this.controlledTraits = new HashSet<>();
		this.controlledTraits.addAll(controlledTraits);
	}

	@Override
	public TagSet transformUserTraitsToTagSet(final Subject subject) {
		TagSet tagSet = new TagSet();

		boolean enableTrustedTraits = trustedTraitSettingProvider.get();
		subject.getAttributes().stream()
			.filter(attribute -> UserTraitSubjectAttribute.TYPE.equals(attribute.getType()))
			.map(attribute -> ((UserTraitSubjectAttribute) attribute).getTrait())
			.forEach(trait -> {
				Optional<StructuredErrorMessage> error = verifyTrustedTraitIsAllowed(trait, enableTrustedTraits);
				if (error.isPresent()) {
					throw new EpValidationException("Trusted traits not enabled", Collections.singletonList(error.get()));
				}

				try {
					Tag tag = tagFactory.createTagFromTagName(trait.getName(), trait.getValue());
					tagSet.addTag(trait.getName(), tag);
				} catch (ConversionMalformedValueException e) {
					LOG.debug("Ignoring trait {} due to error converting to tag: {}", trait, e.getMessage());
				}
			});

		return tagSet;
	}

	/**
	 * Checks to see if a trait is trusted and if so if trusted traits are enabled.
	 * @param trait the trait.
	 * @param enableTrustedTraits setting of whether traits are enabled.
	 * @return Optional structured error message if validation fails.
	 */
	Optional<StructuredErrorMessage> verifyTrustedTraitIsAllowed(final Trait trait, final boolean enableTrustedTraits) {
		if (!enableTrustedTraits && controlledTraits.contains(trait.getName())) {
			Map<String, String> data = new HashMap<>();
			data.put("trait-name", trait.getName());
			return Optional.of(new StructuredErrorMessage("trusted.traits.not.enabled",
					"Trusted Traits are not enabled", data));
		}
		return Optional.empty();
	}
}