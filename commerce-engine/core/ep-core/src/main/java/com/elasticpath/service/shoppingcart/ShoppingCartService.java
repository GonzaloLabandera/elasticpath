/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.shoppingcart;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.EpPersistenceService;

/**
 * Provide customer-session related business service.
 */
public interface ShoppingCartService extends EpPersistenceService {

	/**
	 * Updates the given shopping cart. Note that client code should use CartDirector.saveShoppingCart().
	 *
	 * @param shoppingCart the shopping cart to update
	 * @return the persisted instance of shoppingCart
	 * @throws EpServiceException - in case of any errors
	 */
	ShoppingCart saveOrUpdate(ShoppingCart shoppingCart) throws EpServiceException;

	/**
	 * Saves the given shopping cart if and only if the cart has never been persisted before.
	 *
	 * @param shoppingCart the shopping cart to save
	 * @return the persisted instance of shoppingCart
	 * @throws EpServiceException - in case of any errors
	 */
	ShoppingCart saveIfNotPersisted(ShoppingCart shoppingCart) throws EpServiceException;

	/**
	 * Removes the given shopping cart.
	 *
	 * @param shoppingCart the shopping cart to remove.
	 * @throws EpServiceException if the cart cannot be removed.
	 */
	void remove(ShoppingCart shoppingCart) throws EpServiceException;

	/**
	 * Gets the descriptors for a given cart.
	 * @param cartGuid the cart.
	 * @return the map of cartData descriptors. map.
	 */
	Map<String, String> getCartDescriptors(String cartGuid);

	/**
	 * This method retrieves the shopping cart corresponding to the given guid.<br/>
	 * Note that the result of this method is not yet a valid ShoppingCart. Calling code needs to call 
	 * setCustomerSession() and then calculateShoppingCartTaxAndBeforeTaxPrices() in order to ensure that the
	 * taxes and totals are calculated.
	 *
	 * @param guid the guid for the shopping cart to retrieve
	 * @return the shopping cart corresponding to the specified guid or null if no matching cart can be found
	 * @throws EpServiceException - in case of any errors
	 */
	ShoppingCart findByGuid(String guid) throws EpServiceException;

	/**
	 * This method retrieves the shopping cart corresponding to the given Shopper.
	 *
	 * @param shopper the shopping context that defines the shopping cart.
	 * @return the default shopping cart corresponding to the shopper
	 * @throws EpServiceException - in case of any errors
	 */
	ShoppingCart findOrCreateDefaultCartByShopper(Shopper shopper) throws EpServiceException;

	/**
	 * Creates a shopping cart for a shopper.
	 * @param shopper the shopper.
	 * @return an empty shopping cart.
	 */
	ShoppingCart createByShopper(Shopper shopper);

	/**
	 * "Touches" the cart by setting the last modified date on the given cart to the current date.
	 *
	 * @param shoppingCartGuid the cart guid of the cart to touch
	 * @throws EpServiceException if something goes wrong
	 */
	void touch(String shoppingCartGuid) throws EpServiceException;

	/**
	 * Deletes all shopping carts that are associated with the {@link Shopper} uid.
	 * Even the ones that are not empty.
	 * 
	 * @param shopperUid the shopper id.
	 * @return the number of deleted shopping carts
	 */
	int deleteAllShoppingCartsByShopperUid(Long shopperUid);

	/**
	 * Returns a list of ShoppingCart GUIDs for the given Customer GUID and Store Code.
	 *
	 * @param customerGuid the customer GUID
	 * @param accountSharedId the account shared ID, or null if it is not specified
	 * @param storeCode the store code
	 * @return The list of ShoppingCart GUIDS or an empty list if there are none.
	 */
	List<String> findByCustomerAndStore(String customerGuid, String accountSharedId, String storeCode);

	/**
	 * Deletes shopping carts given their GUIDs.
	 *
	 * @param shoppingCartGuids list of guids
	 * @return the number of deleted shopping carts
	 */
	int deleteShoppingCartsByGuid(List<String> shoppingCartGuids);

	/**
	 * Gets the last modified date of the shopping cart.
	 *
	 * @param cartGuid the cart Guid
	 * @return the last modified date
	 */
	Date getShoppingCartLastModifiedDate(String cartGuid);

	/**
	 * Checks if a shopping cart with the given cart GUID exists.
	 *
	 * @param cartGuid the cart guid
	 * @return true, shopping cart exists
	 */
	boolean shoppingCartExists(String cartGuid);

	/**
	 * Checks if shopping cart exists for a store.
	 *
	 * @param cartGuid the cart guid
	 * @param storeCode the store code
	 * @return true, if shopping cart exists in store
	 */
	boolean shoppingCartExistsForStore(String cartGuid, String storeCode);

	/**
	 * Returns true if this cart has ever been persisted.
	 *
	 * @param shoppingCart the cart
	 * @return true if the cart has ever been persisted
	 */
	boolean isPersisted(ShoppingCart shoppingCart);

	/**
	 * Finds the default (active) shopping cart guid for the given Shopper.
	 * @param shopper the shopper
	 * @return the shopping cart guid of the default cart.
	 * @throws EpServiceException - in case of any errors
	 */
	String findOrCreateDefaultCartGuidByShopper(Shopper shopper) throws EpServiceException;

	/**
	 *  Finds the store code for the given cart guid.
	 * @param cartGuid the cart guid.
	 * @return the storecode.
	 */
	String findStoreCodeByCartGuid(String cartGuid);

	/**
	 * Retrieves a list of cart data maps.
	 * @param cartGuids the cart guids.
	 * @return the list of maps of cartdatas.
	 */
	Map<String, List<Map<String, String>>> findCartDataForCarts(List<String> cartGuids);

	/**
	 * Change cart's state so it is no longer associated
	 * with the shopper. All deactivated carts and respective orders
	 * will be removed in a batch job.
	 *
	 * @param shoppingCart the shopping cart
	 */
	void deactivateCart(ShoppingCart shoppingCart);
}
