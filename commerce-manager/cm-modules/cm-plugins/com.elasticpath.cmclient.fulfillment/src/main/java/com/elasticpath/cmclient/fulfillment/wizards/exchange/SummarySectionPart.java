/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.fulfillment.wizards.exchange;

import static com.elasticpath.cmclient.fulfillment.wizards.exchange.OrderPopulateStep.SHIPMENT_DISCOUNT_MODIFIED;
import static com.elasticpath.cmclient.fulfillment.wizards.exchange.OrderPopulateStep.SHIPPING_COST_MODIFIED;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import org.apache.log4j.Logger;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.elasticpath.cmclient.core.CmClientResources;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.ui.framework.AbstractCmClientFormSectionPart;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.validation.CompoundValidator;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.FulfillmentPlugin;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.service.tax.TaxCalculationResult;

/**
 * This Section contains summary information about new getOrder().
 */
class SummarySectionPart extends AbstractCmClientFormSectionPart {

	/**
	 * The logger.
	 */
	private static final Logger LOG = Logger.getLogger(SummarySectionPart.class);

	private static final int HORIZONTAL_SPAN = 3;

	private final ExchangeOrderItemsPage parentPage;

	/**
	 * Price information.
	 */
	private Text shippingCostText;
	private Text shipmentDiscountText;
	private Text itemSubTotalLabel;
	private Text itemTaxesLabel;
	private Text shippingTaxesLabel;
	private Text orderTotalLabel;

	private Label totalBeforeTaxLabel;
	private Text totalBeforeTaxTextField;

	private boolean inclusiveTax;

	private BigDecimal shippingCost;
	private BigDecimal shipmentDiscount;

	/**
	 * Constructor.
	 *
	 * @param parentPage {@link ExchangeOrderItemsPage}
	 * @param parent     the parent form
	 * @param toolkit    the form toolkit
	 */
	SummarySectionPart(final ExchangeOrderItemsPage parentPage, final Composite parent, final FormToolkit toolkit) {
		super(parent, toolkit, parentPage.getDataBindingContext(), ExpandableComposite.TITLE_BAR);
		this.parentPage = parentPage;
		this.inclusiveTax = parentPage.getModel().getOrderReturn().isInclusiveTax();
	}

	/**
	 * Update inclusive tax modifier.
	 *
	 * @param isInclusiveTax true if tax jurisdiction is inclusive
	 */
	void updateInclusiveTax(final boolean isInclusiveTax) {
		this.inclusiveTax = isInclusiveTax;

		totalBeforeTaxLabel.setVisible(!inclusiveTax);
		totalBeforeTaxTextField.setVisible(!inclusiveTax);

		getSection().pack();
	}

	@Override
	protected void createControls(final Composite parent, final FormToolkit toolkit) {
		IEpLayoutComposite controlPane = CompositeFactory.createTableWrapLayoutComposite(parent, 1, false);
		controlPane.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));

		final IEpLayoutData summaryInfoPaneLayout = controlPane.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.CENTER, true, true);
		IEpLayoutComposite summaryInfoPane = controlPane.addTableWrapLayoutComposite(HORIZONTAL_SPAN, false, summaryInfoPaneLayout);

		final IEpLayoutData labelData = controlPane.createLayoutData(IEpLayoutData.END, IEpLayoutData.CENTER);
		final IEpLayoutData currencyData = summaryInfoPane.createLayoutData(IEpLayoutData.END, IEpLayoutData.CENTER);
		final IEpLayoutData priceData = summaryInfoPane.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.CENTER);

		summaryInfoPane.addLabelBold(FulfillmentMessages.get().ExchangeWizard_ItemSubTotal_Label, labelData);
		createCurrencyTextControl(summaryInfoPane, currencyData);
		itemSubTotalLabel = summaryInfoPane.addTextField(EpControlFactory.EpState.READ_ONLY, priceData);

		Label shippingCostLabel = summaryInfoPane.addLabelBold(FulfillmentMessages.get().ExchangeWizard_ShippingCost_Label, labelData);
		shippingCostLabel.setForeground(CmClientResources.getColor(CmClientResources.COLOR_RED));
		createCurrencyTextControl(summaryInfoPane, currencyData);
		shippingCostText = summaryInfoPane.addTextField(EpControlFactory.EpState.EDITABLE, priceData);

		Label shipmentDiscountLabel = summaryInfoPane.addLabelBold(FulfillmentMessages
				.get().ExchangeWizard_ShipmentDiscount_Label, labelData);
		shipmentDiscountLabel.setForeground(CmClientResources.getColor(CmClientResources.COLOR_RED));
		createCurrencyTextControl(summaryInfoPane, currencyData);
		shipmentDiscountText = summaryInfoPane.addTextField(EpControlFactory.EpState.EDITABLE, priceData);

		totalBeforeTaxLabel = summaryInfoPane.addLabelBold(FulfillmentMessages.get().ExchangeWizard_TotalBeforeTax_Label, labelData);
		createCurrencyTextControl(summaryInfoPane, currencyData);
		totalBeforeTaxTextField = summaryInfoPane.addTextField(EpControlFactory.EpState.READ_ONLY, priceData);

		summaryInfoPane.addLabelBold(FulfillmentMessages.get().ExchangeWizard_ItemTaxes_Label, labelData);
		createCurrencyTextControl(summaryInfoPane, currencyData);
		itemTaxesLabel = summaryInfoPane.addTextField(EpControlFactory.EpState.READ_ONLY, priceData);

		summaryInfoPane.addLabelBold(FulfillmentMessages.get().ExchangeWizard_ShippingTaxes_Label, labelData);
		createCurrencyTextControl(summaryInfoPane, currencyData);
		shippingTaxesLabel = summaryInfoPane.addTextField(EpControlFactory.EpState.READ_ONLY, priceData);

		summaryInfoPane.addLabelBold(FulfillmentMessages.get().ExchangeWizard_OrderTotal_Label, labelData);
		createCurrencyTextControl(summaryInfoPane, currencyData);
		orderTotalLabel = summaryInfoPane.addTextField(EpControlFactory.EpState.READ_ONLY, priceData);

		addFocusListenerForEditableFields();
		getSection().pack();
	}

	/**
	 * Add the focus listener for editable fields.
	 */
	protected void addFocusListenerForEditableFields() {
		shippingCostText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent event) {
				parentPage.populateShoppingCart(SHIPPING_COST_MODIFIED);
			}
		});
		shipmentDiscountText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent event) {
				String price = ((Text) event.getSource()).getText();
				try {
					shipmentDiscount = new BigDecimal(price);
				} catch (NumberFormatException exception) {
					shipmentDiscount = BigDecimal.ZERO;
					LOG.debug("wrong number", exception); //$NON-NLS-1$
				}
				parentPage.populateShoppingCart(SHIPMENT_DISCOUNT_MODIFIED);

			}
		});
	}

	@Override
	protected void populateControls() {
		Currency currency = parentPage.getOrder().getCurrency();

		shippingCostText.setText(formatPriceForCurrency(BigDecimal.ZERO, currency));
		shipmentDiscountText.setText(formatPriceForCurrency(BigDecimal.ZERO, currency));
		itemSubTotalLabel.setText(formatPriceForCurrency(BigDecimal.ZERO, currency));
		itemTaxesLabel.setText(formatPriceForCurrency(BigDecimal.ZERO, currency));
		shippingTaxesLabel.setText(formatPriceForCurrency(BigDecimal.ZERO, currency));
		orderTotalLabel.setText(formatPriceForCurrency(BigDecimal.ZERO, currency));
		totalBeforeTaxTextField.setText(BigDecimal.ZERO.toString());

		shippingCost = BigDecimal.ZERO;
		shipmentDiscount = BigDecimal.ZERO;
	}

	@Override
	protected String getSectionTitle() {
		return FulfillmentMessages.get().ExchangeWizard_ExchangeOrderSummary_Section;
	}

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		final boolean hideDecorationOnFirstValidation = true;
		final EpControlBindingProvider binder = EpControlBindingProvider.getInstance();

		IValidator validator = new CompoundValidator(EpValidatorFactory.NON_NEGATIVE_BIG_DECIMAL, EpValidatorFactory.REQUIRED);
		binder.bind(bindingContext, shippingCostText, validator, null, new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				shippingCost = new BigDecimal((String) newValue);
				return Status.OK_STATUS;
			}
		}, hideDecorationOnFirstValidation);

		IValidator discountValidator = new CompoundValidator(EpValidatorFactory.NON_NEGATIVE_BIG_DECIMAL, EpValidatorFactory.REQUIRED, value -> {
			BigDecimal discount = new BigDecimal((String) value);
			BigDecimal cost = new BigDecimal(itemSubTotalLabel.getText());
			if (discount.compareTo(cost) > 0) {
				return new Status(IStatus.ERROR, FulfillmentPlugin.PLUGIN_ID, IStatus.ERROR,
						FulfillmentMessages.get().ExchangeWizard_TooBigDiscount_Message, null);
			}
			return Status.OK_STATUS;
		});
		binder.bind(bindingContext, shipmentDiscountText, discountValidator, null, new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				return Status.OK_STATUS;
			}
		}, hideDecorationOnFirstValidation);
		bindingContext.updateModels();
	}

	/**
	 * Update information about prices/taxes/shipment.
	 */
	protected void updatePrices() {
		final ShoppingCart shoppingCart = parentPage.getShoppingCart();
		ShoppingCartPricingSnapshot pricingSnapshot = parentPage.getPricingSnapshotService().getPricingSnapshotForCart(shoppingCart);
		ShoppingCartTaxSnapshot taxSnapshot = parentPage.getTaxSnapshotService().getTaxSnapshotForCart(shoppingCart, pricingSnapshot);
		TaxCalculationResult taxCalculationResult = taxSnapshot.getTaxCalculationResult();
		itemSubTotalLabel.setText(pricingSnapshot.getSubtotal().toString());
		shippingCostText.setText(pricingSnapshot.getShippingCost().getAmount().toString());
		shipmentDiscountText.setText(pricingSnapshot.getSubtotalDiscount().toString());

		if (!inclusiveTax) {
			totalBeforeTaxTextField.setText(pricingSnapshot.getBeforeTaxTotal().getAmount().toString());
		}
		itemTaxesLabel.setText(taxCalculationResult.getTotalItemTax().getAmount().toString());
		shippingTaxesLabel.setText(taxCalculationResult.getShippingTax().getAmount().toString());
		orderTotalLabel.setText(taxSnapshot.getTotal().toString());
	}

	private void createCurrencyTextControl(final IEpLayoutComposite controlPane, final IEpLayoutData currencyData) {
		String currencyCode = parentPage.getOrder().getCurrency().getCurrencyCode();

		Text shippingCostCurrencyTxt = controlPane.addTextField(EpControlFactory.EpState.READ_ONLY, currencyData);
		shippingCostCurrencyTxt.setText(currencyCode);
	}

	private String formatPriceForCurrency(final BigDecimal price, final Currency currency) {
		return price.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP).toPlainString();
	}

	BigDecimal getShippingCost() {
		return shippingCost;
	}

	BigDecimal getShipmentDiscount() {
		return shipmentDiscount;
	}

}
