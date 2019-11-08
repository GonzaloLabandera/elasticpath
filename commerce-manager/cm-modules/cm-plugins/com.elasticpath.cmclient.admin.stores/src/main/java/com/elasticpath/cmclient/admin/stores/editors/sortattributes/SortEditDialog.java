/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.editors.sortattributes;

import static com.elasticpath.service.search.solr.FacetConstants.ATTRIBUTE_LABEL;
import static com.elasticpath.service.search.solr.FacetConstants.FIELD_LABEL;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.elasticpath.cmclient.admin.stores.AdminStoresMessages;
import com.elasticpath.cmclient.core.helpers.store.StoreEditorModel;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortAttributeGroup;

/**
 * Dialog for editing existing sort attributes.
 */
public class SortEditDialog extends AbstractSortDialog {

	/**
	 * Constructor.
	 * @param parentShell the main shell
	 * @param storeEditorModel store model
	 * @param sortAttribute sort attribute to be edited
	 */
	public SortEditDialog(final Shell parentShell, final StoreEditorModel storeEditorModel, final SortAttribute sortAttribute) {
		super(parentShell, storeEditorModel);
		setSelectedSortAttribute(sortAttribute);
	}

	@Override
	public void createSortAttributeName(final IEpLayoutComposite parent) {
		Text text = parent.addTextField(EpControlFactory.EpState.READ_ONLY, parent.createLayoutData());
		String label =  getSelectedSortAttribute().getSortAttributeGroup().getOrdinal() == SortAttributeGroup.FIELD_TYPE.getOrdinal()
				? FIELD_LABEL : ATTRIBUTE_LABEL;
		text.setText(label + getSelectedSortAttribute().getBusinessObjectId());

		GridData textLayoutData = (GridData) text.getLayoutData();
		textLayoutData.widthHint = ROW_TEXT_FIELD_WIDTH;
	}

	@Override
	public String getTitle() {
		return AdminStoresMessages.get().SortEditAttributeDialogTitle;
	}
}
