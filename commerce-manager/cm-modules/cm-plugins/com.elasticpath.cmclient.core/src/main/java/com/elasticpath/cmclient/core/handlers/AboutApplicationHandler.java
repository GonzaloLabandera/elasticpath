/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.cmclient.core.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import com.elasticpath.cmclient.core.ui.dialog.AboutApplicationDialog;

/**
 * Used to open up the change pagination settings dialog.
 */
public class AboutApplicationHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent arg0) throws ExecutionException {
		new AboutApplicationDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).open();
		return null;
	}
}
