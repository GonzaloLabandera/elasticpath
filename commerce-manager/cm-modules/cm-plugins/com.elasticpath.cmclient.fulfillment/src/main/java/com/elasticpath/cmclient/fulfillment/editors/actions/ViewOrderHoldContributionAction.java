package com.elasticpath.cmclient.fulfillment.editors.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.editors.order.dialog.EditOrderHoldDialog;
import com.elasticpath.domain.order.OrderHold;

/**
 * An action that display an uneditable dialog to view the comments associated with an order hold.
 */
public class ViewOrderHoldContributionAction extends Action {

	private final OrderHold orderHold;
	private boolean viewOnly;

	/**
	 * Constructor.
	 *
	 * @param orderHold the order hold to view
	 */
	public ViewOrderHoldContributionAction(final OrderHold orderHold) {
		super(FulfillmentMessages.get().OrderHoldView_Button, CoreImageRegistry.IMAGE_OPEN);
		setToolTipText(FulfillmentMessages.get().OrderHoldView_Button);
		this.orderHold = orderHold;
	}

	@Override
	public void run() {
		final EditOrderHoldDialog dialog =
				new EditOrderHoldDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						this.orderHold, this.viewOnly);
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