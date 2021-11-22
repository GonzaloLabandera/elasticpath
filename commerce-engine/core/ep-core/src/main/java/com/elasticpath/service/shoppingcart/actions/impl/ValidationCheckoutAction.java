/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import java.util.Collection;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.service.shoppingcart.actions.CheckoutAction;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.exception.CheckoutValidationException;
import com.elasticpath.service.shoppingcart.validation.PurchaseCartValidationService;

/**
 * Checkout action for validating the shopping cart state.
 */
public class ValidationCheckoutAction implements CheckoutAction {

	private PurchaseCartValidationService purchaseCartValidationService;

	@Override
	public void execute(final PreCaptureCheckoutActionContext context) throws EpSystemException {
		final Collection<StructuredErrorMessage> errorMessages = purchaseCartValidationService.validate(context.getShoppingCart(),
				context.getShopper(), context.getShoppingCart().getStore());

		if (!errorMessages.isEmpty()) {
			throw new CheckoutValidationException(errorMessages);
		}
	}

	protected PurchaseCartValidationService getPurchaseCartValidationService() {
		return purchaseCartValidationService;
	}

	public void setPurchaseCartValidationService(final PurchaseCartValidationService purchaseCartValidationService) {
		this.purchaseCartValidationService = purchaseCartValidationService;
	}
}
