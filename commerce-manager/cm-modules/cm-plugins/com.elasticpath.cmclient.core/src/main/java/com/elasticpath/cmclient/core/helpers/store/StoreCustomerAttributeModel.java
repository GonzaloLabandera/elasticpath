/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.core.helpers.store;

import com.elasticpath.domain.customer.PolicyKey;

/**
 * Store customer attribute model.
 */
public class StoreCustomerAttributeModel {

	private String guid;

	private String attributeKey;

	private PolicyKey policyKey;

	private String storeCode;

	/**
	 * Constructor.
	 *
	 * @param guid      the guid
	 * @param storeCode the store code
	 */
	public StoreCustomerAttributeModel(final String guid, final String storeCode) {
		this.guid = guid;
		this.storeCode = storeCode;
	}

	/**
	 * Constructor.
	 *
	 * @param guid         the guid
	 * @param attributeKey the attribute key
	 * @param policyKey    the policy key
	 * @param storeCode    the store code
	 */
	public StoreCustomerAttributeModel(final String guid, final String attributeKey, final PolicyKey policyKey, final String storeCode) {
		this.guid = guid;
		this.attributeKey = attributeKey;
		this.policyKey = policyKey;
		this.storeCode = storeCode;
	}

	/**
	 * Get the guid.
	 *
	 * @return the guid
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * Get the attribute key.
	 *
	 * @return the attribute key
	 */
	public String getAttributeKey() {
		return attributeKey;
	}

	/**
	 * Get the policy key.
	 *
	 * @return the policy key
	 */
	public PolicyKey getPolicyKey() {
		return policyKey;
	}

	/**
	 * Get the store code.
	 *
	 * @return the store code
	 */
	public String getStoreCode() {
		return storeCode;
	}

	/**
	 * Set the guid.
	 *
	 * @param guid the guid
	 */
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	/**
	 * Set the attribute key.
	 *
	 * @param attributeKey the attribute key
	 */
	public void setAttributeKey(final String attributeKey) {
		this.attributeKey = attributeKey;
	}

	/**
	 * Set the policy key.
	 *
	 * @param policyKey the policy key
	 */
	public void setPolicyKey(final PolicyKey policyKey) {
		this.policyKey = policyKey;
	}

	/**
	 * Set the store code.
	 *
	 * @param storeCode the store code
	 */
	public void setStoreCode(final String storeCode) {
		this.storeCode = storeCode;
	}
}
