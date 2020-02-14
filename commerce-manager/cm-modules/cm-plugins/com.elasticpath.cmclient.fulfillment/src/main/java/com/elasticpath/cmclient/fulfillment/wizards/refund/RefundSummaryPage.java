/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards.refund;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.elasticpath.cmclient.core.ui.framework.AbstractCmClientFormSectionPart;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.wizard.AbstractEPWizardPage;
import com.elasticpath.cmclient.fulfillment.wizards.PaymentSummaryControl;

/**
 * Page for refund confirmation.
 */
public class RefundSummaryPage extends AbstractEPWizardPage<RefundItem> {

	private PaymentSummaryControl refundSummary;

	/**
	 * The constructor.
	 *
	 * @param pageName the page name
	 */
	protected RefundSummaryPage(final String pageName) {
		super(1, false, pageName, new DataBindingContext());
		setPageComplete(false);
	}

	@Override
	protected void createEpPageContent(final IEpLayoutComposite pageComposite) {
		final IManagedForm managedForm = EpControlFactory.getInstance().createManagedForm(pageComposite.getSwtComposite());
		managedForm.getForm().getBody().setLayout(new TableWrapLayout());
		final ConfirmationSectionPart confirmationSectionPart = new ConfirmationSectionPart(
				managedForm.getForm().getBody(), managedForm.getToolkit(), getDataBindingContext());
		managedForm.addPart(confirmationSectionPart);

		setControl(pageComposite.getSwtComposite());
	}

	@Override
	public boolean beforeFromPrev(final PageChangingEvent event) {
		final RefundItem refundItem = getWizard().getModel();
		final String amount = getMoneyFormatter().formatCurrency(refundItem.getRefundedAmount(), refundItem.getOrder().getLocale());
		if (refundItem.isManual()) {
			refundSummary.setManualRefundValues(amount);
		} else {
			refundSummary.setRefundValues(refundItem.getPaymentTransactions());
		}
		getControl().pack();
		getShell().pack();
		setPageComplete(true);
		return true;
	}

	@Override
	protected void bindControls() {
		// do nothing
	}

	@Override
	protected void populateControls() {
		// do nothing
	}

	@Override
	public RefundWizard getWizard() {
		return (RefundWizard) super.getWizard();
	}

	/**
	 * Confirmation section.
	 */
	class ConfirmationSectionPart extends AbstractCmClientFormSectionPart {

		/**
		 * Constructor.
		 *
		 * @param parent             the parent form
		 * @param toolkit            the form toolkit
		 * @param dataBindingContext the form data binding context
		 */
		ConfirmationSectionPart(final Composite parent, final FormToolkit toolkit, final DataBindingContext dataBindingContext) {
			super(parent, toolkit, dataBindingContext, ExpandableComposite.TITLE_BAR);
		}

		@Override
		protected void createControls(final Composite parent, final FormToolkit toolkit) {
			final IEpLayoutComposite controlPane = CompositeFactory.createGridLayoutComposite(parent, 1, false);
			final IEpLayoutData fieldLayout = controlPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL);
			refundSummary = new PaymentSummaryControl(controlPane.getSwtComposite(), fieldLayout.getSwtLayoutData(), getDataBindingContext());
		}

		@Override
		protected void populateControls() {
			// nothing to populate here.
		}

		@Override
		protected void bindControls(final DataBindingContext bindingContext) {
			// nothing to bind here.
		}

	}
}
