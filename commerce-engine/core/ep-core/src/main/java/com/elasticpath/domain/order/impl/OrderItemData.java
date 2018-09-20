/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.order.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.elasticpath.domain.impl.AbstractItemData;

/**
 * Key/Value metadata associated with an order item.
 */
@Entity
@Table(name = OrderItemData.TABLE_NAME)
public class OrderItemData extends AbstractItemData {

	private static final long serialVersionUID = 2264404135360493998L;

	/** The name of the DB table to use for persisting this object. */
	public static final String TABLE_NAME = "TORDERITEMDATA";

	private long uidPk;

	/**
	 * Constructor for JPA.
	 */
	protected OrderItemData() {
		super(null, null);
	}
	
	/**
	 * Constructor.
	 * @param key the key
	 * @param value the value
	 */
	public OrderItemData(final String key, final String value) {
		super(key, value);
	}

	/**
	 * Gets the unique identifier for this domain model object.
	 *
	 * @return the unique identifier.
	 */
	@Override
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
	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof OrderItemData)) {
			return false;
		}
		return super.equals(other);
	}

	@Override
	@SuppressWarnings("PMD.UselessOverridingMethod")
	public int hashCode() {
		return super.hashCode();
	}

}
