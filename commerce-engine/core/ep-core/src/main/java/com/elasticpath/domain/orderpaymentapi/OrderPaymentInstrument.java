/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.orderpaymentapi;

/**
 * Relationship of OrderPayment and PaymentInstrument.
 */
public interface OrderPaymentInstrument extends CorePaymentInstrument {

    /**
     * Get the order number.
     *
     * @return order number
     */
    String getOrderNumber();

    /**
     * Sets the order number.
     *
     * @param orderNumber order number
     */
    void setOrderNumber(String orderNumber);

}
