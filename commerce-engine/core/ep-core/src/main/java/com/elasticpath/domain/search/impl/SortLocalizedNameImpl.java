/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.domain.search.impl;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.elasticpath.domain.search.SortLocalizedName;
import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 * Default implementation of SortLocalizedName.
 */
@Entity
@Table(name = SortLocalizedNameImpl.TABLE_NAME)
public class SortLocalizedNameImpl extends AbstractPersistableImpl implements SortLocalizedName {

	private static final long serialVersionUID = 5000000001L;

	/**
	 * Table name for sort localized name.
	 */
	public static final String TABLE_NAME = "TSORTLOCALIZEDNAME";

	private long uidPk;

	private String localeCode;

	private String name;

	@Override
	@Basic(optional = false)
	@Column(name = "LOCALE_CODE")
	public String getLocaleCode() {
		return localeCode;
	}

	@Override
	public void setLocaleCode(final String localeCode) {
		this.localeCode = localeCode;
	}

	@Override
	@Basic(optional = false)
	@Column(name = "NAME")
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
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
}
