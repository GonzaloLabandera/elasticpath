/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.admin.taxes.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;

import com.elasticpath.cmclient.admin.taxes.TaxesMessages;
import com.elasticpath.cmclient.admin.taxes.dialogs.TaxJurisdictionDialog;
import com.elasticpath.cmclient.admin.taxes.views.TaxJurisdictionsListView;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CorePlugin;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.misc.Geography;
import com.elasticpath.domain.tax.TaxJurisdiction;
import com.elasticpath.service.tax.TaxJurisdictionService;

/**
 * Edit Tax Jurisdiction implementation.
 */
public class EditTaxJurisdictionAction extends Action {

	/** The logger. */
	private static final Logger LOG = LogManager.getLogger(EditTaxJurisdictionAction.class);

	/** TaxJurisdictionsListView list view. */
	private final TaxJurisdictionsListView listView;

	/**
	 * The constructor.
	 *
	 * @param listView the tax jurisdictions list view.
	 * @param text the tool tip text.
	 * @param imageDescriptor the image is shown at the title.
	 */
	public EditTaxJurisdictionAction(final TaxJurisdictionsListView listView, final String text, final ImageDescriptor imageDescriptor) {
		super(text, imageDescriptor);
		this.listView = listView;
	}

	@Override
	public void run() {
		LOG.debug("Edit TaxJurisdiction Action called."); //$NON-NLS-1$

		TaxJurisdictionService taxJurisdictionService = BeanLocator
				.getSingletonBean(ContextIdNames.TAX_JURISDICTION_SERVICE, TaxJurisdictionService.class);

		TaxJurisdiction selectedTaxJurisdiction = listView.getSelectedTaxJurisdiction();

		TaxJurisdiction taxJurisdictionToEdit = taxJurisdictionService.get(selectedTaxJurisdiction.getUidPk());

		if (taxJurisdictionToEdit == null) {
			Geography geography = BeanLocator.getSingletonBean(ContextIdNames.GEOGRAPHY, Geography.class);
			String countryName = geography.getCountryDisplayName(selectedTaxJurisdiction.getRegionCode(), CorePlugin.getDefault().getDefaultLocale());
			MessageDialog.openInformation(listView.getSite().getShell(), TaxesMessages.get().NoLongerExistTaxJurisdictionMsgBoxTitle,

					NLS.bind(TaxesMessages.get().NoLongerExistTaxJurisdictionMsgBoxText,
					countryName));
			listView.refreshViewerInput();
			return;
		}

		final boolean dialogOk = TaxJurisdictionDialog.openEditDialog(listView.getSite().getShell(), taxJurisdictionToEdit);
		if (dialogOk) {
			taxJurisdictionService.update(taxJurisdictionToEdit);
			listView.refreshViewerInput();
		}
	}

}
