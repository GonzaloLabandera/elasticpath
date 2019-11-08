package com.elasticpath.definitions.stateobjects;


import java.util.Date;
import java.util.Objects;

/**
 * Object to pass the state of generated option projection.
 */
public class Projection {

	/**
	 * Option projection type.
	 */
	public static final String OPTION_TYPE = "option";

	/**
	 * Brand projection type.
	 */
	public static final String BRAND_TYPE = "brand";

	private String store;
	private String code;
	private String version;
	private Date projectionDateTime;
	private int deleted;
	private String schemaVersion;
	private String contentHash;
	private String content;

	public String getStore() {
		return store;
	}

	public void setStore(final String store) {
		this.store = store;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public Date getProjectionDateTime() {
		return projectionDateTime;
	}

	public void setProjectionDateTime(final Date projectionDateTime) {
		this.projectionDateTime = projectionDateTime;
	}

	public int getDeleted() {
		return deleted;
	}

	public void setDeleted(final int deleted) {
		this.deleted = deleted;
	}

	public String getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(final String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public String getContentHash() {
		return contentHash;
	}

	public void setContentHash(final String contentHash) {
		this.contentHash = contentHash;
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		Projection that = (Projection) object;
		return deleted == that.deleted
				&& Objects.equals(store, that.store)
				&& Objects.equals(code, that.code)
				&& Objects.equals(version, that.version)
				&& Objects.equals(projectionDateTime, that.projectionDateTime)
				&& Objects.equals(schemaVersion, that.schemaVersion)
				&& Objects.equals(contentHash, that.contentHash)
				&& Objects.equals(content, that.content);
	}

	@Override
	public int hashCode() {
		return Objects.hash(store, code, version, projectionDateTime, deleted, schemaVersion, contentHash, content);
	}
}
