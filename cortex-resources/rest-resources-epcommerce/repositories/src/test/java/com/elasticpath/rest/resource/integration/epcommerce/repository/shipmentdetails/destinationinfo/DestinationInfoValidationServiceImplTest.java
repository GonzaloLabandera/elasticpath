/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.destinationinfo;

import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.Maybe;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.shipping.ShipmentType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.DestinationInfoIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsConstants;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Test for {@link DestinationInfoValidationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DestinationInfoValidationServiceImplTest {

	private static final String SCOPE = "scope";

	private static final String ORDER_ID = "orderId";

	private static final String ADDRESS_ID = "addressId";

	private final Set<ShipmentType> shipmentTypes = new HashSet<>();

	private final OrderIdentifier orderIdentifier = OrderIdentifier.builder()
			.withOrderId(StringIdentifier.of(ORDER_ID))
			.withScope(StringIdentifier.of(SCOPE))
			.build();

	@Mock
	private CartOrder cartOrder;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private Address address;

	@InjectMocks
	private DestinationInfoServiceImpl destinationInfoService;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;

	private final DestinationInfoValidationServiceImpl destinationInfoValidationService = new DestinationInfoValidationServiceImpl();

	@Before
	public void setUp() {
		destinationInfoValidationService.setDestinationInfoService(destinationInfoService);
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, ORDER_ID)).thenReturn(Single.just(cartOrder));
		when(shoppingCartRepository.getDefaultShoppingCart()).thenReturn(Single.just(shoppingCart));
		when(shoppingCart.getShipmentTypes()).thenReturn(shipmentTypes);
		when(address.getGuid()).thenReturn(ADDRESS_ID);
	}

	@Test
	public void verifyNoLinkedMessageWhenOrderIsNotShippable() {
		shipmentTypes.add(ShipmentType.ELECTRONIC);
		when(cartOrderRepository.getShippingAddressAsMaybe(cartOrder)).thenReturn(Maybe.just(address));

		destinationInfoValidationService.validateDestinationInfo(orderIdentifier)
				.test()
				.assertNoValues()
				.assertNoErrors();
	}

	@Test
	public void verifyLinkedMessageCreatedWhenNoShippingAddressIsFound() {
		shipmentTypes.add(ShipmentType.PHYSICAL);
		when(cartOrderRepository.getShippingAddressAsMaybe(cartOrder))
				.thenReturn(Maybe.empty());

		destinationInfoValidationService.validateDestinationInfo(orderIdentifier)
				.test()
				.assertValue(this::isLinkedMessageValid)
				.assertNoErrors();
	}

	private boolean isLinkedMessageValid(final LinkedMessage<DestinationInfoIdentifier> linkedMessage) {
		return linkedMessage.getDebugMessage().equals(ShipmentDetailsConstants.MESSAGE_NEED_SHIPMENT_DETAILS)
				&& linkedMessage.getId().equals(StructuredErrorMessageIdConstants.NEED_SHIPMENT_DETAILS)
				&& linkedMessage.getType().equals(StructuredMessageTypes.NEEDINFO);
	}
}
