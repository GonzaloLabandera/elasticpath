/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.modifier.impl;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.elasticpath.domain.modifier.ModifierGroupFilter;
import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 *  Modifier Group Filter Impl.
 */
@Entity
@Table(name = ModifierGroupFilterImpl.TABLE_NAME)
public class ModifierGroupFilterImpl extends AbstractPersistableImpl implements ModifierGroupFilter {


	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;
	/**
	 * The name of the table.
	 */
	public static final String TABLE_NAME = "TMODIFIERGROUPFILTER";
	private long uidPk;
	private String referenceGuid;
	private String modifierCode;
	private String type;

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
	@Column(name = "REFERENCE_GUID")
	public String getReferenceGuid() {
		return referenceGuid;
	}

	@Override
	public void setReferenceGuid(final String referenceGuid) {
		this.referenceGuid = referenceGuid;
	}

	@Override
	@Basic
	@Column(name = "MODIFIER_CODE")
	public String getModifierCode() {
		return modifierCode;
	}

	@Override
	public void setModifierCode(final String modifierCode) {
		this.modifierCode = modifierCode;
	}

	@Override
	@Basic
	@Column(name = "TYPE")
	public String getType() {
		return type;
	}

	@Override
	public void setType(final String type) {
		this.type = type;
	}

	
}
