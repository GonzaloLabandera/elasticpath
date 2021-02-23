/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;

/**
 * Checkout Action to set the transient event originator field on an order.
 */
public class SetOrderEventOriginatorCheckoutAction implements ReversibleCheckoutAction {

	private EventOriginatorHelper eventOriginatorHelper;

	@Override
	public void execute(final PreCaptureCheckoutActionContext context) throws EpSystemException {
		context.getOrder().setModifiedBy(getEventOriginatorHelper().getCustomerOriginator(context.getCustomer()));
	}

	@Override
	public void rollback(final PreCaptureCheckoutActionContext context) throws EpSystemException {
		// Do nothing
	}

	public EventOriginatorHelper getEventOriginatorHelper() {
		return eventOriginatorHelper;
	}

	public void setEventOriginatorHelper(final EventOriginatorHelper eventOriginatorHelper) {
		this.eventOriginatorHelper = eventOriginatorHelper;
	}
}
