/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity;

import java.beans.Transient;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a base entity for projections.
 */
public abstract class AbstractProjection implements Projection {

	private final ZonedDateTime modifiedDateTime;
	private final boolean deleted;

	/**
	 * Constructor for AbstractProjection.
	 *
	 * @param deleted          flag for deleted projection.
	 * @param modifiedDateTime zoned date time the projection was modified.
	 */
	@JsonCreator
	public AbstractProjection(@JsonProperty("modifiedDateTime") final ZonedDateTime modifiedDateTime,
							  @JsonProperty("deleted") final boolean deleted) {
		this.modifiedDateTime = modifiedDateTime;
		this.deleted = deleted;
	}

	@Override
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx")
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

	/**
	 * Return the disable date time of Projection.
	 *
	 * @return the disable date time.
	 */
	@Override
	@Transient
	public ZonedDateTime getDisableDateTime() {
		return null;
	}

}
