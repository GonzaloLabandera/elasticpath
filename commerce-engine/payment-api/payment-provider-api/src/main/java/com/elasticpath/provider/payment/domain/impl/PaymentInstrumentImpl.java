/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.impl;

import java.util.Objects;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.apache.openjpa.persistence.ElementDependent;
import org.apache.openjpa.persistence.jdbc.ElementForeignKey;
import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;

import com.elasticpath.persistence.api.AbstractEntityImpl;
import com.elasticpath.provider.payment.domain.PaymentInstrument;
import com.elasticpath.provider.payment.domain.PaymentInstrumentData;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;

/**
 * The implementation for payment instrument.
 */
@Entity
@Table(name = PaymentInstrumentImpl.TABLE_NAME)
public class PaymentInstrumentImpl extends AbstractEntityImpl implements PaymentInstrument {

	/**
	 * Payment instrument table name.
	 */
	public static final String TABLE_NAME = "TPAYMENTINSTRUMENT";

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private String billingAddressGuid;

	private long uidPk;

	private String guid;

	private String name;

	private PaymentProviderConfiguration paymentProviderConfiguration;

	private Set<PaymentInstrumentData> paymentInstrumentData;

	private boolean singleReservePerPI;

	private boolean supportingMultiCharges;

	@Override
	@Basic
	@Column(name = "BILLING_ADDRESS_GUID", nullable = true)
	public String getBillingAddressGuid() {
		return this.billingAddressGuid;
	}

	@Override
	public void setBillingAddressGuid(final String guid) {
		this.billingAddressGuid = guid;
	}

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
	@Column(name = "NAME")
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	@ManyToOne(targetEntity = PaymentProviderConfigurationImpl.class)
	@JoinColumn(name = "PAYMENTPROVIDERCONFIG_UID", nullable = false)
	public PaymentProviderConfiguration getPaymentProviderConfiguration() {
		return paymentProviderConfiguration;
	}

	@Override
	public void setPaymentProviderConfiguration(final PaymentProviderConfiguration paymentProviderConfiguration) {
		this.paymentProviderConfiguration = paymentProviderConfiguration;
	}

	@Override
	@OneToMany(targetEntity = PaymentInstrumentDataImpl.class, fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
	@ElementJoinColumn(name = "PAYMENTINSTRUMENT_UID", nullable = false)
	@ElementForeignKey
	@ElementDependent
	public Set<PaymentInstrumentData> getPaymentInstrumentData() {
		return paymentInstrumentData;
	}

	@Override
	public void setPaymentInstrumentData(final Set<PaymentInstrumentData> paymentInstrumentData) {
		this.paymentInstrumentData = paymentInstrumentData;
	}

	@Override
	@Basic
	@Column(name = "IS_SINGLE_RESERVE_PER_PI", nullable = false)
	public boolean isSingleReservePerPI() {
		return singleReservePerPI;
	}

	@Override
	public void setSingleReservePerPI(final boolean singleReservePerPI) {
		this.singleReservePerPI = singleReservePerPI;
	}

	@Override
	@Basic
	@Column(name = "IS_SUPPORTING_MULTI_CHARGES", nullable = false)
	public boolean isSupportingMultiCharges() {
		return supportingMultiCharges;
	}

	@Override
	public void setSupportingMultiCharges(final boolean supportingMultiCharges) {
		this.supportingMultiCharges = supportingMultiCharges;
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
		if (obj instanceof PaymentInstrumentImpl) {
			PaymentInstrumentImpl other = (PaymentInstrumentImpl) obj;
			return Objects.equals(other.getGuid(), this.getGuid());
		}
		return false;
	}

}

