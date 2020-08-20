/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.shoppingcart.dao;

import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.WishList;

/**
 * The wish list dao interface.
 */
public interface WishListDao {

	/**
	 * find wish list by shopping context data.
	 *
	 * @param shopper the customer context data
	 * @return the wish list found
	 */
	WishList findByShopper(Shopper shopper);

	/**
	 * Get wish list by uidpk.
	 *
	 * @param uid the uidpk
	 * @return the wish list found
	 */
	WishList get(long uid);

	/**
	 * Get wish list by guid.
	 * @param guid the guid
	 * @return the wish list found
	 */
	WishList findByGuid(String guid);
	
	/**
	 * save or update wish list.
	 *
	 * @param wishList the wish list to be saved or updated.
	 * @return the saved/updated wish list
	 */
	WishList saveOrUpdate(WishList wishList);

	/**
	 * Remove the wish list.
	 *
	 * @param wishList the wish list
	 */
	void remove(WishList wishList);
}
