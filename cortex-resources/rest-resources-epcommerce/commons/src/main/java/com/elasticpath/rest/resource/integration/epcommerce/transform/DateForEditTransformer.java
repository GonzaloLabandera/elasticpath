/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.transform;

import java.util.Date;
import java.util.Optional;

import com.elasticpath.domain.attribute.AttributeType;

/**
 * A transformer that converts between <code>java.util.Date</code> and an ISO-8601 UTC string (Zulu time).
 */
public interface DateForEditTransformer {

	/**
	 * Transform an ISO-8601 UTC formatted string into a Date object.
	 * @param attributeType the attribute type
	 * @param date the ISO-8601 formatted string
	 * @return java.util.Date
	 */
	Optional<Date> transformToDomain(AttributeType attributeType, String date);

	/**
	 * Transform a Date object into an ISO-8601 UTC formatted string.
	 *
	 * @param attributeType the attribute type
	 * @param date          the java.util.Date to transform
	 * @return the string representation
	 */
	Optional<String> transformToString(AttributeType attributeType, Date date);
}
