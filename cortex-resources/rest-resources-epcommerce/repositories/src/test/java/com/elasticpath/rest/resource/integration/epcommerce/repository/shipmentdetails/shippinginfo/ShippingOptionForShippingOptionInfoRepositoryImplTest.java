/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.shippinginfo;

import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsUtil.createShipmentDetailsId;

import java.util.Map;

import io.reactivex.Maybe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.rest.definition.shipmentdetails.ShippingOptionIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.ShippingOptionInfoIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipping.ShippingServiceLevelRepository;

/**
 * Test for {@link ShippingOptionForShippingOptionInfoRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShippingOptionForShippingOptionInfoRepositoryImplTest {

	private static final String SCOPE = "scope";
	private static final String ORDER_ID = "orderId";
	private static final String SELECTED_ID = "selectedId";
	private final Map<String, String> shipmentDetailsId = createShipmentDetailsId(ORDER_ID, ShipmentDetailsConstants.SHIPMENT_TYPE);

	private final ShippingOptionInfoIdentifier shippingOptionInfoIdentifier = ShippingOptionInfoIdentifier.builder()
			.withScope(StringIdentifier.of(SCOPE))
			.withShipmentDetailsId(CompositeIdentifier.of(shipmentDetailsId))
			.build();

	@InjectMocks
	private ShippingOptionForShippingOptionInfoRepositoryImpl<ShippingOptionInfoIdentifier, ShippingOptionIdentifier> repository;

	@Mock
	private ShippingServiceLevelRepository shippingServiceLevelRepository;

	@Test
	public void verifyGetElementsReturnsSelectedShippingOptionWhenItExists() {
		when(shippingServiceLevelRepository.getSelectedShippingOptionIdForShipmentDetails(SCOPE, shipmentDetailsId))
				.thenReturn(Maybe.just(SELECTED_ID));

		repository.getElements(shippingOptionInfoIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(identifier -> identifier.getShippingOptionId().getValue().equals(SELECTED_ID));
	}

	@Test
	public void verifyGetElementsReturnsNotFoundWhenSelectedShippingOptionDoesNotExist() {
		when(shippingServiceLevelRepository.getSelectedShippingOptionIdForShipmentDetails(SCOPE, shipmentDetailsId))
				.thenReturn(Maybe.empty());

		repository.getElements(shippingOptionInfoIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}
}
