/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.fulfillment.wizards.exchange;

import static com.elasticpath.cmclient.fulfillment.wizards.exchange.OrderPopulateStep.SHIPPING_METHOD_MODIFIED;

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.ui.framework.AbstractCmClientFormSectionPart;
import com.elasticpath.cmclient.core.ui.framework.CompositeFactory;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.util.AddressUtil;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.order.OrderAddress;
import com.elasticpath.domain.tax.TaxJurisdiction;
import com.elasticpath.service.customer.AddressService;
import com.elasticpath.service.shipping.ShippingOptionService;
import com.elasticpath.service.tax.TaxJurisdictionService;
import com.elasticpath.service.tax.adapter.TaxAddressAdapter;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;

/**
 * This section contains information about shipping.
 */
class ShippingInfoSectionPart extends AbstractCmClientFormSectionPart {

	private final TaxJurisdictionService taxJurisdictionService;

	/**
	 * Address to deliver order exchange.
	 */
	private OrderAddress shippingAddress;
	private ShippingOption shippingOption;

	private final ExchangeOrderItemsPage parentPage;

	/**
	 * Shipping address of the customer associated with current getOrder().
	 */
	private CCombo addressCombo;

	/**
	 * Shipping type for delivering order exchange to the customer.
	 */
	private CCombo shippingMethodCombo;

	private Label shippingMethodLabel;

	private final String defaultAddressPrompt = FulfillmentMessages.get().ExchangeWizard_SelectAddress_Combo;
	private final String defaultMethodPrompt = FulfillmentMessages.get().ExchangeWizard_SelectShippingMethod_Combo;
	private final AddressService addressService;

	/**
	 * The Constructor.
	 *
	 * @param parentPage {@link ExchangeOrderItemsPage}
	 * @param parent     the parent form
	 * @param toolkit    the form toolkit
	 */
	ShippingInfoSectionPart(final ExchangeOrderItemsPage parentPage, final Composite parent, final FormToolkit toolkit) {
		super(parent, toolkit, parentPage.getDataBindingContext(), ExpandableComposite.TITLE_BAR);
		this.parentPage = parentPage;
		taxJurisdictionService = BeanLocator.getSingletonBean(ContextIdNames.TAX_JURISDICTION_SERVICE, TaxJurisdictionService.class);
		this.addressService = BeanLocator.getSingletonBean(ContextIdNames.ADDRESS_SERVICE, AddressService.class);
	}

	@Override
	protected void createControls(final Composite parent, final FormToolkit toolkit) {
		final IEpLayoutComposite controlPane = CompositeFactory.createGridLayoutComposite(parent, 2, true);
		final IEpLayoutData fieldData = controlPane.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.FILL, true, false);

		controlPane.addLabelBold(FulfillmentMessages.get().ExchangeWizard_ShippingAddress_Label, fieldData);
		shippingMethodLabel = controlPane.addLabelBold(FulfillmentMessages.get().ExchangeWizard_ShippingMethod_Label, fieldData);
		shippingMethodLabel.setEnabled(false);

		addressCombo = controlPane.addComboBox(EpControlFactory.EpState.EDITABLE, fieldData);
		shippingMethodCombo = controlPane.addComboBox(EpControlFactory.EpState.READ_ONLY, fieldData);
		addListeners();
	}

	@Override
	protected void populateControls() {
		List<CustomerAddress> addresses = addressService.findByCustomer(parentPage.getOrder().getCustomer().getUidPk());

		addressCombo.add(defaultAddressPrompt);
		addressCombo.select(0);
		for (CustomerAddress customerAddress : addresses) {
			OrderAddress orderAddress = BeanLocator.getPrototypeBean(ContextIdNames.ORDER_ADDRESS, OrderAddress.class);
			orderAddress.init(customerAddress);
			final String fullAddress = getFullAddress(orderAddress);
			addressCombo.setData(fullAddress, orderAddress);
			addressCombo.add(fullAddress);
		}

		// see shipping method population in updateMethod
		shippingMethodCombo.add(defaultMethodPrompt);
		shippingMethodCombo.select(0);
	}

	private String getFullAddress(final Address customerAddress) {
		return AddressUtil.formatAddress(customerAddress, true);
	}

	@Override
	protected String getSectionTitle() {
		return FulfillmentMessages.get().ExchangeWizard_ShippingInformation_Section;
	}

	@Override
	protected void bindControls(final DataBindingContext bindingContext) {
		final boolean hideDecorationOnFirstValidation = true;
		final EpControlBindingProvider binder = EpControlBindingProvider.getInstance();
		binder.bind(bindingContext, addressCombo, EpValidatorFactory.REQUIRED, null, new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				return Status.OK_STATUS;
			}
		}, hideDecorationOnFirstValidation);
		binder.bind(bindingContext, shippingMethodCombo, EpValidatorFactory.REQUIRED, null, new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				return Status.OK_STATUS;
			}
		}, hideDecorationOnFirstValidation);

	}

	private void addListeners() {
		addressCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				shippingAddress = (OrderAddress) addressCombo.getData(addressCombo.getText());
				boolean isShippingAddressSelected = isShippingAddressSelected();

				updateShippingMethod();
				parentPage.getSummarySection().updateInclusiveTax(isInclusiveTax(shippingAddress));
				parentPage.setErrorMessage(null);

				shippingMethodCombo.setEnabled(isShippingAddressSelected);
				shippingMethodLabel.setEnabled(isShippingAddressSelected);
			}
		});

		shippingMethodCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				shippingOption = (ShippingOption) shippingMethodCombo.getData(shippingMethodCombo.getText());
				parentPage.populateShoppingCart(SHIPPING_METHOD_MODIFIED);
				if (isShippingAddressSelected()) {
					parentPage.setErrorMessage(null);
				}
			}
		});
	}

	/**
	 * Populate shippingMethodCombo with available Shipping Methods for selected address.
	 */
	private void updateShippingMethod() {
		shippingMethodCombo.removeAll();
		shippingMethodCombo.add(defaultMethodPrompt);
		shippingMethodCombo.select(0);

		final ShippingOptionService shippingOptionService =
				BeanLocator.getSingletonBean(ContextIdNames.SHIPPING_OPTION_SERVICE, ShippingOptionService.class);
		final List<ShippingOption> shippingOptions = shippingOptionService.getShippingOptions(shippingAddress,
				parentPage.getOrder().getStoreCode(), parentPage.getOrder().getLocale()).getAvailableShippingOptions();
		for (ShippingOption availableShippingOption : shippingOptions) {
			String methodName = availableShippingOption.getDisplayName(parentPage.getOrder().getLocale()).orElse(null);
			shippingMethodCombo.setData(methodName, availableShippingOption);
			shippingMethodCombo.add(methodName);
		}
	}

	private boolean isInclusiveTax(final Address address) {
		if (address != null) {
			TaxAddressAdapter addressAdapter = new TaxAddressAdapter();

			TaxJurisdiction taxJurisdiction = taxJurisdictionService.retrieveEnabledInStoreTaxJurisdiction(
					parentPage.getOrder().getStoreCode(), addressAdapter.toTaxAddress(address));
			if (null != taxJurisdiction) {
				return taxJurisdiction.getPriceCalculationMethod().equals(TaxJurisdiction.PRICE_CALCULATION_INCLUSIVE);
			}
		}
		return false;
	}

	/**
	 * Validate this section.
	 *
	 * @return true if section is valid
	 */
	boolean validate() {
		if (!isShippingAddressSelected()) {
			parentPage.setErrorMessage(FulfillmentMessages.get().ExchangeWizard_AddressShouldBeSelected_Message);
			return false;
		}

		if (!isShippingMethodSelected()) {
			parentPage.setErrorMessage(FulfillmentMessages.get().ExchangeWizard_MethodShouldBeSelected_Message);
			return false;
		}
		return true;
	}

	boolean isShippingMethodSelected() {
		return !defaultMethodPrompt.equals(shippingMethodCombo.getText());
	}

	private boolean isShippingAddressSelected() {
		return !defaultAddressPrompt.equals(addressCombo.getText());
	}

	OrderAddress getShippingAddress() {
		return shippingAddress;
	}

	ShippingOption getShippingOption() {
		return shippingOption;
	}
}
