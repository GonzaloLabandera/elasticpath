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

import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;

/**
 * Default implementation of {@link CartOrderPaymentInstrument}.
 */
@Entity
@Table(name = CartOrderPaymentInstrumentImpl.TABLE_NAME)
@DataCache(enabled = false)
public class CartOrderPaymentInstrumentImpl extends AbstractCorePaymentInstrumentImpl implements CartOrderPaymentInstrument {

    private static final long serialVersionUID = 5000000001L;

    /**
     * Table name.
     */
    public static final String TABLE_NAME = "TCARTORDERPAYMENTINSTRUMENT";

    private long uidPk;

    private long cartOrderUid;

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
        return uidPk;
    }

    @Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
    }

    @Override
    @Basic
    @Column(name = "CART_ORDER_UID")
    public long getCartOrderUid() {
        return cartOrderUid;
    }

    @Override
    public void setCartOrderUid(final long cartOrderUid) {
        this.cartOrderUid = cartOrderUid;
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
		if (obj instanceof CartOrderPaymentInstrumentImpl) {
			CartOrderPaymentInstrumentImpl other = (CartOrderPaymentInstrumentImpl) obj;
			return Objects.equals(other.getGuid(), this.getGuid());
		}
		return false;
	}

}
