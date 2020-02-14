/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.cmclient.fulfillment.editors.order.actions.impl;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import com.elasticpath.cmclient.fulfillment.editors.order.dialog.OrderPaymentDetailsDialog;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;

/**
 * An Action that is triggered when the user clicks on View Order Details button on Payments tab.
 */
public class ViewOrderPaymentDetailsAction extends Action {

	private final OrderPayment orderPayment;

	/**
	 * Create a new instance of {@link ViewOrderPaymentDetailsAction}.
	 * @param orderPayment {@link OrderPayment} selected by the user.
	 */
	public ViewOrderPaymentDetailsAction(final OrderPayment orderPayment) {
		this.orderPayment = orderPayment;
	}

	@Override
	public void run() {
		OrderPaymentDetailsDialog orderPaymentDetailsDialog =
				new OrderPaymentDetailsDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), orderPayment);
		orderPaymentDetailsDialog.open();
	}
}
