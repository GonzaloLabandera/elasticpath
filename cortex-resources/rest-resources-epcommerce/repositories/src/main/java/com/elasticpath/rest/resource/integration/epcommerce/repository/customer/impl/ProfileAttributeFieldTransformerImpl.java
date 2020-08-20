/**
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;

/**
 * A transformer to convert between internal profile attribute keys and profile attribute dynamic fields.
 */
public class ProfileAttributeFieldTransformerImpl implements ProfileAttributeFieldTransformer {

	private final BiMap<String, String> profileAttributeTranslationMap;

	/**
	 * Constructor.
	 *
	 * @param profileAttributeTranslationMap the mapping between field names and attribute keys
	 */
	public ProfileAttributeFieldTransformerImpl(final Map<String, String> profileAttributeTranslationMap) {
		this.profileAttributeTranslationMap = new ImmutableBiMap.Builder<String, String>().putAll(profileAttributeTranslationMap).build();
	}

	@Override
	public String transformToFieldName(final String attributeKey) {
		return Optional.ofNullable(profileAttributeTranslationMap.get(attributeKey)).orElse(attributeKey);
	}

	@Override
	public String transformToAttributeKey(final String fieldName) {
		return Optional.ofNullable(profileAttributeTranslationMap.inverse().get(fieldName)).orElse(fieldName);
	}
}
