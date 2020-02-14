/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.impl;

import java.util.Optional;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodsIdentifier;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.PaymentMethodRepository;

/**
 * Payment Method Entity repository for Order Payment Method.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class OrderPaymentMethodEntityRepositoryImpl<E extends PaymentMethodEntity, I extends OrderPaymentMethodIdentifier>
		implements Repository<PaymentMethodEntity, OrderPaymentMethodIdentifier> {

	private ResourceOperationContext resourceOperationContext;
	private PaymentMethodRepository paymentMethodRepository;

	@Override
	public Observable<OrderPaymentMethodIdentifier> findAll(final IdentifierPart<String> scope) {

		Optional<ResourceIdentifier> resourceIdentifier = resourceOperationContext.getResourceIdentifier();

		if (resourceIdentifier.isPresent()) {
			OrderPaymentMethodsIdentifier orderPaymentMethodsIdentifier = (OrderPaymentMethodsIdentifier) resourceIdentifier.get();
			return paymentMethodRepository.getStorePaymentProviderConfigsForStoreCode(scope.getValue())
					.map(storePaymentProviderConfig -> OrderPaymentMethodIdentifier.builder()
							.withOrderPaymentMethods(orderPaymentMethodsIdentifier)
							.withPaymentMethodId(StringIdentifier.of(storePaymentProviderConfig.getGuid()))
							.build());
		}
		return Observable.empty();
	}

	@Override
	public Single<PaymentMethodEntity> findOne(final OrderPaymentMethodIdentifier identifier) {
		String paymentMethodId = identifier.getPaymentMethodId().getValue();
		return paymentMethodRepository.findOnePaymentMethodEntityForMethodId(paymentMethodId);
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setPaymentMethodRepository(final PaymentMethodRepository paymentMethodRepository) {
		this.paymentMethodRepository = paymentMethodRepository;
	}
}
