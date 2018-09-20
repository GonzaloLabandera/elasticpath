/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipment.impl.shippingoptions;

import java.util.Collections;
import java.util.Locale;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.base.CostEntity;
import com.elasticpath.rest.definition.shipmentdetails.ShippingOptionEntity;
import com.elasticpath.rest.definition.shipments.ShipmentIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipment.ShipmentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.rest.resource.integration.epcommerce.transform.MoneyTransformer;
import com.elasticpath.service.shipping.ShippingServiceLevelService;

/**
 * Repository for reading shipping option for shipment.
 * @param <E>	extends ShippingOptionEntity
 * @param <I>	extends ShipmentIdentifier
 */
@Component
public class ShippingOptionForShipmentRepositoryImpl<E extends ShippingOptionEntity, I extends ShipmentIdentifier>
		implements Repository<ShippingOptionEntity, ShipmentIdentifier> {

	private static final String SHIPPING_OPTION_NOT_FOUND = "Shipping option not found.";
	private MoneyTransformer moneyTransformer;
	private ShipmentRepository shipmentRepository;
	private ShippingServiceLevelService shippingServiceLevelService;
	private ReactiveAdapter reactiveAdapter;
	private ResourceOperationContext resourceOperationContext;

	@Override
	public Single<ShippingOptionEntity> findOne(final ShipmentIdentifier identifier) {
		String purchaseId = identifier.getShipments().getPurchase().getPurchaseId().getValue();
		String shipmentId = identifier.getShipmentId().getValue();
		return shipmentRepository.find(purchaseId, shipmentId)
				.flatMap(this::getShippingOptionFromOrderShipment);
	}

	private Single<ShippingOptionEntity> getShippingOptionFromOrderShipment(final PhysicalOrderShipment orderShipment) {
		return Single.just(moneyTransformer.transformToEntity(orderShipment.getShippingCostMoney()))
				.flatMap(costEntity -> buildShippingOptionEntityFromCostEntity(orderShipment, costEntity));
	}

	private Single<ShippingOptionEntity> buildShippingOptionEntityFromCostEntity(final PhysicalOrderShipment orderShipment,
																				 final CostEntity costEntity) {
		return getShippingServiceLevel(orderShipment)
				.map(serviceLevel -> buildShippingOptionEntity(costEntity, serviceLevel));
	}

	@CacheResult
	private Single<ShippingServiceLevel> getShippingServiceLevel(final PhysicalOrderShipment orderShipment) {
		return reactiveAdapter.fromServiceAsSingle(
				() -> shippingServiceLevelService.findByGuid(orderShipment.getShippingServiceLevelGuid()), SHIPPING_OPTION_NOT_FOUND);
	}

	private ShippingOptionEntity buildShippingOptionEntity(final CostEntity costEntity, final ShippingServiceLevel serviceLevel) {
		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		return ShippingOptionEntity.builder()
				.withCost(Collections.singleton(costEntity))
				.withCarrier(serviceLevel.getCarrier())
				.withName(serviceLevel.getCode())
				.withDisplayName(serviceLevel.getDisplayName(locale, true))
				.build();
	}

	@Reference
	public void setMoneyTransformer(final MoneyTransformer moneyTransformer) {
		this.moneyTransformer = moneyTransformer;
	}

	@Reference
	public void setShipmentRepository(final ShipmentRepository shipmentRepository) {
		this.shipmentRepository = shipmentRepository;
	}

	@Reference
	public void setShippingServiceLevelRepository(final ShippingServiceLevelService shippingServiceLevelService) {
		this.shippingServiceLevelService = shippingServiceLevelService;
	}

	@Reference
	public void setShippingServiceLevelService(final ShippingServiceLevelService shippingServiceLevelService) {
		this.shippingServiceLevelService = shippingServiceLevelService;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}
}
