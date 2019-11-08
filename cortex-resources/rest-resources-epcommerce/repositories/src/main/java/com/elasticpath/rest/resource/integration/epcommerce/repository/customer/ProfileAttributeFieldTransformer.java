/**
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer;

/**
 * A transformer to convert between internal profile attribute keys and profile attribute dynamic fields.
 */
public interface ProfileAttributeFieldTransformer {

	/**
	 * Transforms an attribute key to an API dynamic field name.
	 * @param attributeKey the domain attribute key
	 * @return the dynamic field name
	 */
	String transformToFieldName(String attributeKey);

	/**
	 * Transforms a dynamic field name to an attribute key.
	 * @param fieldName the dynamic field name
	 * @return the domain attribute key
	 */
	String transformToAttributeKey(String fieldName);
}
