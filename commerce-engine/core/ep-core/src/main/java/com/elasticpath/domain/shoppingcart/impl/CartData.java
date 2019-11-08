/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
/**
 * 
 */
package com.elasticpath.domain.shoppingcart.impl;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.elasticpath.domain.DatabaseCreationDate;
import com.elasticpath.domain.DatabaseLastModifiedDate;
import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 * Key/Value data associated with a shopping cart.
 */
@Entity
@Table(name = CartData.TABLE_NAME)
public class CartData extends AbstractPersistableImpl implements DatabaseCreationDate, DatabaseLastModifiedDate {
	private static final long serialVersionUID = 4449647744232193936L;

	private long uidPk;

	/** The name of the DB table to use for persisting this object. */
	public static final String TABLE_NAME = "TCARTDATA";

	private Date lastModifiedDate;

	private Date creationDate;

	private String key;
	private String value;

	/**
	 * Constructor for JPA.
	 */
	protected CartData() {
		//no-op
	}

	/**
	 * Constructor.
	 * @param key the key
	 * @param value the value
	 */
	public CartData(final String key, final String value) {
		this.key = key;
		this.value = value;
	}




	/**
	 * @return the key
	 */
	@Basic
	@Column(name = "DATA_KEY", nullable = false) // can't use "KEY" as it is reserved
	public String getKey() {
		return key;
	}

	/**
	 * Set the key.
	 * @param key the key to set
	 */
	protected void setKey(final String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	@Basic
	@Column(name = "VALUE")
	@Lob
	public String getValue() {
		return value;
	}

	/**
	 * Set the value.
	 * @param value the value to set
	 */
	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof CartData)) {
			return false;
		}

		CartData data = (CartData) other;
		return Objects.equals(this.key, data.key);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.key);
	}
	/**
	 * Gets the unique identifier for this domain model object.
	 *
	 * @return the unique identifier.
	 */
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID",
			valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME, allocationSize = HIGH_CONCURRENCY_ALLOCATION_SIZE)
	public long getUidPk() {
		return this.uidPk;
	}

	/**
	 * Sets the unique identifier for this domain model object.
	 *
	 * @param uidPk the new unique identifier.
	 */
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_MODIFIED_DATE", nullable = false)
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	@Override
	public void setLastModifiedDate(final Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	@Override
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_DATE", nullable = false)
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(final Date creationDate) {
		this.creationDate = creationDate;
	}

}
