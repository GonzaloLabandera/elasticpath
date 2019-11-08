/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static java.util.stream.Collectors.toList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.translation.AttributeTranslation;
import com.elasticpath.catalog.entity.translation.DetailsTranslation;
import com.elasticpath.commons.constants.GlobalConstants;
import com.elasticpath.commons.constants.ImportConstants;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.attribute.AttributeValueGroup;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.store.Store;

/**
 * Represents a class which implements extracting translation logic for Category and Offer.
 */
@SuppressWarnings({"PMD.SimpleDateFormatNeedsLocale"})
public class DetailsExtractor {
	private static final String DASH = "-";
	private static final String UNDERSCORES = "_";
	private final Locale locale;
	private final AttributeValueGroup attributeValueGroup;
	private final List<Attribute> attributes;
	private final Store store;
	private final Catalog catalog;
	private final List<DetailsTranslation> translations;

	/**
	 * Constructor.
	 *
	 * @param supplier            is function which  gets list of {@link AttributeValue} by {@link Locale}.
	 * @param language            represent current locale.
	 * @param attributeValueGroup {@link AttributeValueGroup}.
	 * @param attributes          list of {@link Attribute}.
	 * @param store               {@link Store}.
	 * @param catalog             {@link Catalog}.
	 */
	public DetailsExtractor(final Function<Locale, List<AttributeValue>> supplier, final String language,
							final AttributeValueGroup attributeValueGroup,
							final List<Attribute> attributes, final Store store, final Catalog catalog) {
		this.attributeValueGroup = attributeValueGroup;
		this.attributes = attributes;
		this.store = store;
		this.catalog = catalog;
		this.locale = extractLocale(language);
		this.translations = extractDetailsTranslations(supplier);
	}

	/**
	 * Returns the details translations.
	 *
	 * @return list of {@link DetailsTranslation}.
	 */
	public List<DetailsTranslation> getTranslations() {
		return translations;
	}

	private List<DetailsTranslation> extractDetailsTranslations(final Function<Locale, List<AttributeValue>> supplier) {
		return supplier.apply(locale)
				.stream()
				.map(value -> isStubValue(value) ? extractDefault(attributeValueGroup, value) : value)
				.filter(Objects::nonNull)
				.filter(value -> Objects.nonNull(value.getValue()))
				.map(value -> new DetailsTranslation(getDisplayValue(value, locale),
						value.getAttribute().getKey(),
						createDisplayValues(value),
						createValues(value)))
				.collect(toList());
	}

	private Locale extractLocale(final String language) {
		return store.getSupportedLocales()
				.stream()
				.filter(currentLocale -> currentLocale.toString().equals(language.replace(DASH, UNDERSCORES)))
				.findAny()
				.orElse(null);
	}

	private boolean isStubValue(final AttributeValue value) {
		return Objects.isNull(value.getValue())
				|| StringUtils.isEmpty(value.getStringValue())
				|| GlobalConstants.NULL_VALUE.equalsIgnoreCase(value.getStringValue());
	}

	private String getDisplayValue(final AttributeValue value, final Locale currentLocale) {
		final List<AttributeTranslation> attributeTranslations = attributes.stream()
				.filter(attr -> attr.getIdentity().getCode().equals(value.getAttribute().getKey()))
				.findFirst()
				.map(Attribute::getTranslations)
				.orElse(Collections.emptyList());

		return Stream.concat(Stream.of(currentLocale), getLocaleByPriority(value).stream())
				.map(prioritizedLocale -> attributeTranslations.stream()
						.filter(translation -> translation.getLanguage().replace(DASH, UNDERSCORES).equals(prioritizedLocale.toString()))
						.findFirst()
						.orElse(null))
				.filter(Objects::nonNull)
				.findFirst().map(AttributeTranslation::getDisplayName)
				.orElse(null);
	}

	private List<String> createDisplayValues(final AttributeValue data) {
		final List<String> values;
		final AttributeType type = data.getAttributeType();

		if (AttributeType.SHORT_TEXT.equals(type)) {
			values = createShortTextValue(data);
		} else if (AttributeType.DATE.equals(type)) {
			values = createDateDisplayValue(data);
		} else if (AttributeType.DATETIME.equals(type)) {
			values = createDateTimeDisplayValue(data);
		} else if (AttributeType.BOOLEAN.equals(type)) {
			values = createBooleanDisplayValue(data);
		} else if (AttributeType.DECIMAL.equals(type)) {
			values = createDecimalDisplayValue(data);
		} else {
			values = Collections.singletonList(data.getStringValue());
		}

		return values;
	}

	private List<Object> createValues(final AttributeValue data) {
		final List<Object> values;
		final AttributeType type = data.getAttributeType();

		if (AttributeType.SHORT_TEXT.equals(type)) {
			values = new ArrayList<>(createShortTextValue(data));
		} else if (AttributeType.DATE.equals(type)) {
			values = createDateValue(data);
		} else if (AttributeType.DATETIME.equals(type)) {
			values = createDateTimeValue(data);
		} else if (AttributeType.BOOLEAN.equals(type)) {
			values = createBooleanValue(data);
		} else if (AttributeType.DECIMAL.equals(type)) {
			values = createDecimalValue(data);
		} else {
			values = Collections.singletonList(data.getStringValue());
		}

		return values;
	}

	private AttributeValue extractDefault(final AttributeValueGroup attributeValueGroup, final AttributeValue attributeValue) {
		return getLocaleByPriority(attributeValue).stream()
				.map(prioritizedLocale -> attributeValueGroup.getAttributeValue(attributeValue.getAttribute().getKey(), prioritizedLocale))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

	private List<Locale> getLocaleByPriority(final AttributeValue attributeValue) {
		final List<Locale> additionalLocales = attributes.stream()
				.filter(attr -> attr.getIdentity().getCode().equals(attributeValue.getAttribute().getKey()))
				.findFirst()
				.map(Attribute::getTranslations)
				.map(attributeTranslations -> attributeTranslations.stream().map(translation -> LocaleUtils.toLocale(translation.getLanguage()
						.replace(DASH, UNDERSCORES)))
						.collect(toList()))
				.orElse(Collections.emptyList());

		return Stream.concat(Stream.of(store.getDefaultLocale(), catalog.getDefaultLocale()), additionalLocales.stream())
				.filter(Objects::nonNull)
				.collect(toList());
	}

	private List<String> createDecimalDisplayValue(final AttributeValue data) {
		return Optional.ofNullable(data.getStringValue())
				.map(Collections::singletonList)
				.orElse(Collections.emptyList());
	}

	private List<String> createBooleanDisplayValue(final AttributeValue data) {
		return Optional.ofNullable(data.getStringValue())
				.map(String::toUpperCase)
				.map(BooleanEnum::valueOf)
				.map(Objects::toString)
				.map(Collections::singletonList)
				.orElse(Collections.emptyList());
	}

	private List<String> createDateTimeDisplayValue(final AttributeValue data) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		return Optional.ofNullable(data.getValue())
				.map(dateFormat::format)
				.map(Collections::singletonList)
				.orElse(Collections.emptyList());
	}

	private List<String> createDateDisplayValue(final AttributeValue data) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		return Optional.ofNullable(data.getValue())
				.map(dateFormat::format)
				.map(Collections::singletonList)
				.orElse(Collections.emptyList());
	}

	private List<String> createShortTextValue(final AttributeValue data) {
		final List<String> values;
		if (data.getAttribute().isMultiValueEnabled()) {
			values = data.getAttribute()
					.getMultiValueType()
					.getEncoder()
					.decodeStringToList(data.getStringValue(), ImportConstants.SHORT_TEXT_MULTI_VALUE_SEPARATOR_CHAR);
		} else {
			values = Collections.singletonList(data.getStringValue());
		}

		return values;
	}

	private List<Object> createDateValue(final AttributeValue data) {
		return Optional.ofNullable(data.getValue())
				.map(date -> (Date) date)
				.map(Date::getTime)
				.map(date -> (Object) date)
				.map(Collections::singletonList)
				.orElse(Collections.emptyList());
	}

	private List<Object> createDecimalValue(final AttributeValue data) {
		return Optional.ofNullable(data.getValue())
				.map(Collections::singletonList)
				.orElse(Collections.emptyList());
	}

	private List<Object> createBooleanValue(final AttributeValue data) {
		return Collections.singletonList(data.getValue());
	}

	private List<Object> createDateTimeValue(final AttributeValue data) {
		return createDateValue(data);
	}
}
