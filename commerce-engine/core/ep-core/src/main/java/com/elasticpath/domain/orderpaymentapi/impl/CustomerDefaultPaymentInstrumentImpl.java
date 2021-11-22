/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.domain.orderpaymentapi.impl;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.openjpa.persistence.DataCache;
import org.apache.openjpa.persistence.jdbc.ForeignKey;

import com.elasticpath.domain.orderpaymentapi.CustomerDefaultPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 * Default implementation for {@link CustomerDefaultPaymentInstrument}.
 */
@Entity
@Table(name = CustomerDefaultPaymentInstrumentImpl.TABLE_NAME)
@DataCache(enabled = false)
public class CustomerDefaultPaymentInstrumentImpl extends AbstractPersistableImpl implements CustomerDefaultPaymentInstrument {

    private static final long serialVersionUID = 5000000001L;

    /**
     * Table name.
     */
    public static final String TABLE_NAME = "TCUSTDEFAULTPAYMENTINSTRUMENT";

    private long uidPk;

    private long customerUid;

    private CustomerPaymentInstrument customerPaymentInstrument;

    /**
     * Default Constructor.
     */
    public CustomerDefaultPaymentInstrumentImpl() {
        //  Default Constructor for OpenJPA
    }

	/**
	 * Regular Constructor.
	 *
	 * @param customerPaymentInstrument the {@link CustomerPaymentInstrument} to be default
	 */
	public CustomerDefaultPaymentInstrumentImpl(final CustomerPaymentInstrument customerPaymentInstrument) {
		this.customerUid = customerPaymentInstrument.getCustomerUid();
		this.customerPaymentInstrument = customerPaymentInstrument;
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
		return uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@Basic
	@Column(name = "CUSTOMER_UID", nullable = false, unique = true)
	public long getCustomerUid() {
		return customerUid;
	}

	@Override
	public void setCustomerUid(final long customerUid) {
		this.customerUid = customerUid;
	}

	@Override
	@ManyToOne(targetEntity = CustomerPaymentInstrumentImpl.class)
	@JoinColumn(name = "CUSTOMER_PYMT_INSTRUMENT_UID", nullable = false, unique = true)
	@ForeignKey(name = "CPI_CDPI_FK")
	public CustomerPaymentInstrument getCustomerPaymentInstrument() {
		return customerPaymentInstrument;
	}

	@Override
	public void setCustomerPaymentInstrument(final CustomerPaymentInstrument customerPaymentInstrument) {
		this.customerPaymentInstrument = customerPaymentInstrument;
	}

}
