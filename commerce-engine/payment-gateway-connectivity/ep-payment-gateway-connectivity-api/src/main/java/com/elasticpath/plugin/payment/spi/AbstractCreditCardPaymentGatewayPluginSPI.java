/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.plugin.payment.spi;

import com.elasticpath.plugin.payment.PaymentGatewayType;
import com.elasticpath.plugin.payment.capabilities.CreditCardCapability;
import com.elasticpath.plugin.payment.dto.OrderPaymentDto;
import com.elasticpath.plugin.payment.dto.PayerAuthenticationEnrollmentResultDto;
import com.elasticpath.plugin.payment.dto.ShoppingCartDto;
import com.elasticpath.plugin.payment.dto.impl.PayerAuthenticationEnrollmentResultDtoImpl;

/**
 * Service Provider Interface for extension classes implementing credit card payment gateway plugins.
 */
public abstract class AbstractCreditCardPaymentGatewayPluginSPI extends AbstractPaymentGatewayPluginSPI
		implements CreditCardCapability {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1L;

	private boolean validateCvv;

	@Override
	public PaymentGatewayType getPaymentGatewayType() {
		return PaymentGatewayType.CREDITCARD;
	}

	@Override
	public boolean isCvv2ValidationEnabled() {
		return validateCvv;
	}

	@Override
	public void setValidateCvv2(final boolean validate) {
		this.validateCvv = validate;
	}

	@Override
	public PayerAuthenticationEnrollmentResultDto checkEnrollment(final ShoppingCartDto shoppingCart, final OrderPaymentDto payment) {
		return new PayerAuthenticationEnrollmentResultDtoImpl();
	}

	@Override
	public boolean validateAuthentication(final OrderPaymentDto payment, final String paRes) {
		return true;
	}

}
