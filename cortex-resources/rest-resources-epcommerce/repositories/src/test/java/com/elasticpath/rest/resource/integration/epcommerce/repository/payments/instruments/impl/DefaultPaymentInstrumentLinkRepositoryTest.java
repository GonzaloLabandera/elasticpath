/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;

/**
 * Tests for {@link com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.DefaultPaymentInstrumentLinkRepository}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultPaymentInstrumentLinkRepositoryTest {

	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("MOBEE");
	private static final IdentifierPart<String> CUSTOMER_ID = StringIdentifier.of("CUSTOMER_ID");
	private static final IdentifierPart<String> CUSTOMER_PAYMENT_INSTRUMENT_ID = StringIdentifier.of("CUSTOMER_PAYMENT_INSTRUMENT_ID");
	private static final String OTHER_CUSTOMER = "other customer";

	@InjectMocks
	private DefaultPaymentInstrumentLinkRepositoryImpl repository;

	@Mock
	private ResourceOperationContext resourceOperationContext;
	@Mock
	private CustomerRepository customerRepository;
	@Mock
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;
	@Mock
	private ReactiveAdapterImpl reactiveAdapter;

	private final Customer customer = mock(Customer.class);
	private final CustomerPaymentInstrument customerPaymentInstrument = mock(CustomerPaymentInstrument.class);

	@Before
	public void setUp() {
		final Subject subject = mock(Subject.class);
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(customerRepository.getCustomerGuid(OTHER_CUSTOMER, subject)).thenReturn(CUSTOMER_ID.getValue());
		when(resourceOperationContext.getUserIdentifier()).thenReturn(CUSTOMER_ID.getValue());
		when(customerRepository.getCustomer(CUSTOMER_ID.getValue())).thenReturn(Single.just(customer));
		when(customerPaymentInstrument.getGuid()).thenReturn(CUSTOMER_PAYMENT_INSTRUMENT_ID.getValue());
		when(reactiveAdapter.fromService(any())).thenCallRealMethod();
		when(reactiveAdapter.fromServiceAsMaybe(any())).thenCallRealMethod();
	}

	@Test
	public void getDefaultPaymentInstrumentIdentifierReturnsExpectedResult() {
		when(filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(customer, SCOPE.getValue()))
				.thenReturn(customerPaymentInstrument);

		repository.getDefaultPaymentInstrumentIdentifier(createPaymentInstrumentsIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(getExpectedPaymentInstrumentIdentifier());
	}

	@Test
	public void getContextAwareDefaultPaymentInstrumentIdentifierReturnsExpectedResult() {
		when(resourceOperationContext.getUserIdentifier()).thenReturn(OTHER_CUSTOMER);
		when(filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(customer, SCOPE.getValue()))
				.thenReturn(customerPaymentInstrument);

		repository.getContextAwareDefaultPaymentInstrumentIdentifier(createPaymentInstrumentsIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(getExpectedPaymentInstrumentIdentifier());
	}

	@Test
	public void getDefaultPaymentInstrumentIdentifierReturnsEmptyResultWhenDefaultPaymentInstrumentIsNotSet() {
		repository.getDefaultPaymentInstrumentIdentifier(createPaymentInstrumentsIdentifier())
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}

	@Test
	public void getDefaultPaymentInstrumentIdentifierReturnsExceptionWhenCustomerNotFound() {
		when(customerRepository.getCustomer(CUSTOMER_ID.getValue()))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));

		repository.getDefaultPaymentInstrumentIdentifier(createPaymentInstrumentsIdentifier())
				.test()
				.assertNoValues()
				.assertError(ResourceOperationFailure.notFound());
	}

	private PaymentInstrumentIdentifier getExpectedPaymentInstrumentIdentifier() {
		return PaymentInstrumentIdentifier.builder()
				.withPaymentInstrumentId(CUSTOMER_PAYMENT_INSTRUMENT_ID)
				.withPaymentInstruments(createPaymentInstrumentsIdentifier())
				.build();
	}

	private PaymentInstrumentsIdentifier createPaymentInstrumentsIdentifier() {
		return PaymentInstrumentsIdentifier.builder()
				.withScope(SCOPE)
				.build();
	}
}
