/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.methods.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildAccountPaymentMethodIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildAccountPaymentMethodsIdentifier;
import static org.mockito.Mockito.when;

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
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodsIdentifier;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.PaymentMethodRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.impl.AccountPaymentMethodEntityRepositoryImpl;

/**
 * Test for {@link AccountPaymentMethodEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountPaymentMethodEntityRepositoryImplTest {

	private static final String SCOPE = "MOBEE";
	private static final Locale LOCALE = Locale.CANADA;
	private static final Currency CURRENCY = Currency.getInstance(LOCALE);
	private static final String CONFIG_ID_1 = "CONFIG_GUID_1";
	private static final String CONFIG_ID_2 = "CONFIG_GUID_2";
	private static final String ACCOUNT_ID = "ACCOUNT_ID";

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private PaymentMethodRepository paymentMethodRepository;

	@InjectMocks
	private AccountPaymentMethodEntityRepositoryImpl repository;

	private AccountIdentifier accountIdentifier;

	private AccountsIdentifier accountsIdentifier;

	@Before
	public void setup() {
		accountsIdentifier = AccountsIdentifier.builder()
				.withScope(StringIdentifier.of(SCOPE)).build();
		accountIdentifier = AccountIdentifier.builder().withAccounts(accountsIdentifier)
				.withAccountId(StringIdentifier.of(ACCOUNT_ID))
				.build();

		AccountPaymentMethodsIdentifier accountPaymentMethodsIdentifier =
				buildAccountPaymentMethodsIdentifier(accountIdentifier);
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.of(accountPaymentMethodsIdentifier));
		when(resourceOperationContext.getSubject())
				.thenReturn(TestSubjectFactory.createWithScopeAndUserIdAndLocaleAndCurrency(SCOPE, ACCOUNT_ID, LOCALE, CURRENCY));

	}


	@Test
	public void findAllReturnsExpectedPaymentMethods() {
		StorePaymentProviderConfig storePaymentProviderConfig1 = createTestStorePaymentProviderConfig(CONFIG_ID_1);
		StorePaymentProviderConfig storePaymentProviderConfig2 = createTestStorePaymentProviderConfig(CONFIG_ID_2);

		when(paymentMethodRepository.getSaveableStorePaymentProviderConfigsForStoreCode(SCOPE, ACCOUNT_ID, LOCALE, CURRENCY))
				.thenReturn(Observable.just(storePaymentProviderConfig1, storePaymentProviderConfig2));

		AccountPaymentMethodIdentifier accountPaymentMethodIdentifier =
				buildAccountPaymentMethodIdentifier(accountIdentifier, StringIdentifier.of(CONFIG_ID_1));

		AccountPaymentMethodIdentifier accountPaymentMethodIdentifier2 =
				buildAccountPaymentMethodIdentifier(accountIdentifier, StringIdentifier.of(CONFIG_ID_2));

		repository.findAll(StringIdentifier.of(SCOPE))
				.test()
				.assertValues(accountPaymentMethodIdentifier, accountPaymentMethodIdentifier2);
	}

	@Test
	public void findAllReturnsEmptyWhenNoPaymentMethodsExist() {
		when(paymentMethodRepository.getSaveableStorePaymentProviderConfigsForStoreCode(SCOPE, ACCOUNT_ID, LOCALE, CURRENCY))
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

		AccountPaymentMethodIdentifier accountPaymentMethodIdentifier =
				buildAccountPaymentMethodIdentifier(accountIdentifier, StringIdentifier.of(CONFIG_ID_1));

		repository.findOne(accountPaymentMethodIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(paymentMethodEntity);
	}

	@Test
	public void findOneReturnsErrorWhenPaymentMethodDoesNotExist() {
		when(paymentMethodRepository.findOnePaymentMethodEntityForMethodId(CONFIG_ID_1))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));

		AccountPaymentMethodIdentifier accountPaymentMethodIdentifier =
				buildAccountPaymentMethodIdentifier(accountIdentifier, StringIdentifier.of(CONFIG_ID_1));

		repository.findOne(accountPaymentMethodIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound());
	}

	private StorePaymentProviderConfig createTestStorePaymentProviderConfig(final String guid) {
		StorePaymentProviderConfig storePaymentProviderConfig = new StorePaymentProviderConfigImpl();
		storePaymentProviderConfig.setGuid(guid);
		return storePaymentProviderConfig;
	}
}
