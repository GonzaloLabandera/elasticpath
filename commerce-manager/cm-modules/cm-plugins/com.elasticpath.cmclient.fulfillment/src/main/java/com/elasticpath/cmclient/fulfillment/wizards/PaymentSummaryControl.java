/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards;

import java.util.Collection;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.ui.framework.AbstractCmClientFormSectionPart;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.money.MoneyFormatter;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatistic;

/**
 * Control to display refund summary info.
 */
public class PaymentSummaryControl {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private final IEpLayoutComposite mainComposite;
	private final RefundResultSection resultsSection;
	private final Label actionLabel;

	/**
	 * The constructor.
	 *
	 * @param parent             parent composite.
	 * @param layoutData         layout data for this control.
	 * @param dataBindingContext not used. will be passed to RefundResultSection.
	 */
	public PaymentSummaryControl(final Composite parent, final Object layoutData, final DataBindingContext dataBindingContext) {
		mainComposite = CompositeFactory.createTableWrapLayoutComposite(parent, 1, false);
		mainComposite.setLayoutData(layoutData);

		actionLabel = mainComposite.addLabel(EMPTY_STRING, mainComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));

		final IManagedForm managedForm = EpControlFactory.getInstance().createManagedForm(parent);
		final TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 1;
		final int margin = 10;
		layout.leftMargin = margin;
		layout.rightMargin = margin;

		final ScrolledForm scrolledForm = managedForm.getForm();
		scrolledForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrolledForm.getBody().setLayout(layout);

		resultsSection = new RefundResultSection(scrolledForm.getBody(), managedForm.getToolkit(), dataBindingContext);
		managedForm.addPart(resultsSection);
	}

	/**
	 * Show refund summary values.
	 *
	 * @param paymentTransactions credit card info
	 */
	public void setRefundValues(final Collection<PaymentStatistic> paymentTransactions) {
		actionLabel.setText(FulfillmentMessages.get().PaymentSummaryControl_Refund_Payment);
		setPaymentTransactions(paymentTransactions);
	}

	/**
	 * Show manual refund summary values.
	 *
	 * @param amount total amount refunded
	 */
	public void setManualRefundValues(final String amount) {
		actionLabel.setText(NLS.bind(FulfillmentMessages.get().PaymentSummaryControl_Manual_Refund_Payment, amount));
		resultsSection.setVisible(false);
	}

	/**
	 * Show additional authorization summary values.
	 *
	 * @param paymentTransactions credit card info
	 */
	public void setAdditionalAuthValues(final Collection<PaymentStatistic> paymentTransactions) {
		actionLabel.setText(FulfillmentMessages.get().PaymentSummaryControl_Additional_Payment);
		setPaymentTransactions(paymentTransactions);
	}

	private void setPaymentTransactions(final Collection<PaymentStatistic> paymentTransactions) {
		resultsSection.setData(paymentTransactions);
		resultsSection.setVisible(true);
		mainComposite.getSwtComposite().pack();
	}

	/**
	 * Marks the receiver as visible if the argument is <code>true</code>, and marks it invisible otherwise.
	 *
	 * @param visible the new visibility state.
	 */
	public void setVisible(final boolean visible) {
		resultsSection.setVisible(visible);

		mainComposite.getSwtComposite().setVisible(visible);
		mainComposite.getSwtComposite().pack();
	}

	/**
	 * Display refund result - list of succeeded or failed refunds.
	 */
	class RefundResultSection extends AbstractCmClientFormSectionPart {

		private static final String REFUND_SUMMARY_PAGE_TABLE = "Refund Summary Page Table"; //$NON-NLS-1$

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
		RefundResultSection(final Composite parent, final FormToolkit toolkit, final DataBindingContext dataBindingContext) {
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
			IEpLayoutComposite controlPane = CompositeFactory.createTableWrapLayoutComposite(parent, 1, false);

			final Label titleLabel = controlPane.addLabel(FulfillmentMessages.get().PaymentSummaryControl_New_Authorizations,
					controlPane.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
			final FontData fontData = titleLabel.getFont().getFontData()[0];
			titleLabel.setFont(new Font(Display.getCurrent(), new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD)));

			table = controlPane.addTableViewer(false, EpControlFactory.EpState.READ_ONLY,
					controlPane.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING),
					REFUND_SUMMARY_PAGE_TABLE);
			table.addTableColumn(FulfillmentMessages.get().RefundWizard_PaymentSource_ColumnTitle, COLUMN_WIDTH_PAYMENT_SOURCE);
			table.addTableColumn(FulfillmentMessages.get().RefundWizard_Authorization_ColumnTitle, COLUMN_WIDTH_AMOUNT);
			table.addTableColumn(FulfillmentMessages.get().RefundWizard_Status_ColumnTitle, COLUMN_WIDTH_STATUS);
			table.setContentProvider(new ArrayContentProvider());
			table.setLabelProvider(new ResultsTableLabelProvider());
		}

		@Override
		protected void populateControls() {
			// nothing
		}

		/**
		 * Sets the section visibility.
		 *
		 * @param visible true if visible
		 */
		protected void setVisible(final boolean visible) {
			getSection().setVisible(visible);
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
								: FulfillmentMessages.get().PaymentStatus_Not_Processed;
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

	protected MoneyFormatter getMoneyFormatter() {
		return BeanLocator.getSingletonBean(ContextIdNames.MONEY_FORMATTER, MoneyFormatter.class);
	}

}
