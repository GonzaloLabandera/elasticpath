/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.shipmentdetails.ShipmentDetailsUtil.createShipmentDetailsId;

import java.util.Map;

import io.reactivex.Observable;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Service that returns shipment detail ids accessible by a user.
 */
@Component
public class ShipmentDetailsIdParameterServiceImpl implements ShipmentDetailsIdParameterService {

	private static final Logger LOG = LoggerFactory.getLogger(ShipmentDetailsIdParameterServiceImpl.class);
	private CartOrderRepository cartOrderRepository;
	private ResourceOperationContext resourceOperationContext;
	private CustomerRepository customerRepository;

	@Override
	public Observable<IdentifierPart<Map<String, String>>> findShipmentDetailsIds(final String scope, final String userId) {
		final String sharedId = SubjectUtil.getAccountSharedId(resourceOperationContext.getSubject());

		final Observable<String> guids = StringUtils.isEmpty(sharedId)
				? cartOrderRepository.findCartOrderGuidsByCustomer(scope, userId)
				: cartOrderRepository.findCartOrderGuidsByAccount(scope, customerRepository.getAccountGuid(resourceOperationContext.getSubject()));

		return guids.map(orderId -> createShipmentDetailsId(orderId, ShipmentDetailsConstants.SHIPMENT_TYPE))
				.map(fieldValueMap -> (IdentifierPart<Map<String, String>>) CompositeIdentifier.of(fieldValueMap))
				.doOnError(throwable -> LOG.info("Shipment details were empty for scope '{}' and user id '{}'.", scope, userId))
				.onErrorResumeNext(Observable.empty());
	}

	@Reference
	public void setCartOrderRepository(final CartOrderRepository cartOrderRepository) {
		this.cartOrderRepository = cartOrderRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}
}
