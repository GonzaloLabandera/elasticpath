/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.orderpaymentapi.impl;

import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.openjpa.persistence.DataCache;

import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.persistence.api.AbstractEntityImpl;

/**
 * Default implementation for {@link CustomerPaymentInstrument}.
 */
@Entity
@Table(name = CustomerPaymentInstrumentImpl.TABLE_NAME)
@DataCache(enabled = false)
public class CustomerPaymentInstrumentImpl extends AbstractEntityImpl implements CustomerPaymentInstrument {

	private static final long serialVersionUID = 5000000001L;

	/**
	 * Table name.
	 */
	public static final String TABLE_NAME = "TCUSTOMERPAYMENTINSTRUMENT";

	private long uidPk;

	private String guid;

	private String paymentInstrumentGuid;

	private long customerUid;

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
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID", valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME)
	public long getUidPk() {
		return uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@Basic
	@Column(name = "PAYMENT_INSTRUMENT_GUID")
	public String getPaymentInstrumentGuid() {
		return paymentInstrumentGuid;
	}

	@Override
	public void setPaymentInstrumentGuid(final String paymentInstrumentGuid) {
		this.paymentInstrumentGuid = paymentInstrumentGuid;
	}

	@Override
	@Basic
	@Column(name = "CUSTOMER_UID")
	public long getCustomerUid() {
		return customerUid;
	}

	@Override
	public void setCustomerUid(final long customerUid) {
		this.customerUid = customerUid;
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
		if (obj instanceof CustomerPaymentInstrumentImpl) {
			CustomerPaymentInstrumentImpl other = (CustomerPaymentInstrumentImpl) obj;
			return Objects.equals(other.getGuid(), this.getGuid());
		}
		return false;
	}

}
