/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer.impl;

import java.util.Set;

import com.google.common.collect.Sets;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.service.customer.CustomerProfileAttributeValueRestrictor;
import com.elasticpath.service.customer.CustomerProfileAttributeValueRestrictorContext;

/**
 * Restricts values to the set of supported genders.
 */
public class GenderValueRestrictorImpl implements CustomerProfileAttributeValueRestrictor {

	@Override
	public Set<String> getRestrictedValues(final CustomerProfileAttributeValueRestrictorContext context) {
		return Sets.newHashSet(String.valueOf(Customer.GENDER_MALE), String.valueOf(Customer.GENDER_FEMALE));
	}
}
