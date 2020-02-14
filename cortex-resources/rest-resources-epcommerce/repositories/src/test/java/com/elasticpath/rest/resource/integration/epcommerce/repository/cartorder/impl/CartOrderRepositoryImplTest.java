/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ErrorCheckPredicate.createErrorCheckPredicate;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl.CartOrderRepositoryImpl.ORDER_WITH_GUID_NOT_FOUND;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsUtil.createShipmentDetailsId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import javax.persistence.PersistenceException;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.cartorder.impl.CartOrderImpl;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.cartorder.CartOrderCouponService;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.cartorder.CartOrderShippingService;
import com.elasticpath.service.rules.CartOrderCouponAutoApplier;

/**
 * Test that {@link CartOrderRepositoryImpl} behaves as expected.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class CartOrderRepositoryImplTest {

	private static final String STORE_CODE = "store";
	private static final String CART_ORDER_GUID = "cart order guid";
	private static final String INVALID_CART_ORDER_ID = "invalidCartId";
	private static final String CART_GUID = "cart guid";
	private static final String CUSTOMER_GUID = "customer guid";
	private static final String SHIPPING_ADDRESS_GUID = "SHIPPING_ADDRESS_GUID";
	private static final String ORDER_WITH_CART_GUID_NOT_FOUND = "No cart order with cart GUID %s was found.";
	private static final String DELIVERY_ID = "delivery id";

	@Mock
	private CartOrderService cartOrderService;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;

	@Mock
	private CartOrderShippingService cartOrderShippingService;

	@Mock
	private CartOrderCouponService cartOrderCouponService;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	private CartOrderRepositoryImpl repository;

	@Mock
	private CartOrder cartOrder;

	@Mock
	private CartOrderCouponAutoApplier cartOrderCouponAutoApplier;

	@Mock
	private ShoppingCart shoppingCart;

	@Before
	public void setUp() {
		allowingValidCartOrder();

		repository = new CartOrderRepositoryImpl(cartOrderService, shoppingCartRepository, cartOrderShippingService, cartOrderCouponService,
				cartOrderCouponAutoApplier, reactiveAdapter);

		when(shoppingCartRepository.getShoppingCart(CART_GUID)).thenReturn(Single.just(shoppingCart));
	}

	/**
	 * Test the behaviour of find by guid.
	 */
	@Test
	public void testFindByStoreCodeAndGuid() {
		allowingCartOrderStoreCodeToBe(STORE_CODE);

		repository.findByGuid(STORE_CODE, CART_ORDER_GUID)
				.test()
				.assertNoErrors()
				.assertValue(cartOrder);
	}

	/**
	 * Test the behaviour of find by guid not found.
	 */
	@Test
	public void testFindByStoreCodeAndGuidNotFound() {
		String errorMsg = String.format("No cart order with GUID %s was found in store %s.", CART_ORDER_GUID, STORE_CODE);
		when(cartOrderService.findByStoreCodeAndGuid(STORE_CODE, CART_ORDER_GUID)).thenReturn(null);

		repository.findByGuid(STORE_CODE, CART_ORDER_GUID)
				.test()
				.assertError(ResourceOperationFailure.notFound(errorMsg));
	}

	/**
	 * Test the behaviour of find by cart guid.
	 */
	@Test
	public void testFindByCartGuid() {

		when(cartOrderService.findByShoppingCartGuid(CART_GUID)).thenReturn(cartOrder);

		repository.findByCartGuid(CART_GUID)
				.test()
				.assertValue(cartOrder);
	}

	/**
	 * Test the behaviour of find by cart guid when not found.
	 */
	@Test
	public void testFindByCartGuidWhenNotFound() {

		when(cartOrderService.findByShoppingCartGuid(CART_GUID)).thenReturn(null);

		repository.findByCartGuid(CART_GUID)
				.test()
				.assertError(ResourceOperationFailure.notFound(String.format(ORDER_WITH_CART_GUID_NOT_FOUND, CART_GUID)));
	}

	/**
	 * Test the behaviour of find by shipment details id.
	 */
	@Test
	public void testFindByShipmentDetailsId() {

		when(cartOrderService.findByStoreCodeAndGuid(STORE_CODE, CART_ORDER_GUID)).thenReturn(cartOrder);
		allowingCartOrderStoreCodeToBe(STORE_CODE);

		repository.findByShipmentDetailsId(STORE_CODE, createShipmentDetailsId(CART_ORDER_GUID, DELIVERY_ID))
				.test()
				.assertNoErrors()
				.assertValue(cartOrder);
	}

	/**
	 * Test the behaviour of find by shipment details id with invalid id.
	 */
	@Test
	public void testFindByShipmentDetailsIdWithInvalidId() {
		when(cartOrderService.findByStoreCodeAndGuid(STORE_CODE, INVALID_CART_ORDER_ID)).thenReturn(null);

		String errorMsg = String.format(ORDER_WITH_GUID_NOT_FOUND, INVALID_CART_ORDER_ID, STORE_CODE);
		repository.findByShipmentDetailsId(STORE_CODE, createShipmentDetailsId(INVALID_CART_ORDER_ID, DELIVERY_ID))
				.test()
				.assertError(createErrorCheckPredicate(errorMsg, ResourceStatus.NOT_FOUND));
	}

	/**
	 * Test the behaviour of find cart order guids by customer.
	 */
	@Test
	public void testFindCartOrderGuidsByCustomer() {
		List<String> listOfCartOrderGuids = Collections.singletonList(CART_ORDER_GUID);
		when(cartOrderService.findCartOrderGuidsByCustomerGuid(STORE_CODE, CUSTOMER_GUID)).thenReturn(listOfCartOrderGuids);

		repository.findCartOrderGuidsByCustomer(STORE_CODE, CUSTOMER_GUID)
				.test()
				.assertNoErrors()
				.assertValueSequence(listOfCartOrderGuids);
	}

	/**
	 * Test the behaviour of find cart order guids by customer when none found.
	 */
	@Test
	public void testFindCartOrderGuidsByCustomerWhenNoneFound() {
		String errorMsg = String.format("No cart orders for customer with GUID %s were found in store %s.", CUSTOMER_GUID, STORE_CODE);
		when(cartOrderService.findCartOrderGuidsByCustomerGuid(STORE_CODE, CUSTOMER_GUID)).thenReturn(null);

		repository.findCartOrderGuidsByCustomer(STORE_CODE, CUSTOMER_GUID)
				.test()
				.assertError(ResourceOperationFailure.notFound(errorMsg));
	}

	/**
	 * Test the behaviour of get billing address.
	 */
	@Test
	public void testGetBillingAddress() {
		final Address address = mock(Address.class);
		when(cartOrderService.getBillingAddress(cartOrder)).thenReturn(address);

		repository.getBillingAddress(cartOrder)
				.test()
				.assertNoErrors()
				.assertValue(address);
	}

	/**
	 * Test the behaviour of get billing address when none found.
	 */
	@Test
	public void testGetBillingAddressWhenNoneFound() {
		when(cartOrderService.getBillingAddress(cartOrder)).thenReturn(null);

		repository.getBillingAddress(cartOrder)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	/**
	 * Test the behaviour of save cart order.
	 */
	@Test
	public void testSaveCartOrder() {
		final CartOrder savedCartOrder = mock(CartOrder.class, "saved");
		when(cartOrderService.saveOrUpdate(cartOrder)).thenReturn(savedCartOrder);

		repository.saveCartOrder(cartOrder)
				.test()
				.assertNoErrors()
				.assertValue(savedCartOrder);
	}

	/**
	 * Test the behaviour of save cart order with exception.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testSaveCartOrderWithException() {
		when(cartOrderService.saveOrUpdate(cartOrder)).thenThrow(PersistenceException.class);

		repository.saveCartOrder(cartOrder)
				.test()
				.assertError(ResourceOperationFailure.serverError("Unable to save cart order."));
	}

	/**
	 * Test the behaviour of get enriched shopping cart.
	 */
	@Test
	public void testGetEnrichedShoppingCart() {
		final ShoppingCart shoppingCart = mock(ShoppingCart.class);
		final ShoppingCart enrichedShoppingCart = mock(ShoppingCart.class, "enriched");
		when(cartOrderService.findByStoreCodeAndGuid(STORE_CODE, CART_ORDER_GUID)).thenReturn(cartOrder);
		allowingCartOrderStoreCodeToBe(STORE_CODE);
		when(shoppingCartRepository.getShoppingCart(CART_GUID)).thenReturn(Single.just(shoppingCart));
		when(cartOrderCouponService.populateCouponCodesOnShoppingCart(shoppingCart, cartOrder)).thenReturn(enrichedShoppingCart);
		when(cartOrderShippingService.populateAddressAndShippingFields(enrichedShoppingCart, cartOrder)).thenReturn(enrichedShoppingCart);

		repository.getEnrichedShoppingCart(STORE_CODE, CART_ORDER_GUID, CartOrderRepository.FindCartOrder.BY_ORDER_GUID)
				.test()
				.assertNoErrors()
				.assertValue(enrichedShoppingCart);
	}

	/**
	 * Test the behaviour of get enriched shopping cart when order not found.
	 */
	@Test
	public void testGetEnrichedShoppingCartWhenOrderNotFound() {
		when(cartOrderService.findByStoreCodeAndGuid(STORE_CODE, CART_ORDER_GUID)).thenReturn(null);

		repository.getEnrichedShoppingCart(STORE_CODE, CART_ORDER_GUID, CartOrderRepository.FindCartOrder.BY_ORDER_GUID)
				.test()
				.assertError(ResourceOperationFailure.notFound(String.format(ORDER_WITH_GUID_NOT_FOUND, CART_ORDER_GUID, STORE_CODE)))
				.assertNoValues();
	}

	/**
	 * Test the behaviour of get enriched shopping cart when shopping cart not found.
	 */
	@Test
	public void testGetEnrichedShoppingCartWhenShoppingCartNotFound() {
		when(cartOrderService.findByStoreCodeAndGuid(STORE_CODE, CART_ORDER_GUID)).thenReturn(cartOrder);
		allowingCartOrderStoreCodeToBe(STORE_CODE);
		when(shoppingCartRepository.getShoppingCart(CART_GUID)).thenReturn(Single.error(ResourceOperationFailure.notFound("not found")));
		repository.getEnrichedShoppingCart(STORE_CODE, CART_ORDER_GUID, CartOrderRepository.FindCartOrder.BY_ORDER_GUID)
				.test()
				.assertError(ResourceOperationFailure.notFound("not found"))
				.assertNoValues();
	}

	@Test
	public void testCartIsUpdatedWhenAddressIsUpdated() {

		when(cartOrderService.findByStoreCodeAndGuid(STORE_CODE, CART_ORDER_GUID)).thenReturn(cartOrder);
		allowingCartOrderStoreCodeToBe(STORE_CODE);
		when(cartOrderShippingService.updateCartOrderShippingAddress(SHIPPING_ADDRESS_GUID, shoppingCart, cartOrder)).thenReturn(true);

		repository.updateShippingAddressOnCartOrder(SHIPPING_ADDRESS_GUID, CART_ORDER_GUID, STORE_CODE).ignoreElement()
				.test();

		verify(cartOrderService, times(1)).saveOrUpdate(cartOrder);
	}

	@Test
	public void testCartIsNotUpdatedWhenAddressIsNotUpdated() {

		when(cartOrderService.findByStoreCodeAndGuid(STORE_CODE, CART_ORDER_GUID)).thenReturn(cartOrder);
		allowingCartOrderStoreCodeToBe(STORE_CODE);
		when(cartOrderShippingService.updateCartOrderShippingAddress(SHIPPING_ADDRESS_GUID, shoppingCart, cartOrder)).thenReturn(false);

		repository.updateShippingAddressOnCartOrder(SHIPPING_ADDRESS_GUID, CART_ORDER_GUID, STORE_CODE).ignoreElement()
				.test();

		verify(cartOrderService, times(0)).saveOrUpdate(any(CartOrder.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateShippingAddressReturnsServerErrorWhenFailsToSaveAsCompletable() {

		when(cartOrderService.findByStoreCodeAndGuid(STORE_CODE, CART_ORDER_GUID)).thenReturn(cartOrder);
		allowingCartOrderStoreCodeToBe(STORE_CODE);

		when(cartOrderShippingService.updateCartOrderShippingAddress(SHIPPING_ADDRESS_GUID, shoppingCart, cartOrder)).thenReturn(true);
		when(cartOrderService.saveOrUpdate(cartOrder)).thenThrow(EpPersistenceException.class);

		repository.updateShippingAddressOnCartOrder(SHIPPING_ADDRESS_GUID, CART_ORDER_GUID, STORE_CODE).ignoreElement()
				.test()
				.assertError(ResourceOperationFailure.serverError("Unable to save cart order."));
	}

	@Test
	public void testFilterAndAutoApplyCoupons() {
		CartOrder cartOrder = new CartOrderImpl();
		Store store = new StoreImpl();
		String customerEmail = "customerEmail";

		when(cartOrderCouponAutoApplier.filterAndAutoApplyCoupons(any(CartOrder.class), any(Store.class), anyString())).thenReturn(true);

		repository.filterAndAutoApplyCoupons(cartOrder, store, customerEmail)
				.test()
				.assertNoErrors()
				.assertValue(true);

		verify(cartOrderCouponAutoApplier).filterAndAutoApplyCoupons(any(CartOrder.class), any(Store.class), anyString());
	}

	private void allowingValidCartOrder() {
		when(cartOrder.getShoppingCartGuid()).thenReturn(CART_GUID);
	}

	private void allowingCartOrderStoreCodeToBe(final String storeCode) {
		when(cartOrderService.findByStoreCodeAndGuid(storeCode, CART_ORDER_GUID)).thenReturn(cartOrder);
	}

}
