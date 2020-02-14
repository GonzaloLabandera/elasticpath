/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.common.factory;

import com.elasticpath.common.dto.customer.builder.CustomerDTOBuilder;

/**
 * A factory for producing test {@link CustomerDTOBuilder}s.
 */
public class TestCustomerDTOBuilderFactory {
	private static final String TEST_GUID = "testGuid";

	/**
	 * Create {@link CustomerDTOBuilder} pre-populated with default test values.
	 *
	 * @return the {@link CustomerDTOBuilder}
	 */
	public CustomerDTOBuilder create() {
		return new CustomerDTOBuilder()
				.withGuid(TEST_GUID);
	}

}
