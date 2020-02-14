/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Implementation of PaymentLocalizedPropertyValue for Payment Provider Configuration.
 */
@Entity
@DiscriminatorValue("PaymentProviderConfiguration")
public class PaymentLocalizedPropertyValueImpl extends AbstractPaymentLocalizedPropertyValueImpl {
	private static final long serialVersionUID = -7599508209589653382L;
}
