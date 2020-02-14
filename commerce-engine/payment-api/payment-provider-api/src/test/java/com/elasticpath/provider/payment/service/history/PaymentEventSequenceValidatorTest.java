/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CANCEL_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CHARGE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.CREDIT;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.MANUAL_CREDIT;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.MODIFY_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.REVERSE_CHARGE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.validator.PaymentEventSequenceValidator;

/**
 * Tests for {@link PaymentEventSequenceValidator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentEventSequenceValidatorTest {
	private final Map<TransactionType, List<TransactionType>> mapTransactionTypes = setUpMapTransactionTypes();

	private final PaymentEventSequenceValidator paymentEventSequenceValidator = new PaymentEventSequenceValidator(mapTransactionTypes);

	@Test
	public void validateFirstEventInSequenceMustBeReserve() {

		assertThatThrownBy(() -> paymentEventSequenceValidator.validate(
				singletonList(mockPaymentEvent(CHARGE, APPROVED))))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void validateReverseChargeIsLegal() {
		final PaymentEvent reserveSeq1 = mockPaymentEvent(RESERVE, APPROVED);
		final PaymentEvent chargeSeq1 = mockPaymentEvent(CHARGE, APPROVED);
		final PaymentEvent reverseChargeSeq1 = mockPaymentEvent(REVERSE_CHARGE, APPROVED);

		assertThatCode(() -> paymentEventSequenceValidator.validate(asList(reserveSeq1, chargeSeq1, reverseChargeSeq1)))
				.doesNotThrowAnyException();
	}

	@Test
	public void validateMultipleModificationsAreLegal() {
		final PaymentEvent reserveSeq1 = mockPaymentEvent(RESERVE, APPROVED);
		final PaymentEvent modify1Seq1 = mockPaymentEvent(MODIFY_RESERVE, APPROVED);
		final PaymentEvent modify2Seq1 = mockPaymentEvent(MODIFY_RESERVE, APPROVED);
		final PaymentEvent chargeSeq1 = mockPaymentEvent(CHARGE, APPROVED);

		assertThatCode(() -> paymentEventSequenceValidator.validate(asList(reserveSeq1, modify1Seq1, modify2Seq1, chargeSeq1)))
				.doesNotThrowAnyException();
	}

	@Test
	public void validateCreditsAreLegal() {
		final PaymentEvent reserveSeq1 = mockPaymentEvent(RESERVE, APPROVED);
		final PaymentEvent modifySeq1 = mockPaymentEvent(MODIFY_RESERVE, APPROVED);
		final PaymentEvent chargeSeq1 = mockPaymentEvent(CHARGE, APPROVED);
		final PaymentEvent creditSeq1 = mockPaymentEvent(CREDIT, APPROVED);
		final PaymentEvent manualCreditSeq1 = mockPaymentEvent(MANUAL_CREDIT, APPROVED);

		assertThatCode(() -> paymentEventSequenceValidator.validate(asList(reserveSeq1, modifySeq1, chargeSeq1, creditSeq1, manualCreditSeq1)))
				.doesNotThrowAnyException();
	}

	@Test
	public void validateCancelReserveAfterModifyIsLegal() {
		final PaymentEvent reserveSeq1 = mockPaymentEvent(RESERVE, APPROVED);
		final PaymentEvent modifySeq1 = mockPaymentEvent(MODIFY_RESERVE, APPROVED);
		final PaymentEvent cancelSeq1 = mockPaymentEvent(CANCEL_RESERVE, APPROVED);

		assertThatCode(() -> paymentEventSequenceValidator.validate(asList(reserveSeq1, modifySeq1, cancelSeq1)))
				.doesNotThrowAnyException();
	}

	@Test
	public void validateShouldThrowExceptionWhenEventSequenceContainsDoubleReserve() {
		final PaymentEvent reserve1Seq1 = mockPaymentEvent(RESERVE, APPROVED);
		final PaymentEvent reserve2Seq1 = mockPaymentEvent(RESERVE, APPROVED);
		final PaymentEvent chargeSeq1 = mockPaymentEvent(CHARGE, APPROVED);

		assertThatThrownBy(() -> paymentEventSequenceValidator.validate(asList(reserve1Seq1, reserve2Seq1, chargeSeq1)))
				.isInstanceOf(IllegalStateException.class);
	}

	private PaymentEvent mockPaymentEvent(final TransactionType transactionType,
										  final PaymentStatus paymentStatus) {
		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getPaymentType()).thenReturn(transactionType);
		when(paymentEvent.getPaymentStatus()).thenReturn(paymentStatus);
		return paymentEvent;
	}

	private Map<TransactionType, List<TransactionType>> setUpMapTransactionTypes() {
		Map<TransactionType, List<TransactionType>> mapTransactionTypes = new HashMap<>();
		mapTransactionTypes.put(RESERVE, asList(MODIFY_RESERVE, CANCEL_RESERVE, CHARGE));
		mapTransactionTypes.put(MODIFY_RESERVE, asList(MODIFY_RESERVE, CANCEL_RESERVE, CHARGE));
		mapTransactionTypes.put(CANCEL_RESERVE, Collections.emptyList());
		mapTransactionTypes.put(CHARGE, asList(REVERSE_CHARGE, CREDIT, MANUAL_CREDIT));
		mapTransactionTypes.put(REVERSE_CHARGE, Collections.emptyList());
		mapTransactionTypes.put(CREDIT, asList(CREDIT, MANUAL_CREDIT));
		mapTransactionTypes.put(MANUAL_CREDIT, asList(CREDIT, MANUAL_CREDIT));
		return mapTransactionTypes;
	}
}
