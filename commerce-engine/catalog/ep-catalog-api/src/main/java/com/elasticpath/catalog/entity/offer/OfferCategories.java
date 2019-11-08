/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represents categories for {@link Offer}.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"code", "path", "enableDateTime", "disableDateTime", "default", "featured"})
public class OfferCategories {
	private final String code;
	private final List<String> path;
	private final ZonedDateTime enableDateTime;
	private final ZonedDateTime disableDateTime;
	private final boolean defaultCategory;
	private final Integer featured;

	/**
	 * Constructor.
	 *
	 * @param code            offer category code.
	 * @param path            offer category path.
	 * @param enableDateTime  is enableDateTime of offer category.
	 * @param disableDateTime is disableDateTime of offer category.
	 * @param defaultCategory is the default offer category.
	 * @param featured        featured product order.
	 */
	@JsonCreator
	public OfferCategories(@JsonProperty("code") final String code,
						   @JsonProperty("path") final List<String> path,
						   @JsonProperty("enableDateTime") final ZonedDateTime enableDateTime,
						   @JsonProperty("disableDateTime") final ZonedDateTime disableDateTime,
						   @JsonProperty("default") final boolean defaultCategory,
						   @JsonProperty("featured") final Integer featured) {
		this.code = code;
		this.path = path;
		this.enableDateTime = enableDateTime;
		this.disableDateTime = disableDateTime;
		this.defaultCategory = defaultCategory;
		this.featured = featured;
	}

	/**
	 * Get offer category code.
	 *
	 * @return offer category code.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getCode() {
		return code;
	}

	/**
	 * Get offer category path.
	 *
	 * @return path.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<String> getPath() {
		return path;
	}

	/**
	 * Get enableDateTime of offer category.
	 *
	 * @return enableDateTime of offer category.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx")
	public ZonedDateTime getEnableDateTime() {
		return enableDateTime;
	}

	/**
	 * Get disableDateTime of offer category.
	 *
	 * @return enableDateTime of offer category.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx")
	public ZonedDateTime getDisableDateTime() {
		return disableDateTime;
	}

	/**
	 * Check is category default.
	 *
	 * @return true if this offer category is a default.
	 */
	@JsonProperty("default")
	@JsonView(ProjectionView.ContentOnly.class)
	public boolean isDefaultCategory() {
		return defaultCategory;
	}

	/**
	 * Get the featured of offer category.
	 *
	 * @return the featured of offer category.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public Integer getFeatured() {
		return featured;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof OfferCategories)) {
			return false;
		}
		final OfferCategories offerCategories = (OfferCategories) other;
		return code.equals(offerCategories.code);
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}
}
