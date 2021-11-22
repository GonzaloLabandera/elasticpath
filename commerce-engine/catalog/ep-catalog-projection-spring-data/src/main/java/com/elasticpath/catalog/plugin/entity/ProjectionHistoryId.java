/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.entity;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represent primary key for entity {@link ProjectionHistoryEntity}.
 */
@Embeddable
public class ProjectionHistoryId implements Serializable {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;
	private static final int COLUMN_LENGTH = 64;

	/**
	 * The store code for the entity.
	 */
	private String store;

	/**
	 * The version number for this projection.
	 */
	private Long version;

	/**
	 * Data type of the entity.
	 */
	private String type;

	/**
	 * Code of the entity.
	 */
	private String code;

	@Basic(optional = false)
	@Column(name = "STORE", length = COLUMN_LENGTH)
	public String getStore() {
		return store;
	}

	public void setStore(final String store) {
		this.store = store;
	}

	@Basic(optional = false)
	@Column(name = "VERSION")
	public Long getVersion() {
		return version;
	}

	public void setVersion(final Long version) {
		this.version = version;
	}

	@Basic(optional = false)
	@Column(name = "TYPE", nullable = false, length = COLUMN_LENGTH)
	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	@Basic(optional = false)
	@Column(name = "CODE", length = COLUMN_LENGTH)
	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final ProjectionHistoryId that = (ProjectionHistoryId) obj;
		return Objects.equals(store, that.store)
				&& Objects.equals(version, that.version)
				&& Objects.equals(type, that.type)
				&& Objects.equals(code, that.code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(store, version, type, code);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("store", getStore())
				.append("version", getVersion())
				.append("dataType", getType())
				.append("code", getCode())
				.toString();
	}
}
