/**
 * Copyright (c) Elastic Path Software Inc., 2012
 */
package com.elasticpath.service.security.impl;

import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.core.userdetails.UserDetails;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAuthentication;

/**
 * Salt Source that gets the salt from the Customer Authentication object.
 */
public class CustomerSaltSourceImpl implements SaltSource {

	@Override
	public Object getSalt(final UserDetails user) {
		CustomerAuthentication customerAuthentication = ((Customer) user).getCustomerAuthentication();
		if (customerAuthentication == null) {
			return null;
		}
		return customerAuthentication.getSalt();
	}

}
