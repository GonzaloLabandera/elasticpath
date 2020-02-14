/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.impl;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.elasticpath.persistence.api.AbstractPersistableImpl;
import com.elasticpath.provider.payment.domain.PaymentInstrumentData;

/**
 * The implementation for payment instrument data.
 */
@Entity
@Table(name = PaymentInstrumentDataImpl.TABLE_NAME)
public class PaymentInstrumentDataImpl extends AbstractPersistableImpl implements PaymentInstrumentData {

	/**
	 * Payment instrument data table name.
	 */
	public static final String TABLE_NAME = "TPAYMENTINSTRUMENTDATA";

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private long uidPk;

	private String key;

	private String data;

	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID", valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME)
	public long getUidPk() {
		return this.uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@Basic
	@Column(name = "CONFIG_KEY")
	public String getKey() {
		return key;
	}

	@Override
	public void setKey(final String key) {
		this.key = key;
	}

	@Override
	@Basic
	@Column(name = "CONFIG_DATA")
	public String getData() {
		return data;
	}

	@Override
	public void setData(final String data) {
		this.data = data;
	}
}

