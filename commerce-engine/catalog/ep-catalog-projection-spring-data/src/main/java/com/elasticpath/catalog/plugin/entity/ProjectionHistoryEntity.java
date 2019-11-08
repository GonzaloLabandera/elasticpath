/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.entity;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Represents historical projection entity.
 */
@Entity
@Table(name = ProjectionHistoryEntity.TABLE_NAME)
public class ProjectionHistoryEntity {

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TCATALOGHISTORY";

	private static final int SCHEMA_VERSION_COLUMN_LENGTH = 64;
	private static final int CONTENT_HASH_COLUMN_LENGTH = 256;
	private static final int CONTENT_COLUMN_LENGTH = 16_777_215;

	/**
	 * The composite primary key.
	 */
	private ProjectionHistoryId historyId;

	/**
	 * The time and date that the projection was created.
	 */
	private Date projectionDateTime;

	/**
	 * Indicates whether or not the entity has been deleted.
	 */
	private boolean deleted;

	/**
	 * The version number of the schema used when generating this projection.
	 */
	private String schemaVersion;

	/**
	 * A SHA-256 hash of the projection content.
	 */
	private String contentHash;

	/**
	 * The contents of the projection, encoded as JSON.
	 */
	private String content;

	private ProjectionEntity projectionEntity;

	@EmbeddedId
	public ProjectionHistoryId getHistoryId() {
		return historyId;
	}

	public void setHistoryId(final ProjectionHistoryId historyId) {
		this.historyId = historyId;
	}

	@Basic(optional = false)
	@Column(name = "PROJECTION_DATE_TIME")
	public Date getProjectionDateTime() {
		return projectionDateTime;
	}

	public void setProjectionDateTime(final Date projectionDateTime) {
		this.projectionDateTime = projectionDateTime;
	}

	@Basic(optional = false)
	@Column(name = "DELETED")
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(final boolean deleted) {
		this.deleted = deleted;
	}

	@Basic
	@Column(name = "SCHEMA_VERSION", length = SCHEMA_VERSION_COLUMN_LENGTH)
	public String getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(final String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	@Basic
	@Column(name = "CONTENT_HASH", length = CONTENT_HASH_COLUMN_LENGTH)
	public String getContentHash() {
		return contentHash;
	}

	public void setContentHash(final String contentHash) {
		this.contentHash = contentHash;
	}

	@Basic
	@Column(name = "CONTENT", length = CONTENT_COLUMN_LENGTH, columnDefinition = "Text")
	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumns({
			@JoinColumn(name = "TYPE", referencedColumnName = "TYPE"),
			@JoinColumn(name = "STORE", referencedColumnName = "STORE"),
			@JoinColumn(name = "CODE", referencedColumnName = "CODE")
	})
	public ProjectionEntity getProjectionEntity() {
		return projectionEntity;
	}

	public void setProjectionEntity(final ProjectionEntity projectionEntity) {
		this.projectionEntity = projectionEntity;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof ProjectionHistoryEntity)) {
			return false;
		}

		final ProjectionHistoryEntity otherEntity = (ProjectionHistoryEntity) other;
		return Objects.equals(getHistoryId(), otherEntity.getHistoryId());
	}

	@Transient
	public String getType() {
		return getHistoryId().getType();
	}

	@Transient
	public String getStore() {
		return getHistoryId().getStore();
	}

	@Transient
	public Long getVersion() {
		return getHistoryId().getVersion();
	}

	@Transient
	public String getCode() {
		return getHistoryId().getCode();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getHistoryId());
	}

}
