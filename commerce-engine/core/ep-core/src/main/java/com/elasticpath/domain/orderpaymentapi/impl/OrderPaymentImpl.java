/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.orderpaymentapi.impl;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.openjpa.persistence.DataCache;
import org.apache.openjpa.persistence.ElementDependent;
import org.apache.openjpa.persistence.Externalizer;
import org.apache.openjpa.persistence.Factory;
import org.apache.openjpa.persistence.Persistent;
import org.apache.openjpa.persistence.jdbc.ElementForeignKey;

import com.elasticpath.domain.order.OrderPaymentStatus;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentData;
import com.elasticpath.persistence.api.AbstractEntityImpl;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;

/**
 * Default implementation of {@link OrderPayment}.
 */
@Entity
@Table(name = OrderPaymentImpl.TABLE_NAME)
@DataCache(enabled = false)
public class OrderPaymentImpl extends AbstractEntityImpl implements OrderPayment {

	private static final long serialVersionUID = 5000000001L;

	/**
	 * Table name.
	 */
	public static final String TABLE_NAME = "TORDERPAYMENTS";

	private long uidPk;

	private String guid;

	private Date createdDate;

	private TransactionType transactionType;

	private OrderPaymentStatus orderPaymentStatus;

	private BigDecimal amount;

	private Currency currency;

	private String parentOrderPaymentGuid;

	private Set<OrderPaymentData> orderPaymentData;

	private String paymentInstrumentGuid;

	private boolean originalPI;

	private String orderNumber;

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
	@Basic
	@Column(name = "ORDER_NUMBER")
	public String getOrderNumber() {
		return orderNumber;
	}

	@Override
	public void setOrderNumber(final String orderNumber) {
		this.orderNumber = orderNumber;
	}

	@Override
	public void setCreatedDate(final Date createdDate) {
		this.createdDate = createdDate;
	}

	@Override
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE", nullable = false)
	public Date getCreatedDate() {
		return createdDate;
	}

	@Override
	public void setTransactionType(final TransactionType transactionType) {
		this.transactionType = transactionType;
	}

	@Override
	@Basic
	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE")
	public TransactionType getTransactionType() {
		return transactionType;
	}

	@Override
	public void setOrderPaymentStatus(final OrderPaymentStatus orderPaymentStatus) {
		this.orderPaymentStatus = orderPaymentStatus;
	}

	@Override
	@Basic
	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS")
	public OrderPaymentStatus getOrderPaymentStatus() {
		return orderPaymentStatus;
	}

	@Override
	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	@Basic
	@Column(name = "AMOUNT", precision = DECIMAL_PRECISION, scale = DECIMAL_SCALE)
	public BigDecimal getAmount() {
		return amount;
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
	public void setParentOrderPaymentGuid(final String parentOrderPaymentGuid) {
		this.parentOrderPaymentGuid = parentOrderPaymentGuid;
	}

	@Override
	@Column(name = "PARENT_ORDER_PAYMENT_GUID")
	public String getParentOrderPaymentGuid() {
		return parentOrderPaymentGuid;
	}

	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID",
			valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME, allocationSize = HIGH_CONCURRENCY_ALLOCATION_SIZE)
	public long getUidPk() {
		return uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@OneToMany(targetEntity = OrderPaymentDataImpl.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "ORDER_PAYMENT_UID", nullable = false)
	@ElementForeignKey
	@ElementDependent
	public Set<OrderPaymentData> getOrderPaymentData() {
		return orderPaymentData;
	}

	@Override
	public void setOrderPaymentData(final Set<OrderPaymentData> orderPaymentData) {
		this.orderPaymentData = orderPaymentData;
	}

	@Override
	@Basic
	@Column(name = "PAYMENT_INSTRUMENT_GUID", nullable = false)
	public String getPaymentInstrumentGuid() {
		return paymentInstrumentGuid;
	}

	@Override
	public void setPaymentInstrumentGuid(final String paymentInstrumentGuid) {
		this.paymentInstrumentGuid = paymentInstrumentGuid;
	}

	@Basic
	@Column(name = "IS_ORIGINAL_PI")
	@Override
	public boolean isOriginalPI() {
		return originalPI;
	}

	@Override
	public void setOriginalPI(final boolean isOriginalPI) {
		this.originalPI = isOriginalPI;
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
		if (obj instanceof OrderPaymentImpl) {
			OrderPaymentImpl other = (OrderPaymentImpl) obj;
			return Objects.equals(other.getGuid(), this.getGuid());
		}
		return false;
	}

}
