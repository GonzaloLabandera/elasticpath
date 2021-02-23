/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.editors.order;

import org.eclipse.ui.forms.IManagedForm;

import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.FulfillmentPlugin;

/**
 * A page to display the holds associated to an order.
 */
public class OrderHoldPage extends AbstractOrderPage {

	/**
	 * Constructor.
	 *
	 * @param editor <code>FormEditor</code>
	 */
	public OrderHoldPage(final AbstractCmClientFormEditor editor) {
		super(editor, "OrderHoldPage", FulfillmentMessages.get().OrderHoldPage_Title); //$NON-NLS-1$
	}

	@Override
	protected int getFormColumnsCount() {
		return 1;
	}

	@Override
	protected String getFormTitle() {
		return FulfillmentMessages.get().OrderHoldPage_Form_Title;
	}

	@Override
	// ---- DOCaddEditorSections
	protected void addEditorSections(final AbstractCmClientFormEditor editor, final IManagedForm managedForm) {
		managedForm.addPart(new OrderHoldListSectionPart(this, editor));
		addExtensionEditorSections(editor, managedForm, FulfillmentPlugin.PLUGIN_ID, this.getClass().getSimpleName());
	}
	// ---- DOCaddEditorSections
}