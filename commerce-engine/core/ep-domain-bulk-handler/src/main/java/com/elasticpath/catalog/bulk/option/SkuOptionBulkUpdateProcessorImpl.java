/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.option;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OPTION_IDENTITY_TYPE;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.entity.TranslatedName;
import com.elasticpath.catalog.entity.offer.Item;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.entity.translation.ItemOptionTranslation;
import com.elasticpath.catalog.entity.translation.ItemTranslation;
import com.elasticpath.catalog.entity.translation.OfferTranslation;
import com.elasticpath.catalog.entity.translation.OptionTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.entity.translation.TranslationUnit;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.service.misc.TimeService;

/**
 * An implementation of {@link SkuOptionBulkUpdateProcessor}.
 */
public class SkuOptionBulkUpdateProcessorImpl implements SkuOptionBulkUpdateProcessor {
	private final CatalogService catalogService;
	private final TimeService timeService;

	/**
	 * Constructor.
	 *
	 * @param catalogService catalogService.
	 * @param timeService    timeService.
	 */
	public SkuOptionBulkUpdateProcessorImpl(final CatalogService catalogService, final TimeService timeService) {
		this.catalogService = catalogService;
		this.timeService = timeService;
	}

	@Override
	public void updateSkuOptionDisplayNamesInOffers(final List<String> products, final String skuOption) {

		final List<Option> skuOptions = catalogService.readAll(OPTION_IDENTITY_TYPE, skuOption);
		final List<Offer> offers = catalogService.readAll(OFFER_IDENTITY_TYPE, products);

		final List<Offer> updatedOffers = offers.stream().filter(offer -> !offer.isDeleted())
				.map(offer -> createOfferWithUpdatedSkuOptionDisplayName(offer, findSkuOptionForStore(skuOptions, offer.getIdentity().getStore())))
				.collect(Collectors.toList());
		catalogService.saveOrUpdateAll(updatedOffers);
	}

	private Option findSkuOptionForStore(final List<Option> skuOptions, final String store) {
		return skuOptions.stream()
				.filter(skuOption -> skuOption.getIdentity().getStore().equals(store)).findAny()
				.orElseThrow(() -> new IllegalArgumentException("Option projection for " + store + " not found"));
	}

	private Offer createOfferWithUpdatedSkuOptionDisplayName(final Offer offer, final Option updatedSkuOption) {
		final List<OfferTranslation> offerTranslations = offer.getTranslations().stream()
				.map(translation -> createOfferTranslationWithUpdatedSkuOptionDisplayName(translation, updatedSkuOption))
				.collect(Collectors.toList());
		final List<Item> newItems = offer.getItems().stream()
				.map(item -> createItemsWithUpdatedSkuOptionDisplayName(item, updatedSkuOption))
				.collect(Collectors.toList());

		return new Offer(offer.getIdentity(),
				ZonedDateTime.ofInstant(timeService.getCurrentTime().toInstant(), ZoneId.of("GMT")),
				offer.isDeleted(),
				newItems,
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

	private Item createItemsWithUpdatedSkuOptionDisplayName(final Item item, final Option option) {
		final List<ItemTranslation> itemTranslations = new ArrayList<>(item.getTranslations().size());
		for (ItemTranslation itemTranslation : item.getTranslations()) {
			final List<ItemOptionTranslation> itemOptionTranslations = buildItemOptionTranslations(option, itemTranslation);
			final ItemTranslation newItemTranslation = new ItemTranslation(itemTranslation.getLanguage(),
					itemTranslation.getDetails(), itemOptionTranslations);
			itemTranslations.add(newItemTranslation);
		}
		return new Item(item.getItemCode(), item.getExtensions(), item.getProperties(), item.getAvailabilityRules(), item.getShippingProperties(),
				itemTranslations);
	}

	private List<ItemOptionTranslation> buildItemOptionTranslations(final Option option, final ItemTranslation itemTranslation) {
		final List<ItemOptionTranslation> itemOptionTranslations = new ArrayList<>(itemTranslation.getOptions().size());
		for (ItemOptionTranslation itemOptionTranslation : itemTranslation.getOptions()) {
			itemOptionTranslations.add(getItemOptionTranslation(
					option,
					option.getTranslations(),
					itemTranslation.getLanguage(),
					itemOptionTranslation));
		}
		return itemOptionTranslations;
	}

	private ItemOptionTranslation getItemOptionTranslation(final Projection option, final List<OptionTranslation> translations,
														   final String language,
														   final ItemOptionTranslation itemOptionTranslation) {
		if (itemOptionTranslation.getName().equals(option.getIdentity().getCode())) {
			final String value = itemOptionTranslation.getValue();
			return new ItemOptionTranslation(
					getDisplayNameValue(translations, language, itemOptionTranslation),
					itemOptionTranslation.getName(),
					getDisplayValue(translations, language, itemOptionTranslation, value),
					value);
		}
		return itemOptionTranslation;

	}

	private String getDisplayValue(final List<OptionTranslation> translations, final String language,
								   final ItemOptionTranslation itemOptionTranslation,
								   final String value) {
		for (OptionTranslation translation : translations) {
			Optional<TranslatedName> translatedName =
					translation.getOptionValues().stream().filter(info -> (translation.getLanguage().equals(language)
							&& info.getValue().equals(value))).findFirst();
			if (translatedName.isPresent()) {
				return translatedName.get().getDisplayValue();
			}
		}
		return itemOptionTranslation.getDisplayValue();
	}

	private String getDisplayNameValue(final List<OptionTranslation> translations, final String language,
									   final ItemOptionTranslation itemOptionTranslation) {
		return translations.stream()
				.filter(translation -> language.equals(translation.getLanguage()))
				.map(Translation::getDisplayName)
				.findFirst().orElse(itemOptionTranslation.getDisplayName());
	}

	private OfferTranslation createOfferTranslationWithUpdatedSkuOptionDisplayName(final OfferTranslation offerTranslation,
																				   final Option skuOption) {
		final List<OptionTranslation> skuOptionTranslations = skuOption.getTranslations();
		final String language = offerTranslation.getLanguage();
		final String updatedDisplayName = skuOptionTranslations.stream().filter(translation -> language.equals(translation.getLanguage()))
				.map(Translation::getDisplayName).findFirst().orElse(null);
		return new OfferTranslation(
				language,
				offerTranslation.getDisplayName(),
				offerTranslation.getBrand(),
				buildTranslationUnits(offerTranslation, skuOption, updatedDisplayName),
				offerTranslation.getDetails());
	}

	private List<TranslationUnit> buildTranslationUnits(final OfferTranslation offerTranslation, final Projection projection,
														final String updatedDisplayName) {
		final List<TranslationUnit> translationUnits = new ArrayList<>();
		for (TranslationUnit option : offerTranslation.getOptions()) {
			if (projection.getIdentity().getCode().equals(option.getName())) {
				final String name = option.getName();
				translationUnits.add(new TranslationUnit(updatedDisplayName, name));
			} else {
				translationUnits.add(option);
			}
		}
		return translationUnits;
	}
}
