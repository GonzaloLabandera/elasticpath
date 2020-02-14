/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.giftcertificate.impl;

import static com.elasticpath.commons.constants.ContextIdNames.GIFT_CERTIFICATE_TRANSACTION;
import static com.elasticpath.commons.constants.ContextIdNames.GIFT_CERTIFICATE_TRANSACTION_SERVICE;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.elasticpath.commons.util.GiftCertificateCodeGenerator;
import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.domain.payment.GiftCertificateTransaction;
import com.elasticpath.money.Money;
import com.elasticpath.service.giftcertificate.GiftCertificateTransactionException;
import com.elasticpath.service.giftcertificate.GiftCertificateTransactionService;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.misc.TimeService;

/**
 * Gift certificate transaction service implementation. All payment plugin calls should end up calling the {@link GiftCertificateTransactionService}.
 */
@SuppressWarnings("PMD.GodClass")
public class GiftCertificateTransactionServiceImpl extends AbstractEpPersistenceServiceImpl implements GiftCertificateTransactionService {
	private static final String AUTHORIZATION_TRANSACTION = "Authorization";
	private static final String CAPTURE_TRANSACTION = "Capture";
	private static final String REVERSE_AUTHORIZATION = "Authorization Reversal";
	private static final String REFUND_TRANSACTION = "Refund";

	private GiftCertificateCodeGenerator giftCertificateCodeGenerator;

	private TimeService timeService;

	@Override
	public GiftCertificateTransaction saveOrUpdate(final GiftCertificateTransaction giftCertificateTransaction) {
		sanityCheck();
		return getPersistenceEngine().saveOrUpdate(giftCertificateTransaction);
	}

	@Override
	public Object getObject(final long uid) {
		sanityCheck();
		if (uid <= 0) {
			return getPrototypeBean(GIFT_CERTIFICATE_TRANSACTION, GiftCertificateTransaction.class);
		}
		return getPersistentBeanFinder().load(GIFT_CERTIFICATE_TRANSACTION, uid);
	}

	/**
	 * Gets transactional proxy for {@link GiftCertificateTransactionService}.
	 *
	 * @return new transactional proxy for {@link GiftCertificateTransactionService}
	 */
	protected GiftCertificateTransactionService getGiftCertificateTransactionService() {
		return getSingletonBean(GIFT_CERTIFICATE_TRANSACTION_SERVICE, GiftCertificateTransactionService.class);
	}

	@Override
	public GiftCertificateTransactionResponse preAuthorize(final GiftCertificate giftCertificate, final Money amount) {
		List<GiftCertificateTransaction> allTransactions = getGiftCertificateTransactions(giftCertificate);

		if (getBalanceInternal(allTransactions, giftCertificate).compareTo(amount.getAmount()) < 0) {
			throw new GiftCertificateTransactionException("Not enough balance on this gift certificate");
		}

		final GiftCertificateTransaction authTransaction = createTransaction(giftCertificate, AUTHORIZATION_TRANSACTION);
		authTransaction.setAmount(amount.getAmount());
		authTransaction.setAuthorizationCode(getGiftCertificateCodeGenerator().generateCode());
		getGiftCertificateTransactionService().saveOrUpdate(authTransaction);
		return createResponse(authTransaction.getAuthorizationCode(), giftCertificate.getGiftCertificateCode());
	}

	@Override
	public GiftCertificateTransactionResponse capture(final GiftCertificate giftCertificate, final String authorizationCode, final Money amount) {
		List<GiftCertificateTransaction> allTransactions = getGiftCertificateTransactions(giftCertificate);

		GiftCertificateTransaction authTransaction = getAuthTransaction(allTransactions, authorizationCode);
		if (authTransaction == null) {
			throw new GiftCertificateTransactionException("Associated authorization transaction could not be found for the capture payment.");
		}

		if (getReverseTransaction(allTransactions, authorizationCode) != null) {
			throw new GiftCertificateTransactionException("Associated authorization transaction has already been reversed.");
		}

		if (getCaptureTransaction(allTransactions, authorizationCode) != null) {
			throw new GiftCertificateTransactionException("This authorization transaction has already been captured.");
		}

		if (authTransaction.getAmount().compareTo(amount.getAmount()) < 0) {
			throw new GiftCertificateTransactionException("Something wrong on this gift certificate, the reserved amount can't cover this payment.");
		}

		final GiftCertificateTransaction captureTransaction = createTransaction(giftCertificate, CAPTURE_TRANSACTION);
		captureTransaction.setAmount(amount.getAmount());
		captureTransaction.setAuthorizationCode(authorizationCode);
		getGiftCertificateTransactionService().saveOrUpdate(captureTransaction);
		return createResponse(authorizationCode, giftCertificate.getGiftCertificateCode());
	}

	@Override
	public void reversePreAuthorization(final GiftCertificate giftCertificate, final String authorizationCode, final Money amount) {
		List<GiftCertificateTransaction> allTransactions = getGiftCertificateTransactions(giftCertificate);

		GiftCertificateTransaction authTransaction = getAuthTransaction(allTransactions, authorizationCode);
		if (authTransaction == null) {
			throw new GiftCertificateTransactionException("Associated authorization transaction could not be found for the reverse payment.");
		}

		if (getReverseTransaction(allTransactions, authorizationCode) != null) {
			throw new GiftCertificateTransactionException("Associated authorization transaction has already been reversed.");
		}

		GiftCertificateTransaction captureTransaction = getCaptureTransaction(allTransactions, authorizationCode);
		if (captureTransaction != null) {
			// refund everything that was captured, even if it was partial charge
			refund(giftCertificate, authorizationCode, Money.valueOf(captureTransaction.getAmount(), amount.getCurrency()));
			return;
		}

		if (amount.getAmount().compareTo(authTransaction.getAmount()) != 0) {
			throw new GiftCertificateTransactionException("Reversed amount should equal to authorized amount for gift certificate.");
		}

		final GiftCertificateTransaction reverseTransaction = createTransaction(giftCertificate, REVERSE_AUTHORIZATION);
		reverseTransaction.setAmount(amount.getAmount());
		reverseTransaction.setAuthorizationCode(authorizationCode);
		getGiftCertificateTransactionService().saveOrUpdate(reverseTransaction);
	}

	@Override
	public void modifyPreAuthorization(final GiftCertificate giftCertificate, final String authorizationCode, final Money amount) {
		List<GiftCertificateTransaction> allTransactions = getGiftCertificateTransactions(giftCertificate);

		GiftCertificateTransaction authTransaction = getAuthTransaction(allTransactions, authorizationCode);
		if (authTransaction == null) {
			throw new GiftCertificateTransactionException("Associated authorization transaction could not be found for the modification.");
		}

		if (getReverseTransaction(allTransactions, authorizationCode) != null) {
			throw new GiftCertificateTransactionException("Associated authorization transaction has already been reversed.");
		}

		if (getCaptureTransaction(allTransactions, authorizationCode) != null) {
			throw new GiftCertificateTransactionException("This authorization transaction has already been captured.");
		}

		if (getBalanceInternal(allTransactions, giftCertificate).add(authTransaction.getAmount()).compareTo(amount.getAmount()) < 0) {
			throw new GiftCertificateTransactionException("Not enough balance on this gift certificate");
		}

		authTransaction.setAmount(amount.getAmount());
		getGiftCertificateTransactionService().saveOrUpdate(authTransaction);
	}

	@Override
	public void refund(final GiftCertificate giftCertificate, final String authorizationCode, final Money amount) {
		List<GiftCertificateTransaction> allTransactions = getGiftCertificateTransactions(giftCertificate);

		GiftCertificateTransaction captureTransaction = getCaptureTransaction(allTransactions, authorizationCode);
		if (captureTransaction == null) {
			throw new GiftCertificateTransactionException("Associated authorization transaction has not been captured.");
		}

		final BigDecimal capturedAmount = captureTransaction.getAmount().subtract(calcRefundAmount(allTransactions, authorizationCode));
		if (amount.getAmount().compareTo(capturedAmount) > 0) {
			throw new GiftCertificateTransactionException("Refunded amount should be less or equal to captured amount for gift certificate.");
		}

		final GiftCertificateTransaction refundTransaction = createTransaction(giftCertificate, REFUND_TRANSACTION);
		refundTransaction.setAmount(amount.getAmount());
		refundTransaction.setAuthorizationCode(authorizationCode);
		getGiftCertificateTransactionService().saveOrUpdate(refundTransaction);
	}

	@Override
	public BigDecimal getBalance(final GiftCertificate giftCertificate) {
		List<GiftCertificateTransaction> allTransactions = getGiftCertificateTransactions(giftCertificate);
		return getBalanceInternal(allTransactions, giftCertificate);
	}

	private BigDecimal getBalanceInternal(final List<GiftCertificateTransaction> allTransactions, final GiftCertificate giftCertificate) {
		BigDecimal balance = giftCertificate.getPurchaseAmount();
		BigDecimal allocatedAmount = calcTransactionBalance(allTransactions);
		return balance.subtract(allocatedAmount);
	}

	@Override
	public BigDecimal calcTransactionBalance(final List<GiftCertificateTransaction> allTransactions) {
		/*
		 					Auth_transaction
		 						  |
		 			Has_reversed_transaction_associated
		 					/  			   \
		 				   YES  		   NO
		 					|              |
						 minus=0      Has_capture_transaction
		 							   	/		      \
		 							  YES   	      NO
		 							   |        	  |
		 				Has_refund_transaction        Minus=Authorised
		 				/					\
		 		      YES					NO
		 			   |					|
		 		Minus=Captured-Refunded		Minus=Captured
		 */
		List<GiftCertificateTransaction> authTransactions = getAuthTransactions(allTransactions);
		BigDecimal allocatedAmount = BigDecimal.ZERO;
		for (GiftCertificateTransaction authTransaction : authTransactions) {
			final BigDecimal dueTransaction;
			final String authorizationCode = authTransaction.getAuthorizationCode();
			GiftCertificateTransaction reverseTransaction = getReverseTransaction(allTransactions, authorizationCode);
			if (reverseTransaction == null) {
				GiftCertificateTransaction captureTransaction = getCaptureTransaction(allTransactions, authorizationCode);
				if (captureTransaction == null) {
					dueTransaction = authTransaction.getAmount();
				} else {
					dueTransaction = captureTransaction.getAmount().subtract(calcRefundAmount(allTransactions, authorizationCode));
					if (dueTransaction.compareTo(BigDecimal.ZERO) < 0) {
						throw new GiftCertificateTransactionException(
								"Found that total refunded amount for this gift certificate exceeded captured amount.");
					}
				}
			} else {
				dueTransaction = BigDecimal.ZERO;
			}
			allocatedAmount = allocatedAmount.add(dueTransaction);
		}
		return allocatedAmount;
	}

	private GiftCertificateTransaction getAuthTransaction(final List<GiftCertificateTransaction> allTransactions, final String authorizationCode) {
		final List<GiftCertificateTransaction> authTransactions = new ArrayList<>();

		for (GiftCertificateTransaction giftCertificateTransaction : getAuthTransactions(allTransactions)) {
			if (authorizationCode.equals(giftCertificateTransaction.getAuthorizationCode())) {
				authTransactions.add(giftCertificateTransaction);
			}
		}

		if (authTransactions.isEmpty()) {
			return null;
		} else if (authTransactions.size() > 1) {
			throw new GiftCertificateTransactionException("Found more than 1 gift certificate authorized transactions for the auth code.");
		}
		return authTransactions.get(0);
	}

	private List<GiftCertificateTransaction> getAuthTransactions(final List<GiftCertificateTransaction> allTransactions) {
		final List<GiftCertificateTransaction> authTransactions = new ArrayList<>();

		for (GiftCertificateTransaction giftCertificateTransaction : allTransactions) {
			if (AUTHORIZATION_TRANSACTION.equals(giftCertificateTransaction.getTransactionType())) {
				authTransactions.add(giftCertificateTransaction);
			}
		}
		return authTransactions;
	}

	private GiftCertificateTransaction getReverseTransaction(final List<GiftCertificateTransaction> allTransactions,
															 final String authorizationCode) {
		final List<GiftCertificateTransaction> reverseTransactions = new ArrayList<>();

		for (GiftCertificateTransaction giftCertificateTransaction : allTransactions) {
			if (REVERSE_AUTHORIZATION.equals(giftCertificateTransaction.getTransactionType())
					&& authorizationCode.equals(giftCertificateTransaction.getAuthorizationCode())) {
				reverseTransactions.add(giftCertificateTransaction);
			}
		}

		if (reverseTransactions.isEmpty()) {
			return null;
		} else if (reverseTransactions.size() > 1) {
			throw new GiftCertificateTransactionException("Found more than 1 gift certificate reverse transactions for the auth code.");
		}
		return reverseTransactions.get(0);
	}

	private GiftCertificateTransaction getCaptureTransaction(final List<GiftCertificateTransaction> allTransactions,
															 final String authorizationCode) {
		List<GiftCertificateTransaction> captureTransactions = new ArrayList<>();

		for (GiftCertificateTransaction giftCertificateTransaction : allTransactions) {
			if (CAPTURE_TRANSACTION.equals(giftCertificateTransaction.getTransactionType())
					&& authorizationCode.equals(giftCertificateTransaction.getAuthorizationCode())) {
				captureTransactions.add(giftCertificateTransaction);
			}
		}

		if (captureTransactions.isEmpty()) {
			return null;
		} else if (captureTransactions.size() > 1) {
			throw new GiftCertificateTransactionException("Found more than 1 gift certificate capture transactions for the auth code.");
		}
		return captureTransactions.get(0);
	}

	private List<GiftCertificateTransaction> getRefundTransactions(final List<GiftCertificateTransaction> allTransactions,
																   final String authorizationCode) {
		List<GiftCertificateTransaction> refundTransactions = new ArrayList<>();

		for (GiftCertificateTransaction giftCertificateTransaction : allTransactions) {
			if (REFUND_TRANSACTION.equals(giftCertificateTransaction.getTransactionType())
					&& authorizationCode.equals(giftCertificateTransaction.getAuthorizationCode())) {
				refundTransactions.add(giftCertificateTransaction);
			}
		}

		return refundTransactions;
	}

	private BigDecimal calcRefundAmount(final List<GiftCertificateTransaction> allTransactions, final String authorizationCode) {
		BigDecimal refundAmount = BigDecimal.ZERO;
		final List<GiftCertificateTransaction> refundTransactions = getRefundTransactions(allTransactions, authorizationCode);
		for (GiftCertificateTransaction refundTransaction : refundTransactions) {
			refundAmount = refundAmount.add(refundTransaction.getAmount());
		}
		return refundAmount;
	}

	@Override
	public List<GiftCertificateTransaction> getGiftCertificateTransactions(final GiftCertificate giftCertificate) {
		return getPersistenceEngine().retrieveByNamedQuery("GIFT_CERTIFICATE_TRANSACTIONS", giftCertificate.getUidPk());
	}

	private GiftCertificateTransaction createTransaction(final GiftCertificate giftCertificate, final String transactionType) {
		GiftCertificateTransaction certificateTransaction = getPrototypeBean(GIFT_CERTIFICATE_TRANSACTION, GiftCertificateTransaction.class);
		certificateTransaction.setTransactionType(transactionType);
		certificateTransaction.setCreatedDate(timeService.getCurrentTime());
		certificateTransaction.setGiftCertificate(giftCertificate);
		return certificateTransaction;
	}

	private GiftCertificateTransactionResponse createResponse(final String authorizationCode, final String giftCertificateCode) {
		GiftCertificateTransactionResponse response = new GiftCertificateTransactionResponseImpl();
		response.setAuthorizationCode(authorizationCode);
		response.setGiftCertificateCode(giftCertificateCode);
		return response;
	}

	@Override
	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}

	/**
	 * @return the giftCertificateCodeGenerator
	 */
	protected GiftCertificateCodeGenerator getGiftCertificateCodeGenerator() {
		return giftCertificateCodeGenerator;
	}

	/**
	 * @param giftCertificateCodeGenerator the giftCertificateCodeGenerator to set
	 */
	public void setGiftCertificateCodeGenerator(final GiftCertificateCodeGenerator giftCertificateCodeGenerator) {
		this.giftCertificateCodeGenerator = giftCertificateCodeGenerator;
	}

}
