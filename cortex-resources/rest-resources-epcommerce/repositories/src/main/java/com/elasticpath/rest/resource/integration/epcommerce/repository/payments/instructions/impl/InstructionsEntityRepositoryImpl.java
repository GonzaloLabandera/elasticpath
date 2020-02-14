/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPICFieldsRequestContext;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPICRequestContext;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentMethodConfigurationEntity;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Single;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.domain.orderpaymentapi.impl.PICRequestContext;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.paymentinstructions.PaymentMethodConfigurationEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.validator.AddressValidator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.StorePaymentProviderConfigRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instructions.InstructionsEntityRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment.OrderPaymentApiRepository;

/**
 * The implementation of {@link InstructionsEntityRepository} related operations.
 */
@Singleton
@Named("instructionsEntityRepository")
public class InstructionsEntityRepositoryImpl implements InstructionsEntityRepository {

	private final OrderPaymentApiRepository orderPaymentApiRepository;
	private final StorePaymentProviderConfigRepository configRepository;
	private final CustomerRepository customerRepository;
	private final AddressValidator addressValidator;
	private final ConversionService conversionService;

	/**
	 * Constructor.
	 *
	 * @param orderPaymentApiRepository order payment API repository
	 * @param repository                store payment provider configuration repository
	 * @param customerRepository        customer repository
	 * @param addressValidator          address validator
	 * @param conversionService         conversion service
	 */
	@Inject
	public InstructionsEntityRepositoryImpl(@Named("orderPaymentApiRepository") final OrderPaymentApiRepository orderPaymentApiRepository,
											@Named("storePaymentProviderConfigRepository") final StorePaymentProviderConfigRepository repository,
											@Named("customerRepository") final CustomerRepository customerRepository,
											@Named("addressValidator") final AddressValidator addressValidator,
											@Named("conversionService") final ConversionService conversionService) {
		this.orderPaymentApiRepository = orderPaymentApiRepository;
		this.configRepository = repository;
		this.customerRepository = customerRepository;
		this.addressValidator = addressValidator;
		this.conversionService = conversionService;
	}

	@Override
	@CacheResult
	public Single<PaymentMethodConfigurationEntity> getPaymentInstrumentCreationInstructionsFieldsForMethodId(final String methodId,
																											  final String userId,
																											  final Currency currency,
																											  final Locale locale) {
		return customerRepository.getCustomer(userId)
				.map(customer -> buildPICFieldsRequestContext(locale, currency, customer))
				.flatMap(picFieldsRequestContext -> configRepository.getPaymentProviderConfigIdByStorePaymentProviderConfigId(methodId)
						.flatMap(configId -> getPICInstructionsFieldsForPaymentProviderConfigId(configId, picFieldsRequestContext))
						.flatMap(fields -> configRepository.requiresBillingAddress(methodId)
								.map(requiresBillingAddress -> buildPaymentMethodConfigurationEntity(fields, requiresBillingAddress))));
	}

	@Override
	public Single<PICInstructionsDTO> submitRequestInstructionsForm(final String methodId, final Locale locale, final Currency currency,
																	final PaymentMethodConfigurationEntity configEntity, final String userId) {
		Map<String, String> formData;
		AddressEntity billingAddressEntity = null;

		if (configEntity.getDynamicProperties() == null || configEntity.getDynamicProperties().isEmpty()) {
			formData = Collections.emptyMap();
		} else {
			formData = configEntity.getDynamicProperties();
		}

		if (configEntity.getBillingAddress() != null) {
			billingAddressEntity = configEntity.getBillingAddress();
		}
		return submitInstructionsForm(methodId, locale, currency, userId, formData, billingAddressEntity);
	}

	private Single<PICInstructionsDTO> submitInstructionsForm(final String methodId, final Locale locale, final Currency currency,
															  final String userId, final Map<String, String> formData,
															  final AddressEntity billingAddressEntity) {
		if (billingAddressEntity != null) {
			return customerRepository.getCustomer(userId)
					.flatMap(customer -> submitInstructionsFormWithAddress(methodId, locale, currency, formData, billingAddressEntity, customer));
		}
		return customerRepository.getCustomer(userId)
				.flatMap(customer -> submitForm(methodId, formData, buildPICRequestContext(locale, currency, null, customer)));
	}

	private Single<PICInstructionsDTO> submitInstructionsFormWithAddress(final String methodId, final Locale locale, final Currency currency,
																		 final Map<String, String> formData,
																		 final AddressEntity billingAddressEntity, final Customer customer) {
		CustomerAddress customerAddress = conversionService.convert(billingAddressEntity, CustomerAddress.class);
		return addressValidator.validate(billingAddressEntity)
				.andThen(customerRepository.createAddressForCustomer(customer, customerAddress)
						.map(address -> conversionService.convert(address, AddressDTO.class))
						.map(billingAddressDTO -> buildPICRequestContext(locale, currency, billingAddressDTO, customer))
						.flatMap(picRequestContext -> submitForm(methodId, formData, picRequestContext)));
	}

	private Single<PICInstructionsDTO> submitForm(final String methodId, final Map<String, String> formData,
												  final PICRequestContext picRequestContext) {

		return configRepository.getPaymentProviderConfigIdByStorePaymentProviderConfigId(methodId)
				.flatMap(configId -> orderPaymentApiRepository.getPICInstructions(configId, formData, picRequestContext));
	}

	private Single<List<String>> getPICInstructionsFieldsForPaymentProviderConfigId(final String paymentProviderConfigId,
																					final PICFieldsRequestContext picFieldsRequestContext) {
		return orderPaymentApiRepository.getPICInstructionsFields(paymentProviderConfigId, picFieldsRequestContext)
				.map(PICInstructionsFieldsDTO::getFields);
	}
}
