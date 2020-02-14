/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.REVERSE_CHARGE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequestBuilder;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.service.payment.provider.PaymentProviderPluginForIntegrationTesting;

public class ReverseChargeProcessorImplTest extends AbstractProcessorImplTestBase {

	@Inject
	@Named(PaymentProviderApiContextIdNames.CREDIT_PROCESSOR)
	private CreditProcessor testee;

	@Test
	public void reverseChargeWithOneReserveEventPlusOneChargeEventAndReverseChargeCapabilityIsSupported() {
		final PaymentAPIResponse response = testee.reverseCharge(createReverseChargeRequest());

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(REVERSE_CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void reverseChargeWithOneReserveEventPlusOneChargeEventAndReverseChargeCapabilityIsUnsupportedAndCreditCapabilityIsSupported() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ReverseChargeCapability.class);

		final PaymentAPIResponse response = testee.reverseCharge(createReverseChargeRequest());

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(REVERSE_CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void reverseChargeWithOneReserveEventPlusOneChargeEventAndReverseChargeCapabilityIsUnsupportedAndCreditThrowsException() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), CreditCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ReverseChargeCapability.class);

		final PaymentAPIResponse response = testee.reverseCharge(createReverseChargeRequest());

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getInternalMessage()).isEqualTo("The request failed.");
		assertThat(response.getExternalMessage()).isEqualTo("The capability throws exception.");
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(REVERSE_CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(FAILED);
	}

	@Test
	public void reverseChargeWithOneReserveEventPlusOneChargeEventAndReverseChargeCapabilityIsUnsupportedAndCreditCapabilityIsUnsupported() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ReverseChargeCapability.class);
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), CreditCapability.class);

		final PaymentAPIResponse response = testee.reverseCharge(createReverseChargeRequest());

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getExternalMessage()).isEqualTo("The reverse charge request failed.");
		assertThat(response.getInternalMessage()).isEqualTo("The reverse charge process failed as there are no chargeable events or the reverse "
				+ "charge capability is not supported(has thrown exception) by the payment provider.");
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void reverseChargeWithOneReserveEventPlusOneChargeEventAndReverseChargeThrowsExceptionAndCreditCapabilityIsSupported() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReverseChargeCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});

		final PaymentAPIResponse response = testee.reverseCharge(createReverseChargeRequest());

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(REVERSE_CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED);
	}

	@Test
	public void reverseChargeWithOneReserveEventPlusOneChargeEventAndReverseChargeThrowsExceptionAndCreditThrowsException() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReverseChargeCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), CreditCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});

		final PaymentAPIResponse response = testee.reverseCharge(createReverseChargeRequest());

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getInternalMessage()).isEqualTo("The request failed.");
		assertThat(response.getExternalMessage()).isEqualTo("The capability throws exception.");
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(REVERSE_CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(FAILED);
	}

	@Test
	public void reverseChargeWithOneReserveEventPlusOneChargeEventAndReverseChargeThrowsExceptionAndCreditCapabilityIsUnsupported() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReverseChargeCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), CreditCapability.class);

		final PaymentAPIResponse response = testee.reverseCharge(createReverseChargeRequest());

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getExternalMessage()).isEqualTo("The reverse charge request failed.");
		assertThat(response.getInternalMessage()).isEqualTo("The reverse charge process failed as there are no chargeable events or the reverse "
				+ "charge capability is not supported(has thrown exception) by the payment provider.");
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void reverseChargeWithTwoReserveEventPlusTwoChargeEventAndReverseChargeCapabilityIsSupported() {
		final PaymentAPIResponse response =
				testee.reverseCharge(createReverseChargeRequestWithTwoReserveEventsAndTwoChargeEvents());

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN, TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(REVERSE_CHARGE, REVERSE_CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void reverseChargeWithTwoReserveEventPlusTwoChargeEventAndReverseChargeCapabilityIsUnsupportedAndCreditCapabilityIsSupported() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ReverseChargeCapability.class);

		final PaymentAPIResponse response =
				testee.reverseCharge(createReverseChargeRequestWithTwoReserveEventsAndTwoChargeEvents());

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN, TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(REVERSE_CHARGE, REVERSE_CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void reverseChargeWithTwoReserveEventPlusTwoChargeEventAndReverseChargeCapabilityIsUnsupportedAndCreditThrowsException() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), CreditCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ReverseChargeCapability.class);

		final PaymentAPIResponse response =
				testee.reverseCharge(createReverseChargeRequestWithTwoReserveEventsAndTwoChargeEvents());

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getInternalMessage()).isEqualTo("The request failed.");
		assertThat(response.getExternalMessage()).isEqualTo("The capability throws exception.");
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN, TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(REVERSE_CHARGE, REVERSE_CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(FAILED, FAILED);
	}

	@Test
	public void reverseChargeWithTwoReserveEventPlusTwoChargeEventAndReverseChargeCapabilityIsUnsupportedAndCreditCapabilityIsUnsupported() {
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), ReverseChargeCapability.class);
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), CreditCapability.class);

		final PaymentAPIResponse response =
				testee.reverseCharge(createReverseChargeRequestWithTwoReserveEventsAndTwoChargeEvents());

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getExternalMessage()).isEqualTo("The reverse charge request failed.");
		assertThat(response.getInternalMessage()).isEqualTo("The reverse charge process failed as there are no chargeable events or the reverse "
				+ "charge capability is not supported(has thrown exception) by the payment provider.");
		assertThat(response.getEvents()).isEmpty();
	}

	@Test
	public void reverseChargeWithTwoReserveEventPlusTwoChargeEventAndReverseChargeThrowsExceptionAndCreditCapabilityIsSupported() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReverseChargeCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});

		final PaymentAPIResponse response =
				testee.reverseCharge(createReverseChargeRequestWithTwoReserveEventsAndTwoChargeEvents());

		assertThat(response.isSuccess()).isEqualTo(true);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN, TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(REVERSE_CHARGE, REVERSE_CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(APPROVED, APPROVED);
	}

	@Test
	public void reverseChargeWithTwoReserveEventPlusTwoChargeEventAndReverseChargeThrowsExceptionAndCreditThrowsException() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReverseChargeCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), CreditCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});

		final PaymentAPIResponse response =
				testee.reverseCharge(createReverseChargeRequestWithTwoReserveEventsAndTwoChargeEvents());

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getInternalMessage()).isEqualTo("The request failed.");
		assertThat(response.getExternalMessage()).isEqualTo("The capability throws exception.");
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getAmount)
				.extracting(MoneyDTO::getAmount)
				.containsExactly(TEN, TEN);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentType)
				.containsExactly(REVERSE_CHARGE, REVERSE_CHARGE);
		assertThat(response.getEvents())
				.extracting(PaymentEvent::getPaymentStatus)
				.containsExactly(FAILED, FAILED);
	}

	@Test
	public void reverseChargeWithTwoReserveEventPlusTwoChargeEventAndReverseChargeThrowsExceptionAndCreditCapabilityIsUnsupported() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReverseChargeCapability.class, request -> {
			throw CAPABILITY_EXCEPTION;
		});
		PaymentProviderPluginForIntegrationTesting.removeCapability(getClass(), CreditCapability.class);

		final PaymentAPIResponse response =
				testee.reverseCharge(createReverseChargeRequestWithTwoReserveEventsAndTwoChargeEvents());

		assertThat(response.isSuccess()).isEqualTo(false);
		assertThat(response.getExternalMessage()).isEqualTo("The reverse charge request failed.");
		assertThat(response.getInternalMessage()).isEqualTo("The reverse charge process failed as there are no chargeable events or the reverse "
				+ "charge capability is not supported(has thrown exception) by the payment provider.");
		assertThat(response.getEvents()).isEmpty();
	}

	private ReverseChargeRequest createReverseChargeRequestWithTwoReserveEventsAndTwoChargeEvents() {
		PaymentEvent firstReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent firstChargeEvent = createChargeEvent(firstReservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent secondReservationEvent = createReservationEvent(createOrderPaymentInstrumentDTO(), DEFAULT_RESERVED_AMOUNT_10_USD);
		PaymentEvent secondChargeEvent = createChargeEvent(secondReservationEvent, DEFAULT_RESERVED_AMOUNT_10_USD);
		return ReverseChargeRequestBuilder.builder()
				.withOrderPaymentInstruments(Arrays.asList(firstReservationEvent.getOrderPaymentInstrumentDTO(),
						secondReservationEvent.getOrderPaymentInstrumentDTO()))
				.withSelectedPaymentEvents(Arrays.asList(firstChargeEvent, secondChargeEvent))
				.withLedger(ImmutableList.of(firstReservationEvent, firstChargeEvent, secondReservationEvent, secondChargeEvent))
				.withCustomRequestData(Collections.emptyMap())
				.withOrderContext(createOrderContext())
				.build(getBeanFactory());
	}
}