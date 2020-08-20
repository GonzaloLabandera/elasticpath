/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.methods.impl;

import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildProfilePaymentMethodIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildProfilePaymentMethodsIdentifier;

import java.util.Currency;
import java.util.Locale;
import java.util.Optional;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.orderpaymentapi.impl.StorePaymentProviderConfigImpl;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.PaymentMethodRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.impl.ProfilePaymentMethodEntityRepositoryImpl;

/**
 * Test for {@link ProfilePaymentMethodEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfilePaymentMethodEntityRepositoryTest {

	private static final String SCOPE = "MOBEE";
	private static final String CONFIG_ID_1 = "CONFIG_GUID_1";
	private static final String CONFIG_ID_2 = "CONFIG_GUID_2";
	private static final Locale LOCALE = Locale.CANADA;
	private static final Currency CURRENCY = Currency.getInstance(LOCALE);
	private static final String USER_ID = "SHARED_ID";

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private PaymentMethodRepository paymentMethodRepository;

	@InjectMocks
	private ProfilePaymentMethodEntityRepositoryImpl<PaymentMethodEntity, ProfilePaymentMethodIdentifier> repository;

	@Before
	public void setup() {
		ProfilePaymentMethodsIdentifier profilePaymentMethodsIdentifier =
				buildProfilePaymentMethodsIdentifier(StringIdentifier.of(USER_ID), StringIdentifier.of(SCOPE));
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.of(profilePaymentMethodsIdentifier));
		when(resourceOperationContext.getSubject())
				.thenReturn(TestSubjectFactory.createWithScopeAndUserIdAndLocaleAndCurrency(SCOPE, USER_ID, LOCALE, CURRENCY));
		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_ID);
	}


	@Test
	public void findAllReturnsExpectedPaymentMethods() {
		StorePaymentProviderConfig storePaymentProviderConfig1 = createTestStorePaymentProviderConfig(CONFIG_ID_1);
		StorePaymentProviderConfig storePaymentProviderConfig2 = createTestStorePaymentProviderConfig(CONFIG_ID_2);

		when(paymentMethodRepository.getSaveableStorePaymentProviderConfigsForStoreCode(SCOPE, USER_ID, LOCALE, CURRENCY))
				.thenReturn(Observable.just(storePaymentProviderConfig1, storePaymentProviderConfig2));

		ProfilePaymentMethodIdentifier paymentMethodIdentifier1 =
				buildProfilePaymentMethodIdentifier(StringIdentifier.of(USER_ID), StringIdentifier.of(SCOPE), StringIdentifier.of(CONFIG_ID_1));

		ProfilePaymentMethodIdentifier paymentMethodIdentifier2 =
				buildProfilePaymentMethodIdentifier(StringIdentifier.of(USER_ID), StringIdentifier.of(SCOPE), StringIdentifier.of(CONFIG_ID_2));

		repository.findAll(StringIdentifier.of(SCOPE))
				.test()
				.assertValues(paymentMethodIdentifier1, paymentMethodIdentifier2);
	}

	@Test
	public void findAllReturnsEmptyWhenNoPaymentMethodsExist() {
		when(paymentMethodRepository.getSaveableStorePaymentProviderConfigsForStoreCode(SCOPE, USER_ID, LOCALE, CURRENCY))
				.thenReturn(Observable.empty());

		repository.findAll(StringIdentifier.of(SCOPE))
				.test()
				.assertNoValues()
				.assertNoErrors();
	}

	@Test
	public void findAllReturnsEmptyWhenResourceIdentifierDoesNotExist() {
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.empty());
		repository.findAll(StringIdentifier.of(SCOPE))
				.test()
				.assertNoValues()
				.assertNoErrors();
	}

	@Test
	public void findOneReturnsExpectedPaymentMethod() {
		PaymentMethodEntity paymentMethodEntity = PaymentMethodEntity.builder()
				.withName("Test Payment Method Name")
				.build();

		when(paymentMethodRepository.findOnePaymentMethodEntityForMethodId(CONFIG_ID_1)).thenReturn(Single.just(paymentMethodEntity));

		ProfilePaymentMethodIdentifier profilePaymentMethodIdentifier =
				buildProfilePaymentMethodIdentifier(StringIdentifier.of(USER_ID), StringIdentifier.of(SCOPE), StringIdentifier.of(CONFIG_ID_1));

		repository.findOne(profilePaymentMethodIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(paymentMethodEntity);
	}

	@Test
	public void findOneReturnsErrorWhenPaymentMethodDoesNotExist() {
		when(paymentMethodRepository.findOnePaymentMethodEntityForMethodId(CONFIG_ID_1))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));

		ProfilePaymentMethodIdentifier profilePaymentMethodIdentifier =
				buildProfilePaymentMethodIdentifier(StringIdentifier.of(USER_ID), StringIdentifier.of(SCOPE), StringIdentifier.of(CONFIG_ID_1));

		repository.findOne(profilePaymentMethodIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound());
	}

	private StorePaymentProviderConfig createTestStorePaymentProviderConfig(final String guid) {
		StorePaymentProviderConfig storePaymentProviderConfig = new StorePaymentProviderConfigImpl();
		storePaymentProviderConfig.setGuid(guid);
		return storePaymentProviderConfig;
	}
}
