/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.caching.core.catalog;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Validator;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.common.dto.StructuredErrorResolution;
import com.elasticpath.cache.Cache;
import com.elasticpath.commons.util.Pair;
import com.elasticpath.validation.service.ModifierFieldValidationService;
import com.elasticpath.xpf.connectivity.entity.XPFModifierField;

/**
 * Caching implementation of the Modifier Field Validation Service.
 */
@SuppressWarnings("PMD.GodClass")
public class CachingModifierFieldValidationServiceImpl implements ModifierFieldValidationService {

	private Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache;
	private Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache;
	private ModifierFieldValidationService fallbackModifierFieldValidationService;

	@Override
	public List<StructuredErrorMessage> validate(final Map<String, String> itemsToValidate,
												 final Set<XPFModifierField> referentFields,
												 final StructuredErrorResolution resolution) {
		return fallbackModifierFieldValidationService.validateWithCache(itemsToValidate, referentFields, resolution, requiredValidatorCache,
				completeValidatorCache);
	}

	@Override
	public List<StructuredErrorMessage> validateWithCache(final Map<String, String> itemsToValidate,
														  final Set<XPFModifierField> referentFields,
														  final StructuredErrorResolution resolution,
														  final Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache,
														  final Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache) {
		return fallbackModifierFieldValidationService.validateWithCache(itemsToValidate, referentFields, resolution, requiredValidatorCache,
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

	protected ModifierFieldValidationService getFallbackModifierFieldValidationService() {
		return fallbackModifierFieldValidationService;
	}

	public void setFallbackModifierFieldValidationService(final ModifierFieldValidationService fallbackModifierFieldValidationService) {
		this.fallbackModifierFieldValidationService = fallbackModifierFieldValidationService;
	}
}
