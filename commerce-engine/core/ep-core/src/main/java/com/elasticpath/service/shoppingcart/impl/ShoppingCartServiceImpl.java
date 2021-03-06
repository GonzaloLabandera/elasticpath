/*
 * Copyright (c) Elastic Path Software Inc., 2019
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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ItemType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartMemento;
import com.elasticpath.domain.shoppingcart.ShoppingCartMementoHolder;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiCleanupService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.store.StoreService;

/** Service for retrieving and saving Shopping Carts. */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods"})
public class ShoppingCartServiceImpl extends AbstractEpPersistenceServiceImpl implements ShoppingCartService {
	private static final String LIST_PARAMETER = "list";
	private static final Logger LOG = LogManager.getLogger(ShoppingCartServiceImpl.class);

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
	private OrderPaymentApiCleanupService orderPaymentApiCleanupService;
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
	public Map<String, String> getCartDescriptors(final String cartGuid) {
		ShoppingCart cartByGuid = findByGuid(cartGuid);
		Map<String, String> cartDescriptorMap = new HashMap<>();

		Store store = cartByGuid.getStore();

		if (store != null) {
			List<ModifierField> modifierFields = store.getModifierFields();
			Map<String, String> cartDataMap = cartByGuid.getModifierFields().getMap();


			modifierFields.forEach(modifierField -> updateCartDescriptorMap(cartDataMap, cartDescriptorMap, modifierField));
		}
		return cartDescriptorMap;
	}

	/**
	 * Prepares cartDescriptorMap for the given modifierField, with modifierFieldCode as key, and
	 *    - If cartDataMap has an entry for this key, use the corresponding value.
	 *    - If cartDataMap does not have corresponding value, but modifierField has a default value then use it.
	 *    - Else set null as value
	 *
	 * @param cartDataMap DataMap for the given cart.
	 * @param cartDescriptorMap cartDescriptorMap to be prepared with obtained key,value entry.
	 * @param modifierField Modifier field for which the value needs to be obtained
	 */
	private void updateCartDescriptorMap(final Map<String, String> cartDataMap, final Map<String, String> cartDescriptorMap,
			final ModifierField modifierField) {
		String cartDataValue = null;
		if (cartDataMap.containsKey(modifierField.getCode())) {
			cartDataValue = cartDataMap.get(modifierField.getCode());
		} else if (StringUtils.isNotBlank(modifierField.getDefaultCartValue())) {
			cartDataValue = modifierField.getDefaultCartValue();
		}

		cartDescriptorMap.put(modifierField.getCode(), cartDataValue);
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

		final ShoppingCart shoppingCart = getPrototypeBean(ContextIdNames.SHOPPING_CART, ShoppingCart.class);
		shoppingCart.setStore(store);
		shoppingCart.setShopper(shopper);
		if (shoppingCartMemento != null) {
			setShoppingCartMemento(shoppingCart, shoppingCartMemento);
		}
		return shoppingCart;
	}

	@Override
	public String findOrCreateDefaultCartGuidByShopper(final Shopper shopper) throws EpServiceException {

		final List<String> cartGuids = getPersistenceEngine().retrieveByNamedQuery("DEFAULT_SHOPPING_CARTGUID_FIND_BY_SHOPPER_UID",
			new Object[]{shopper.getUidPk()}, 0, 1);
		if (cartGuids.isEmpty()) {
			ShoppingCart shoppingCart = findOrCreateDefaultCartByShopper(shopper);

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

	@Override
	public ShoppingCart findOrCreateDefaultCartByShopper(final Shopper shopper) throws EpServiceException {

		final List<ShoppingCartMemento> carts = getPersistenceEngine()
			.withLoadTuners(getLoadTuners())
			.retrieveByNamedQuery(getFindOrCreateByShopperNamedQuery(),
			new Object[]{shopper.getUidPk()}, 0, 1);

		ShoppingCartMemento shoppingCartMemento = Iterables.getFirst(carts, null);

		restoreDependents(shoppingCartMemento);

		ShoppingCart shoppingCart = createShoppingCart(shopper, shoppingCartMemento);
		shoppingCart.setDefault(true);
		initializeDefaultCartModifierFields(shoppingCart);
		return shoppingCart;
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

	private void initializeDefaultCartModifierFields(final ShoppingCart shoppingCart) {
		final Map<String, String> cartModifierFields = new HashMap<>();
		getModifierFieldsWithDefaultValues(shoppingCart)
				.forEach(modifierField -> updateCartDataFieldValueForDefaultCart(shoppingCart, modifierField, cartModifierFields));

		shoppingCart.getModifierFields().putAll(cartModifierFields);
	}

	/**
	 * Given cartData does not have entry for modifierField.getCode() or an entry exists but its value is null, then use
	 * modifierField.getDefaultValue().
	 *
	 * @param shoppingCart shoppingCart for which the cartData is to be set.
	 * @param modifierField modifierField for which the cartData value is to be derived and set.
	 */
	private void updateCartDataFieldValueForDefaultCart(final ShoppingCart shoppingCart, final ModifierField modifierField,
														final Map<String, String> cartModifierFields) {
		String cartDataValue = shoppingCart.getModifierFields().get(modifierField.getCode());
		String finalFieldValue = StringUtils.defaultIfEmpty(cartDataValue, modifierField.getDefaultCartValue());

		cartModifierFields.put(modifierField.getCode(), finalFieldValue);
	}

	@Override
	public ShoppingCart createByShopper(final Shopper shopper) {
		return createShoppingCart(shopper, null);
	}

	@Override
	public List<String> findByCustomerAndStore(final String customerGuid, final String accountSharedId, final String storeCode)
			throws EpServiceException {
		if (accountSharedId == null) {
			return getPersistenceEngine().retrieveByNamedQuery("ACTIVE_SHOPPING_CART_GUID_FIND_BY_CUSTOMER_AND_STORE", customerGuid, storeCode);
		} else {
			return getPersistenceEngine().retrieveByNamedQuery("ACTIVE_SHOPPING_CART_GUID_FIND_BY_CUSTOMER_AND_ACCOUNT_AND_STORE", customerGuid,
					accountSharedId, storeCode);
		}
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
			shoppingCart = getPrototypeBean(ContextIdNames.SHOPPING_CART, ShoppingCart.class);
		} else {
			final ShoppingCartMemento memento = loadMemento(uid);
			if (memento == null) {
				shoppingCart = getPrototypeBean(ContextIdNames.SHOPPING_CART, ShoppingCart.class);
			} else {
				shoppingCart = createShoppingCart(memento);
			}
		}

		return shoppingCart;
	}

	@Override
	public void touch(final String cartGuid) {
		getPersistenceEngine().executeNamedQuery("TOUCH_THE_CART", getTimeService().getCurrentTime(), cartGuid);
	}

	@Override
	public int deleteAllShoppingCartsByShopperUid(final Long shopperUid) {
		if (shopperUid == null) {
			throw new EpServiceException(SHOPPER_UIDS_NULL_ERROR_MESSAGE);
		}

		/*
			As of PERF-252, cascade deletion is set on the db level for all cart-related tables. The benefit of doing that is that no additional
			JPQL DELETE calls are required but the one below.
			It's also very important to understand how JPQL DELETE works: as per documentation, there should be 1:1 between JPQL and native
			DELETE queries.

			However, what is not mentioned in the docs is that if entity manager factory contains one or more lifecycle listeners, the OpenJPA will
			fire multiple SELECTs/UPDATEs and DELETEs for every entity that is related with the one being deleted.

			The batch server can work without LC listeners, thus they are disabled so the max possible performance is achieved for the batch server.
			Conversely, Cortex and similar apps can't work without LC listeners (nor they can be disabled at runtime), thus all DELETE operations
			will suffer (the more complex entity to delete, the more db calls will be made).

			See also the comments in PurgeCartsBatchProcessor.
		 */
		//in case of carts with bundles, it is mandatory to disable parent-child relation before deleting a cart
		getPersistenceEngine().executeNamedQuery("DISABLE_PARENT_CHILD_CART_ITEM_RELATION_BY_SHOPPER_UID", shopperUid);

		return getPersistenceEngine().executeNamedQuery("DELETE_SHOPPING_CARTS_BY_SHOPPER_UID", shopperUid);
	}

	@Override
	public int deleteShoppingCartsByGuid(final List<String> shoppingCartGuids) {
		return getPersistenceEngine().executeNamedQueryWithList("DELETE_SHOPPING_CART_BY_GUIDS", LIST_PARAMETER, shoppingCartGuids);
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
	public Map<String, List<Map<String, String>>> findCartDataForCarts(final List<String> cartGuids) {

		Map<String, List<Map<String, String>>> result = new HashMap<>();
		List<Object[]> data = getPersistenceEngine().retrieveByNamedQueryWithList("FIND_CART_DATA_FOR_CARTS", "list", cartGuids);
		data.forEach(row -> {

			String cartGuid = (String) row[0];
			ModifierFieldsMapWrapper cartModifierFields = (ModifierFieldsMapWrapper) row[1];

			List<Map<String, String>> cartDataList = result.computeIfAbsent(cartGuid, key -> new ArrayList<>());
			cartDataList.add(cartModifierFields.getMap());

		});
		return result;
	}

	@Override
	public void deactivateCart(final ShoppingCart shoppingCart) {
		shoppingCart.deactivateCart();
		getPersistenceEngine().executeNamedQuery("DEACTIVATE_SHOPPING_CART", shoppingCart.getGuid());
	}

	/**
	 * Get list of modifier fields with default cart values.
	 *
	 * @param cart the shopping cart
	 * @return list of modifier fields with default values
	 */
	protected List<ModifierField> getModifierFieldsWithDefaultValues(final ShoppingCart cart) {
		List<ModifierGroup> modifierGroups = cart.getStore().getShoppingCartTypes().stream()
				.flatMap(cartType -> cartType.getModifiers().stream())
				.collect(Collectors.toList());
		return modifierGroups.stream()
				.flatMap(modifierGroup -> modifierGroup.getModifierFields().stream())
				.filter(modifierField -> modifierField.getDefaultCartValue() != null)
				.collect(Collectors.toList());
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

	protected OrderPaymentApiCleanupService getOrderPaymentApiCleanupService() {
		return orderPaymentApiCleanupService;
	}

	public void setOrderPaymentApiCleanupService(final OrderPaymentApiCleanupService orderPaymentApiCleanupService) {
		this.orderPaymentApiCleanupService = orderPaymentApiCleanupService;
	}

	// END - SPRING SETTERS
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
