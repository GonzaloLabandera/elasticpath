/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipping.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ErrorCheckPredicate.createErrorCheckPredicate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.jmock.MockeryFactory;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl.CartOrderRepositoryImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipping.ShippingServiceLevelRepository;

/**
 * Test that {@link ShippingServiceLevelRepositoryImpl} behaves as expected.
 */
public class ShippingServiceLevelRepositoryImplTest {

	private static final String STORE_CODE = "store";
	private static final String ORDER_ID = "orderId";
	private static final String DELIVERY_ID = ShipmentDetailsConstants.SHIPMENT_TYPE;
	private static final String SHIPPING_SERVICE_LEVEL_GUID = "ssl guid";

	@Rule
	public final JUnitRuleMockery context = MockeryFactory.newRuleInstance();

	private final CartOrderRepository mockCartOrderRepository = context.mock(CartOrderRepository.class);

	private final ShoppingCart mockShoppingCart = context.mock(ShoppingCart.class);

	private final ShippingServiceLevel shippingServiceLevel = context.mock(ShippingServiceLevel.class);
	private final Address shippingAddress = context.mock(Address.class);

	private final List<ShippingServiceLevel> shippingServiceLevelList = Collections.singletonList(shippingServiceLevel);
	private final List<String> shippingServiceLevelGuidsList = Collections.singletonList(SHIPPING_SERVICE_LEVEL_GUID);

	private final Map<String, String> shipmentDetailsId = ShipmentDetailsUtil.createShipmentDetailsId(ORDER_ID, DELIVERY_ID);

	private final ShippingServiceLevelRepository repository = new ShippingServiceLevelRepositoryImpl(mockCartOrderRepository);

	/**
	 * Test the behaviour of find shipping service levels for shipment.
	 */
	@Test
	public void testFindShippingServiceLevelsForShipment() {
		allowingShippingServiceListToBeRetrieved();

		repository.findShippingServiceLevelGuidsForShipment(STORE_CODE, shipmentDetailsId)
				.test()
				.assertNoErrors()
				.assertValueSet(shippingServiceLevelGuidsList);
	}

	/**
	 * Test the behavior of find shipping service levels for shipment when cart order not found.
	 */
	@Test
	public void testFindShippingServiceLevelsForShipmentWhenCartOrderNotFound() {
		allowingCartOrderNotFound();

		String errorMsg = String.format(CartOrderRepositoryImpl.ORDER_WITH_GUID_NOT_FOUND, ORDER_ID, STORE_CODE);
		repository.findShippingServiceLevelGuidsForShipment(STORE_CODE, shipmentDetailsId)
				.test()
				.assertError(createErrorCheckPredicate(errorMsg, ResourceStatus.NOT_FOUND));
	}

	/**
	 * Test the behaviour of find by guid.
	 */
	@Test
	public void testFindByGuid() {
		allowingShippingServiceListToBeRetrieved();

		repository.findByGuid(STORE_CODE, shipmentDetailsId, SHIPPING_SERVICE_LEVEL_GUID)
				.test()
				.assertNoErrors()
				.assertValue(shippingServiceLevel);
	}

	/**
	 * Test the behaviour of find by guid when not found.
	 */
	@Test
	public void testFindByGuidWhenNotFound() {
		context.checking(new Expectations() {
			{
				oneOf(shippingServiceLevel).getGuid();
				will(returnValue("OTHER_GUID"));
			}
		});
		allowingShippingServiceListToBeRetrieved();

		repository.findByGuid(STORE_CODE, shipmentDetailsId, SHIPPING_SERVICE_LEVEL_GUID)
				.test()
				.assertError(createErrorCheckPredicate(ShippingServiceLevelRepositoryImpl.SHIPPING_OPTION_NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	/**
	 * Test the behaviour of get selected shipping service level for shipment.
	 */
	@Test
	public void testGetSelectedShippingServiceLevelForShipment() {
		final CartOrder cartOrder = allowingShippingServiceListToBeRetrieved();
		context.checking(new Expectations() {
			{
				allowing(mockShoppingCart).getSelectedShippingServiceLevel();
				will(returnValue(shippingServiceLevel));

				allowing(cartOrder).getShippingServiceLevelGuid();
				will(returnValue(SHIPPING_SERVICE_LEVEL_GUID));

				allowing(shippingServiceLevel).getGuid();
				will(returnValue(SHIPPING_SERVICE_LEVEL_GUID));

			}
		});


		repository.getSelectedShippingOptionIdForShipmentDetails(STORE_CODE, shipmentDetailsId)
				.test()
				.assertNoErrors()
				.assertValue(SHIPPING_SERVICE_LEVEL_GUID);
	}

	/**
	 * Test the behaviour of get selected shipping service level for shipment when cart order not found.
	 */
	@Test
	public void testGetSelectedShippingServiceLevelForShipmentWhenCartOrderNotFound() {
		allowingCartOrderNotFound();

		String errorMsg = String.format(CartOrderRepositoryImpl.ORDER_WITH_GUID_NOT_FOUND, ORDER_ID, STORE_CODE);
		repository.getSelectedShippingOptionIdForShipmentDetails(STORE_CODE, shipmentDetailsId)
				.test()
				.assertError(createErrorCheckPredicate(errorMsg, ResourceStatus.NOT_FOUND));
	}

	/**
	 * Test the behaviour of get selected shipping service level for shipment when cart order has no service level guid.
	 */
	@Test
	public void testGetSelectedShippingServiceLevelForShipmentWhenCartOrderHasNoServiceLevelGuid() {
		final CartOrder cartOrder = allowingShippingServiceListToBeRetrieved();
		context.checking(new Expectations() {
			{
				allowing(cartOrder).getShippingServiceLevelGuid();
				will(returnValue(null));
			}
		});

		repository.getSelectedShippingOptionIdForShipmentDetails(STORE_CODE, shipmentDetailsId)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	/**
	 * Test the behaviour of get selected shipping service level for shipment when shipping service level not found.
	 */
	@Test
	public void testGetSelectedShippingServiceLevelForShipmentWhenShippingServiceLevelNotFound() {
		context.checking(new Expectations() {
			{
				allowing(mockCartOrderRepository).findShippingServiceLevels(STORE_CODE, shippingAddress);
				will(returnValue(Collections.emptyList()));
			}
		});

		final CartOrder cartOrder = allowingShippingServiceListToBeRetrieved();
		context.checking(new Expectations() {
			{
				allowing(cartOrder).getShippingServiceLevelGuid();
				will(returnValue(SHIPPING_SERVICE_LEVEL_GUID));

			}
		});

		String errorMsg = ShippingServiceLevelRepositoryImpl.SHIPPING_SERVICE_LEVEL_SELECTION_OUT_OF_SYNC;
		repository.getSelectedShippingOptionIdForShipmentDetails(STORE_CODE, shipmentDetailsId)
				.test()
				.assertError(createErrorCheckPredicate(errorMsg, ResourceStatus.SERVER_ERROR));
	}

	/**
	 * Test the behaviour of get selected shipping service level for shipment when shipping service level not found.
	 */
	@Test
	public void testGetSelectedShippingServiceLevelForShipmentWhenSelectedLevelsAreOutOfSync() {
		context.checking(new Expectations() {
			{
				allowing(shippingServiceLevel).getGuid();
				will(returnValue("mismatch-shipping-service-level-guid."));
			}
		});

		final CartOrder cartOrder = allowingShippingServiceListToBeRetrieved();
		context.checking(new Expectations() {
			{
				allowing(cartOrder).getShippingServiceLevelGuid();
				will(returnValue(SHIPPING_SERVICE_LEVEL_GUID));
			}
		});

		String errorMsg = ShippingServiceLevelRepositoryImpl.SHIPPING_SERVICE_LEVEL_SELECTION_OUT_OF_SYNC;
		repository.getSelectedShippingOptionIdForShipmentDetails(STORE_CODE, shipmentDetailsId)
				.test()
				.assertError(createErrorCheckPredicate(errorMsg, ResourceStatus.SERVER_ERROR));
	}

	private CartOrder allowingShippingServiceListToBeRetrieved() {
		final CartOrder cartOrder = context.mock(CartOrder.class);

		context.checking(new Expectations() {
			{
				atLeast(1).of(mockCartOrderRepository).findByShipmentDetailsId(STORE_CODE, shipmentDetailsId);
				will(returnValue(Single.just(cartOrder)));

				oneOf(mockCartOrderRepository).getShippingAddress(cartOrder);
				will(returnValue(Single.just(shippingAddress)));

				allowing(mockCartOrderRepository).findShippingServiceLevels(STORE_CODE, shippingAddress);
				will(returnValue(shippingServiceLevelList));

				allowing(shippingServiceLevel).getGuid();
				will(returnValue(SHIPPING_SERVICE_LEVEL_GUID));

			}
		});
		return cartOrder;
	}

	private void allowingCartOrderNotFound() {
		context.checking(new Expectations() {
			{
				oneOf(mockCartOrderRepository).findByShipmentDetailsId(STORE_CODE, shipmentDetailsId);
				will(returnValue(Single.error(ResourceOperationFailure.notFound(
						String.format(CartOrderRepositoryImpl.ORDER_WITH_GUID_NOT_FOUND, ORDER_ID, STORE_CODE)))));
			}
		});
	}

}
