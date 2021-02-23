/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.common.dto.sellingchannel.ShoppingItemDtoFactory;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.rest.cache.CacheRemove;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategyHolder;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.sellingchannel.director.CartDirectorService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.impl.AddToWishlistResult;

/**
 * The facade for shopping cart related operations.
 */
@Singleton
@Named("shoppingCartRepository")
public class ShoppingCartRepositoryImpl implements ShoppingCartRepository {

	@Inject
	private ShoppingCartService shoppingCartService;
	@Inject
	private CartDirectorService cartDirectorService;
	@Inject
	private CustomerSessionRepository customerSessionRepository;
	@Inject
	private ShoppingItemDtoFactory shoppingItemDtoFactory;
	@Inject
	private ReactiveAdapter reactiveAdapter;
	@Inject
	private ProductSkuRepository productSkuRepository;


	@Inject
	private MultiCartResolutionStrategyHolder cartResolutionStrategiesHolder;
	@Inject
	private ResourceOperationContext resourceOperationContext;


	@Override
	@CacheResult(uniqueIdentifier = "getDefaultShoppingCartGuid")
	public Single<String> getDefaultShoppingCartGuid() {
		return getStrategy().getDefaultShoppingCartGuid();
	}

	@Override
	@CacheResult(uniqueIdentifier = "getDefaultShoppingCart")
	public Single<ShoppingCart> getDefaultShoppingCart() {
		return getStrategy().getDefaultShoppingCart();
	}

	@Override
	public Map<String, CartData> getCartDescriptors(final String cartGuid) {
		return shoppingCartService.getCartDescriptors(cartGuid);
	}

	@Override
	public Single<ShoppingCart> getShoppingCartForCustomer(final String customerGuid, final String storeCode) {
		return customerSessionRepository.findCustomerSessionByGuidAndStoreCodeAsSingle(customerGuid, storeCode)
				.flatMap(this.getStrategy()::getDefaultCart);
	}

	@Override
	@CacheResult(uniqueIdentifier = "getShoppingCartByGuid")
	public Single<ShoppingCart> getShoppingCart(final String cartGuid) {
		return getStrategy().getShoppingCartSingle(cartGuid);
	}


	@Override
	@CacheResult(uniqueIdentifier = "verifyShoppingCartExistsForStore")
	public Single<Boolean> verifyShoppingCartExistsForStore(final String cartGuid, final String storeCode) {
		return reactiveAdapter.fromServiceAsSingle(() -> shoppingCartService.shoppingCartExistsForStore(cartGuid, storeCode));
	}

	@CacheResult(uniqueIdentifier = "getStoreForCartGuid")
	private Single<String> getStoreForCartGuid(final String cartGuid) {
		return reactiveAdapter.fromServiceAsSingle(()
				-> shoppingCartService.findStoreCodeByCartGuid(cartGuid), "STORE NOT FOUND");
	}

	@Override
	@CacheResult(uniqueIdentifier = "findAllCarts")
	public Observable<String> findAllCarts(final String customerGuid, final String accountSharedId, final String storeCode) {
		MultiCartResolutionStrategy strategy = getStrategy();
		return strategy.findAllCarts(customerGuid, accountSharedId, storeCode, resourceOperationContext.getSubject());

	}

	private MultiCartResolutionStrategy getStrategy() {
		MultiCartResolutionStrategy strategy = null;
		for (MultiCartResolutionStrategy cartResolutionStrategy : this.cartResolutionStrategiesHolder.getStrategies()) {
			if (cartResolutionStrategy.isApplicable(resourceOperationContext.getSubject())) {
				strategy = cartResolutionStrategy;
			}
		}
		assert strategy != null;
		return strategy;
	}

	@Override
	public Single<String> findStoreForCartGuid(final String cartGuid) {
		return getStoreForCartGuid(cartGuid);
	}

	@Override
	public Completable removeCart(final String shoppingCartGuid) {
		return reactiveAdapter.fromServiceAsCompletable(() ->
				shoppingCartService.deleteShoppingCartsByGuid(Collections.singletonList(shoppingCartGuid)));
	}

	@Override
	public Single<ShoppingCart> createCart(final Map<String, String> descriptors, final String scope) {
		return getStrategy().createCart(descriptors, scope);

	}

	@Override
	public boolean canCreateCart(final String storeCode) {
		return getStrategy().hasMulticartEnabled(storeCode);
	}


	@Override
	@CacheRemove(typesToInvalidate = {ShoppingCart.class, ShoppingCartPricingSnapshot.class, ShoppingCartTaxSnapshot.class})
	public Single<ShoppingItem> addItemToCart(final ShoppingCart cart, final String skuCode, final int quantity, final Map<String, String> fields) {
		ShoppingItemDto shoppingItemDto = getShoppingItemDto(skuCode, quantity, fields);

		return reactiveAdapter.fromServiceAsSingle(() -> cartDirectorService.addItemToCart(cart, shoppingItemDto));
	}

	@Override
	public ShoppingItemDto getShoppingItemDto(final String skuCode, final int quantity, final Map<String, String> fields) {
		ShoppingItemDto shoppingItemDto = shoppingItemDtoFactory.createDto(skuCode, quantity);
		shoppingItemDto.setItemFields(fields);
		return shoppingItemDto;
	}

	@Override
	public Single<ShoppingCart> addItemsToCart(final ShoppingCart cart, final List<ShoppingItemDto> dtoList) {
		return reactiveAdapter.fromServiceAsSingle(() -> cartDirectorService.addItemsToCart(cart, dtoList));
	}

	@Override
	@CacheRemove(typesToInvalidate = {ShoppingCart.class, WishList.class, ShoppingCartPricingSnapshot.class, ShoppingCartTaxSnapshot.class})
	public Single<ShoppingItem> moveItemToCart(final ShoppingCart cart, final String wishlistLineItemGuid, final String skuCode,
											   final int quantity, final Map<String, String> fields) {
		ShoppingItemDto dto = getShoppingItemDto(skuCode, quantity, fields);
		return reactiveAdapter.fromServiceAsSingle(() -> cartDirectorService.moveItemFromWishListToCart(cart, dto, wishlistLineItemGuid));
	}

	@Override
	public Single<AddToWishlistResult> moveItemToWishlist(final ShoppingCart cart, final String cartLineItemGuid) {
		return reactiveAdapter.fromServiceAsSingle(() -> cartDirectorService.moveItemFromCartToWishList(cart, cartLineItemGuid));
	}

	@Override
	@CacheResult
	public Single<ProductSku> getProductSku(final String cartId, final String lineItemId) {
		return getShoppingCart(cartId)
				.flatMap(cart -> getShoppingItem(lineItemId, cart))
				.flatMap(shoppingItem -> productSkuRepository.getProductSkuWithAttributesByGuid(shoppingItem.getSkuGuid()));
	}

	@Override
	@CacheResult
	public Single<ShoppingItem> getShoppingItem(final String lineItemId, final ShoppingCart cart) {
		return reactiveAdapter.fromNullableAsSingle(()
				-> cart.getCartItemByGuid(lineItemId), String.format(ShoppingCartResourceConstants.LINEITEM_WAS_NOT_FOUND, lineItemId));
	}

	@Override
	@CacheRemove(typesToInvalidate = {ShoppingCart.class, ShoppingCartPricingSnapshot.class, ShoppingCartTaxSnapshot.class})
	public Completable updateCartItem(final ShoppingCart cart, final ShoppingItem shoppingItem, final String skuCode, final int quantity) {
		Map<String, String> itemFields = Optional.ofNullable(shoppingItem.getFields())
				.orElse(Collections.emptyMap());

		ShoppingItemDto shoppingItemDto = getShoppingItemDto(skuCode, quantity, itemFields);

		return reactiveAdapter.fromServiceAsCompletable(() -> cartDirectorService.updateCartItem(cart, shoppingItem.getUidPk(),
				shoppingItemDto));
	}

	@Override
	@CacheRemove(typesToInvalidate = {ShoppingCart.class, ShoppingCartPricingSnapshot.class, ShoppingCartTaxSnapshot.class})
	public Completable removeItemFromCart(final ShoppingCart cart, final String shoppingItemGuid) {
		//LOG.warn("remove Item from Cart");
		return reactiveAdapter.fromServiceAsCompletable(() -> cartDirectorService.removeItemsFromCart(cart, shoppingItemGuid));
	}

	@Override
	@CacheRemove(typesToInvalidate = {ShoppingCart.class, ShoppingCartPricingSnapshot.class, ShoppingCartTaxSnapshot.class})
	public Completable removeAllItemsFromDefaultCart() {
		return getDefaultShoppingCart()
				.flatMapCompletable(cart -> reactiveAdapter.fromServiceAsCompletable(() -> cartDirectorService.clearItems(cart)));
	}

	@Override
	@CacheRemove(typesToInvalidate = {ShoppingCart.class, ShoppingCartPricingSnapshot.class, ShoppingCartTaxSnapshot.class})
	public Completable removeAllItemsFromCart(final ShoppingCart cart) {
		return reactiveAdapter.fromServiceAsCompletable(() -> cartDirectorService.clearItems(cart));
	}

	/**
	 * Used for testing.
	 *
	 * @param reactiveAdapter the reactive adapter.
	 */
	protected void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

}
