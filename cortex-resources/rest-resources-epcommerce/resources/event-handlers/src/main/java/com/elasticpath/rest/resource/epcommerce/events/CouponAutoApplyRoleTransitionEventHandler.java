/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.epcommerce.events;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.relos.rs.events.RoleTransitionEvent;
import com.elasticpath.rest.relos.rs.events.ScopedEventEntityHandler;
import com.elasticpath.rest.resource.integration.epcommerce.common.authentication.AuthenticationConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;

/**
 * Automatically applies coupons to new customer.
 */
@Component(property = {
		Constants.SERVICE_RANKING + ":Integer=50",
		"eventType=" + RoleTransitionEvent.EVENT_TYPE })
public class CouponAutoApplyRoleTransitionEventHandler implements ScopedEventEntityHandler<RoleTransitionEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(CouponAutoApplyRoleTransitionEventHandler.class);

	@Reference
	private StoreRepository storeRepository;
	@Reference
	private CartOrderRepository cartOrderRepository;
	@Reference
	private ShoppingCartRepository shoppingCartRepository;


	@Override
	public void handleEvent(final String scope, final RoleTransitionEvent event) {
		if (isTransitionToRegisteredRole(event)) {
			String customerGuid = event.getNewUserGuid();
			filterAndAutoApplyCoupons(scope, customerGuid)
					.subscribe(
							() -> {
								//do nothing
							},
							throwable -> LOG.error("Error auto apply coupons to cart order: {}", throwable.getMessage())
					);
		}
	}

	private boolean isTransitionToRegisteredRole(final RoleTransitionEvent event) {
		return AuthenticationConstants.REGISTERED_ROLE.equals(event.getNewRole());
	}

	private Completable filterAndAutoApplyCoupons(final String storeCode, final String customerGuid) {
		/*
		 * Thread Local still doesn't have any resource operations present at this point in time.
		 * Hence it is not possible to get current Session for the user.
		 * Do not use {@link ShoppingCartRepository#getDefaultShoppingCart()} because of this reason.
		 * You must pass customer guid explicitly but not reach Resource Operation Context for it.
		 */
		return shoppingCartRepository.getDefaultShoppingCartForCustomer(customerGuid, storeCode)
				.flatMapCompletable(shoppingCart -> getCartOrder(shoppingCart)
						.flatMapCompletable(cartOrder -> {
							String customerEmailAddress = shoppingCart.getShopper().getCustomer().getEmail();
							return storeRepository.findStoreAsSingle(storeCode)
									.flatMapCompletable(store -> filterAndAutoApplyCoupons(cartOrder, customerEmailAddress, store)
											.flatMapCompletable(isCartOrderUpdated -> isCartOrderUpdated
													? cartOrderRepository.saveCartOrder(cartOrder).ignoreElement()
													: Completable.complete()));
						}));
	}

	private Single<CartOrder> getCartOrder(final ShoppingCart shoppingCart) {
		return cartOrderRepository.findByCartGuid(shoppingCart.getGuid());
	}

	private Single<Boolean> filterAndAutoApplyCoupons(final CartOrder cartOrder, final String customerEmailAddress, final Store store) {
		return cartOrderRepository.filterAndAutoApplyCoupons(cartOrder, store, customerEmailAddress);
	}

}
