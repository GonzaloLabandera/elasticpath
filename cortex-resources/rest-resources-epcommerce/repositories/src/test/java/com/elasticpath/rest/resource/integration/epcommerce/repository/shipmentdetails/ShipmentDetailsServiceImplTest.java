/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails;

import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ErrorCheckPredicate.createErrorCheckPredicate;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.shipping.ShipmentType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl.ShoppingCartRepositoryImpl;

/**
 * Test for {@link ShipmentDetailsServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShipmentDetailsServiceImplTest {

	private static final String CART_ID = "cartId";

	@Mock
	private ShoppingCart shoppingCart;

	@InjectMocks
	private ShipmentDetailsServiceImpl shipmentDetailsService;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;

	@Test
	public void verifyGetShipmentDetailsIdsForOrderReturnsNotFoundWhenShoppingCartIsNotFound() {
		when(shoppingCartRepository.getDefaultShoppingCart())
				.thenReturn(Single.error(ResourceOperationFailure.notFound(ShoppingCartRepositoryImpl.DEFAULT_CART_NOT_FOUND)));

		shipmentDetailsService.getShipmentDetailsIdForOrder(CART_ID)
				.test()
				.assertError(createErrorCheckPredicate(ShoppingCartRepositoryImpl.DEFAULT_CART_NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyGetShipmentDetailsIdsForOrderReturnsNotFoundWhenOrderIsNotShippable() {
		when(shoppingCartRepository.getDefaultShoppingCart()).thenReturn(Single.just(shoppingCart));
		when(shoppingCart.getShipmentTypes()).thenReturn(ImmutableSet.of(ShipmentType.ELECTRONIC));

		shipmentDetailsService.getShipmentDetailsIdForOrder(CART_ID)
				.test()
				.assertError(createErrorCheckPredicate(ShipmentDetailsServiceImpl.COULD_NOT_FIND_SHIPMENT, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyGetShipmentDetailsIdsForOrderReturnsMap() {
		when(shoppingCartRepository.getDefaultShoppingCart()).thenReturn(Single.just(shoppingCart));
		when(shoppingCart.getShipmentTypes()).thenReturn(ImmutableSet.of(ShipmentType.PHYSICAL));

		shipmentDetailsService.getShipmentDetailsIdForOrder(CART_ID)
				.test()
				.assertNoErrors()
				.assertValue(shipmentDetailsId -> shipmentDetailsId.get(ShipmentDetailsConstants.ORDER_ID).equals(CART_ID)
						&& shipmentDetailsId.get(ShipmentDetailsConstants.DELIVERY_ID).equals(ShipmentDetailsConstants.SHIPMENT_TYPE));
	}
}
