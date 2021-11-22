/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.auth.impl;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.jaas.AuthorityGranter;
import org.springframework.security.core.GrantedAuthority;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.service.customer.CustomerService;

/**
 * <code>CustomerAuthorityGranterImpl</code> inspects a JAAS principal (namely a Customer) and returns the
 * role names that should be granted to this principal from the Customer object.
 */
public class CustomerAuthorityGranterImpl implements AuthorityGranter {
	private static final Logger LOG = LogManager.getLogger(CustomerAuthorityGranterImpl.class);

	private CustomerService customerService;

	/**
	 * Set the Customer service.
	 *
	 * @param customerService the customer service
	 */
	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}

	/**
	 * Grants the Customer roles to the Customer principal returned by JAAS.
	 *
	 * @param principal - The principal returned from the <code>LoginContext</code> subject
	 * @return a java.util.Set of role names to grant
	 */
	@Override
	public Set<String> grant(final Principal principal) {
		Set<String> rtnSet = new HashSet<>();
		String sharedId = principal.getName();
		Customer customer = customerService.findBySharedId(sharedId, CustomerType.REGISTERED_USER);
		if (customer == null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("No customer found with name " + sharedId);
			}
		} else {
			Collection<? extends GrantedAuthority> authorities = customer.getAuthorities();
			if (LOG.isDebugEnabled()) {
				LOG.debug(authorities.size() + " authorities found");
			}
			for (GrantedAuthority element : authorities) {
				rtnSet.add(element.getAuthority());
				if (LOG.isDebugEnabled()) {
					LOG.debug("Added authority: " + element.getAuthority());
				}
			}
		}
		return rtnSet;
	}

}