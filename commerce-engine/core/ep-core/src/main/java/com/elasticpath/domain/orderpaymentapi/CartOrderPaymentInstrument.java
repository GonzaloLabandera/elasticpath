/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.orderpaymentapi;

/**
 * Payment instrument linked to a cart order.
 */
public interface CartOrderPaymentInstrument extends CorePaymentInstrument {

    /**
     * Get the cart order UID.
     *
     * @return cart order UID
     */
    long getCartOrderUid();

    /**
     * Sets the cart order UID.
     *
     * @param cartOrderUid cart order UID
     */
    void setCartOrderUid(long cartOrderUid);

}
