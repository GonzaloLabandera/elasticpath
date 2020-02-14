/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.cmclient.fulfillment.domain;

import java.util.Locale;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.util.DateTimeUtilFactory;
import com.elasticpath.cmclient.fulfillment.FulfillmentMessages;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.money.MoneyFormatter;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * A helper class that retrieves displayable data from a given {@link OrderPayment}.
 */
public class OrderPaymentPresenterImpl implements OrderPaymentPresenter {

    private final OrderPayment orderPayment;
    private final Locale locale;
    private final PaymentInstrumentManagementService paymentInstrumentManagementService;
    private final PaymentProviderConfigManagementService paymentProviderConfigManagementService;

    /**
     * Create a new OrderPaymentPresenterImpl.
     *
     * @param orderPayment {@link OrderPayment}.
     * @param locale       order locale
     */
    public OrderPaymentPresenterImpl(final OrderPayment orderPayment, final Locale locale) {
        this.orderPayment = orderPayment;
        this.locale = locale;
        this.paymentInstrumentManagementService =
                BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_INSTRUMENT_MANAGEMENT_SERVICE, PaymentInstrumentManagementService.class);
        this.paymentProviderConfigManagementService =
                BeanLocator.getSingletonBean(ContextIdNames.PAYMENT_PROVIDER_CONFIG_MANAGEMENT_SERVICE,
                        PaymentProviderConfigManagementService.class);
    }

    /**
     * Get the created date for display.
     *
     * @return Order payment created date that is properly formatted for display.
     */
    @Override
    public String getDisplayCreatedDate() {
        return DateTimeUtilFactory.getDateUtil().formatAsDateTime(orderPayment.getCreatedDate());
    }

    /**
     * Get payment method for display. It retrieves payment instrument details based on the payment instrument associated with this Order Payment.
     *
     * @return Payment method String for display.
     */
    @Override
    public String getDisplayPaymentMethod() {
        if (TransactionType.MANUAL_CREDIT.equals(orderPayment.getTransactionType())) {
            return "";
        }
        PaymentInstrumentDTO paymentInstrumentDTO =
                paymentInstrumentManagementService.getPaymentInstrument(orderPayment.getPaymentInstrumentGuid());
        PaymentProviderConfigDTO paymentProviderConfigDTO =
                paymentProviderConfigManagementService.findByGuid(paymentInstrumentDTO.getPaymentProviderConfigurationGuid());
        return paymentProviderConfigDTO.getConfigurationName();
    }

    /**
     * Get localized Transaction Type for display purposes.
     *
     * @return Localized Transaction Type string for display purposes.
     */
    @Override
    public String getDisplayTransactionType() {
        return FulfillmentMessages.get().getLocalizedName(orderPayment.getTransactionType());
    }

    /**
     * Get Payment Instrument Name for display purposes.
     *
     * @return Payment Instrument Name for display purposes.
     */
    @Override
    public String getDisplayPaymentDetails() {
        if (TransactionType.MANUAL_CREDIT.equals(orderPayment.getTransactionType())) {
            return "";
        }
        PaymentInstrumentDTO paymentInstrumentDTO =
                paymentInstrumentManagementService.getPaymentInstrument(orderPayment.getPaymentInstrumentGuid());
        return paymentInstrumentDTO.getName();
    }

    /**
     * Get Order payment status for display.
     *
     * @return Localized Order Payment status for display.
     */
    @Override
    public String getDisplayStatus() {
        return FulfillmentMessages.get().getLocalizedName(orderPayment.getOrderPaymentStatus());
    }

    /**
     * Get Order payment amount formatted for display.
     *
     * @return Amount as a string for display purposes.
     */
    @Override
    public String getDisplayPaymentAmount() {
        String displayPaymentAmount = getMoneyFormatter().formatCurrency(orderPayment.getCurrency(), orderPayment.getAmount(), getLocale());
        return isCreditTransaction() || isReverseChargeTransaction() ? decorateCreditPaymentAmount(displayPaymentAmount) : displayPaymentAmount;
    }

    private boolean isReverseChargeTransaction() {
        return TransactionType.REVERSE_CHARGE.equals(orderPayment.getTransactionType());
    }

    private boolean isCreditTransaction() {
        return TransactionType.CREDIT.equals(orderPayment.getTransactionType())
                || TransactionType.MANUAL_CREDIT.equals(orderPayment.getTransactionType());
    }

    @Override
    public String getDisplayIsOriginalPI() {
        return orderPayment.isOriginalPI() ? FulfillmentMessages.get().OrderPaymentHistorySection_ValueLabel_IsOriginal_PI_Yes
                : FulfillmentMessages.get().OrderPaymentHistorySection_ValueLabel_IsOriginal_PI_No;
    }

    private MoneyFormatter getMoneyFormatter() {
        return BeanLocator.getSingletonBean(ContextIdNames.MONEY_FORMATTER, MoneyFormatter.class);
    }

    private Locale getLocale() {
        return locale;
    }

    private String decorateCreditPaymentAmount(final String displayPaymentAmount) {
        return "-" + displayPaymentAmount;
    }
}