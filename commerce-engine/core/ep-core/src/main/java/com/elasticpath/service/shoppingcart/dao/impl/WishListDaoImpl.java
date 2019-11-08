/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.shoppingcart.dao.impl;

import java.util.List;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.domain.shoppingcart.impl.WishListImpl;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.shoppingcart.dao.WishListDao;

/**
 * The wish list dao implementation class.
 */
public class WishListDaoImpl implements WishListDao {

	private PersistenceEngine persistenceEngine;
	private LoadTuner[] loadTuners;

	@Override
	public WishList findByShopper(final Shopper shopper) {

		Object[] params = new Object[] { shopper.getUidPk()};
		List<WishList> results = getPersistenceEngine()
			.withLoadTuners(getLoadTuners())
			.retrieveByNamedQuery("WISHLIST_BY_SHOPPING_CONTEXT", params);

		WishList wishList = null;
		if (!results.isEmpty()) {
			wishList = results.get(0);
		}
		return wishList;
	}

	@Override
	public WishList get(final long uid) throws EpServiceException {
		return getPersistenceEngine()
			.withLoadTuners(getLoadTuners())
			.load(WishListImpl.class, uid);
	}

	@Override
	public WishList findByGuid(final String guid) {
		List<WishList> results = getPersistenceEngine()
			.withLoadTuners(getLoadTuners())
			.retrieveByNamedQuery("WISHLIST_BY_GUID", guid);

		WishList wishList = null;
		if (!results.isEmpty()) {
			wishList = results.get(0);
		}
		return wishList;
	}

	@Override
	public void remove(final WishList wishList) {
		if (wishList != null) {
			this.getPersistenceEngine().delete(wishList);
		}
	}

	@Override
	public WishList saveOrUpdate(final WishList wishList) {
		return getPersistenceEngine().saveOrUpdate(wishList);
	}

	// This warning had to suppressed because the code is correct as per
	// https://pmd.github.io/latest/pmd_rules_java_performance.html#optimizabletoarraycall
	//TODO remove @SuppressWarnings after upgrading the PMD to 6.x
	@SuppressWarnings("PMD.OptimizableToArrayCall")
	public void setLoadTuners(final List<LoadTuner> loadTuners) {
		this.loadTuners = loadTuners.toArray(new LoadTuner[0]);
	}

	private LoadTuner[] getLoadTuners() {
		return loadTuners;
	}

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

	@Override
	public int deleteEmptyWishListsByShopperUids(final List<Long> shopperUids) {
		if (shopperUids == null) {
			throw new EpServiceException("shopperUids must not be null");
		}
		if (shopperUids.isEmpty()) {
			return 0;
		}

		return persistenceEngine.executeNamedQueryWithList("DELETE_EMPTY_WISHLISTS_BY_SHOPPER_UID", "list", shopperUids);
	}

	@Override
	public int deleteAllWishListsByShopperUids(final List<Long> shopperUids) {
		if (shopperUids == null) {
			throw new EpServiceException("shopperUids must not be null");
		}
		if (shopperUids.isEmpty()) {
			return 0;
		}

		return persistenceEngine.executeNamedQueryWithList("DELETE_ALL_WISHLISTS_BY_SHOPPER_UID", "list", shopperUids);
	}
}
