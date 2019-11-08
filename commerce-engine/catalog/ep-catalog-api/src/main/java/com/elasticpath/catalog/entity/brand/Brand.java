/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.brand;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.BRAND_IDENTITY_TYPE;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.AbstractProjection;
import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * A brand is a name, term, design, symbol, or other feature that distinguishes an organization or product from its rivals in the eyes of the
 * customer.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"identity", "modifiedDateTime", "deleted", "translations"})
public class Brand extends AbstractProjection {

	private final List<Translation> translations;
	private final NameIdentity identity;

	/**
	 * Brand constructor.
	 *
	 * @param translations     {@link Translation}.
	 * @param code             code of projection.
	 * @param store            name of store.
	 * @param deleted          flag for deleted projection.
	 * @param modifiedDateTime date and time when projection is modified.
	 */
	@JsonCreator
	public Brand(@JsonProperty("code") final String code,
				 @JsonProperty("store") final String store,
				 @JsonProperty("translations") final List<Translation> translations,
				 @JsonProperty("modifiedDateTime") final ZonedDateTime modifiedDateTime,
				 @JsonProperty("deleted") final boolean deleted) {
		super(modifiedDateTime, deleted);
		this.translations = translations;
		this.identity = new NameIdentity(BRAND_IDENTITY_TYPE, code, store);
	}

	/**
	 * Get the translations.
	 *
	 * @return the translations
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<Translation> getTranslations() {
		return translations;
	}

	/**
	 * Get the nameIdentity.
	 *
	 * @return the nameIdentity
	 */
	@Override
	public NameIdentity getIdentity() {
		return identity;
	}
}