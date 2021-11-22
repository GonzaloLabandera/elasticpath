/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.domain.customer.impl;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.openjpa.persistence.DataCache;

import com.elasticpath.domain.customer.CustomerClosure;
import com.elasticpath.domain.impl.AbstractLegacyPersistenceImpl;

/**
 * The default implementation of <code>CustomerClosure</code>.
 */
@Entity
@Table(name = CustomerClosureImpl.TABLE_NAME)
@DataCache(enabled = false)
public class CustomerClosureImpl extends AbstractLegacyPersistenceImpl implements CustomerClosure {

	private static final long serialVersionUID = 1L;

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TCUSTOMERCLOSURE";

	private long uidPk;
	private String ancestorGuid;
	private String descendantGuid;
	private long ancestorDepth;

	@Override
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(
			name = TABLE_NAME,
			table = "JPA_GENERATED_KEYS",
			pkColumnName = "ID",
			valueColumnName = "LAST_VALUE",
			pkColumnValue = TABLE_NAME)
	public long getUidPk() {
		return uidPk;
	}

	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}

	@Override
	@Basic
	@Column(name = "ANCESTOR_GUID", nullable = false)
	public String getAncestorGuid() {
		return ancestorGuid;
	}

	@Override
	public void setAncestorGuid(final String ancestorGuid) {
		this.ancestorGuid = ancestorGuid;
	}

	@Override
	@Basic
	@Column(name = "DESCENDANT_GUID", nullable = false)
	public String getDescendantGuid() {
		return descendantGuid;
	}

	@Override
	public void setDescendantGuid(final String descendantGuid) {
		this.descendantGuid = descendantGuid;
	}

	@Override
	@Basic
	@Column(name = "ANCESTOR_DEPTH", nullable = false)
	public long getAncestorDepth() {
		return ancestorDepth;
	}

	@Override
	public void setAncestorDepth(final long ancestorDepth) {
		this.ancestorDepth = ancestorDepth;
	}
}
