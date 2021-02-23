/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.common.dto.StructuredErrorMessageType;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.CustomerAccessor;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidator;

/**
 * Determines if Customer have status SUSPENDED.
 */
public class SuspendedAccountShoppingCartValidatorImpl implements ShoppingCartValidator {

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
	public Collection<StructuredErrorMessage> validate(final ShoppingCartValidationContext context) {
		final Customer account = getAccountFromContext(context);

		if (Objects.nonNull(context) && isAccountSuspended(account)) {
			return createErrorMessage(context);
		}

		return Collections.emptyList();
	}

	private Customer getAccountFromContext(final ShoppingCartValidationContext context) {
		return Optional.ofNullable(context)
				.map(ShoppingCartValidationContext::getShoppingCart)
				.map(ShoppingCart::getShopper)
				.map(CustomerAccessor::getAccount)
				.orElse(null);
	}

	private boolean isAccountSuspended(final Customer account) {
		return Optional.ofNullable(account)
				.map(Customer::getStatus)
				.map(status -> status == Customer.STATUS_SUSPENDED)
				.orElse(false);
	}

	/**
	 * Creates {@link StructuredErrorMessage} regarding to information from {@link ShoppingCartValidationContext}.
	 *
	 * @param context context for the validation.
	 * @return a collection of Structured Error Messages containing validation errors.
	 */
	protected Collection<StructuredErrorMessage> createErrorMessage(final ShoppingCartValidationContext context) {
		final Customer account = context.getShoppingCart().getShopper().getAccount();
		final Map<String, String> data = new HashMap<>();
		data.put("account-shared-id", account.getSharedId());
		data.put("account-business-name", account.getBusinessName());

		final StructuredErrorMessage errorMessage = new StructuredErrorMessage(StructuredErrorMessageType.ERROR, MESSAGE_ID,
				"The account you are transacting for is currently suspended.", data);

		return Collections.singletonList(errorMessage);
	}
}
