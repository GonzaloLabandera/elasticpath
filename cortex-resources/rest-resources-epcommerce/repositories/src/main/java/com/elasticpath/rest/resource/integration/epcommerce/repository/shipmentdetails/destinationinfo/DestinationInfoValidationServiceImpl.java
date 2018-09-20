/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.destinationinfo;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsUtil.createShipmentDetailsId;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.shipmentdetails.DestinationInfoIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsConstants;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Service that creates an advisor when destination info is needed.
 */
@Component
public class DestinationInfoValidationServiceImpl implements DestinationInfoValidationService {

	private DestinationInfoService destinationInfoService;

	@Override
	public Observable<LinkedMessage<DestinationInfoIdentifier>> validateDestinationInfo(final OrderIdentifier orderIdentifier) {
		IdentifierPart<String> scopeId = orderIdentifier.getScope();
		String scope = scopeId.getValue();
		String orderId = orderIdentifier.getOrderId().getValue();
		return destinationInfoService.getSelectedAddressGuidIfShippable(scope, orderId)
				.isEmpty()
				.flatMapObservable(empty -> empty ? buildLinkedMessage(scopeId, orderId) : Observable.empty())
				.onErrorResumeNext(Observable.empty());
	}

	private Observable<LinkedMessage<DestinationInfoIdentifier>> buildLinkedMessage(final IdentifierPart<String> scope, final String orderId) {
		return Observable.just(LinkedMessage.<DestinationInfoIdentifier>builder()
				.withLinkedIdentifier(buildDestinationInfoIdentifier(scope, orderId, ShipmentDetailsConstants.SHIPMENT_TYPE))
				.withDebugMessage(ShipmentDetailsConstants.MESSAGE_NEED_SHIPMENT_DETAILS)
				.withId(StructuredErrorMessageIdConstants.NEED_SHIPMENT_DETAILS)
				.withType(StructuredMessageTypes.NEEDINFO)
				.build());
	}

	private DestinationInfoIdentifier buildDestinationInfoIdentifier(final IdentifierPart<String> scope,
																	 final String orderId,
																	 final String deliveryId) {
		return DestinationInfoIdentifier.builder()
				.withScope(scope)
				.withShipmentDetailsId(CompositeIdentifier.of(createShipmentDetailsId(orderId, deliveryId)))
				.build();
	}

	@Reference
	public void setDestinationInfoService(final DestinationInfoService destinationInfoService) {
		this.destinationInfoService = destinationInfoService;
	}
}
