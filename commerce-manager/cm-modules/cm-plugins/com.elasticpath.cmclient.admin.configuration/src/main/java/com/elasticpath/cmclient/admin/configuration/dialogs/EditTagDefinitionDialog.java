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
import com.elasticpath.cmclient.admin.configuration.views.TagDefinitionDisplayComposite;
import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.ui.dialog.AbstractEpDialog;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.domain.TagGroup;

/**
 * A dialog to present a UI for modification and creation of a TagDefinition.
 */
public class EditTagDefinitionDialog extends AbstractEpDialog {

	private static final int DIALOG_WIDTH = 735;
	private static final int DIALOG_HEIGHT = 750;

	private final DataBindingContext bindingContext;

	private final TagGroupModel model;

	private final TagGroup tagGroup;

	private final String titleMessage;

	private TagDefinition tagDefinition;

	private TagDefinitionDisplayComposite displayComposite;

	/**
	 * Constructor for the SettingValueDialog for either editing or creating settingDefinitions.
	 *
	 * @param parentShell of the Shell
	 * @param tagGroup    the tagGroup that the tag definition will belong to
	 * @param def         the tag definition to edit
	 * @param model       the model to use in operating on setting definitions
	 */
	public EditTagDefinitionDialog(final Shell parentShell, final TagGroup tagGroup, final TagDefinition def, final TagGroupModel model) {
		super(parentShell, 2, false);
		this.tagGroup = tagGroup;
		this.tagDefinition = def;
		this.model = model;
		this.bindingContext = new DataBindingContext();
		if (tagDefinition == null) {
			titleMessage = AdminConfigurationMessages.get().addTagDefinitionDialogTitle;
		} else {
			titleMessage = AdminConfigurationMessages.get().editTagDefinitionDialogTitle;
		}
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
		return model;
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
		displayComposite = new TagDefinitionDisplayComposite(dialogComposite, model);
		if (tagDefinition == null) {
			tagDefinition = model.createTagDefinition();
		}
		displayComposite.setTagDefinition(tagDefinition);
	}

	private boolean isDefinitionExist() {
		boolean definitionExists = model.tagDefinitionCodeExists(tagDefinition);
		if (definitionExists) {
			setErrorMessage(
					NLS.bind(AdminConfigurationMessages.get().TagDefinitionCodeExists,
							tagDefinition.getGuid()));
		} else {
			definitionExists = model.tagDefinitionNameExists(tagDefinition);
			if (definitionExists) {
				setErrorMessage(
						NLS.bind(AdminConfigurationMessages.get().TagDefinitionNameExists,
								tagDefinition.getName()));
			}
		}
		return definitionExists;
	}

	@Override
	protected void okPressed() {
		tagDefinition = displayComposite.getUpdatedTagDefinition();
		//allow save if this is a new tag and the guid and name are not in use OR this is an update to an existing tag
		//note that the ui does not allow the edit of the guid/name fields for an existing tag
		if (tagDefinition.isPersisted() || !isDefinitionExist()) {
			model.updateTagDefinition(tagDefinition, displayComposite.getSelectedDictionaries(), tagGroup);
			super.okPressed();
		}
	}

	@Override
	protected String getInitialMessage() {
		return titleMessage;
	}

	@Override
	protected String getTitle() {
		return titleMessage;
	}

	@Override
	protected String getWindowTitle() {
		return titleMessage;
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
