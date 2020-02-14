/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.orderpaymentapi.impl;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;
import org.apache.openjpa.persistence.Persistent;

import com.elasticpath.domain.orderpaymentapi.CorePaymentInstrument;
import com.elasticpath.persistence.api.AbstractEntityImpl;

/**
 * Default implementation of {@link CorePaymentInstrument}.
 */
@MappedSuperclass
public abstract class AbstractCorePaymentInstrumentImpl extends AbstractEntityImpl implements CorePaymentInstrument {

	private static final long serialVersionUID = 5000000001L;

	private String guid;

	private String paymentInstrumentGuid;

	private BigDecimal limitAmount;

	private Currency currency;

	@Override
	@Basic
	@Column(name = "GUID")
	public String getGuid() {
		return guid;
	}

	@Override
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	@Override
	public void setPaymentInstrumentGuid(final String paymentInstrumentGuid) {
		this.paymentInstrumentGuid = paymentInstrumentGuid;
	}

	@Override
	@Basic
	@Column(name = "PAYMENT_INSTRUMENT_GUID", unique = true)
	public String getPaymentInstrumentGuid() {
		return paymentInstrumentGuid;
	}

	@Override
	public void setLimitAmount(final BigDecimal limitAmount) {
		this.limitAmount = limitAmount;
	}

	@Override
	@Basic
	@Column(name = "LIMIT_AMOUNT", precision = DECIMAL_PRECISION, scale = DECIMAL_SCALE)
	public BigDecimal getLimitAmount() {
		return limitAmount;
	}

	@Override
	public void setCurrency(final Currency currency) {
		this.currency = currency;
	}

	@Override
	@Persistent
	@Externalizer("getCurrencyCode")
	@Factory("com.elasticpath.commons.util.impl.ConverterUtils.currencyFromString")
	@Column(name = "CURRENCY_CODE")
	public Currency getCurrency() {
		return currency;
	}

	@Override
	@Transient
	public int hashCode() {
		return Objects.hashCode(getGuid());
	}

	@Override
	@Transient
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof AbstractCorePaymentInstrumentImpl) {
			AbstractCorePaymentInstrumentImpl other = (AbstractCorePaymentInstrumentImpl) obj;
			return Objects.equals(other.getGuid(), this.getGuid());
		}
		return false;
	}

}
