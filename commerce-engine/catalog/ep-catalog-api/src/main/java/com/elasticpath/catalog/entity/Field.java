/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Data Transfer Object for ModifierField.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Field {
	private final String name;
	private final String displayName;
	private final String dataType;
	private final boolean required;
	private final Integer maxSize;
	private final List<FieldOption> fieldValues;

	/**
	 * Field constructor.
	 *
	 * @param name        is name of field
	 * @param displayName is displayName of field
	 * @param dataType    is dataType of field
	 * @param required    set required of field
	 * @param maxSize     is max size of field
	 * @param fieldValues list of {@link FieldOption}.
	 */
	@JsonCreator
	public Field(@JsonProperty("name") final String name,
				 @JsonProperty("displayName") final String displayName,
				 @JsonProperty("dataType") final String dataType,
				 @JsonProperty("required") final boolean required,
				 @JsonProperty("maxSize") final Integer maxSize,
				 @JsonProperty("fieldValues") final List<FieldOption> fieldValues) {
		this.name = name;
		this.displayName = displayName;
		this.dataType = dataType;
		this.required = required;
		this.maxSize = maxSize;
		this.fieldValues = fieldValues;
	}

	/**
	 * Get the name.
	 *
	 * @return the name
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getName() {
		return name;
	}

	/**
	 * Get the displayName.
	 *
	 * @return the displayName
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Get the dataType.
	 *
	 * @return the dataType
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getDataType() {
		return dataType;
	}

	/**
	 * Get the required.
	 *
	 * @return the required
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public boolean isRequired() {
		return required;
	}

	/**
	 * Get the maxSize.
	 *
	 * @return the maxSize
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public Integer getMaxSize() {
		return maxSize;
	}

	/**
	 * Get the options.
	 *
	 * @return the options
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<FieldOption> getFieldValues() {
		return fieldValues;
	}

}
