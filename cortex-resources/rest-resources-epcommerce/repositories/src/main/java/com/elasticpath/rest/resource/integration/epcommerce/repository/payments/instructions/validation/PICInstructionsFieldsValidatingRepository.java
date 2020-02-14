/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.validation;

import java.util.Collection;
import java.util.Currency;
import java.util.Locale;

import com.elasticpath.base.common.dto.StructuredErrorMessage;

/**
 * Service for validating creating a payment instruction fields.
 */
public interface PICInstructionsFieldsValidatingRepository {
	/**
	 * Validates the payment instruction fields.
	 * @param methodId is method id.
	 * @param currency is currency type.
	 * @param locale is locale type.
	 * @param userId is user id.
	 *
	 * @return a collection of Structured Error Messages containing validation errors, or an
	 * 			empty collection if the validation is successful.
	 */
	Collection<StructuredErrorMessage> validate(String methodId, Currency currency, Locale locale, String userId);
}
