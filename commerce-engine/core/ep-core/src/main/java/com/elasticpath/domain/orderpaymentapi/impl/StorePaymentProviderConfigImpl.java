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

import org.apache.openjpa.persistence.DataCache;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.persistence.api.AbstractEntityImpl;

/**
 * Default implementation for {@link StorePaymentProviderConfig}.
 */
@Entity
@Table(name = StorePaymentProviderConfigImpl.TABLE_NAME)
@DataCache(enabled = false)
public class StorePaymentProviderConfigImpl extends AbstractEntityImpl implements StorePaymentProviderConfig {

	/**
	 * Table name.
	 */
	public static final String TABLE_NAME = "TSTOREPAYMENTPROVIDERCONFIG";

	private static final long serialVersionUID = 5000000001L;

	private long uidPk;

	private String guid;

	private String paymentProviderConfigGuid;

	private String storeCode;

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
	@Column(name = "PAYMENT_PROVIDER_CONFIG_GUID")
	public String getPaymentProviderConfigGuid() {
		return paymentProviderConfigGuid;
	}

	@Override
	public void setPaymentProviderConfigGuid(final String paymentProviderConfigGuid) {
		this.paymentProviderConfigGuid = paymentProviderConfigGuid;
	}

	@Override
	@Basic
	@Column(name = "STORECODE")
	public String getStoreCode() {
		return storeCode;
	}

	@Override
	public void setStoreCode(final String storeCode) {
		this.storeCode = storeCode;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		StorePaymentProviderConfigImpl that = (StorePaymentProviderConfigImpl) other;
		return getUidPk() == that.getUidPk()
				&& getGuid().equals(that.getGuid())
				&& getStoreCode().equals(that.getStoreCode());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getUidPk(), getGuid(), getPaymentProviderConfigGuid(), getStoreCode());
	}
}
