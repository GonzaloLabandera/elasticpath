/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.wizards;

import static com.elasticpath.cmclient.fulfillment.FulfillmentMessages.EMPTY_STRING;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.ui.framework.EpControlFactory.EpState;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutComposite;
import com.elasticpath.cmclient.core.ui.framework.IEpLayoutData;
import com.elasticpath.cmclient.core.wizard.AbstractEPWizardPage;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;

/**
 * Provides selection of payment source - original or input new one.
 */
@SuppressWarnings({"PMD.UnusedPrivateField"})
public class PaymentSourceOptionsComposite {

	private static final int MARGIN = 15;

	private final IEpLayoutComposite mainComposite;
	private final Order order;
	private final PurposeEnum purpose;

	private List<PaymentInstrumentDTO> alternatePaymentInstruments;
	private final List<PaymentInstrumentDTO> orderPaymentInstruments;

	private Button originalPaymentSourceRadioButton;
	private Button alternatePaymentSourceRadioButton;
	private CCombo alternatePaymentSourceCombo;
	private final AbstractEPWizardPage<?> parentPage;
	private Label authorizationNoteLabel;
	/**
	 * The constructor.
	 *
	 * @param parentPage    {@link AbstractEPWizardPage}.
	 * @param order         order.
	 * @param pageComposite composite on which the control will be created.
	 * @param layoutData    layout used to place the control.
	 * @param purpose       what the selected payment instrument will be used for.
	 */
	@SuppressWarnings({"PMD.UnusedFormalParameter"})
	public PaymentSourceOptionsComposite(final AbstractEPWizardPage<?> parentPage,
										 final Order order,
										 final IEpLayoutComposite pageComposite,
										 final IEpLayoutData layoutData,
										 final PurposeEnum purpose) {
		this.order = order;
		this.purpose = purpose;
		this.parentPage = parentPage;

		mainComposite = pageComposite.addTableWrapLayoutComposite(1, false, layoutData);

		if (purpose == PurposeEnum.AUTHORIZATION) {
			alternatePaymentInstruments = findCustomerInstruments();
		}
		orderPaymentInstruments = getPaymentInstrumentManagementService().findOrderInstruments(order);

		createEpPageContent();
	}

	private List<PaymentInstrumentDTO> findCustomerInstruments() {
		final PaymentInstrumentManagementService paymentInstrumentManagementService = getPaymentInstrumentManagementService();
		return getFilteredPaymentInstrumentService().findCustomerPaymentInstrumentsForCustomerAndStore(order.getCustomer(), order.getStoreCode())
				.stream()
				.map(CustomerPaymentInstrument::getPaymentInstrumentGuid)
				.map(paymentInstrumentManagementService::getPaymentInstrument)
				.collect(Collectors.toList());
	}

	/**
	 * @return true if payment instrument selection was made
	 */
	public boolean isValid() {
		return originalPaymentSourceRadioButton.getSelection() || alternatePaymentSourceCombo.getSelectionIndex() > 0;
	}

	/**
	 * @return true if payment instrument selection was made
	 */
	public boolean isAlternateSelection() {
		return alternatePaymentSourceRadioButton.getSelection();
	}

	/**
	 * @return selected payment instrument (original or newly inputed).
	 */
	public List<PaymentInstrumentDTO> getSelectedPaymentInstruments() {
		return Stream.concat(getSelectedOriginalPaymentInstruments().stream(), getSelectedAlternatePaymentInstruments().stream())
				.collect(Collectors.toList());
	}

	/**
	 * @return selected original payment instrument.
	 */
	public List<PaymentInstrumentDTO> getSelectedOriginalPaymentInstruments() {
		if (originalPaymentSourceRadioButton.getSelection()) {
			if (purpose == PurposeEnum.AUTHORIZATION) {
				return getPaymentInstrumentManagementService().findUnlimitedOrderInstruments(order);
			} else {
				return orderPaymentInstruments;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * @return selected alternate payment instrument.
	 */
	public List<PaymentInstrumentDTO> getSelectedAlternatePaymentInstruments() {
		if (alternatePaymentSourceCombo != null && isAlternateSelection()) {
			final int selectionIndex = alternatePaymentSourceCombo.getSelectionIndex() - 1;
			if (selectionIndex < 0) {
				parentPage.setErrorMessage(FulfillmentMessages.get().ExchangeWizard_PaymentSourceShouldBeSelected_Message);
				return Collections.emptyList();
			}
			return alternatePaymentInstruments.subList(selectionIndex, selectionIndex + 1);
		}
		return Collections.emptyList();
	}

	/**
	 * Sets message for authorization note.
	 *
	 * @param noteMessage note message.
	 */
	public void setAuthorizationNoteMessage(final String noteMessage) {
		authorizationNoteLabel.setText(noteMessage);
	}

	private boolean hasSingleReservePerPI() {
		return orderPaymentInstruments.stream().anyMatch(PaymentInstrumentDTO::isSingleReservePerPI);
	}

	private void createOriginalPaymentSourceRadioButton() {
		if (hasSingleReservePerPI() && purpose == PurposeEnum.AUTHORIZATION) {
			originalPaymentSourceRadioButton = mainComposite.addRadioButton(
					FulfillmentMessages.get().RefundWizard_PaymentSource, EpState.DISABLED,
					mainComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
		} else {
			originalPaymentSourceRadioButton = mainComposite.addRadioButton(
					FulfillmentMessages.get().RefundWizard_PaymentSource, EpState.EDITABLE,
					mainComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
		}
	}

	private void createEpPageContent() {
		createOriginalPaymentSourceRadioButton();

		final IEpLayoutComposite selectOriginalPaymentSourceComposite = mainComposite.addTableWrapLayoutComposite(2, false,
				mainComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, true, false));

		final TableWrapLayout originalPaymentSourceLayout = (TableWrapLayout) selectOriginalPaymentSourceComposite.getSwtComposite().getLayout();
		originalPaymentSourceLayout.leftMargin = MARGIN;

		CCombo originalPaymentSourceCombo = selectOriginalPaymentSourceComposite.addComboBox(EpState.EDITABLE,
				selectOriginalPaymentSourceComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, true, false));
		originalPaymentSourceCombo.add(FulfillmentMessages.get().RefundWizard_OriginalPaymentSource);
		originalPaymentSourceCombo.select(0);
		if (hasSingleReservePerPI() && purpose == PurposeEnum.AUTHORIZATION) {
			final IEpLayoutComposite noteComposite = mainComposite.addTableWrapLayoutComposite(2, false,
					mainComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, true, false));
			noteComposite.addLabelBold(FulfillmentMessages.get().RefundOptionsComposite_CautionHeader_Label,
					noteComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
			noteComposite.addLabel(FulfillmentMessages.get().RefundOptionsComposite_OriginalPaymentSourceBlocked_Label,
					noteComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
		}

		originalPaymentSourceRadioButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent event) {
				originalPaymentSourceCombo.setEnabled(true);
				if (alternatePaymentSourceCombo != null) {
					alternatePaymentSourceCombo.setEnabled(false);
				}
				parentPage.setErrorMessage(null);
			}

			@Override
			public void widgetSelected(final SelectionEvent event) {
				originalPaymentSourceCombo.setEnabled(true);
				if (alternatePaymentSourceCombo != null) {
					alternatePaymentSourceCombo.setEnabled(false);
				}
				parentPage.setErrorMessage(null);
			}
		});

		if (purpose == PurposeEnum.AUTHORIZATION) {
			addAlternatePaymentSourceRadioButton(originalPaymentSourceCombo);
		} else {
			addManualRefundRadioButton(originalPaymentSourceCombo);
		}

		if (hasSingleReservePerPI() && purpose == PurposeEnum.AUTHORIZATION) {
			originalPaymentSourceRadioButton.setSelection(false);
			originalPaymentSourceCombo.setEnabled(false);
		} else {
			originalPaymentSourceRadioButton.setSelection(true);
		}

		final IEpLayoutComposite noteComposite = mainComposite.addTableWrapLayoutComposite(2, false,
				mainComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, true, false));

		noteComposite.addLabelBold(FulfillmentMessages.get().RefundOptionsComposite_CautionHeader_Label,
				noteComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
		if (purpose == PurposeEnum.AUTHORIZATION) {
			authorizationNoteLabel = noteComposite.addLabel(EMPTY_STRING,
					noteComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
		} else {
			noteComposite.addLabel(FulfillmentMessages.get().RefundOptionsComposite_CautionRefund_Label,
					noteComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
		}
	}

	private void addAlternatePaymentSourceRadioButton(final CCombo originalPaymentSourceCombo) {
		alternatePaymentSourceRadioButton = mainComposite.addRadioButton(
				FulfillmentMessages.get().RefundWizard_AlternatePaymentSource, EpState.EDITABLE,
				mainComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
		alternatePaymentSourceRadioButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent event) {
				originalPaymentSourceCombo.setEnabled(false);
				alternatePaymentSourceCombo.setEnabled(true);
				parentPage.setErrorMessage(null);
			}

			@Override
			public void widgetSelected(final SelectionEvent event) {
				originalPaymentSourceCombo.setEnabled(false);
				alternatePaymentSourceCombo.setEnabled(true);
				parentPage.setErrorMessage(null);
			}
		});

		final IEpLayoutComposite selectAlternatePaymentSourceComposite = mainComposite.addTableWrapLayoutComposite(2, false,
				mainComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, true, false));

		final TableWrapLayout alternatePaymentSourceLayout = (TableWrapLayout) selectAlternatePaymentSourceComposite.getSwtComposite().getLayout();
		alternatePaymentSourceLayout.leftMargin = MARGIN;

		alternatePaymentSourceCombo = selectAlternatePaymentSourceComposite.addComboBox(EpState.EDITABLE,
				selectAlternatePaymentSourceComposite.createLayoutData(IEpLayoutData.FILL, IEpLayoutData.BEGINNING, true, false));

		alternatePaymentSourceCombo.add(FulfillmentMessages.get().RefundWizard_SelectAlternatePaymentSource);
		for (PaymentInstrumentDTO instrument : alternatePaymentInstruments) {
			alternatePaymentSourceCombo.add(instrument.getName());
		}
		alternatePaymentSourceCombo.select(0);
		if (hasSingleReservePerPI() && purpose == PurposeEnum.AUTHORIZATION) {
			alternatePaymentSourceRadioButton.setSelection(true);
			alternatePaymentSourceCombo.setEnabled(true);
		} else {
			alternatePaymentSourceCombo.setEnabled(false);
		}

		alternatePaymentSourceCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				parentPage.setErrorMessage(null);
			}
		});
	}

	private void addManualRefundRadioButton(final CCombo originalPaymentSourceCombo) {
		final Button manualRefundRadioButton = mainComposite.addRadioButton(
				FulfillmentMessages.get().RefundWizard_ManualRefund, EpState.EDITABLE,
				mainComposite.createLayoutData(IEpLayoutData.BEGINNING, IEpLayoutData.BEGINNING));
		manualRefundRadioButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(final SelectionEvent event) {
				originalPaymentSourceCombo.setEnabled(false);
			}

			@Override
			public void widgetSelected(final SelectionEvent event) {
				originalPaymentSourceCombo.setEnabled(false);
			}
		});
	}

	protected static PaymentInstrumentManagementService getPaymentInstrumentManagementService() {
		return BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_INSTRUMENT_MANAGEMENT_SERVICE, PaymentInstrumentManagementService.class);
	}

	protected static FilteredPaymentInstrumentService getFilteredPaymentInstrumentService() {
		return BeanLocator.getSingletonBean(ContextIdNames.FILTERED_PAYMENT_INSTRUMENT_SERVICE, FilteredPaymentInstrumentService.class);
	}

	/**
	 * Enum representing the purpose of the payment method selection.
	 */
	public enum PurposeEnum {
		/**
		 * Order payment will be used for doing authorizations.
		 */
		AUTHORIZATION,

		/**
		 * Order payment will be used for doing refunds.
		 */
		REFUND
	}
}
