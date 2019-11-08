/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.translation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represent translations for {@link com.elasticpath.catalog.entity.attribute.Attribute}.
 */
public class AttributeTranslation extends Translation {

	private final String dataType;
	private final Boolean multiValue;

	/**
	 * Translation constructor.
	 *
	 * @param language    translation language.
	 * @param displayName translation displayName.
	 * @param dataType    translation dataType.
	 * @param multiValue  translation multiValue.
	 */
	@JsonCreator
	public AttributeTranslation(@JsonProperty("language") final String language, @JsonProperty("displayName")final String displayName,
		@JsonProperty("dataType")final String dataType, @JsonProperty("multiValue")final Boolean multiValue) {
		super(language, displayName);
		this.dataType = dataType;
		this.multiValue = multiValue;
	}

	/**
	 * Translation constructor.
	 *
	 * @param translation base translation.
	 * @param dataType    translation dataType.
	 * @param multiValue  translation multiValue.
	 */
	public AttributeTranslation(final Translation translation, final String dataType, final Boolean multiValue) {
		this(translation.getLanguage(), translation.getDisplayName(), dataType, multiValue);
	}

	/**
	 * Get the dataType.
	 *
	 * @return the dataType.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getDataType() {
		return dataType;
	}

	/**
	 * Get the multiValue.
	 *
	 * @return the multiValue.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public Boolean getMultiValue() {
		return multiValue;
	}

}
