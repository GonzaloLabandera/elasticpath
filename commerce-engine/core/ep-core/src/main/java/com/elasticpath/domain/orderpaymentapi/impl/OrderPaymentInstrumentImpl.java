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

import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;

/**
 * Default implementation of {@link OrderPaymentInstrument}.
 */
@Entity
@Table(name = OrderPaymentInstrumentImpl.TABLE_NAME)
@DataCache(enabled = false)
public class OrderPaymentInstrumentImpl extends AbstractCorePaymentInstrumentImpl implements OrderPaymentInstrument {

    private static final long serialVersionUID = 5000000301L;

    /**
     * Table name.
     */
    public static final String TABLE_NAME = "TORDERPAYMENTINSTRUMENT";

    private long uidPk;

    private String orderNumber;

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
		if (obj instanceof OrderPaymentInstrumentImpl) {
			OrderPaymentInstrumentImpl other = (OrderPaymentInstrumentImpl) obj;
			return Objects.equals(other.getGuid(), this.getGuid());
		}
		return false;
	}

}
