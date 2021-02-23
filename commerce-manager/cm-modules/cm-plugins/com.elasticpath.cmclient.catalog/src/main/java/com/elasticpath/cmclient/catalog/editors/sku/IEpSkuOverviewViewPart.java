/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.catalog.editors.sku;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.core.databinding.DataBindingContext;

import com.elasticpath.cmclient.core.ui.framework.ControlModificationListener;
import com.elasticpath.cmclient.core.ui.framework.IEpViewPart;
import com.elasticpath.cmclient.policy.StatePolicyDelegate;

/**
 * Aggregating interface for SkuOverview view part.
 */
public interface IEpSkuOverviewViewPart extends IEpViewPart, SelectionListener, ModifyListener, StatePolicyDelegate {

	/**
	 * Sets a control modification listener.
	 * 
	 * @param listener the listener to be set
	 */
	void setControlModificationListener(ControlModificationListener listener);
	
	
	/** 
	 * @param productSkuEventListener product sku event listener to use. 
	 */
	void setProductSkuEventListener(IProductSkuEventListener productSkuEventListener);
	
	
	/**
	 * Sets validate sku flag.
	 * 
	 * @param validateSku should the validation begin
	 */
	void setValidateSku(boolean validateSku);

	/**
	 * Binds the specific controls to the binding context for wizard dialog.
	 *
	 * @param bindingContext the context
	 */
	void bindWizardDialogControls(DataBindingContext bindingContext);

}
