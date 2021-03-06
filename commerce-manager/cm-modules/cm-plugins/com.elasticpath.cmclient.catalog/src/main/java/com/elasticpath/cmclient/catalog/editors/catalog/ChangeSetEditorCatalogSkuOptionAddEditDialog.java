/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.catalog.editors.catalog;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.helpers.ChangeSetHelper;
import com.elasticpath.service.changeset.ChangeSetMemberAction;

/**
 *  *  An edit only dialog for SkuOptions in changesets.
 */
public class ChangeSetEditorCatalogSkuOptionAddEditDialog  extends CatalogSkuOptionAddEditDialog {

	private final ChangeSetHelper changeSetHelper = BeanLocator.getSingletonBean(ChangeSetHelper.BEAN_ID, ChangeSetHelper.class);

	@Override
	protected void performSaveOperation() {
		getSkuOptionService().update(getSkuOption());
		changeSetHelper.addObjectToChangeSet(getSkuOption(), ChangeSetMemberAction.EDIT);
	}


}
