/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.editors.order;

import java.util.Locale;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.editors.AbstractCmClientEditorPageSectionPart;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentAmounts;
import com.elasticpath.money.MoneyFormatter;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;

/**
 * Represents the UI of an order payment summary section.
 */
public class OrderPaymentsSummarySectionPart extends AbstractCmClientEditorPageSectionPart {

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		// Nothing to do.
	}

	@Override
	protected void populateControls() {
		// Nothing to do.
	}

	private Text orderedText;

	private Text paidText;

	private Text dueText;

	private final EpState editMode = EpState.READ_ONLY;

	/**
	 * Constructor.
	 *
	 * @param formPage the formpage
	 * @param editor the CmClientFormEditor that contains the form
	 */
	public OrderPaymentsSummarySectionPart(final FormPage formPage, final AbstractCmClientFormEditor editor) {
		super(formPage, editor, ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
	}

	@Override
	protected void createControls(final Composite client, final FormToolkit toolkit) {
		final IEpLayoutComposite controlPane = CompositeFactory.createTableWrapLayoutComposite(client, 2, false);
		final TableWrapData data = new TableWrapData(TableWrapData.FILL, TableWrapData.FILL);
		controlPane.setLayoutData(data);

		final IEpLayoutData labelData = controlPane.createLayoutData(IEpLayoutData.END, IEpLayoutData.CENTER, false, false);
		final IEpLayoutData fieldData = controlPane.createLayoutData(IEpLayoutData.END, IEpLayoutData.BEGINNING, true, false);

		controlPane.addLabelBold(FulfillmentMessages.get().OrderPaymentSummaySection_Ordered, labelData);
		orderedText = controlPane.addTextField(editMode, fieldData);

		controlPane.addLabelBold(FulfillmentMessages.get().OrderPaymentSummaySection_Paid, labelData);
		paidText = controlPane.addTextField(editMode, fieldData);

		controlPane.addLabelBold(FulfillmentMessages.get().OrderPaymentSummaySection_Due, labelData);
		dueText = controlPane.addTextField(editMode, fieldData);
	}

	@Override
	protected String getSectionDescription() {
		return FulfillmentMessages.get().PaymentSummarySection_Description;
	}

	@Override
	protected String getSectionTitle() {
		return FulfillmentMessages.get().PaymentSummarySection_Title;
	}

	/**
	 * Refresh the section with order data.
	 *
	 * @param order the order
	 */
	public void refreshData(final Order order) {
		OrderPaymentApiService orderPaymentApiService =
				BeanLocator.getSingletonBean(ContextIdNames.ORDER_PAYMENT_API_SERVICE, OrderPaymentApiService.class);
		OrderPaymentAmounts orderPaymentAmounts =  orderPaymentApiService.getOrderPaymentAmounts(order);
		final Locale locale = order.getLocale();

		orderedText.setText(getMoneyFormatter().formatCurrency(order.getTotalMoney(), locale));
		paidText.setText(getMoneyFormatter().formatCurrency(orderPaymentAmounts.getAmountPaid(), locale));
		dueText.setText(getMoneyFormatter().formatCurrency(orderPaymentAmounts.getAmountDue(), locale));
	}

	protected MoneyFormatter getMoneyFormatter() {
		return BeanLocator.getSingletonBean(ContextIdNames.MONEY_FORMATTER, MoneyFormatter.class);
	}

}
