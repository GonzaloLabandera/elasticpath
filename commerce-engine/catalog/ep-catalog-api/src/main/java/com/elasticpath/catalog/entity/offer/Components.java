/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represents an offer component entity.
 */
@JsonView(ProjectionView.ContentOnly.class)
public class Components {
	private final List<Component> list;

	/**
	 * Constructor.
	 *
	 * @param list is list of Component.
	 */
	@JsonCreator
	public Components(@JsonProperty("list") final List<Component> list) {
		this.list = list;
	}

	/**
	 * Get list of Component.
	 *
	 * @return list of Component.
	 */
	public List<Component> getList() {
		return list;
	}
}
