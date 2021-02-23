/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.catalog.bulk;

import static com.elasticpath.catalog.bulk.DomainBulkEventType.ATTRIBUTE_BULK_UPDATE;
import static com.elasticpath.catalog.bulk.DomainBulkEventType.ATTRIBUTE_CATEGORY_BULK_UPDATE;
import static com.elasticpath.catalog.bulk.DomainBulkEventType.ATTRIBUTE_SKU_BULK_UPDATE;
import static com.elasticpath.catalog.bulk.DomainBulkEventType.BRAND_BULK_UPDATE;
import static com.elasticpath.catalog.bulk.DomainBulkEventType.OPTION_BULK_UPDATE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;
import static com.elasticpath.catalog.update.processor.connectivity.impl.SkuOptionUpdateProcessorImpl.PRODUCTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TEN_SECONDS;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.persistence.EntityNotFoundException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;

import com.elasticpath.catalog.bulk.BulkEventProcessor;
import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.TranslatedName;
import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryProperties;
import com.elasticpath.catalog.entity.offer.Association;
import com.elasticpath.catalog.entity.offer.Components;
import com.elasticpath.catalog.entity.offer.Item;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.offer.OfferAvailabilityRules;
import com.elasticpath.catalog.entity.offer.OfferCategories;
import com.elasticpath.catalog.entity.offer.SelectionRules;
import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.entity.translation.AttributeTranslation;
import com.elasticpath.catalog.entity.translation.CategoryTranslation;
import com.elasticpath.catalog.entity.translation.DetailsTranslation;
import com.elasticpath.catalog.entity.translation.ItemOptionTranslation;
import com.elasticpath.catalog.entity.translation.ItemTranslation;
import com.elasticpath.catalog.entity.translation.OfferTranslation;
import com.elasticpath.catalog.entity.translation.OptionTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.entity.translation.TranslationUnit;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.impl.EventMessageImpl;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Test of {@link BulkEventProcessor}.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DirtiesDatabase
public class BulkEventProcessorTest extends DbTestCase {

	private static final Logger LOGGER = Logger.getLogger(BulkEventProcessorTest.class);

	private static final String BRAND_CODE = "brand";
	private static final String BRAND_NAME = "brandName";
	private static final String UPDATED_OFFER = "offerCode";
	private static final String NOT_UPDATED_OFFER = "notUpdatedOfferCode";
	private static final String UPDATED_CATEGORY = "categoryCode";
	private static final String NOT_UPDATED_CATEGORY = "notUpdatedCATEGORYCode";
	private static final String STORE = "store";
	private static final String OFFER_TRANSLATION_DISPLAY_NAME = "displayName";
	private static final String BRAND_DISPLAY_NAME_EN = "displayNameEn";
	private static final String BRAND_DISPLAY_NAME_FR = "displayNameFr";
	private static final String UPDATED_BRAND_DISPLAY_NAME_EN = "updatedDisplayNameEn";
	private static final String UPDATED_BRAND_DISPLAY_NAME_FR = "updatedDisplayNameFr";

	private static final String OPTION = "option";
	private static final String VALUE = "value";
	private static final TranslatedName RED_TRANSLATION_EN = new TranslatedName(VALUE, "Red");
	private static final TranslatedName RED_TRANSLATION_FR = new TranslatedName(VALUE, "Rouge");
	private static final String LANGUAGE_EN = "en";
	private static final String LANGUAGE_FR = "fr";
	private static final String COLOUR_EN = "Colour";
	private static final String COLOUR_FR = "Couleur";
	private static final String OPTION_DISPLAY_NAME_EN = "displayNameEn";
	private static final String OPTION_DISPLAY_NAME_FR = "displayNameFr";
	private static final String OPTION_DISPLAY_VALUE_EN = "displayNameEn";
	private static final String OPTION_DISPLAY_VALUE_FR = "displayNameFr";

	private static final String CATALOG_MESSAGING_CAMEL_CONTEXT = "ep-catalog-messaging";
	private static final String JMS_CATALOG_ENDPOINT = "jms:topic:VirtualTopic.ep.catalog";
	private static final String ATTRIBUTE_CODE = "attribute";
	private static final String ATTRIBUTE_NAME = "attributeName";
	private static final String ATTRIBUTE_DISPLAY_NAME_EN = "displayNameEn";
	private static final String ATTRIBUTE_DISPLAY_NAME_FR = "displayNameFr";
	private static final String UPDATED_ATTRIBUTE_DISPLAY_NAME_EN = "updatedDisplayNameEn";
	private static final String UPDATED_ATTRIBUTE_DISPLAY_NAME_FR = "updatedDisplayNameFr";


	@Autowired
	@Qualifier("ep-domain-bulk-messaging")
	private CamelContext camelContext;

	@Autowired
	private BulkEventProcessor bulkEventProcessor;

	@Autowired
	private CatalogService catalogService;

	@Autowired
	@Qualifier(CATALOG_MESSAGING_CAMEL_CONTEXT)
	private CamelContext catalogCamelContext;

	@Before
	public void setUp() throws Exception {
		catalogCamelContext.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(JMS_CATALOG_ENDPOINT)
						.process(exchange -> LOGGER.info("Catalog endpoint exchange: " + exchange.getIn().getBody()));
			}
		});
	}

	@Test
	public void offerTranslationsShouldNotContainUpdatedBrandDisplayNameWhenOfferCodeIsNotInBrandBulkUpdateEvent() {
		final int expectedNumberOfCatalogEvents = 2;

		final NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		final Translation brandTranslationEn = new Translation(Locale.ENGLISH.getLanguage(), UPDATED_BRAND_DISPLAY_NAME_EN);
		final Translation brandTranslationFr = new Translation(Locale.FRANCE.getLanguage(), UPDATED_BRAND_DISPLAY_NAME_FR);

		final Brand brand = createBrandWithTranslations(BRAND_CODE, STORE, Arrays.asList(brandTranslationEn, brandTranslationFr));
		catalogService.saveOrUpdate(brand);

		final OfferTranslation offerTranslationsEn = createOfferTranslationsWithBrand(Locale.ENGLISH.getLanguage(), BRAND_NAME,
				BRAND_DISPLAY_NAME_EN);
		final OfferTranslation offerTranslationsFr = createOfferTranslationsWithBrand(Locale.FRANCE.getLanguage(), BRAND_NAME,
				BRAND_DISPLAY_NAME_FR);

		final Offer offer = createOfferWithTranslations(NOT_UPDATED_OFFER, STORE, Arrays.asList(offerTranslationsEn, offerTranslationsFr));
		catalogService.saveOrUpdate(offer);

		final Exchange exchange = createExchange(BRAND_CODE, Collections.singletonList(UPDATED_OFFER));
		bulkEventProcessor.process(exchange);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);

		final Offer projection = catalogService.<Offer>read(OFFER_IDENTITY_TYPE, NOT_UPDATED_OFFER, STORE).orElseThrow(EntityNotFoundException::new);

		assertThat(projection.getTranslations())
				.extracting(OfferTranslation::getBrand)
				.extracting(TranslationUnit::getDisplayName)
				.doesNotContain(UPDATED_BRAND_DISPLAY_NAME_EN, UPDATED_BRAND_DISPLAY_NAME_FR)
				.containsOnly(BRAND_DISPLAY_NAME_EN, BRAND_DISPLAY_NAME_FR);
	}

	@Test
	public void offerTranslationsShouldContainOnlyUpdatedBrandDisplayNameWhenOfferCodeIsInBrandBulkUpdateEvent() {
		final int expectedNumberOfCatalogEvents = 3;

		final NotifyBuilder catalogNotifyBuilder = new NotifyBuilder(catalogCamelContext)
				.from(JMS_CATALOG_ENDPOINT).whenExactlyCompleted(expectedNumberOfCatalogEvents).create();

		final Translation brandTranslationEn = new Translation(Locale.ENGLISH.getLanguage(), UPDATED_BRAND_DISPLAY_NAME_EN);
		final Translation brandTranslationFr = new Translation(Locale.FRANCE.getLanguage(), UPDATED_BRAND_DISPLAY_NAME_FR);

		final Brand brand = createBrandWithTranslations(BRAND_CODE, STORE, Arrays.asList(brandTranslationEn, brandTranslationFr));
		catalogService.saveOrUpdate(brand);

		final OfferTranslation offerTranslationsEn = createOfferTranslationsWithBrand(Locale.ENGLISH.getLanguage(), BRAND_NAME,
				BRAND_DISPLAY_NAME_EN);
		final OfferTranslation offerTranslationsFr = createOfferTranslationsWithBrand(Locale.FRANCE.getLanguage(), BRAND_NAME,
				BRAND_DISPLAY_NAME_FR);

		final Offer offer = createOfferWithTranslations(UPDATED_OFFER, STORE, Arrays.asList(offerTranslationsEn, offerTranslationsFr));
		catalogService.saveOrUpdate(offer);

		final Exchange exchange = createExchange(BRAND_CODE, Collections.singletonList(UPDATED_OFFER));
		bulkEventProcessor.process(exchange);

		await().atMost(TEN_SECONDS).until(catalogNotifyBuilder::matches);

		Offer projection = (Offer) catalogService.read(OFFER_IDENTITY_TYPE, UPDATED_OFFER, STORE).orElseThrow(IllegalArgumentException::new);

		assertThat(projection.getTranslations())
				.extracting(OfferTranslation::getBrand)
				.extracting(TranslationUnit::getDisplayName)
				.containsOnly(UPDATED_BRAND_DISPLAY_NAME_EN, UPDATED_BRAND_DISPLAY_NAME_FR)
				.doesNotContain(BRAND_DISPLAY_NAME_EN, BRAND_DISPLAY_NAME_FR);
	}

	@Test
	public void offerTranslationsShouldNotContainUpdatedSkuOptionDisplayNameWhenOfferCodeIsNotInSkuOptionBulkUpdateEvent() {
		final Option option = createOptionWithTranslations(OPTION, STORE);
		catalogService.saveOrUpdate(option);

		final OfferTranslation offerTranslationsEn = createOfferTranslationsWithOption(Locale.ENGLISH.getLanguage(), OPTION,
				OPTION_DISPLAY_NAME_EN);
		final OfferTranslation offerTranslationsFr = createOfferTranslationsWithOption(Locale.FRANCE.getLanguage(), OPTION,
				OPTION_DISPLAY_NAME_FR);

		final Offer offer = createOfferWithTranslations(NOT_UPDATED_OFFER, STORE, Arrays.asList(offerTranslationsEn, offerTranslationsFr));
		catalogService.saveOrUpdate(offer);

		final Exchange exchange = createExchangeOption(OPTION, Collections.singletonList(UPDATED_OFFER));
		bulkEventProcessor.process(exchange);

		final Offer projection = catalogService.<Offer>read(OFFER_IDENTITY_TYPE, NOT_UPDATED_OFFER, STORE).orElseThrow(EntityNotFoundException::new);

		assertThat(projection.getTranslations())
				.extracting(offerTranslation -> offerTranslation.getOptions().get(0))
				.extracting(TranslationUnit::getDisplayName)
				.doesNotContain(COLOUR_EN, COLOUR_FR)
				.containsOnly(OPTION_DISPLAY_NAME_EN, OPTION_DISPLAY_NAME_FR);
	}

	@Test
	public void offerTranslationsShouldContainOnlyUpdatedSkuOptionDisplayNameWhenOfferCodeIsInSkuOptionBulkUpdateEvent() {
		final Option option = createOptionWithTranslations(OPTION, STORE);
		catalogService.saveOrUpdate(option);

		final OfferTranslation offerTranslationsEn = createOfferTranslationsWithOption(Locale.ENGLISH.getLanguage(), OPTION,
				OPTION_DISPLAY_NAME_EN);
		final OfferTranslation offerTranslationsFr = createOfferTranslationsWithOption(Locale.FRANCE.getLanguage(), OPTION,
				OPTION_DISPLAY_NAME_FR);

		final Offer offer = createOfferWithTranslations(UPDATED_OFFER, STORE, Arrays.asList(offerTranslationsEn, offerTranslationsFr));
		catalogService.saveOrUpdate(offer);

		final Exchange exchange = createExchangeOption(OPTION, Collections.singletonList(UPDATED_OFFER));
		bulkEventProcessor.process(exchange);

		final Offer projection = catalogService.<Offer>read(OFFER_IDENTITY_TYPE, UPDATED_OFFER, STORE).orElseThrow(EntityNotFoundException::new);

		assertThat(projection.getTranslations())
				.extracting(offerTranslation -> offerTranslation.getOptions().get(0))
				.extracting(TranslationUnit::getDisplayName)
				.containsOnly(COLOUR_EN, COLOUR_FR)
				.doesNotContain(OPTION_DISPLAY_NAME_EN, OPTION_DISPLAY_NAME_FR);
	}

	@Test
	public void offerTranslationsShouldNotContainUpdatedItemTranslationOptionDisplayNameWhenOfferCodeIsNotInSkuOptionBulkUpdateEvent() {
		final Option option = createOptionWithTranslations(OPTION, STORE);
		catalogService.saveOrUpdate(option);
		final ItemOptionTranslation itemOptionTranslationEn = new ItemOptionTranslation(OPTION_DISPLAY_NAME_EN, OPTION,
				OPTION_DISPLAY_VALUE_EN, VALUE);
		final ItemTranslation itemTranslationEn = new ItemTranslation("en", null,
				Collections.singletonList(itemOptionTranslationEn));
		final ItemOptionTranslation itemOptionTranslationFr = new ItemOptionTranslation(OPTION_DISPLAY_NAME_FR, OPTION,
				OPTION_DISPLAY_VALUE_FR, VALUE);
		final ItemTranslation itemTranslationFr = new ItemTranslation("fr", null,
				Collections.singletonList(itemOptionTranslationFr));
		final Item item = new Item("itemCode", new Object(), Collections.emptyList(), null, null,
				Arrays.asList(itemTranslationEn, itemTranslationFr));

		final Offer offer = createOfferWithItemTranslations(NOT_UPDATED_OFFER, STORE, Collections.singletonList(item));
		catalogService.saveOrUpdate(offer);

		final Exchange exchange = createExchangeOption(OPTION, Collections.singletonList(UPDATED_OFFER));
		bulkEventProcessor.process(exchange);

		final Offer projection = catalogService.<Offer>read(OFFER_IDENTITY_TYPE, NOT_UPDATED_OFFER, STORE).orElseThrow(EntityNotFoundException::new);

		assertThat(projection.getItems())
				.extracting(value -> value.getTranslations().get(0).getOptions().get(0))
				.extracting(ItemOptionTranslation::getDisplayName)
				.doesNotContain("Colour")
				.containsOnly(OPTION_DISPLAY_NAME_EN);
		assertThat(projection.getItems())
				.extracting(value -> value.getTranslations().get(1).getOptions().get(0))
				.extracting(ItemOptionTranslation::getDisplayName)
				.doesNotContain("Couleur")
				.containsOnly(OPTION_DISPLAY_NAME_FR);
	}

	@Test
	public void offerTranslationsShouldContainOnlyUpdatedItemTranslationOptionDisplayNameWhenOfferCodeIsInOptionBulkUpdateEvent() {
		final Option option = createOptionWithTranslations(OPTION, STORE);
		catalogService.saveOrUpdate(option);
		final ItemOptionTranslation itemOptionTranslationEn = new ItemOptionTranslation(OPTION_DISPLAY_NAME_EN, OPTION,
				OPTION_DISPLAY_VALUE_EN, VALUE);
		final ItemTranslation itemTranslationEn = new ItemTranslation("en", null,
				Collections.singletonList(itemOptionTranslationEn));
		final ItemOptionTranslation itemOptionTranslationFr = new ItemOptionTranslation(OPTION_DISPLAY_NAME_FR, OPTION,
				OPTION_DISPLAY_VALUE_FR, VALUE);
		final ItemTranslation itemTranslationFr = new ItemTranslation("fr", null,
				Collections.singletonList(itemOptionTranslationFr));
		final Item item = new Item("itemCode", new Object(), Collections.emptyList(), null, null,
				Arrays.asList(itemTranslationEn, itemTranslationFr));

		final Offer offer = createOfferWithItemTranslations(UPDATED_OFFER, STORE, Collections.singletonList(item));
		catalogService.saveOrUpdate(offer);

		final Exchange exchange = createExchangeOption(OPTION, Collections.singletonList(UPDATED_OFFER));
		bulkEventProcessor.process(exchange);

		final Offer projection = catalogService.<Offer>read(OFFER_IDENTITY_TYPE, UPDATED_OFFER, STORE).orElseThrow(EntityNotFoundException::new);

		assertThat(projection.getItems())
				.extracting(value -> value.getTranslations().get(0).getOptions().get(0))
				.extracting(ItemOptionTranslation::getDisplayName)
				.containsOnly("Colour")
				.doesNotContain(OPTION_DISPLAY_NAME_EN);
		assertThat(projection.getItems())
				.extracting(value -> value.getTranslations().get(1).getOptions().get(0))
				.extracting(ItemOptionTranslation::getDisplayName)
				.containsOnly("Couleur")
				.doesNotContain(OPTION_DISPLAY_NAME_FR);
	}

	@Test
	public void offerTranslationsShouldNotContainUpdatedItemTranslationOptionDisplayValueWhenOfferCodeIsNotInSkuOptionBulkUpdateEvent() {
		final Option option = createOptionWithTranslations(OPTION, STORE);
		catalogService.saveOrUpdate(option);
		final ItemOptionTranslation itemOptionTranslationEn = new ItemOptionTranslation(OPTION_DISPLAY_NAME_EN, OPTION,
				OPTION_DISPLAY_VALUE_EN, VALUE);
		final ItemTranslation itemTranslationEn = new ItemTranslation("en", null,
				Collections.singletonList(itemOptionTranslationEn));
		final ItemOptionTranslation itemOptionTranslationFr = new ItemOptionTranslation(OPTION_DISPLAY_NAME_FR, OPTION,
				OPTION_DISPLAY_VALUE_FR, VALUE);
		final ItemTranslation itemTranslationFr = new ItemTranslation("fr", null,
				Collections.singletonList(itemOptionTranslationFr));
		final Item item = new Item("itemCode", new Object(), Collections.emptyList(), null, null,
				Arrays.asList(itemTranslationEn, itemTranslationFr));

		final Offer offer = createOfferWithItemTranslations(NOT_UPDATED_OFFER, STORE, Collections.singletonList(item));
		catalogService.saveOrUpdate(offer);

		final Exchange exchange = createExchangeOption(OPTION, Collections.singletonList(UPDATED_OFFER));
		bulkEventProcessor.process(exchange);

		final Offer projection = catalogService.<Offer>read(OFFER_IDENTITY_TYPE, NOT_UPDATED_OFFER, STORE).orElseThrow(EntityNotFoundException::new);

		assertThat(projection.getItems())
				.extracting(value -> value.getTranslations().get(0).getOptions().get(0))
				.extracting(ItemOptionTranslation::getDisplayValue)
				.doesNotContain("Red")
				.containsOnly(OPTION_DISPLAY_VALUE_EN);
		assertThat(projection.getItems())
				.extracting(value -> value.getTranslations().get(1).getOptions().get(0))
				.extracting(ItemOptionTranslation::getDisplayValue)
				.doesNotContain("Rouge")
				.containsOnly(OPTION_DISPLAY_VALUE_FR);
	}

	@Test
	public void offerTranslationsShouldContainOnlyUpdatedItemTranslationOptionDisplayValueWhenOfferCodeIsInOptionBulkUpdateEvent() {
		final Option option = createOptionWithTranslations(OPTION, STORE);
		catalogService.saveOrUpdate(option);
		final ItemOptionTranslation itemOptionTranslationEn = new ItemOptionTranslation(OPTION_DISPLAY_NAME_EN, OPTION,
				OPTION_DISPLAY_VALUE_EN, VALUE);
		final ItemTranslation itemTranslationEn = new ItemTranslation("en", null,
				Collections.singletonList(itemOptionTranslationEn));
		final ItemOptionTranslation itemOptionTranslationFr = new ItemOptionTranslation(OPTION_DISPLAY_NAME_FR, OPTION,
				OPTION_DISPLAY_VALUE_FR, VALUE);
		final ItemTranslation itemTranslationFr = new ItemTranslation("fr", null,
				Collections.singletonList(itemOptionTranslationFr));
		final Item item = new Item("itemCode", new Object(), Collections.emptyList(), null, null,
				Arrays.asList(itemTranslationEn, itemTranslationFr));

		final Offer offer = createOfferWithItemTranslations(UPDATED_OFFER, STORE, Collections.singletonList(item));
		catalogService.saveOrUpdate(offer);

		final Exchange exchange = createExchangeOption(OPTION, Collections.singletonList(UPDATED_OFFER));
		bulkEventProcessor.process(exchange);

		final Offer projection = catalogService.<Offer>read(OFFER_IDENTITY_TYPE, UPDATED_OFFER, STORE).orElseThrow(EntityNotFoundException::new);

		assertThat(projection.getItems())
				.extracting(value -> value.getTranslations().get(0).getOptions().get(0))
				.extracting(ItemOptionTranslation::getDisplayValue)
				.containsOnly("Red")
				.doesNotContain(OPTION_DISPLAY_VALUE_EN);
		assertThat(projection.getItems())
				.extracting(value -> value.getTranslations().get(1).getOptions().get(0))
				.extracting(ItemOptionTranslation::getDisplayValue)
				.containsOnly("Rouge")
				.doesNotContain(OPTION_DISPLAY_VALUE_FR);

	}

	@Test
	public void offerTranslationsShouldNotContainUpdatedAttributeDisplayNameWhenOfferCodeIsNotInAttributeBulkUpdateEvent() {
		final Attribute attribute = createAttributeWithTranslations(ATTRIBUTE_CODE, STORE);
		catalogService.saveOrUpdate(attribute);

		final OfferTranslation offerTranslationsEn = createOfferTranslationsWithAttribute(Locale.ENGLISH.getLanguage(), ATTRIBUTE_NAME,
				ATTRIBUTE_DISPLAY_NAME_EN);
		final OfferTranslation offerTranslationsFr = createOfferTranslationsWithAttribute(Locale.FRANCE.getLanguage(), ATTRIBUTE_NAME,
				ATTRIBUTE_DISPLAY_NAME_FR);

		final Offer offer = createOfferWithTranslations(NOT_UPDATED_OFFER, STORE, Arrays.asList(offerTranslationsEn, offerTranslationsFr));
		catalogService.saveOrUpdate(offer);

		final Exchange exchange = createExchangeAttribute(ATTRIBUTE_CODE, Collections.singletonList(UPDATED_OFFER));
		bulkEventProcessor.process(exchange);

		Offer projection = (Offer) catalogService.read(OFFER_IDENTITY_TYPE, NOT_UPDATED_OFFER, STORE).orElseThrow(IllegalArgumentException::new);

		assertThat(projection.getTranslations())
				.extracting(offerTranslation -> offerTranslation.getDetails().get(0))
				.extracting(TranslationUnit::getDisplayName)
				.doesNotContain(UPDATED_ATTRIBUTE_DISPLAY_NAME_EN, UPDATED_ATTRIBUTE_DISPLAY_NAME_FR)
				.containsOnly(ATTRIBUTE_DISPLAY_NAME_EN, ATTRIBUTE_DISPLAY_NAME_FR);
	}

	@Test
	public void offerTranslationsShouldContainOnlyUpdatedAttributeDisplayNameWhenOfferCodeIsInAttributeBulkUpdateEvent() {
		final Attribute attribute = createAttributeWithTranslations(ATTRIBUTE_CODE, STORE);
		catalogService.saveOrUpdate(attribute);

		final OfferTranslation offerTranslationsEn = createOfferTranslationsWithAttribute(Locale.ENGLISH.getLanguage(), ATTRIBUTE_CODE,
				ATTRIBUTE_DISPLAY_NAME_EN);
		final OfferTranslation offerTranslationsFr = createOfferTranslationsWithAttribute(Locale.FRANCE.getLanguage(), ATTRIBUTE_CODE,
				ATTRIBUTE_DISPLAY_NAME_FR);

		final Offer offer = createOfferWithTranslations(UPDATED_OFFER, STORE, Arrays.asList(offerTranslationsEn, offerTranslationsFr));
		catalogService.saveOrUpdate(offer);

		final Exchange exchange = createExchangeAttribute(ATTRIBUTE_CODE, Collections.singletonList(UPDATED_OFFER));
		bulkEventProcessor.process(exchange);

		Offer projection = (Offer) catalogService.read(OFFER_IDENTITY_TYPE, UPDATED_OFFER, STORE).orElseThrow(IllegalArgumentException::new);

		assertThat(projection.getTranslations())
				.extracting(offerTranslation -> offerTranslation.getDetails().get(0))
				.extracting(TranslationUnit::getDisplayName)
				.containsOnly(UPDATED_ATTRIBUTE_DISPLAY_NAME_EN, UPDATED_ATTRIBUTE_DISPLAY_NAME_FR)
				.doesNotContain(ATTRIBUTE_DISPLAY_NAME_EN, ATTRIBUTE_DISPLAY_NAME_FR);
	}

	@Test
	public void offerTranslationsShouldNotContainUpdatedItemTranslationAttributeDisplayNameWhenOfferCodeIsNotInSkuAttributeBulkUpdateEvent() {
		final Attribute attribute = createAttributeWithTranslations(ATTRIBUTE_CODE, STORE);
		catalogService.saveOrUpdate(attribute);
		final DetailsTranslation detailsTranslationEn = new DetailsTranslation(
				new TranslationUnit(ATTRIBUTE_DISPLAY_NAME_EN, ATTRIBUTE_CODE), Collections.singletonList("displayValues"),
				Collections.singletonList("values"));
		final ItemTranslation itemTranslationEn = new ItemTranslation("en", Collections.singletonList(detailsTranslationEn),
				null);
		final DetailsTranslation detailsTranslationFr = new DetailsTranslation(
				new TranslationUnit(ATTRIBUTE_DISPLAY_NAME_FR, ATTRIBUTE_CODE), Collections.singletonList("displayValues"),
				Collections.singletonList("values"));
		final ItemTranslation itemTranslationFr = new ItemTranslation("fr", Collections.singletonList(detailsTranslationFr),
				null);
		final Item item = new Item("itemCode", new Object(), Collections.emptyList(), null, null,
				Arrays.asList(itemTranslationEn, itemTranslationFr));

		final Offer offer = createOfferWithItemTranslations(NOT_UPDATED_OFFER, STORE, Collections.singletonList(item));
		catalogService.saveOrUpdate(offer);

		final Exchange exchange = createExchangeSkuAttribute(ATTRIBUTE_CODE, Collections.singletonList(UPDATED_OFFER));
		bulkEventProcessor.process(exchange);

		Offer projection = (Offer) catalogService.read(OFFER_IDENTITY_TYPE, NOT_UPDATED_OFFER, STORE).orElseThrow(IllegalArgumentException::new);

		assertThat(projection.getItems())
				.extracting(value -> value.getTranslations().get(0).getDetails().get(0))
				.extracting(DetailsTranslation::getDisplayName)
				.doesNotContain(UPDATED_ATTRIBUTE_DISPLAY_NAME_EN)
				.containsOnly(ATTRIBUTE_DISPLAY_NAME_EN);
		assertThat(projection.getItems())
				.extracting(value -> value.getTranslations().get(1).getDetails().get(0))
				.extracting(DetailsTranslation::getDisplayName)
				.doesNotContain(UPDATED_ATTRIBUTE_DISPLAY_NAME_FR)
				.containsOnly(ATTRIBUTE_DISPLAY_NAME_FR);
	}

	@Test
	public void offerTranslationsShouldContainOnlyUpdatedItemTranslationAttributeDisplayNameWhenOfferCodeIsInSkuAttributeBulkUpdateEvent() {
		final Attribute attribute = createAttributeWithTranslations(ATTRIBUTE_CODE, STORE);
		catalogService.saveOrUpdate(attribute);
		final DetailsTranslation detailsTranslationEn = new DetailsTranslation(
				new TranslationUnit(ATTRIBUTE_DISPLAY_NAME_EN, ATTRIBUTE_CODE), Collections.singletonList("displayValues"),
				Collections.singletonList("values"));
		final ItemTranslation itemTranslationEn = new ItemTranslation("en", Collections.singletonList(detailsTranslationEn),
				null);
		final DetailsTranslation detailsTranslationFr = new DetailsTranslation(
				new TranslationUnit(ATTRIBUTE_DISPLAY_NAME_FR, ATTRIBUTE_CODE), Collections.singletonList("displayValues"),
				Collections.singletonList("values"));
		final ItemTranslation itemTranslationFr = new ItemTranslation("fr", Collections.singletonList(detailsTranslationFr),
				null);
		final Item item = new Item("itemCode", new Object(), Collections.emptyList(), null, null,
				Arrays.asList(itemTranslationEn, itemTranslationFr));

		final Offer offer = createOfferWithItemTranslations(UPDATED_OFFER, STORE, Collections.singletonList(item));
		catalogService.saveOrUpdate(offer);

		final Exchange exchange = createExchangeSkuAttribute(ATTRIBUTE_CODE, Collections.singletonList(UPDATED_OFFER));
		bulkEventProcessor.process(exchange);

		Offer projection = (Offer) catalogService.read(OFFER_IDENTITY_TYPE, UPDATED_OFFER, STORE).orElseThrow(IllegalArgumentException::new);

		assertThat(projection.getItems())
				.extracting(value -> value.getTranslations().get(0).getDetails().get(0))
				.extracting(DetailsTranslation::getDisplayName)
				.containsOnly(UPDATED_ATTRIBUTE_DISPLAY_NAME_EN)
				.doesNotContain(ATTRIBUTE_DISPLAY_NAME_EN);
		assertThat(projection.getItems())
				.extracting(value -> value.getTranslations().get(1).getDetails().get(0))
				.extracting(DetailsTranslation::getDisplayName)
				.containsOnly(UPDATED_ATTRIBUTE_DISPLAY_NAME_FR)
				.doesNotContain(ATTRIBUTE_DISPLAY_NAME_FR);
	}

	@Test
	public void categoryTranslationsShouldNotContainUpdatedAttributeDisplayNameWhenCategoryCodeIsNotInAttributeBulkUpdateEvent() {
		final Attribute attribute = createAttributeWithTranslations(ATTRIBUTE_CODE, STORE);
		catalogService.saveOrUpdate(attribute);

		final CategoryTranslation categoryTranslationsEn = createCategoryTranslationsWithAttribute(Locale.ENGLISH.getLanguage(), ATTRIBUTE_NAME,
				ATTRIBUTE_DISPLAY_NAME_EN);
		final CategoryTranslation categoryTranslationsFr = createCategoryTranslationsWithAttribute(Locale.FRANCE.getLanguage(), ATTRIBUTE_NAME,
				ATTRIBUTE_DISPLAY_NAME_FR);

		final Category category = createCategoryWithTranslations(NOT_UPDATED_CATEGORY, STORE, Arrays.asList(categoryTranslationsEn,
				categoryTranslationsFr));
		catalogService.saveOrUpdate(category);

		final Exchange exchange = createExchangeAttributeForCategory(ATTRIBUTE_CODE, Collections.singletonList(UPDATED_CATEGORY));
		bulkEventProcessor.process(exchange);

		Category projection =
				(Category) catalogService.read(CATEGORY_IDENTITY_TYPE, NOT_UPDATED_CATEGORY, STORE).orElseThrow(IllegalArgumentException::new);

		assertThat(projection.getTranslations())
				.extracting(categoryTranslation -> categoryTranslation.getDetails().get(0))
				.extracting(TranslationUnit::getDisplayName)
				.doesNotContain(UPDATED_ATTRIBUTE_DISPLAY_NAME_EN, UPDATED_ATTRIBUTE_DISPLAY_NAME_FR)
				.containsOnly(ATTRIBUTE_DISPLAY_NAME_EN, ATTRIBUTE_DISPLAY_NAME_FR);
	}

	@Test
	public void categoryTranslationsShouldContainOnlyUpdatedAttributeDisplayNameWhenCategoryCodeIsInAttributeBulkUpdateEvent() {
		final Attribute attribute = createAttributeWithTranslations(ATTRIBUTE_CODE, STORE);
		catalogService.saveOrUpdate(attribute);

		final CategoryTranslation categoryTranslationsEn = createCategoryTranslationsWithAttribute(Locale.ENGLISH.getLanguage(), ATTRIBUTE_CODE,
				ATTRIBUTE_DISPLAY_NAME_EN);
		final CategoryTranslation categoryTranslationsFr = createCategoryTranslationsWithAttribute(Locale.FRANCE.getLanguage(), ATTRIBUTE_CODE,
				ATTRIBUTE_DISPLAY_NAME_FR);

		final Category category = createCategoryWithTranslations(UPDATED_CATEGORY, STORE, Arrays.asList(categoryTranslationsEn,
				categoryTranslationsFr));
		catalogService.saveOrUpdate(category);

		final Exchange exchange = createExchangeAttributeForCategory(ATTRIBUTE_CODE, Collections.singletonList(UPDATED_CATEGORY));
		bulkEventProcessor.process(exchange);

		Category projection =
				(Category) catalogService.read(CATEGORY_IDENTITY_TYPE, UPDATED_CATEGORY, STORE).orElseThrow(IllegalArgumentException::new);

		assertThat(projection.getTranslations())
				.extracting(categoryTranslation -> categoryTranslation.getDetails().get(0))
				.extracting(TranslationUnit::getDisplayName)
				.containsOnly(UPDATED_ATTRIBUTE_DISPLAY_NAME_EN, UPDATED_ATTRIBUTE_DISPLAY_NAME_FR)
				.doesNotContain(ATTRIBUTE_DISPLAY_NAME_EN, ATTRIBUTE_DISPLAY_NAME_FR);
	}

	private Brand createBrandWithTranslations(final String code, final String store, final List<Translation> translations) {
		final ZonedDateTime modifiedDateTime = ZonedDateTime.now();
		final boolean deleted = false;

		return new Brand(code, store, translations, modifiedDateTime, deleted);
	}

	private Option createOptionWithTranslations(final String code, final String store) {
		final List<TranslatedName> optionValuesRedEn = new ArrayList<>();
		optionValuesRedEn.add(RED_TRANSLATION_EN);
		final List<TranslatedName> optionValuesRedFr = new ArrayList<>();
		optionValuesRedFr.add(RED_TRANSLATION_FR);
		final List<OptionTranslation> translations = new ArrayList<>();
		translations.add(new OptionTranslation(LANGUAGE_EN, COLOUR_EN, optionValuesRedEn));
		translations.add(new OptionTranslation(LANGUAGE_FR, COLOUR_FR, optionValuesRedFr));

		final ZonedDateTime modifiedDateTime = ZonedDateTime.now();
		final boolean deleted = false;
		return new Option(code, store, translations, modifiedDateTime, deleted);
	}

	private Offer createOfferWithTranslations(final String code, final String store, final List<OfferTranslation> translations) {
		final NameIdentity nameIdentity = new NameIdentity(OFFER_IDENTITY_TYPE, code, store);
		final ZonedDateTime modifiedDateTime = ZonedDateTime.now();
		final boolean deleted = false;
		final List<Item> items = Collections.emptyList();
		final Object extensions = new Object();
		final List<Property> properties = Collections.emptyList();
		final OfferAvailabilityRules availabilityRules = null;
		final List<Association> associations = null;
		final SelectionRules selectionRules = null;
		final Components components = null;
		final List<String> formFields = Collections.emptyList();
		final Set<OfferCategories> categories = Collections.emptySet();

		return new Offer(nameIdentity, modifiedDateTime, deleted, items, extensions, properties, availabilityRules, associations, selectionRules,
				components, formFields, translations, categories);
	}

	private Offer createOfferWithItemTranslations(final String code, final String store, final List<Item> items) {
		final NameIdentity nameIdentity = new NameIdentity(OFFER_IDENTITY_TYPE, code, store);
		final ZonedDateTime modifiedDateTime = ZonedDateTime.now();
		final boolean deleted = false;
		final List<OfferTranslation> translations = Collections.emptyList();
		final Object extensions = new Object();
		final List<Property> properties = Collections.emptyList();
		final OfferAvailabilityRules availabilityRules = null;
		final List<Association> associations = null;
		final SelectionRules selectionRules = null;
		final Components components = null;
		final List<String> formFields = Collections.emptyList();
		final Set<OfferCategories> categories = Collections.emptySet();
		return new Offer(nameIdentity, modifiedDateTime, deleted, items, extensions, properties, availabilityRules, associations, selectionRules,
				components, formFields, translations, categories);
	}

	private Attribute createAttributeWithTranslations(final String code, final String store) {
		final List<AttributeTranslation> translations = new ArrayList<>();
		translations.add(new AttributeTranslation(Locale.ENGLISH.getLanguage(),
				UPDATED_ATTRIBUTE_DISPLAY_NAME_EN, "", false));
		translations.add(new AttributeTranslation(Locale.FRANCE.getLanguage(),
				UPDATED_ATTRIBUTE_DISPLAY_NAME_FR, "", false));
		final ZonedDateTime modifiedDateTime = ZonedDateTime.now();
		final boolean deleted = false;
		return new Attribute(code, store, translations, modifiedDateTime, deleted);
	}

	private Category createCategoryWithTranslations(final String code, final String store, final List<CategoryTranslation> translations) {
		final NameIdentity nameIdentity = new NameIdentity(CATEGORY_IDENTITY_TYPE, code, store);
		final ZonedDateTime modifiedDateTime = ZonedDateTime.now();
		final boolean deleted = false;
		final Object extensions = new Object();
		final List<Property> properties = Collections.emptyList();
		final AvailabilityRules availabilityRules = new AvailabilityRules(null, null);
		final List<String> path = Collections.emptyList();
		final String parent = null;
		final List<String> children = Collections.emptyList();
		return new Category(new CategoryProperties(
				new ProjectionProperties(nameIdentity.getCode(),
						nameIdentity.getStore(),
						modifiedDateTime,
						deleted),
				properties),
				extensions,
				translations,
				children,
				availabilityRules,
				path,
				parent);
	}

	private OfferTranslation createOfferTranslationsWithBrand(final String language, final String name, final String displayName) {
		final TranslationUnit brandTranslationUnit = new TranslationUnit(displayName, name);
		final List<TranslationUnit> options = Collections.emptyList();
		final List<DetailsTranslation> details = Collections.emptyList();

		return new OfferTranslation(language, OFFER_TRANSLATION_DISPLAY_NAME, brandTranslationUnit, options, details);
	}

	private OfferTranslation createOfferTranslationsWithOption(final String language, final String name, final String displayName) {
		final TranslationUnit translationUnit = null;
		final List<TranslationUnit> options = Collections.singletonList(new TranslationUnit(displayName, name));
		final List<DetailsTranslation> details = Collections.emptyList();

		return new OfferTranslation(language, OFFER_TRANSLATION_DISPLAY_NAME, translationUnit, options, details);
	}

	private OfferTranslation createOfferTranslationsWithAttribute(final String language, final String name, final String displayName) {
		final TranslationUnit translationUnit = null;
		final List<TranslationUnit> options = Collections.emptyList();
		final List<DetailsTranslation> details = Collections.singletonList(new DetailsTranslation(
				new TranslationUnit(displayName, name), Collections.singletonList("displayValues"), Collections.singletonList("values")));
		return new OfferTranslation(language, OFFER_TRANSLATION_DISPLAY_NAME, translationUnit, options, details);
	}

	private CategoryTranslation createCategoryTranslationsWithAttribute(final String language, final String name, final String displayName) {
		final List<DetailsTranslation> details = Collections.singletonList(new DetailsTranslation(
				new TranslationUnit(displayName, name), Collections.singletonList("displayValues"), Collections.singletonList("values")));
		return new CategoryTranslation(new Translation(language, displayName), details);
	}

	private Exchange createExchangeOption(final String option, final List<String> products) {
		final EventMessage eventMessage = new EventMessageImpl(OPTION_BULK_UPDATE, option, Collections.singletonMap(PRODUCTS, products));

		final Message message = new DefaultMessage(camelContext);
		message.setBody(eventMessage);

		final DefaultExchange exchange = new DefaultExchange(camelContext);
		exchange.setIn(message);

		return exchange;
	}

	private Exchange createExchange(final String brand, final List<String> products) {
		final EventMessage eventMessage = new EventMessageImpl(BRAND_BULK_UPDATE, brand, Collections.singletonMap(PRODUCTS, products));

		final Message message = new DefaultMessage(camelContext);
		message.setBody(eventMessage);

		final DefaultExchange exchange = new DefaultExchange(camelContext);
		exchange.setIn(message);

		return exchange;
	}

	private Exchange createExchangeAttribute(final String attribute, final List<String> products) {
		final EventMessage eventMessage = new EventMessageImpl(ATTRIBUTE_BULK_UPDATE, attribute, Collections.singletonMap(PRODUCTS, products));

		final Message message = new DefaultMessage(camelContext);
		message.setBody(eventMessage);

		final DefaultExchange exchange = new DefaultExchange(camelContext);
		exchange.setIn(message);

		return exchange;
	}

	private Exchange createExchangeSkuAttribute(final String attribute, final List<String> products) {
		final EventMessage eventMessage = new EventMessageImpl(ATTRIBUTE_SKU_BULK_UPDATE, attribute, Collections.singletonMap(PRODUCTS, products));

		final Message message = new DefaultMessage(camelContext);
		message.setBody(eventMessage);

		final DefaultExchange exchange = new DefaultExchange(camelContext);
		exchange.setIn(message);

		return exchange;
	}

	private Exchange createExchangeAttributeForCategory(final String attribute, final List<String> categories) {
		final EventMessage eventMessage = new EventMessageImpl(ATTRIBUTE_CATEGORY_BULK_UPDATE, attribute, Collections.singletonMap(PRODUCTS,
				categories));

		final Message message = new DefaultMessage(camelContext);
		message.setBody(eventMessage);

		final DefaultExchange exchange = new DefaultExchange(camelContext);
		exchange.setIn(message);

		return exchange;
	}
}
