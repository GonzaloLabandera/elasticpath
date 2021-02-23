/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.editors.order.dialog;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.binding.EpDialogSupport;
import com.elasticpath.cmclient.core.ui.dialog.AbstractEpDialog;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.FulfillmentPlugin;
import com.elasticpath.domain.order.OrderHold;

/**
 * A dialog to display order hold comments.
 */
public class EditOrderHoldDialog extends AbstractEpDialog {

	private final OrderHold orderHold;

	private Text textArea;

	private Button cancelButton;

	private final DataBindingContext bindingContext;

	private final boolean viewOnly;

	/**
	 * Constructor.
	 *
	 * @param parentShell the Shell
	 * @param orderHold the OrderNote
	 * @param viewOnly the view only state
	 */
	public EditOrderHoldDialog(final Shell parentShell, final OrderHold orderHold, final boolean viewOnly) {
		super(parentShell, 2, false);
		this.orderHold = orderHold;

		this.bindingContext = new DataBindingContext();
		this.viewOnly = viewOnly;
	}

	@Override
	// ---- DOCbindControls
	protected void bindControls() {
		EpDialogSupport.create(this, this.bindingContext);
	}
	// ---- DOCbindControls

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		Button okButton = createEpOkButton(parent, "OK", null);
		if (viewOnly) {
			okButton.setGrayed(true);
		}

		cancelButton = createEpCancelButton(parent);
	}

	@Override
	protected void createEpDialogContent(final IEpLayoutComposite dialogComposite) {

		final IEpLayoutData labelData = dialogComposite.createLayoutData(IEpLayoutData.END, IEpLayoutData.FILL);
		final IEpLayoutData fieldDataText = dialogComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true);

		EpControlFactory.EpState state = EpControlFactory.EpState.EDITABLE;
		if (viewOnly) {
			state = EpControlFactory.EpState.READ_ONLY;
		}
		dialogComposite.addLabelBoldRequired(FulfillmentMessages.get().OrderHoldDialog_DialogLabelComment, state, labelData);
		textArea = dialogComposite.addTextArea(false, false, state, fieldDataText);
	}

	@Override
	protected String getPluginId() {
		return FulfillmentPlugin.PLUGIN_ID;
	}

	@Override
	public Object getModel() {
		return orderHold;
	}

	@Override
	protected void populateControls() {
		cancelButton.setFocus();
		if (orderHold.getReviewerNotes() != null) {
			textArea.setText(orderHold.getReviewerNotes());
		}
	}

	@Override
	protected void okPressed() {
		close();
	}

	@Override
	protected String getInitialMessage() {
		return FulfillmentMessages.get().OrderHoldDialog_ViewCommentMessage;
	}

	@Override
	protected String getTitle() {
		return null;
	}

	@Override
	protected String getWindowTitle() {
		return FulfillmentMessages.get().OrderHoldDialog_ViewDialogWindowTitle;
	}

	@Override
	protected Image getWindowImage() {
		return CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_OPEN);
	}

	public String getComment() {
		return textArea.getText();
	}

}