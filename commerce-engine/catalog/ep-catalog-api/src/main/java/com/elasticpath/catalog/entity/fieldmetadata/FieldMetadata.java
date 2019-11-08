/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.fieldmetadata;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.FIELD_METADATA_IDENTITY_TYPE;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.AbstractProjection;
import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.translation.FieldMetadataTranslation;
import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Field Meta Data represents data that describe other data.
 * Data Transfer Object for ModifierField.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"identity", "modifiedDateTime", "deleted", "translations", "filters"})
public class FieldMetadata extends AbstractProjection {

	private final NameIdentity identity;
	private final List<FieldMetadataTranslation> translations;

	/**
	 * FieldMetadata constructor.
	 * @param code             code of projection.
	 * @param store            name of store.
	 * @param translations     list of {@link com.elasticpath.catalog.entity.translation.Translation}.
	 * @param modifiedDateTime date and time when projection is modified.
	 * @param deleted          flag for deleted projection.
	 */
	@JsonCreator
	public FieldMetadata(@JsonProperty("code") final String code,
						 @JsonProperty("store") final String store,
						 @JsonProperty("translations") final List<FieldMetadataTranslation> translations,
						 @JsonProperty("modifiedDateTime") final ZonedDateTime modifiedDateTime,
						 @JsonProperty("deleted") final boolean deleted) {
		super(modifiedDateTime, deleted);
		this.identity = new NameIdentity(FIELD_METADATA_IDENTITY_TYPE, code, store);
		this.translations = translations;
	}

	public NameIdentity getIdentity() {
		return identity;
	}

	/**
	 * Get the translations.
	 *
	 * @return the translations
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<FieldMetadataTranslation> getTranslations() {
		return translations;
	}
	}

