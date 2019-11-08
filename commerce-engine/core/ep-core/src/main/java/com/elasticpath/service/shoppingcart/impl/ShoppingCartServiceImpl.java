/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.shoppingcart.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ItemType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartMemento;
import com.elasticpath.domain.shoppingcart.ShoppingCartMementoHolder;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.actions.FinalizeCheckoutActionContext;
import com.elasticpath.service.store.StoreService;

/** Service for retrieving and saving Shopping Carts. */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods"})
public class ShoppingCartServiceImpl extends AbstractEpPersistenceServiceImpl implements ShoppingCartService {
	private static final String LIST_PARAMETER = "list";
	private static final Logger LOG = Logger.getLogger(ShoppingCartServiceImpl.class);

	/** OpenJPA Query Name. */
	protected static final String SHOPPING_CART_FIND_BY_GUID_EAGER = "SHOPPING_CART_FIND_BY_GUID_EAGER";
	/** OpenJPA Query Name. */
	protected static final String DEFAULT_SHOPPING_CART_FIND_BY_SHOPPER_UID = "DEFAULT_SHOPPING_CART_FIND_BY_SHOPPER_UID";
	/** A list with valid types for root items having children. */
	protected static final List<ItemType> ROOT_ITEM_WITH_CHILDREN_TYPES = Lists.newArrayList(ItemType.BUNDLE, ItemType.SKU_WITH_DEPENDENTS);
	private static final String SHOPPER_UIDS_NULL_ERROR_MESSAGE = "shopperUids must not be null";

	private ShopperService shopperService;
	private StoreService storeService;
	private TimeService timeService;
	private LoadTuner[] loadTuners;

	/**
	 * Updates the given shopping cart.
	 *
	 * @param shoppingCart the shopping cart to update
	 * @return the persisted instance of shoppingCart.
	 * @throws EpServiceException - in case of any errors, if there is an error
	 *         the cart will be in a state consistent with the database.
	 */
	@Override
	public ShoppingCart saveOrUpdate(final ShoppingCart shoppingCart) throws EpServiceException {
		verifyShoppingCartInterfaces(shoppingCart);

		return saveOrUpdate(shoppingCart, (ShoppingCartMementoHolder) shoppingCart);
	}

	/**
	 * Updates the given shopping cart.
	 *
	 * @param shoppingCart the shopping cart to update
	 * @param mementoContainer the shopping cart memento container interface
	 * @return the persisted instance of shoppingCart.
	 * @throws EpServiceException - in case of any errors, if there is an error
	 *         the cart will be in a state consistent with the database.
	 */
	protected ShoppingCart saveOrUpdate(final ShoppingCart shoppingCart, final ShoppingCartMementoHolder mementoContainer) {
		LOG.debug("saving shopping cart...");

		final ShoppingCartMemento shoppingCartMemento = mementoContainer.getShoppingCartMemento();
		shoppingCartMemento.setStoreCode(shoppingCart.getStore().getCode());

		final ShoppingCartMemento updatedShoppingCartMemento = getPersistenceEngine().saveOrUpdate(shoppingCartMemento);
		mementoContainer.setShoppingCartMemento(updatedShoppingCartMemento);

		if (shoppingCart.isActive()) {
			// when cart is deactivated in the final checkout phase, there is no need to set store

			shoppingCart.setStore(storeService.findStoreWithCode(shoppingCartMemento.getStoreCode()));
		}

		return shoppingCart;
	}

	@Override
	public void disconnectCartFromShopperAndCustomerSession(final ShoppingCart oldCart, final FinalizeCheckoutActionContext context) {
		LOG.debug("disconnecting old cart from the shopper ...");

		//save the state so the old cart is no longer referenced
		saveOrUpdate(oldCart);

		ShoppingCartMementoHolder newCartMementoContainer = null;

		try {

			Shopper shopper = oldCart.getShopper();
			shopper.setStoreCode(oldCart.getStore().getCode());
			//create a new cart
			ShoppingCart newShoppingCart = createShoppingCart(shopper, null);

			newCartMementoContainer = (ShoppingCartMementoHolder) newShoppingCart;

			//update new cart memento with store code
			final ShoppingCartMemento shoppingCartMemento = newCartMementoContainer.getShoppingCartMemento();
			shoppingCartMemento.setStoreCode(newShoppingCart.getStore().getCode());


			//connect new cart with cart data, or set to default cart
			newShoppingCart.setDefault(oldCart.isDefault());

			oldCart.getCartData().values()
					.forEach(oldCartValue -> newShoppingCart.setCartDataFieldValue(oldCartValue.getKey(), oldCartValue.getValue()));


			//save new cart memento and update the memento container (i.e. the cart) with saved memento
			final ShoppingCartMemento updatedShoppingCartMemento = getPersistenceEngine().saveOrUpdate(shoppingCartMemento);
			newCartMementoContainer.setShoppingCartMemento(updatedShoppingCartMemento);

			newShoppingCart.setCompletedOrder(context.getOrder());

			//connect new cart with shopper and customer's session
			shopper.setCurrentShoppingCart(newShoppingCart);
			shopper.updateTransientDataWith(context.getCustomerSession());

			shopperService.save(shopper);

		} catch (RuntimeException e) {
			// If the update fails then re-read the memento from the database,
			// the one we tried to write will be broken and no longer usable.
			if (newCartMementoContainer != null && newCartMementoContainer.getShoppingCartMemento().isPersisted()) {
				final ShoppingCartMemento freshlyReadMemento = loadMemento(newCartMementoContainer.getShoppingCartMemento().getUidPk());
				newCartMementoContainer.setShoppingCartMemento(freshlyReadMemento);
			}
			throw e;
		}
	}


	@Override
	public ShoppingCart saveIfNotPersisted(final ShoppingCart shoppingCart) throws EpServiceException {
		verifyShoppingCartInterfaces(shoppingCart);

		if (!((ShoppingCartMementoHolder) shoppingCart).getShoppingCartMemento().isPersisted()) {
			return saveOrUpdate(shoppingCart);
		}

		return shoppingCart;
	}

	private void verifyShoppingCartInterfaces(final ShoppingCart shoppingCart) {
		if (!(shoppingCart instanceof ShoppingCartMementoHolder)) {
			throw new EpServiceException("ShoppingCart class " + shoppingCart.getClass() + " must implements ShoppingCartMementoHolder");
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * This remove is null-safe and only removes a persistent ShoppingCart.
	 */
	@Override
	public void remove(final ShoppingCart shoppingCart) throws EpServiceException {
		verifyShoppingCartInterfaces(shoppingCart);

		if (getShoppingCartMemento(shoppingCart).isPersisted()) {
			getPersistenceEngine().delete(getShoppingCartMemento(shoppingCart));
		}
	}

	/**
	 * Retrieves a specific memento by uid.
	 * @param uidPk the uid of the memento to load.
	 * @return the requested memento , or null if it cannot be found.
	 */
	private ShoppingCartMemento loadMemento(final long uidPk) {
		return getPersistentBeanFinder()
			.withLoadTuners(getLoadTuners())
			.load(ContextIdNames.SHOPPING_CART_MEMENTO, uidPk);
	}

	/**
	 * Returns the shopping cart memento associated with a given shopping cart.
	 *
	 * @param shoppingCart the shopping cart
	 * @return the memento, or null if it does not exist.
	 */
	protected ShoppingCartMemento getShoppingCartMemento(final ShoppingCart shoppingCart) {
		verifyShoppingCartInterfaces(shoppingCart);
		return ((ShoppingCartMementoHolder) shoppingCart).getShoppingCartMemento();
	}

	/**
	 * Sets the shopping cart memento associated with a given shopping cart.
	 *
	 * @param shoppingCart the shopping cart
	 * @param memento the memento
	 */
	protected void setShoppingCartMemento(final ShoppingCart shoppingCart, final ShoppingCartMemento memento) {
		verifyShoppingCartInterfaces(shoppingCart);
		((ShoppingCartMementoHolder) shoppingCart).setShoppingCartMemento(memento);
	}

	/**
	 * Retrieves a specific memento by guid.
	 * @param guid the guid of the memento to load.
	 * @return the requested memento , or null if it cannot be found.
	 */
	private ShoppingCartMemento loadShoppingCartMemento(final String guid) {
		return loadShoppingCartMemento(guid, getShoppingCartFindByGuidEagerNamedQuery());
	}

	/**
	 * Could be called from extensions with a different query name.
	 *
	 * @param guid the guid
	 * @param queryName the query name
	 * @return shopping cart memento
	 */
	protected ShoppingCartMemento loadShoppingCartMemento(final String guid, final String queryName) {
		final List<ShoppingCartMemento> carts = getPersistenceEngine().retrieveByNamedQuery(queryName, guid);
		if (carts.size() == 1) {
			ShoppingCartMemento shoppingCartMemento = carts.get(0);

			restoreDependents(shoppingCartMemento);

			return shoppingCartMemento;
		}
		if (carts.size() > 1) {
			throw new EpServiceException("Inconsistent data -- duplicate guid:" + guid);
		}
		return null;
	}

	/*
		The call in loadShoppingCartMemento fetches only 1st (root) level items, while the call in this method
		brings all children from the given cart and the roots.
		The children are then added to their relevant roots.
	 */
	private void restoreDependents(final ShoppingCartMemento shoppingCartMemento) {
		if (shoppingCartMemento != null && !shoppingCartMemento.getAllItems().isEmpty()) {

			//map parent UID to parent item - filter roots without children (dependent items or bundle constituents)
			Map<Long, ShoppingItem> parentUidToItemMap = shoppingCartMemento.getAllItems().stream()
				.filter(rootItem -> ROOT_ITEM_WITH_CHILDREN_TYPES.contains(rootItem.getItemType()))
				.collect(Collectors.toMap(ShoppingItem::getUidPk, Function.identity()));

			if (parentUidToItemMap.isEmpty()) {
				return;
			}

			List<ShoppingItem> children = getPersistenceEngine().retrieveByNamedQuery("CART_ITEMS_BY_CART_UID",
				shoppingCartMemento.getUidPk());

			//map child UID to child item (some children can be parents as well)
			Map<Long, ShoppingItem> childUidToItemMap = new TreeMap<>();

			children.forEach(shoppingItem -> childUidToItemMap.put(shoppingItem.getUidPk(), shoppingItem));

			//attach children to parents
			for (Map.Entry<Long, ShoppingItem> childEntry : childUidToItemMap.entrySet()) {
				ShoppingItem childItem = childEntry.getValue();

				Long parentUid = childItem.getParentItemUid();
				ShoppingItem root = parentUidToItemMap.get(parentUid);

				if (root == null) {
					root = childUidToItemMap.get(parentUid);
				}

				if (root != null) {
					root.addChildItem(childItem);
				}
			}
		}
	}

	@Override
	public ShoppingCart findByGuid(final String guid) throws EpServiceException {
		getFetchPlanHelper().setLoadTuners(getLoadTuners());

		final ShoppingCartMemento shoppingCartMemento = loadShoppingCartMemento(guid);
		if (shoppingCartMemento == null) {
			return null;
		}

		return createShoppingCart(shoppingCartMemento);
	}

	/**
	 * Factory method which constructs a shopping cart from a memento.
	 *
	 * @param shoppingCartMemento the memento
	 * @return a shopping cart
	 */
	private ShoppingCart createShoppingCart(final ShoppingCartMemento shoppingCartMemento) {
		final long shopperUidFromMemento = shoppingCartMemento.getShopperUid();
		final Shopper shopperFromShoppingCartMemento = shopperService.get(shopperUidFromMemento);

		return createShoppingCart(shopperFromShoppingCartMemento, shoppingCartMemento);
	}

	private ShoppingCart createShoppingCart(final Shopper shopper, final ShoppingCartMemento shoppingCartMemento) {
		String storeCode = shopper.getStoreCode();
		if (shoppingCartMemento != null) {
			storeCode = shoppingCartMemento.getStoreCode();
		}

		final Store store = storeService.findStoreWithCode(storeCode);

		final ShoppingCart shoppingCart = getBean(ContextIdNames.SHOPPING_CART);
		shoppingCart.setStore(store);
		shoppingCart.setShopper(shopper);
		if (shoppingCartMemento != null) {
			setShoppingCartMemento(shoppingCart, shoppingCartMemento);
		}
		return shoppingCart;
	}

	@Override
	public String findDefaultShoppingCartGuidByCustomerSession(final CustomerSession customerSession) throws EpServiceException {

		final List<String> cartGuids = getPersistenceEngine().retrieveByNamedQuery("DEFAULT_SHOPPING_CARTGUID_FIND_BY_SHOPPER_UID",
			new Object[]{customerSession.getShopper().getUidPk()}, 0, 1);
		if (cartGuids.isEmpty()) {
			ShoppingCart shoppingCart = findOrCreateDefaultCartByCustomerSession(customerSession);

			// Call via getShoppingCartService necessary so that a transaction can be started
			getShoppingCartService().saveIfNotPersisted(shoppingCart);
			return shoppingCart.getGuid();
		}
		return cartGuids.get(0);
	}

	@Override
	public String findStoreCodeByCartGuid(final String cartGuid) {
		final List<String> stores = getPersistenceEngine().retrieveByNamedQuery("STORE_BY_CARTGUID",
				new Object[]{cartGuid}, 0, 1);
		return Iterables.getFirst(stores, null);

	}


	@Deprecated
	@Override
	public ShoppingCart findOrCreateByShopper(final Shopper shopper) throws EpServiceException {

		final List<ShoppingCartMemento> carts = getPersistenceEngine()
			.withLoadTuners(getLoadTuners())
			.retrieveByNamedQuery(getFindOrCreateByShopperNamedQuery(),
			new Object[]{shopper.getUidPk()}, 0, 1);

		ShoppingCartMemento shoppingCartMemento = Iterables.getFirst(carts, null);

		restoreDependents(shoppingCartMemento);

		return createShoppingCart(shopper, shoppingCartMemento);
	}

	/**
	 * Return the default name query for finding active shopping cart by shopper uid.
	 *
	 * @return the default query name
	 */
	protected String getFindOrCreateByShopperNamedQuery() {
		return DEFAULT_SHOPPING_CART_FIND_BY_SHOPPER_UID;
	}

	/**
	 * Return the default name query for finding shopping cart by shopper guid.
	 *
	 * @return the default query name
	 */
	protected String getShoppingCartFindByGuidEagerNamedQuery() {
		return SHOPPING_CART_FIND_BY_GUID_EAGER;
	}

	@Override
	public ShoppingCart findOrCreateDefaultCartByCustomerSession(final CustomerSession customerSession) throws EpServiceException {
		ShoppingCart shoppingCart = findOrCreateByShopper(customerSession.getShopper());
		shoppingCart.setCustomerSession(customerSession);
		shoppingCart.setDefault(true);
		return shoppingCart;
	}

	@Override
	public ShoppingCart createByCustomerSession(final CustomerSession customerSession) {
		ShoppingCart shoppingCart = createShoppingCart(customerSession.getShopper(), null);
		shoppingCart.setShopper(customerSession.getShopper());
		return shoppingCart;

	}

	@Override
	public List<String> findByCustomerAndStore(final String customerGuid, final String storeCode) throws EpServiceException {
		return getPersistenceEngine().retrieveByNamedQuery("ACTIVE_SHOPPING_CART_GUID_FIND_BY_CUSTOMER_AND_STORE", customerGuid, storeCode);
	}

	/**
	 * Generic get method for all persistent domain models.
	 *
	 * @param uid the persisted instance uid
	 * @return the persisted instance if exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Object getObject(final long uid) throws EpServiceException {
		ShoppingCart shoppingCart;
		if (uid <= 0) {
			shoppingCart = getBean(ContextIdNames.SHOPPING_CART);
		} else {
			final ShoppingCartMemento memento = loadMemento(uid);
			if (memento == null) {
				shoppingCart = getBean(ContextIdNames.SHOPPING_CART);
			} else {
				shoppingCart = createShoppingCart(memento);
			}
		}

		return shoppingCart;
	}

	@Override
	public void touch(final String cartGuid) {
		ShoppingCartMemento memento = loadShoppingCartMemento(cartGuid);
		if (memento != null) {
			memento.setLastModifiedDate(getTimeService().getCurrentTime());
		}

		getPersistenceEngine().saveOrUpdate(memento);
	}

	@Override
	public int deleteDefaultEmptyShoppingCartsByShopperUids(final List<Long> shopperUids) {
		if (shopperUids == null) {
			throw new EpServiceException(SHOPPER_UIDS_NULL_ERROR_MESSAGE);
		}
		if (shopperUids.isEmpty()) {
			return 0;
		}

		return getPersistenceEngine().executeNamedQueryWithList("DELETE_DEFAULT_EMPTY_SHOPPING_CARTS_BY_SHOPPER_UID", LIST_PARAMETER, shopperUids);
	}

	@Override
	public int deleteAllShoppingCartsByShopperUids(final List<Long> shopperUids) {
		if (shopperUids == null) {
			throw new EpServiceException(SHOPPER_UIDS_NULL_ERROR_MESSAGE);
		}
		if (shopperUids.isEmpty()) {
			return 0;
		}

		getPersistenceEngine().executeNamedQueryWithList("DELETE_ALL_CART_ORDERS_BY_SHOPPER_UID", LIST_PARAMETER, shopperUids);
		return getPersistenceEngine().executeNamedQueryWithList("DELETE_ALL_SHOPPING_CARTS_BY_SHOPPER_UID", LIST_PARAMETER, shopperUids);
	}

	@Override
	public int deleteAllInactiveShoppingCartsByShopperUids(final List<Long> shopperUids) {
		if (shopperUids == null) {
			throw new EpServiceException(SHOPPER_UIDS_NULL_ERROR_MESSAGE);
		}
		if (shopperUids.isEmpty()) {
			return 0;
		}

		getPersistenceEngine().executeNamedQueryWithList("DELETE_ALL_INACTIVE_CART_ORDERS_BY_SHOPPER_UID", LIST_PARAMETER, shopperUids);
		return getPersistenceEngine().executeNamedQueryWithList("DELETE_ALL_INACTIVE_SHOPPING_CARTS_BY_SHOPPER_UID", LIST_PARAMETER, shopperUids);
	}

	@Override
	public int deleteShoppingCartsByGuid(final List<String> shoppingCartGuids) {
		return getPersistenceEngine().executeNamedQueryWithList("SHOPPING_CART_DELETE_BY_GUID", LIST_PARAMETER, shoppingCartGuids);
	}

	@Override
	public Date getShoppingCartLastModifiedDate(final String cartGuid) {
		final List<Date> dates = getPersistenceEngine().retrieveByNamedQuery("FIND_SHOPPING_CART_LAST_MODIFIED_DATE", cartGuid);
		if (dates.isEmpty()) {
			return null;
		}
		return dates.get(0);
	}

	@Override
	public boolean shoppingCartExists(final String cartGuid) {
		final List<String> guids = getPersistenceEngine().retrieveByNamedQuery("FIND_GUID_OF_SHOPPING_CART_WITH_MATCHING_GUID", cartGuid);

		return !guids.isEmpty();
	}

	@Override
	public boolean shoppingCartExistsForStore(final String cartGuid, final String storeCode) {
		final List<String> guids = getPersistenceEngine().retrieveByNamedQuery("FIND_GUID_OF_SHOPPING_CART_WITH_MATCHING_GUID_AND_STORE_CODE",
				cartGuid, storeCode);

		return !guids.isEmpty();
	}

	@Override
	public boolean isPersisted(final ShoppingCart shoppingCart) {
		verifyShoppingCartInterfaces(shoppingCart);
		return ((ShoppingCartMementoHolder) shoppingCart).getShoppingCartMemento().isPersisted();
	}

	@Override
	public Map<String, List<CartData>> findCartDataForCarts(final List<String> cartGuids) {

		Map<String, List<CartData>> result = new HashMap<>();
		List<Object[]> data = getPersistenceEngine().retrieveByNamedQueryWithList("FIND_CART_DATA_FOR_CARTS", "list", cartGuids);
		data.forEach(objects -> {

			Object key = objects[0];
			Object value = objects[1];
			if (!(key instanceof String) || !(value instanceof CartData)) {
				throw new EpServiceException("Data retrieved from database not in correct format");
			}
			String cartGuid = (String) key;
			CartData cartData = (CartData) value;
			List<CartData> cartDataList = result.get(cartGuid);
			if (cartDataList == null) {
				cartDataList = new ArrayList<>();
			}
			cartDataList.add(cartData);
			result.put(cartGuid, cartDataList);

		});
		return result;
	}

	/**
	 * Used for retrieving a txProxyTemplate wrapped copy of this service.
	 * @return shopping cart service
	 */
	protected ShoppingCartService getShoppingCartService() {
		return getSingletonBean(ContextIdNames.SHOPPING_CART_SERVICE, ShoppingCartService.class);
	}

	/**
	 * @param shopperService the shopperService to set
	 */
	public void setShopperService(final ShopperService shopperService) {
		this.shopperService = shopperService;
	}

	/**
	 * @return the shopperService
	 */
	public ShopperService getShopperService() {
		return shopperService;
	}

	/**
	 * @param storeService the storeService to set
	 */
	public void setStoreService(final StoreService storeService) {
		this.storeService = storeService;
	}

	/**
	 * @return the storeService
	 */
	public StoreService getStoreService() {
		return storeService;
	}

	protected TimeService getTimeService() {
		return timeService;
	}

	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
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
}
