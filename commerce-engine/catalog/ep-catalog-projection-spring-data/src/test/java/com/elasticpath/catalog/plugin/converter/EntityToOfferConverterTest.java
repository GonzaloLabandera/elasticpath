/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.offer.Association;
import com.elasticpath.catalog.entity.offer.AssociationValue;
import com.elasticpath.catalog.entity.offer.Item;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.offer.OfferCategories;
import com.elasticpath.catalog.entity.offer.OfferProperties;
import com.elasticpath.catalog.entity.offer.OfferRules;
import com.elasticpath.catalog.entity.Property;
import com.elasticpath.catalog.entity.offer.SelectionType;
import com.elasticpath.catalog.plugin.converter.impl.EntityToOfferConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;

/**
 * Test for {@link EntityToOfferConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityToOfferConverterTest {

	private static final String STORE = "store";
	private static final String CODE = "code";
	private static final String HASH = "hash";
	private static final String SCHEMA_VERSION = "1.1";
	private static final String ZONED_DATE_TIME = "2019-01-09T23:12:00.000+03:00";

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoOfferProjectionWithFilledContent() {
		final int expectedPropertiesSize = 4;
		final int expectedExtensions = 3;

		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore("store");
		projectionId.setCode(CODE);
		projectionId.setType(OFFER_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent("{\"items\":[{\"itemCode\":\"testCode1\", \"extensions\":1, "
				+ "\"properties\":[{\"name\":\"ITEM_TYPE\",\"value\":\"PHYSICAL\"},{\"name\":\"TAX_CODE\",\"value\":\"PC040100\"}]}],"
				+ "\"extensions\":3, \"properties\":[{\"name\":\"OFFER_TYPE\", \"value\":\"WOMENS_SHIRTS\"},{\"name\":\"NOT_SOLD_SEPARATELY\", "
				+ "\"value\":\"true\"}, {\"name\":\"MINIMUM_ORDER_QUANTITY\", \"value\":\"1\"},"
				+ " {\"name\":\"BUNDLE_PRICING\",\"value\":\"ASSIGNED\"}],\"associations\":[{\"type\":\"crosssell\","
				+ "\"list\":[{\"offer\":\"offerCode\",\"enableDateTime\":\"2018-01-01T14:47:00.754+00:00\",\"disableDateTime\":\"2018-01-15T14:47:00"
				+ ".754+00:00\"}]}]}");
		entity.setSchemaVersion("1.1");
		entity.setContentHash("hash");
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		Offer offer = converter.convert(entity);

		assertThat(offer).isNotNull();
		assertThat(offer.getModifiedDateTime()).isEqualTo(ZonedDateTime.ofInstant(new Date(1).toInstant(), ZoneId.of("GMT")));
		assertFalse(offer.isDeleted());
		assertThat(offer.getExtensions()).isEqualTo(expectedExtensions);
		assertThat(offer.getIdentity().getCode()).isEqualTo(CODE);
		assertThat(offer.getIdentity().getStore()).isEqualTo(STORE);
		assertThat(offer.getIdentity().getType()).isEqualTo(OFFER_IDENTITY_TYPE);
		assertThat(offer.getItems().size()).isEqualTo(1);
		assertThat(offer.getItems().get(0).getItemCode()).isEqualTo("testCode1");
		assertThat(offer.getItems().get(0).getExtensions()).isEqualTo(1);
		assertThat(offer.getProperties().size()).isEqualTo(expectedPropertiesSize);
		assertThat(offer.getProperties()).extracting(Property::getName)
				.containsExactly("OFFER_TYPE", "NOT_SOLD_SEPARATELY", "MINIMUM_ORDER_QUANTITY", "BUNDLE_PRICING");
		assertThat(offer.getProperties()).extracting(Property::getValue)
				.containsExactly("WOMENS_SHIRTS", "true", "1", "ASSIGNED");
		assertThat(offer.getAssociations()).isNotEmpty();
		assertThat(offer.getAssociations()).extracting(Association::getType).containsOnly("crosssell");
		assertThat(offer.getAssociations().get(0).getList()).extracting(AssociationValue::getOffer).containsOnly("offerCode");
		assertThat(offer.getAssociations().get(0).getList()).extracting(AssociationValue::getEnableDateTime).containsOnly(ZonedDateTime.parse("2018"
				+ "-01-01T14:47:00.754+00:00").withZoneSameLocal(ZoneId.of("UTC")));
		assertThat(offer.getAssociations().get(0).getList()).extracting(AssociationValue::getDisableDateTime).containsOnly(ZonedDateTime.parse("2018"
				+ "-01-15T14:47:00.754+00:00").withZoneSameLocal(ZoneId.of("UTC")));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoOfferProjectionWithEmptyContent() {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore("store");
		projectionId.setCode(CODE);
		projectionId.setType(OFFER_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent(null);
		entity.setSchemaVersion(null);
		entity.setContentHash(null);
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		Offer offer = converter.convert(entity);

		assertThat(offer.getItems()).isEmpty();
	}

	@Test
	public void availabilityRulesShouldContainsEnableDateTimeSameAsEnableDateTimeFromContent() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent("{\"items\":[],\"availabilityRules\":{\"enableDateTime\":\""
				+ ZONED_DATE_TIME + "\"}}");

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);

		assertThat(offer.getAvailabilityRules().getEnableDateTime()).isEqualTo(ZonedDateTime.parse(ZONED_DATE_TIME));
	}

	@Test
	public void availabilityRulesShouldContainsDisableDateTimeSameAsDisableDateTimeFromContent() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent("{\"items\":[],\"availabilityRules\":{\"disableDateTime\":\""
				+ ZONED_DATE_TIME + "\"}}");

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);

		assertThat(offer.getAvailabilityRules().getDisableDateTime()).isEqualTo(ZonedDateTime.parse(ZONED_DATE_TIME));
	}

	@Test
	public void availabilityRulesShouldContainsReleaseDateTimeSameAsReleaseDateTimeFromContent() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent("{\"items\":[],\"availabilityRules\":{\"releaseDateTime\":\""
				+ ZONED_DATE_TIME + "\"}}");

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);

		assertThat(offer.getAvailabilityRules().getReleaseDateTime()).isEqualTo(ZonedDateTime.parse(ZONED_DATE_TIME));
	}

	@Test
	public void availabilityRulesShouldContainsCanDiscoverSameAsCanDiscoverFromContent() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent("{\"items\":[],\"availabilityRules\":{"
				+ "\"canDiscover\":[\"ALWAYS\"]}}");

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);

		assertThat(offer.getAvailabilityRules().getCanDiscover()).containsExactly("ALWAYS");
	}

	@Test
	public void availabilityRulesShouldContainsCanViewSameAsCanViewFromContent() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent("{\"items\":[],\"availabilityRules\":{"
				+ "\"canView\":[\"ALWAYS\"]}}");

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);

		assertThat(offer.getAvailabilityRules().getCanView()).containsExactly("ALWAYS");
	}

	@Test
	public void availabilityRulesShouldContainsCanAddToCartSameAsCanViewFromContent() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent("{\"items\":[],"
				+ "\"availabilityRules\":{\"canAddToCart\":[\"ALWAYS\"]}}");

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);

		assertThat(offer.getAvailabilityRules().getCanAddToCart()).containsExactly("ALWAYS");
	}

	@Test
	public void itemAvailabilityRulesShouldContainsEnableDateTimeSameAsEnableDateTimeFromContent() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent("{\"items\":[{\"availabilityRules\":{\"enableDateTime\":\""
				+ ZONED_DATE_TIME + "\"}}]}");

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);

		assertThat(offer.getItems().get(0).getAvailabilityRules().getEnableDateTime()).isEqualTo(ZonedDateTime.parse(ZONED_DATE_TIME));
	}

	@Test
	public void itemAvailabilityRulesShouldContainsDisableDateTimeSameAsEnableDateTimeFromContent() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent("{\"items\":[{\"availabilityRules\":{\"disableDateTime\":\""
				+ ZONED_DATE_TIME + "\"}}]}");

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);

		assertThat(offer.getItems().get(0).getAvailabilityRules().getDisableDateTime()).isEqualTo(ZonedDateTime.parse(ZONED_DATE_TIME));
	}

	@Test
	public void selectionRulesAreParsedByConverter() {
		final int quantity = 3;

		final ProjectionEntity projectionEntity = createProjectionEntityWithContent("{\"items\":[],  \"selectionRules\": {\"selectionType\": "
				+ "\"COMPONENT\", \"quantity\": 3}}");

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);

		assertThat(offer.getSelectionRules().getSelectionType()).isEqualTo(SelectionType.COMPONENT);
		assertThat(offer.getSelectionRules().getQuantity()).isEqualTo(quantity);
	}

	@Test
	public void componentsAreParsedByConverter() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent("{\"items\":[],  \"components\": "
				+ "{\"list\":[{\"offer\":\"offerCode\", \"item\":\"itemCode\",\"quantity\": 2}]}}");

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);

		assertThat(offer.getComponents().getList().get(0).getOffer()).isEqualTo("offerCode");
		assertThat(offer.getComponents().getList().get(0).getItem()).isEqualTo("itemCode");
		assertThat(offer.getComponents().getList().get(0).getQuantity()).isEqualTo(2);
	}

	@Test
	public void formFieldsAreParsedByConverter() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent("{\"items\":[], \"formFields\": [\"field1\",\"field2\"]}");

		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);

		assertThat(offer.getFormFields().size()).isEqualTo(2);
		assertThat(offer.getFormFields().get(0)).isEqualTo("field1");
	}

	@Test
	public void shippingPropertiesAreParsedByConverter() {
		final BigDecimal expectedWeight = new BigDecimal(5);
		final BigDecimal expectedWidth = new BigDecimal(6);
		final BigDecimal expectedLength = new BigDecimal(7);
		final BigDecimal expectedHeight = new BigDecimal(8);
		final String unitsWeight = "KG";
		final String unitsLength = "M";

		final ProjectionEntity projectionEntity = createProjectionEntityWithContent(
				"{\"items\":[{\"shippingProperties\":{\"weight\":5, \"width\":6, \"length\":7, \"height\":8, \"unitsWeight\":\"KG\","
						+ " \"unitsLength\":\"M\"}}]}");
		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);
		assertThat(offer.getItems().get(0).getShippingProperties().getWeight()).isEqualTo(expectedWeight);
		assertThat(offer.getItems().get(0).getShippingProperties().getWidth()).isEqualTo(expectedWidth);
		assertThat(offer.getItems().get(0).getShippingProperties().getLength()).isEqualTo(expectedLength);
		assertThat(offer.getItems().get(0).getShippingProperties().getHeight()).isEqualTo(expectedHeight);
		assertThat(offer.getItems().get(0).getShippingProperties().getUnitsWeight()).isEqualTo(unitsWeight);
		assertThat(offer.getItems().get(0).getShippingProperties().getUnitsLength()).isEqualTo(unitsLength);
	}

	@Test
	public void offerExtensionsFilledAsEmptyObject() throws JsonProcessingException {
		ProjectionProperties projectionProperties = new ProjectionProperties(null, null, null, false);
		Property property = new Property(null, null);
		OfferProperties offerProperties = new OfferProperties(projectionProperties, Collections.singletonList(property));

		OfferRules offerRules = new OfferRules(null, null);

		assertThat(objectMapper.writeValueAsString(new Offer(offerProperties, Collections.emptyList(),
				new Object(), Collections.emptyList(), null, offerRules, null, Collections.emptyList(), Collections.emptySet())))
				.contains("\"extensions\":{}");
	}

	@Test
	public void itemsFilledAsEmptyArray() throws JsonProcessingException {
		ProjectionProperties projectionProperties = new ProjectionProperties(null, null, null, false);
		Property property = new Property(null, null);
		OfferProperties offerProperties = new OfferProperties(projectionProperties, Collections.singletonList(property));

		OfferRules offerRules = new OfferRules(null, null);

		assertThat(objectMapper.writeValueAsString(new Offer(offerProperties, Collections.emptyList(),
				new Object(), Collections.emptyList(), null, offerRules, null, Collections.emptyList(), Collections.emptySet())))
				.contains("\"items\":[]");
	}

	@Test
	public void itemExtensionsFilledAsEmptyObject() throws JsonProcessingException {
		ProjectionProperties projectionProperties = new ProjectionProperties(null, null, null, false);
		Property property = new Property(null, null);
		OfferProperties offerProperties = new OfferProperties(projectionProperties, Collections.singletonList(property));

		OfferRules offerRules = new OfferRules(null, null);
		Item item = new Item(null, 1, null, null, null, Collections.emptyList());

		assertThat(objectMapper.writeValueAsString(new Offer(offerProperties, Collections.singletonList(item),
				new Object(), Collections.emptyList(), null, offerRules, null, Collections.emptyList(), Collections.emptySet())))
				.contains("\"extensions\":{}");
	}

	@Test
	public void categoriesAreParsedByConverter() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent(
				"{\"categories\":[{\"code\": \"code\", \"path\": [\"path\"],"
						+ "\"default\": true, \"featured\": 1}]}");
		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);
		final OfferCategories categories = offer.getCategories().iterator().next();
		assertThat(offer.getCategories().size()).isEqualTo(1);
		assertThat(categories.getCode()).isEqualTo(CODE);
		assertThat(categories.getPath().get(0)).isEqualTo("path");
		assertThat(categories.isDefaultCategory()).isTrue();
		assertThat(categories.getFeatured()).isEqualTo(1);
	}

	@Test
	public void translationsAreParsedByConverter() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent(
				"{\"translations\": [{\"language\": \"en\",\"displayName\": \"Team Jersey\",\"brand\": {\"displayName\": \"Dior Sports\","
						+ "\"name\": \"DIOR_SPORTS\"},\"options\": [{\"displayName\": \"Colour\",\"name\": \"COLOR\"}],\"details\": "
						+ "[{\"displayName\": \"Fabric\",\"displayValues\": [\"Cotton\"],\"name\": \"FABRIC\",\"values\": [\"Cotton_value\"]}]}]}");
		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);
		assertThat(offer.getTranslations().get(0).getLanguage()).isEqualTo("en");
		assertThat(offer.getTranslations().get(0).getDisplayName()).isEqualTo("Team Jersey");
		assertThat(offer.getTranslations().get(0).getBrand().getDisplayName()).isEqualTo("Dior Sports");
		assertThat(offer.getTranslations().get(0).getBrand().getName()).isEqualTo("DIOR_SPORTS");
		assertThat(offer.getTranslations().get(0).getOptions().get(0).getName()).isEqualTo("COLOR");
		assertThat(offer.getTranslations().get(0).getOptions().get(0).getDisplayName()).isEqualTo("Colour");
		assertThat(offer.getTranslations().get(0).getDetails().get(0).getDisplayName()).isEqualTo("Fabric");
		assertThat(offer.getTranslations().get(0).getDetails().get(0).getName()).isEqualTo("FABRIC");
		assertThat(offer.getTranslations().get(0).getDetails().get(0).getDisplayValues().get(0)).isEqualTo("Cotton");
		assertThat(offer.getTranslations().get(0).getDetails().get(0).getValues().get(0)).isEqualTo("Cotton_value");
	}

	@Test
	public void itemTranslationsAreParsedByConverter() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent(
				"{\"items\": [{\"translations\": [{\"language\": \"en\",\"details\": [{\"displayName\": \"Image Gallery\",\"displayValues\": "
						+ "[\"<imagePathA>\"],\"name\": \"ITEM_IMAGES\",\"values\": [\"<image1Path>\"]}],\"options\": [{\"displayName\": \"Colour\","
						+ "\"displayValue\": \"Red\",\"name\": \"COLOR\",\"value\": \"RED\"}]}]}]}");
		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);
		assertThat(offer.getItems().get(0).getTranslations().get(0).getLanguage()).isEqualTo("en");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getOptions().get(0).getDisplayValue()).isEqualTo("Red");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getOptions().get(0).getValue()).isEqualTo("RED");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getOptions().get(0).getDisplayName()).isEqualTo("Colour");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getOptions().get(0).getName()).isEqualTo("COLOR");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getDetails().get(0).getName()).isEqualTo("ITEM_IMAGES");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getDetails().get(0).getDisplayName()).isEqualTo("Image Gallery");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getDetails().get(0).getDisplayValues().get(0)).isEqualTo("<imagePathA>");
		assertThat(offer.getItems().get(0).getTranslations().get(0).getDetails().get(0).getValues().get(0)).isEqualTo("<image1Path>");
	}

	@Test
	public void itemsShouldBeConvertedToNullForDeletedOffer() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent(null);
		projectionEntity.setDeleted(true);
		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);
		assertThat(offer.getItems()).isNull();
	}

	@Test
	public void propertiesShouldBeConvertedToNullForDeletedOffer() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent(null);
		projectionEntity.setDeleted(true);
		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);
		assertThat(offer.getProperties()).isNull();
	}

	@Test
	public void associationsShouldBeConvertedToNullForDeletedOffer() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent(null);
		projectionEntity.setDeleted(true);
		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);
		assertThat(offer.getAssociations()).isNull();
	}

	@Test
	public void translationsShouldBeConvertedToNullForDeletedOffer() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent(null);
		projectionEntity.setDeleted(true);
		final EntityToOfferConverter converter = new EntityToOfferConverter(objectMapper);
		final Offer offer = converter.convert(projectionEntity);
		assertThat(offer.getTranslations()).isNull();
	}

	private ProjectionEntity createProjectionEntityWithContent(final String content) {
		final ProjectionId projectionId = new ProjectionId();
		projectionId.setStore(STORE);
		projectionId.setCode(CODE);
		projectionId.setType(OFFER_IDENTITY_TYPE);
		final ProjectionEntity projectionEntity = new ProjectionEntity();
		projectionEntity.setProjectionId(projectionId);
		projectionEntity.setContent(content);
		projectionEntity.setSchemaVersion(SCHEMA_VERSION);
		projectionEntity.setContentHash(HASH);
		projectionEntity.setDeleted(false);
		projectionEntity.setProjectionDateTime(new Date());

		return projectionEntity;
	}

}
