/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represents entity for Offer associations.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Association {

	private final String type;
	private final List<AssociationValue> list;

	/**
	 * Constructor.
	 *
	 * @param type is association type.
	 * @param list is set of association values.
	 */
	@JsonCreator
	public Association(@JsonProperty("type") final String type, @JsonProperty("list") final List<AssociationValue> list) {
		this.type = type;
		this.list = list;
	}

	/**
	 * Get association type.
	 *
	 * @return association type.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getType() {
		return type;
	}

	/**
	 * Get set of association values.
	 *
	 * @return set of association values.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<AssociationValue> getList() {
		return list;
	}

}
