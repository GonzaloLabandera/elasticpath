/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.orderpaymentapi.impl;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.openjpa.persistence.DataCache;

import com.elasticpath.domain.orderpaymentapi.OrderPaymentData;
import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 * Default implementation of {@link OrderPaymentData}.
 */
@Entity
@Table(name = OrderPaymentDataImpl.TABLE_NAME)
@DataCache(enabled = false)
public class OrderPaymentDataImpl extends AbstractPersistableImpl implements OrderPaymentData {

	private static final long serialVersionUID = 5000000001L;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TORDERPAYMENTDATA";

	private long uidpk;

	private String key;

	private String value;

	@Override
	public void setKey(final String key) {
		this.key = key;
	}

	@Override
	@Basic(optional = false)
	@Column(name = "DATA_KEY")
	public String getKey() {
		return key;
	}

	@Override
	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	@Basic(optional = false)
	@Column(name = "DATA_VALUE")
	public String getValue() {
		return value;
	}

	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME,
			table = "JPA_GENERATED_KEYS",
			pkColumnName = "ID",
			valueColumnName = "LAST_VALUE",
			pkColumnValue = TABLE_NAME,
			allocationSize = HIGH_CONCURRENCY_ALLOCATION_SIZE)
	public long getUidPk() {
		return uidpk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidpk = uidPk;
	}
}
