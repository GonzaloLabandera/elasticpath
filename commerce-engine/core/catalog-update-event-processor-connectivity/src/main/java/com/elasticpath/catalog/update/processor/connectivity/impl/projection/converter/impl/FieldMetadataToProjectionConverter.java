/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import com.elasticpath.catalog.entity.Field;
import com.elasticpath.catalog.entity.FieldOption;
import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.entity.translation.FieldMetadataTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.Converter;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter.ModifierFieldLdfAdapter;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter.ModifierFieldOptionLdfAdapter;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter.ModifierGroupLdfAdapter;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierFieldLdf;
import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierGroupLdf;
import com.elasticpath.domain.modifier.ModifierType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.modifier.ModifierService;

/**
 * A projection converter which converts ModifierGroup to FieldMetadata.
 */
public class FieldMetadataToProjectionConverter implements Converter<ModifierGroup, FieldMetadata> {

	private final CatalogTranslationExtractor translationExtractor;
	private final TimeService timeService;
	private final ModifierService modifierService;


	/**
	 * Constructor for FieldMetadataToProjectionConverter.
	 * @param translationExtractor {@link CatalogTranslationExtractor}.
	 * @param timeService          the time service.
	 * @param modifierService the modifier service.
	 */
	public FieldMetadataToProjectionConverter(final CatalogTranslationExtractor translationExtractor,
											  final TimeService timeService,
											  final ModifierService modifierService) {
		this.translationExtractor = translationExtractor;
		this.timeService = timeService;
		this.modifierService = modifierService;
	}

	/**
	 * Convert ModifierGroup {@link ModifierGroup} to FieldMetadata {@link FieldMetadata}.
	 * for particularly store {@link Store}.
	 *
	 * @param group {@link ModifierGroup}.
	 * @param store {@link Store}.
	 * @return projection {@link FieldMetadataToProjectionConverter}.
	 */
	@Override
	public FieldMetadata convert(final ModifierGroup group, final Store store, final Catalog catalog) {
		final List<Translation> externalTranslation = extractGroupTranslations(group, store);

		final List<FieldMetadataTranslation> fieldMetadataTranslations = externalTranslation
				.stream()
				.map(translationGroup -> extractFieldMetadataTranslation(group, translationGroup, store))
				.collect(Collectors.toList());

		ZonedDateTime currentTime = ZonedDateTime.ofInstant(timeService.getCurrentTime().toInstant(), ZoneId.of("GMT"));

		return new FieldMetadata(group.getCode(), store.getCode(), fieldMetadataTranslations, currentTime, false);
	}

	private FieldMetadataTranslation extractFieldMetadataTranslation(final ModifierGroup group, final Translation translationGroup,
																	 final Store store) {
		Locale locale = getLocaleForGroup(group);
		final List<Field> fields = extractFieldsByLanguage(locale, group.getModifierFields(),
				translationGroup.getLanguage(), store);


		return new FieldMetadataTranslation(translationGroup.getLanguage(), translationGroup.getDisplayName(), fields);
	}

	private Locale getLocaleForGroup(final ModifierGroup group) {
		Catalog catalogForGroup = modifierService.findCatalogForModifierGroup(group);

		Locale locale;
		if (catalogForGroup == null) {
			Set<ModifierGroupLdf> modifierGroupLdf = group.getModifierGroupLdf();
			if (CollectionUtils.isEmpty(modifierGroupLdf)) {
				locale = Locale.ENGLISH;
			} else {
				locale = Locale.forLanguageTag(modifierGroupLdf.stream().map(ModifierGroupLdf::getLocale).findFirst().orElse("EN"));
			}
		} else {
			locale = catalogForGroup.getDefaultLocale();
		}
		return locale;
	}

	private List<Field> extractFieldsByLanguage(final Locale defaultCatalogLocale, final Set<ModifierField> cartItemModifierFields,
												final String translation,
												final Store store) {
		return Optional.ofNullable(cartItemModifierFields)
				.map(fields -> makeSortedFields(defaultCatalogLocale, fields, translation, store))
				.orElse(null);
	}

	/**
	 * Makes sorted fields.
	 *
	 * @param defaultCatalogLocale default locale for Catalog.
	 * @param cartItemModifierFields list of ModifierField.
	 * @param language required language.
	 * @param store store.
	 * @return list of fields.
	 */
	List<Field> makeSortedFields(final Locale defaultCatalogLocale,
								 final Set<ModifierField> cartItemModifierFields,
								 final String language,
								 final Store store) {
		return cartItemModifierFields.stream()
				.sorted(Comparator.comparingInt(ModifierField::getOrdering))
				.map(field -> new Field(field.getCode(),
						extractFieldDisplayName(defaultCatalogLocale, field.getModifierFieldsLdf(), language, store),
						extractFieldType(field),
						field.isRequired(),
						field.getMaxSize(),
						extractOptions(defaultCatalogLocale, field, language, store)))
				.collect(Collectors.toList());
	}

	private String extractFieldType(final ModifierField field) {
		return Optional.ofNullable(field.getFieldType())
				.map(ModifierType::getCamelName)
				.orElse(null);
	}


	private List<Translation> extractGroupTranslations(final ModifierGroup group, final Store store) {

		return translationExtractor.getProjectionTranslations(store.getDefaultLocale(),
				store.getSupportedLocales(),
				new ModifierGroupLdfAdapter(getLocaleForGroup(group), group.getModifierGroupLdf()));
	}

	private List<Translation> extractFieldTranslations(final Locale defaultCatalogLocale,
													   final Set<ModifierFieldLdf> cartItemModifierFieldsLdf,
													   final Store store) {
		return translationExtractor.getProjectionTranslations(store.getDefaultLocale(),
				store.getSupportedLocales(),
				new ModifierFieldLdfAdapter(defaultCatalogLocale, cartItemModifierFieldsLdf));
	}

	private List<Translation> extractOptionTranslations(final Locale defaultCatalogLocale,
														final ModifierFieldOption cartItemModifierFieldOption,
														final Store store) {
		return translationExtractor.getProjectionTranslations(store.getDefaultLocale(),
				store.getSupportedLocales(),
				new ModifierFieldOptionLdfAdapter(defaultCatalogLocale, cartItemModifierFieldOption.getModifierFieldOptionsLdf()));
	}

	/**
	 * Extracts field options.
	 *
	 * @param defaultCatalogLocale default locale for Catalog.
	 * @param field source fields.
	 * @param language required language.
	 * @param store store.
	 * @return list of field options.
	 */
	List<FieldOption> extractOptions(final Locale defaultCatalogLocale, final ModifierField field, final String language, final Store store) {
		final Set<ModifierFieldOption> fields = Optional.ofNullable(field.getModifierFieldOptions())
				.orElse(Collections.emptySet());

		return fields.stream()
				.sorted(Comparator.comparingInt(ModifierFieldOption::getOrdering))
				.map(option -> new FieldOption(option.getValue(),
						extractOptionDisplayValue(defaultCatalogLocale, option, language, store)))
				.collect(Collectors.toList());
	}

	private String extractOptionDisplayValue(final Locale defaultCatalogLocale, final ModifierFieldOption option, final String language,
											 final Store store) {
		return extractDisplayName(extractOptionTranslations(defaultCatalogLocale, option, store), language);
	}

	private String extractFieldDisplayName(final Locale defaultCatalogLocale, final Set<ModifierFieldLdf> cartItemModifierFieldsLdf,
										   final String language, final Store store) {
		return extractDisplayName(extractFieldTranslations(defaultCatalogLocale, cartItemModifierFieldsLdf, store), language);
	}

	private String extractDisplayName(final List<Translation> translations, final String language) {
		return translations
				.stream()
				.filter(translation -> translation.getLanguage().equals(language))
				.findFirst()
				.map(Translation::getDisplayName)
				.orElse("");
	}
}


