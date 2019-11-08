/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.transform;

import com.elasticpath.domain.attribute.CustomerProfileValue;

/**
 * Transforms <code>CustomerProfileValue</code> objects to string representations.
 */
public interface CustomerProfileValueTransformer {
	/**
	 * Transform a CustomerProfileAttribute to a string.
	 * @param customerProfileValue the value to transform
	 * @return the string representation
	 */
	String transformToString(CustomerProfileValue customerProfileValue);
}
