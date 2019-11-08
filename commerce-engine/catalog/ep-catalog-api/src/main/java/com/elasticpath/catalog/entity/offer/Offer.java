/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;

import com.elasticpath.catalog.entity.AbstractProjection;
import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.translation.OfferTranslation;
import com.elasticpath.catalog.entity.view.ProjectionView;

/**
 * Represent a projection entity for StoreProduct domain entity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"identity", "modifiedDateTime", "deleted", "properties", "formFields", "availabilityRules", "selectionRules", "components",
		"translations", "categories", "associations", "extensions", "items", "extensions"})
public class Offer extends AbstractProjection {

	private final NameIdentity identity;
	private final List<Item> items;
	private final Object extensions;
	private final List<Property> properties;
	private final OfferAvailabilityRules availabilityRules;
	private final List<Association> associations;
	private final SelectionRules selectionRules;
	private final Components components;
	private final List<String> formFields;
	private final List<OfferTranslation> translations;
	private final Set<OfferCategories> categories;

	/**
	 * Constructor.
	 *
	 * @param offerProperties offer properties.
	 * @param items           list of Offer items.
	 * @param extensions      extensions of Offer.
	 * @param associations    offer associations.
	 * @param components      offer components.
	 * @param rules           offer rules.
	 * @param formFields      list of form fields for projection entity.
	 * @param translations    list of form translations for projection entity.
	 * @param categories      set of offer categories.
	 */
	@JsonCreator
	@SuppressWarnings({"PMD.ExcessiveParameterList", "checkstyle:parameternumber"})
	public Offer(@JsonProperty("offerProperties") final OfferProperties offerProperties,
				 @JsonProperty("items") final List<Item> items,
				 @JsonProperty("extensions") final Object extensions,
				 @JsonProperty("associations") final List<Association> associations,
				 @JsonProperty("components") final Components components,
				 @JsonProperty("rules") final OfferRules rules,
				 @JsonProperty("formFields") final List<String> formFields,
				 @JsonProperty("translations") final List<OfferTranslation> translations,
				 @JsonProperty("categories") final Set<OfferCategories> categories) {
		super(offerProperties.getProjectionProperties().getModifiedDateTime(), offerProperties.getProjectionProperties().isDeleted());

		this.identity = new NameIdentity(OFFER_IDENTITY_TYPE, offerProperties.getProjectionProperties().getCode(),
				offerProperties.getProjectionProperties().getStore());
		this.items = items;
		this.extensions = extensions;
		this.properties = offerProperties.getOfferSpecificProperties();
		this.availabilityRules = rules.getAvailabilityRules();
		this.associations = associations;
		this.selectionRules = rules.getSelectionRules();
		this.components = components;
		this.formFields = formFields;
		this.translations = translations;
		this.categories = categories;
	}

	/**
	 * Constructor.
	 *
	 * @param identity          identity of Offer.
	 * @param modifiedDateTime  modified date.
	 * @param deleted           flag for deleted projection.
	 * @param items             list of Offer items.
	 * @param extensions        extensions of Offer.
	 * @param properties        offer properties.
	 * @param availabilityRules availability rules.
	 * @param associations      offer associations.
	 * @param selectionRules    selection rules.
	 * @param components        offer components.
	 * @param formFields        list of form fields for projection entity.
	 * @param translations      list of form translations for projection entity.
	 * @param categories        set of offer categories.
	 */
	@SuppressWarnings({"PMD.ExcessiveParameterList", "checkstyle:parameternumber"})
	public Offer(final NameIdentity identity, final ZonedDateTime modifiedDateTime, final boolean deleted, final List<Item> items,
				 final Object extensions, final List<Property> properties, final OfferAvailabilityRules availabilityRules,
				 final List<Association> associations, final SelectionRules selectionRules, final Components components,
				 final List<String> formFields, final List<OfferTranslation> translations, final Set<OfferCategories> categories) {

		super(modifiedDateTime, deleted);

		this.identity = identity;
		this.items = items;
		this.extensions = extensions;
		this.properties = properties;
		this.availabilityRules = availabilityRules;
		this.associations = associations;
		this.selectionRules = selectionRules;
		this.components = components;
		this.formFields = formFields;
		this.translations = translations;
		this.categories = categories;
	}

	/**
	 * Constructor.
	 *
	 * @param modifiedDateTime modified date.
	 * @param code             code.
	 * @param store            store.
	 * @param deleted          flag for deleted projection.
	 */
	public Offer(final String code, final String store, final ZonedDateTime modifiedDateTime, final boolean deleted) {
		super(modifiedDateTime, deleted);
		this.identity = new NameIdentity(OFFER_IDENTITY_TYPE, code, store);
		this.items = null;
		this.extensions = null;
		this.properties = null;
		this.availabilityRules = null;
		this.associations = null;
		this.selectionRules = null;
		this.components = null;
		this.formFields = null;
		this.translations = null;
		this.categories = null;
	}

	@Override
	public NameIdentity getIdentity() {
		return identity;
	}

	/**
	 * Get items.
	 *
	 * @return list of items.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<Item> getItems() {
		return items;
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
	 * Get properties.
	 *
	 * @return list of properties.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<Property> getProperties() {
		return properties;
	}

	/**
	 * Get availability rules.
	 *
	 * @return availability rules.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public OfferAvailabilityRules getAvailabilityRules() {
		return availabilityRules;
	}

	/**
	 * Get offer associations.
	 *
	 * @return offer associations.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<Association> getAssociations() {
		return associations;
	}

	/**
	 * Get offer selectionRules.
	 *
	 * @return offer selectionRules.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public SelectionRules getSelectionRules() {
		return selectionRules;
	}

	/**
	 * Get offer components.
	 *
	 * @return offer components.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public Components getComponents() {
		return components;
	}

	/**
	 * Get offer form fields.
	 *
	 * @return offer form fields.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public List<String> getFormFields() {
		return formFields;
	}

	/**
	 * Get offer translations.
	 *
	 * @return offer translations.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public List<OfferTranslation> getTranslations() {
		return translations;
	}

	/**
	 * Get categories.
	 *
	 * @return set of offer categories.
	 */
	@JsonView(ProjectionView.ContentOnly.class)
	public Set<OfferCategories> getCategories() {
		return categories;
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
