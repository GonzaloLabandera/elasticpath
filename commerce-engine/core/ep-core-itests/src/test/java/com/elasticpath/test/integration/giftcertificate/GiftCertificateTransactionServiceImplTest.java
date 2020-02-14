/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.test.integration.giftcertificate;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.domain.payment.GiftCertificateTransaction;
import com.elasticpath.money.Money;
import com.elasticpath.service.catalog.GiftCertificateService;
import com.elasticpath.service.giftcertificate.GiftCertificateTransactionException;
import com.elasticpath.service.giftcertificate.GiftCertificateTransactionService;
import com.elasticpath.service.giftcertificate.impl.GiftCertificateTransactionResponse;
import com.elasticpath.test.db.DbTestCase;

public class GiftCertificateTransactionServiceImplTest extends DbTestCase {

	private static final String GIFT_CERTIFICATE_CODE = "GC0123456789XXXABC";
	private static final Currency CURRENCY = Currency.getInstance("CAD");
	private static final Money GC_AMOUNT_MONEY = Money.valueOf(BigDecimal.valueOf(20), CURRENCY);
	private static final Money AUTHORIZED_AMOUNT_MONEY = Money.valueOf(BigDecimal.valueOf(7), CURRENCY);
	private static final Money MODIFIED_AMOUNT_MONEY = Money.valueOf(BigDecimal.valueOf(5), CURRENCY);
	private static final Money CAPTURED_AMOUNT_MONEY = Money.valueOf(BigDecimal.valueOf(5), CURRENCY);
	private static final Money REFUND_AMOUNT_MONEY = Money.valueOf(BigDecimal.valueOf(2), CURRENCY);
	private static final Money ONE_DOLLAR = Money.valueOf(BigDecimal.ONE, CURRENCY);

	@Autowired
	private GiftCertificateTransactionService testee;

	@Autowired
	private GiftCertificateService giftCertificateService;

	private GiftCertificate giftCertificate;

	@Before
	public void setUp() throws Exception {
		giftCertificate = createAndPersistGiftCertificate();
	}

	@Test
	public void preAuthorizeDecreasesBalance() {
		final GiftCertificateTransactionResponse response = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final BigDecimal balance = testee.getBalance(giftCertificate);

		assertThat(response.getGiftCertificateCode()).isEqualTo(GIFT_CERTIFICATE_CODE);
		assertThat(balance).isEqualTo(GC_AMOUNT_MONEY.getAmount().subtract(AUTHORIZED_AMOUNT_MONEY.getAmount()));
	}

	@Test
	public void preAuthorizeRecordsTransaction() {
		testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final List<GiftCertificateTransaction> transactions = testee.getGiftCertificateTransactions(giftCertificate);
		final BigDecimal transactionBalance = testee.calcTransactionBalance(transactions);

		assertThat(transactions)
				.filteredOn(transaction -> transaction.getTransactionType().equals("Authorization"))
				.extracting(GiftCertificateTransaction::getAmount)
				.hasOnlyOneElementSatisfying(amount -> assertThat(amount).isEqualByComparingTo(AUTHORIZED_AMOUNT_MONEY.getAmount()));
		assertThat(transactionBalance).isEqualTo(AUTHORIZED_AMOUNT_MONEY.getAmount());
	}

	@Test
	public void reversePreAuthorizationRestoresBalance() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		testee.reversePreAuthorization(giftCertificate, preAuthorizeResponse.getAuthorizationCode(), AUTHORIZED_AMOUNT_MONEY);
		final BigDecimal balance = testee.getBalance(giftCertificate);

		assertThat(balance).isEqualTo(GC_AMOUNT_MONEY.getAmount());
	}

	@Test
	public void reversePreAuthorizationRecordsTransaction() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		testee.reversePreAuthorization(giftCertificate, preAuthorizeResponse.getAuthorizationCode(), AUTHORIZED_AMOUNT_MONEY);
		final List<GiftCertificateTransaction> transactions = testee.getGiftCertificateTransactions(giftCertificate);
		final BigDecimal transactionBalance = testee.calcTransactionBalance(transactions);

		assertThat(transactions)
				.filteredOn(transaction -> transaction.getTransactionType().equals("Authorization Reversal"))
				.extracting(GiftCertificateTransaction::getAmount)
				.hasOnlyOneElementSatisfying(amount -> assertThat(amount).isEqualByComparingTo(AUTHORIZED_AMOUNT_MONEY.getAmount()));
		assertThat(transactionBalance).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	public void modifyPreAuthorizationModifiesBalance() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		testee.modifyPreAuthorization(giftCertificate, preAuthorizeResponse.getAuthorizationCode(), MODIFIED_AMOUNT_MONEY);
		final BigDecimal balance = testee.getBalance(giftCertificate);

		assertThat(balance).isEqualTo(GC_AMOUNT_MONEY.getAmount().subtract(MODIFIED_AMOUNT_MONEY.getAmount()));
	}

	@Test
	public void modifyPreAuthorizationDoesNotRecordTransaction() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		testee.modifyPreAuthorization(giftCertificate, preAuthorizeResponse.getAuthorizationCode(), MODIFIED_AMOUNT_MONEY);
		final List<GiftCertificateTransaction> transactions = testee.getGiftCertificateTransactions(giftCertificate);
		final BigDecimal transactionBalance = testee.calcTransactionBalance(transactions);

		assertThat(transactions)
				.filteredOn(transaction -> transaction.getTransactionType().equals("Authorization"))
				.extracting(GiftCertificateTransaction::getAmount)
				.hasOnlyOneElementSatisfying(amount -> assertThat(amount).isEqualByComparingTo(MODIFIED_AMOUNT_MONEY.getAmount()));
		assertThat(transactionBalance).isEqualTo(MODIFIED_AMOUNT_MONEY.getAmount());
	}

	@Test
	public void captureModifiesBalance() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		final GiftCertificateTransactionResponse response = testee.capture(giftCertificate, authorizationCode, CAPTURED_AMOUNT_MONEY);
		final BigDecimal balance = testee.getBalance(giftCertificate);

		assertThat(response.getGiftCertificateCode()).isEqualTo(GIFT_CERTIFICATE_CODE);
		assertThat(response.getAuthorizationCode()).isEqualTo(authorizationCode);
		assertThat(balance).isEqualTo(GC_AMOUNT_MONEY.getAmount().subtract(CAPTURED_AMOUNT_MONEY.getAmount()));
	}

	@Test
	public void captureRecordsTransaction() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		testee.capture(giftCertificate, preAuthorizeResponse.getAuthorizationCode(), CAPTURED_AMOUNT_MONEY);
		final List<GiftCertificateTransaction> transactions = testee.getGiftCertificateTransactions(giftCertificate);
		final BigDecimal transactionBalance = testee.calcTransactionBalance(transactions);

		assertThat(transactions)
				.filteredOn(transaction -> transaction.getTransactionType().equals("Capture"))
				.extracting(GiftCertificateTransaction::getAmount)
				.hasOnlyOneElementSatisfying(amount -> assertThat(amount).isEqualByComparingTo(CAPTURED_AMOUNT_MONEY.getAmount()));
		assertThat(transactionBalance).isEqualTo(CAPTURED_AMOUNT_MONEY.getAmount());
	}

	@Test
	public void reverseCaptureRestoresBalance() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.capture(giftCertificate, authorizationCode, AUTHORIZED_AMOUNT_MONEY);
		testee.reversePreAuthorization(giftCertificate, authorizationCode, AUTHORIZED_AMOUNT_MONEY);
		final BigDecimal balance = testee.getBalance(giftCertificate);

		assertThat(balance).isEqualTo(GC_AMOUNT_MONEY.getAmount());
	}

	@Test
	public void reverseCaptureCanBeDoneForPartiallyChargedAmountDuringDigitalPurchaseReversals() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.capture(giftCertificate, authorizationCode, CAPTURED_AMOUNT_MONEY);
		testee.reversePreAuthorization(giftCertificate, authorizationCode, CAPTURED_AMOUNT_MONEY);
		final BigDecimal balance = testee.getBalance(giftCertificate);
		final List<GiftCertificateTransaction> transactions = testee.getGiftCertificateTransactions(giftCertificate);
		final BigDecimal transactionBalance = testee.calcTransactionBalance(transactions);

		assertThat(balance).isEqualTo(GC_AMOUNT_MONEY.getAmount());
		assertThat(transactions)
				.filteredOn(transaction -> transaction.getTransactionType().equals("Refund"))
				.extracting(GiftCertificateTransaction::getAmount)
				.hasOnlyOneElementSatisfying(amount -> assertThat(amount).isEqualByComparingTo(CAPTURED_AMOUNT_MONEY.getAmount()));
		assertThat(transactionBalance).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	public void reverseCaptureRecordsRefundTransaction() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.capture(giftCertificate, authorizationCode, AUTHORIZED_AMOUNT_MONEY);
		testee.reversePreAuthorization(giftCertificate, authorizationCode, AUTHORIZED_AMOUNT_MONEY);
		final List<GiftCertificateTransaction> transactions = testee.getGiftCertificateTransactions(giftCertificate);
		final BigDecimal transactionBalance = testee.calcTransactionBalance(transactions);

		assertThat(transactions)
				.filteredOn(transaction -> transaction.getTransactionType().equals("Refund"))
				.extracting(GiftCertificateTransaction::getAmount)
				.hasOnlyOneElementSatisfying(amount -> assertThat(amount).isEqualByComparingTo(AUTHORIZED_AMOUNT_MONEY.getAmount()));
		assertThat(transactionBalance).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	public void refundModifiesBalance() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.capture(giftCertificate, authorizationCode, CAPTURED_AMOUNT_MONEY);
		testee.refund(giftCertificate, authorizationCode, REFUND_AMOUNT_MONEY);
		final BigDecimal balance = testee.getBalance(giftCertificate);

		assertThat(balance).isEqualTo(GC_AMOUNT_MONEY.getAmount()
				.subtract(CAPTURED_AMOUNT_MONEY.getAmount())
				.add(REFUND_AMOUNT_MONEY.getAmount()));
	}

	@Test
	public void refundRecordsTransaction() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.capture(giftCertificate, authorizationCode, CAPTURED_AMOUNT_MONEY);
		testee.refund(giftCertificate, authorizationCode, REFUND_AMOUNT_MONEY);
		final List<GiftCertificateTransaction> transactions = testee.getGiftCertificateTransactions(giftCertificate);
		final BigDecimal transactionBalance = testee.calcTransactionBalance(transactions);

		assertThat(transactions)
				.filteredOn(transaction -> transaction.getTransactionType().equals("Refund"))
				.extracting(GiftCertificateTransaction::getAmount)
				.hasOnlyOneElementSatisfying(amount -> assertThat(amount).isEqualByComparingTo(REFUND_AMOUNT_MONEY.getAmount()));
		assertThat(transactionBalance).isEqualTo(CAPTURED_AMOUNT_MONEY.getAmount().subtract(REFUND_AMOUNT_MONEY.getAmount()));
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void overflowGCAmountOnPreAuthorizationIsNotPossible() {
		final Money overflowAmount = GC_AMOUNT_MONEY.add(ONE_DOLLAR);

		testee.preAuthorize(giftCertificate, overflowAmount);
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void overflowGCAmountByPreAuthorizationsTotalIsNotPossible() {
		final Money overflowAmount = GC_AMOUNT_MONEY.add(ONE_DOLLAR);

		testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		testee.preAuthorize(giftCertificate, overflowAmount.subtract(AUTHORIZED_AMOUNT_MONEY));
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void captureWithoutAuthorizeShouldFail() {
		testee.capture(giftCertificate, "AUTHORIZATION_CODE", CAPTURED_AMOUNT_MONEY);
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void captureReversedPreAuthorizationShouldFail() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.reversePreAuthorization(giftCertificate, authorizationCode, AUTHORIZED_AMOUNT_MONEY);
		testee.capture(giftCertificate, authorizationCode, AUTHORIZED_AMOUNT_MONEY);
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void captureShouldOnlyBePossibleOnceForSameAuthorization() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.capture(giftCertificate, authorizationCode, ONE_DOLLAR);
		testee.capture(giftCertificate, authorizationCode, ONE_DOLLAR);
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void overflowPreAuthorizedAmountOnCaptureIsNotPossible() {
		final Money overflowAmount = AUTHORIZED_AMOUNT_MONEY.add(ONE_DOLLAR);
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		testee.capture(giftCertificate, preAuthorizeResponse.getAuthorizationCode(), overflowAmount);
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void reversePreAuthorizationWithoutPreAuthorizeShouldFail() {
		testee.reversePreAuthorization(giftCertificate, "AUTHORIZATION_CODE", AUTHORIZED_AMOUNT_MONEY);
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void reversePreAuthorizationShouldOnlyBePossibleOnceForSameAuthorization() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.reversePreAuthorization(giftCertificate, authorizationCode, AUTHORIZED_AMOUNT_MONEY);
		testee.reversePreAuthorization(giftCertificate, authorizationCode, AUTHORIZED_AMOUNT_MONEY);
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void refundWithoutCaptureShouldFail() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		testee.refund(giftCertificate, preAuthorizeResponse.getAuthorizationCode(), REFUND_AMOUNT_MONEY);
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void singleRefundShouldNotExceedCapturedAmount() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.capture(giftCertificate, authorizationCode, CAPTURED_AMOUNT_MONEY);
		testee.refund(giftCertificate, authorizationCode, CAPTURED_AMOUNT_MONEY.add(ONE_DOLLAR));
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void refundTotalShouldNotExceedCapturedAmount() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.capture(giftCertificate, authorizationCode, CAPTURED_AMOUNT_MONEY);
		testee.refund(giftCertificate, authorizationCode, CAPTURED_AMOUNT_MONEY);
		testee.refund(giftCertificate, authorizationCode, ONE_DOLLAR);
	}

	@Test
	public void multiRefundsAreOk() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.capture(giftCertificate, authorizationCode, CAPTURED_AMOUNT_MONEY);
		testee.refund(giftCertificate, authorizationCode, REFUND_AMOUNT_MONEY);
		testee.refund(giftCertificate, authorizationCode, REFUND_AMOUNT_MONEY);

		final List<GiftCertificateTransaction> transactions = testee.getGiftCertificateTransactions(giftCertificate);
		final BigDecimal transactionBalance = testee.calcTransactionBalance(transactions);

		assertThat(transactions)
				.filteredOn(transaction -> transaction.getTransactionType().equals("Refund"))
				.hasSize(2);
		assertThat(transactionBalance).isEqualTo(CAPTURED_AMOUNT_MONEY
				.subtract(REFUND_AMOUNT_MONEY)
				.subtract(REFUND_AMOUNT_MONEY).getAmount());
	}

	@Test
	public void multiPaymentSequencesAreOk() {
		final GiftCertificateTransactionResponse preAuthorizeResponse1 = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final GiftCertificateTransactionResponse preAuthorizeResponse2 = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);

		testee.modifyPreAuthorization(giftCertificate, preAuthorizeResponse1.getAuthorizationCode(), MODIFIED_AMOUNT_MONEY);
		testee.capture(giftCertificate, preAuthorizeResponse1.getAuthorizationCode(), CAPTURED_AMOUNT_MONEY);
		testee.refund(giftCertificate, preAuthorizeResponse1.getAuthorizationCode(), REFUND_AMOUNT_MONEY);

		testee.modifyPreAuthorization(giftCertificate, preAuthorizeResponse2.getAuthorizationCode(), MODIFIED_AMOUNT_MONEY);
		testee.capture(giftCertificate, preAuthorizeResponse2.getAuthorizationCode(), CAPTURED_AMOUNT_MONEY);
		testee.refund(giftCertificate, preAuthorizeResponse2.getAuthorizationCode(), REFUND_AMOUNT_MONEY);

		final List<GiftCertificateTransaction> transactions = testee.getGiftCertificateTransactions(giftCertificate);
		final BigDecimal transactionBalance = testee.calcTransactionBalance(transactions);

		assertThat(transactions).hasSize(6);
		assertThat(transactionBalance).isEqualTo(CAPTURED_AMOUNT_MONEY.subtract(REFUND_AMOUNT_MONEY).multiply(2).getAmount());
	}


	@Test(expected = GiftCertificateTransactionException.class)
	public void modifyPreAuthorizationWithoutAuthorizeShouldFail() {
		testee.modifyPreAuthorization(giftCertificate, "AUTHORIZATION_CODE", MODIFIED_AMOUNT_MONEY);
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void modifyReversedPreAuthorizationShouldFail() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.reversePreAuthorization(giftCertificate, authorizationCode, AUTHORIZED_AMOUNT_MONEY);
		testee.modifyPreAuthorization(giftCertificate, authorizationCode, MODIFIED_AMOUNT_MONEY);
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void modifyCapturedAuthorizationShouldFail() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		final String authorizationCode = preAuthorizeResponse.getAuthorizationCode();
		testee.capture(giftCertificate, authorizationCode, CAPTURED_AMOUNT_MONEY);
		testee.modifyPreAuthorization(giftCertificate, authorizationCode, MODIFIED_AMOUNT_MONEY);
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void overflowGCAmountOnModifyPreAuthorizationIsNotPossible() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		testee.modifyPreAuthorization(giftCertificate, preAuthorizeResponse.getAuthorizationCode(), GC_AMOUNT_MONEY.add(ONE_DOLLAR));
	}

	@Test(expected = GiftCertificateTransactionException.class)
	public void overflowGCAmountOnModifyPreAuthorizationByPreAuthorizationsTotalIsNotPossible() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, AUTHORIZED_AMOUNT_MONEY);
		testee.preAuthorize(giftCertificate, GC_AMOUNT_MONEY.subtract(AUTHORIZED_AMOUNT_MONEY));
		testee.modifyPreAuthorization(giftCertificate, preAuthorizeResponse.getAuthorizationCode(), AUTHORIZED_AMOUNT_MONEY.add(ONE_DOLLAR));
	}

	@Test
	public void modifyPreAuthorizationOfFullGCAmount() {
		final GiftCertificateTransactionResponse preAuthorizeResponse = testee.preAuthorize(giftCertificate, GC_AMOUNT_MONEY);
		testee.modifyPreAuthorization(giftCertificate, preAuthorizeResponse.getAuthorizationCode(), AUTHORIZED_AMOUNT_MONEY);

		final List<GiftCertificateTransaction> transactions = testee.getGiftCertificateTransactions(giftCertificate);
		final BigDecimal transactionBalance = testee.calcTransactionBalance(transactions);
		assertThat(transactionBalance).isEqualTo(AUTHORIZED_AMOUNT_MONEY.getAmount());
	}

	private GiftCertificate createAndPersistGiftCertificate() {
		GiftCertificate giftCertificate = getBeanFactory().getPrototypeBean(ContextIdNames.GIFT_CERTIFICATE, GiftCertificate.class);
		giftCertificate.setStore(scenario.getStore());
		giftCertificate.setCreationDate(new Date());
		giftCertificate.setPurchaseAmount(GC_AMOUNT_MONEY.getAmount());
		giftCertificate.setCurrencyCode(GC_AMOUNT_MONEY.getCurrency().getCurrencyCode());
		giftCertificate.setGiftCertificateCode(GIFT_CERTIFICATE_CODE);
		giftCertificateService.add(giftCertificate);
		return giftCertificate;
	}

}