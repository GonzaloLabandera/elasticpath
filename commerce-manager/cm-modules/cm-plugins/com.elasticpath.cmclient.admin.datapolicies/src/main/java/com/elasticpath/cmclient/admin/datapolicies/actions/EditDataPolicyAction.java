/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cmclient.admin.datapolicies.actions;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;

import com.elasticpath.cmclient.admin.datapolicies.editors.DataPolicyEditor;
import com.elasticpath.cmclient.admin.datapolicies.editors.DataPolicyEditorInput;
import com.elasticpath.cmclient.admin.datapolicies.views.DataPolicyListView;
import com.elasticpath.domain.datapolicy.DataPolicy;

/**
 * Action to edit a data policy.
 */
public class EditDataPolicyAction extends Action {

	private static final Logger LOG = LogManager.getLogger(EditDataPolicyAction.class);

	private final DataPolicyListView listView;

	/**
	 * Constructor.
	 *
	 * @param listView        the customer list view
	 * @param text            the action's text
	 * @param imageDescriptor the action's image
	 */
	public EditDataPolicyAction(final DataPolicyListView listView, final String text, final ImageDescriptor imageDescriptor) {
		super(text, imageDescriptor);
		this.listView = listView;
	}

	@Override
	public void run() {
		Optional<DataPolicy> dataPolicyOptional = listView.getSelectedDataPolicy();

		if (dataPolicyOptional.isPresent()) {
			DataPolicy dataPolicy = dataPolicyOptional.get();
			final IEditorInput editorInput = new DataPolicyEditorInput(dataPolicy.getPolicyName(), dataPolicy.getUidPk(), DataPolicy.class);
			try {
				listView.getSite().getWorkbenchWindow().getActivePage().openEditor(editorInput, DataPolicyEditor.ID_EDITOR);
			} catch (PartInitException e) {
				LOG.error(e.getMessage());
			}
		}
	}
}
