/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.builder.customer;

import com.elasticpath.domain.customer.CustomerCreditCard;
import com.elasticpath.domain.customer.impl.AbstractCustomerCreditCardBuilder;
import com.elasticpath.domain.customer.impl.CustomerCreditCardImpl;

/**
 * {@link CustomerCreditCard} Builder that uses new to create instances.
 */
public class NewInstanceCustomerCreditCardBuilder extends AbstractCustomerCreditCardBuilder {

	@Override
	protected CustomerCreditCard create() {
		return new CustomerCreditCardImpl();
	}

}