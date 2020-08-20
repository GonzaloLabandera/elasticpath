/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.wizards.account;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.graphics.Image;

import com.elasticpath.cmclient.catalog.CatalogMessages;
import com.elasticpath.cmclient.catalog.editors.attribute.AttributeEditingSupport;
import com.elasticpath.cmclient.catalog.editors.attribute.AttributesLabelProviderUtil;
import com.elasticpath.cmclient.catalog.editors.attribute.AttributesViewPart;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableColumn;

/**
 * Customer Attributes UI controls.
 */
public class CustomerAttributesViewPart extends AttributesViewPart {

	/**
	 * Constructor.
	 *
	 * @param model          the object model.
	 * @param rolePermission the role permissions.
	 * @param toolbarManager the toolbar manager.
	 */
	public CustomerAttributesViewPart(final Object model, final EpControlFactory.EpState rolePermission, final IToolBarManager toolbarManager) {
		super(model, rolePermission, toolbarManager);
	}

	/**
	 * Creates UI controls.
	 *
	 * @param mainComposite the main composite
	 */
	@Override
	public void createControls(final IEpLayoutComposite mainComposite) {
		setMainComposite(mainComposite);
		// layout for the table area
		final IEpLayoutData tableLayoutData = mainComposite.createLayoutData(
				IEpLayoutData.FILL, IEpLayoutData.FILL, true, true);
		setAttributesTableViewer(getMainComposite().addTableViewer(false,
				getRolePermission(), tableLayoutData, ATTRIBUTES_VIEW_TABLE));

		// attributesTableViewer.getSwtTable().set
		// the name column content of the attribute table
		final IEpTableColumn nameColumn = getAttributesTableViewer().addTableColumn(
						CatalogMessages.get().ProductEditorAttributeSection_TableColumnTitle_Name,
						150);
		// The attribute type column content of the attribute table
		final IEpTableColumn typeColumn = getAttributesTableViewer().addTableColumn(
						CatalogMessages.get().ProductEditorAttributeSection_TableColumnTitle_Type,
						80);
		// The required column content of the attribute table
		final IEpTableColumn requiredColumn = getAttributesTableViewer().addTableColumn(
						CatalogMessages.get().ProductEditorAttributeSection_TableColumnTitle_Required,
						70);

		// The attribute value column content of the attribute table
		final IEpTableColumn valueColumn = getAttributesTableViewer().addTableColumn(
						CatalogMessages.get().ProductEditorAttributeSection_TableColumnTitle_Value,
						200);

		final AttributesLabelProviderUtil labelProviderUtil = new AttributesLabelProviderUtil(getRolePermission());
		labelProviderUtil.setNameColumnLabel(nameColumn);
		labelProviderUtil.setTypeColumnLabel(typeColumn);
		labelProviderUtil.setRequiredColumnLabel(requiredColumn);
		labelProviderUtil.setValueColumnLabel(valueColumn);

		if (getRolePermission() == EpControlFactory.EpState.EDITABLE) {
			// add EditSupport to the attribute value column
			AttributeEditingSupport editorSupport = new AttributeEditingSupport(getAttributesTableViewer(), this.getModel(), getDialogService());
			editorSupport.addAttributeChangedListener(this);
			valueColumn.setEditingSupport(editorSupport);

		}

		setButtonsComposite(mainComposite.addGridLayoutComposite(1, false, null));

		// create edit button for invoking the editing
		final Image editImage = CoreImageRegistry
				.getImage(CoreImageRegistry.IMAGE_EDIT);
		setEditButton(getButtonsComposite().addPushButton(
				CatalogMessages.get().AttributePage_ButtonEdit, editImage,
				getRolePermission(), mainComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING)));
		getEditButton().addSelectionListener(this);

		setResetButton(getButtonsComposite().addPushButton(
				CatalogMessages.get().AttributePage_ButtonReset, CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_X),
				getRolePermission(), mainComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING)));
		getResetButton().addSelectionListener(this);

		getEditButton().setEnabled(false);
		getResetButton().setEnabled(false);

		getAttributesTableViewer().setContentProvider(new ArrayContentProvider());

		if (getToolbarManager() != null) {
			this.createActions(getToolbarManager());
		}

		getAttributesTableViewer().getSwtTableViewer().addSelectionChangedListener(this);
	}
}
