/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl.ShoppingCartResourceConstants.CREATE_CART_NOT_SUPPORTED;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl.ShoppingCartResourceConstants.DEFAULT_CART_NOT_FOUND;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.identity.Subject;

/**
 * B2C strategy for multi-carts.
 */
@Singleton
@Named("b2cMulticartStrategy")
public class B2CMultiCartResolutionStrategyImpl extends AbstractEpMultiCartStrategyImpl {

	@Override
	public boolean isApplicable(final Subject subject) {
		return true;
	}

	@Override
	public Observable<String> findAllCarts(final String customerGuid, final String accountSharedId, final String storeCode, final Subject subject) {

		final String storeCodeUpperCase = storeCode.toUpperCase(Locale.getDefault());
		List<String> listOfCartIds = getShoppingCartService().findByCustomerAndStore(customerGuid, accountSharedId, storeCodeUpperCase);

		return Observable.fromIterable(listOfCartIds);
	}

	@Override
	public Single<ShoppingCart> getShoppingCartSingle(final String cartGuid) {
		return getCustomerSessionRepository().findOrCreateCustomerSession()
				.flatMap(customerSession -> getShoppingCartSingle(cartGuid, customerSession));
	}

	@Override
	public boolean supportsCreate(final Subject subject, final Shopper shopper, final String storeCode) {
		if (!hasMulticartEnabled(storeCode)) {
			return false;
		}
		return shopper.getCustomer().isRegistered();
	}

	@Override
	public Single<String> getDefaultShoppingCartGuid() {
		return getCustomerSessionRepository().findOrCreateCustomerSession()
				.flatMap(this::getDefaultCartGuid);
	}

	@CacheResult(uniqueIdentifier = "B2CDefaultCartGuid")
	private Single<String> getDefaultCartGuid(final CustomerSession customerSession) {
		return getReactiveAdapter().fromServiceAsSingle(()
				-> getShoppingCartService().findDefaultShoppingCartGuidByCustomerSession(customerSession));
	}


	@Override
	public Single<ShoppingCart> getDefaultShoppingCart() {
		return getCustomerSessionRepository().findOrCreateCustomerSession()
				.flatMap(this::getDefaultCart);
	}


	@CacheResult(uniqueIdentifier = "B2CCartForCustomerSession")
	private ShoppingCart getCartForCustomerSession(final CustomerSession customerSession) {
		final ShoppingCart cart = getShoppingCartService().findOrCreateDefaultCartByCustomerSession(customerSession);

		getModifierFieldsWithDefaultValues(cart).forEach(modifierField -> {
			if ((cart.getCartData().get(modifierField.getCode()) == null) || (cart.getCartData().get(modifierField.getCode()).getValue() == null)) {
				cart.setCartDataFieldValue(modifierField.getCode(), modifierField.getDefaultCartValue());
			}
		});

		final ShoppingCart savedCart = getShoppingCartService().saveIfNotPersisted(cart);

		getCartPostProcessor().postProcessCart(savedCart, customerSession.getShopper(), customerSession);
		return savedCart;
	}

	@Override
	@CacheResult(uniqueIdentifier = "B2CDefaultCart")
	public Single<ShoppingCart> getDefaultCart(final CustomerSession customerSession) {
		return getReactiveAdapter().fromServiceAsSingle(() -> getCartForCustomerSession(customerSession), DEFAULT_CART_NOT_FOUND);
	}

	@Override
	public Single<ShoppingCart> createCart(final Map<String, String> descriptors, final String scope) {
		if (!hasMulticartEnabled(scope)) {
			return Single.error(ResourceOperationFailure.stateFailure(CREATE_CART_NOT_SUPPORTED));
		}

		return getCustomerSessionRepository().createCustomerSessionAsSingle()
				.flatMap(customerSession -> createCartInternal(customerSession, descriptors));
	}
}
