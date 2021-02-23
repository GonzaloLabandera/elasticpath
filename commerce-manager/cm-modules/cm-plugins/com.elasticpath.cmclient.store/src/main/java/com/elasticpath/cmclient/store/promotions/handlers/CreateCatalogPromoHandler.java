/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.store.promotions.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.wizard.EpWizardDialog;
import com.elasticpath.cmclient.policy.common.PolicyActionContainer;
import com.elasticpath.cmclient.policy.ui.AbstractPolicyAwareHandler;
import com.elasticpath.cmclient.store.promotions.wizard.NewCatalogPromotionWizard;

/**
 * Creates a catalog promotion by opening up the new store promotion wizard. If the wizard is
 * completed successfully, the promotion is saved.
 */
public class CreateCatalogPromoHandler extends AbstractPolicyAwareHandler {

	private final PolicyActionContainer handlerContainer = addPolicyActionContainer("createCatalogPromoHandler"); //$NON-NLS-1$

	@Override
	public Object execute(final ExecutionEvent arg0) throws ExecutionException {
		final NewCatalogPromotionWizard wizard = new NewCatalogPromotionWizard();
		final WizardDialog dialog = new EpWizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
		dialog.setMinimumPageSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		dialog.addPageChangingListener(wizard);

		if (Window.OK == dialog.open()) {
			return wizard.getModel();
		}

		return null;
	}
	
	@Override
	public boolean isEnabled() {
		boolean enabled = true;
		if (getStatePolicy() != null) {
			enabled = (EpState.EDITABLE == getStatePolicy().determineState(handlerContainer));
		}
		return enabled;		
	}

	@Override
	public String getTargetIdentifier() {
		return "createCatalogPromoHandler"; //$NON-NLS-1$
	}
}
