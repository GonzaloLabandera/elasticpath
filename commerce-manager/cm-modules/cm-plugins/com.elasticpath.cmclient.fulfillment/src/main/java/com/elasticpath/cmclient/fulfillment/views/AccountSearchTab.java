/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.views;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.ui.framework.ControlModificationListener;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.EpSortingCompositeControl;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.ui.framework.IEpTabFolder;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.helpers.AccountSearchRequestJob;
import com.elasticpath.cmclient.fulfillment.views.customer.AccountSearchResultsView;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.service.search.query.AccountSearchCriteria;
import com.elasticpath.service.search.query.StandardSortBy;

/**
 * The account search tab in the search view.
 */
public class AccountSearchTab implements ISearchTab {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private Button rootAccountsOnlyFlag;

	private Text sharedIdText;

	private Text businessNameText;

	private Text businessNumberText;

	private Text phoneNumberText;

	private Text faxText;

	private Text zipCodeText;

	private final EpState epState = EpState.EDITABLE;

	private IEpLayoutComposite searchTermsGroup;

	private final AccountSearchCriteria searchCriteria = BeanLocator.getPrototypeBean(ContextIdNames.ACCOUNT_SEARCH_CRITERIA,
			AccountSearchCriteria.class);

	private final AccountSearchRequestJob searchJob = new AccountSearchRequestJob();

	private DataBindingContext dataBindingContext;

	private EpSortingCompositeControl sortingControl;

	/**
	 * Construct the account search tab.
	 *
	 * @param tabFolder the tabFolder
	 * @param tabIndex  the tab index
	 */
	public AccountSearchTab(final IEpTabFolder tabFolder, final int tabIndex) {
		final IEpLayoutComposite accountsTab = tabFolder.addTabItem(FulfillmentMessages.get().SearchView_AccountsTab,
				CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_USER), tabIndex, 1, false);

		// Create search group
		createSearchTermsGroup(accountsTab);

		// Create optional filters group
		createFiltersGroup(accountsTab);

		// Create sorting group
		createSortingGroup(accountsTab);
	}

	@Override
	public void search() {
		sortingControl.updateSearchCriteriaValues();
		if (hasSearchTermEntered()) {
			searchJob.setSearchCriteria(getModel());
			searchJob.executeSearch(null);
		}
	}

	@Override
	public void clear() {
		clearFields();
		this.dataBindingContext.updateModels();
	}

	@Override
	public boolean hasSearchTermEntered() {
		return true; // no checks as nothing is required for the search
	}

	@Override
	public void tabActivated() {
		setFocus();
	}

	/**
	 * Sets the focus to an internal UI control.
	 */
	@Override
	public void setFocus() {
		if (this.sharedIdText != null) {
			this.sharedIdText.setFocus();
		}
	}

	@Override
	public void bindControls(final EpControlBindingProvider bindingProvider, final DataBindingContext context) {
		this.dataBindingContext = context;

		bindingProvider.bind(context, this.rootAccountsOnlyFlag, this.getModel(), "searchRootAccountsOnly", null, null, true); //$NON-NLS-1$
		bindingProvider.bind(context, this.sharedIdText, this.getModel(), "sharedId", null, null, true); //$NON-NLS-1$
		bindingProvider.bind(context, this.businessNameText, this.getModel(), "businessName", null, null, true); //$NON-NLS-1$
		bindingProvider.bind(context, this.businessNumberText, this.getModel(), "businessNumber", null, null, true); //$NON-NLS-1$
		bindingProvider.bind(context, this.phoneNumberText, this.getModel(), "phoneNumber", null, null, true); //$NON-NLS-1$
		bindingProvider.bind(context, this.faxText, this.getModel(), "faxNumber", null, null, true); //$NON-NLS-1$
		bindingProvider.bind(context, this.zipCodeText, this.getModel(), "zipOrPostalCode", null, null, true); //$NON-NLS-1$
	}

	@Override
	public void setControlModificationListener(final ControlModificationListener listener) {
		searchTermsGroup.setControlModificationListener(listener);
	}

	@Override
	public String getResultViewId() {
		return AccountSearchResultsView.VIEW_ID;
	}

	@Override
	public boolean isWidgetDisposed() {
		return searchTermsGroup.getSwtComposite().isDisposed();
	}

	@Override
	public void setSelectionListener(final SelectionListener listener) {
		sharedIdText.addSelectionListener(listener);
		businessNameText.addSelectionListener(listener);
		businessNumberText.addSelectionListener(listener);
		phoneNumberText.addSelectionListener(listener);
		faxText.addSelectionListener(listener);
		zipCodeText.addSelectionListener(listener);
	}

	/**
	 * Gets the model object.
	 *
	 * @return model object
	 */
	protected final AccountSearchCriteria getModel() {
		return searchCriteria;
	}

	/**
	 * Creates the account search terms group.
	 */
	private void createSearchTermsGroup(final IEpLayoutComposite parentComposite) {
		final IEpLayoutData data = parentComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false);

		searchTermsGroup = parentComposite.addGroup(FulfillmentMessages.get().AccountsSearchTab_SearchTermsGroup, 1, false, data);

		searchTermsGroup.addLabelBold(FulfillmentMessages.get().AccountDetails_SharedId, null);
		sharedIdText = searchTermsGroup.addTextField(epState, data);

		searchTermsGroup.addLabelBold(FulfillmentMessages.get().AccountDetails_BusinessName, null);
		businessNameText = searchTermsGroup.addTextField(epState, data);

		searchTermsGroup.addLabelBold(FulfillmentMessages.get().AccountDetails_BusinessNumber, null);
		businessNumberText = searchTermsGroup.addTextField(epState, data);

		searchTermsGroup.addLabelBold(FulfillmentMessages.get().AccountDetails_Phone, null);
		phoneNumberText = searchTermsGroup.addTextField(epState, data);

		searchTermsGroup.addLabelBold(FulfillmentMessages.get().AccountDetails_Fax, null);
		faxText = searchTermsGroup.addTextField(epState, data);

		searchTermsGroup.addLabelBold(FulfillmentMessages.get().AccountDetails_ZipPostalCode, null);
		zipCodeText = searchTermsGroup.addTextField(epState, data);
	}

	private void createFiltersGroup(final IEpLayoutComposite parentComposite) {
		final IEpLayoutComposite groupComposite = parentComposite.addGroup(FulfillmentMessages.get().SearchView_FiltersGroup, 2, false, null);
		final IEpLayoutData data = groupComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false);

		rootAccountsOnlyFlag = groupComposite.addCheckBoxButton(FulfillmentMessages.get().AccountDetails_RootAccountsOnly, epState, data);
	}

	private void createSortingGroup(final IEpLayoutComposite parentComposite) {
		this.sortingControl = new EpSortingCompositeControl(parentComposite, searchCriteria);
		this.sortingControl.addSortTypeItem(FulfillmentMessages.get().AccountSearchResultsView_SharedId, StandardSortBy.SHARED_ID, true);
		this.sortingControl.addSortTypeItem(FulfillmentMessages.get().AccountSearchResultsView_BusinessName, StandardSortBy.BUSINESS_NAME);
		this.sortingControl.addSortTypeItem(FulfillmentMessages.get().AccountSearchResultsView_BusinessNumber, StandardSortBy.BUSINESS_NUMBER);
		this.sortingControl.addSortTypeItem(FulfillmentMessages.get().AccountSearchResultsView_DefaultBillingAddress, StandardSortBy.ADDRESS);

	}

	private void clearFields() {
		sharedIdText.setText(EMPTY_STRING);
		businessNameText.setText(EMPTY_STRING);
		businessNumberText.setText(EMPTY_STRING);
		phoneNumberText.setText(EMPTY_STRING);
		faxText.setText(EMPTY_STRING);

		rootAccountsOnlyFlag.setSelection(false);

		sortingControl.clear();
	}

}
