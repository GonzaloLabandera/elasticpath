/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.pf4j.Extension;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFCustomer;
import com.elasticpath.xpf.connectivity.entity.XPFCustomerStatusEnum;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ShoppingCartValidator;

/**
 * Determines if Customer have status SUSPENDED.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_SHOPPING_CART_AT_CHECKOUT, priority = 1080)
public class SuspendedAccountShoppingCartValidatorImpl extends XPFExtensionPointImpl implements ShoppingCartValidator {

	/**
	 * Message id for this validation.
	 */
	public static final String MESSAGE_ID = "account.suspended";

	/**
	 * Validates customer status.
	 *
	 * @param context context for the validation.
	 * @return a collection of Structured Error Messages containing validation errors, or an
	 * empty collection if the validation is successful.
	 */
	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFShoppingCartValidationContext context) {
		final XPFCustomer contextAccount = getAccountFromContext(context);

		if (context != null && isAccountSuspended(contextAccount)) {
			return createErrorMessage(contextAccount);
		}

		return Collections.emptyList();
	}

	private XPFCustomer getAccountFromContext(final XPFShoppingCartValidationContext context) {
		return Optional.ofNullable(context)
				.map(XPFShoppingCartValidationContext::getShoppingCart)
				.map(XPFShoppingCart::getShopper)
				.map(XPFShopper::getAccount)
				.orElse(null);
	}

	private boolean isAccountSuspended(final XPFCustomer account) {
		return Optional.ofNullable(account)
				.map(XPFCustomer::getStatus)
				.map(status -> status == XPFCustomerStatusEnum.STATUS_SUSPENDED)
				.orElse(false);
	}

	/**
	 * Creates {@link XPFStructuredErrorMessage} regarding to information from {@link XPFShoppingCartValidationContext}.
	 *
	 * @param account account for the validation.
	 * @return a collection of Structured Error Messages containing validation errors.
	 */
	protected Collection<XPFStructuredErrorMessage> createErrorMessage(final XPFCustomer account) {
		final Map<String, String> data = new HashMap<>();
		data.put("account-shared-id", account.getSharedId());
		data.put("account-business-name", getApName(account));

		final XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.ERROR, MESSAGE_ID,
				"The account you are transacting for is currently suspended.", data);

		return Collections.singletonList(errorMessage);
	}

	private String getApName(final XPFCustomer account) {
		return account.getAttributeValueByKey("AP_NAME", null)
				.map(XPFAttributeValue::getStringValue).orElse(null);
	}
}
