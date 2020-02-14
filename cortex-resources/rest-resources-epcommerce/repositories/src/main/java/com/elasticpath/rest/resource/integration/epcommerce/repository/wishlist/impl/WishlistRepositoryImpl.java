/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist.WishlistIdentifierUtil.buildWishlistLineItemIdentifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.base.GloballyIdentifiable;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.ShopperReference;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheRemove;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.wishlists.WishlistLineItemIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist.WishlistRepository;
import com.elasticpath.service.shoppingcart.WishListService;
import com.elasticpath.service.shoppingcart.impl.AddToWishlistResult;

/**
 * The facade for shopping cart related operations.
 */
@Singleton
@Named("wishlistRepository")
public class WishlistRepositoryImpl implements WishlistRepository {
	private static final String WISHLIST_WAS_NOT_FOUND = "Requested wishlist was not found";
	private static final String ITEM_NOT_FOUND = "Item not found in wishlist";
	private static final Logger LOG = LoggerFactory.getLogger(WishlistRepositoryImpl.class);

	private final WishListService wishListService;
	private final CustomerSessionRepository customerSessionRepository;
	private final ItemRepository itemRepository;
	private final ProductSkuRepository productSkuRepository;
	private final StoreRepository storeRepository;
	private final ResourceOperationContext resourceOperationContext;
	private final ReactiveAdapter reactiveAdapter;

	/**
	 * Constructor.
	 *
	 * @param wishListService           the wishlistService
	 * @param customerSessionRepository the customer session repo
	 * @param itemRepository            the item repo
	 * @param productSkuRepository      the product sku repo
	 * @param storeRepository           the store repo
	 * @param resourceOperationContext  the resource operation context
	 * @param reactiveAdapter           the reactive adapter
	 */
	@Inject
	WishlistRepositoryImpl(
			@Named("wishListService") final WishListService wishListService,
			@Named("customerSessionRepository") final CustomerSessionRepository customerSessionRepository,
			@Named("itemRepository") final ItemRepository itemRepository,
			@Named("productSkuRepository") final ProductSkuRepository productSkuRepository,
			@Named("storeRepository") final StoreRepository storeRepository,
			@Named("resourceOperationContext") final ResourceOperationContext resourceOperationContext,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter) {

		this.wishListService = wishListService;
		this.customerSessionRepository = customerSessionRepository;
		this.itemRepository = itemRepository;
		this.storeRepository = storeRepository;
		this.resourceOperationContext = resourceOperationContext;
		this.reactiveAdapter = reactiveAdapter;
		this.productSkuRepository = productSkuRepository;
	}

	@Override
	@CacheResult
	public Observable<String> getWishlistIds(final String customerGuid, final String storeCode) {
		LOG.trace("finding by customer");
		return customerSessionRepository.findCustomerSessionByGuidAsSingle(customerGuid)
				.map(ShopperReference::getShopper)
				.flatMapObservable(shopper -> getWishlistInternal(shopper)
						.map(GloballyIdentifiable::getGuid)
						.toObservable());
	}

	@Override
	@CacheResult(uniqueIdentifier = "cachingGetWishlist")
	public Single<WishList> getWishlist(final String guid) {
		return reactiveAdapter.fromServiceAsSingle(() -> wishListService.findByGuid(guid), WISHLIST_WAS_NOT_FOUND);
	}

	@Override
	public Single<AddToWishlistResult> addItemToWishlist(final String wishlistId, final String storecode, final String sku) {
		return getWishlist(wishlistId)
				.flatMap(wishList -> storeRepository.findStoreAsSingle(storecode)
						.flatMap(store -> addItemToWishlist(sku, wishList, store)));
	}

	/**
	 * Add item to wishlist.
	 *
	 * @param sku      the item sku
	 * @param wishList the wishList
	 * @param store    the store
	 * @return the result
	 */
	protected Single<AddToWishlistResult> addItemToWishlist(final String sku, final WishList wishList, final Store store) {
		return reactiveAdapter.fromServiceAsSingle(() -> wishListService.addProductSku(wishList, store, sku));
	}

	@Override
	@CacheRemove(typesToInvalidate = {WishList.class})
	public Completable removeItemFromWishlist(final String wishlistId, final String lineItemGuid) {
		return getWishlist(wishlistId)
				.flatMapCompletable(wishList -> removeItemFromWishlist(wishList, lineItemGuid))
				.onErrorResumeNext(throwable -> Completable.error(ResourceOperationFailure.
						serverError("Error saving the changes to wishlist with id {}"
								+ wishlistId + ", Could not remove item with id" + lineItemGuid)));
	}

	/**
	 * Remove line item from wishlist.
	 *
	 * @param wishList     the wishList
	 * @param lineItemGuid the lineItemGuid
	 * @return the Completable
	 */
	protected Completable removeItemFromWishlist(final WishList wishList, final String lineItemGuid) {
		wishList.removeItem(lineItemGuid);
		return saveChanges(wishList).toCompletable();
	}

	@Override
	@CacheRemove(typesToInvalidate = {WishList.class})
	public Completable removeAllItemsFromWishlist(final String wishlistGuid) {
		return getWishlist(wishlistGuid)
				.flatMapCompletable(this::removeAllItemsFromWishlist)
				.onErrorResumeNext(throwable -> Completable.error(ResourceOperationFailure
						.serverError("Error saving the changes to wishlist with id {}" + wishlistGuid + ", Could not remove all items.")));
	}

	/**
	 * Remove all items from wishlist.
	 *
	 * @param wishList wishList
	 * @return the Completable
	 */
	protected Completable removeAllItemsFromWishlist(final WishList wishList) {
		wishList.setAllItems(new ArrayList<>());
		return saveChanges(wishList).toCompletable();
	}

	@Override
	@CacheResult
	public Maybe<WishList> findWishlistsContainingItem(final Map<String, String> itemIdMap) {
		return customerSessionRepository.findOrCreateCustomerSession()
				.flatMap(customerSession -> getWishlistInternal(customerSession.getShopper()))
				.flatMapMaybe(filterWishlist(itemIdMap))
				.filter(Objects::nonNull);
	}

	/**
	 * Filter Wishlist containing given item.
	 *
	 * @param itemIdMap the itemIdMap
	 * @return function to perform the operation
	 */
	protected Function<WishList, Maybe<WishList>> filterWishlist(final Map<String, String> itemIdMap) {
		return wishList -> itemRepository.getSkuForItemId(itemIdMap)
				.flatMapMaybe(productSku -> getWishListContainingItem(wishList, productSku.getGuid()));
	}

	@Override
	@CacheResult(uniqueIdentifier = "cachingGetDefaultWishlist")
	public Single<String> getDefaultWishlistId(final String scope) {
		//Only one wishlist supported!!
		return getWishlistIds(resourceOperationContext.getUserIdentifier(), scope)
				.firstElement()
				.toSingle();
	}

	@Override
	public Single<ProductSku> getProductSku(final String wishlistId, final String lineItemGuid) {
		return getWishlist(wishlistId)
				.flatMap(wishList -> getProductSku(wishList, lineItemGuid));
	}

	@Override
	@CacheResult(uniqueIdentifier = "cachingGetProductSku")
	public Single<ProductSku> getProductSku(final WishList wishlist, final String lineItemGuid) {
		return getShoppingItem(wishlist, lineItemGuid)
				.flatMap(shoppingItem -> productSkuRepository.getProductSkuWithAttributesByGuid(shoppingItem.getSkuGuid()))
				.filter(WishlistRepositoryImpl::isProductDiscoverable)
				.toSingle()
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(ITEM_NOT_FOUND)));
	}

	@Override
	@CacheResult(uniqueIdentifier = "cachingGetShoppingItem")
	public Single<ShoppingItem> getShoppingItem(final WishList wishlist, final String lineItemGuid) {
		return Observable.fromIterable(wishlist.getAllItems())
				.filter(shoppingItem -> shoppingItem.getGuid().equals(lineItemGuid))
				.switchIfEmpty(Observable.error(ResourceOperationFailure.notFound(ITEM_NOT_FOUND)))
				.firstElement().toSingle();
	}

	@Override
	public Single<SubmitResult<WishlistLineItemIdentifier>> buildSubmitResult(final String scope, final String wishlistId,
																			  final AddToWishlistResult addToWishlistResult) {
		ShoppingItem shoppingItem = addToWishlistResult.getShoppingItem();
		return Single.just(buildWishlistLineItemIdentifier(scope, wishlistId, shoppingItem.getGuid()))
				.map(wishlistLineItemIdentifier -> SubmitResult.<WishlistLineItemIdentifier>builder()
						.withIdentifier(wishlistLineItemIdentifier)
						.withStatus(addToWishlistResult.isNewlyCreated() ? SubmitStatus.CREATED : SubmitStatus.EXISTING)
						.build());
	}

	/**
	 * Get the wishlist for a shopper.
	 *
	 * @param shopper the shopper
	 * @return the wishlist
	 */
	protected Single<WishList> getWishlistInternal(final Shopper shopper) {
		LOG.trace("Wishlist by shopper: {}", shopper.getGuid());
		return reactiveAdapter.fromServiceAsSingle(() -> wishListService.findOrCreateWishListByShopper(shopper), WISHLIST_WAS_NOT_FOUND)
				.flatMap(this::saveWishlistIfNeeded);
	}

	/**
	 * Save the wishlist if needed.
	 *
	 * @param wishList the wishList
	 * @return the wishList
	 */
	protected Single<WishList> saveWishlistIfNeeded(final WishList wishList) {
		if (!wishList.isPersisted()) {
			return saveChanges(wishList);
		}
		return Single.just(wishList);
	}

	/**
	 * Save the changes to the wishlist.
	 *
	 * @param wishList the wishlist
	 * @return the updated wishlist
	 */
	protected Single<WishList> saveChanges(final WishList wishList) {
		return reactiveAdapter.fromServiceAsSingle(() -> wishListService.save(wishList));
	}

	/**
	 * Utility method to check to see if the given sku is contained in the given wishlist.
	 *
	 * @param wishList the wishlist
	 * @param skuGuid  the guid of the SKU
	 * @return the wishList if the item is in the wishlist, nothing otherwise
	 */
	protected Maybe<WishList> getWishListContainingItem(final WishList wishList, final String skuGuid) {
		return Observable.fromIterable(wishList.getAllItems())
				.filter(shoppingItem -> skuGuid.equals(shoppingItem.getSkuGuid()))
				.flatMapMaybe(shoppingItem -> Maybe.just(wishList))
				.switchIfEmpty(Observable.empty())
				.singleElement();
	}

	/**
	 * Checks whether a product is discoverable.
	 *
	 * @param productSku the product sku
	 * @return true if the product is discoverable
	 */
	static boolean isProductDiscoverable(final ProductSku productSku) {
		Date now = new Date();
		return productSku != null
				&& productSku.isWithinDateRange(now)
				&& !productSku.getProduct().isHidden()
				&& productSku.getProduct().isWithinDateRange(now);
	}

}
