package com.elasticpath.cmclient.admin.configuration.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Shell;

import com.elasticpath.cmclient.admin.configuration.AdminConfigurationMessages;
import com.elasticpath.cmclient.admin.configuration.dialogs.EditTagGroupDialog;
import com.elasticpath.cmclient.admin.configuration.models.TagGroupModel;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.tags.domain.TagGroup;

/**
 * An action that opens the EditTagGroupDialog.
 */
public class EditTagGroupAction extends Action {

	private final Shell shell;
	private final TagGroupModel tagGroupModel;
	private final TableViewer tagGroupTableViewer;

	/**
	 * The constructor.
	 *
	 * @param shell               the shell
	 * @param tagGroupModel       the model that will be used to update the tag group
	 * @param tagGroupTableViewer the table viewer to fetch the selected tag group from for editing
	 */
	public EditTagGroupAction(final Shell shell, final TagGroupModel tagGroupModel, final TableViewer tagGroupTableViewer) {
		super(AdminConfigurationMessages.get().editGroupButton, CoreImageRegistry.IMAGE_EDIT);
		this.shell = shell;
		this.tagGroupModel = tagGroupModel;
		this.tagGroupTableViewer = tagGroupTableViewer;
		this.setToolTipText(AdminConfigurationMessages.get().addGroupButton);
	}

	@Override
	public void run() {
		TagGroup tagGroupToEdit;
		if (tagGroupTableViewer.getSelection() != null
				&& tagGroupTableViewer.getSelection() instanceof IStructuredSelection
				&& ((IStructuredSelection) tagGroupTableViewer.getSelection()).getFirstElement() != null
				&& ((IStructuredSelection) tagGroupTableViewer.getSelection()).getFirstElement() instanceof TagGroup
		) {
			tagGroupToEdit = (TagGroup) ((IStructuredSelection) tagGroupTableViewer.getSelection()).getFirstElement();
			final EditTagGroupDialog dialog = new EditTagGroupDialog(shell, tagGroupToEdit, tagGroupModel);
			dialog.open();
		}
	}
}