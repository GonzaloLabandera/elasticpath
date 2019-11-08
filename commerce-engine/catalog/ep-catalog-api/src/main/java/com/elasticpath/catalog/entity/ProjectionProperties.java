/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents common projection properties.
 */
public class ProjectionProperties {
	private final String code;
	private final String store;
	private final ZonedDateTime modifiedDateTime;
	private final boolean deleted;

	/**
	 * Constructor.
	 *
	 * @param code             projection code.
	 * @param store            projection store.
	 * @param modifiedDateTime projection modifiedDateTime.
	 * @param deleted          flag for deleted projection.
	 */
	@JsonCreator
	public ProjectionProperties(@JsonProperty("code") final String code,
								@JsonProperty("store") final String store,
								@JsonProperty("modifiedDateTime") final ZonedDateTime modifiedDateTime,
								@JsonProperty("deleted") final boolean deleted) {
		this.code = code;
		this.store = store;
		this.modifiedDateTime = modifiedDateTime;
		this.deleted = deleted;
	}

	/**
	 * Return the code.
	 *
	 * @return the code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Return the store code.
	 *
	 * @return the store code.
	 */
	public String getStore() {
		return store;
	}

	/**
	 * Returns the modified date time.
	 *
	 * @return the modified date time.
	 */
	public ZonedDateTime getModifiedDateTime() {
		return modifiedDateTime;
	}

	/**
	 * Field has value "true",if projection deleted.
	 *
	 * @return boolean value.
	 */
	public boolean isDeleted() {
		return deleted;
	}
}
