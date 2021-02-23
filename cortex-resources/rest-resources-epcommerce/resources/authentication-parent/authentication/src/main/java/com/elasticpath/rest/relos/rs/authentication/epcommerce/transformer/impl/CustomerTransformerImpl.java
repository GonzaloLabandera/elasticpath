/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.relos.rs.authentication.epcommerce.transformer.impl;

import javax.json.Json;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.identity.util.PrincipalsUtil;
import com.elasticpath.rest.relos.rs.authentication.User;
import com.elasticpath.rest.relos.rs.authentication.epcommerce.impl.UserImpl;
import com.elasticpath.rest.relos.rs.authentication.epcommerce.transformer.CustomerTransformer;
import com.elasticpath.rest.resource.transform.AbstractDomainTransformer;

/**
 * Transforms a {@link Customer} into a  instance {@link User}.
 */
@Component(property = AbstractDomainTransformer.DS_SERVICE_RANKING)
public class CustomerTransformerImpl implements CustomerTransformer {

	@Override
	public User transform(final Customer customer) {
		assert customer != null : "Customer is null.";
		assert customer.getCustomerAuthentication() != null : "Customer authentication is null";

		User user = new UserImpl();

		user.setRequestedScope(customer.getStoreCode());
		user.setUsername(customer.getUserId());

		/* 	We need a way for the EpUserPasswordEncoder to be aware of both the password and salt so it can hash using the old SHA256 encoding
			approach. However, Spring security only passes a single field for the password hash to the Bcrypt encoder (because the salt is
			included in the Bcrypt hash). Therefore, we encode the password hash and salt in a JSON object which is decoded within
			EpUserPasswordEncoder (if the salt is present). This also allows us to differentiate between a password that is hashed with
			Bcrypt(password) vs Bcrypt(SHA256(password)).
		 */
		String salt = customer.getCustomerAuthentication().getSalt();
		if (StringUtils.isEmpty(salt)) {
			user.setPassword(customer.getCustomerAuthentication().getPassword());
		} else {
			String passwordWithSalt = Json.createObjectBuilder()
					.add("password", customer.getCustomerAuthentication().getPassword())
					.add("salt", customer.getCustomerAuthentication().getSalt())
					.build()
					.toString();

			user.setPassword(passwordWithSalt);
		}

		user.setPrincipals(PrincipalsUtil.createRolePrincipals(customer.getCustomerRoleMapper().getAllRoles()));
		setUserStatesFromCustomerStatus(user, customer.getStatus());
		user.setUserId(customer.getGuid());
		user.setAccountExpired(!customer.isAccountNonExpired());

		return user;
	}

	private void setUserStatesFromCustomerStatus(final User user, final int status) {
		boolean active = status == Customer.STATUS_ACTIVE;
		user.setAccountEnabled(active);
		user.setAccountLocked(!active);
	}
}
