/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.offer;

import static com.elasticpath.catalog.entity.constants.ProjectionSchemaPath.OFFER_SCHEMA_JSON;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import joptsimple.internal.Strings;
import org.junit.Test;

import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.BaseSetUp;
import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.translation.ItemOptionTranslation;
import com.elasticpath.catalog.entity.translation.ItemTranslation;
import com.elasticpath.catalog.entity.translation.DetailsTranslation;
import com.elasticpath.catalog.entity.translation.OfferTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.entity.translation.TranslationUnit;
import com.elasticpath.catalog.validator.impl.ProjectionValidator;

/**
 * Tests {@link Offer}.
 */
public class OfferTest extends BaseSetUp {

	private static final String ALWAYS = "ALWAYS";
	private static final String NAME = "name";
	private static final String VALUE = "value";
	private static final String DISPLAY_NAME = "displayName";
	private static final String EXTENSION_KEY = "extensionKey";
	private static final String EXTENSION_VALUE = "extensionValue";
	private static final String CODE = "code";
	private static final String STORE = "store";
	private static final String ITEM_CODE = "itemCode";
	private static final String NAME_ONE = "nameOne";
	private static final String VALUE_ONE = "valueOne";

	@Test
	public void testThatValidateOfferJsonNotThrowAnyException() throws JsonProcessingException {
		final List<Property> properties = Arrays.asList(new Property(NAME, VALUE), new Property(NAME_ONE, VALUE_ONE));
		final BigDecimal zero = BigDecimal.ZERO;
		final ShippingProperties shippingProperties = new ShippingProperties(zero, zero, zero,
				zero, Strings.EMPTY, Strings.EMPTY);
		final AvailabilityRules availabilityRules = new AvailabilityRules(ZonedDateTime.now(), ZonedDateTime.now());
		final DetailsTranslation detailTranslation = new DetailsTranslation(new TranslationUnit("diplayOfferName",
				"displayOfferValue"),
				Collections.singletonList("displayValues"), Arrays.asList("value1", "value2", "value3"));
		final ItemOptionTranslation itemOptionTranslation = new ItemOptionTranslation(DISPLAY_NAME, NAME,
				"displayValue", VALUE);
		final ItemTranslation itemTranslation = new ItemTranslation("en", Collections.singletonList(detailTranslation),
				Collections.singletonList(itemOptionTranslation));
		final Item item = new Item(ITEM_CODE, new Object(), properties, availabilityRules, shippingProperties,
				Collections.singletonList(itemTranslation));
		final OfferAvailabilityRules offerAvailabilityRules = new OfferAvailabilityRules(ZonedDateTime.now(), ZonedDateTime.now(),
				ZonedDateTime.now(), Collections.singleton(ALWAYS), Collections.singleton(ALWAYS), Collections.singleton(ALWAYS));
		final ProjectionProperties projectionProperties = new ProjectionProperties(CODE, STORE, ZonedDateTime.now(), false);

		final Offer offer = new Offer(new OfferProperties(projectionProperties, properties), Collections.singletonList(item), new Object(),
				Collections.emptyList(), new Components(Collections.emptyList()), new OfferRules(offerAvailabilityRules,
				new SelectionRules(SelectionType.NONE, 0)), Arrays.asList("formField1", "formField2"), Collections.emptyList(),
				Collections.emptySet());

		final String offerJson = getObjectMapper().writeValueAsString(offer);

		assertThatCode(() -> new ProjectionValidator(OFFER_SCHEMA_JSON)
				.validate(offerJson)).doesNotThrowAnyException();
	}

	@Test
	public void ensureThatValidateOfferJsonWithExtensionsNotThrowAnyException() throws JsonProcessingException {
		final List<Property> properties = Arrays.asList(new Property(NAME, VALUE), new Property(NAME_ONE, VALUE_ONE));
		final AvailabilityRules availabilityRules = new AvailabilityRules(ZonedDateTime.now(), ZonedDateTime.now());
		final BigDecimal zero = BigDecimal.ZERO;
		final ShippingProperties shippingProperties = new ShippingProperties(zero, zero, zero, zero, Strings.EMPTY, Strings.EMPTY);
		final Item item = new Item("itemCode", Collections.singletonMap(EXTENSION_KEY, EXTENSION_VALUE), properties, availabilityRules,
				shippingProperties, Collections.emptyList());

		final OfferAvailabilityRules offerAvailabilityRules = new OfferAvailabilityRules(ZonedDateTime.now(), ZonedDateTime.now(),
				ZonedDateTime.now(), Collections.singleton(ALWAYS), Collections.singleton(ALWAYS), Collections.singleton(ALWAYS));
		final ProjectionProperties projectionProperties = new ProjectionProperties(CODE, STORE, ZonedDateTime.now(), false);

		final Offer offer = new Offer(new OfferProperties(projectionProperties, properties), Collections.singletonList(item),
				Collections.singletonMap(EXTENSION_KEY, EXTENSION_VALUE), Collections.emptyList(), new Components(Collections.emptyList()),
				new OfferRules(offerAvailabilityRules, new SelectionRules(SelectionType.NONE, 0)), Collections.emptyList(),
				Collections.emptyList(), Collections.emptySet());

		final String offerJson = getObjectMapper().writeValueAsString(offer);

		assertThatCode(() -> new ProjectionValidator(OFFER_SCHEMA_JSON)
				.validate(offerJson)).doesNotThrowAnyException();
	}

	@Test
	public void ensureThatValidateOfferJsonWithCategoriesNotThrowAnyException() throws JsonProcessingException {
		final List<Property> properties = Arrays.asList(new Property(NAME, VALUE), new Property(NAME_ONE, VALUE_ONE));
		final AvailabilityRules availabilityRules = new AvailabilityRules(ZonedDateTime.now(), ZonedDateTime.now());
		final BigDecimal zero = BigDecimal.ZERO;
		final Set<OfferCategories> categories = Collections.singleton(new OfferCategories(CODE, Collections.emptyList(), ZonedDateTime.now(),
				ZonedDateTime.now(), false, 3));
		final ShippingProperties shippingProperties = new ShippingProperties(zero, zero, zero, zero, Strings.EMPTY, Strings.EMPTY);
		final Item item = new Item(ITEM_CODE, new Object(), properties, availabilityRules,
				shippingProperties, Collections.emptyList());

		final OfferAvailabilityRules offerAvailabilityRules = new OfferAvailabilityRules(ZonedDateTime.now(), ZonedDateTime.now(),
				ZonedDateTime.now(), Collections.singleton(ALWAYS), Collections.singleton(ALWAYS), Collections.singleton(ALWAYS));
		final ProjectionProperties projectionProperties = new ProjectionProperties(CODE, STORE, ZonedDateTime.now(), false);

		final Offer offer = new Offer(new OfferProperties(projectionProperties, properties), Collections.singletonList(item),
				new Object(), Collections.emptyList(), new Components(Collections.emptyList()),
				new OfferRules(offerAvailabilityRules, new SelectionRules(SelectionType.NONE, 0)), Collections.emptyList(),
				Collections.emptyList(), categories);

		final String offerJson = getObjectMapper().writeValueAsString(offer);

		assertThatCode(() -> new ProjectionValidator(OFFER_SCHEMA_JSON)
				.validate(offerJson)).doesNotThrowAnyException();
	}

	@Test
	public void ensureThatValidateOfferJsonWithEmptyCategoriesListNotThrowAnyException() throws JsonProcessingException {
		final List<Property> properties = Arrays.asList(new Property(NAME, VALUE), new Property(NAME_ONE, VALUE_ONE));
		final AvailabilityRules availabilityRules = new AvailabilityRules(ZonedDateTime.now(), ZonedDateTime.now());
		final BigDecimal zero = BigDecimal.ZERO;
		final ShippingProperties shippingProperties = new ShippingProperties(zero, zero, zero, zero, Strings.EMPTY, Strings.EMPTY);
		final Item item = new Item(ITEM_CODE, new Object(), properties, availabilityRules,
				shippingProperties, Collections.emptyList());
		final OfferAvailabilityRules offerAvailabilityRules = new OfferAvailabilityRules(ZonedDateTime.now(), ZonedDateTime.now(),
				ZonedDateTime.now(), Collections.singleton(ALWAYS), Collections.singleton(ALWAYS), Collections.singleton(ALWAYS));
		final ProjectionProperties projectionProperties = new ProjectionProperties(CODE, STORE, ZonedDateTime.now(), false);
		final Offer offer = new Offer(new OfferProperties(projectionProperties, properties), Collections.singletonList(item),
				new Object(), Collections.emptyList(), new Components(Collections.emptyList()),
				new OfferRules(offerAvailabilityRules, new SelectionRules(SelectionType.NONE, 0)), Collections.emptyList(),
				Collections.emptyList(), Collections.emptySet());

		final String offerJson = getObjectMapper().writeValueAsString(offer);

		assertThatCode(() -> new ProjectionValidator(OFFER_SCHEMA_JSON)
				.validate(offerJson)).doesNotThrowAnyException();
	}

	@Test
	public void ensureThatValidateOfferJsonWithTranslationsNotThrowAnyException() throws JsonProcessingException {
		final List<Property> properties = Arrays.asList(new Property(NAME, VALUE), new Property(NAME_ONE, VALUE_ONE));
		final AvailabilityRules availabilityRules = new AvailabilityRules(ZonedDateTime.now(), ZonedDateTime.now());
		final BigDecimal zero = BigDecimal.ZERO;
		final ShippingProperties shippingProperties = new ShippingProperties(zero, zero, zero, zero, Strings.EMPTY, Strings.EMPTY);
		final Item item = new Item("itemCode", Collections.singletonMap(EXTENSION_KEY, EXTENSION_VALUE), properties, availabilityRules,
				shippingProperties, Collections.emptyList());
		final OfferAvailabilityRules offerAvailabilityRules = new OfferAvailabilityRules(ZonedDateTime.now(), ZonedDateTime.now(),
				ZonedDateTime.now(), Collections.singleton(ALWAYS), Collections.singleton(ALWAYS), Collections.singleton(ALWAYS));
		final ProjectionProperties projectionProperties = new ProjectionProperties("code", "store", ZonedDateTime.now(), false);
		final List<OfferTranslation> translations = Collections.singletonList(
				new OfferTranslation(new Translation("language", DISPLAY_NAME),
						new TranslationUnit(DISPLAY_NAME, NAME),
						Collections.singletonList(new TranslationUnit(DISPLAY_NAME, NAME)),
						Collections.singletonList(new DetailsTranslation(DISPLAY_NAME, NAME,
								Collections.emptyList(), Collections.emptyList()))));
		final Offer offer = new Offer(new OfferProperties(projectionProperties, properties), Collections.singletonList(item),
				Collections.singletonMap(EXTENSION_KEY, EXTENSION_VALUE), Collections.emptyList(), new Components(Collections.emptyList()),
				new OfferRules(offerAvailabilityRules, new SelectionRules(SelectionType.NONE, 0)), Collections.emptyList(),
				translations, Collections.emptySet());

		final String offerJson = getObjectMapper().writeValueAsString(offer);

		assertThatCode(() -> new ProjectionValidator(OFFER_SCHEMA_JSON)
				.validate(offerJson)).doesNotThrowAnyException();
	}
}
