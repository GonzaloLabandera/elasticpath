/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.catalog.dialogs.catalog;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.helpers.ChangeSetHelper;
import com.elasticpath.service.changeset.ChangeSetMemberAction;

/**
 *  An edit only dialog for attributes in changesets.
 */
public class ChangeSetEditorAttributeEditDialog extends CatalogAttributesAddEditDialog {

	private final ChangeSetHelper changeSetHelper = BeanLocator.getSingletonBean(ChangeSetHelper.BEAN_ID, ChangeSetHelper.class);

	@Override
	protected void performSaveOperation() {

		if (changeSetHelper.isChangeSetsEnabled() && changeSetHelper.getActiveChangeSet() != null) {
			if (isEditMode()) {
				getCatalogModel().getAttributeTableItems().addModifiedItem(getAttribute());
			} else {
				getCatalogModel().getAttributeTableItems().addAddedItem(getAttribute());
			}
		}
		getAttributeService().update(getAttribute());
		changeSetHelper.addObjectToChangeSet(getAttribute(), ChangeSetMemberAction.EDIT);
	}

}
