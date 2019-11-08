/**
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;

/**
 * A transformer to convert between internal profile attribute keys and profile attribute dynamic fields.
 */
public class ProfileAttributeFieldTransformerImpl implements ProfileAttributeFieldTransformer {

	private final Map<String, String> profileAttributeTranslationMap;
	private final Map<String, String> reverseAttributeTranslationMap = Maps.newHashMap();

	/**
	 * Constructor.
	 *
	 * @param profileAttributeTranslationMap the mapping between field names and attribute keys
	 */
	public ProfileAttributeFieldTransformerImpl(final Map<String, String> profileAttributeTranslationMap) {
		this.profileAttributeTranslationMap = profileAttributeTranslationMap;

		// now add the reverse
		profileAttributeTranslationMap.keySet().stream()
				.forEach(key -> reverseAttributeTranslationMap.put(profileAttributeTranslationMap.get(key), key));
	}

	@Override
	public String transformToFieldName(final String attributeKey) {
		return Optional.ofNullable(profileAttributeTranslationMap.get(attributeKey)).orElse(attributeKey);
	}

	@Override
	public String transformToAttributeKey(final String fieldName) {
		return Optional.ofNullable(reverseAttributeTranslationMap.get(fieldName)).orElse(fieldName);
	}
}
