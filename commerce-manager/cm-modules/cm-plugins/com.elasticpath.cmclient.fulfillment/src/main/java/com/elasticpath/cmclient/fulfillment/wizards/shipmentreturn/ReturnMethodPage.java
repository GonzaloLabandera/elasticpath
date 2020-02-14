/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards.shipmentreturn;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;

import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.wizard.AbstractEPWizardPage;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.wizards.refund.RefundOptionsComposite;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.service.order.ReturnExchangeRefundTypeEnum;

/**
 * Return method wizard page.
 */
public class ReturnMethodPage extends AbstractEPWizardPage<OrderReturnItem> {
	private static final int UPPER_COLUMN_COUNT = 3;
	private static final int LOWER_COLUMN_COUNT = 1;

	private Label totalRefundAmountLabel;
	private Button physicalReturnRequiredCheckbox;
	private RefundOptionsComposite refundOptionsComposite;

	/**
	 * The constructor.
	 *
	 * @param pageName The page name
	 */
	protected ReturnMethodPage(final String pageName) {
		super(1, true, pageName, new DataBindingContext());
		setMessage(FulfillmentMessages.get().ReturnWizard_MethodPage_Message);
	}

	@Override
	protected void bindControls() {
		// Nothing
	}

	@Override
	protected void populateControls() {
		physicalReturnRequiredCheckbox.setSelection(showPhysicalReturnCheckBox());
		refundOptionsComposite.setEnabled(!physicalReturnRequiredCheckbox.getSelection());

		populateRefundTotal();
	}

	private boolean showPhysicalReturnCheckBox() {
		return getWizard().getReturnWizardType() != ReturnWizard.ReturnWizardType.COMPLETE_RETURN;
	}

	private void populateRefundTotal() {
		OrderReturn orderReturn = getModel().getOrderReturn();
		Currency currency = orderReturn.getCurrency();
		BigDecimal refundTotal = orderReturn.getReturnTotal();
		Locale locale = orderReturn.getOrder().getLocale();
		totalRefundAmountLabel.setText(getMoneyFormatter().formatCurrency(currency, refundTotal, locale) + " " + currency.getCurrencyCode());
		totalRefundAmountLabel.pack(true);
	}

	@Override
	protected void createEpPageContent(final IEpLayoutComposite parent) {
		IEpLayoutComposite upperComposite = parent.addTableWrapLayoutComposite(UPPER_COLUMN_COUNT, false, parent.createLayoutData());
		IEpLayoutComposite physicalReturnComposite = parent.addTableWrapLayoutComposite(LOWER_COLUMN_COUNT, false, parent.createLayoutData());
		IEpLayoutComposite lowerComposite = parent.addTableWrapLayoutComposite(LOWER_COLUMN_COUNT, false, parent.createLayoutData());

		upperComposite.addLabelBold(FulfillmentMessages.get().ReturnWizard_TotalRefundAmount_Label, upperComposite.createLayoutData());
		totalRefundAmountLabel = upperComposite.addLabel(FulfillmentMessages.EMPTY_STRING, upperComposite.createLayoutData());

		physicalReturnRequiredCheckbox = physicalReturnComposite
				.addCheckBoxButton(FulfillmentMessages.get().ReturnWizard_PhysicalReturnRequired_Label, EpState.EDITABLE, null);
		physicalReturnRequiredCheckbox.setVisible(showPhysicalReturnCheckBox());
		physicalReturnRequiredCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				Button button = (Button) event.widget;
				refundOptionsComposite.setEnabled(!button.getSelection());
			}
		});

		refundOptionsComposite = RefundOptionsComposite.createRefundOptionsComposite(lowerComposite, null);

		this.setControl(parent.getSwtComposite());
	}

	/**
	 * RefundControl selection done by user.
	 *
	 * @return refund control selection
	 */
	public ReturnExchangeRefundTypeEnum getRefundControlResult() {
		if (physicalReturnRequiredCheckbox.getSelection()) {
			return ReturnExchangeRefundTypeEnum.PHYSICAL_RETURN_REQUIRED;
		}
		if (refundOptionsComposite.useOriginalPayment()) {
			return ReturnExchangeRefundTypeEnum.REFUND_TO_ORIGINAL;
		} else {
			return ReturnExchangeRefundTypeEnum.MANUAL_REFUND;
		}
	}

	@Override
	public boolean beforeNext(final PageChangingEvent event) {
		final ReturnWizard.ReturnWizardType returnWizardType = getWizard().getReturnWizardType();
		if (returnWizardType == ReturnWizard.ReturnWizardType.CREATE_RETURN) {
			return getWizard().createReturn();
		} else if (returnWizardType == ReturnWizard.ReturnWizardType.COMPLETE_RETURN) {
			return getWizard().completeReturn();
		}
		return true;
	}

	@Override
	public ReturnWizard getWizard() {
		return (ReturnWizard) super.getWizard();
	}

	@Override
	public boolean beforeFromPrev(final PageChangingEvent event) {
		populateRefundTotal();
		return true;
	}

}
