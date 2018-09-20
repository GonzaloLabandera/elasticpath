/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.domain.customer.impl;

import java.util.Arrays;
import java.util.Collection;

/**
 * Unit test for {@link CustomerCreditCardImpl}.
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class CustomerCreditCardImplTest extends AbstractPaymentMethodImplTest<CustomerCreditCardImpl> {

	@Override
	protected CustomerCreditCardImpl create() {
		return new CustomerCreditCardImpl();
	}

	@Override
	protected Collection<String> getExcludedFieldNames() {
		return Arrays.asList("guid");
	}
}
