/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.wizards.account;

import org.drools.core.util.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import com.elasticpath.cmclient.core.EpUiException;
import com.elasticpath.cmclient.core.binding.EpControlBindingProvider;
import com.elasticpath.cmclient.core.binding.EpWizardPageSupport;
import com.elasticpath.cmclient.core.binding.ObservableUpdateValueStrategy;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.validation.CompoundValidator;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;
import com.elasticpath.cmclient.core.wizard.AbstractEPWizardPage;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.domain.customer.Customer;

/**
 * Account create wizard.
 */
public class AccountCreateWizardDetailsPage extends AbstractEPWizardPage<Customer> {

	private static final int PAGE_LAYOUT_NUM_COLUMNS = 1;
	private static final int STATUS_ACTIVE = 1;
	private static final int STATUS_DISABLED = 0;

	private CCombo accountStatus;

	private Text businessNameText;

	private Text businessNumber;

	private Text phoneNumber;

	private Text faxNumber;

	/**
	 * Constructor.
	 *
	 * @param pageName the page name
	 * @param title    the page title
	 */
	protected AccountCreateWizardDetailsPage(final String pageName, final String title) {
		super(PAGE_LAYOUT_NUM_COLUMNS, true, pageName, new DataBindingContext());

		setTitle(title);
	}

	@Override
	protected void createEpPageContent(final IEpLayoutComposite pageComposite) {
		final IEpLayoutComposite container = pageComposite.addScrolledGridLayoutComposite(1, true);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		createAccountDetailsSection(container);

		setControl(pageComposite.getSwtComposite());
	}

	@Override
	protected void populateControls() {
		// do nothing
	}

	private void createAccountDetailsSection(final IEpLayoutComposite container) {
		final IEpLayoutComposite overviewComposite = container.addGridLayoutSection(3, StringUtils.EMPTY, ExpandableComposite.TITLE_BAR,
				container.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL, true, false));

		final IEpLayoutData labelData = overviewComposite.createLayoutData(IEpLayoutData.END, IEpLayoutData.FILL);

		final IEpLayoutData fieldData = overviewComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.FILL,
				true, false, 2, 1);

		overviewComposite.addLabelBoldRequired(FulfillmentMessages.get().CreateAccountWizard_BusinessName, EpControlFactory.EpState.EDITABLE,
				labelData);
		businessNameText = overviewComposite.addTextField(EpControlFactory.EpState.EDITABLE, fieldData);

		overviewComposite.addLabelBold(FulfillmentMessages.get().CreateAccountWizard_BusinessNumber, labelData);
		businessNumber = overviewComposite.addTextField(EpControlFactory.EpState.EDITABLE, fieldData);

		overviewComposite.addLabelBold(FulfillmentMessages.get().CreateAccountWizard_PhoneNumber, labelData);
		phoneNumber = overviewComposite.addTextField(EpControlFactory.EpState.EDITABLE, fieldData);

		overviewComposite.addLabelBold(FulfillmentMessages.get().CreateAccountWizard_FaxNumber, labelData);
		faxNumber = overviewComposite.addTextField(EpControlFactory.EpState.EDITABLE, fieldData);

		overviewComposite.addLabelBoldRequired(FulfillmentMessages.get().CreateAccountWizard_Status, EpControlFactory.EpState.EDITABLE, labelData);
		accountStatus = overviewComposite.addComboBox(EpControlFactory.EpState.EDITABLE, fieldData);
		initializeAccountStatus();
	}

	private void initializeAccountStatus() {
		accountStatus.add("Disabled", STATUS_DISABLED);
		accountStatus.add("Active", STATUS_ACTIVE);
		accountStatus.select(1);
		accountStatus.setEditable(false);
	}

	@Override
	public boolean beforeNext(final PageChangingEvent event) {
		if (isBusinessNameEmpty()) {
			setErrorMessage(NLS.bind("Business name must be specified ", getBusinessName()));
			return false;
		}

		setErrorMessage(null);
		return true;
	}

	private boolean isBusinessNameEmpty() {
		return StringUtils.isEmpty(getBusinessName());
	}

	private String getBusinessName() {
		return getModel().getBusinessName();
	}


	/**
	 * Validates business name before going to the next page.
	 *
	 * @return false if validation failed.
	 */
	public boolean validate() {
		return !isBusinessNameEmpty();
	}

	@Override
	protected void bindControls() {
		bindBusinessNameText();
		bindBusinessNumber();
		bindPhoneNumber();
		bindFaxNumber();
		bindAccountStatus();
		EpWizardPageSupport.create(this, getDataBindingContext());
	}

	private void bindBusinessNameText() {
		final ObservableUpdateValueStrategy businessNameUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				getModel().setBusinessName((String) newValue);
				return Status.OK_STATUS;
			}
		};

		EpControlBindingProvider.getInstance().bind(getDataBindingContext(), this.businessNameText,
				new CompoundValidator(EpValidatorFactory.MAX_LENGTH_255), null, businessNameUpdateStrategy, true);
	}

	private void bindBusinessNumber() {
		final ObservableUpdateValueStrategy businessNumberUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				getModel().setAccountBusinessNumber((String) newValue);
				return Status.OK_STATUS;
			}
		};

		EpControlBindingProvider.getInstance().bind(getDataBindingContext(), this.businessNumber,
				new CompoundValidator(EpValidatorFactory.MAX_LENGTH_255), null, businessNumberUpdateStrategy, true);
	}

	private void bindPhoneNumber() {
		final ObservableUpdateValueStrategy phoneNumberUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				getModel().setAccountPhoneNumber((String) newValue);
				return Status.OK_STATUS;
			}
		};

		EpControlBindingProvider.getInstance().bind(getDataBindingContext(), this.phoneNumber,
				new CompoundValidator(EpValidatorFactory.PHONE_IGNORE_SPACES), null, phoneNumberUpdateStrategy, true);
	}

	private void bindFaxNumber() {
		final ObservableUpdateValueStrategy faxNumberUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				getModel().setAccountFaxNumber((String) newValue);
				return Status.OK_STATUS;
			}
		};

		EpControlBindingProvider.getInstance().bind(getDataBindingContext(), this.faxNumber,
				new CompoundValidator(EpValidatorFactory.FAX_IGNORE_SPACES), null, faxNumberUpdateStrategy, true);
	}

	private void bindAccountStatus() {
		final ObservableUpdateValueStrategy accountStatusUpdateStrategy = new ObservableUpdateValueStrategy() {
			@Override
			protected IStatus doSet(final IObservableValue observableValue, final Object newValue) {
				final int statusIndex = accountStatus.getSelectionIndex();
				getModel().setStatus(resolveStatus(statusIndex));
				return Status.OK_STATUS;
			}

			private int resolveStatus(final int statusIndex) {
				final int status;
				switch (statusIndex) {
					case STATUS_ACTIVE:
						status = Customer.STATUS_ACTIVE;
						break;
					case STATUS_DISABLED:
						status = Customer.STATUS_DISABLED;
						break;
					default:
						throw new EpUiException("Customer status is not valid", null);
				}
				return status;
			}
		};

		EpControlBindingProvider.getInstance().bind(getDataBindingContext(), this.accountStatus,
				new CompoundValidator(EpValidatorFactory.REQUIRED), null, accountStatusUpdateStrategy, true);
	}
}
