/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.brand;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.BRAND_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;

import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.offer.Association;
import com.elasticpath.catalog.entity.offer.Components;
import com.elasticpath.catalog.entity.offer.Item;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.offer.OfferAvailabilityRules;
import com.elasticpath.catalog.entity.offer.OfferCategories;
import com.elasticpath.catalog.entity.offer.SelectionRules;
import com.elasticpath.catalog.entity.translation.DetailsTranslation;
import com.elasticpath.catalog.entity.translation.OfferTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.entity.translation.TranslationUnit;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.service.misc.TimeService;

/**
 * An implementation of {@link BrandBulkUpdateProcessor}.
 */
public class BrandBulkUpdateProcessorImpl implements BrandBulkUpdateProcessor {

	private final CatalogService catalogService;
	private final TimeService timeService;

	/**
	 * Constructor.
	 *
	 * @param catalogService catalogService.
	 * @param timeService    timeService.
	 */
	public BrandBulkUpdateProcessorImpl(final CatalogService catalogService, final TimeService timeService) {
		this.catalogService = catalogService;
		this.timeService = timeService;
	}

	@Override
	public void updateBrandDisplayNamesInOffers(final List<String> products, final String brand) {

		final List<Brand> brands = catalogService.readAll(BRAND_IDENTITY_TYPE, brand);
		final List<Offer> offers = catalogService.readAll(OFFER_IDENTITY_TYPE, products);

		final List<Offer> updatedOffers = offers.stream()
				.filter(offer -> !offer.isDeleted())
				.map(offer -> createOfferWithUpdatedBrandDisplay(offer, findBrandForStore(brands, offer.getIdentity().getStore())))
				.collect(Collectors.toList());

		catalogService.saveOrUpdateAll(updatedOffers);
	}

	private Brand findBrandForStore(final List<Brand> brands, final String store) {
		return brands.stream()
				.filter(brand -> brand.getIdentity().getStore().equals(store)).findAny()
				.orElseThrow(() -> new EntityNotFoundException("Brand projection for " + store + " not found"));
	}

	private Offer createOfferWithUpdatedBrandDisplay(final Offer offer, final Brand updatedBrand) {
		final NameIdentity identity = offer.getIdentity();
		final ZonedDateTime modifiedDateTime = ZonedDateTime.ofInstant(timeService.getCurrentTime().toInstant(), ZoneId.of("GMT"));
		final boolean deleted = offer.isDeleted();
		final List<Item> items = offer.getItems();
		final Object extensions = offer.getExtensions();
		final List<Property> properties = offer.getProperties();
		final OfferAvailabilityRules availabilityRules = offer.getAvailabilityRules();
		final List<Association> associations = offer.getAssociations();
		final SelectionRules selectionRules = offer.getSelectionRules();
		final Components components = offer.getComponents();
		final List<String> formFields = offer.getFormFields();
		final List<OfferTranslation> translations = offer.getTranslations();
		final Set<OfferCategories> categories = offer.getCategories();

		final List<OfferTranslation> updatedTranslations = translations.stream()
				.map(translation -> createOfferTranslationWithUpdatedBrandDisplayName(translation, updatedBrand.getTranslations()))
				.collect(Collectors.toList());

		return new Offer(identity, modifiedDateTime, deleted, items, extensions, properties, availabilityRules, associations, selectionRules,
				components, formFields, updatedTranslations, categories);
	}

	private OfferTranslation createOfferTranslationWithUpdatedBrandDisplayName(final OfferTranslation offerTranslation,
																			   final List<Translation> brandTranslations) {
		final String language = offerTranslation.getLanguage();
		final String displayName = offerTranslation.getDisplayName();
		final TranslationUnit brand = offerTranslation.getBrand();
		final List<TranslationUnit> options = offerTranslation.getOptions();
		final List<DetailsTranslation> details = offerTranslation.getDetails();

		final String updatedDisplayName = brandTranslations.stream()
				.filter(translation -> language.equals(translation.getLanguage()))
				.map(Translation::getDisplayName)
				.findFirst().orElse(null);

		final String name = brand.getName();

		final TranslationUnit newTranslationUnit = new TranslationUnit(updatedDisplayName, name);

		return new OfferTranslation(language, displayName, newTranslationUnit, options, details);
	}

}
