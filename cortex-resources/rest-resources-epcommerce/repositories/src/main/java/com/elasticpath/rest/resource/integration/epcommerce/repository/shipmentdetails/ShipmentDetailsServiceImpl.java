/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsUtil.createShipmentDetailsId;

import java.util.Map;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;

/**
 * Service that retrieves shipment detail ids for an order.
 */
@Component
public class ShipmentDetailsServiceImpl implements ShipmentDetailsService {

	/**
	 * Error message when shipment can't be found.
	 */
	public static final String COULD_NOT_FIND_SHIPMENT = "Could not find shipment";
	private ShoppingCartRepository shoppingCartRepository;

	@Override
	public Single<Map<String, String>> getShipmentDetailsIdForOrder(final String cartId) {
		return validateOrderDeliveryIsShippable()
				.flatMap(isShippable -> isShippable ? Single.just(createShipmentDetailsId(cartId, ShipmentDetailsConstants.SHIPMENT_TYPE))
						: Single.error(ResourceOperationFailure.notFound(COULD_NOT_FIND_SHIPMENT)));
	}

	private Single<Boolean> validateOrderDeliveryIsShippable() {
		return shoppingCartRepository.getDefaultShoppingCart()
				.map(ShipmentDetailsUtil::containsPhysicalShipment);
	}

	@Reference
	public void setShoppingCartRepository(final ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}
}

