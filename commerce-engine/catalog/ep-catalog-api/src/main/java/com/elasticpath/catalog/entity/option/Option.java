/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.option;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OPTION_IDENTITY_TYPE;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.AbstractProjection;
import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.translation.OptionTranslation;
import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * An Option represents a facet by which offers may be configured. For example, an Offer representing a T-Shirt may have an Option representing the
 * COLOUR - red, blue, or green - and an Option representing the SIZE - small, medium, or large. A shopper would select from the two Options to
 * configure the T-shirt by selecting a COLOUR and SIZE combination.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"identity", "modifiedDateTime", "deleted", "translations"})
public class Option extends AbstractProjection {

	private final NameIdentity identity;
	private final List<OptionTranslation> translations;

	/**
	 * Option constructor.
	 *
	 * @param code             code of projection.
	 * @param store            store code.
	 * @param translations     list of {@link com.elasticpath.catalog.entity.translation.Translation}.
	 * @param deleted          flag for deleted projection.
	 * @param modifiedDateTime date and time when projection is modified.
	 */
	@JsonCreator
	public Option(@JsonProperty("code") final String code,
				  @JsonProperty("store") final String store,
				  @JsonProperty("translations") final List<OptionTranslation> translations,
				  @JsonProperty("modifiedDateTime") final ZonedDateTime modifiedDateTime,
				  @JsonProperty("deleted") final boolean deleted) {
		super(modifiedDateTime, deleted);
		this.identity = new NameIdentity(OPTION_IDENTITY_TYPE, code, store);
		this.translations = translations;
	}

	/**
	 * Return the identity.
	 *
	 * @return the identity {@link NameIdentity}.
	 */
	public NameIdentity getIdentity() {
		return identity;
	}

	/**
	 * Return the translations.
	 *
	 * @return the list of {@link OptionTranslation}.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<OptionTranslation> getTranslations() {
		return translations;
	}

}
