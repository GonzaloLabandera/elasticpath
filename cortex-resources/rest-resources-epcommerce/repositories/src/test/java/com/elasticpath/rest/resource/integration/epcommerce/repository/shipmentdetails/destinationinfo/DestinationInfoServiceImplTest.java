/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.destinationinfo;

import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ErrorCheckPredicate.createErrorCheckPredicate;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.shipping.ShipmentType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl.ShoppingCartRepositoryImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsServiceImpl;

/**
 * Test for {@link DestinationInfoServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DestinationInfoServiceImplTest {

	private static final String ORDER_ID = "orderId";
	private static final String SCOPE = "scope";
	private static final String SELECTED_ID = "addressId";

	private final Set<ShipmentType> electronic = ImmutableSet.of(ShipmentType.ELECTRONIC);
	private final Set<ShipmentType> physical = ImmutableSet.of(ShipmentType.PHYSICAL);

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private CartOrder cartOrder;

	@Mock
	private Address address;

	@InjectMocks
	private DestinationInfoServiceImpl destinationInfoService;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;

	private void mockShoppingCart(final Set<ShipmentType> shipmentTypes) {
		when(shoppingCartRepository.getDefaultShoppingCart()).thenReturn(Single.just(shoppingCart));
		when(shoppingCart.getShipmentTypes()).thenReturn(shipmentTypes);
	}

	private void mockCartOrder() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, ORDER_ID)).thenReturn(Single.just(cartOrder));
		when(cartOrderRepository.getShippingAddressAsMaybe(cartOrder)).thenReturn(Maybe.just(address));
		when(address.getGuid()).thenReturn(SELECTED_ID);
	}

	@Test
	public void verifyValidateOrderIsShippableReturnsFalseIfOrderHasNoShippableItems() {
		mockShoppingCart(electronic);

		destinationInfoService.validateOrderIsShippable(SCOPE, ORDER_ID)
				.test()
				.assertNoErrors()
				.assertValue(false);
	}

	@Test
	public void verifyValidateOrderIsShippableReturnsTrueIfOrderHasShippableItems() {
		mockShoppingCart(physical);

		destinationInfoService.validateOrderIsShippable(SCOPE, ORDER_ID)
				.test()
				.assertNoErrors()
				.assertValue(true);
	}

	@Test
	public void verifyGetSelectedAddressGuidIfShippableReturnsNotFoundWhenDefaultCartNotFound() {
		when(shoppingCartRepository.getDefaultShoppingCart())
				.thenReturn(Single.error(ResourceOperationFailure.notFound(ShoppingCartRepositoryImpl.DEFAULT_CART_NOT_FOUND)));
		when(shoppingCart.getShipmentTypes()).thenReturn(physical);
		mockCartOrder();
		when(address.getGuid()).thenReturn(SELECTED_ID);

		destinationInfoService.getSelectedAddressGuidIfShippable(SCOPE, ORDER_ID)
				.test()
				.assertError(createErrorCheckPredicate(ShoppingCartRepositoryImpl.DEFAULT_CART_NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyGetSelectedAddressGuidIfShippableReturnsNotFoundWhenOrderNotShippable() {
		mockShoppingCart(electronic);
		mockCartOrder();
		when(address.getGuid()).thenReturn(SELECTED_ID);

		destinationInfoService.getSelectedAddressGuidIfShippable(SCOPE, ORDER_ID)
				.test()
				.assertError(createErrorCheckPredicate(ShipmentDetailsServiceImpl.COULD_NOT_FIND_SHIPMENT, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyGetSelectedAddressGuidIfShippableReturnsEmptyWhenThereIsNoSelectedAddress() {
		when(shoppingCartRepository.getDefaultShoppingCart()).thenReturn(Single.just(shoppingCart));
		when(shoppingCart.getShipmentTypes()).thenReturn(physical);
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, ORDER_ID)).thenReturn(Single.just(cartOrder));
		when(cartOrderRepository.getShippingAddressAsMaybe(cartOrder)).thenReturn(Maybe.empty());

		destinationInfoService.getSelectedAddressGuidIfShippable(SCOPE, ORDER_ID)
				.test()
				.assertNoValues()
				.assertNoErrors();
	}

	@Test
	public void verifyGetSelectedAddressGuidIfShippableReturnsAddressWhenThereIsASelectedAddress() {
		when(shoppingCartRepository.getDefaultShoppingCart()).thenReturn(Single.just(shoppingCart));
		when(shoppingCart.getShipmentTypes()).thenReturn(physical);
		mockCartOrder();

		destinationInfoService.getSelectedAddressGuidIfShippable(SCOPE, ORDER_ID)
				.test()
				.assertNoErrors()
				.assertValue(SELECTED_ID);
	}
}
