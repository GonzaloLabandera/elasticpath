/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.customer.StoreCustomerAttributeService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.store.StoreService;

/**
 * Implementation of {@link StoreCustomerAttributeService}.
 */
public class StoreCustomerAttributeServiceImpl extends AbstractEpPersistenceServiceImpl implements StoreCustomerAttributeService {

	private StoreService storeService;

	@Override
	public Optional<StoreCustomerAttribute> findByGuid(final String guid) {
		final List<StoreCustomerAttribute> results = getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_ATTRIBUTE_FIND_BY_GUID", guid);

		return results.stream()
				.findFirst();
	}

	@Override
	public Optional<StoreCustomerAttribute> findByStoreCodeAndAttributeKey(final String storeCode, final String attributeKey) {
		final List<StoreCustomerAttribute> result = getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_ATTRIBUTE",
				getNormalizedStoreCode(storeCode), attributeKey);

		return result.stream()
				.findFirst();
	}

	@Override
	public List<StoreCustomerAttribute> findByGuids(final List<String> guids) {
		return getPersistenceEngine().retrieveByNamedQueryWithList("CUSTOMER_ATTRIBUTE_FIND_BY_GUIDS", "list", guids);
	}

	@Override
	public List<StoreCustomerAttribute> findByStore(final String storeCode) {
		return getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_ATTRIBUTES_FIND_BY_STORE", getNormalizedStoreCode(storeCode));
	}

	@Override
	public List<StoreCustomerAttribute> findAll() {
		return getPersistenceEngine().retrieveByNamedQuery("CUSTOMER_ATTRIBUTES_FIND_ALL");
	}

	@Override
	public void remove(final StoreCustomerAttribute storeCustomerAttribute) {
		getPersistenceEngine().delete(storeCustomerAttribute);
	}

	@Override
	public void add(final StoreCustomerAttribute storeCustomerAttribute) {
		getPersistenceEngine().save(storeCustomerAttribute);
	}

	@Override
	public void update(final StoreCustomerAttribute storeCustomerAttribute) {
		getPersistenceEngine().saveOrUpdate(storeCustomerAttribute);
	}

	@Override
	public void updateAll(final String storeCode, final Map<String, StoreCustomerAttribute> storeCustomerAttributes) {
		final List<StoreCustomerAttribute> attributes = this.findByStore(storeCode);
		final Map<String, StoreCustomerAttribute> updatedAttributeMap = new HashMap<>(storeCustomerAttributes);
		for (StoreCustomerAttribute attribute : attributes) {
			final StoreCustomerAttribute updatedAttribute = updatedAttributeMap.remove(attribute.getGuid());
			if (updatedAttribute == null) {
				this.remove(attribute);
			} else {
				attribute.setAttributeKey(updatedAttribute.getAttributeKey());
				attribute.setPolicyKey(updatedAttribute.getPolicyKey());
				this.update(attribute);
			}
		}

		updatedAttributeMap.forEach((key, value) -> this.add(value));
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return getPersistenceEngine().get(StoreCustomerAttribute.class, uid);
	}

	private String getNormalizedStoreCode(final String storeCode) {
		// normalize the code
		Store store = storeService.findStoreWithCode(storeCode);
		if (store != null) {
			return store.getCode();
		}
		return null;
	}

	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}
}
