/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.entity.TranslatedName;
import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.entity.translation.ItemOptionTranslation;
import com.elasticpath.catalog.entity.translation.ItemTranslation;
import com.elasticpath.catalog.entity.translation.OfferTranslation;
import com.elasticpath.catalog.entity.translation.OptionTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.entity.translation.TranslationUnit;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.catalogview.StoreProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.store.Store;

/**
 * Represents a class which implements extracting translation logic for {@link com.elasticpath.catalog.entity.offer.Offer}.
 */
public class OfferTranslationExtractor {

	private final List<Translation> translations;
	private final Brand brand;
	private final List<Option> options;
	private final List<OfferTranslation> offerTranslations;
	private final Map<StoreProductSku, List<ItemTranslation>> itemTranslations;
	private final Store store;
	private final Catalog catalog;
	private final List<Attribute> attributes;

	/**
	 * Constructor.
	 *
	 * @param translations    list of {@link com.elasticpath.catalog.entity.offer.Offer} translation.
	 * @param data           {@link TranslationExtractorData}.
	 * @param storeProductSku list of {@link com.elasticpath.catalog.entity.offer.Offer} storeProductSkus.
	 * @param store           {@link Store}.
	 * @param storeProduct    {@link StoreProduct}.
	 * @param catalog         {@link Catalog}.
	 */
	public OfferTranslationExtractor(final List<Translation> translations,
									 final TranslationExtractorData data,
									 final List<StoreProductSku> storeProductSku,
									 final Store store,
									 final StoreProduct storeProduct,
									 final Catalog catalog) {
		this.translations = translations;
		this.brand = data.getBrand();
		this.options = data.getOptions();
		this.store = store;
		this.catalog = catalog;
		this.attributes = data.getAttributes();
		this.offerTranslations = extractOfferTranslations(translations, options, storeProduct);
		this.itemTranslations = storeProductSku.stream().collect(Collectors.toMap(Function.identity(), this::extractItemTranslations));
	}

	/**
	 * Returns the offer translations.
	 *
	 * @return list of {@link OfferTranslation}
	 */
	public List<OfferTranslation> getOfferTranslations() {
		return offerTranslations;
	}

	/**
	 * Returns the item translations.
	 *
	 * @return map of {@link ItemTranslation}.
	 */
	public Map<StoreProductSku, List<ItemTranslation>> getItemTranslationsMap() {
		return itemTranslations;
	}

	private List<OfferTranslation> extractOfferTranslations(final List<Translation> translations,
															final List<Option> options,
															final StoreProduct storeProduct) {

		return translations.stream()
				.map(translation -> new OfferTranslation(translation,
						extractBrandTranslations(translation),
						extractOptionTranslations(translation, options),
						new DetailsExtractor(storeProduct::getFullAttributeValues,
								translation.getLanguage(),
								storeProduct.getAttributeValueGroup(),
								attributes,
								store,
								catalog)
								.getTranslations()))
				.collect(toList());
	}

	private TranslationUnit extractBrandTranslations(final Translation translation) {
		return Optional.ofNullable(brand).map(brandValue -> extractTranslation(translation, brandValue.getTranslations(), brandValue))
				.orElse(new TranslationUnit(null, null));
	}

	private List<TranslationUnit> extractOptionTranslations(final Translation translation,
															final List<Option> options) {
		return options.stream().map(option -> extractTranslation(translation, option.getTranslations(), option)).collect(toList());
	}

	private List<ItemTranslation> extractItemTranslations(final StoreProductSku productSku) {
		return translations.stream()
				.map(translation -> new ItemTranslation(translation.getLanguage(),
						new DetailsExtractor(productSku::getFullAttributeValues,
								translation.getLanguage(),
								productSku.getAttributeValueGroup(),
								attributes,
								store,
								catalog)
								.getTranslations(),
						extractItemOptionTranslations(translation, options, productSku.getOptionValueMap())))
				.collect(toList());
	}

	private TranslationUnit extractTranslation(final Translation translation,
											   final List<? extends Translation> translations,
											   final Projection projection) {
		final String displayName = Optional.of(translations).orElse(Collections.emptyList())
				.stream()
				.filter(optionTranslation -> optionTranslation.getLanguage().equals(translation.getLanguage()))
				.findAny()
				.map(Translation::getDisplayName)
				.orElse(null);

		return new TranslationUnit(displayName, projection.getIdentity().getCode());
	}

	private List<ItemOptionTranslation> extractItemOptionTranslations(final Translation translation,
																	  final List<Option> options,
																	  final Map<String, SkuOptionValue> optionValues) {
		return optionValues.keySet()
				.stream()
				.map(optionCode -> createItemOptionTranslation(findOptionTranslation(optionCode, options, translation),
						findOptionValueTranslation(optionCode, options, translation, optionValues),
						optionCode))
				.collect(toList());
	}

	private ItemOptionTranslation createItemOptionTranslation(final OptionTranslation optionTranslation,
															  final TranslatedName optionValueTranslation,
															  final String optionCode) {
		final Optional<TranslatedName> optionValue = Optional.ofNullable(optionValueTranslation);

		return new ItemOptionTranslation(Optional.ofNullable(optionTranslation).map(Translation::getDisplayName).orElse(null),
				optionCode,
				optionValue.map(TranslatedName::getDisplayValue).orElse(null),
				optionValue.map(TranslatedName::getValue).orElse(null));
	}

	private OptionTranslation findOptionTranslation(final String skuCode, final List<Option> options, final Translation translation) {
		return options.stream()
				.filter(option -> option.getIdentity().getCode().equals(skuCode))
				.findAny()
				.map(Option::getTranslations)
				.map(optionTranslations -> optionTranslations.stream()
						.filter(optionTranslation -> optionTranslation.getLanguage().equals(translation.getLanguage()))
						.findAny()
						.orElse(null))
				.orElse(null);
	}

	private TranslatedName findOptionValueTranslation(final String skuCode,
													  final List<Option> options,
													  final Translation translation,
													  final Map<String, SkuOptionValue> optionValues) {
		final OptionTranslation optionTranslation = findOptionTranslation(skuCode, options, translation);
		final SkuOptionValue skuOptionValue = optionValues.get(skuCode);
		return Optional.ofNullable(optionTranslation).map(OptionTranslation::getOptionValues)
				.map(values -> values.stream()
						.filter(value -> value.getValue().equals(skuOptionValue.getOptionValueKey()))
						.findAny()
						.orElse(null))
				.orElse(null);
	}
}
