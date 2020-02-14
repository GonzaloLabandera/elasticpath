/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPICFieldsRequestContext;

import java.util.Currency;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.domain.orderpaymentapi.util.PaymentDTOsUtil;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.PaymentProviderConfigManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.StorePaymentProviderConfigRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.PaymentMethodRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment.OrderPaymentApiRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;

/**
 * The facade for operations with payment method identifiers.
 */
@Singleton
@Named("paymentMethodRepository")
public class PaymentMethodRepositoryImpl implements PaymentMethodRepository {

	private final StorePaymentProviderConfigRepository storePaymentProviderConfigRepository;
	private final PaymentProviderConfigManagementRepository paymentProviderConfigManagementRepository;
	private final OrderPaymentApiRepository orderPaymentApiRepository;
	private final StoreRepository storeRepository;
	private final CustomerRepository customerRepository;
	private final ResourceOperationContext resourceOperationContext;

	/**
	 * Constructor.
	 *
	 * @param storePaymentProviderConfigRepository      storePaymentProviderConfigRepository
	 * @param paymentProviderConfigManagementRepository paymentProviderConfigManagementRepository
	 * @param orderPaymentApiRepository                 orderPaymentApiRepository
	 * @param storeRepository                           storeRepository
	 * @param customerRepository                        customerRepository
	 * @param resourceOperationContext                  resourceOperationContext
	 */
	@Inject
	public PaymentMethodRepositoryImpl(
			@Named("storePaymentProviderConfigRepository") final StorePaymentProviderConfigRepository storePaymentProviderConfigRepository,
			@Named("paymentProviderConfigManagementRepository") final PaymentProviderConfigManagementRepository
					paymentProviderConfigManagementRepository,
			@Named("orderPaymentApiRepository") final OrderPaymentApiRepository orderPaymentApiRepository,
			@Named("storeRepository") final StoreRepository storeRepository,
			@Named("customerRepository") final CustomerRepository customerRepository,
			@Named("resourceOperationContext") final ResourceOperationContext resourceOperationContext) {
		this.storePaymentProviderConfigRepository = storePaymentProviderConfigRepository;
		this.paymentProviderConfigManagementRepository = paymentProviderConfigManagementRepository;
		this.orderPaymentApiRepository = orderPaymentApiRepository;
		this.storeRepository = storeRepository;
		this.customerRepository = customerRepository;
		this.resourceOperationContext = resourceOperationContext;
	}

	@Override
	public Single<PaymentMethodEntity> findOnePaymentMethodEntityForMethodId(final String paymentMethodId) {
		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());

		return storePaymentProviderConfigRepository.findByGuid(paymentMethodId)
				.map(StorePaymentProviderConfig::getPaymentProviderConfigGuid)
				.flatMap(paymentProviderConfigManagementRepository::findByGuid)
				.map(paymentProviderConfigDTO -> PaymentMethodEntity.builder()
						.withName(paymentProviderConfigDTO.getConfigurationName())
						.withDisplayName(PaymentDTOsUtil.getDisplayName(paymentProviderConfigDTO, locale))
						.build());
	}

	@Override
	@CacheResult
	public Observable<StorePaymentProviderConfig> getStorePaymentProviderConfigsForStoreCode(final String storeCode) {
		return storeRepository.findStoreAsSingle(storeCode)
				.flatMapObservable(storePaymentProviderConfigRepository::findByStore);
	}

	@Override
	@CacheResult
	public Observable<StorePaymentProviderConfig> getSaveableStorePaymentProviderConfigsForStoreCode(final String storeCode, final String userId,
																									 final Locale locale, final Currency currency) {
		return customerRepository.getCustomer(userId)
				.map(customer -> buildPICFieldsRequestContext(locale, currency, customer))
				.flatMapObservable(picRequestContext -> getStorePaymentProviderConfigsForStoreCode(storeCode)
						.flatMapSingle(storePaymentProviderConfig ->
								getStorePaymentProviderConfigSaveablePair(picRequestContext, storePaymentProviderConfig)))
				.filter(Pair::getSecond)
				.map(Pair::getFirst);
	}

	private Single<Pair<StorePaymentProviderConfig, Boolean>> getStorePaymentProviderConfigSaveablePair(final PICFieldsRequestContext
																												picRequestContext,
																										final StorePaymentProviderConfig
																												storePaymentProviderConfig) {
		return orderPaymentApiRepository.getPICFields(storePaymentProviderConfig.getPaymentProviderConfigGuid(), picRequestContext)
				.map(PaymentInstrumentCreationFieldsDTO::isSaveable)
				.map(isSaveable -> Pair.of(storePaymentProviderConfig, isSaveable));
	}

	protected StorePaymentProviderConfigRepository getStorePaymentProviderConfigRepository() {
		return storePaymentProviderConfigRepository;
	}

	protected PaymentProviderConfigManagementRepository getPaymentProviderConfigManagementRepository() {
		return paymentProviderConfigManagementRepository;
	}

	protected OrderPaymentApiRepository getOrderPaymentApiRepository() {
		return orderPaymentApiRepository;
	}

	protected StoreRepository getStoreRepository() {
		return storeRepository;
	}

	protected CustomerRepository getCustomerRepository() {
		return customerRepository;
	}

	protected ResourceOperationContext getResourceOperationContext() {
		return resourceOperationContext;
	}
}
