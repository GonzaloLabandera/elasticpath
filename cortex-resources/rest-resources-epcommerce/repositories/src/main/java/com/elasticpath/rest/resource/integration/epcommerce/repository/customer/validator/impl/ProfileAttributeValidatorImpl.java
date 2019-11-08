/**
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.validator.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.reactivex.Completable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.profiles.ProfileEntity;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.validator.ProfileAttributeValidator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorMessageTransformer;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.customer.CustomerProfileAttributeService;

/**
 * Implementation of <code>ProfileAttributeValidator</code>.
 */
@Component
public class ProfileAttributeValidatorImpl implements ProfileAttributeValidator {

	private static final String NOT_EDITABLE_KEYS_ERROR = "Profile keys not editable.";
	private static final String NOT_EDITABLE_KEYS_ERROR_ID = "profile.update.noneditable.keys";
	private static final String NOT_EDITABLE_KEYS_ERROR_DEBUG = "Profile fields {%s} are not available for edit.";

	private static final String PROFILE_VALUES_NOT_VALID = "One or more profile values were invalid.";
	private static final String FIELDS = "fields";

	private static final String MISSING_PROFILE_KEYS_ERROR = "Missing profile keys.";
	private static final String MISSING_PROFILE_KEYS_ERROR_ID = "profile.update.missing.keys";
	private static final String MISSING_PROFILE_KEYS_ERROR_DEBUG = "Required profile fields {%s} are missing.";


	@Reference
	private CustomerProfileAttributeService customerProfileAttributeService;

	@Reference
	private ProfileAttributeFieldTransformer profileAttributeFieldTransformer;

	@Reference
	private StructuredErrorMessageTransformer structuredErrorMessageTransformer;

	@Reference
	private AttributeService attributeService;

	@Override
	public Completable validate(final ProfileEntity profileEntity, final ProfileIdentifier profileIdentifier) {
		final String scope = profileIdentifier.getScope().getValue();
		final Set<String> editableAttributeKeys = customerProfileAttributeService.getCustomerEditableAttributeKeys(scope);

		// transform profile field names to attribute keys
		Map<String, String> transformedMap = profileEntity.getDynamicProperties().entrySet()
				.stream()
				.collect(Collectors.toMap(entry -> profileAttributeFieldTransformer.transformToAttributeKey(entry.getKey()),
						Map.Entry::getValue));

		// validate attributes are allowed
		Set<String> disallowedKeys = transformedMap.keySet()
				.stream()
				.filter(key -> !editableAttributeKeys.contains(key))
				.map(key -> profileAttributeFieldTransformer.transformToFieldName(key))
				.collect(Collectors.toSet());

		if (!disallowedKeys.isEmpty()) {
			return Completable.error(ResourceOperationFailure.badRequestBody(
					NOT_EDITABLE_KEYS_ERROR,
					generateErrorMessage(NOT_EDITABLE_KEYS_ERROR_ID,
							NOT_EDITABLE_KEYS_ERROR_DEBUG, disallowedKeys, profileIdentifier.getProfileId().getValue())));
		}

		// validate all required, editable attributes are present
		Set<String> missingKeys = validateRequiredFieldsPresent(transformedMap, editableAttributeKeys);
		if (!missingKeys.isEmpty()) {
			return Completable.error(ResourceOperationFailure.badRequestBody(
					MISSING_PROFILE_KEYS_ERROR,
					generateErrorMessage(MISSING_PROFILE_KEYS_ERROR_ID,
							MISSING_PROFILE_KEYS_ERROR_DEBUG, missingKeys, profileIdentifier.getProfileId().getValue())));
		}

		List<Message> messages = structuredErrorMessageTransformer.transform(customerProfileAttributeService.validateAttributes(transformedMap,
				scope),
				profileIdentifier.getProfileId().getValue());

		if (!messages.isEmpty()) {
			return Completable.error(ResourceOperationFailure.badRequestBody(PROFILE_VALUES_NOT_VALID, messages));
		}

		return Completable.complete();
	}

	private List<Message> generateErrorMessage(final String messageIdentifier, final String debugMessage, final Set<String> fields,
											   final String resourceId) {
		Map<String, String> data = Maps.newHashMap();
		data.put(FIELDS, String.join(",", fields));
		StructuredErrorMessage message = new StructuredErrorMessage(messageIdentifier, String.format(debugMessage, data.get(FIELDS)), data);
		return structuredErrorMessageTransformer.transform(Lists.newArrayList(message), resourceId);
	}

	private Set<String> validateRequiredFieldsPresent(final Map<String, String> transformedMap, final Set<String> editableAttributeKeys) {
		Map<String, Attribute> attributeMap = attributeService.getCustomerProfileAttributesMap();
		return editableAttributeKeys.stream()
				.filter(key -> attributeMap.get(key).isRequired())
				.filter(key -> !transformedMap.keySet().contains(key))
				.map(key -> profileAttributeFieldTransformer.transformToFieldName(key))
				.collect(Collectors.toSet());
	}
}
