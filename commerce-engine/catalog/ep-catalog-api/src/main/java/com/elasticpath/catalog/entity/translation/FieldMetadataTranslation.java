/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.translation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.Field;
import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represent translations for {@link com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata}.
 */
public class FieldMetadataTranslation extends Translation {

	private final List<Field> fields;

	/**
	 * Translation constructor.
	 *
	 * @param language    translation language.
	 * @param displayName translation displayName.
	 * @param fields      list of {@link Field}.
	 */
	@JsonCreator
	public FieldMetadataTranslation(@JsonProperty("language") final String language, @JsonProperty("displayName") final String displayName,
									@JsonProperty("fields") final List<Field> fields) {
		super(language, displayName);
		this.fields = fields;
	}

	/**
	 * Get the fields.
	 *
	 * @return the fields
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<Field> getFields() {
		return fields;
	}

}
