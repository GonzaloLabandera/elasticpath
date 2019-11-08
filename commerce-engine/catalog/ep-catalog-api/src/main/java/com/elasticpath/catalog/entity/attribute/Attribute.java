/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.catalog.entity.attribute;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.ATTRIBUTE_IDENTITY_TYPE;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.AbstractProjection;
import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.translation.AttributeTranslation;
import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * <p>An Attribute represents a trait or characteristic of an entity. For example, an Offer may have an Attribute 'FABRIC' representing the type of
 * fabric of which it is made. In this example, many different Offers would share the same Attribute, but may have different values.</p>
 * <p>Each Attribute may be used as a key to discover the corresponding attribute value from an entity instance.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"identity", "modifiedDateTime", "deleted", "translations"})
public class Attribute extends AbstractProjection {

	private final NameIdentity identity;
	private final List<AttributeTranslation> translations;

	/**
	 * Constructor with parameters for Attribute.
	 *
	 * @param code             name of Attribute.
	 * @param store            code of the Store, that related with Attribute.
	 * @param translations     list of {@link com.elasticpath.catalog.entity.translation.Translation} of Attribute.
	 * @param deleted          flag for deleted projection.
	 * @param modifiedDateTime date and time when projection is modified.
	 */
	@JsonCreator
	public Attribute(@JsonProperty("code") final String code,
					 @JsonProperty("store") final String store,
					 @JsonProperty("translations") final List<AttributeTranslation> translations,
					 @JsonProperty("modifiedDateTime") final ZonedDateTime modifiedDateTime,
					 @JsonProperty("deleted") final boolean deleted) {
		super(modifiedDateTime, deleted);
		this.identity = new NameIdentity(ATTRIBUTE_IDENTITY_TYPE, code, store);
		this.translations = translations;
	}

	/**
	 * Return translations.
	 *
	 * @return list of {@link AttributeTranslation}
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<AttributeTranslation> getTranslations() {
		return translations;
	}

	/**
	 * Return the identity of Projection.
	 *
	 * @return the identity {@link NameIdentity}
	 */
	public NameIdentity getIdentity() {
		return identity;
	}

}
