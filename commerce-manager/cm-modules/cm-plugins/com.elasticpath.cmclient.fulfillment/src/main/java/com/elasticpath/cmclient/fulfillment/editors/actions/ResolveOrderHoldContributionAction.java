/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.editors.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.LoginManager;
import com.elasticpath.cmclient.core.service.AuthorizationService;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.editors.order.dialog.EditOrderHoldDialog;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;
import com.elasticpath.service.order.OrderHoldService;

/**
 * An action that displays a dialog to capture a comment, then resolves the hold.
 */
public class ResolveOrderHoldContributionAction extends Action {


	private final OrderHoldService orderHoldService = BeanLocator.getSingletonBean(ContextIdNames.ORDER_HOLD_SERVICE, OrderHoldService.class);

	private final OrderHold orderHold;
	private final Order order;
	private boolean viewOnly;

	/**
	 * Constructor.
	 *
	 * @param orderHold the order hold to view
	 * @param order the order that owns the order hold
	 */
	public ResolveOrderHoldContributionAction(final OrderHold orderHold,
											  final Order order) {
		super(FulfillmentMessages.get().OrderHoldView_Button, CoreImageRegistry.IMAGE_OPEN);
		setToolTipText(FulfillmentMessages.get().OrderHoldView_Button);
		this.orderHold = orderHold;
		this.order = order;
	}

	@Override
	public void run() {
		final EditOrderHoldDialog dialog =
				new EditOrderHoldDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), orderHold, viewOnly) {
					@Override
					protected void okPressed() {
						if (AuthorizationService.getInstance().isAuthorizedWithPermission(orderHold.getPermission())) {
							CmUser cmUser = LoginManager.getCmUser();
							orderHold.setStatus(OrderHoldStatus.RESOLVE_PENDING);
							orderHoldService.update(orderHold);
							orderHoldService.markHoldResolved(order, orderHold, cmUser.getUserName(), getComment());
						}
						super.okPressed();
					}

					@Override
					protected String getInitialMessage() {
						return FulfillmentMessages.get().OrderHoldDialog_ResolveHoldMessage;
					}

					@Override
					protected String getWindowTitle() {
						return FulfillmentMessages.get().OrderHoldDialog_ResolveHoldWindowTitle;
					}
				};
		dialog.open();
	}

	/**
	 * Sets the view only mode.
	 *
	 * @param isViewOnly whether the dialog should be view only
	 */
	public void setViewOnly(final boolean isViewOnly) {
		this.viewOnly = isViewOnly;
	}
}