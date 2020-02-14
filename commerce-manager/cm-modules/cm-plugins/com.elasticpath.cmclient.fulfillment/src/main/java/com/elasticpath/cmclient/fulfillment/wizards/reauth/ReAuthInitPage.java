/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards.reauth;

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.wizard.AbstractEPWizardPage;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;

/**
 * First page of reauthorization wizard. User can select payment sources for the order.
 */
public class ReAuthInitPage extends AbstractEPWizardPage<ReAuthorizationItem> {

	private CCombo paymentSourceCombo;

	/**
	 * The constructor.
	 *
	 * @param pageName           name of this page. Will be passed to super.
	 * @param dataBindingContext binding context
	 */
	protected ReAuthInitPage(final String pageName,
							 final DataBindingContext dataBindingContext) {
		super(1, true, pageName, dataBindingContext);
		setMessage(FulfillmentMessages.get().CaptureWizard_CardInfoPage_Message);
	}

	@Override
	protected void bindControls() {
		// nothing
	}

	@Override
	protected void createEpPageContent(final IEpLayoutComposite pageComposite) {
		setControl(pageComposite.getSwtComposite());

		final IEpLayoutComposite page = pageComposite.addGridLayoutComposite(2, false,
				pageComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true));

		page.addLabelBold(FulfillmentMessages.get().ReAuthWizard_PrevAuthAmount,
				page.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
		page.addLabel(FulfillmentMessages.get().formatMoneyAsString(
				getModel().getOriginalAuthorizedAmount(), getModel().getOrder().getLocale()),
				page.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));

		page.addLabelBold(FulfillmentMessages.get().ReAuthWizard_NewAuthAmount,
				page.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
		page.addLabel(FulfillmentMessages.get().formatMoneyAsString(
				getModel().getNewAuthorizedAmount(), getModel().getOrder().getLocale()),
				page.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));

		page.addLabelBoldRequired(FulfillmentMessages.get().ReAuthWizard_PaymentSource, EpState.EDITABLE,
				page.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.END));
		paymentSourceCombo = page.addComboBox(EpState.EDITABLE,
				page.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.END));
		paymentSourceCombo.addSelectionListener(new PaymentSourceComboListener());
		paymentSourceCombo.select(-1);

		pageComposite.addEmptyComponent(pageComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL));

		final IEpLayoutComposite notePage = pageComposite.addGridLayoutComposite(2, false,
				pageComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.END, true, false));
		notePage.addLabelBoldRequired(FulfillmentMessages.get().RefundWizard_Note, EpState.EDITABLE, notePage.createLayoutData());
		notePage.addLabel(FulfillmentMessages.get().CaptureWizard_Note_Text,
				notePage.createLayoutData(IEpLayoutData.END, IEpLayoutData.BEGINNING));
	}

	@Override
	protected void populateControls() {
		paymentSourceCombo.removeAll();
		paymentSourceCombo.add(FulfillmentMessages.get().ReAuthWizard_OriginalPaymentSource);
		paymentSourceCombo.select(0);
	}

	private List<PaymentInstrumentDTO> getSelectedInstruments() {
		return getModel().getOriginalInstruments();
	}

	@Override
	public ReAuthWizard getWizard() {
		return (ReAuthWizard) super.getWizard();
	}

	@Override
	public boolean beforeNext(final PageChangingEvent event) {
		getModel().setNewInstruments(getSelectedInstruments());
		return getWizard().process();
	}

	/**
	 * Selection listener for payment source drop downs. Used for enabling/disabling of the Next button.
	 */
	class PaymentSourceComboListener extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent event) {
			getWizard().getContainer().updateButtons();
		}

	}

}