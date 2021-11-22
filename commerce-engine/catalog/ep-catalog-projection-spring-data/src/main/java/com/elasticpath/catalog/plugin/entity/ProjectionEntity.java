/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.entity;

import java.util.Date;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.elasticpath.persistence.api.AbstractEntityImpl;

/**
 * Represents projection entity to be saved.
 */
@Entity
@Table(name = ProjectionEntity.TABLE_NAME)
public class ProjectionEntity {

	/**
	 * The name of the table & generator to use for persistence.
	 */
	public static final String TABLE_NAME = "TCATALOGPROJECTIONS";

	private static final int SCHEMA_VERSION_COLUMN_LENGTH = 64;
	private static final int CONTENT_HASH_COLUMN_LENGTH = 256;
	private static final int CONTENT_COLUMN_LENGTH = 16_777_215;

	/**
	 * The version number for this projection.
	 */
	private Long version;

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

	/**
	 * The time and date that the projection will be tombstone.
	 */
	private Date disableDateTime;

	/**
	 * The guid of the projection.
	 */
	private String guid;

	/**
	 * The composite primary key.
	 */
	private ProjectionId projectionId;


	@EmbeddedId
	public ProjectionId getProjectionId() {
		return projectionId;
	}

	public void setProjectionId(final ProjectionId projectionId) {
		this.projectionId = projectionId;
	}

	@Version
	@Column(name = "VERSION", nullable = false)
	public Long getVersion() {
		return version;
	}

	public void setVersion(final Long version) {
		this.version = version;
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

	@Column(name = "DISABLE_DATE_TIME")
	public Date getDisableDateTime() {
		return disableDateTime;
	}

	@Basic
	@Column(name = "GUID", length = AbstractEntityImpl.GUID_LENGTH, nullable = false, unique = true)
	public String getGuid() {
		return guid;
	}

	public void setGuid(final String guid) {
		this.guid = guid;
	}

	public void setDisableDateTime(final Date disableDateTime) {
		this.disableDateTime = disableDateTime;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof ProjectionEntity)) {
			return false;
		}

		ProjectionEntity otherEntity = (ProjectionEntity) other;
		return Objects.equals(getProjectionId(), otherEntity.getProjectionId());
	}

	@Transient
	public String getType() {
		return getProjectionId().getType();
	}

	@Transient
	public String getStore() {
		return getProjectionId().getStore();
	}

	@Transient
	public String getCode() {
		return getProjectionId().getCode();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getProjectionId());
	}

}