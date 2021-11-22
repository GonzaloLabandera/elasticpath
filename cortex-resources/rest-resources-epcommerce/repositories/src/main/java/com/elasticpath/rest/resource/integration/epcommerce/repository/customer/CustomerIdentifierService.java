/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer;

import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.rest.command.ExecutionResult;

/**
 * Utilities for customer identification strategy purposes.
 */
public interface CustomerIdentifierService {

    /**
     * Util method which invokes corresponding strategy's isCustomerExists method based on input parameters.
     *
     * @param userId userId of the customer
     * @param customerType customer type
     * @param issuer issuer
     *
     * @return true if customer exists for given parameters.
     */
    ExecutionResult<Boolean> isCustomerExists(String userId, CustomerType customerType, String issuer);

    /**
     * Util method over deriveCustomer method of corresponding strategy which returns retrieved customer's guid.
     *
     * @param userId userId of the customer
	 * @param customerType customer type
     * @param issuer issuer
     *
     * @return guid of the derived customer.
     */
    ExecutionResult<String> deriveCustomerGuid(String userId, CustomerType customerType, String issuer);
}
