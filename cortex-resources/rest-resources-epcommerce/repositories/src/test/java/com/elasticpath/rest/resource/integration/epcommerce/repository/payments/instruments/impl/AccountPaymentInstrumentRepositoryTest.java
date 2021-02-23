/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.CUSTOMER_PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.buildTestAddressDTO;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.buildTestAddressEntity;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.buildTestCustomerAddress;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildAccountPaymentInstrumentIdentifier;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.AccountPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentAttributesEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodsIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;

@SuppressWarnings({"PMD.TestClassWithoutTestCases"})
public class AccountPaymentInstrumentRepositoryTest extends AbstractPaymentInstrumentRepositoryTest {

	private static final long ORDER_UID = 101L;

	private AddressEntity addressEntity;

	@Before
	public void localSetup() {
		addressEntity = buildTestAddressEntity();
		CustomerAddress customerAddress = buildTestCustomerAddress();
		when(conversionService.convert(addressEntity, CustomerAddress.class)).thenReturn(customerAddress);
		when(conversionService.convert(customerAddress, AddressDTO.class)).thenReturn(buildTestAddressDTO());
		when(addressValidator.validate(addressEntity)).thenReturn(Completable.complete());
		when(customerRepository.createAddressForCustomer(account, customerAddress)).thenReturn(Single.just(customerAddress));
		when(resourceOperationContext.getResourceIdentifier())
				.thenReturn(createAccountPaymentInstrumentFormIdentifier());
		when(customerRepository.getCustomer(ACCOUNT_ID.getValue())).thenReturn(Single.just(account));
		when(account.getGuid()).thenReturn(ACCOUNT_ID.getValue());
		when(customerDefaultPaymentInstrumentRepository.hasDefaultPaymentInstrument(account)).thenReturn(Single.just(true));

	}

	@Test
	public void submitAccountPaymentInstrumentReturnsAppropriateSubmitResultAndCreatesCustomerPaymentInstrument() {
		when(account.getUidPk()).thenReturn(ORDER_UID);

		repository.submitAccountPaymentInstrument(SCOPE, createAccountPIFormEntity(false, false))
				.test()
				.assertNoErrors()
				.assertValue(getExpectedAccountPISubmitResult());

		verify(customerPaymentInstrument).setCustomerUid(ORDER_UID);
		verify(customerPaymentInstrument).setPaymentInstrumentGuid(PAYMENT_INSTRUMENT_ID);
		verify(customerPaymentInstrumentRepository).saveOrUpdate(customerPaymentInstrument);
		verify(customerDefaultPaymentInstrumentRepository, never()).saveAsDefault(any());
	}

	@Test
	public void submitAccountPaymentInstrumentReturnsAppropriateSubmitResultAndCreatesDefaultCustomerPaymentInstrumentIfThereIsNone() {
		when(customerDefaultPaymentInstrumentRepository.hasDefaultPaymentInstrument(account)).thenReturn(Single.just(false));

		repository.submitAccountPaymentInstrument(SCOPE, createAccountPIFormEntity(false, false))
				.test()
				.assertNoErrors()
				.assertValue(getExpectedAccountPISubmitResult());

		verify(customerPaymentInstrumentRepository).saveOrUpdate(customerPaymentInstrument);
		verify(customerDefaultPaymentInstrumentRepository).saveAsDefault(customerPaymentInstrument);
	}

	@Test
	public void submitAccountPaymentInstrumentOverwritesCustomerDefaultPaymentInstrument() {
		repository.submitAccountPaymentInstrument(SCOPE, createAccountPIFormEntity(true, false))
				.test()
				.assertNoErrors()
				.assertValue(getExpectedAccountPISubmitResult());

		verify(customerDefaultPaymentInstrumentRepository).saveAsDefault(customerPaymentInstrument);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void submitAccountPaymentInstrumentReturnsPaymentMethodNotFoundError() {
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.empty());
		repository.submitAccountPaymentInstrument(SCOPE, createAccountPIFormEntity(false, false));
	}

	@Test
	public void submitAccountPaymentInstrumentHandlesInternalExceptionCases() {
		when(orderPaymentApiRepository.createPI(any(), any(), any())).thenThrow(ResourceOperationFailure.notFound());

		repository.submitAccountPaymentInstrument(SCOPE, createAccountPIFormEntity(false, false))
				.test()
				.assertNoValues()
				.assertError(ResourceOperationFailure.notFound());

		verifyZeroInteractions(customerPaymentInstrumentRepository);
	}

	@Test
	public void submitAccountPaymentInstrumentNullDataReturnsAppropriateSubmitResult() {
		repository.submitAccountPaymentInstrument(SCOPE, PaymentInstrumentForFormEntity.builder().build())
				.test()
				.assertNoErrors()
				.assertValue(getExpectedAccountPISubmitResult());
	}

	@Test
	public void submitAccountPaymentInstrumentEmptyDataReturnsAppropriateSubmitResult() {
		repository.submitAccountPaymentInstrument(SCOPE,
				PaymentInstrumentForFormEntity.builder()
						.withPaymentInstrumentIdentificationForm(PaymentInstrumentAttributesEntity.builder().build())
						.build())
				.test()
				.assertNoErrors()
				.assertValue(getExpectedAccountPISubmitResult());
	}

	@Test
	public void submitAccountPaymentInstrumentWithBillingAddressReturnsAppropriateSubmitResult() {
		repository.submitAccountPaymentInstrument(SCOPE, createAccountPIFormEntity(false, true))
				.test()
				.assertNoErrors()
				.assertValue(SubmitResult.<AccountPaymentInstrumentIdentifier>builder()
						.withIdentifier(buildAccountPaymentInstrumentIdentifier(SCOPE,
								ACCOUNT_ID, StringIdentifier.of(CUSTOMER_PAYMENT_INSTRUMENT_ID)))
						.withStatus(SubmitStatus.CREATED)
						.build());
	}

	@Test
	public void submitAccountPaymentInstrumentWithInvalidBillingAddressFails() {
		ResourceOperationFailure invalidAddressFailure = ResourceOperationFailure.badRequestBody("Invalid address");

		when(addressValidator.validate(addressEntity)).thenReturn(Completable.error(invalidAddressFailure));

		repository.submitAccountPaymentInstrument(SCOPE, createAccountPIFormEntity(false, true))
				.test()
				.assertError(invalidAddressFailure);
	}

	private static Optional<ResourceIdentifier> createAccountPaymentInstrumentFormIdentifier() {
		return Optional.of(AccountPaymentInstrumentFormIdentifier.builder()
				.withAccountPaymentMethod(AccountPaymentMethodIdentifier.builder()
						.withAccountPaymentMethods(AccountPaymentMethodsIdentifier.builder()
								.withAccount(AccountIdentifier.builder()
										.withAccountId(ACCOUNT_ID)
										.withAccounts(AccountsIdentifier.builder()
												.withScope(SCOPE).build())
										.build())
								.build())
						.withAccountPaymentMethodId(STORE_PAYMENT_CONFIGURATION_ID)
						.build())
				.build());
	}

}
