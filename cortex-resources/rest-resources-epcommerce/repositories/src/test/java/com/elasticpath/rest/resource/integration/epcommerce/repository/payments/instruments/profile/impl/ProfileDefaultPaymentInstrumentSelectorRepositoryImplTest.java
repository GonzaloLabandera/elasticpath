/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.profile.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.ProfileDefaultPaymentInstrumentSelectorIdentifier;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerDefaultPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.CustomerPaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.rest.selector.Choice;
import com.elasticpath.rest.selector.ChoiceStatus;
import com.elasticpath.rest.selector.SelectStatus;
import com.elasticpath.rest.selector.SelectorChoice;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;

/**
 * Tests for {@link ProfileDefaultPaymentInstrumentSelectorRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileDefaultPaymentInstrumentSelectorRepositoryImplTest {

	private static final String STORE_CODE = "STORE_CODE";
	private static final String USER_IDENTIFIER = "USER_IDENTIFIER";
	private static final String DEFAULT_PAYMENT_INSTRUMENT = "DEFAULT_PAYMENT_INSTRUMENT";
	private static final String OLD_DEFAULT_PAYMENT_INSTRUMENT = "OLD_DEFAULT_PAYMENT_INSTRUMENT";
	private static final String CHOOSABLE_PAYMENT_INSTRUMENT = "CHOOSABLE_PAYMENT_INSTRUMENT";

	@Mock
	private CustomerPaymentInstrumentRepository customerPaymentInstrumentRepository;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private PaymentInstrumentRepository paymentInstrumentRepository;

	@Mock
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;

	@Mock
	private CustomerDefaultPaymentInstrumentRepository customerDefaultPaymentInstrumentRepository;

	@Mock
	private ExceptionTransformer exceptionTransformer;

	private Customer customer;
	private CustomerPaymentInstrument defaultCustomerPaymentInstrument;
	private CustomerPaymentInstrument oldDefaultCustomerPaymentInstrument;

	@InjectMocks
	private ProfileDefaultPaymentInstrumentSelectorRepositoryImpl<ProfileDefaultPaymentInstrumentSelectorIdentifier,
			ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier> selectorRepository;

	/**
	 * Setup test environment.
	 */
	@Before
	public void setUp() {
		customer = mock(Customer.class);

		defaultCustomerPaymentInstrument = mock(CustomerPaymentInstrument.class);
		when(defaultCustomerPaymentInstrument.getGuid()).thenReturn(DEFAULT_PAYMENT_INSTRUMENT);
		when(defaultCustomerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(DEFAULT_PAYMENT_INSTRUMENT);

		oldDefaultCustomerPaymentInstrument = mock(CustomerPaymentInstrument.class);
		when(oldDefaultCustomerPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(OLD_DEFAULT_PAYMENT_INSTRUMENT);

		final PaymentInstrumentIdentifier choosableInstrumentIdentifier = createPaymentInstrument(CHOOSABLE_PAYMENT_INSTRUMENT);
		final PaymentInstrumentIdentifier defaultInstrumentIdentifier = createPaymentInstrument(DEFAULT_PAYMENT_INSTRUMENT);

		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_IDENTIFIER);
		when(customerRepository.getCustomer(USER_IDENTIFIER)).thenReturn(Single.just(customer));

		when(filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(customer, STORE_CODE))
				.thenReturn(defaultCustomerPaymentInstrument);

		when(paymentInstrumentRepository.findAll(StringIdentifier.of(STORE_CODE))).thenReturn(Observable.fromIterable(
				Arrays.asList(choosableInstrumentIdentifier, defaultInstrumentIdentifier)));

		selectorRepository.setReactiveAdapter(new ReactiveAdapterImpl(exceptionTransformer));
	}

	private PaymentInstrumentIdentifier createPaymentInstrument(final String choosablePaymentInstrument) {
		return PaymentInstrumentIdentifier.builder()
				.withPaymentInstruments(createPaymentInstrumentsIdentifier())
				.withPaymentInstrumentId(StringIdentifier.of(choosablePaymentInstrument))
				.build();
	}

	/**
	 * Test that getChoices return correct chosen payment instrument and correct choosable payment instrument.
	 */
	@Test
	public void testGetChoicesReturnsPaymentInstrumentXChosenAndPaymentInstrumentYChoosable() {
		selectorRepository.getChoices(createProfileDefaultPaymentInstrumentSelectorIdentifier())
				.test()
				.assertNoErrors()
				.assertValueCount(2)
				.assertValueAt(0, selectorChoiceOf(CHOOSABLE_PAYMENT_INSTRUMENT, ChoiceStatus.CHOOSABLE))
				.assertValueAt(1, selectorChoiceOf(DEFAULT_PAYMENT_INSTRUMENT, ChoiceStatus.CHOSEN));
	}

	/**
	 * Test that getChoices return choosable payment instrument even if nothing is chosen.
	 */
	@Test
	public void testGetChoicesReturnsChoosablePaymentInstrumentsWhenNothingIsChosen() {
		when(filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(customer, STORE_CODE)).thenReturn(null);
		final PaymentInstrumentIdentifier choosableInstrumentIdentifier = createPaymentInstrument(CHOOSABLE_PAYMENT_INSTRUMENT);
		when(paymentInstrumentRepository.findAll(StringIdentifier.of(STORE_CODE))).thenReturn(Observable.just(choosableInstrumentIdentifier));

		selectorRepository.getChoices(createProfileDefaultPaymentInstrumentSelectorIdentifier())
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(selectorChoiceOf(CHOOSABLE_PAYMENT_INSTRUMENT, ChoiceStatus.CHOOSABLE));
	}

	/**
	 * Test that getChoice return correct chosen payment instrument.
	 */
	@Test
	public void testGetChoiceDefaultPaymentInstrumentIsChosen() {
		ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier selectorChoiceIdentifier =
				createProfileDefaultPaymentInstrumentSelectorChoiceIdentifier();

		selectorRepository.getChoice(selectorChoiceIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choiceOf(DEFAULT_PAYMENT_INSTRUMENT, selectorChoiceIdentifier, ChoiceStatus.CHOOSABLE));
	}

	/**
	 * Test that selectChoice select default payment instrument and return the existing status.
	 */
	@Test
	public void testSelectChoiceSelectDefaultPaymentInstrumentAndReturnTheExistingStatus() {
		selectorRepository.selectChoice(createProfileDefaultPaymentInstrumentSelectorChoiceIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(result -> result.getStatus() == SelectStatus.EXISTING)
				.assertValue(result -> result.getIdentifier().getPaymentInstruments().getScope().getValue().equals(STORE_CODE));

		verify(customerPaymentInstrumentRepository, never()).findByGuid(any());
		verify(customerDefaultPaymentInstrumentRepository, never()).saveAsDefault(any());
	}

	/**
	 * Test that selectChoice select new default payment instrument and return the selected status.
	 */
	@Test
	public void testSelectChoiceSelectNewDefaultPaymentInstrumentAndReturnTheSelectedStatus() {
		when(filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(customer, STORE_CODE))
				.thenReturn(oldDefaultCustomerPaymentInstrument);
		when(customerPaymentInstrumentRepository.findByGuid(DEFAULT_PAYMENT_INSTRUMENT)).thenReturn(Single.just(defaultCustomerPaymentInstrument));
		when(customerDefaultPaymentInstrumentRepository.saveAsDefault(defaultCustomerPaymentInstrument)).thenReturn(Completable.complete());

		selectorRepository.selectChoice(createProfileDefaultPaymentInstrumentSelectorChoiceIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(result -> result.getStatus() == SelectStatus.SELECTED)
				.assertValue(result -> result.getIdentifier().getPaymentInstruments().getScope().getValue().equals(STORE_CODE));

		verify(customerPaymentInstrumentRepository).findByGuid(DEFAULT_PAYMENT_INSTRUMENT);
		verify(customerDefaultPaymentInstrumentRepository).saveAsDefault(defaultCustomerPaymentInstrument);
	}

	private static ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier createProfileDefaultPaymentInstrumentSelectorChoiceIdentifier() {
		return ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier.builder()
				.withPaymentInstrument(createPaymentInstrumentIdentifier())
				.withProfileDefaultPaymentInstrumentSelector(createProfileDefaultPaymentInstrumentSelectorIdentifier())
				.build();
	}

	private static PaymentInstrumentIdentifier createPaymentInstrumentIdentifier() {
		return PaymentInstrumentIdentifier.builder()
				.withPaymentInstruments(createPaymentInstrumentsIdentifier())
				.withPaymentInstrumentId(StringIdentifier.of(DEFAULT_PAYMENT_INSTRUMENT))
				.build();
	}

	private static ProfileDefaultPaymentInstrumentSelectorIdentifier createProfileDefaultPaymentInstrumentSelectorIdentifier() {
		return ProfileDefaultPaymentInstrumentSelectorIdentifier.builder()
				.withPaymentInstruments(createPaymentInstrumentsIdentifier())
				.build();
	}

	private static PaymentInstrumentsIdentifier createPaymentInstrumentsIdentifier() {
		return PaymentInstrumentsIdentifier.builder()
				.withScope(StringIdentifier.of(STORE_CODE))
				.build();
	}

	private Predicate<SelectorChoice> selectorChoiceOf(final String instrumentId, final ChoiceStatus choiceStatus) {
		return selectorChoice -> {
			final ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier choiceIdentifier =
					(ProfileDefaultPaymentInstrumentSelectorChoiceIdentifier) selectorChoice.getChoice();
			assertThat(choiceIdentifier.getProfileDefaultPaymentInstrumentSelector())
					.isEqualTo(createProfileDefaultPaymentInstrumentSelectorIdentifier());
			assertThat(choiceIdentifier.getPaymentInstrument().getPaymentInstrumentId().getValue()).isEqualTo(instrumentId);
			assertThat(selectorChoice.getStatus()).isEqualTo(choiceStatus);
			return true;
		};
	}

	private Predicate<Choice> choiceOf(final String instrumentGuid, final ResourceIdentifier action, final ChoiceStatus choiceStatus) {
		return choice -> {
			assertThat(choice.getAction()).isEqualTo(Optional.ofNullable(action));
			assertThat(choice.getStatus()).isEqualTo(choiceStatus);

			final PaymentInstrumentIdentifier paymentInstrumentIdentifier = (PaymentInstrumentIdentifier) choice.getDescription();
			assertThat(paymentInstrumentIdentifier.getPaymentInstrumentId().getValue()).isEqualTo(instrumentGuid);
			assertThat(paymentInstrumentIdentifier.getPaymentInstruments()).isEqualTo(createPaymentInstrumentsIdentifier());

			return true;
		};
	}
}
