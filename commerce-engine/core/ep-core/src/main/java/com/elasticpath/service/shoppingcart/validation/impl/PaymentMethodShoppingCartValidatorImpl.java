/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.base.GloballyIdentifiable;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingCartValidator;

/**
 * Determines if a valid payment method has been specified.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_CART_AT_CHECKOUT, priority = 1060)
public class PaymentMethodShoppingCartValidatorImpl extends XPFExtensionPointImpl implements ShoppingCartValidator {

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "need.payment.method";

	@Autowired
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingCartValidationContext context) {
		if (context.isPaymentRequired()) {
			XPFShopper shopper = context.getShoppingCart().getShopper();
			Set<String> selectedPaymentInstrumentGuids;
			if (shopper.getAccount() == null) {
				selectedPaymentInstrumentGuids = getSelectedPaymentInstrumentGuids(context.getShoppingCart().getCartOrderGuid(),
						shopper.getStore().getCode(), shopper.getUser().getGuid());
			} else {
				selectedPaymentInstrumentGuids = getSelectedPaymentInstrumentGuids(context.getShoppingCart().getCartOrderGuid(),
						shopper.getStore().getCode(), shopper.getAccount().getGuid());
			}
			// If payment required
			if (context.isPaymentRequired() && selectedPaymentInstrumentGuids.isEmpty()) {
				XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, MESSAGE_ID,
						"Payment method must be specified.", Collections.emptyMap(),
						new XPFStructuredErrorResolution(CartOrder.class, context.getShoppingCart().getCartOrderGuid()));
				return Collections.singletonList(errorMessage);
			}
		}
		return Collections.emptyList();
	}

	private Set<String> getSelectedPaymentInstrumentGuids(final String cartOrderGuid, final String storeCode, final String customerGuid) {
		Set<String> paymentInstrumentGuids =
				filteredPaymentInstrumentService.findCartOrderPaymentInstrumentsForCartOrderGuidAndStore(cartOrderGuid, storeCode).stream()
						.map(GloballyIdentifiable::getGuid)
						.collect(Collectors.toSet());

		CustomerPaymentInstrument defaultPaymentInstrument =
				filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerGuidAndStore(customerGuid, storeCode);
		if (defaultPaymentInstrument != null) {
			paymentInstrumentGuids.add(defaultPaymentInstrument.getGuid());
		}

		return paymentInstrumentGuids;
	}

}
