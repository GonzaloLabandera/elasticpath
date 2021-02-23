/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.PolicyKey;

/**
 * This service supports convenience methods which pull together customer profile attributes, attribute policies
 * and store based customer profile attribute key to policy associations to support "views" on the set
 * of customer profile attributes for a given customer and store.
 */
public interface CustomerProfileAttributeService {

	/**
	 * Gets the full set of editable attribute keys for a given store.
	 *
	 * @param storeCode the store
	 * @return the set of editable keys
	 */
	Set<String> getCustomerEditableAttributeKeys(String storeCode);

	/**
	 * Gets the full set of editable attribute keys for a given store.
	 *
	 * @param storeCode the store
	 * @return the set of editable keys
	 */
	Set<String> getAccountEditableAttributeKeys(String storeCode);

	/**
	 * Gets the full set of editable attributes for a given store and customer.
	 * Combines attributes that already have values for the customer along with attributes that do not
	 * but which are still considered editable.
	 *
	 * @param storeCode the store code
	 * @param customer the customer
	 * @return the map of attribute keys to attributes
	 */
	Map<String, Optional<CustomerProfileValue>> getCustomerEditableAttributes(String storeCode, Customer customer);

	/**
	 * Gets the full set of read-only attributes for a given store and customer.
	 * Combines attributes that already have values for the customer along with attributes that do not
	 * but which are still considered viewable.
	 *
	 * @param storeCode the store code
	 * @param customer the customer
	 * @return the map of attribute keys to attributes
	 */
	Map<String, Optional<CustomerProfileValue>> getCustomerReadOnlyAttributes(String storeCode, Customer customer);

	/**
	 * Gets the full set of read-only attributes for a given store and account.
	 * Combines attributes that already have values for the account along with attributes that do not
	 * but which are still considered viewable.
	 *
	 * @param storeCode the store code
	 * @param account the customer
	 * @return the map of attribute keys to attributes
	 */
	Map<String, Optional<CustomerProfileValue>> getAccountReadOnlyAttributes(String storeCode, Customer account);

	/**
	 * Gets the full set of editable attributes for a given store and account.
	 * Combines attributes that already have values for the account along with attributes that do not
	 * but which are still considered viewable.
	 *
	 * @param storeCode the store code
	 * @param account the customer
	 * @return the map of attribute keys to attributes
	 */
	Map<String, Optional<CustomerProfileValue>> getAccountEditableAttributes(String storeCode, Customer account);

	/**
	 * Gets the full set of attributes for a given store and account.
	 *
	 * @param account the account
	 * @return the map of attribute keys to attributes
	 */
	Map<String, Optional<CustomerProfileValue>> getAccountAttributes(Customer account);

	/**
	 * Validates a map of customer attribute values.
	 * @param attributeValueMap the map of attribute keys and values
	 * @param storeCode the storeCode used to lookup shared stores to build the validation context.
	 * @param attributeUsage the attribute usage
	 * @return error messages for validation failure
	 */
	Collection<StructuredErrorMessage> validateAttributes(Map<String, String> attributeValueMap, String storeCode, AttributeUsage attributeUsage);

	/**
	 * Returns the map of predefined profile attribute policies.
	 * @return the map of predefined policies
	 */
	Map<String, PolicyKey> getPredefinedProfileAttributePolicies();
}
