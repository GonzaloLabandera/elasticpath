/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.wizards.account;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import com.elasticpath.base.GloballyIdentifiable;
import com.elasticpath.cmclient.catalog.CatalogImageRegistry;
import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.event.ItemChangeEvent;
import com.elasticpath.cmclient.core.wizard.AbstractEpWizard;
import com.elasticpath.cmclient.core.wizard.EpWizardDialog;
import com.elasticpath.cmclient.fulfillment.FulfillmentImageRegistry;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.cmclient.fulfillment.event.FulfillmentEventService;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.service.customer.CustomerService;

/**
 * The wizard for creating of the account.
 */
public class CreateAccountWizard extends AbstractEpWizard<Customer> {

	private final CustomerService customerService = BeanLocator.getSingletonBean(ContextIdNames.CUSTOMER_SERVICE, CustomerService.class);
	private final Customer customer;

	private final AccountCreateWizardDetailsPage accountCreateWizardDetailsPage;
	private final AccountCreateWizardAttributesPage accountCreateWizardAttributesPage;

	private static final String PAGE_ACCOUNT_DETAILS = "PageAccountDetails";
	private static final String PAGE_ACCOUNT_ATTRIBUTES = "PageAccountAttributes";

	/**
	 * Constructor.
	 *
	 * @param windowTitle the title of the wizard.
	 * @param customer    parent account.
	 */
	public CreateAccountWizard(final String windowTitle, final Customer customer) {
		super(windowTitle, StringUtils.EMPTY, CatalogImageRegistry.getImage(FulfillmentImageRegistry.ADD_ACCOUNT_ICON));
		accountCreateWizardDetailsPage = new AccountCreateWizardDetailsPage(PAGE_ACCOUNT_DETAILS,
				FulfillmentMessages.get().CreateAccountWizard_AccountDetails_Title);
		accountCreateWizardAttributesPage = new AccountCreateWizardAttributesPage(PAGE_ACCOUNT_ATTRIBUTES,
				FulfillmentMessages.get().CreateAccountWizard_AccountAttributes_Title);
		this.customer = initializeCustomer(customer);
	}

	@Override
	public void addPages() {
		addPage(accountCreateWizardDetailsPage);
		addPage(accountCreateWizardAttributesPage);
	}

	@Override
	public void onUpdateButtons(final EpWizardDialog dialog) {
		final IWizardPage currentPage = dialog.getCurrentPage();
		if (accountCreateWizardDetailsPage.equals(currentPage)) {
			dialog.getWizardButton(IDialogConstants.FINISH_ID).setEnabled(false);
			dialog.getWizardButton(IDialogConstants.NEXT_ID).setEnabled(accountCreateWizardDetailsPage.validate());
		} else {
			dialog.getWizardButton(IDialogConstants.FINISH_ID).setEnabled(accountCreateWizardAttributesPage.validate());
		}
	}

	@Override
	protected Customer getModel() {
		return customer;
	}

	@Override
	public boolean performFinish() {
		final Customer customerToSave = getModel();

		final String requiredAttributesWithoutValues = accountCreateWizardAttributesPage.findRequiredAttributesWithoutValues(customerToSave);

		if (StringUtils.isNotEmpty(requiredAttributesWithoutValues)) {
			final String errorMessage = NLS.bind("Before finish you should fill {0} required attributes!", requiredAttributesWithoutValues);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Fill required attributes error", errorMessage);
			return false;
		}

		customerService.add(customerToSave);
		FulfillmentEventService.getInstance().fireCustomerChangeEvent(new ItemChangeEvent<>(this, customerToSave));
		return true;
	}

	private Customer initializeCustomer(final Customer parent) {
		final Customer target = BeanLocator.getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		target.setCustomerType(CustomerType.ACCOUNT);
		Optional.ofNullable(parent)
				.map(GloballyIdentifiable::getGuid)
				.ifPresent(target::setParentGuid);
		return target;
	}

}
