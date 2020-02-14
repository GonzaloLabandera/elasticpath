/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.CUSTOMER_PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.PAYMENT_INSTRUMENT_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.buildTestAddressDTO;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.buildTestAddressEntity;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.PaymentsTestHelpers.buildTestCustomerAddress;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.commons.PaymentResourceHelpers.buildPaymentInstrumentIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl.PaymentInstrumentRepositoryImpl.PAYMENT_METHOD_IS_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import io.reactivex.Completable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentAttributesEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.type.StringIdentifier;

@SuppressWarnings({"PMD.TestClassWithoutTestCases"})
public class ProfilePaymentInstrumentRepositoryTest extends AbstractPaymentInstrumentRepositoryTest {

	private static final long ORDER_UID = 101L;

	private AddressEntity addressEntity;

	@Before
	public void localSetup() {
		addressEntity = buildTestAddressEntity();
		CustomerAddress customerAddress = buildTestCustomerAddress();
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.of(createProfilePIFormIdentifier()));
		when(conversionService.convert(addressEntity, CustomerAddress.class)).thenReturn(customerAddress);
		when(conversionService.convert(customerAddress, AddressDTO.class)).thenReturn(buildTestAddressDTO());
		when(addressValidator.validate(addressEntity)).thenReturn(Completable.complete());
		when(customerRepository.createAddressForCustomer(customer, customerAddress)).thenReturn(Single.just(customerAddress));
	}

	@Test
	public void submitProfilePaymentInstrumentReturnsAppropriateSubmitResultAndCreatesCustomerPaymentInstrument() {
		when(customer.getUidPk()).thenReturn(ORDER_UID);

        repository.submitProfilePaymentInstrument(SCOPE, createProfilePIFormEntity(false, false))
                .test()
                .assertNoErrors()
                .assertValue(getExpectedProfilePISubmitResult());

		verify(customerPaymentInstrument).setCustomerUid(ORDER_UID);
		verify(customerPaymentInstrument).setPaymentInstrumentGuid(PAYMENT_INSTRUMENT_ID);
        verify(customerPaymentInstrumentRepository).saveOrUpdate(customerPaymentInstrument);
        verify(customerDefaultPaymentInstrumentRepository, never()).saveAsDefault(any());
    }

	@Test
	public void submitProfilePaymentInstrumentReturnsAppropriateSubmitResultAndCreatesDefaultCustomerPaymentInstrumentIfThereIsNone() {
		when(customerDefaultPaymentInstrumentRepository.hasDefaultPaymentInstrument(customer)).thenReturn(Single.just(false));

		repository.submitProfilePaymentInstrument(SCOPE, createProfilePIFormEntity(false, false))
				.test()
				.assertNoErrors()
				.assertValue(getExpectedProfilePISubmitResult());

		verify(customerPaymentInstrumentRepository).saveOrUpdate(customerPaymentInstrument);
		verify(customerDefaultPaymentInstrumentRepository).saveAsDefault(customerPaymentInstrument);
	}

	@Test
	public void submitProfilePaymentInstrumentOverwritesCustomerDefaultPaymentInstrument() {
		repository.submitProfilePaymentInstrument(SCOPE, createProfilePIFormEntity(true, false))
				.test()
				.assertNoErrors()
				.assertValue(getExpectedProfilePISubmitResult());

		verify(customerDefaultPaymentInstrumentRepository).saveAsDefault(customerPaymentInstrument);
	}

	@Test
	public void submitProfilePaymentInstrumentReturnsPaymentMethodNotFoundError() {
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.empty());

		repository.submitProfilePaymentInstrument(SCOPE, createProfilePIFormEntity(false, false))
				.test()
				.assertError(ResourceOperationFailure.notFound(PAYMENT_METHOD_IS_NOT_FOUND));
	}

	@Test
	public void submitProfilePaymentInstrumentHandlesInternalExceptionCases() {
		when(orderPaymentApiRepository.createPI(any(), any(), any())).thenThrow(ResourceOperationFailure.notFound());

		repository.submitProfilePaymentInstrument(SCOPE, createProfilePIFormEntity(false, false))
				.test()
				.assertNoValues()
				.assertError(ResourceOperationFailure.notFound());

		verifyZeroInteractions(customerPaymentInstrumentRepository);
	}

	@Test
	public void submitProfilePaymentInstrumentNullDataReturnsAppropriateSubmitResult() {
		repository.submitProfilePaymentInstrument(SCOPE, PaymentInstrumentForFormEntity.builder().build())
				.test()
				.assertNoErrors()
				.assertValue(getExpectedProfilePISubmitResult());
	}

	@Test
	public void submitProfilePaymentInstrumentEmptyDataReturnsAppropriateSubmitResult() {
		repository.submitProfilePaymentInstrument(SCOPE,
				PaymentInstrumentForFormEntity.builder()
						.withPaymentInstrumentIdentificationForm(PaymentInstrumentAttributesEntity.builder().build())
						.build())
				.test()
				.assertNoErrors()
				.assertValue(getExpectedProfilePISubmitResult());
	}

	@Test
	public void getPaymentInstrumentCreationFieldsForProviderConfigGuidReturnsExpectedCreationFields() {
		final PaymentInstrumentCreationFieldsDTO picFields = new PaymentInstrumentCreationFieldsDTO();
		picFields.setFields(ImmutableList.of("Field 1", "Field 2"));
		picFields.setStructuredErrorMessages(Collections.emptyList());
		picFields.setSaveable(true);

		when(orderPaymentApiRepository.getPICFields(eq(PAYMENT_CONFIGURATION_ID.getValue()), any(PICFieldsRequestContext.class)))
				.thenReturn(Single.just(picFields));

		repository.getPaymentInstrumentCreationFieldsForProviderConfigGuid(STORE_PAYMENT_CONFIGURATION_ID.getValue())
				.test()
				.assertNoErrors()
				.assertValue(picFields);
	}

	@Test
	public void submitProfilePaymentInstrumentWithBillingAddressReturnsAppropriateSubmitResult() {
		repository.submitProfilePaymentInstrument(SCOPE, createProfilePIFormEntity(false, true))
				.test()
				.assertNoErrors()
				.assertValue(SubmitResult.<PaymentInstrumentIdentifier>builder()
						.withIdentifier(buildPaymentInstrumentIdentifier(SCOPE, StringIdentifier.of(CUSTOMER_PAYMENT_INSTRUMENT_ID)))
						.withStatus(SubmitStatus.CREATED)
						.build());
	}

	@Test
	public void submitProfilePaymentInstrumentWithInvalidBillingAddressFails() {
		ResourceOperationFailure invalidAddressFailure = ResourceOperationFailure.badRequestBody("Invalid address");

		when(addressValidator.validate(addressEntity)).thenReturn(Completable.error(invalidAddressFailure));

		repository.submitProfilePaymentInstrument(SCOPE, createProfilePIFormEntity(false, true))
				.test()
				.assertError(invalidAddressFailure);
	}
}
