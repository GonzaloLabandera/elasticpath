/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.epcommerce.events;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.relos.rs.events.RoleTransitionEvent;
import com.elasticpath.rest.resource.integration.epcommerce.common.authentication.AuthenticationConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;

@RunWith(MockitoJUnitRunner.class)
public class CouponAutoApplyRoleTransitionEventHandlerTest {
	private static final String EMAIL = "EMAIL";

	private static final String SHOPPING_CART_GUID = "SHOPPING_CART_GUID";

	private static final String NEW_USER_GUID = "NEW_USER_GUID";

	private static final String STORE_CODE = "STORE_CODE";

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private CouponAutoApplyRoleTransitionEventHandler couponAutoApplyRoleTransitionEventHandler;

	@Mock
	private RoleTransitionEvent mockEvent;

	@Mock
	private CartOrder mockCartOrder;

	@Mock
	private Store mockStore;

	@Before
	public void setUp() {
		when(mockEvent.getNewRole()).thenReturn(AuthenticationConstants.REGISTERED_ROLE);
		when(mockEvent.getNewUserGuid()).thenReturn(NEW_USER_GUID);

		Customer mockCustomer = mock(Customer.class);
		when(mockCustomer.getEmail()).thenReturn(EMAIL);

		ShoppingCart mockShoppingCart = mock(ShoppingCart.class, Answers.RETURNS_DEEP_STUBS.get());
		when(shoppingCartRepository.getDefaultShoppingCartForCustomer(NEW_USER_GUID, STORE_CODE))
				.thenReturn(Single.just(mockShoppingCart));
		when(mockShoppingCart.getShopper().getCustomer()).thenReturn(mockCustomer);
		when(mockShoppingCart.getGuid()).thenReturn(SHOPPING_CART_GUID);

		when(storeRepository.findStoreAsSingle(STORE_CODE)).thenReturn(Single.just(mockStore));

		when(cartOrderRepository.saveCartOrder(mockCartOrder)).thenReturn(Single.just(mockCartOrder));
	}

	@Test
	public void testCartOrderIsSavedWhenCouponAutoApplierUpdatesCartOrder() throws Exception {
		allowingRepositoryToFindCartOrder(true);
		when(cartOrderRepository.filterAndAutoApplyCoupons(mockCartOrder, mockStore, EMAIL)).thenReturn(Single.just(true));

		couponAutoApplyRoleTransitionEventHandler.handleEvent(STORE_CODE, mockEvent);

		verifyMocks(atLeastOnce());
		verify(storeRepository).findStoreAsSingle(STORE_CODE);
		verify(cartOrderRepository).filterAndAutoApplyCoupons(any(CartOrder.class), any(Store.class), eq(EMAIL));
		verify(cartOrderRepository).saveCartOrder(mockCartOrder);
	}


	@Test
	public void testCartOrderIsNotSavedWhenNoCouponsAreAutoAppliedToCartOrder() throws Exception {
		allowingRepositoryToFindCartOrder(true);
		when(cartOrderRepository.filterAndAutoApplyCoupons(mockCartOrder, mockStore, EMAIL)).thenReturn(Single.just(false));

		couponAutoApplyRoleTransitionEventHandler.handleEvent(STORE_CODE, mockEvent);

		verifyMocks(atLeastOnce());
		verify(storeRepository).findStoreAsSingle(STORE_CODE);
		verify(cartOrderRepository).filterAndAutoApplyCoupons(any(CartOrder.class), any(Store.class), eq(EMAIL));
		verify(cartOrderRepository, never()).saveCartOrder(mockCartOrder);
	}

	@Test
	public void testCartOrderNotSavedWhenCartOrderNotFound() throws Exception {
		allowingRepositoryToFindCartOrder(false);

		couponAutoApplyRoleTransitionEventHandler.handleEvent(STORE_CODE, mockEvent);

		verifyMocks(atLeastOnce());
		verify(storeRepository, never()).findStoreAsSingle(STORE_CODE);
		verify(cartOrderRepository, never()).filterAndAutoApplyCoupons(any(CartOrder.class), any(Store.class), eq(EMAIL));
		verify(cartOrderRepository, never()).saveCartOrder(mockCartOrder);
	}

	private void allowingRepositoryToFindCartOrder(final boolean findCartOrder) {
		if (findCartOrder) {
			when(cartOrderRepository.findByCartGuid(SHOPPING_CART_GUID)).thenReturn(Single.just(mockCartOrder));
		} else {
			when(cartOrderRepository.findByCartGuid(SHOPPING_CART_GUID)).thenReturn(Single.error(ResourceOperationFailure.notFound()));
		}
	}

	@Test
	public void testEventHandledForEmptyToRegisteredAuth() {
		lenient().when(mockEvent.getOldRole()).thenReturn("");
		lenient().when(mockEvent.getNewRole()).thenReturn(AuthenticationConstants.REGISTERED_ROLE);

		allowingRepositoryToFindCartOrder(false);
		couponAutoApplyRoleTransitionEventHandler.handleEvent(STORE_CODE, mockEvent);

		verifyMocks(atLeastOnce());
	}

	@Test
	public void testEventHandledForPublicToRegisteredAuth() {
		lenient().when(mockEvent.getOldRole()).thenReturn(AuthenticationConstants.PUBLIC_ROLENAME);
		lenient().when(mockEvent.getNewRole()).thenReturn(AuthenticationConstants.REGISTERED_ROLE);

		allowingRepositoryToFindCartOrder(false);
		couponAutoApplyRoleTransitionEventHandler.handleEvent(STORE_CODE, mockEvent);

		verifyMocks(atLeastOnce());
	}

	@Test
	public void testEventNotHandledForPublicAuth() {
		lenient().when(mockEvent.getOldRole()).thenReturn("");
		lenient().when(mockEvent.getNewRole()).thenReturn(AuthenticationConstants.PUBLIC_ROLENAME);

		couponAutoApplyRoleTransitionEventHandler.handleEvent(STORE_CODE, mockEvent);

		verifyMocks(never());
	}

	private void verifyMocks(final VerificationMode mode) {
		verify(shoppingCartRepository, mode).getDefaultShoppingCartForCustomer(NEW_USER_GUID, STORE_CODE);
		verify(cartOrderRepository, mode).findByCartGuid(SHOPPING_CART_GUID);
	}
}
