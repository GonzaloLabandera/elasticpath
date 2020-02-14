/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.elasticpath.commons.constants.ContextIdNames.CUSTOMER_PAYMENT_INSTRUMENT;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CURRENCY;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.LOCALE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.CUSTOMER_PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.CART_ORDER_PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.buildTestAddressEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Completable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.orderpaymentapi.impl.PICRequestContext;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentAttributesEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfilePaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodsIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.validator.AddressValidator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.StorePaymentProviderConfigRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CartOrderPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerDefaultPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment.OrderPaymentApiRepository;

/**
 * Tests for {@link com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository}.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod", "PMD.ExcessiveImports"})
public abstract class AbstractPaymentInstrumentRepositoryTest {

	static final IdentifierPart<String> SCOPE = StringIdentifier.of("MOBEE");
	static final IdentifierPart<String> CART_ORDER_ID = StringIdentifier.of("CART_ORDER_ID");
	static final IdentifierPart<String> CUSTOMER_ID = StringIdentifier.of("CUSTOMER_ID");
	static final IdentifierPart<String> PAYMENT_CONFIGURATION_ID = StringIdentifier.of("PAYMENT_CONFIGURATION_ID");
	static final IdentifierPart<String> STORE_PAYMENT_CONFIGURATION_ID = StringIdentifier.of("STORE_PAYMENT_CONFIGURATION_ID");

	@InjectMocks
	PaymentInstrumentRepositoryImpl repository;

	@Mock
	ResourceOperationContext resourceOperationContext;

	@Mock
	OrderPaymentApiRepository orderPaymentApiRepository;

	@Mock
	CustomerRepository customerRepository;

	@Mock
	CartOrderRepository cartOrderRepository;

	@Mock
	CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;

	@Mock
	CustomerDefaultPaymentInstrumentRepository customerDefaultPaymentInstrumentRepository;

	@Mock
	private StorePaymentProviderConfigRepository storePaymentProviderConfigRepository;

	@Mock
	CartOrderPaymentInstrumentRepository cartOrderPaymentInstrumentRepository;

	@Mock
	ConversionService conversionService;

	@Mock
	AddressValidator addressValidator;

	@Mock
	BeanFactory beanFactory;

	Customer customer;
	CustomerPaymentInstrument customerPaymentInstrument;
	CartOrder cartOrder;
	CartOrderPaymentInstrument cartOrderPaymentInstrument;

	@Before
	public void setUp() {
		customer = mock(Customer.class);
		cartOrder = mock(CartOrder.class);
		customerPaymentInstrument = mock(CustomerPaymentInstrument.class);
		cartOrderPaymentInstrument = mock(CartOrderPaymentInstrument.class);

		when(resourceOperationContext.getUserIdentifier()).thenReturn(CUSTOMER_ID.getValue());
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.of(createOrderPIFormIdentifier()));
		when(resourceOperationContext.getSubject())
				.thenReturn(TestSubjectFactory.createWithScopeAndUserIdAndLocaleAndCurrency(SCOPE.getValue(),
						CUSTOMER_ID.getValue(), LOCALE, CURRENCY));

		when(customerRepository.getCustomer(CUSTOMER_ID.getValue())).thenReturn(Single.just(customer));

		when(customerPaymentInstrument.getGuid()).thenReturn(CUSTOMER_PAYMENT_INSTRUMENT_ID);
		when(beanFactory.getPrototypeBean(CUSTOMER_PAYMENT_INSTRUMENT, CustomerPaymentInstrument.class)).thenReturn(customerPaymentInstrument);

		when(orderPaymentApiRepository.createPI(eq(PAYMENT_CONFIGURATION_ID.getValue()),
				eq(ImmutableMap.of("key", "data")), any(PICRequestContext.class))).thenReturn(Single.just(PAYMENT_INSTRUMENT_ID));

		when(orderPaymentApiRepository.createPI(eq(PAYMENT_CONFIGURATION_ID.getValue()),
				eq(Collections.emptyMap()), any(PICRequestContext.class))).thenReturn(Single.just(PAYMENT_INSTRUMENT_ID));

		final StorePaymentProviderConfig storePaymentProviderConfig = mock(StorePaymentProviderConfig.class);
		when(storePaymentProviderConfig.getPaymentProviderConfigGuid()).thenReturn(PAYMENT_CONFIGURATION_ID.getValue());
		when(storePaymentProviderConfigRepository.findByGuid(STORE_PAYMENT_CONFIGURATION_ID.getValue()))
				.thenReturn(Single.just(storePaymentProviderConfig));

		when(customerDefaultPaymentInstrumentRepository.hasDefaultPaymentInstrument(customer)).thenReturn(Single.just(true));

		when(customerPaymentInstrumentRepository.saveOrUpdate(customerPaymentInstrument)).thenReturn(Single.just(customerPaymentInstrument));
		when(customerDefaultPaymentInstrumentRepository.saveAsDefault(customerPaymentInstrument)).thenReturn(Completable.complete());

	}

	static ProfilePaymentInstrumentFormIdentifier createProfilePIFormIdentifier() {
		ProfileIdentifier profileIdentifier = ProfileIdentifier.builder().withProfileId(CUSTOMER_ID)
				.withScope(SCOPE)
				.build();

		return ProfilePaymentInstrumentFormIdentifier.builder()
				.withProfilePaymentMethod(ProfilePaymentMethodIdentifier.builder()
						.withPaymentMethodId(STORE_PAYMENT_CONFIGURATION_ID)
						.withProfilePaymentMethods(ProfilePaymentMethodsIdentifier.builder()
								.withProfile(profileIdentifier)
								.build())
						.build())
				.build();
	}

	private static OrderPaymentInstrumentFormIdentifier createOrderPIFormIdentifier() {
		return OrderPaymentInstrumentFormIdentifier.builder()
				.withOrderPaymentMethod(OrderPaymentMethodIdentifier.builder()
						.withPaymentMethodId(STORE_PAYMENT_CONFIGURATION_ID)
						.withOrderPaymentMethods(OrderPaymentMethodsIdentifier.builder()
								.withOrder(OrderIdentifier.builder()
										.withScope(SCOPE)
										.withOrderId(CART_ORDER_ID)
										.build())
								.build())
						.build())
				.build();
	}

	static PaymentInstrumentForFormEntity createProfilePIFormEntity(final boolean defaultOnProfile, final boolean billingAddressRequired) {
		PaymentInstrumentForFormEntity.Builder builder = PaymentInstrumentForFormEntity.builder()
				.withDefaultOnProfile(defaultOnProfile)
				.withPaymentInstrumentIdentificationForm(PaymentInstrumentAttributesEntity.builder()
						.addingProperty("key", "data")
						.build());

		if (billingAddressRequired) {
			builder.withBillingAddress(buildTestAddressEntity());
		}

		return builder
				.build();
	}

	static SubmitResult<PaymentInstrumentIdentifier> getExpectedProfilePISubmitResult() {
		return SubmitResult.<PaymentInstrumentIdentifier>builder()
				.withIdentifier(buildExpectedPIIdentifier())
				.withStatus(SubmitStatus.CREATED)
				.build();
	}

	private static PaymentInstrumentIdentifier buildExpectedPIIdentifier() {
		return PaymentInstrumentIdentifier.builder()
				.withPaymentInstrumentId(StringIdentifier.of(CUSTOMER_PAYMENT_INSTRUMENT_ID))
				.withPaymentInstruments(PaymentInstrumentsIdentifier.builder()
						.withScope(SCOPE)
						.build())
				.build();
	}

	static OrderPaymentInstrumentForFormEntity createOrderPIFormEntity(final boolean saveOnProfile, final boolean defaultOnProfile) {
		return OrderPaymentInstrumentForFormEntity.builder()
				.withLimitAmount(BigDecimal.TEN)
				.withPaymentInstrumentIdentificationForm(PaymentInstrumentAttributesEntity.builder()
						.addingProperty("key", "data")
						.build())
				.withDefaultOnProfile(defaultOnProfile)
				.withSaveOnProfile(saveOnProfile)
				.build();
	}

	static SubmitResult<OrderPaymentInstrumentIdentifier> getExpectedOrderPISubmitResult() {
		return SubmitResult.<OrderPaymentInstrumentIdentifier>builder()
				.withIdentifier(buildExpectedOrderPIIdentifier())
				.withStatus(SubmitStatus.CREATED)
				.build();
	}

	private static OrderPaymentInstrumentIdentifier buildExpectedOrderPIIdentifier() {
		return OrderPaymentInstrumentIdentifier.builder()
				.withPaymentInstrumentId(StringIdentifier.of(CART_ORDER_PAYMENT_INSTRUMENT_ID))
				.withOrder(OrderIdentifier.builder()
						.withScope(SCOPE)
						.withOrderId(CART_ORDER_ID)
						.build())
				.build();
	}


}
