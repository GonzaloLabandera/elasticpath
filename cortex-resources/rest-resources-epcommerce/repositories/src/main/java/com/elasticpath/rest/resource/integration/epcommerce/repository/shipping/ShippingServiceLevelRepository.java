/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipping;

import java.util.Map;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.shipping.ShippingServiceLevel;

/**
 * The facade for operations with shipping service levels.
 */
public interface ShippingServiceLevelRepository {

	/**
	 * Find a Shipping Service Level by guid.
	 * @param storeCode the store code
	 * @param shipmentDetailsId the shipment details id
	 * @param shippingServiceLevelGuid the shipping service level guid
	 *
	 * @return Single with the shipping service level
	 */
	Single<ShippingServiceLevel> findByGuid(String storeCode, Map<String, String> shipmentDetailsId, String shippingServiceLevelGuid);

	/**
	 * Find shippingServiceLevel.
	 *
	 * @param shippingServiceLevelGuid	shipping service level id
	 * @param shippingServiceLevelList	observable shipping service level
	 * @return Single with shipping service level
	 */
	Single<ShippingServiceLevel> getShippingServiceLevel(String shippingServiceLevelGuid, Observable<ShippingServiceLevel> shippingServiceLevelList);

	/**
	 * Find available shipping service levels for the given shipment.
	 *
	 * @param storeCode the store code
	 * @param shipmentDetailsId the shipment details id
	 * @return ExecutionResult with the collection of valid shipping service levels
	 */
	Observable<String> findShippingServiceLevelGuidsForShipment(String storeCode, Map<String, String> shipmentDetailsId);

	/**
	 * Gets the selected shipping service level GUID for the given shipment.
	 *
	 * @param storeCode the store code
	 * @param shipmentDetailsId the shipment details id
	 * @return Single with the selected shipping service level GUID
	 */
	Maybe<String> getSelectedShippingOptionIdForShipmentDetails(String storeCode, Map<String, String> shipmentDetailsId);
}
