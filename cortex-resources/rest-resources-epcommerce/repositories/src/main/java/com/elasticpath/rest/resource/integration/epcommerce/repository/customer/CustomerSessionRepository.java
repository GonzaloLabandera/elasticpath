/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer;

import io.reactivex.Completable;
import io.reactivex.Single;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.rest.command.ExecutionResult;

/**
 * Encapsulates operations on customer session.
 */
public interface CustomerSessionRepository {

	/**
	 * Find or create a customer session. The customer session will have a shopper with a valid TagSet.
	 *
	 * @return the customer session
	 */
	Single<CustomerSession> findOrCreateCustomerSession();

	/**
	 * Finds or creates the customer session for the given context.
	 * @return the customer session.
	 */
	Single<CustomerSession> createCustomerSessionAsSingle();

	/**
	 * Creates a customer session for a given customer.
	 *
	 * @param customerGuid the customer guid.
	 * @param storeCode the store code.
	 * @return a customer session.
	 * @deprecated use {@link #findCustomerSessionByGuidAndStoreCodeAsSingle}
	 */
	@Deprecated
	ExecutionResult<CustomerSession> findCustomerSessionByGuidAndStoreCode(String customerGuid, String storeCode);

	/**
	 * Creates a customer session for a given customer.
	 *
	 * @param customerGuid the customer guid.
	 * @param storeCode the store code.
	 * @return a customer session.
	 */
	Single<CustomerSession> findCustomerSessionByGuidAndStoreCodeAsSingle(String customerGuid, String storeCode);

	/**
	 * Creates a customer session for a given customer and account.
	 *
	 * @param customerGuid the customer guid.
	 * @param accountSharedId the account shared Id.
	 * @return a customer session.
	 */
	Single<CustomerSession> findCustomerSessionByGuidAndAccountSharedIdAsSingle(String customerGuid, String accountSharedId);

	/**
	 * Creates a customer session for a given customer.
	 *
	 * @param storeCode      the storeCode.
	 * @param customerUserId the customer guid.
	 * @return a customer session.
	 */
	ExecutionResult<CustomerSession> findCustomerSessionBySharedId(String storeCode, String customerUserId);

	/**
	 * Creates a customer session for a given customer and account.
	 *
	 * @param customerGuid the customer guid.
	 * @param accountSharedId the account shared ID.
	 * @param storeCode the storeCode.
	 * @return a customer session.
	 */
	ExecutionResult<CustomerSession> findCustomerSessionByCustomerGuidAndAccountSharedId(
			String customerGuid,
			String accountSharedId,
			String storeCode);

	/**
	 * Creates a customer session for a given customer and account.
	 *
	 * @param customerSharedId the customer shared ID.
	 * @param accountSharedId  the account shared ID.
	 * @param storeCode  the storeCode.
	 * @return a customer session.
	 */
	ExecutionResult<CustomerSession> findCustomerSessionByUserIdAndAccountSharedId(String customerSharedId, String accountSharedId, String storeCode);

	/**
	 * Triggers invalidation of CustomerSession instance associated with given customer guid.
	 *
	 * @param customerGuid the customer guid.
	 * @return completed Completable
	 */
	Completable invalidateCustomerSessionByGuid(String customerGuid);
}
