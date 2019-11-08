/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.catalog.entity.category;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.AbstractProjection;
import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.translation.CategoryTranslation;
import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represent a projection entity for Category domain entity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"identity", "modifiedDateTime", "deleted", "properties", "availabilityRules",
		"path", "parent", "children", "translations", "extensions"})
public class Category extends AbstractProjection {

	private final NameIdentity identity;
	private final Object extensions;
	private final List<CategoryTranslation> translations;
	private final List<String> children;
	private final List<Property> properties;
	private final AvailabilityRules availabilityRules;
	private final List<String> path;
	private final String parent;

	/**
	 * Constructor.
	 *
	 * @param categoryProperties category properties.
	 * @param availabilityRules  category availabilityRules.
	 * @param path               list of path.
	 * @param parent             parent of Category.
	 * @param extensions         extensions of category.
	 * @param translations       list of form translations for projection entity.
	 * @param children           children list.
	 */
	public Category(@JsonProperty("categoryProperties") final CategoryProperties categoryProperties,
					@JsonProperty("extensions") final Object extensions,
					@JsonProperty("translations") final List<CategoryTranslation> translations,
					@JsonProperty("children") final List<String> children,
					@JsonProperty("availabilityRules") final AvailabilityRules availabilityRules,
					@JsonProperty("path") final List<String> path,
					@JsonProperty("parent") final String parent) {
		super(categoryProperties.getProjectionProperties().getModifiedDateTime(), categoryProperties.getProjectionProperties().isDeleted());
		this.identity = new NameIdentity(CATEGORY_IDENTITY_TYPE, categoryProperties.getProjectionProperties().getCode(),
				categoryProperties.getProjectionProperties().getStore());
		this.extensions = extensions;
		this.translations = translations;
		this.children = children;
		this.properties = categoryProperties.getCategorySpecificProperties();
		this.availabilityRules = availabilityRules;
		this.path = path;
		this.parent = parent;
	}

	/**
	 * Constructor for tombstoned category.
	 *
	 * @param categoryProperties category properties.
	 */
	public Category(final CategoryProperties categoryProperties) {
		super(categoryProperties.getProjectionProperties().getModifiedDateTime(), categoryProperties.getProjectionProperties().isDeleted());
		this.identity = new NameIdentity(CATEGORY_IDENTITY_TYPE, categoryProperties.getProjectionProperties().getCode(),
				categoryProperties.getProjectionProperties().getStore());
		this.extensions = null;
		this.translations = null;
		this.children = null;
		this.properties = categoryProperties.getCategorySpecificProperties();
		this.availabilityRules = null;
		this.path = null;
		this.parent = null;
	}

	@Override
	public NameIdentity getIdentity() {
		return identity;
	}

	/**
	 * Get extensions.
	 *
	 * @return extensions.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public Object getExtensions() {
		return extensions;
	}

	/**
	 * Get category translations.
	 *
	 * @return category translations.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<CategoryTranslation> getTranslations() {
		return translations;
	}

	/**
	 * Get children.
	 *
	 * @return list of children.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<String> getChildren() {
		return children;
	}

	/**
	 * Get properties.
	 *
	 * @return list of properties.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<Property> getProperties() {
		return properties;
	}

	/**
	 * Get availabilityRules.
	 *
	 * @return availabilityRules.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public AvailabilityRules getAvailabilityRules() {
		return availabilityRules;
	}

	/**
	 * Get path.
	 *
	 * @return list of path.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<String> getPath() {
		return path;
	}

	/**
	 * Get parent.
	 *
	 * @return parent.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public String getParent() {
		return parent;
	}

	/**
	 * Return the disable date time of Projection.
	 *
	 * @return the disable date time.
	 */
	@Override
	public ZonedDateTime getDisableDateTime() {
		return Optional.ofNullable(getAvailabilityRules())
				.map(AvailabilityRules::getDisableDateTime)
				.orElse(null);
	}
}
