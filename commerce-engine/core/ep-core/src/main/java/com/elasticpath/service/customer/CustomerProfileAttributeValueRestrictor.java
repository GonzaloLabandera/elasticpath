/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer;

import java.util.Set;

/**
 * Evaluates a set of restricted values for a context, which is not necessarily specific to a given customer profile attribute key.
 */
public interface CustomerProfileAttributeValueRestrictor {

	/**
	 * Gets the set of restricted values for a given context.
	 * @param context the customer profile attribute context
	 * @return the array of restricted values
	 */
	Set<String> getRestrictedValues(CustomerProfileAttributeValueRestrictorContext context);
}
