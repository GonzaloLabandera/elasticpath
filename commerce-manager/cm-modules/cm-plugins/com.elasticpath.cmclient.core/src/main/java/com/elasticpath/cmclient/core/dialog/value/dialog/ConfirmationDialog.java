/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.core.dialog.value.dialog;

import org.eclipse.jface.dialogs.IDialogLabelKeys;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * Confirmation Dialog for CM.
 */
public class ConfirmationDialog extends MessageDialog {

	private static final int DIALOG_WIDTH = 300;

	private static final int DIALOG_HEIGHT = 200;

	private static final String[] BUTTONS = {
			JFaceResources.getString(IDialogLabelKeys.OK_LABEL_KEY),
			JFaceResources.getString(IDialogLabelKeys.CANCEL_LABEL_KEY) };


	/**
	 * Constructor.
	 * @param parentShell shell
	 * @param dialogTitle title
	 * @param dialogMessage message
	 */
	public ConfirmationDialog(final Shell parentShell, final String dialogTitle, final String dialogMessage) {
		super(parentShell, dialogTitle, null, dialogMessage, MessageDialog.NONE, BUTTONS, 0);
		setShellStyle(getShellStyle() & ~SWT.CLOSE);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(DIALOG_WIDTH, DIALOG_HEIGHT);
	}
}
