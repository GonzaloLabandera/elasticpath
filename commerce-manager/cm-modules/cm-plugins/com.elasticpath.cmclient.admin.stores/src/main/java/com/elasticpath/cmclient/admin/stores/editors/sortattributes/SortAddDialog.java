/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.admin.stores.editors.sortattributes;

import java.util.Map;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Shell;

import com.elasticpath.cmclient.admin.stores.AdminStoresMessages;
import com.elasticpath.cmclient.admin.stores.actions.StoreSortAttributes;
import com.elasticpath.cmclient.core.helpers.store.StoreEditorModel;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.domain.search.SortAttribute;

/**
 * Dialog for adding sort attributes.
 */
public class SortAddDialog extends AbstractSortDialog {

	private final Map<String, SortAttribute> availableSortModels;

	/**
	 * Constructor.
	 * @param parentShell the main composite
	 * @param storeEditorModel store model
	 * @param storeSortAttributes class that retrieves sort attributes
	 */
	public SortAddDialog(final Shell parentShell, final StoreEditorModel storeEditorModel, final StoreSortAttributes storeSortAttributes) {
		super(parentShell, storeEditorModel);
		availableSortModels = storeSortAttributes.getAllAvailableSortAttributes();
	}

	@Override
	public void createSortAttributeName(final IEpLayoutComposite parent) {
		CCombo cCombo = parent.addComboBox(EpControlFactory.EpState.READ_ONLY, parent.createLayoutData());
		cCombo.setEnabled(true);
		availableSortModels.keySet().stream()
				.sorted()
				.forEach(cCombo::add);

		cCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				updateCurrentlySelectedSortAttribute();
				setSelectedSortAttribute(availableSortModels.get(cCombo.getItem(cCombo.getSelectionIndex())));
				updateDisplayNameTextField();
				enableDropdowns();
			}
		});

		GridData textLayoutData = (GridData) cCombo.getLayoutData();
		textLayoutData.widthHint = DROPDOWN_WIDTH;
	}

	@Override
	public String getTitle() {
		return AdminStoresMessages.get().SortAddAttributeDialogTitle;
	}
}
