/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.event.OrderEventHelper;
import com.elasticpath.service.shoppingcart.actions.FinalizeCheckoutAction;
import com.elasticpath.service.shoppingcart.actions.FinalizeCheckoutActionContext;
import com.elasticpath.tags.TagSet;

/**
 * Logs a note on the order if the SHOPPING_CONTEXT_DATE_OVERRIDE trait was used.
 */
public class LogShoppingContextDateOverriddenAction implements FinalizeCheckoutAction {

	/** Tag name. */
	private static final String SHOPPING_DATE_OVERRIDE_KEY = "SHOPPING_CONTEXT_DATE_OVERRIDE";

	private OrderEventHelper orderEventHelper;

	@Override
	public void execute(final FinalizeCheckoutActionContext context) throws EpSystemException {
		TagSet tags = context.getCustomerSession().getCustomerTagSet();
		if (tags.getTags().containsKey(SHOPPING_DATE_OVERRIDE_KEY)) {
			String overrideDateString = (String) tags.getTagValue(SHOPPING_DATE_OVERRIDE_KEY).getValue();
			String message = "This order was placed with the SHOPPING_CONTEXT_DATE_OVERRIDE trait specified with a value of " + overrideDateString;
			orderEventHelper.logOrderNote(context.getOrder(), message);
		}
	}

	public OrderEventHelper getOrderEventHelper() {
		return orderEventHelper;
	}

	public void setOrderEventHelper(final OrderEventHelper orderEventHelper) {
		this.orderEventHelper = orderEventHelper;
	}

}
