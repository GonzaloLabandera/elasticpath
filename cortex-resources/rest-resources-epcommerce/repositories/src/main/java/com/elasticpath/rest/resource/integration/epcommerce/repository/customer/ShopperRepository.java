/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer;

import io.reactivex.Single;

import com.elasticpath.domain.shopper.Shopper;

/**
 * A repository for {@link Shopper}s.
 */
public interface ShopperRepository {

	/**
	 * Find or create a Shopper based on data in the Subject, populated from the Cortex request headers.
	 *
	 * @return the shopper with associated customer session
	 */
	Single<Shopper> findOrCreateShopper();

	/**
	 * Find or create a Shopper based on passed parameters.
	 *
	 * @param userGuid the user/shopper GUID
	 * @param storeCode the store code
	 * @return the shopper with associated customer session
	 */
	Single<Shopper> findOrCreateShopper(String userGuid, String storeCode);

	/**
	 * Find or create a Shopper based on passed parameters.
	 *
	 * @param userGuid the user/shopper GUID
	 * @param accountSharedId the account shared ID
	 * @param storeCode the store code
	 * @return the shopper with associated customer session
	 */
	Single<Shopper> findOrCreateShopper(String userGuid, String accountSharedId, String storeCode);
}
