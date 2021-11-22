package com.elasticpath.cmclient.fulfillment.editors.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.EpUiException;
import com.elasticpath.cmclient.core.ui.util.EditorUtil;
import com.elasticpath.cmclient.fulfillment.FulfillmentImageRegistry;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.editors.customer.AccountDetailsEditor;
import com.elasticpath.cmclient.fulfillment.views.customer.AccountSearchResultsView;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.service.customer.AccountTreeService;
import com.elasticpath.service.customer.CustomerService;

/**
 * Delete account action.
 */
public class DeleteAccountAction extends Action {

	private final AccountTreeService accountTreeService = BeanLocator.getSingletonBean(ContextIdNames.ACCOUNT_TREE_SERVICE, AccountTreeService.class);

	private final AccountSearchResultsView listView;

	/**
	 * Constructor.
	 * @param listView the account list view
	 */
	public DeleteAccountAction(final AccountSearchResultsView listView) {
		super(FulfillmentMessages.get().DeleteAccountWizard_DeleteAccount_Label, FulfillmentImageRegistry.DELETE_ACCOUNT_ICON);
		this.listView = listView;
	}

	@Override
	public void run() {
		final CustomerService customerService = BeanLocator.getSingletonBean(ContextIdNames.CUSTOMER_SERVICE, CustomerService.class);
		final AccountTreeService accountTreeService = BeanLocator.getSingletonBean(ContextIdNames.ACCOUNT_TREE_SERVICE, AccountTreeService.class);

		Customer account = listView.getSelectedAccount();

		List<String> descendantGuids = accountTreeService.findDescendantGuids(account.getGuid());
		descendantGuids.add(account.getGuid());

		if (customerService.countAssociatedOrders(descendantGuids) > 0) {
			MessageDialog.openInformation(listView.getSite().getShell(), FulfillmentMessages.get().DeleteAccountWarningTitle,
					FulfillmentMessages.get().DeleteAccountWarningText);
		} else {
			boolean confirmed = MessageDialog.openConfirm(listView.getSite().getShell(), FulfillmentMessages.get().ConfirmDeleteAccountMsgBoxTitle,
					NLS.bind(FulfillmentMessages.get().ConfirmDeleteAccountMsgBoxText, descendantGuids.size()));

			if (confirmed) {
				closeAccountTab(account);
				customerService.remove(account);
				listView.refreshTableContent();
			}
		}
	}

	/**
	 * Closes account details tab.
	 * @param account the account
	 */
	private void closeAccountTab(final Customer account) {
		List<String> descendantAccountGuids = accountTreeService.findDescendantGuids(account.getGuid());
		descendantAccountGuids.add(account.getGuid());

		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		for (IEditorReference editorRef : workbenchPage.getEditorReferences()) {
			try {
				Customer customerEditor = editorRef.getEditorInput().getAdapter(Customer.class);
				if (EditorUtil.isSameEditor(editorRef, AccountDetailsEditor.ID_EDITOR) && descendantAccountGuids.contains(customerEditor.getGuid())) {
					IEditorPart editorPart = editorRef.getEditor(false);
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editorPart, true);
				}
			} catch (final PartInitException e) {
				throw new EpUiException("Could not get saved condition editor input", e); //$NON-NLS-1$
			}
		}
	}

}
