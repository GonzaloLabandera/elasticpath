/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.validation.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.attribute.Attribute;

/**
 * Service for dynamic validation of {@link Attribute} values.
 */
public interface AttributeValueValidationService {

	/**
	 * Dynamic validation of {@link Attribute} values.
	 *
	 * @param itemsToValidate map with attribute key name and  value entries to validate.
	 * @param referentAttributes map of attribute definitions and valid values
	 * @return set of constraint violations, if any.
	 */
	List<StructuredErrorMessage> validate(Map<String, String> itemsToValidate,
										  Map<Attribute, Set<String>> referentAttributes);
}
