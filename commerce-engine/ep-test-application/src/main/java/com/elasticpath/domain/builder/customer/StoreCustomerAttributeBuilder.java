/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.builder.customer;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.builder.DomainObjectBuilder;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.StoreCustomerAttribute;

/**
 * A builder that builds {@link StoreCustomerAttribute}s for testing purposes.
 */
public class StoreCustomerAttributeBuilder implements DomainObjectBuilder<StoreCustomerAttribute> {

	@Autowired
	private BeanFactory beanFactory;

	private Long uidPk = 0L;

	private String guid;

	private String storeCode;

	private String attributeKey;

	private PolicyKey policyKey;

	/**
	 * Get the instance.
	 * @return the instance
	 */
	public StoreCustomerAttributeBuilder newInstance() {
		final StoreCustomerAttributeBuilder newBuilder = new StoreCustomerAttributeBuilder();
		newBuilder.setBeanFactory(beanFactory);
		return newBuilder;
	}

	/**
	 * Add the uidpk.
	 * @param uidPk the uidpk
	 * @return the builder
	 */
	public StoreCustomerAttributeBuilder withUidPk(final Long uidPk) {
		this.uidPk = uidPk;
		return this;
	}

	/**
	 * Add the guid.
	 * @param guid the guid
	 * @return the builder
	 */
	public StoreCustomerAttributeBuilder withGuid(final String guid) {
		this.guid = guid;
		return this;
	}

	/**
	 * Add the store code.
	 * @param storeCode the store code
	 * @return the builder
	 */
	public StoreCustomerAttributeBuilder withStoreCode(final String storeCode) {
		this.storeCode = storeCode;
		return this;
	}

	/**
	 * Add the attribute key.
	 * @param attributeKey the attribute key
	 * @return the builder
	 */
	public StoreCustomerAttributeBuilder withAttributeKey(final String attributeKey) {
		this.attributeKey = attributeKey;
		return this;
	}

	/**
	 * Add the policy key.
	 * @param policyKey the policy key
	 * @return the builder
	 */
	public StoreCustomerAttributeBuilder withPolicyKey(final PolicyKey policyKey) {
		this.policyKey = policyKey;
		return this;
	}

	@Override
	public StoreCustomerAttribute build() {
		final StoreCustomerAttribute storeCustomerAttribute =
				beanFactory.getPrototypeBean(ContextIdNames.STORE_CUSTOMER_ATTRIBUTE, StoreCustomerAttribute.class);
		storeCustomerAttribute.setUidPk(uidPk);
		storeCustomerAttribute.setGuid(guid);
		storeCustomerAttribute.setStoreCode(storeCode);
		storeCustomerAttribute.setAttributeKey(attributeKey);
		storeCustomerAttribute.setPolicyKey(policyKey);
		return storeCustomerAttribute;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
