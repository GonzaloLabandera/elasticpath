/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.translation.DetailsTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.domain.attribute.AttributeMultiValueType;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.attribute.AttributeValueGroup;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.attribute.impl.ProductAttributeValueImpl;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.store.Store;

/**
 * Tests {@link OfferTranslationExtractor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OfferTranslationExtractorTest {

	private static final double ANY_DECIMAL_VALUE = 9.5;
	private static final String DATE = "Date";
	private static final String MULTI_SHORT_TEXT_VALUE = "date1,date2";
	private static final String ANY_CODE = "anyCode";

	@Test
	public void testThatExtractorReturnsRightDateDisplayValue() throws ParseException {
		final Date date = getDate();

		final OfferTranslationExtractor extractor = createExtractor(Collections.singletonList(getValue(AttributeType.DATE, date, DATE)));

		assertThat(extractor.getOfferTranslations().get(0).getDetails().get(0).getDisplayValues().get(0)).isEqualTo("1982-08-31");
	}

	@Test
	public void testThatExtractorReturnsRightDateTimeDisplayValue() throws ParseException {
		final Date date = getDate();

		final OfferTranslationExtractor extractor = createExtractor(Collections.singletonList(getValue(AttributeType.DATETIME, date, DATE)));

		Date actualDate = Date.from(Instant.from(
				DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.systemDefault()).parse(
						extractor.getOfferTranslations().get(0).getDetails().get(0).getDisplayValues().get(0))));

		assertThat(date).isEqualTo(actualDate);
	}

	@Test
	public void testThatExtractorReturnsRightBooleanDisplayValue() {
		final OfferTranslationExtractor extractor = createExtractor(Collections.singletonList(getValue(AttributeType.BOOLEAN, true, "true")));

		assertThat(extractor.getOfferTranslations().get(0).getDetails().get(0).getDisplayValues().get(0)).isEqualTo("True");
	}

	@Test
	public void testThatExtractorReturnsRightDecimalDisplayValue() {
		final OfferTranslationExtractor extractor = createExtractor(Collections.singletonList(getValue(AttributeType.DECIMAL, ANY_DECIMAL_VALUE,
				"9.50")));

		assertThat(extractor.getOfferTranslations().get(0).getDetails().get(0).getDisplayValues().get(0)).isEqualTo("9.50");
	}

	@Test
	public void testThatExtractorReturnsRightDateValue() throws ParseException {
		final Date date = getDate();

		final OfferTranslationExtractor extractor = createExtractor(Collections.singletonList(getValue(AttributeType.DATE, date, DATE)));

		assertThat(extractor.getOfferTranslations().get(0).getDetails().get(0).getValues().get(0)).isEqualTo(date.getTime());
	}

	@Test
	public void testThatExtractorReturnsRightDateTimeValue() throws ParseException {
		final Date date = getDate();

		final OfferTranslationExtractor extractor = createExtractor(Collections.singletonList(getValue(AttributeType.DATETIME, date, DATE)));

		assertThat(extractor.getOfferTranslations().get(0).getDetails().get(0).getValues().get(0)).isEqualTo(date.getTime());
	}

	@Test
	public void testThatExtractorReturnsRightBooleanValue() {
		final OfferTranslationExtractor extractor = createExtractor(Collections.singletonList(getValue(AttributeType.BOOLEAN, true, "true")));

		Assert.assertTrue((boolean) extractor.getOfferTranslations().get(0).getDetails().get(0).getValues().get(0));
	}

	@Test
	public void testThatExtractorReturnsRightDecimalValue() {
		final OfferTranslationExtractor extractor = createExtractor(Collections.singletonList(getValue(AttributeType.DECIMAL, ANY_DECIMAL_VALUE,
				"9.50")));

		assertThat(extractor.getOfferTranslations().get(0).getDetails().get(0).getValues().get(0)).isEqualTo(ANY_DECIMAL_VALUE);
	}

	@Test
	public void testThatExtractorReturnsRightMultiValueShortText() {
		final OfferTranslationExtractor extractor = createExtractor(Collections.singletonList(getValue(AttributeType.SHORT_TEXT, DATE,
				MULTI_SHORT_TEXT_VALUE)));

		assertThat(extractor.getOfferTranslations().get(0).getDetails().get(0))
				.extracting(DetailsTranslation::getValues)
				.isEqualTo(Arrays.asList("date1", "date2"));
	}

	@Test
	public void testThatEmptyShortTextMultiAttributeValueShouldBeReplaced() {
		final OfferTranslationExtractor extractor = createExtractor(Arrays.asList(getValue(AttributeType.SHORT_TEXT, DATE,
				MULTI_SHORT_TEXT_VALUE), getEmptyValue(AttributeType.SHORT_TEXT)));

		assertFalse(extractor.getOfferTranslations().get(0).getDetails().get(0).getDisplayValues().isEmpty());
		assertFalse(extractor.getOfferTranslations().get(0).getDetails().get(1).getDisplayValues().isEmpty());
	}

	@Test
	public void testThatEmptyIntegerValueShouldBeReplaced() {
		final OfferTranslationExtractor extractor = createExtractor(Arrays.asList(getValue(AttributeType.SHORT_TEXT, DATE,
				MULTI_SHORT_TEXT_VALUE), getEmptyValue(AttributeType.INTEGER)));

		assertFalse(extractor.getOfferTranslations().get(0).getDetails().get(0).getDisplayValues().isEmpty());
		assertFalse(extractor.getOfferTranslations().get(0).getDetails().get(1).getDisplayValues().isEmpty());
	}

	@Test
	public void testThatEmptyDecimalValueShouldBeReplaced() {
		final OfferTranslationExtractor extractor = createExtractor(Arrays.asList(getValue(AttributeType.SHORT_TEXT, DATE,
				MULTI_SHORT_TEXT_VALUE), getEmptyValue(AttributeType.DECIMAL)));

		assertFalse(extractor.getOfferTranslations().get(0).getDetails().get(0).getDisplayValues().isEmpty());
		assertFalse(extractor.getOfferTranslations().get(0).getDetails().get(1).getDisplayValues().isEmpty());
	}

	private OfferTranslationExtractor createExtractor(final List<AttributeValue> values) {
		final Brand brand = mock(Brand.class);
		final Attribute attribute = mock(Attribute.class);
		final TranslationExtractorData extractorData = new TranslationExtractorData(brand,
				Collections.emptyList(),
				Collections.singletonList(attribute));
		final Store store = mock(Store.class);
		final StoreProduct storeProduct = mock(StoreProduct.class);
		final Catalog catalog = mock(Catalog.class);
		final NameIdentity identity = new NameIdentity("anyType", ANY_CODE, "anyStore");
		final AttributeValueGroup attributeValueGroup = mock(AttributeValueGroup.class);

		when(brand.getIdentity()).thenReturn(identity);
		when(attribute.getIdentity()).thenReturn(identity);
		when(store.getSupportedLocales()).thenReturn(Collections.singletonList(Locale.ENGLISH));
		when(store.getDefaultLocale()).thenReturn(Locale.ENGLISH);
		when(storeProduct.getFullAttributeValues(Locale.ENGLISH)).thenReturn(values);
		when(storeProduct.getAttributeValueGroup()).thenReturn(attributeValueGroup);
		when(attributeValueGroup.getAttributeValue(ANY_CODE, Locale.ENGLISH)).thenReturn(values.get(0));

		return new OfferTranslationExtractor(Collections.singletonList(new Translation("en", "displayName")),
				extractorData, Collections.emptyList(), store, storeProduct, catalog);
	}

	private AttributeValue getValue(final AttributeType date, final Object value, final String stringValue) {
		final AttributeValue attributeValue = mock(AttributeValue.class);
		final com.elasticpath.domain.attribute.Attribute attributeDomain = new AttributeImpl();

		attributeDomain.setKey(ANY_CODE);
		attributeDomain.setMultiValueType(AttributeMultiValueType.RFC_4180);

		when(attributeValue.getAttributeType()).thenReturn(date);
		when(attributeValue.getAttribute()).thenReturn(attributeDomain);
		when(attributeValue.getStringValue()).thenReturn(stringValue);
		when(attributeValue.getValue()).thenReturn(value);
		return attributeValue;
	}

	private AttributeValue getEmptyValue(final AttributeType type) {
		final AttributeValue attributeValue = new ProductAttributeValueImpl();
		final com.elasticpath.domain.attribute.Attribute attributeDomain = new AttributeImpl();

		attributeDomain.setKey(ANY_CODE);
		attributeDomain.setMultiValueType(AttributeMultiValueType.RFC_4180);

		attributeValue.setAttributeType(type);
		attributeValue.setAttribute(attributeDomain);

		return attributeValue;
	}

	private Date getDate() throws ParseException {
		final SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss", Locale.ENGLISH);
		final String dateInString = "31-08-1982 10:20:56";
		return format.parse(dateInString);
	}
}
