/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.order.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildOrderPaymentInstrumentForFormEntity;

import java.math.BigDecimal;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.StorePaymentProviderConfigRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;

/**
 * Repository for Order Payment Instrument Form.
 *
 * @param <E> extends OrderPaymentInstrumentForFormEntity
 * @param <I> extends OrderPaymentInstrumentFormIdentifier
 */
@Component
public class OrderPaymentInstrumentFormEntityRepositoryImpl
		<E extends OrderPaymentInstrumentForFormEntity, I extends OrderPaymentInstrumentFormIdentifier>
		implements Repository<OrderPaymentInstrumentForFormEntity, OrderPaymentInstrumentFormIdentifier> {

	private PaymentInstrumentRepository repository;
	private CustomerRepository customerRepository;
	private ResourceOperationContext resourceOperationContext;
	private StorePaymentProviderConfigRepository storePaymentProviderConfigRepository;

	@Override
	public Single<OrderPaymentInstrumentForFormEntity> findOne(final OrderPaymentInstrumentFormIdentifier identifier) {
		final String providerConfigGuid = identifier.getOrderPaymentMethod().getPaymentMethodId().getValue();
		final String customerGuid = resourceOperationContext.getUserIdentifier();

		return customerRepository.getCustomer(customerGuid)
				.map(Customer::isRegistered)
				.flatMap(isRegistered -> repository.getPaymentInstrumentCreationFieldsForProviderConfigGuid(providerConfigGuid)
						.flatMap(fields -> buildOrderPaymentInstrumentForm(providerConfigGuid, isRegistered, fields)));
	}

	private Single<OrderPaymentInstrumentForFormEntity> buildOrderPaymentInstrumentForm(final String providerConfigGuid, final Boolean isRegistered,
																						final PaymentInstrumentCreationFieldsDTO fields) {
		return storePaymentProviderConfigRepository.requiresBillingAddress(providerConfigGuid)
				.map(requiresBillingAddress -> buildOrderPaymentInstrumentForFormEntity(BigDecimal.ZERO, fields.getFields(),
						isRegistered && fields.isSaveable(), requiresBillingAddress));
	}

	@Reference
	public void setPaymentInstrumentRepository(final PaymentInstrumentRepository repository) {
		this.repository = repository;
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setStorePaymentProviderConfigRepository(final StorePaymentProviderConfigRepository storePaymentProviderConfigRepository) {
		this.storePaymentProviderConfigRepository = storePaymentProviderConfigRepository;
	}
}
