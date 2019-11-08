package com.elasticpath.cmclient.admin.configuration.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;

import com.elasticpath.cmclient.admin.configuration.AdminConfigurationMessages;
import com.elasticpath.cmclient.admin.configuration.dialogs.AddTagGroupDialog;
import com.elasticpath.cmclient.admin.configuration.models.TagGroupModel;
import com.elasticpath.cmclient.core.CoreImageRegistry;

/**
 * An action that opens the AddTagGroupDialog.
 */
public class AddTagGroupAction extends Action {

	private final Shell shell;
	private final TagGroupModel model;

	/**
	 * The constructor.
	 *
	 * @param shell the shell
	 * @param model the tag group model that will be used to save the new tag group
	 */
	public AddTagGroupAction(final Shell shell, final TagGroupModel model) {
		super(AdminConfigurationMessages.get().addGroupButton, CoreImageRegistry.IMAGE_ADD);
		this.shell = shell;
		this.model = model;
		this.setToolTipText(AdminConfigurationMessages.get().addGroupButton);
	}

	@Override
	public void run() {
		final AddTagGroupDialog dialog = new AddTagGroupDialog(shell, model);
		dialog.open();
	}

}