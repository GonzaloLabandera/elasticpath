/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.admin.configuration.dialogs;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.elasticpath.cmclient.admin.configuration.AdminConfigurationMessages;
import com.elasticpath.cmclient.admin.configuration.AdminConfigurationPlugin;
import com.elasticpath.cmclient.admin.configuration.models.TagGroupModel;
import com.elasticpath.cmclient.admin.configuration.views.TagGroupDisplayComposite;
import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.ui.dialog.AbstractEpDialog;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.tags.domain.TagGroup;

/**
 * A dialog to present a UI for addition of a new TagGroup.
 */
public class AddTagGroupDialog extends AbstractEpDialog {

	private static final int DIALOG_WIDTH = 730;
	private static final int DIALOG_HEIGHT = 275;

	private final DataBindingContext bindingContext;

	private final TagGroupModel model;

	private TagGroup tagGroup;

	private TagGroupDisplayComposite displayComposite;

	/**
	 * Constructor for the AddTagGroupDialog for creating a new tag group.
	 *
	 * @param parentShell of the Shell
	 * @param model       the model used to save the TagGroup
	 */
	public AddTagGroupDialog(final Shell parentShell, final TagGroupModel model) {
		super(parentShell, 2, false);
		this.model = model;
		this.bindingContext = new DataBindingContext();
		this.tagGroup = model.createTagGroup();
	}

	@Override
	protected void bindControls() {
		displayComposite.bindFields(bindingContext, this);
	}

	@Override
	protected String getPluginId() {
		return AdminConfigurationPlugin.PLUGIN_ID;
	}

	@Override
	public Object getModel() {
		return tagGroup;
	}

	@Override
	protected void populateControls() {
		//no controls to populate as we are using a composite to populate them
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createEpOkButton(parent, CoreMessages.get().AbstractEpDialog_ButtonSave, null);
		createEpCancelButton(parent);
	}

	@Override
	protected void createEpDialogContent(final IEpLayoutComposite dialogComposite) {
		displayComposite = new TagGroupDisplayComposite(dialogComposite);
		displayComposite.setTagGroup(tagGroup);
	}

	@Override
	protected void okPressed() {
		tagGroup = displayComposite.getUpdatedTagGroup();
		if (!isCodeExist()) {
			model.updateTagGroup(tagGroup);
			super.okPressed();
		}
	}

	private boolean isCodeExist() {
		boolean keyExists = model.tagGroupCodeExists(tagGroup);
		if (keyExists) {
			setErrorMessage(
					NLS.bind(AdminConfigurationMessages.get().TagGroupCodeExists,
							tagGroup.getGuid()));
		}
		return keyExists;
	}

	@Override
	protected String getInitialMessage() {
		return AdminConfigurationMessages.get().addGroupDialogTitle;
	}

	@Override
	protected String getTitle() {
		return AdminConfigurationMessages.get().addGroupDialogTitle;
	}

	@Override
	protected String getWindowTitle() {
		return AdminConfigurationMessages.get().addGroupDialogTitle;
	}

	@Override
	protected Image getWindowImage() {
		return null;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(DIALOG_WIDTH, DIALOG_HEIGHT);
	}

}
