/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.builder.customer;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.CustomerCreditCard;
import com.elasticpath.domain.customer.impl.AbstractCustomerCreditCardBuilder;

/**
 * A builder that builds {CustomerCreditCard}s for testing purposes.
 */
public class CustomerCreditCardBuilder extends AbstractCustomerCreditCardBuilder {
	private BeanFactory beanFactory;

	@Override
	protected CustomerCreditCard create() {
		return beanFactory.getBean(ContextIdNames.CUSTOMER_CREDIT_CARD);
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
