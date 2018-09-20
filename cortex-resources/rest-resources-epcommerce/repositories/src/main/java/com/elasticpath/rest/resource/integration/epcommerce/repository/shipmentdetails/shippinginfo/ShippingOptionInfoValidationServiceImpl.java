/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.shippinginfo;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsUtil.createShipmentDetailsId;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.ShippingOptionInfoIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsService;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsServiceImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipping.ShippingServiceLevelRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Validation service that validates shipping option info.
 */
@Component
public class ShippingOptionInfoValidationServiceImpl implements ShippingOptionInfoValidationService {

	private ShippingServiceLevelRepository shippingServiceLevelRepository;
	private ShipmentDetailsService shipmentDetailsService;

	@Override
	public Observable<LinkedMessage<ShippingOptionInfoIdentifier>> validateShippingOptionInfo(final OrderIdentifier orderIdentifier) {
		IdentifierPart<String> scopeId = orderIdentifier.getScope();
		String scope = scopeId.getValue();
		String cartId = orderIdentifier.getOrderId().getValue();
		return shipmentDetailsService.getShipmentDetailsIdForOrder(cartId)
				.flatMapMaybe(shipmentDetailsId ->
						shippingServiceLevelRepository.getSelectedShippingOptionIdForShipmentDetails(scope, shipmentDetailsId))
				.isEmpty()
				.flatMapObservable(empty -> empty ? buildLinkedMessage(scopeId, cartId) : Observable.empty())
				.onErrorResumeNext(handleShippingOptionErrors(scopeId, cartId));
	}

	private Function<Throwable, Observable<LinkedMessage<ShippingOptionInfoIdentifier>>> handleShippingOptionErrors(
			final IdentifierPart<String> scopeId,
			final String cartId) {
		return throwable -> throwable.getLocalizedMessage().equals(ShipmentDetailsServiceImpl.COULD_NOT_FIND_SHIPMENT)
				? Observable.empty() : buildLinkedMessage(scopeId, cartId);
	}

	private Observable<LinkedMessage<ShippingOptionInfoIdentifier>> buildLinkedMessage(final IdentifierPart<String> scopeId, final String cartId) {
		return Observable.just(LinkedMessage.<ShippingOptionInfoIdentifier>builder()
				.withLinkedIdentifier(buildShippingOptionInfoIdentifier(scopeId, cartId))
				.withId(StructuredErrorMessageIdConstants.NEED_SHIPMENT_DETAILS)
				.withDebugMessage(ShipmentDetailsConstants.MESSAGE_NEED_SHIPMENT_DETAILS)
				.withType(StructuredMessageTypes.NEEDINFO)
				.build());
	}

	private ShippingOptionInfoIdentifier buildShippingOptionInfoIdentifier(final IdentifierPart<String> scopeId, final String cartId) {
		return ShippingOptionInfoIdentifier.builder()
				.withShipmentDetailsId(CompositeIdentifier.of(createShipmentDetailsId(cartId, ShipmentDetailsConstants.SHIPMENT_TYPE)))
				.withScope(scopeId)
				.build();
	}

	@Reference
	public void setShippingServiceLevelRepository(final ShippingServiceLevelRepository shippingServiceLevelRepository) {
		this.shippingServiceLevelRepository = shippingServiceLevelRepository;
	}

	@Reference
	public void setShipmentDetailsService(final ShipmentDetailsService shipmentDetailsService) {
		this.shipmentDetailsService = shipmentDetailsService;
	}
}
