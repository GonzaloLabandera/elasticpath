/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.pf4j.Extension;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;
import com.elasticpath.xpf.connectivity.entity.XPFCustomer;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingCartValidator;

/**
 * Determines if a valid email address has been provided.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_CART_AT_CHECKOUT, priority = 1030)
public class EmailAddressShoppingCartValidatorImpl extends XPFExtensionPointImpl implements ShoppingCartValidator {

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "need.email";

	/**
	 * Validates customer email address.
	 *
	 * @param context context for the validation.
	 * @return a collection of Structured Error Messages containing validation errors, or an
	 * empty collection if the validation is successful.
	 */
	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingCartValidationContext context) {
		XPFShoppingCart shoppingCart = context.getShoppingCart();

		XPFShopper shopper = shoppingCart.getShopper();

		XPFCustomer customer = shopper.getUser();

		String email = customer.getEmail();

		if (isValidEmail(email)) {
			return Collections.emptyList();
		}

		XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, MESSAGE_ID,
				"Customer email address must be specified.", Collections.emptyMap(),
				new XPFStructuredErrorResolution(Customer.class, customer.getGuid()));
		return Collections.singletonList(errorMessage);
	}

	/**
	 * Determine if the email address is valid.
	 *
	 * @param emailAddress the email address
	 * @return true if the email is valid
	 */
	protected boolean isValidEmail(final String emailAddress) {
		return StringUtils.isNotBlank(emailAddress) && EmailValidator.getInstance().isValid(emailAddress);
	}
}
