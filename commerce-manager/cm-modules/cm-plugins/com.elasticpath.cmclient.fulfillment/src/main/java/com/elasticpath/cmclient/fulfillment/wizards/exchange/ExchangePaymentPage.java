/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.wizards.exchange;

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.elasticpath.cmclient.core.binding.EpWizardPageSupport;
import com.elasticpath.cmclient.core.ui.framework.AbstractCmClientFormSectionPart;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.wizard.AbstractEPWizardPage;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.wizards.PaymentSourceOptionsComposite;
import com.elasticpath.cmclient.fulfillment.wizards.refund.RefundOptionsComposite;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.money.Money;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.order.ReturnExchangeRefundTypeEnum;

/**
 * Payment exchange wizard page.
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.GodClass"})
public class ExchangePaymentPage extends AbstractEPWizardPage<ExchangeModel> {

	private static final int CONTENT_HEIGHT_HINT = 550;

	private ExchangeSummarySectionPart exchangeSummarySectionPart;

	private RefundOptionsComposite refundOptionsComposite;
	private ReserveOptionsSectionPart reserveOptionsSectionPart;
	private PaymentSourceOptionsComposite paymentSourceOptionsComposite;

	private Button physicalReturnCheckbox;

	private Money exchangeTotal;
	private Money exchangeOrderTotal;

	/**
	 * The constructor.
	 *
	 * @param pageName the page name
	 */
	protected ExchangePaymentPage(final String pageName) {
		super(1, false, pageName, new DataBindingContext());
		setMessage(FulfillmentMessages.get().ExchangeWizard_PaymentPage_Message);
		EpWizardPageSupport.create(this, getDataBindingContext());
		setPageComplete(false);
	}

	@Override
	protected void bindControls() {
		// nothing to bind here
	}

	@Override
	protected void populateControls() {
		// nothing to populate here
		if (isCompleteExchange()) {
			beforeFromPrev(null);
		}
	}

	@Override
	protected void createEpPageContent(final IEpLayoutComposite parent) {
		IEpLayoutComposite composite = parent.addTableWrapLayoutComposite(1, true,
				parent.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true));

		final TableWrapData tableWrapData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB);
		tableWrapData.heightHint = CONTENT_HEIGHT_HINT;

		final IManagedForm managedForm = EpControlFactory.getInstance().createManagedForm(composite.getSwtComposite());
		final Composite formBody = managedForm.getForm().getBody();
		formBody.setLayout(new TableWrapLayout());
		managedForm.getForm().setLayoutData(tableWrapData);

		final FormToolkit toolkit = managedForm.getToolkit();
		final DataBindingContext dataBindingContext = getDataBindingContext();

		exchangeSummarySectionPart = new ExchangeSummarySectionPart(formBody, toolkit, dataBindingContext);
		managedForm.addPart(exchangeSummarySectionPart);

		reserveOptionsSectionPart = new ReserveOptionsSectionPart(formBody, toolkit, dataBindingContext);
		managedForm.addPart(reserveOptionsSectionPart);

		setControl(parent.getSwtComposite());

		final OrderReturn orderReturn = getModel().getOrderReturn();
		orderReturn.recalculateOrderReturn();
	}

	/**
	 * Depending on several factors, components on this page have to be enabled and disabled.
	 */
	private void toggleComponents() {
		if (physicalReturnCheckbox.getSelection()) {
			refundOptionsComposite.setEnabled(false);
			paymentSourceOptionsComposite.setAuthorizationNoteMessage(
					FulfillmentMessages.get().RefundOptionsComposite_CautionAuthorizeRefund_Label);
		} else {
			refundOptionsComposite.setEnabled(true);
			paymentSourceOptionsComposite.setAuthorizationNoteMessage(
					FulfillmentMessages.get().RefundOptionsComposite_CautionAuthorizeRefundAndReserve_Label);
		}
	}

	/**
	 * Exchange Summary Section.
	 */
	class ExchangeSummarySectionPart extends AbstractCmClientFormSectionPart {

		private static final String EMPTY_TEXT = ""; //$NON-NLS-1$

		private IEpLayoutComposite controlPane;
		private Label totalRefundAmountLabel;

		/**
		 * Constructor.
		 *
		 * @param parent             the parent form
		 * @param toolkit            the form toolkit
		 * @param dataBindingContext the form databinding context
		 */
		ExchangeSummarySectionPart(final Composite parent, final FormToolkit toolkit, final DataBindingContext dataBindingContext) {
			super(parent, toolkit, dataBindingContext, ExpandableComposite.TITLE_BAR);
		}

		@Override
		protected void createControls(final Composite parent, final FormToolkit toolkit) {
			controlPane = CompositeFactory.createTableWrapLayoutComposite(parent, 1, false);

			final Label sectionName = toolkit.createLabel(controlPane.getSwtComposite(),
					FulfillmentMessages.get().ExchangeWizard_OriginalOrder_Section);
			sectionName.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));

			totalRefundAmountLabel = toolkit.createLabel(controlPane.getSwtComposite(), EMPTY_TEXT);

			physicalReturnCheckbox = controlPane.addCheckBoxButton(
					FulfillmentMessages.get().ExchangeWizard_PhysicalReturnRequiredBeforeRefund_Label, EpState.EDITABLE,
					null);

			toolkit.createLabel(controlPane.getSwtComposite(), FulfillmentMessages.get().ExchangeWizard_RefundOptions_Section);

			final IEpLayoutData tableLayoutData = controlPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true, 1, 3);
			refundOptionsComposite = RefundOptionsComposite.createRefundOptionsComposite(controlPane, tableLayoutData);

			getSection().setExpanded(true);
		}

		@Override
		protected void populateControls() {
			physicalReturnCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent event) {
					toggleComponents();
				}
			});
			physicalReturnCheckbox.setSelection(true);
		}

		@Override
		protected String getSectionTitle() {
			return FulfillmentMessages.get().ExchangeWizard_ExchangeSummary_Section;
		}

		@Override
		protected void bindControls(final DataBindingContext bindingContext) {
			// nothing to bound here.
		}

		/**
		 * Should be called in this.beforeShow() method.
		 */
		public void repopulate() {
			final String refundAmountLabel = NLS.bind(FulfillmentMessages.get().ExchangeWizard_TotalPriceOfItemsToBeExchanged_Label,
					getModel().getOrderReturn().getCurrency().getCurrencyCode(),
					FulfillmentMessages.get().formatMoneyAsString(exchangeTotal, getModel().getOrder().getLocale()));
			totalRefundAmountLabel.setText(refundAmountLabel);

			controlPane.getSwtComposite().pack(true);
		}
	}

	/**
	 * Additional Authorization Section.
	 */
	class ReserveOptionsSectionPart extends AbstractCmClientFormSectionPart {

		private static final String EMPTY_TEXT = ""; //$NON-NLS-1$

		private IEpLayoutComposite controlPane;

		private Label totalAmountNewOrderLabel;

		/**
		 * Constructor.
		 *
		 * @param parent             the parent form
		 * @param toolkit            the form toolkit
		 * @param dataBindingContext the form databinding context
		 */
		ReserveOptionsSectionPart(final Composite parent, final FormToolkit toolkit, final DataBindingContext dataBindingContext) {
			super(parent, toolkit, dataBindingContext, ExpandableComposite.TITLE_BAR);
		}

		@Override
		protected void createControls(final Composite parent, final FormToolkit toolkit) {
			controlPane = CompositeFactory.createTableWrapLayoutComposite(parent, 1, false);
			final IEpLayoutData tableLayoutData = controlPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, true, 1, 1);

			totalAmountNewOrderLabel = toolkit.createLabel(controlPane.getSwtComposite(), EMPTY_TEXT);
			toolkit.createLabel(controlPane.getSwtComposite(), FulfillmentMessages.get().ExchangeWizard_ReservePaymentForExchangedOrder_Label);

			paymentSourceOptionsComposite = new PaymentSourceOptionsComposite(ExchangePaymentPage.this,
					getModel().getOrderReturn().getOrder(), controlPane, tableLayoutData,
					PaymentSourceOptionsComposite.PurposeEnum.AUTHORIZATION);
		}

		/**
		 * Enables composite and controls if the argument is <code>true</code>, and disables it otherwise.
		 *
		 * @param enabled if control should be enabled.
		 */
		public void setEnabled(final boolean enabled) {
			getSection().setVisible(enabled);
		}

		@Override
		protected void populateControls() {
			// nothing
		}

		@Override
		protected String getSectionTitle() {
			return FulfillmentMessages.get().ExchangeWizard_ReserveOptions_Section;
		}

		@Override
		protected void bindControls(final DataBindingContext bindingContext) {
			// nothing to bound here.
		}

		/**
		 * Should be called in this.beforeShow() method.
		 */
		public void repopulate() {
			final String refundAmountLabel = NLS.bind(FulfillmentMessages.get().ExchangeWizard_TotalPriceOfItemsToBeOrdered_Label,
					getModel().getOrderReturn().getCurrency().getCurrencyCode(),
					FulfillmentMessages.get().formatMoneyAsString(exchangeOrderTotal, getModel().getOrder().getLocale()));
			totalAmountNewOrderLabel.setText(refundAmountLabel);

			controlPane.getSwtComposite().pack(true);
		}
	}

	@Override
	public boolean beforeFromPrev(final PageChangingEvent event) {
		final OrderReturn orderReturn = getModel().getOrderReturn();
		exchangeTotal = orderReturn.getReturnTotalMoney();

		final boolean isCompleteExchange = isCompleteExchange();

		reserveOptionsSectionPart.setEnabled(!isCompleteExchange);
		if (isCompleteExchange) {
			exchangeOrderTotal = orderReturn.getExchangeOrder().getTotalMoney();
		} else {
			exchangeOrderTotal = orderReturn.getExchangePricingSnapshot().getTotalMoney();
		}

		if (isCompleteExchange) {
			refundOptionsComposite.setEnabled(true);
			physicalReturnCheckbox.setVisible(false);
			physicalReturnCheckbox.setEnabled(false);
			physicalReturnCheckbox.setSelection(false);
		}

		toggleComponents();

		exchangeSummarySectionPart.repopulate();
		reserveOptionsSectionPart.repopulate();
		setPageComplete(true);
		this.setErrorMessage(null);

		return super.beforeFromPrev(event);
	}

	@Override
	public boolean beforePrev(final PageChangingEvent event) {
		setPageComplete(false);
		return super.beforePrev(event);
	}

	@Override
	public ExchangeWizard getWizard() {
		return (ExchangeWizard) super.getWizard();
	}

	/**
	 * Returns what option was selected.
	 *
	 * @return selected option
	 */
	public ReturnExchangeRefundTypeEnum getSelectionResult() {
		if (isPhysicalReturn()) {
			return ReturnExchangeRefundTypeEnum.PHYSICAL_RETURN_REQUIRED;
		} else {
			if (refundOptionsComposite.useOriginalPayment()) {
				return ReturnExchangeRefundTypeEnum.REFUND_TO_ORIGINAL;
			} else {
				return ReturnExchangeRefundTypeEnum.MANUAL_REFUND;
			}
		}
	}

	private boolean isPhysicalReturn() {
		return physicalReturnCheckbox.getSelection();
	}

	/**
	 * @return selected original payment instrument.
	 */
	public List<PaymentInstrumentDTO> getSelectedOriginalPaymentInstruments() {
		return paymentSourceOptionsComposite.getSelectedOriginalPaymentInstruments();
	}

	/**
	 * @return return true if alternate payment instrument was selected.
	 */
	public boolean isAlternateSelection() {
		return paymentSourceOptionsComposite.isAlternateSelection();
	}

	/**
	 * @return selected alternate payment instrument.
	 */
	public List<PaymentInstrumentDTO> getSelectedAlternatePaymentInstruments() {
		return paymentSourceOptionsComposite.getSelectedAlternatePaymentInstruments();
	}

	/**
	 * @return true if original or alternate payment instrument selection was made on create exchange.
	 */
	public boolean isPaymentSourceOptionsValid() {
		return paymentSourceOptionsComposite.isValid();
	}

	/**
	 * @return true if original payment instrument or manual refund was made on complete exchange.
	 */
	public boolean isRefundMethodValid() {
		return refundOptionsComposite.isValid();
	}

	private boolean isCompleteExchange() {
		return getWizard().getExchangeWizardType() == ExchangeWizard.ExchangeWizardType.COMPLETE_EXCHANGE;
	}
}
