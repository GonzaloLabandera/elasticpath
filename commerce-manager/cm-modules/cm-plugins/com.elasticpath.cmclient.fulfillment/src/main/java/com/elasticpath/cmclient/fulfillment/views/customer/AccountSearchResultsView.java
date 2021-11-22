/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.views.customer;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.elasticpath.cmclient.core.CoreImageRegistry;
import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.event.ItemChangeEvent;
import com.elasticpath.cmclient.core.event.SearchResultEvent;
import com.elasticpath.cmclient.core.helpers.AbstractSearchRequestJob;
import com.elasticpath.cmclient.core.service.AuthorizationService;
import com.elasticpath.cmclient.core.ui.framework.IEpTableColumn;
import com.elasticpath.cmclient.core.ui.framework.IEpTableViewer;
import com.elasticpath.cmclient.core.views.AbstractSortListView;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.FulfillmentPermissions;
import com.elasticpath.cmclient.fulfillment.FulfillmentPlugin;
import com.elasticpath.cmclient.fulfillment.editors.actions.CreateAccountAction;
import com.elasticpath.cmclient.fulfillment.editors.actions.DeleteAccountAction;
import com.elasticpath.cmclient.fulfillment.event.AccountEventListener;
import com.elasticpath.cmclient.fulfillment.event.FulfillmentEventService;
import com.elasticpath.cmclient.fulfillment.helpers.AccountSearchRequestJob;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.service.search.query.SortBy;
import com.elasticpath.service.search.query.StandardSortBy;


/**
 * Account search results view.
 */
public class AccountSearchResultsView extends AbstractSortListView implements AccountEventListener {
	private static final String ACCOUNT_SEARCH_RESULT_TABLE = "Account Search Result Table";

	private static final Logger LOG = LogManager.getLogger(AccountSearchResultsView.class);


	// table column constants
	private static final int COLUMN_IMAGE = 0;

	private static final int COLUMN_SHAREID = 1;

	private static final int COLUMN_BUSINESS_NAME = 2;

	private static final int COLUMN_BUSINESS_NUMBER = 3;

	private static final int COLUMN_DEFAULT_BILLING_ADDRESS = 4;

	private AccountSearchRequestJob accountSearchRequestJob;

	private Object[] objects;

	private static final Object[] EMPTY_ARRAY = new Object[0];

	/**
	 * Account search results view ID.
	 */
	public static final String VIEW_ID = AccountSearchResultsView.class.getName();

	/**
	 *
	 */
	public AccountSearchResultsView() {
		super(true, ACCOUNT_SEARCH_RESULT_TABLE);
		AccountSearchResultsView.this.updateNavigationComponents();
	}

	@Override
	public void dispose() {
		super.dispose();
		FulfillmentEventService.getInstance().unregisterAccountEventListener(this);
	}

	@Override
	public void init(final IViewSite site) throws PartInitException {
		super.init(site);
		FulfillmentEventService.getInstance().registerAccountEventListener(this);
	}

	@Override
	protected Object getModel() {
		return null;
	}

	/**
	 * Label provider class for the results table.
	 */
	private final class AccountSearchResultsViewLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(final Object element, final int columnIndex) {
			final Customer customer = (Customer) element;
			Image result = null;
			if (columnIndex == COLUMN_IMAGE) {
				result = getCustomerImage(customer);
			}
			return result;
		}

		private Image getCustomerImage(final Customer customer) {
			Image result = null;
			switch (customer.getStatus()) {
				case Customer.STATUS_ACTIVE:
					result = CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_USER_SMALL);
					break;
				case Customer.STATUS_DISABLED:
				case Customer.STATUS_SUSPENDED:
					result = CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_USER_DISABLED_SMALL);
					break;
				case Customer.STATUS_PENDING_APPROVAL:
					result = CoreImageRegistry.getImage(CoreImageRegistry.IMAGE_USER_PENDING_APPROVAL_SMALL);
					break;
				default:
					// no image
			}
			return result;
		}

		@Override
		public String getColumnText(final Object element, final int columnIndex) {
			String result = ""; //$NON-NLS-1$
			final Customer customer = (Customer) element;

			switch (columnIndex) {
				case COLUMN_SHAREID:
					result = customer.getSharedId();
					break;
				case COLUMN_BUSINESS_NAME:
					result = customer.getBusinessName();
					break;
				case COLUMN_BUSINESS_NUMBER:
					result =  customer.getAccountBusinessNumber();
					break;
				case COLUMN_DEFAULT_BILLING_ADDRESS:
					result = Optional.ofNullable(customer.getPreferredBillingAddress())
							.map(Address::toPlainString).orElse(StringUtils.EMPTY);
					break;
				default: // nothing as result by default is ""
			}
			return result;
		}
	}

	@Override
	public void searchResultsUpdate(final SearchResultEvent<Customer> event) {
		accountSearchRequestJob = (AccountSearchRequestJob) event.getSource();
		getViewer().getTable().getDisplay().syncExec(() -> {
			AccountSearchResultsView.this.objects = event.getItems().toArray();
			AccountSearchResultsView.this.setResultsCount(event.getTotalNumberFound());
			AccountSearchResultsView.this.getViewer().getTable().clearAll();

			if (event.getItems().isEmpty() && event.getStartIndex() <= 0) {
				AccountSearchResultsView.this.showMessage(CoreMessages.get().NoSearchResultsError);
			} else {
				AccountSearchResultsView.this.hideErrorMessage();
			}

			AccountSearchResultsView.this.setResultsStartIndex(event.getStartIndex());
			AccountSearchResultsView.this.refreshViewerInput();
			AccountSearchResultsView.this.updateSortingOrder(accountSearchRequestJob.getSearchCriteria());
			AccountSearchResultsView.this.updateNavigationComponents();
		});
	}

	@Override
	public void accountChanged(final ItemChangeEvent<Customer> event) {
		final Customer changedCustomer = event.getItem();
		for (final TableItem currTableItem : getViewer().getTable().getItems()) {
			final Customer currCustomer = (Customer) currTableItem.getData();
			if (currCustomer.getUidPk() == changedCustomer.getUidPk()) {
				currTableItem.setData(changedCustomer);
				getViewer().update(changedCustomer, null);
				break;
			}
		}
	}

	@Override
	protected Object[] getViewInput() {
		if (objects == null) {
			return new Object[0];
		}

		return objects.clone();
	}

	@Override
	protected ITableLabelProvider getViewLabelProvider() {
		return new AccountSearchResultsViewLabelProvider();
	}

	@Override
	protected void initializeTable(final IEpTableViewer epTableViewer) {
		final int[] widths = new int[]{21, 200, 135, 135, 270};
		final String[] columnNames = new String[]{StringUtils.EMPTY,
				FulfillmentMessages.get().AccountSearchResultsView_SharedId,
				FulfillmentMessages.get().AccountSearchResultsView_BusinessName,
				FulfillmentMessages.get().AccountSearchResultsView_BusinessNumber,
				FulfillmentMessages.get().AccountSearchResultsView_DefaultBillingAddress,
		};
		final SortBy[] sortTypes = new SortBy[]{
				null,
				StandardSortBy.SHARED_ID,
				StandardSortBy.BUSINESS_NAME,
				StandardSortBy.BUSINESS_NUMBER,
				StandardSortBy.ADDRESS
		};
		for (int i = 0; i < columnNames.length; i++) {
			IEpTableColumn addTableColumn = epTableViewer.addTableColumn(columnNames[i], widths[i]);
			registerTableColumn(addTableColumn, sortTypes[i]);
		}

		getSite().setSelectionProvider(epTableViewer.getSwtTableViewer());
	}

	@Override
	protected String getPluginId() {
		return FulfillmentPlugin.PLUGIN_ID;
	}

	@Override
	protected void initializeViewToolbar() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Initialize the customer search results view."); //$NON-NLS-1$
		}
		final OpenAccountDetailsEditorAction action = new OpenAccountDetailsEditorAction(getViewer(), this.getSite());
		addDoubleClickAction(action);

		final Separator customerActionSeparator = new Separator("customerActionSeparator"); //$NON-NLS-1$
		getToolbarManager().add(customerActionSeparator);

		final AuthorizationService authorizationService = AuthorizationService.getInstance();
		boolean isAuthorizedToCreateAccounts =
				authorizationService.isAuthorizedWithPermission(FulfillmentPermissions.CREATE_EDIT_ACCOUNTS);

		final CreateAccountAction createAccountAction = new CreateAccountAction();
		createAccountAction.setEnabled(isAuthorizedToCreateAccounts);
		final DeleteAccountAction deleteAccountAction = new DeleteAccountAction(this);
		deleteAccountAction.setEnabled(false);

		final ActionContributionItem createAccountActionContributionItem = new ActionContributionItem(createAccountAction);
		final ActionContributionItem deleteAccountActionContributionItem = new ActionContributionItem(deleteAccountAction);

		createAccountActionContributionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		deleteAccountActionContributionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);

		getToolbarManager().appendToGroup(customerActionSeparator.getGroupName(), createAccountActionContributionItem);
		getToolbarManager().appendToGroup(customerActionSeparator.getGroupName(), deleteAccountActionContributionItem);

		this.getViewer().addSelectionChangedListener(event -> {
			final IStructuredSelection strSelection = (IStructuredSelection) event.getSelection();
			Customer customer = (Customer) strSelection.getFirstElement();

			deleteAccountAction.setEnabled(customer != null);
		});
	}

	/**
	 * Get selected account from list.
	 * @return Customer
	 */
	public Customer getSelectedAccount() {
		final IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		Customer customer = null;
		if (!selection.isEmpty()) {
			customer = (Customer) selection.getFirstElement();
		}
		return customer;
	}

	/**
	 * Refresh table data.
	 */
	public void refreshTableContent() {
		if (accountSearchRequestJob != null) {
			accountSearchRequestJob.executeSearchFromIndex(null, getResultsStartIndex());
		}
	}

	@Override
	protected void navigateFirst() {
		getViewer().setInput(EMPTY_ARRAY);
		super.navigateFirst();

		accountSearchRequestJob.executeSearchFromIndex(null, getResultsStartIndex());
		refreshViewerInput();
	}

	@Override
	protected void navigateNext() {
		getViewer().setInput(EMPTY_ARRAY);
		super.navigateNext();

		accountSearchRequestJob.executeSearchFromIndex(null, getResultsStartIndex());
		refreshViewerInput();
	}

	@Override
	protected void navigatePrevious() {
		getViewer().setInput(EMPTY_ARRAY);
		super.navigatePrevious();

		accountSearchRequestJob.executeSearchFromIndex(null, getResultsStartIndex());
		refreshViewerInput();
	}

	@Override
	protected void navigateLast() {
		getViewer().setInput(EMPTY_ARRAY);
		super.navigateLast();

		accountSearchRequestJob.executeSearchFromIndex(null, getResultsStartIndex());
		refreshViewerInput();
	}

	@Override
	protected void navigateTo(final int pageNumber) {
		getViewer().setInput(EMPTY_ARRAY);

		accountSearchRequestJob.executeSearchFromIndex(null, getStartIndexByPageNumber(pageNumber, getResultsPaging()));
		refreshViewerInput();
	}

	@Override
	public AbstractSearchRequestJob<? extends Persistable> getSearchRequestJob() {
		return accountSearchRequestJob;
	}

	@Override
	protected String getPartId() {
		return VIEW_ID;
	}
}