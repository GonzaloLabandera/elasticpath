/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.orderpaymentapi.management;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderPaymentStatus;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.money.MoneyFormatter;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;

/**
 * Payment transaction wrapping {@link OrderPayment} and corresponding {@link PaymentInstrumentDTO}.
 * <p/>
 * Allows {@link PaymentStatisticService} to group and aggregate payment events
 * to produce various reports of interactions with payment provider.
 */
public class PaymentStatistic {

    private static final String ERROR_MESSAGE = "Accumulated order payment %s: %s, accumulator = %s";

    private Locale locale;
    private PaymentInstrumentDTO instrument;
    private OrderPayment initialOrderPayment;
    private OrderPaymentStatus status;
    private BigDecimal amount;

    /**
     * Initializes statistic by setting an {@link OrderPayment} payment event and instruments associated with the order.
     *
     * @param order               order
     * @param initialOrderPayment initial order payment
     * @param instruments         list of instruments used for the order, see
     *                            {@link PaymentInstrumentManagementService#findOrderInstruments(Order)}
     */
    public void initOrderPayment(final Order order, final OrderPayment initialOrderPayment, final List<PaymentInstrumentDTO> instruments) {
        if (this.initialOrderPayment != null) {
            throw new IllegalStateException("Initial order payment may be set only once");
        }
        this.initialOrderPayment = initialOrderPayment;
        this.locale = order.getLocale();
        this.status = initialOrderPayment.getOrderPaymentStatus();
        this.amount = initialOrderPayment.getAmount();
        this.instrument = filterInstrumentByGuid(instruments, initialOrderPayment.getPaymentInstrumentGuid());
    }

	/**
	 * Filters instruments list to find one instrument by its GUID.
	 *
	 * @param instruments list of instruments used for the order
	 * @param guid        instrument GUID
	 * @return instrument
	 */
	protected final PaymentInstrumentDTO filterInstrumentByGuid(final List<PaymentInstrumentDTO> instruments, final String guid) {
		return instruments
				.stream()
				.filter(each -> each.getGUID().equals(guid))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Gets instrument associated with this transaction.
	 *
	 * @return instrument
	 */
	public PaymentInstrumentDTO getInstrument() {
		return instrument;
	}

	/**
	 * Gets the transaction amount.
	 *
	 * @return amount
	 */
	public BigDecimal getAmount() {
		return amount;
	}

	/**
	 * Sets the transaction amount.
	 *
	 * @param amount the amount
	 */
	protected void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	/**
	 * Gets formatted transaction amount in order currency and locale.
	 *
	 * @param moneyFormatter money formatter
	 * @return formatted amount
	 */
	public String getFormattedAmount(final MoneyFormatter moneyFormatter) {
        return moneyFormatter.formatCurrency(initialOrderPayment.getCurrency(), getAmount(), locale);
	}

	/**
	 * Accumulates another payment transaction when grouping them together by some criteria.
	 * <p/>
	 * It only makes sense to group transactions when it is about the same order and instrument.
	 *
	 * @param another another transaction in the same group
	 */
	public void accumulate(final PaymentStatistic another) {
        if (another.initialOrderPayment.equals(initialOrderPayment)) {
            throw new IllegalStateException(String.format(ERROR_MESSAGE, "is the same as accumulator", another, this));
        }
        if (!another.initialOrderPayment.getOrderNumber().equals(initialOrderPayment.getOrderNumber())) {
            throw new IllegalStateException(String.format(ERROR_MESSAGE, "belongs to a different order", another, this));
        }
        if (!another.initialOrderPayment.getPaymentInstrumentGuid().equals(initialOrderPayment.getPaymentInstrumentGuid())) {
            throw new IllegalStateException(String.format(ERROR_MESSAGE, "belongs to a different instrument", another, this));
        }
        if (!another.initialOrderPayment.getCurrency().equals(initialOrderPayment.getCurrency())) {
            throw new IllegalStateException(String.format(ERROR_MESSAGE, "has different currency", another, this));
        }
        setAmount(amount.add(another.amount));
        updateStatus(another.status);
	}

	/**
	 * Check if all payments included in this statistic were successful.
	 *
	 * @return true if all payments were successful
	 */
	public boolean isSuccessful() {
		return status == OrderPaymentStatus.APPROVED;
	}

	/**
	 * Updates status of the payments group.
	 *
	 * @param status new status, potentially a failing one
	 */
	protected void updateStatus(final OrderPaymentStatus status) {
		if (this.status == null || this.status == OrderPaymentStatus.APPROVED) {
			this.status = status;
		}
	}

	@Override
	public boolean equals(final Object another) {
		if (this == another) {
			return true;
		}
		if (another == null || getClass() != another.getClass()) {
			return false;
		}
		PaymentStatistic that = (PaymentStatistic) another;
		return initialOrderPayment.equals(that.initialOrderPayment);
	}

	@Override
	public int hashCode() {
		return Objects.hash(initialOrderPayment);
	}

	@Override
	public String toString() {
		return "PaymentStatistic{"
				+ "instrument=" + instrument.getName()
				+ ", orderPayment=" + initialOrderPayment.getGuid()
				+ ", amount=" + amount + '}';
	}

	/**
	 * Gets initial order payment.
	 *
	 * @return initial order payment
	 */
	protected OrderPayment getInitialOrderPayment() {
		return initialOrderPayment;
	}
}
