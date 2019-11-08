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

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.offer.Item;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.translation.AttributeTranslation;
import com.elasticpath.catalog.entity.translation.DetailsTranslation;
import com.elasticpath.catalog.entity.translation.ItemTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.entity.translation.TranslationUnit;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.service.misc.TimeService;

/**
 * An implementation of {@link AttributeCategoryBulkUpdateProcessor}.
 */
public class AttributeSkuBulkUpdateProcessorImpl implements AttributeSkuBulkUpdateProcessor {
	private final CatalogService catalogService;
	private final TimeService timeService;

	/**
	 * Constructor.
	 *
	 * @param catalogService catalogService.
	 * @param timeService    timeService.
	 */
	public AttributeSkuBulkUpdateProcessorImpl(final CatalogService catalogService, final TimeService timeService) {
		this.catalogService = catalogService;
		this.timeService = timeService;
	}

	@Override
	public void updateSkuAttributeDisplayNameInOffers(final List<String> products, final String attribute) {
		final List<Attribute> attributes = catalogService.readAll(ATTRIBUTE_IDENTITY_TYPE, attribute);
		final List<Offer> offers = catalogService.readAll(OFFER_IDENTITY_TYPE, products);

		final List<Offer> updatedOffers = offers.stream().filter(offer -> !offer.isDeleted())
				.map(offer -> createOfferWithUpdatedSkuAttributeDisplayName(offer, findAttributeForStore(attributes,
						offer.getIdentity().getStore())))
				.collect(Collectors.toList());
		catalogService.saveOrUpdateAll(updatedOffers);
	}

	private Offer createOfferWithUpdatedSkuAttributeDisplayName(final Offer offer, final Attribute updatedAttribute) {
		final List<Item> offerItems = offer.getItems().stream()
				.map(item -> createOfferItemWithUpdatedSkuAttributeDisplayName(item, updatedAttribute))
				.collect(Collectors.toList());

		return new Offer(offer.getIdentity(),
				ZonedDateTime.ofInstant(timeService.getCurrentTime().toInstant(), ZoneId.systemDefault()),
				offer.isDeleted(),
				offerItems,
				offer.getExtensions(),
				offer.getProperties(),
				offer.getAvailabilityRules(),
				offer.getAssociations(),
				offer.getSelectionRules(),
				offer.getComponents(),
				offer.getFormFields(),
				offer.getTranslations(),
				offer.getCategories());
	}

	private Item createOfferItemWithUpdatedSkuAttributeDisplayName(final Item item, final Attribute attribute) {
		final List<ItemTranslation> itemTranslations = new ArrayList<>(item.getTranslations().size());
		for (ItemTranslation itemTranslation : item.getTranslations()) {
			final List<DetailsTranslation> detailsTranslations = buildDetailTranslations(attribute, itemTranslation);
			final ItemTranslation newItemTranslation = new ItemTranslation(itemTranslation.getLanguage(),
					detailsTranslations, itemTranslation.getOptions());
			itemTranslations.add(newItemTranslation);
		}
		return new Item(item.getItemCode(), item.getExtensions(), item.getProperties(), item.getAvailabilityRules(), item.getShippingProperties(),
				itemTranslations);
	}

	private List<DetailsTranslation> buildDetailTranslations(final Attribute attribute, final ItemTranslation itemTranslation) {
		final List<DetailsTranslation> detailsTranslations = new ArrayList<>();
		final List<AttributeTranslation> attributeTranslations = attribute.getTranslations();
		final String language = itemTranslation.getLanguage();
		final String updatedDisplayName = attributeTranslations.stream().filter(translation -> language.equals(translation.getLanguage()))
				.map(Translation::getDisplayName).findFirst().orElse(null);
		for (DetailsTranslation detailsTranslation : itemTranslation.getDetails()) {
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
