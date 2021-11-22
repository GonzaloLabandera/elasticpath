/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.validation.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Validator;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.common.dto.StructuredErrorResolution;
import com.elasticpath.cache.Cache;
import com.elasticpath.commons.util.Pair;
import com.elasticpath.xpf.connectivity.entity.XPFModifierField;


/**
 * Service for dynamic validation of {@link XPFModifierField} fields.
 */
public interface ModifierFieldValidationService {

	/**
	 * Dynamic validation of {@link XPFModifierField} instances.
	 *
	 * @param itemsToValidate map with field name and field value entries to validate.
	 * @param referentFields  field definitions.
	 * @param resolution structured error message resolution to use if an error is generated
	 * @return set of constraint violations, if any.
	 */
	List<StructuredErrorMessage> validate(Map<String, String> itemsToValidate,
										  Set<XPFModifierField> referentFields,
										  StructuredErrorResolution resolution);

	/**
	 * Dynamic validation of {@link XPFModifierField} instances.
	 *
	 * @param itemsToValidate map with field name and field value entries to validate.
	 * @param referentFields field definitions
	 * @param resolution structured error message resolution to use if an error is generated
	 * @param requiredValidatorCache a cache for "is required" dynamic value validators
	 * @param completeValidatorCache a cache for "complete" dynamic value validators
	 * @return set of constraint violations, if any.
	 */
	List<StructuredErrorMessage> validateWithCache(Map<String, String> itemsToValidate,
										  Set<XPFModifierField> referentFields,
										  StructuredErrorResolution resolution,
										  Cache<Pair<Object, Set<String>>, Validator> requiredValidatorCache,
										  Cache<Pair<Object, Set<String>>, Validator> completeValidatorCache);
}
