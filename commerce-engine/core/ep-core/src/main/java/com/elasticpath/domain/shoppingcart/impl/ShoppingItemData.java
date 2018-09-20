/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
/**
 * 
 */
package com.elasticpath.domain.shoppingcart.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.elasticpath.domain.impl.AbstractItemData;

/**
 * Key/Value data associated with a shopping item.
 */
@Entity
@Table(name = ShoppingItemData.TABLE_NAME)
public class ShoppingItemData extends AbstractItemData {
	private static final long serialVersionUID = 4449647744232193936L;

	private long uidPk;
	
	/** The name of the DB table to use for persisting this object. */
	public static final String TABLE_NAME = "TSHOPPINGITEMDATA";
	
	/**
	 * Constructor for JPA.
	 */
	protected ShoppingItemData() {
		super(null, null);
	}
	
	/**
	 * Constructor.
	 * @param key the key
	 * @param value the value
	 */
	public ShoppingItemData(final String key, final String value) {
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
	@SuppressWarnings("PMD.UselessOverridingMethod")
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * {@inheritDoc}
	 * This implementation assumes equality if the keys are equal.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof ShoppingItemData)) {
			return false;
		}
		return super.equals(obj);
	}
}
