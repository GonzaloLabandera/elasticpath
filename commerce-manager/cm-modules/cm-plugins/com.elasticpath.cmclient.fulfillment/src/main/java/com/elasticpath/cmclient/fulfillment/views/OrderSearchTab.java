/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.cmclient.fulfillment.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.search.SafeSearchCodes;
import com.elasticpath.cmclient.core.search.impl.SafeSearchCodesImpl;
import com.elasticpath.cmclient.core.ui.dialog.SkuFinderDialog;
import com.elasticpath.cmclient.core.ui.framework.ControlModificationListener;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.EpSortingCompositeControl;
import com.elasticpath.cmclient.core.ui.framework.IEpDateTimePicker;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTabFolder;
import com.elasticpath.cmclient.core.validation.CompoundValidator;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.FulfillmentPlugin;
import com.elasticpath.cmclient.fulfillment.helpers.OrderSearchRequestJob;
import com.elasticpath.cmclient.fulfillment.views.order.OrderSearchResultsView;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.search.query.CustomerSearchCriteria;
import com.elasticpath.service.search.query.OrderSearchCriteria;
import com.elasticpath.service.search.query.StandardSortBy;

/**
 * The order search tab in the search view.
 */
@SuppressWarnings("PMD.TooManyFields")
public class OrderSearchTab implements ISearchTab {

	// private static final Logger LOG = Logger.getLogger(OrderSearchTab.class);

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private Text orderNumberText;

	private Text userSharedIdText;

	private Text userEmailText;

	private Text userFirstNameText;

	private Text userLastNameText;

	private Text userPhoneNumberText;

	private Text accountSharedIdText;

	private Text accountBusinessNameText;

	private Text accountBusinessNumberText;

	private Text accountPhoneNumberText;

	private Text containsSkuText;

	private ImageHyperlink containsSkuLink;

	private Text rmaText;

	private Text zipPostalCodeText;

	private IEpDateTimePicker fromDatePicker;

	private IEpDateTimePicker toDatePicker;

	private final OrderSearchCriteria searchCriteria = BeanLocator.getPrototypeBean(ContextIdNames.ORDER_SEARCH_CRITERIA, OrderSearchCriteria.class);

	private final OrderSearchRequestJob searchJob = new OrderSearchRequestJob();

	private CCombo orderStatusCombo;

	private CCombo storeCombo;

	private final EpState epState = EpState.EDITABLE;

	private IEpLayoutComposite orderDetailsGroup;

	private IEpLayoutComposite userDetailsGroup;

	private IEpLayoutComposite accountDetailsGroup;

	private IEpLayoutComposite searchTermsAdvancedGroup;

	private List<Store> stores;

	private IEpLayoutComposite searchTermsFiltersGroup;

	private CCombo shipmentStatusCombo;

	private DataBindingContext dataBindingContext;

	private EpSortingCompositeControl sortingControl;

	private List<OrderStatus> orderStatusValues;

	/**
	 * Construct the customer search tab.
	 *
	 * @param tabFolder the tabFolder
	 * @param tabIndex the tab index
	 */
	public OrderSearchTab(final IEpTabFolder tabFolder, final int tabIndex) {
		final IEpLayoutComposite compositeTab = tabFolder.addTabItem(FulfillmentMessages.get().SearchView_OrdersTab,
				CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_ORDER), tabIndex, 1, false);

		// Create order details search group
		this.createOrderDetailsGroup(compositeTab);

		//create user details search group
		this.createUserDetailsGroup(compositeTab);

		//create account details search group
		this.createAccountDetailsGroup(compositeTab);

		// Create optional filters group
		this.createFiltersGroup(compositeTab);

		// Create advanced filters group
		this.createSearchTermsAdvanced(compositeTab);

		this.createSortingGroup(compositeTab);

		fillStoresCombo();

		initializeSearchCriteria();

	}

	private void initializeSearchCriteria() {
		searchCriteria
				.setCustomerSearchCriteria(BeanLocator.getPrototypeBean(ContextIdNames.CUSTOMER_SEARCH_CRITERIA, CustomerSearchCriteria.class));
	}

	/**
	 * Creates the filters group.
	 *
	 * @param parentComposite the parent EP composite
	 */
	private void createFiltersGroup(final IEpLayoutComposite parentComposite) {
		final IEpLayoutData data = parentComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false);

		searchTermsFiltersGroup = parentComposite.addGroup(FulfillmentMessages.get().SearchView_FiltersGroup, 1, false, data);

		searchTermsFiltersGroup.addLabelBold(FulfillmentMessages.get().SearchView_Filter_OrderStatus, null);

		orderStatusCombo = searchTermsFiltersGroup.addComboBox(epState, data);
		orderStatusCombo.setEnabled(true);
		final String[] orderStatusNames = getOrderStatusNames();

		orderStatusCombo.setItems(orderStatusNames);
		orderStatusCombo.select(0);
		orderStatusCombo.setVisibleItemCount(orderStatusNames.length);

		searchTermsFiltersGroup.addLabelBold(FulfillmentMessages.get().SearchView_Filter_ShipmentStatus, null);

		shipmentStatusCombo = searchTermsFiltersGroup.addComboBox(epState, data);
		shipmentStatusCombo.setEnabled(true);
		final OrderShipmentStatus[] shipmentStatusValues = OrderShipmentStatus.getOrderShipmentStatusArray();
		final String[] shipmentStatusNames = new String[shipmentStatusValues.length + 1];
		shipmentStatusNames[0] = FulfillmentMessages.get().SearchView_Status_Any;
		for (int i = 0; i < shipmentStatusValues.length; i++) {
			shipmentStatusNames[i + 1] = FulfillmentMessages.get().getLocalizedName(shipmentStatusValues[i]);
		}

		shipmentStatusCombo.setItems(shipmentStatusNames);
		shipmentStatusCombo.select(0);
		shipmentStatusCombo.setVisibleItemCount(shipmentStatusNames.length);

		searchTermsFiltersGroup.addLabelBold(FulfillmentMessages.get().SearchView_Filter_Stores, null);

		storeCombo = searchTermsFiltersGroup.addComboBox(epState, data);
		storeCombo.setEnabled(true);
	}

	/**
	 * Gets the order status names.
	 *
	 * @return the order status names
	 */
	private String[] getOrderStatusNames() {
		orderStatusValues = new ArrayList<>(OrderStatus.values());

		final String[] orderStatusNames = new String[orderStatusValues.size() + 1];
		orderStatusNames[0] = FulfillmentMessages.get().SearchView_Status_Any;

		int count = 1;
		for (OrderStatus orderStatus : orderStatusValues) {
			orderStatusNames[count++] = FulfillmentMessages.get().getLocalizedName(orderStatus);
		}

		return orderStatusNames;
	}

	/**
	 * Creates the customer search terms group.
	 */
	private void createOrderDetailsGroup(final IEpLayoutComposite parentComposite) {
		final IEpLayoutData data = parentComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false, 2, 1);
		orderDetailsGroup = parentComposite.addGroup(FulfillmentMessages.get().SearchView_OrderDetails, 2, false, data);

		orderDetailsGroup.addLabelBold(FulfillmentMessages.get().SearchView_OrderNumber, data);
		this.orderNumberText = orderDetailsGroup.addTextField(epState, data);

		orderDetailsGroup.addLabelBold(FulfillmentMessages.get().SearchView_FromDate, data);
		this.fromDatePicker = orderDetailsGroup.addDateTimeComponent(IEpDateTimePicker.STYLE_DATE_AND_TIME, epState, data);

		orderDetailsGroup.addLabelBold(FulfillmentMessages.get().SearchView_ToDate, data);
		this.toDatePicker = orderDetailsGroup.addDateTimeComponent(IEpDateTimePicker.STYLE_DATE_AND_TIME, epState, data);

	}

	/**
	 * Creates the user details search group.
	 */
	private void createUserDetailsGroup(final IEpLayoutComposite parentComposite) {
		final IEpLayoutData data = parentComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false, 2, 1);
		userDetailsGroup = parentComposite.addGroup(FulfillmentMessages.get().SearchView_UserDetails, 2, false, data);

		userDetailsGroup.addLabelBold(FulfillmentMessages.get().SearchView_SharedId, data);
		this.userSharedIdText = userDetailsGroup.addTextField(epState, data);

		userDetailsGroup.addLabelBold(FulfillmentMessages.get().CustomerDetails_FirstNameLabel, data);
		this.userFirstNameText = userDetailsGroup.addTextField(epState, data);

		userDetailsGroup.addLabelBold(FulfillmentMessages.get().CustomerDetails_LastNameLabel, data);
		this.userLastNameText = userDetailsGroup.addTextField(epState, data);

		userDetailsGroup.addLabelBold(FulfillmentMessages.get().SearchView_Email, data);
		this.userEmailText = userDetailsGroup.addTextField(epState, data);

		userDetailsGroup.addLabelBold(FulfillmentMessages.get().SearchView_PhoneNumber, data);
		this.userPhoneNumberText = userDetailsGroup.addTextField(epState, data);
	}

	/**
	 * Creates account details search group.
	 */
	private void createAccountDetailsGroup(final IEpLayoutComposite parentComposite) {
		final IEpLayoutData data = parentComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false, 2, 1);
		accountDetailsGroup = parentComposite.addGroup(FulfillmentMessages.get().SearchView_AccountDetails, 2, false, data);

		accountDetailsGroup.addLabelBold(FulfillmentMessages.get().SearchView_SharedId, data);
		this.accountSharedIdText = accountDetailsGroup.addTextField(epState, data);

		accountDetailsGroup.addLabelBold(FulfillmentMessages.get().AccountDetails_BusinessName, data);
		this.accountBusinessNameText = accountDetailsGroup.addTextField(epState, data);

		accountDetailsGroup.addLabelBold(FulfillmentMessages.get().AccountDetails_BusinessNumber, data);
		this.accountBusinessNumberText = accountDetailsGroup.addTextField(epState, data);

		accountDetailsGroup.addLabelBold(FulfillmentMessages.get().AccountDetails_Phone, data);
		this.accountPhoneNumberText = accountDetailsGroup.addTextField(epState, data);
	}

	private void createSearchTermsAdvanced(final IEpLayoutComposite parentComposite) {
		final IEpLayoutData fieldData = parentComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false, 1, 1);
		final IEpLayoutData data = parentComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false, 2, 1);

		searchTermsAdvancedGroup = parentComposite.addGroup(FulfillmentMessages.get().SearchView_AdvancedGroup, 2, false, data);
		searchTermsAdvancedGroup.addLabelBold(FulfillmentMessages.get().SearchView_ContainsSku, data);
		this.containsSkuText = searchTermsAdvancedGroup.addTextField(epState, fieldData);

		this.containsSkuLink = searchTermsAdvancedGroup.addHyperLinkImage(CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_SEARCH), epState,
				null);
		containsSkuLink.setToolTipText(FulfillmentMessages.get().SearchView_ContainsSku_Tooltip);
		containsSkuLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent event) {
				SkuFinderDialog dialog = new SkuFinderDialog(null, null, false);
				if (dialog.open() == Window.OK) {
					Object selectedObject = dialog.getSelectedObject();
					if (selectedObject instanceof ProductSku) {
						containsSkuText.setText(((ProductSku) selectedObject).getSkuCode());
					} else if (selectedObject instanceof Product) {
						containsSkuText.setText(((Product) selectedObject).getDefaultSku().getSkuCode());
					}
				}
			}
		});

		searchTermsAdvancedGroup.addLabelBold(FulfillmentMessages.get().SearchView_RMA, data);
		this.rmaText = searchTermsAdvancedGroup.addTextField(epState, fieldData);

		searchTermsAdvancedGroup.addLabelBold(FulfillmentMessages.get().SearchView_PostalCode, data);
		this.zipPostalCodeText = searchTermsAdvancedGroup.addTextField(epState, data);
	}

	private void createSortingGroup(final IEpLayoutComposite parentComposite) {
		this.sortingControl = new EpSortingCompositeControl(parentComposite, searchCriteria);
		this.sortingControl.addSortTypeItem(FulfillmentMessages.get().OrderSearchResultsView_CustomerName, StandardSortBy.CUSTOMER_NAME);
		this.sortingControl.addSortTypeItem(FulfillmentMessages.get().OrderSearchResultsView_Date, StandardSortBy.DATE);
		this.sortingControl.addSortTypeItem(FulfillmentMessages.get().OrderSearchResultsView_OrderNumber, StandardSortBy.ORDER_NUMBER, true);
		this.sortingControl.addSortTypeItem(FulfillmentMessages.get().OrderSearchResultsView_Status, StandardSortBy.STATUS);
		this.sortingControl.addSortTypeItem(FulfillmentMessages.get().OrderSearchResultsView_Store, StandardSortBy.STORE_CODE);
		this.sortingControl.addSortTypeItem(FulfillmentMessages.get().OrderSearchResultsView_Total, StandardSortBy.TOTAL);
	}

	private void clearFields() {
		this.orderNumberText.setText(EMPTY_STRING);
		this.fromDatePicker.setDate(null);
		this.toDatePicker.setDate(null);

		this.userSharedIdText.setText(EMPTY_STRING);
		this.userEmailText.setText(EMPTY_STRING);
		this.userFirstNameText.setText(EMPTY_STRING);
		this.userLastNameText.setText(EMPTY_STRING);
		this.userPhoneNumberText.setText(EMPTY_STRING);

		this.accountSharedIdText.setText(EMPTY_STRING);
		this.accountBusinessNameText.setText(EMPTY_STRING);
		this.accountBusinessNumberText.setText(EMPTY_STRING);
		this.accountPhoneNumberText.setText(EMPTY_STRING);

		this.containsSkuText.setText(EMPTY_STRING);
		this.rmaText.setText(EMPTY_STRING);
		this.zipPostalCodeText.setText(EMPTY_STRING);

		this.orderStatusCombo.select(0);
		this.shipmentStatusCombo.select(0);
		this.storeCombo.select(0);
		this.setSearchStoreCodeList(0);
		this.sortingControl.clear();
	}

	/**
	 * Sets the focus to an internal UI control.
	 */
	@Override
	public void setFocus() {
		if (this.orderNumberText != null) {
			this.orderNumberText.setFocus();
		}
	}

	/**
	 * Gets the model object.
	 * 
	 * @return model object
	 */
	protected OrderSearchCriteria getModel() {
		return searchCriteria;
	}

	private void doQuery() {
		if (hasSearchTermEntered()) {
			searchJob.setSearchCriteria(getModel());
			searchJob.executeSearch(null);
		}
	}

	/**
 	*
 	*/
	@Override
	public void clear() {
		this.clearFields();
		this.dataBindingContext.updateModels();
	}

	/**
 	*
 	*/
	@Override
	public void search() {
		this.dataBindingContext.updateModels();

		sortingControl.updateSearchCriteriaValues();

		doQuery();
	}

	/**
 	*
 	*/
	@Override
	public void tabActivated() {
		setFocus();
	}

	@Override
	public void bindControls(final EpControlBindingProvider bindingProvider, final DataBindingContext context) {
		this.dataBindingContext = context;

		final ObservableUpdateValueStrategy storeUpdateStrategy = new ObservableUpdateValueStrategy() {
			@SuppressWarnings("synthetic-access")
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				// The first item in this box will be a "select all" option if there is more than
				// one store.
				setSearchStoreCodeList((Integer) newValue);
				return Status.OK_STATUS;
			}
		};
		bindingProvider.bind(context, storeCombo, null, null, storeUpdateStrategy, false);
		this.setSearchStoreCodeList(0);

		final ObservableUpdateValueStrategy orderStatusUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				int newIndexValue = (Integer) newValue;
				if (newIndexValue == 0) {
					getModel().setOrderStatus(null);
				} else {
					getModel().setOrderStatus(orderStatusValues.get(newIndexValue - 1));
				}
				return Status.OK_STATUS;
			}
		};
		bindingProvider.bind(context, orderStatusCombo, null, null, orderStatusUpdateStrategy, false);

		final ObservableUpdateValueStrategy shipmentStatusUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				int newIndexValue = (Integer) newValue;
				if (newIndexValue == 0) {
					getModel().setShipmentStatus(null);
				} else {
					getModel().setShipmentStatus(OrderShipmentStatus.getOrderShipmentStatusArray()[newIndexValue - 1]);
				}
				return Status.OK_STATUS;
			}
		};
		bindingProvider.bind(context, shipmentStatusCombo, null, null, shipmentStatusUpdateStrategy, false);

		bindingProvider.bind(context, this.orderNumberText, this.getModel(), "orderNumber", //$NON-NLS-1$
				EpValidatorFactory.LONG_IGNORE_SPACE, null, false);
		bindingProvider.bind(context, this.containsSkuText, this.getModel(), "skuCode", EpValidatorFactory.MAX_LENGTH_255, null, false); //$NON-NLS-1$
		bindingProvider.bind(context, this.rmaText, this.getModel(), "rmaCode", EpValidatorFactory.MAX_LENGTH_255, null, false); //$NON-NLS-1$
		bindingProvider.bind(context, this.zipPostalCodeText, this.getModel(),
				"shipmentZipcode", null, null, true); //$NON-NLS-1$

		IValidator fromDateValidator = new CompoundValidator(new IValidator[] { EpValidatorFactory.DATE_TIME, value -> {
			IStatus startDateStatus = Status.OK_STATUS;
			Date endDate = toDatePicker.getDate();
			Date startDate = fromDatePicker.getDate();
			if (null != endDate && null != startDate && endDate.before(startDate)) {
				startDateStatus = new Status(IStatus.ERROR, FulfillmentPlugin.PLUGIN_ID, IStatus.ERROR,
						FulfillmentMessages.get().Validation_FromDateAfterToDate, null);
			}
			return startDateStatus;
		}});
		fromDatePicker.bind(context, fromDateValidator, getModel(), "orderFromDate"); //$NON-NLS-1$

		IValidator toDateValidator = new CompoundValidator(new IValidator[] { EpValidatorFactory.DATE_TIME, value -> {
			IStatus toDateStatus = Status.OK_STATUS;
			Date endDate = toDatePicker.getDate();
			Date startDate = fromDatePicker.getDate();
			if (null != endDate && null != startDate && endDate.before(startDate)) {
				toDateStatus = new Status(IStatus.ERROR, FulfillmentPlugin.PLUGIN_ID, IStatus.ERROR,
						FulfillmentMessages.get().Validation_ToDateBeforeFromDate, null);
			}
			return toDateStatus;
		}});
		toDatePicker.bind(context, toDateValidator, getModel(), "orderToDate"); //$NON-NLS-1$

		bindingProvider.bind(context, this.userSharedIdText, this.getModel().getCustomerSearchCriteria(), "sharedId", null, null, true); //$NON-NLS-1$
		bindingProvider.bind(context, this.userEmailText, this.getModel().getCustomerSearchCriteria(),
				"email", SearchFieldsValidators.EMAIL_PATTERN_VALIDATOR, null, true); //$NON-NLS-1$
		bindingProvider.bind(context, this.userFirstNameText, this.getModel().getCustomerSearchCriteria(),
				"firstName", null, null, true); //$NON-NLS-1$
		bindingProvider.bind(context, this.userLastNameText, this.getModel().getCustomerSearchCriteria(),
				"lastName", null, null, true); //$NON-NLS-1$
		bindingProvider.bind(context, this.userPhoneNumberText, this.getModel().getCustomerSearchCriteria(),
				"phoneNumber", null, null, true); //$NON-NLS-1$
	}

	@Override
	public String getResultViewId() {
		return OrderSearchResultsView.VIEW_ID;
	}

	@Override
	public boolean hasSearchTermEntered() {
		return true; // no checks as nothing is required for the search
	}

	@Override
	public void setControlModificationListener(final ControlModificationListener listener) {
		orderDetailsGroup.setControlModificationListener(listener);
		userDetailsGroup.setControlModificationListener(listener);
		accountDetailsGroup.setControlModificationListener(listener);
		searchTermsAdvancedGroup.setControlModificationListener(listener);
	}

	/**
	 * @param listener selection listener
	 */
	@Override
	public void setSelectionListener(final SelectionListener listener) {
		this.orderNumberText.addSelectionListener(listener);
		this.containsSkuText.addSelectionListener(listener);
		this.rmaText.addSelectionListener(listener);
		this.zipPostalCodeText.addSelectionListener(listener);

		this.userSharedIdText.addSelectionListener(listener);
		this.userEmailText.addSelectionListener(listener);
		this.userFirstNameText.addSelectionListener(listener);
		this.userLastNameText.addSelectionListener(listener);
		this.userPhoneNumberText.addSelectionListener(listener);

		this.accountSharedIdText.addSelectionListener(listener);
		this.accountBusinessNameText.addSelectionListener(listener);
		this.accountBusinessNumberText.addSelectionListener(listener);
		this.accountPhoneNumberText.addSelectionListener(listener);

	}

	private void fillStoresCombo() {
		stores = StoreFilterHelper.getAvailableStores();
		storeCombo.setItems(StoreFilterHelper.getAvailableStoreNames(stores));
		storeCombo.select(0);
		setSearchStoreCodeList(0);
	}

	/**
	 * Store combo box re-initialization.
	 */
	public void reInitializationStores() {
		String text = storeCombo.getText();
		this.storeCombo.removeAll();
		fillStoresCombo();
		int index = storeCombo.indexOf(text);
		if (index >= 0) {
			storeCombo.select(index);
			setSearchStoreCodeList(index);
		}
	}

	private void setSearchStoreCodeList(final int select) {
		// The first item in this box will be a "select all" option if there is more than one
		// store.
		SafeSearchCodes storeCodes = new SafeSearchCodesImpl();
		if (storeCombo.getSelectionIndex() == 0) {
			storeCodes.extractAndAdd(stores, "code"); //$NON-NLS-1$
		} else {
			storeCodes.extractAndAdd(stores.get(select), "code"); //$NON-NLS-1$
		}
		getModel().setStoreCodes(storeCodes.asSet());
	}

	@Override
	public boolean isWidgetDisposed() {
		return orderDetailsGroup.getSwtComposite().isDisposed();
	}
}