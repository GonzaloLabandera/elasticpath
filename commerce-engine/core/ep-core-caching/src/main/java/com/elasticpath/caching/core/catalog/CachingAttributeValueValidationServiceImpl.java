/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.caching.core.catalog;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Validator;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.cache.Cache;
import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.validation.service.AttributeValueValidationService;

/**
 * Caching implementation of the Attribute Value Validation Service.
 */
@SuppressWarnings("PMD.GodClass")
public class CachingAttributeValueValidationServiceImpl implements AttributeValueValidationService {

	private Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache;
	private Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache;
	private AttributeValueValidationService fallbackAttributeValueValidationService;

	@Override
	public List<StructuredErrorMessage> validate(final Map<String, String> itemsToValidate,
												 final Map<Attribute, Set<String>> referentAllowedValues) {
		return fallbackAttributeValueValidationService.validateWithCache(itemsToValidate, referentAllowedValues, requiredValidatorCache,
				completeValidatorCache);
	}

	@Override
	public List<StructuredErrorMessage> validateWithCache(final Map<String, String> itemsToValidate,
														  final Map<Attribute, Set<String>> referentAllowedValues,
														  final Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache,
														  final Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache) {
		return fallbackAttributeValueValidationService.validateWithCache(itemsToValidate, referentAllowedValues, requiredValidatorCache,
				completeValidatorCache);
	}

	protected Cache<Pair<Object, Set<String>>, Validator> getRequiredValidatorCache() {
		return requiredValidatorCache;
	}

	public void setRequiredValidatorCache(final Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache) {
		this.requiredValidatorCache = requiredValidatorCache;
	}

	protected Cache<Pair<Object, Set<String>>, Validator> getCompleteValidatorCache() {
		return completeValidatorCache;
	}

	public void setCompleteValidatorCache(final Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache) {
		this.completeValidatorCache = completeValidatorCache;
	}

	protected AttributeValueValidationService getFallbackAttributeValueValidationService() {
		return fallbackAttributeValueValidationService;
	}

	public void setFallbackAttributeValueValidationService(final AttributeValueValidationService fallbackAttributeValueValidationService) {
		this.fallbackAttributeValueValidationService = fallbackAttributeValueValidationService;
	}
}
