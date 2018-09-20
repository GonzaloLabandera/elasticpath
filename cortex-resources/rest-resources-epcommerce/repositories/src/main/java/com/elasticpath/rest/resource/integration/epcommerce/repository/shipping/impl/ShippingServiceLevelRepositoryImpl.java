/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipping.impl;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.annotations.VisibleForTesting;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.base.GloballyIdentifiable;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipping.ShippingServiceLevelRepository;

/**
 * Implementation of ShippingServiceLevelRepository.
 */
@Singleton
@Named("shippingServiceLevelRepository")
public class ShippingServiceLevelRepositoryImpl implements ShippingServiceLevelRepository {

	/**
	 * Error message when shipping service level are not in sync.
	 */
	@VisibleForTesting
	public static final String SHIPPING_SERVICE_LEVEL_SELECTION_OUT_OF_SYNC =
			"Cart order shipping service level and shopping cart selected shipping service level are no longer in sync.";
	/**
	 * Error message when shipping option not found.
	 */
	static final String SHIPPING_OPTION_NOT_FOUND = "Shipping option not found.";

	/**
	 * Error message when shipping options not found.
	 */
	@VisibleForTesting
	public static final String SHIPPING_OPTIONS_NOT_FOUND = "Shipping options not found.";

	private final CartOrderRepository cartOrderRepository;

	/**
	 * Constructor.
	 *
	 * @param cartOrderRepository the cart order repository
	 */
	@Inject
	public ShippingServiceLevelRepositoryImpl(
			@Named("cartOrderRepository") final CartOrderRepository cartOrderRepository) {

		this.cartOrderRepository = cartOrderRepository;
	}

	@Override
	public Observable<String> findShippingServiceLevelGuidsForShipment(final String storeCode, final Map<String, String> shipmentDetailsId) {
		return findShippingAddressByStoreCodeAndShipmentDetails(storeCode, shipmentDetailsId)
				.flatMapObservable(shippingAddress -> getShippingServiceLevels(storeCode, shippingAddress))
				.map(GloballyIdentifiable::getGuid);
	}

	@Override
	public Single<ShippingServiceLevel> findByGuid(final String storeCode, final Map<String, String> shipmentDetailsId,
															final String shippingServiceLevelGuid) {
		return findShippingAddressByStoreCodeAndShipmentDetails(storeCode, shipmentDetailsId)
				.flatMap(shippingAddress -> getShippingServiceLevel(shippingServiceLevelGuid, getShippingServiceLevels(storeCode, shippingAddress)));
	}

	@Override
	public Single<ShippingServiceLevel> getShippingServiceLevel(final String shippingServiceLevelGuid,
																final Observable<ShippingServiceLevel> shippingServiceLevelList) {
		return shippingServiceLevelList
				.filter(shippingServiceLevel -> shippingServiceLevelGuid.equals(shippingServiceLevel.getGuid()))
				.firstOrError()
				.onErrorResumeNext(Single.error(ResourceOperationFailure.notFound(SHIPPING_OPTION_NOT_FOUND)));
	}

	@Override
	public Maybe<String> getSelectedShippingOptionIdForShipmentDetails(final String storeCode, final Map<String, String> shipmentDetailsId) {
		return cartOrderRepository.findByShipmentDetailsId(storeCode, shipmentDetailsId)
				.flatMapMaybe(cartOrder -> cartOrderRepository.getShippingAddress(cartOrder)
						.flatMapMaybe(shippingAddress -> getSelectedShipmentIdFromCartOrder(storeCode, shippingAddress, cartOrder)));
	}

	private Maybe<String> getSelectedShipmentIdFromCartOrder(final String storeCode, final Address shippingAddress, final CartOrder cartOrder) {
		return getSelectedShippingServiceLevelId(cartOrder)
				.flatMap(selectedShippingServiceLevelGuid -> getShippingServiceLevelsFromSelectedId(storeCode, shippingAddress,
						selectedShippingServiceLevelGuid));
	}

	private Maybe<String> getShippingServiceLevelsFromSelectedId(final String storeCode, final Address shippingAddress,
																  final String selectedShippingServiceLevelGuid) {
		return getShippingServiceLevel(selectedShippingServiceLevelGuid, getShippingServiceLevels(storeCode, shippingAddress))
						.map(GloballyIdentifiable::getGuid)
				.onErrorResumeNext(Single.error(ResourceOperationFailure.serverError(SHIPPING_SERVICE_LEVEL_SELECTION_OUT_OF_SYNC)))
				.toMaybe();
	}

	private Maybe<String> getSelectedShippingServiceLevelId(final CartOrder cartOrder) {
		return cartOrder.getShippingServiceLevelGuid() == null ? Maybe.empty() : Maybe.just(cartOrder.getShippingServiceLevelGuid());
	}

	private Observable<ShippingServiceLevel> getShippingServiceLevels(final String storeCode, final Address shippingAddress) {
		return Observable.fromIterable(cartOrderRepository.findShippingServiceLevels(storeCode, shippingAddress))
				.onErrorResumeNext(Observable.error(ResourceOperationFailure.notFound(SHIPPING_OPTIONS_NOT_FOUND)));
	}

	private Single<Address> findShippingAddressByStoreCodeAndShipmentDetails(final String storeCode,
																			 final Map<String, String> shipmentDetailsId) {
		return cartOrderRepository.findByShipmentDetailsId(storeCode, shipmentDetailsId)
				.flatMap(cartOrderRepository::getShippingAddress);
	}
}
