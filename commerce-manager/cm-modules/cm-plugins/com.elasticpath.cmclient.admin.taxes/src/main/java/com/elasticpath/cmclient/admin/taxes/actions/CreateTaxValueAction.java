/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.admin.taxes.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.elasticpath.cmclient.admin.taxes.dialogs.ManageTaxValuesDialog;
import com.elasticpath.cmclient.admin.taxes.dialogs.TaxValueDialog;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.tax.TaxCategory;
import com.elasticpath.domain.tax.TaxRegion;
import com.elasticpath.service.tax.TaxJurisdictionService;

/**
 * Create Tax Value action.
 */
public class CreateTaxValueAction extends Action {

	/** The logger. */
	private static final Logger LOG = LogManager.getLogger(CreateTaxValueAction.class);

	/** Manage Tax Value Dialog. */
	private final ManageTaxValuesDialog manageTaxValueDialog;

	/**
	 * The constructor.
	 * 
	 * @param manageTaxValueDialog the ManageTaxValueDialog.
	 * @param text the tool tip text.
	 * @param imageDescriptor the image is shown at the title.
	 */
	public CreateTaxValueAction(final ManageTaxValuesDialog manageTaxValueDialog, final String text, final ImageDescriptor imageDescriptor) {
		super(text, imageDescriptor);
		this.manageTaxValueDialog = manageTaxValueDialog;
	}

	@Override
	public void run() {
		LOG.debug("Create Tax Value action called."); //$NON-NLS-1$

		TaxRegion taxRegion = BeanLocator.getPrototypeBean(ContextIdNames.TAX_REGION, TaxRegion.class);
		TaxCategory taxCategory = manageTaxValueDialog.getSelectedTaxCategory();

		boolean dialogOk = TaxValueDialog.openCreateDialog(manageTaxValueDialog.getShell(), taxCategory, taxRegion, taxCategory.getFieldMatchType()
				.getName());

		if (dialogOk) {
			taxCategory.addTaxRegion(taxRegion);
			TaxJurisdictionService taxJurisdictionService = BeanLocator
					.getSingletonBean(ContextIdNames.TAX_JURISDICTION_SERVICE, TaxJurisdictionService.class);
			taxJurisdictionService.update(manageTaxValueDialog.getSelectedTaxJurisdiction());
			manageTaxValueDialog.refreshTaxRegions();

		}
	}
}
