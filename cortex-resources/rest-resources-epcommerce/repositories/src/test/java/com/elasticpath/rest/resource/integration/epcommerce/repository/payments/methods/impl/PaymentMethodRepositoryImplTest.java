/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.methods.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CURRENCY;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.LOCALE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.USER_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.createTestPaymentProviderConfiguration;

import java.util.Currency;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.domain.orderpaymentapi.impl.StorePaymentProviderConfigImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.PaymentProviderConfigManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.StorePaymentProviderConfigRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.impl.PaymentMethodRepositoryImpl;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment.OrderPaymentApiRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;

/**
 * Test for {@link PaymentMethodRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentMethodRepositoryImplTest {

	private static final String SCOPE = "MOBEE";
	private static final String STORE_CONFIG_GUID = "STORE_CONFIG_GUID";
	private static final String PROVIDER_CONFIG_GUID = "PROVIDER_CONFIG_GUID";
	private static final String CONFIG_NAME = "CONFIG_NAME";
	private static final String DISPLAY_NAME = "DISPLAY_NAME";

	@Mock
	private StorePaymentProviderConfigRepository storePaymentProviderConfigRepository;

	@Mock
	private PaymentProviderConfigManagementRepository paymentProviderConfigManagementRepository;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private OrderPaymentApiRepository orderPaymentApiRepository;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	private PaymentMethodRepositoryImpl repository;
	private StorePaymentProviderConfig storePaymentProviderConfig;

	@Before
	public void setup() {
		repository = new PaymentMethodRepositoryImpl(storePaymentProviderConfigRepository, paymentProviderConfigManagementRepository,
				orderPaymentApiRepository, storeRepository, customerRepository, resourceOperationContext);
		storePaymentProviderConfig = createTestStorePaymentProviderConfig(STORE_CONFIG_GUID, PROVIDER_CONFIG_GUID);
		when(resourceOperationContext.getSubject()).thenReturn(
				TestSubjectFactory.createWithScopeAndUserIdAndLocaleAndCurrency(SCOPE, USER_ID, LOCALE, CURRENCY));


	}

	@Test
	public void findOnePaymentMethodEntityForMethodIdReturnsExpectedPaymentMethod() {
		PaymentProviderConfigDTO paymentProviderConfigDto = createTestPaymentProviderConfiguration(PROVIDER_CONFIG_GUID, CONFIG_NAME, DISPLAY_NAME);

		when(storePaymentProviderConfigRepository.findByGuid(STORE_CONFIG_GUID)).thenReturn(Single.just(storePaymentProviderConfig));
		when(paymentProviderConfigManagementRepository.findByGuid(PROVIDER_CONFIG_GUID)).thenReturn(Single.just(paymentProviderConfigDto));

		PaymentMethodEntity paymentMethodEntity = PaymentMethodEntity.builder()
				.withName(CONFIG_NAME)
				.withDisplayName(DISPLAY_NAME)
				.build();

		repository.findOnePaymentMethodEntityForMethodId(STORE_CONFIG_GUID)
				.test()
				.assertValue(paymentMethodEntity);
	}

	@Test
	public void findOnePaymentMethodEntityForMethodIdReturnsNotFoundWhenNoSuchPaymentMethodExists() {
		when(storePaymentProviderConfigRepository.findByGuid(STORE_CONFIG_GUID))
				.thenReturn(Single.error(
						ResourceOperationFailure.notFound("No store payment provider config found for the given guid: " + STORE_CONFIG_GUID)));

		repository.findOnePaymentMethodEntityForMethodId(STORE_CONFIG_GUID)
				.test()
				.assertError(ResourceOperationFailure.notFound("No store payment provider config found for the given guid: " + STORE_CONFIG_GUID));
	}

	@Test
	public void getStorePaymentProviderConfigsForStoreCodeReturnsExpectedStoreConfigs() {
		Store store = mock(Store.class);

		when(storeRepository.findStoreAsSingle(SCOPE)).thenReturn(Single.just(store));
		when(storePaymentProviderConfigRepository.findByStore(store)).thenReturn(Observable.just(storePaymentProviderConfig));

		repository.getStorePaymentProviderConfigsForStoreCode(SCOPE)
				.test()
				.assertNoErrors()
				.assertValue(storePaymentProviderConfig);
	}

	@Test
	public void getSaveableStorePaymentProviderConfigsForStoreCodeReturnsOnlySaveableStoreConfigs() {
		Store store = mock(Store.class);
		Customer customer = mock(Customer.class);
		PaymentInstrumentCreationFieldsDTO creationFieldsDTO = mock(PaymentInstrumentCreationFieldsDTO.class);
		PaymentInstrumentCreationFieldsDTO creationFieldsDTO2 = mock(PaymentInstrumentCreationFieldsDTO.class);
		String storeConfigGuid = "STORE_CONFIG_GUID_2";
		String providerConfigGuid = "PROVIDER_CONFIG_GUID_2";
		Locale locale = Locale.CANADA;
		Currency currency = Currency.getInstance(locale);
		String userId = "USER_ID";

		StorePaymentProviderConfig testStorePaymentProviderConfig2 = createTestStorePaymentProviderConfig(storeConfigGuid, providerConfigGuid);

		when(customerRepository.getCustomer(userId)).thenReturn(Single.just(customer));

		when(storeRepository.findStoreAsSingle(SCOPE)).thenReturn(Single.just(store));
		when(storePaymentProviderConfigRepository.findByStore(store))
				.thenReturn(Observable.just(storePaymentProviderConfig, testStorePaymentProviderConfig2));

		when(orderPaymentApiRepository.getPICFields(eq(PROVIDER_CONFIG_GUID), any(PICFieldsRequestContext.class)))
				.thenReturn(Single.just(creationFieldsDTO));
		when(creationFieldsDTO.isSaveable()).thenReturn(true);

		when(orderPaymentApiRepository.getPICFields(eq(providerConfigGuid),
				any(PICFieldsRequestContext.class))).thenReturn(Single.just(creationFieldsDTO2));
		when(creationFieldsDTO2.isSaveable()).thenReturn(false);

		repository.getSaveableStorePaymentProviderConfigsForStoreCode(SCOPE, userId, locale, currency)
				.test()
				.assertValueCount(1)
				.assertValue(storePaymentProviderConfig)
				.assertNoErrors();
	}

	@Test
	public void getStorePaymentProviderConfigsForStoreCodeReturnsEmptyWhenNoConfigsExist() {
		Store store = mock(Store.class);

		when(storeRepository.findStoreAsSingle(SCOPE)).thenReturn(Single.just(store));
		when(storePaymentProviderConfigRepository.findByStore(store)).thenReturn(Observable.empty());

		repository.getStorePaymentProviderConfigsForStoreCode(SCOPE)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	private StorePaymentProviderConfig createTestStorePaymentProviderConfig(final String storeConfigGuid, final String providerConfigGuid) {
		StorePaymentProviderConfig storePaymentProviderConfig = new StorePaymentProviderConfigImpl();
		storePaymentProviderConfig.setGuid(storeConfigGuid);
		storePaymentProviderConfig.setPaymentProviderConfigGuid(providerConfigGuid);
		return storePaymentProviderConfig;
	}
}
