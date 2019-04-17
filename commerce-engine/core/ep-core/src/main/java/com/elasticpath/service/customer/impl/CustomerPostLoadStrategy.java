/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.customer.impl;

import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.persistence.api.support.PersistablePostLoadStrategy;
import com.elasticpath.service.attribute.AttributeService;

/**
 * This strategy injects the CustomerProfileAttribute metadata needed to set customer attributes
 * onto each CustomerImpl domain object after they are loaded from persistence.
 */
public class CustomerPostLoadStrategy implements PersistablePostLoadStrategy<CustomerImpl> {
	private AttributeService attributeService;

	@Override
	public boolean canProcess(final Object obj) {
		return CustomerImpl.class.isInstance(obj);
	}

	@Override
	public void process(final CustomerImpl customer) {
		customer.setCustomerProfileAttributes(
				getAttributeService().getCustomerProfileAttributesMap());
	}

	public void setAttributeService(final AttributeService attributeService) {
		this.attributeService = attributeService;
	}

	protected AttributeService getAttributeService() {
		return attributeService;
	}
}
