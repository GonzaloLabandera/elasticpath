/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.annotations.VisibleForTesting;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheRemove;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.cartorder.CartOrderCouponService;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.cartorder.CartOrderShippingService;
import com.elasticpath.service.rules.CartOrderCouponAutoApplier;

/**
 * The facade for {@link CartOrder} related operations.
 */
@Singleton
@Named("cartOrderRepository")
@SuppressWarnings("PMD.TooManyMethods")
public class CartOrderRepositoryImpl implements CartOrderRepository {

	/**
	 * Error message when cart order not found.
	 */
	public static final String ORDER_WITH_GUID_NOT_FOUND = "No cart order with GUID %s was found in store %s.";
	private static final String ORDER_WITH_CART_GUID_NOT_FOUND = "No cart order with cart GUID %s was found.";

	/**
	 * Error message when cart orders not found for customer.
	 */
	@VisibleForTesting
	public static final String NO_CART_ORDERS_FOR_CUSTOMER = "No cart orders for customer with GUID %s were found in store %s.";

	private final CartOrderService coreCartOrderService;
	private final CartOrderCouponService coreCartOrderCouponService;
	private final ShoppingCartRepository shoppingCartRepository;
	private final CartOrderShippingService coreCartOrderShippingService;
	private final CartOrderCouponAutoApplier cartOrderCouponAutoApplier;
	private final ReactiveAdapter reactiveAdapter;

	/**
	 * Constructor.
	 *
	 * @param coreCartOrderService         the core cart order service
	 * @param shoppingCartRepository       the shopping cart repository
	 * @param coreCartOrderShippingService the core cart order shipping service
	 * @param coreCartOrderCouponService   the core cart order coupon service
	 * @param cartOrderCouponAutoApplier   cart order coupon auto applier
	 * @param reactiveAdapter              reactiveAdapter
	 */
	@Inject
	public CartOrderRepositoryImpl(
			@Named("cartOrderService") final CartOrderService coreCartOrderService,
			@Named("shoppingCartRepository") final ShoppingCartRepository shoppingCartRepository,
			@Named("cartOrderShippingService") final CartOrderShippingService coreCartOrderShippingService,
			@Named("cartOrderCouponService") final CartOrderCouponService coreCartOrderCouponService,
			@Named("cartOrderCouponAutoApplier") final CartOrderCouponAutoApplier cartOrderCouponAutoApplier,
			@Named("reactiveAdapter") final ReactiveAdapter reactiveAdapter) {

		this.coreCartOrderService = coreCartOrderService;
		this.shoppingCartRepository = shoppingCartRepository;
		this.coreCartOrderShippingService = coreCartOrderShippingService;
		this.coreCartOrderCouponService = coreCartOrderCouponService;
		this.cartOrderCouponAutoApplier = cartOrderCouponAutoApplier;
		this.reactiveAdapter = reactiveAdapter;
	}

	@Override
	public Single<CartOrder> findByGuid(final String storeCode, final String cartOrderGuid) {
		return reactiveAdapter.fromServiceAsSingle(() -> findByStoreCodeAndGuid(storeCode, cartOrderGuid),
				String.format(ORDER_WITH_GUID_NOT_FOUND, cartOrderGuid, storeCode));
	}

	@CacheResult
	private CartOrder findByStoreCodeAndGuid(final String storeCode, final String cartOrderGuid) {
		return coreCartOrderService.findByStoreCodeAndGuid(storeCode, cartOrderGuid);
	}

	@Override
	@CacheResult
	public Single<CartOrder> findByCartGuid(final String cartGuid) {
		return reactiveAdapter.fromServiceAsSingle(() -> coreCartOrderService.findByShoppingCartGuid(cartGuid),
				String.format(ORDER_WITH_CART_GUID_NOT_FOUND, cartGuid));
	}

	@Override
	@CacheResult(uniqueIdentifier = "findByShipmentDetailsId")
	public Single<CartOrder> findByShipmentDetailsId(final String storeCode, final Map<String, String> shipmentDetailsId) {
		String cartId = shipmentDetailsId.get(ShipmentDetailsConstants.ORDER_ID);
		return reactiveAdapter.fromServiceAsSingle(() -> coreCartOrderService
				.findByStoreCodeAndGuid(storeCode, cartId), String.format(ORDER_WITH_GUID_NOT_FOUND, cartId, storeCode));
	}

	@Override
	public Observable<String> findCartOrderGuidsByCustomer(final String storeCode, final String customerGuid) {
		return reactiveAdapter.fromService(() -> findCartOrderGuidsByCustomerGuid(storeCode, customerGuid),
				String.format(NO_CART_ORDERS_FOR_CUSTOMER, customerGuid, storeCode))
				.flatMap(Observable::fromIterable);
	}

	@CacheResult
	private List<String> findCartOrderGuidsByCustomerGuid(final String storeCode, final String customerGuid) {
		return coreCartOrderService.findCartOrderGuidsByCustomerGuid(storeCode, customerGuid);
	}

	@Override
	@CacheResult(uniqueIdentifier = "billingAddress")
	public Maybe<Address> getBillingAddress(final CartOrder cartOrder) {
		return reactiveAdapter.fromServiceAsMaybe(() -> coreCartOrderService.getBillingAddress(cartOrder), Maybe.empty());
	}

	@CacheResult
	private Address getShippingAddressForCartOrder(final CartOrder cartOrder) {
		return coreCartOrderService.getShippingAddress(cartOrder);
	}

	@Override
	public Maybe<Address> getShippingAddress(final CartOrder cartOrder) {
		return reactiveAdapter.fromServiceAsMaybe(() -> getShippingAddressForCartOrder(cartOrder), Maybe.empty());
	}

	@Override
	@CacheRemove(typesToInvalidate = {CartOrder.class, Address.class, ShoppingCart.class,
			ShoppingCartPricingSnapshot.class, ShoppingCartTaxSnapshot.class})
	public Single<CartOrder> saveCartOrder(final CartOrder cartOrder) {
		return reactiveAdapter.fromServiceAsSingle(() -> coreCartOrderService.saveOrUpdate(cartOrder), "Unable to save cart order.")
				.onErrorResumeNext(Single.error(ResourceOperationFailure.serverError("Unable to save cart order.")));
	}

	@Override
	public Single<CartOrder> getCartOrder(final String storeCode, final String guid, final FindCartOrder findBy) {

		Single<CartOrder> cartOrder;
		switch (findBy) {
			case BY_CART_GUID:
				cartOrder = findByCartGuid(guid);
				break;
			case BY_ORDER_GUID:
				cartOrder = findByGuid(storeCode, guid);
				break;
			default:
				return Single.error(ResourceOperationFailure.serverError("Invalid FindCartOrder criteria: " + findBy));
		}

		return cartOrder;
	}

	@Override
	@CacheResult(uniqueIdentifier = "enrichedCartByGuid")
	public Single<ShoppingCart> getEnrichedShoppingCart(final String storeCode, final String guid, final FindCartOrder findBy) {
		return getCartOrder(storeCode, guid, findBy)
				.flatMap(cartOrder -> getEnrichedShoppingCart(storeCode, cartOrder));
	}

	@Override
	@CacheResult(uniqueIdentifier = "enrichedCartForShipments")
	public Single<ShoppingCart> getEnrichedShoppingCartForShipments(final String storeCode, final Map<String, String> shipmentDetailsId) {
		return findByShipmentDetailsId(storeCode, shipmentDetailsId)
				.flatMap(cartOrder -> getEnrichedShoppingCart(storeCode, cartOrder));
	}

	@Override
	@CacheResult(uniqueIdentifier = "enrichedCart")
	public Single<ShoppingCart> getEnrichedShoppingCart(final String storeCode, final CartOrder cartOrder) {
		return shoppingCartRepository.getShoppingCart(cartOrder.getShoppingCartGuid())
				// update the coupon codes on the shopping cart.
				.map(shoppingCart -> coreCartOrderCouponService.populateCouponCodesOnShoppingCart(shoppingCart, cartOrder))
				// Update shipping information on the cart as shipping info is transient.
				.map(enrichedShoppingCart -> coreCartOrderShippingService.populateAddressAndShippingFields(enrichedShoppingCart, cartOrder));
	}

	@Override
	@CacheRemove(typesToInvalidate = {Address.class})
	public Single<Boolean> updateShippingAddressOnCartOrder(final String shippingAddressGuid, final String cartOrderGuid,
															final String storeCode) {
		return findByGuid(storeCode, cartOrderGuid)
				.flatMap(cartOrder -> updateCartOrderShippingAddress(shippingAddressGuid, cartOrder));
	}


	private Single<Boolean> updateCartOrderShippingAddress(final String shippingAddressGuid, final CartOrder cartOrder) {
		return  shoppingCartRepository.getShoppingCart(cartOrder.getShoppingCartGuid())
				.map(cart -> coreCartOrderShippingService.updateCartOrderShippingAddress(shippingAddressGuid, cart, cartOrder))
				.flatMap(isAddressUpdated -> saveCartOrderIfAddressUpdated(cartOrder, isAddressUpdated));
	}

	private Single<Boolean> saveCartOrderIfAddressUpdated(final CartOrder cartOrder, final Boolean isAddressUpdated) {
		if (isAddressUpdated) {
			return saveCartOrder(cartOrder)
					.map(savedCartOrder -> true);
		}
		return Single.just(false);
	}

	@Override
	@CacheRemove(typesToInvalidate = {CartOrder.class, ShoppingCartPricingSnapshot.class, ShoppingCartTaxSnapshot.class})
	public Single<Boolean> filterAndAutoApplyCoupons(final CartOrder cartOrder, final Store store, final String customerEmailAddress) {
		return reactiveAdapter.fromServiceAsSingle(() ->
				cartOrderCouponAutoApplier.filterAndAutoApplyCoupons(cartOrder, store, customerEmailAddress))
				.onErrorResumeNext(Single.error(ResourceOperationFailure.serverError("Server error when auto applying coupons to cart order")));
	}

	@Override
	@CacheResult
	public String getShoppingCartGuid(final String storeCode, final String cartOrderGuid) {
		return coreCartOrderService.getShoppingCartGuid(storeCode, cartOrderGuid);
	}
}
