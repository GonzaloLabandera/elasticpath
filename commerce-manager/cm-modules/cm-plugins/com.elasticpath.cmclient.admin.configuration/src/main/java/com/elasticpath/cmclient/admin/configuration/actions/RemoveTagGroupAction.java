package com.elasticpath.cmclient.admin.configuration.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

import com.elasticpath.cmclient.admin.configuration.AdminConfigurationMessages;
import com.elasticpath.cmclient.admin.configuration.models.TagGroupModel;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.tags.domain.TagGroup;

/**
 * An action that calls the model to remove the selected tag group.
 */
public class RemoveTagGroupAction extends Action {

	private final TagGroupModel model;
	private final TableViewer tagGroupTableViewer;

	/**
	 * The constructor.
	 *
	 * @param model               the tag group model
	 * @param tagGroupTableViewer the table viewer which is displaying the tag group selection
	 */
	public RemoveTagGroupAction(final TagGroupModel model, final TableViewer tagGroupTableViewer) {
		super(AdminConfigurationMessages.get().removeGroupButton, CoreImageRegistry.IMAGE_REMOVE);
		this.model = model;
		this.tagGroupTableViewer = tagGroupTableViewer;
		this.setToolTipText(AdminConfigurationMessages.get().addGroupButton);
	}

	@Override
	public void run() {
		TagGroup tagGroupToDelete;
		if (tagGroupTableViewer.getSelection() != null
				&& tagGroupTableViewer.getSelection() instanceof IStructuredSelection
				&& ((IStructuredSelection) tagGroupTableViewer.getSelection()).getFirstElement() != null
				&& ((IStructuredSelection) tagGroupTableViewer.getSelection()).getFirstElement() instanceof TagGroup
		) {
			tagGroupToDelete = (TagGroup) ((IStructuredSelection) tagGroupTableViewer.getSelection()).getFirstElement();
			model.removeTagGroup(tagGroupToDelete);
		}
	}
}

