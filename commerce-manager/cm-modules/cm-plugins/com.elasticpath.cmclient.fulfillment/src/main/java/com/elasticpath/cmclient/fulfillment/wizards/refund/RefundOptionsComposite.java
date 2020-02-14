/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards.refund;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;

import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;

/**
 * Refund Options Composite.
 */
public final class RefundOptionsComposite {

	private Button refundToOriginalPaymentSourceRadioButton;
	private Button manualRefundRadioButton;
	private final IEpLayoutComposite mainComposite;

	/**
	 * The constructor.
	 *
	 * @param pageComposite composite.
	 * @param layoutData    layout.
	 */
	private RefundOptionsComposite(final IEpLayoutComposite pageComposite, final IEpLayoutData layoutData) {
		this.mainComposite = pageComposite.addGridLayoutComposite(1, false, layoutData);
	}

	/**
	 * The RefundOptionsComposite factory method.
	 *
	 * @param pageComposite composite.
	 * @param layoutData    layout.
	 * @return refund options composite.
	 */
	public static RefundOptionsComposite createRefundOptionsComposite(final IEpLayoutComposite pageComposite, final IEpLayoutData layoutData) {
		RefundOptionsComposite refundOptionsComposite = new RefundOptionsComposite(pageComposite, layoutData);
		refundOptionsComposite.createEpPageContent();
		return refundOptionsComposite;
	}

	private void createEpPageContent() {
		IEpLayoutComposite refundComposite = mainComposite;

		refundToOriginalPaymentSourceRadioButton =
				refundComposite.addRadioButton(FulfillmentMessages.get().RefundOptionsComposite_ReturnToOriginal_RadioButton,
						EpState.EDITABLE, refundComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING)); //$NON-NLS-1$

		manualRefundRadioButton =
				refundComposite.addRadioButton(FulfillmentMessages.get().RefundOptionsComposite_ManualRefund_RadioButton,
						EpState.EDITABLE, refundComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING)); //$NON-NLS-1$
	}

	/**
	 * Only Manual Refund enabled.
	 *
	 * @param enabled if control is enabled.
	 */
	public void enableOnlyManualRefund(final boolean enabled) {
		refundToOriginalPaymentSourceRadioButton.setSelection(false);
		refundToOriginalPaymentSourceRadioButton.setEnabled(false);
		manualRefundRadioButton.setSelection(true);
		manualRefundRadioButton.setEnabled(enabled);
	}

	/**
	 * Enables composite and controls if the argument is <code>true</code>,
	 * and disables it otherwise.
	 *
	 * @param enabled if control should be enabled.
	 */
	public void setEnabled(final boolean enabled) {
		mainComposite.getSwtComposite().setEnabled(enabled);
		refundToOriginalPaymentSourceRadioButton.setSelection(enabled);
		refundToOriginalPaymentSourceRadioButton.setEnabled(enabled);
		manualRefundRadioButton.setSelection(false);
		manualRefundRadioButton.setEnabled(enabled);
	}

	/**
	 * @return true if original payment instrument or manual refund selection was made.
	 */
	public boolean isValid() {
		return refundToOriginalPaymentSourceRadioButton.getSelection() || manualRefundRadioButton.getSelection();
	}

	/**
	 * @return true if original payment is selected.
	 */
	public boolean useOriginalPayment() {
		return refundToOriginalPaymentSourceRadioButton.getSelection();
	}

	/**
	 * Caution label container. Needed to provide bold "Note" part of the label.
	 */
	private static final class CautionLabelContainer {
		private final IEpLayoutComposite composite;
		private Label cautionLabel;
		private Label cautionHeaderLabel;

		private CautionLabelContainer(final IEpLayoutComposite composite) {
			this.composite = composite;
		}

		public void createControls() {
			IEpLayoutComposite labelComposite = composite.addTableWrapLayoutComposite(2, false,
					composite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));

			cautionHeaderLabel = labelComposite.addLabelBold(FulfillmentMessages.get().RefundOptionsComposite_CautionHeader_Label,
					labelComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));

			cautionLabel = labelComposite.addLabel(FulfillmentMessages.get().RefundOptionsComposite_CautionRefund_Label,
					labelComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
		}

		public void setVisible(final boolean visible) {
			cautionHeaderLabel.setVisible(visible);
			cautionLabel.setVisible(visible);
		}

		public void setEnabled(final boolean enabled) {
			cautionHeaderLabel.setEnabled(enabled);
			cautionLabel.setEnabled(enabled);
		}
	}
}
