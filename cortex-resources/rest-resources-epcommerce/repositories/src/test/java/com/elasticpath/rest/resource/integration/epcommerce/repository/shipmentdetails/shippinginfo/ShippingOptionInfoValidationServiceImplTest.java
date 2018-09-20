/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.shippinginfo;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
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

import com.elasticpath.domain.shipping.ShipmentType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.ShippingOptionInfoIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsServiceImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipping.ShippingServiceLevelRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Test for {@link ShippingOptionInfoValidationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShippingOptionInfoValidationServiceImplTest {

	private static final String SCOPE = "scope";

	private static final String ORDER_ID = "orderId";

	private static final String SHIPPING_OPTION_ID = "shippingOptionId";

	private final Set<ShipmentType> shipmentTypes = new HashSet<>();

	private final OrderIdentifier orderIdentifier = OrderIdentifier.builder()
			.withOrderId(StringIdentifier.of(ORDER_ID))
			.withScope(StringIdentifier.of(SCOPE))
			.build();

	@Mock
	private ShoppingCart shoppingCart;

	@InjectMocks
	private ShipmentDetailsServiceImpl shipmentDetailsService;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;

	@InjectMocks
	private ShippingOptionInfoValidationServiceImpl shippingOptionInfoValidationService;

	@Mock
	private ShippingServiceLevelRepository shippingServiceLevelRepository;

	@Before
	public void setUp() {
		when(shoppingCartRepository.getDefaultShoppingCart()).thenReturn(Single.just(shoppingCart));
		when(shoppingCart.getShipmentTypes()).thenReturn(shipmentTypes);
		shippingOptionInfoValidationService.setShipmentDetailsService(shipmentDetailsService);
	}

	@Test
	public void verifyNoLinkedMessageWhenOrderNotShippable() {
		shipmentTypes.add(ShipmentType.ELECTRONIC);
		when(shippingServiceLevelRepository.getSelectedShippingOptionIdForShipmentDetails(anyString(), anyMap()))
				.thenReturn(Maybe.just(SHIPPING_OPTION_ID));

		shippingOptionInfoValidationService.validateShippingOptionInfo(orderIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void verifyLinkedMessageCreatedWhenNoSelectedShippingOption() {
		shipmentTypes.add(ShipmentType.PHYSICAL);
		when(shippingServiceLevelRepository.getSelectedShippingOptionIdForShipmentDetails(anyString(), anyMap()))
				.thenReturn(Maybe.empty());

		shippingOptionInfoValidationService.validateShippingOptionInfo(orderIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(this::isLinkedMessageValid);
	}

	private boolean isLinkedMessageValid(final LinkedMessage<ShippingOptionInfoIdentifier> linkedMessage) {
		return linkedMessage.getDebugMessage().equals(ShipmentDetailsConstants.MESSAGE_NEED_SHIPMENT_DETAILS)
				&& linkedMessage.getId().equals(StructuredErrorMessageIdConstants.NEED_SHIPMENT_DETAILS)
				&& linkedMessage.getType().equals(StructuredMessageTypes.NEEDINFO);
	}
}
