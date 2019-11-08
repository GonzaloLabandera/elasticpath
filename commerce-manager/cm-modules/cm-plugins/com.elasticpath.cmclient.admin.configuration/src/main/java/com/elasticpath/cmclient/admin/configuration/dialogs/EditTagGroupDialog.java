/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.admin.configuration.dialogs;

import org.eclipse.core.databinding.DataBindingContext;
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
 * A dialog to present a UI for modification of a TagGroup.
 */
public class EditTagGroupDialog extends AbstractEpDialog {

	private static final int DIALOG_WIDTH = 730;
	private static final int DIALOG_HEIGHT = 275;

	private final DataBindingContext bindingContext;
	private final TagGroupModel model;

	private TagGroup tagGroup;
	private TagGroupDisplayComposite displayComposite;

	/**
	 * Constructor for the SettingValueDialog for either editing or creating settingDefinitions.
	 *
	 * @param parentShell of the Shell
	 * @param tagGroup    the tag group
	 * @param model       the model to use in operating on setting definitions
	 */
	public EditTagGroupDialog(final Shell parentShell, final TagGroup tagGroup, final TagGroupModel model) {
		super(parentShell, 2, false);
		this.tagGroup = tagGroup;
		this.model = model;
		this.bindingContext = new DataBindingContext();
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
		model.updateTagGroup(tagGroup);
		super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

	@Override
	protected String getInitialMessage() {
		return AdminConfigurationMessages.get().editGroupDialogTitle;
	}

	@Override
	protected String getTitle() {
		return AdminConfigurationMessages.get().editGroupDialogTitle;
	}

	@Override
	protected String getWindowTitle() {
		return AdminConfigurationMessages.get().editGroupDialogTitle;
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
