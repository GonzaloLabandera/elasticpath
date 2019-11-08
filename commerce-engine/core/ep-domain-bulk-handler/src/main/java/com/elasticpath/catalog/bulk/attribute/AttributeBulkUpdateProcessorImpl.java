/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.attribute;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.ATTRIBUTE_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.translation.AttributeTranslation;
import com.elasticpath.catalog.entity.translation.DetailsTranslation;
import com.elasticpath.catalog.entity.translation.OfferTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.entity.translation.TranslationUnit;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.service.misc.TimeService;

/**
 * An implementation of {@link AttributeBulkUpdateProcessor}.
 */
public class AttributeBulkUpdateProcessorImpl implements AttributeBulkUpdateProcessor {
	private final CatalogService catalogService;
	private final TimeService timeService;

	/**
	 * Constructor.
	 *
	 * @param catalogService catalogService.
	 * @param timeService    timeService.
	 */
	public AttributeBulkUpdateProcessorImpl(final CatalogService catalogService, final TimeService timeService) {
		this.catalogService = catalogService;
		this.timeService = timeService;
	}

	@Override
	public void updateAttributeDisplayNameInOffers(final List<String> products, final String attribute) {
		final List<Attribute> attributes = catalogService.readAll(ATTRIBUTE_IDENTITY_TYPE, attribute);
		final List<Offer> offers = catalogService.readAll(OFFER_IDENTITY_TYPE, products);

		final List<Offer> updatedOffers = offers.stream().filter(offer -> !offer.isDeleted())
				.map(offer -> createOfferWithUpdatedAttributeDisplayName(offer, findAttributeForStore(attributes, offer.getIdentity().getStore())))
				.collect(Collectors.toList());
		catalogService.saveOrUpdateAll(updatedOffers);
	}

	private Offer createOfferWithUpdatedAttributeDisplayName(final Offer offer, final Attribute updatedAttribute) {
		final List<OfferTranslation> offerTranslations = offer.getTranslations().stream()
				.map(translation -> createOfferTranslationWithUpdatedAttributeDisplayName(translation, updatedAttribute))
				.collect(Collectors.toList());

		return new Offer(offer.getIdentity(),
				ZonedDateTime.ofInstant(timeService.getCurrentTime().toInstant(), ZoneId.systemDefault()),
				offer.isDeleted(),
				offer.getItems(),
				offer.getExtensions(),
				offer.getProperties(),
				offer.getAvailabilityRules(),
				offer.getAssociations(),
				offer.getSelectionRules(),
				offer.getComponents(),
				offer.getFormFields(),
				offerTranslations,
				offer.getCategories());
	}

	private OfferTranslation createOfferTranslationWithUpdatedAttributeDisplayName(final OfferTranslation offerTranslation,
																				   final Attribute attribute) {
		final List<AttributeTranslation> attributeTranslations = attribute.getTranslations();
		final String language = offerTranslation.getLanguage();
		final String updatedDisplayName = attributeTranslations.stream().filter(translation -> language.equals(translation.getLanguage()))
				.map(Translation::getDisplayName).findFirst().orElse(null);
		return new OfferTranslation(
				language,
				offerTranslation.getDisplayName(),
				offerTranslation.getBrand(),
				offerTranslation.getOptions(),
				buildDetails(offerTranslation, attribute, updatedDisplayName));
	}

	private List<DetailsTranslation> buildDetails(final OfferTranslation offerTranslation, final Projection attribute,
												  final String updatedDisplayName) {
		final List<DetailsTranslation> detailsTranslations = new ArrayList<>();
		for (DetailsTranslation detailsTranslation : offerTranslation.getDetails()) {
			if (attribute.getIdentity().getCode().equals(detailsTranslation.getName())) {
				detailsTranslations.add(new DetailsTranslation(new TranslationUnit(updatedDisplayName,
						detailsTranslation.getName()), detailsTranslation.getDisplayValues(), detailsTranslation.getValues()));
			} else {
				detailsTranslations.add(detailsTranslation);
			}
		}
		return detailsTranslations;
	}


	private Attribute findAttributeForStore(final List<Attribute> attributes, final String store) {
		return attributes.stream()
				.filter(attribute -> attribute.getIdentity().getStore().equals(store)).findAny()
				.orElseThrow(() -> new IllegalArgumentException("Attribute projection for " + store + " not found"));
	}
}
