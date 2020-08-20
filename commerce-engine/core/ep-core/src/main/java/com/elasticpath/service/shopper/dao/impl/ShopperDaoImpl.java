/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.shopper.dao.impl;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.ShopperMemento;
import com.elasticpath.domain.shopper.impl.ShopperMementoImpl;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.shopper.dao.ShopperDao;

/**
 * The wish list dao implementation class.
 */
public class ShopperDaoImpl implements ShopperDao {

	private PersistenceEngine persistenceEngine;

	/** Format string for missing field error. */
	public static final String FIELD_MISSING_ERROR = "%s must be supplied.";

	@Override
	public ShopperMemento get(final long uid) throws EpServiceException {
		return getPersistenceEngine().load(ShopperMementoImpl.class, uid);
	}

	@Override
	public void remove(final ShopperMemento shopperMemento) {
		if (shopperMemento != null) {
			this.getPersistenceEngine().delete(shopperMemento);
		}
	}

	@Override
	public ShopperMemento saveOrUpdate(final ShopperMemento shopperMemento) {
		return getPersistenceEngine().saveOrUpdate(shopperMemento);
	}

	@Override
	public ShopperMemento findByCustomerAndStoreCode(final Customer customer, final String storeCode) {
		return findByCustomerGuidAndStoreCode(customer.getGuid(), storeCode);
	}

	@Override
	public ShopperMemento findByCustomerAccountAndStore(final Customer customer, final Customer account, final String storeCode) {
		return findByCustomerGuidAccountSharedIdAndStore(customer.getGuid(), account.getSharedId(), storeCode);
	}

	@Override
	public ShopperMemento findByCustomerGuid(final String customerGuid) {
		final List<ShopperMemento> results = persistenceEngine.<ShopperMemento>retrieveByNamedQuery("FIND_SHOPPER_BY_CUSTOMER_GUID", customerGuid);

		return (ShopperMemento) CollectionUtils.find(results, Objects::nonNull);
	}

	@Override
	public ShopperMemento findByCustomerGuidAndStoreCode(final String customerGuid, final String storeCode) {
		final List<ShopperMemento> results = persistenceEngine.<ShopperMemento>retrieveByNamedQuery("FIND_SHOPPER_BY_CUSTOMER_GUID_AND_STORECODE",
																									customerGuid, storeCode);
		return (ShopperMemento) CollectionUtils.find(results, Objects::nonNull);
	}

	@Override
	public ShopperMemento findByCustomerGuidAccountSharedIdAndStore(final String customerGuid, final String accountSharedId,
																	final String storeCode) {
		final List<ShopperMemento> results = persistenceEngine.<ShopperMemento>retrieveByNamedQuery(
				"FIND_SHOPPER_BY_CUSTOMER_GUID_ACCOUNT_ID_AND_STORECODE",
				customerGuid, accountSharedId, storeCode);

		return (ShopperMemento) CollectionUtils.find(results, Objects::nonNull);
	}

	@Override
	public ShopperMemento findByCustomerSharedIdAndStore(final String customerSharedId, final String storeCode) {
		final List<ShopperMemento> results = persistenceEngine.<ShopperMemento>retrieveByNamedQuery(
				"FIND_SHOPPER_BY_CUSTOMER_SHAREDID_AND_STORECODE",
				customerSharedId,
				storeCode);

		return (ShopperMemento) CollectionUtils.find(results, Objects::nonNull);
	}

	@Override
	public ShopperMemento findByCustomerSharedIdAndAccountSharedIdAndStore(final String customerSharedId, final String accountSharedId,
																		final String storeCode) {
		final List<ShopperMemento> results = persistenceEngine.<ShopperMemento>retrieveByNamedQuery(
				"FIND_SHOPPER_BY_CUSTOMER_ID_ACCOUNT_ID_AND_STORECODE",
				customerSharedId, accountSharedId, storeCode);
		return (ShopperMemento) CollectionUtils.find(results, Objects::nonNull);
	}


	// Getters / Setters
	// ------------------

	/**
	 * Get the persistence Engine.
	 *
	 * @return the persistence engine
	 */
	public PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	/**
	 * Set the persistence Engine.
	 *
	 * @param persistenceEngine the persistence engine
	 */
	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}
}
