/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.checkout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.elasticpath.domain.orderpaymentapi.OrderPayment;

/**
 * Validates order payments.
 */
public class OrderPaymentValidator extends TypeSafeDiagnosingMatcher<List<OrderPayment>> {
	private final List<OrderPaymentMatcher> orderPaymentMatchers;

	private OrderPaymentValidator(final OrderValidatorBuilder builder) {
		this.orderPaymentMatchers = builder.orderPaymentMatchers;
	}

	/**
	 * Builds an immutable OrderPaymentValidator.
	 */
	public static class OrderValidatorBuilder {
		private final List<OrderPaymentMatcher> orderPaymentMatchers = new ArrayList<>();

		/**
		 * Add the order payment matchers to check.
		 *
		 * @param matchers the matchers
		 * @return this
		 */
		public OrderValidatorBuilder withPaymentMatchers(final OrderPaymentMatcher... matchers) {
			orderPaymentMatchers.addAll(Arrays.asList(matchers));
			return this;
		}

		/**
		 * Builds the OrderPaymentValidator.
		 *
		 * @return the OrderPaymentValidator
		 */
		@SuppressWarnings("PMD.AccessorClassGeneration")
		public OrderPaymentValidator build() {
			return new OrderPaymentValidator(this);
		}
	}

	public static OrderValidatorBuilder builder() {
		return new OrderValidatorBuilder();
	}

	private boolean validPayments(final List<OrderPayment> orderPayments) {
		if (orderPayments.size() != orderPaymentMatchers.size()) {
			return false;
		}

		for (OrderPaymentMatcher orderPaymentMatcher : orderPaymentMatchers) {
			boolean matchingPaymentFound = false;
			Iterator<OrderPayment> orderIterator = orderPayments.iterator();

			while (orderIterator.hasNext() && !matchingPaymentFound) {
				OrderPayment orderFromPayment = orderIterator.next();
				if (orderPaymentMatcher.matches(orderFromPayment)) {
					orderIterator.remove();
					matchingPaymentFound = true;
				}
			}

			if (!matchingPaymentFound) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Order Payments:");
		description.appendValue(orderPaymentMatchers);
	}

	@Override
	protected boolean matchesSafely(List<OrderPayment> orderPayments, Description description) {
		boolean valid = true;

		if (!validPayments(orderPayments)) {
			description.appendText("Order Payments:")
					.appendValue(orderPayments);
			valid = false;
		}
		return valid;
	}
}
