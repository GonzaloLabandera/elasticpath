/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards.reauth;

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.cmclient.core.ui.framework.AbstractCmClientFormSectionPart;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.core.wizard.AbstractEPWizardPage;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatistic;

/**
 * Reauthorization summary page. Displays list of successful or failed reauthorizations.
 */
public class ReAuthSummaryPage extends AbstractEPWizardPage<ReAuthorizationItem> {

	private static final String INTERNAL_MESSAGE = "InternalMessage";
	private AuthResultSection resultSection;
	private Label adviceLabel;

	/**
	 * The constructor.
	 *
	 * @param pageName           wizard page name. Required by super.
	 * @param dataBindingContext not used.
	 */
	protected ReAuthSummaryPage(final String pageName, final DataBindingContext dataBindingContext) {
		super(1, false, pageName, dataBindingContext);
	}

	@Override
	protected void bindControls() {
		// nothing
	}

	@Override
	protected void createEpPageContent(final IEpLayoutComposite pageComposite) {
		setControl(pageComposite.getSwtComposite());

		final IManagedForm managedForm = EpControlFactory.getInstance().createManagedForm(pageComposite.getSwtComposite());
		final TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 1;
		final int margin = 10;
		layout.leftMargin = margin;
		layout.rightMargin = margin;

		final ScrolledForm scrolledForm = managedForm.getForm();
		scrolledForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrolledForm.getBody().setLayout(layout);

		resultSection = new AuthResultSection(scrolledForm.getBody(), managedForm.getToolkit(), getDataBindingContext());
		managedForm.addPart(resultSection);

		adviceLabel = pageComposite.addLabel(FulfillmentMessages.get().ReAuthWizard_AdviceOnError_Note,
				pageComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL));
	}

	@Override
	protected void populateControls() {
		// nothing
	}

	@Override
	public boolean beforeFromPrev(final PageChangingEvent event) {
		final Collection<PaymentStatistic> paymentTransactions = getWizard().getPaymentTransactions();
		resultSection.setData(paymentTransactions);
		resultSection.getSection().setText(FulfillmentMessages.get().ReAuthWizard_NewAuthorizations_TableTitle);
		if (getWizard().isOperationSuccessful() && checkFailedTransactions(paymentTransactions)) {
			setMessage(FulfillmentMessages.get().ReAuthWizard_DecreaseAmountError_Message);
			adviceLabel.setVisible(false);
		} else if (getWizard().isOperationSuccessful()) {
			setMessage(FulfillmentMessages.get().ReAuthWizard_Successful_SectionTitle);
			adviceLabel.setVisible(false);
		} else {
			setMessage(getExceptionMessage());
			adviceLabel.setVisible(true);
		}

		this.getControl().pack();
		this.getShell().pack();
		return super.beforeFromPrev(event);
	}

	private boolean checkFailedTransactions(final Collection<PaymentStatistic> paymentTransactions) {
		return paymentTransactions.stream().anyMatch(transaction -> !transaction.isSuccessful());
	}

	private String getExceptionMessage() {
		final Optional<StructuredErrorMessage> message = getModel().getPaymentsException().getStructuredErrorMessages().stream().findFirst();
		return message.isPresent()
				? message.get().getData().get(INTERNAL_MESSAGE)
				: StringUtils.EMPTY;
	}

	@Override
	public ReAuthWizard getWizard() {
		return (ReAuthWizard) super.getWizard();
	}

	/**
	 * Display reauthorization result - list of succeeded or failed reauthorizations.
	 */
	class AuthResultSection extends AbstractCmClientFormSectionPart {

		private static final String AUTH_SUMMARY_PAGE_TABLE = "Auth Summary Page Table"; //$NON-NLS-1$

		private static final int COLUMN_WIDTH_PAYMENT_SOURCE = 250;
		private static final int COLUMN_INDEX_PAYMENT_SOURCE = 0;

		private static final int COLUMN_WIDTH_AMOUNT = 180;
		private static final int COLUMN_INDEX_AMOUNT = 1;

		private static final int COLUMN_WIDTH_STATUS = 100;
		private static final int COLUMN_INDEX_STATUS = 2;

		private IEpTableViewer table;

		/**
		 * The constructor.
		 *
		 * @param parent             parent composite.
		 * @param toolkit            will be passed to super.
		 * @param dataBindingContext not used. will be passed to super.
		 */
		AuthResultSection(final Composite parent, final FormToolkit toolkit, final DataBindingContext dataBindingContext) {
			super(parent, toolkit, dataBindingContext, ExpandableComposite.TITLE_BAR);
		}

		/**
		 * Refresh results section content with new list of reauthorizations.
		 *
		 * @param paymentTransactions list of reauthorization data for each instrument.
		 */
		public void setData(final Collection<PaymentStatistic> paymentTransactions) {
			table.setInput(paymentTransactions);
			getSection().setVisible(!paymentTransactions.isEmpty());
		}

		@Override
		protected void bindControls(final DataBindingContext bindingContext) {
			// nothing

		}

		@Override
		protected void createControls(final Composite parent, final FormToolkit toolkit) {
			final IEpLayoutComposite controlPane = CompositeFactory.createTableWrapLayoutComposite(parent, 1, false);
			table = controlPane.addTableViewer(false, EpState.READ_ONLY,
					controlPane.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING),
					AUTH_SUMMARY_PAGE_TABLE);
			table.addTableColumn(FulfillmentMessages.get().ReAuthWizard_PaymentSource_ColumnTitle, COLUMN_WIDTH_PAYMENT_SOURCE);
			table.addTableColumn(FulfillmentMessages.get().ReAuthWizard_Amount_ColumnTitle, COLUMN_WIDTH_AMOUNT);
			table.addTableColumn(FulfillmentMessages.get().ReAuthWizard_Status_ColumnTitle, COLUMN_WIDTH_STATUS);
			table.setContentProvider(new ArrayContentProvider());
			table.setLabelProvider(new ResultsTableLabelProvider());
		}

		@Override
		protected void populateControls() {
			// nothing

		}

		/**
		 * Label provider for results table.
		 */
		class ResultsTableLabelProvider implements ITableLabelProvider {

			@Override
			public Image getColumnImage(final Object element, final int columnIndex) {
				return null;
			}

			@Override
			public String getColumnText(final Object element, final int columnIndex) {
				final PaymentStatistic paymentStatistic = (PaymentStatistic) element;

				switch (columnIndex) {
					case COLUMN_INDEX_PAYMENT_SOURCE:
						return paymentStatistic.getInstrument().getName();
					case COLUMN_INDEX_AMOUNT:
						return paymentStatistic.getFormattedAmount(getMoneyFormatter());
					case COLUMN_INDEX_STATUS:
						return paymentStatistic.isSuccessful()
								? FulfillmentMessages.get().PaymentStatus_Approved
								: FulfillmentMessages.get().PaymentStatus_Failed;
					default:
						return null;
				}
			}

			@Override
			public void addListener(final ILabelProviderListener listener) {
				// nothing
			}

			@Override
			public void dispose() {
				// nothing
			}

			@Override
			public boolean isLabelProperty(final Object element, final String property) {
				return false;
			}

			@Override
			public void removeListener(final ILabelProviderListener listener) {
				// nothing
			}

		}
	}

}
