/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards.refund;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.CorePlugin;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.EpWizardPageSupport;
import com.elasticpath.cmclient.core.conversion.EpStringToBigDecimalConverter;
import com.elasticpath.cmclient.core.ui.framework.AbstractCmClientFormSectionPart;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.validation.CompoundValidator;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.cmclient.core.wizard.AbstractEPWizardPage;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.wizards.PaymentSourceOptionsComposite;
import com.elasticpath.domain.order.Order;
import com.elasticpath.money.Money;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;

/**
 * Page for payment source information.
 */
public class RefundCardInfoPage extends AbstractEPWizardPage<RefundItem> {

	private static final int TEXT_AREA_HEIGHT = 50;

	private final Money refundableAmount;

	private Text amountText;
	private Text textArea;
	private BigDecimal amount;
	private String refundNote;
	private PaymentSourceOptionsComposite paymentSourceOptionsComposite;

	/**
	 * The constructor.
	 *
	 * @param pageName the page name.
	 * @param refundableAmount the amount available to refund.
	 */
	protected RefundCardInfoPage(final String pageName, final Money refundableAmount) {
		super(1, true, pageName, new DataBindingContext());
		setMessage(FulfillmentMessages.get().RefundWizard_CardInfoPage_Message);
		this.refundableAmount = refundableAmount;
	}

	@Override
	protected void bindControls() {
		final EpControlBindingProvider binder = EpControlBindingProvider.getInstance();
		binder.bind(getDataBindingContext(),
				amountText,
				this,
				"amount", //$NON-NLS-1$
				new CompoundValidator(EpValidatorFactory.NON_NEGATIVE_NON_ZERO_BIG_DECIMAL, new NonBlankValidator(), new RefundingAmountValidator()),
				new EpStringToBigDecimalConverter(), true);
		binder.bind(getDataBindingContext(),
				textArea,
				this,
				"refundNote",  //$NON-NLS-1$
				EpValidatorFactory.STRING_255_REQUIRED,
				null, true);

		EpWizardPageSupport.create(this, getDataBindingContext());
	}

	@Override
	protected void createEpPageContent(final IEpLayoutComposite pageComposite) {
		setControl(pageComposite.getSwtComposite());

		final String refundableAmountMessage = NLS.bind(FulfillmentMessages.get().OrderEditor_CreateRefund_AvailableRefundAmount,
				convertToMoneyFormat(refundableAmount), refundableAmount.getCurrency());
		pageComposite.addLabel(refundableAmountMessage, pageComposite.createLayoutData());

		final int columnNumber = 3;
		final IEpLayoutComposite refundAmountComposite = pageComposite.addGridLayoutComposite(columnNumber, false, pageComposite
				.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.CENTER, true, true));

		IEpLayoutData labelData = refundAmountComposite
				.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.CENTER);
		refundAmountComposite.addLabelBoldRequired(FulfillmentMessages.get().RefundWizard_RefundAmount, EpState.EDITABLE, labelData);
		refundAmountComposite.addLabel(getModel().getOrder().getCurrency().getCurrencyCode(), labelData);
		amountText = refundAmountComposite.addTextField(EpState.EDITABLE, refundAmountComposite.createLayoutData(IEpLayoutData.FILL,
				IEpLayoutData.CENTER, true, true));
		amountText.addModifyListener((ModifyListener) event -> flushErrorMessage());

		refundAmountComposite.addLabelBoldRequired(FulfillmentMessages.get().RefundWizard_RefundNote, EpState.EDITABLE,
				refundAmountComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.CENTER, false, true, 2, 1));
		textArea = refundAmountComposite.addTextArea(true, false, EpState.EDITABLE,
				refundAmountComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.CENTER, true, true));
		amountText.addModifyListener((ModifyListener) event -> flushErrorMessage());


		((GridData) this.textArea.getLayoutData()).heightHint = TEXT_AREA_HEIGHT;

		final IManagedForm managedForm = EpControlFactory.getInstance().createManagedForm(pageComposite.getSwtComposite());
		final TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 1;
		final int margin = 10;
		layout.leftMargin = margin;
		layout.rightMargin = margin;

		final ScrolledForm scrolledForm = managedForm.getForm();
		scrolledForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrolledForm.getBody().setLayout(layout);

		managedForm.addPart(new RefundOptionsSection(scrolledForm.getBody(), managedForm.getToolkit(), getDataBindingContext()));
	}

	/**
	 * Flushes previously set error message.
	 */
	protected void flushErrorMessage() {
		setErrorMessage(null);
	}

	@Override
	protected void populateControls() {
		amountText.setText(BigDecimal.ZERO.toString());
	}

	@Override
	public RefundWizard getWizard() {
		return (RefundWizard) super.getWizard();
	}

	/**
	 * Getter for refund amount. It should be public for binding.
	 *
	 * @return amount to refund.
	 */
	public BigDecimal getAmount() {
		return amount;
	}

	/**
	 * Setter for refund amount. It should be public for binding.
	 *
	 * @param amount value to set.
	 */
	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	/**
	 * Getter for the refund note.
	 *
	 * @return refund note.
	 */
	public String getRefundNote() {
		return refundNote;
	}

	/**
	 * Setter for the refund note.
	 *
	 * @param refundNote refund note.
	 */
	@SuppressWarnings("unused")
	public void setRefundNote(final String refundNote) {
		this.refundNote = refundNote;
	}

	@Override
	public boolean beforeNext(final PageChangingEvent event) {
		List<PaymentInstrumentDTO> selectedPaymentInstruments = paymentSourceOptionsComposite.getSelectedPaymentInstruments();
		return getWizard().process(selectedPaymentInstruments, getAmount(), getRefundNote());
	}

	private String convertToMoneyFormat(final Money amount) {
		final Order order = getModel().getOrder();
		final Locale locale = order.getLocale();

		return getMoneyFormatter().formatCurrency(amount, locale);
	}

	/**
	 * Validating the string is not blank.
	 */
	static class NonBlankValidator implements IValidator {
		@Override
		public IStatus validate(final Object toValidate) {
			if (StringUtils.isBlank((String) toValidate)) {
				return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, IStatus.ERROR, CoreMessages.get().EpValidatorFactory_BigDecimal, null);
			}
			return Status.OK_STATUS;
		}
	}

	/**
	 * Validating the refunding amount is not more than refundable.
	 */
	class RefundingAmountValidator implements IValidator {
		@Override
		public IStatus validate(final Object toValidate) {

			try	{
				final double refundingAmount = Double.parseDouble((String) toValidate);
				if (refundingAmount > refundableAmount.getAmount().doubleValue()) {
					return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, IStatus.ERROR, CoreMessages.get().EpValidatorFactory_RefundingAmount,
							null);
				}
			} catch (NumberFormatException e) {
				return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, IStatus.ERROR, CoreMessages.get().EpValidatorFactory_BigDecimal, null);
			}

			return Status.OK_STATUS;
		}
	}

	/**
	 * This section contains paymentControl.
	 */
	class RefundOptionsSection extends AbstractCmClientFormSectionPart {

		/**
		 * The constructor.
		 *
		 * @param parent             parent composite.
		 * @param toolkit            will be passed to super.
		 * @param dataBindingContext data binding context.
		 */
		RefundOptionsSection(final Composite parent, final FormToolkit toolkit, final DataBindingContext dataBindingContext) {
			super(parent, toolkit, dataBindingContext, ExpandableComposite.TITLE_BAR);
		}

		@Override
		protected void bindControls(final DataBindingContext bindingContext) {
			// nothing
		}

		@Override
		protected void createControls(final Composite client, final FormToolkit toolkit) {
			IEpLayoutComposite sectionComposite = CompositeFactory.createTableWrapLayoutComposite(client, 1, false);
			paymentSourceOptionsComposite = new PaymentSourceOptionsComposite(RefundCardInfoPage.this, getModel().getOrder(), sectionComposite,
					sectionComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING),
					PaymentSourceOptionsComposite.PurposeEnum.REFUND);
		}

		@Override
		protected void populateControls() {
			// nothing
		}

		@Override
		protected String getSectionTitle() {
			return FulfillmentMessages.get().RefundOptionsSection_Title;
		}

	}

}
