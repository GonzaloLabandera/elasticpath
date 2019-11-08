/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.attribute;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.ATTRIBUTE_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.service.misc.TimeService;

/**
 * Tests {@link AttributeBulkUpdateProcessorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributeBulkUpdateProcessorImplTest {
	private static final String ATTRIBUTE_CODE = "attributeCode";
	private static final String STORE = "store";
	private static final String FIRST_PRODUCT = "product1";
	private static final String SECOND_PRODUCT = "product2";

	@Mock
	private CatalogService catalogService;

	@Mock
	private TimeService timeService;

	@InjectMocks
	private AttributeBulkUpdateProcessorImpl attributeBulkUpdateProcessor;

	@InjectMocks
	private AttributeCategoryBulkUpdateProcessorImpl attributeCategoryBulkUpdateProcessor;

	@InjectMocks
	private AttributeSkuBulkUpdateProcessorImpl attributeSkuBulkUpdateProcessor;

	/**
	 * Set up tests.
	 */
	@Before
	public void setUp() {
		final Date now = new Date();
		when(timeService.getCurrentTime()).thenReturn(now);
	}

	/**
	 * Method attributeBulkUpdateProcessor#updateSkuAttributeDisplayNameInOffers should calls CatalogService#readAll by attribute code and offers
	 * codes.
	 */
	@Test
	public void testThatUpdateAttributeDisplayNameInOffersShouldCallsReadAllAttributeAndReadAllOffers() {
		final List<String> offerCodes = Arrays.asList(FIRST_PRODUCT, SECOND_PRODUCT);

		attributeBulkUpdateProcessor.updateAttributeDisplayNameInOffers(offerCodes, ATTRIBUTE_CODE);

		verify(catalogService).readAll(ATTRIBUTE_IDENTITY_TYPE, ATTRIBUTE_CODE);
		verify(catalogService).readAll(OFFER_IDENTITY_TYPE, offerCodes);
	}

	/**
	 * Method attributeBulkUpdateProcessor#updateSkuAttributeDisplayNameInOffers should calls TimeService#getCurrentTime for each updated offer.
	 */
	@Test
	public void testThatUpdateAttributeDisplayNameInOffersShouldCallsGetCurrentTimeSameAsOfferNumberTimes() {
		final int offersNumber = 2;
		final List<String> offersCodes = Arrays.asList(FIRST_PRODUCT, SECOND_PRODUCT);

		final NameIdentity identity = mock(NameIdentity.class);
		when(identity.getStore()).thenReturn(STORE);

		final Attribute attribute = mock(Attribute.class);
		when((attribute.getIdentity())).thenReturn(identity);

		final NameIdentity offerIdentity = mock(NameIdentity.class);
		when(offerIdentity.getStore()).thenReturn(STORE);

		final Offer offer = mock(Offer.class);
		when(offer.getIdentity()).thenReturn(offerIdentity);
		when(offer.isDeleted()).thenReturn(false);

		when(catalogService.readAll(ATTRIBUTE_IDENTITY_TYPE, ATTRIBUTE_CODE)).thenReturn(Collections.singletonList(attribute));
		when(catalogService.readAll(OFFER_IDENTITY_TYPE, offersCodes)).thenReturn(Collections.nCopies(offersNumber, offer));

		attributeBulkUpdateProcessor.updateAttributeDisplayNameInOffers(offersCodes, ATTRIBUTE_CODE);

		verify(timeService, times(offersNumber)).getCurrentTime();
	}

	/**
	 * Method attributeCategoryBulkUpdateProcessor#updateCategoryAttributeDisplayNameInCategories should calls CatalogService#readAll
	 * by attribute code and categories codes.
	 */
	@Test
	public void testThatUpdateCategoryAttributeDisplayNameInCategoriesShouldCallsReadAllAttributeAndReadAllCategories() {
		final List<String> categoryCodes = Arrays.asList("category1", "category2");

		attributeCategoryBulkUpdateProcessor.updateCategoryAttributeDisplayNameInCategories(categoryCodes, ATTRIBUTE_CODE);

		verify(catalogService).readAll(ATTRIBUTE_IDENTITY_TYPE, ATTRIBUTE_CODE);
		verify(catalogService).readAll(CATEGORY_IDENTITY_TYPE, categoryCodes);
	}

	/**
	 * Method attributeCategoryBulkUpdateProcessor#updateCategoryAttributeDisplayNameInCategories should calls TimeService#getCurrentTime for each
	 * updated
	 * category.
	 */
	@Test
	public void testThatUpdateCategoryAttributeDisplayNameInCategoriesShouldCallsGetCurrentTimeSameAsCategoryNumberTimes() {
		final int categoryNumbers = 2;
		final List<String> categoryCodes = Arrays.asList("category1", "category2");

		final NameIdentity identity = mock(NameIdentity.class);
		when(identity.getStore()).thenReturn(STORE);

		final Attribute attribute = mock(Attribute.class);
		when((attribute.getIdentity())).thenReturn(identity);

		final NameIdentity categoryIdentity = mock(NameIdentity.class);
		when(categoryIdentity.getStore()).thenReturn(STORE);

		final Category category = mock(Category.class);
		when(category.getIdentity()).thenReturn(categoryIdentity);
		when(category.isDeleted()).thenReturn(false);
		when(catalogService.readAll(ATTRIBUTE_IDENTITY_TYPE, ATTRIBUTE_CODE)).thenReturn(Collections.singletonList(attribute));
		when(catalogService.readAll(CATEGORY_IDENTITY_TYPE, categoryCodes)).thenReturn(Collections.nCopies(categoryNumbers, category));

		attributeCategoryBulkUpdateProcessor.updateCategoryAttributeDisplayNameInCategories(categoryCodes, ATTRIBUTE_CODE);

		verify(timeService, times(categoryNumbers)).getCurrentTime();
	}

	/**
	 * Method attributeSkuBulkUpdateProcessor#updateSkuAttributeDisplayNameInOffers should calls CatalogService#readAll by attribute code and offers
	 * codes.
	 */
	@Test
	public void testThatUpdateSkuAttributeDisplayNameInOffersShouldCallsReadAllAttributeAndReadAllOffers() {
		final List<String> offerCodes = Arrays.asList(FIRST_PRODUCT, SECOND_PRODUCT);

		attributeSkuBulkUpdateProcessor.updateSkuAttributeDisplayNameInOffers(offerCodes, ATTRIBUTE_CODE);

		verify(catalogService).readAll(ATTRIBUTE_IDENTITY_TYPE, ATTRIBUTE_CODE);
		verify(catalogService).readAll(OFFER_IDENTITY_TYPE, offerCodes);
	}

	/**
	 * Method attributeSkuBulkUpdateProcessor#updateSkuAttributeDisplayNameInOffers should calls TimeService#getCurrentTime for each updated offer.
	 */
	@Test
	public void testThatUpdateSkuAttributeDisplayNameInOffersShouldCallsGetCurrentTimeSameAsOfferNumberTimes() {
		final int offersNumber = 2;
		final List<String> offersCodes = Arrays.asList(FIRST_PRODUCT, SECOND_PRODUCT);

		final NameIdentity identity = mock(NameIdentity.class);
		when(identity.getStore()).thenReturn(STORE);

		final Attribute attribute = mock(Attribute.class);
		when((attribute.getIdentity())).thenReturn(identity);

		final NameIdentity offerIdentity = mock(NameIdentity.class);
		when(offerIdentity.getStore()).thenReturn(STORE);

		final Offer offer = mock(Offer.class);
		when(offer.getIdentity()).thenReturn(offerIdentity);
		when(offer.isDeleted()).thenReturn(false);

		when(catalogService.readAll(ATTRIBUTE_IDENTITY_TYPE, ATTRIBUTE_CODE)).thenReturn(Collections.singletonList(attribute));
		when(catalogService.readAll(OFFER_IDENTITY_TYPE, offersCodes)).thenReturn(Collections.nCopies(offersNumber, offer));

		attributeSkuBulkUpdateProcessor.updateSkuAttributeDisplayNameInOffers(offersCodes, ATTRIBUTE_CODE);

		verify(timeService, times(offersNumber)).getCurrentTime();
	}

	/**
	 * Method attributeCategoryBulkUpdateProcessor#updateCategoryAttributeDisplayNameInCategories shouldn't calls TimeService#getCurrentTime for each
	 * updated category.
	 */
	@Test
	public void testThatUpdateCategoryAttributeDisplayNameInCategoriesShouldCallsGetCurrentTimeZeroTimes() {
		final int categoryNumbers = 2;
		final List<String> categoryCodes = Arrays.asList("category1", "category2");

		final Category category = mock(Category.class);
		when(category.isDeleted()).thenReturn(true);
		when(catalogService.readAll(CATEGORY_IDENTITY_TYPE, categoryCodes)).thenReturn(Collections.nCopies(categoryNumbers, category));

		attributeCategoryBulkUpdateProcessor.updateCategoryAttributeDisplayNameInCategories(categoryCodes, ATTRIBUTE_CODE);

		verify(timeService, never()).getCurrentTime();
	}

	/**
	 * Method attributeBulkUpdateProcessor#updateSkuAttributeDisplayNameInOffers shouldn't calls TimeService#getCurrentTime for each updated offer.
	 */
	@Test
	public void testThatUpdateAttributeDisplayNameInOffersShouldCallsGetCurrentTimeZeroTimes() {
		final int offersNumber = 2;
		final List<String> offersCodes = Arrays.asList(FIRST_PRODUCT, SECOND_PRODUCT);

		final Offer offer = mock(Offer.class);
		when(offer.isDeleted()).thenReturn(true);
		when(catalogService.readAll(OFFER_IDENTITY_TYPE, offersCodes)).thenReturn(Collections.nCopies(offersNumber, offer));

		attributeBulkUpdateProcessor.updateAttributeDisplayNameInOffers(offersCodes, ATTRIBUTE_CODE);

		verify(timeService, never()).getCurrentTime();
	}

	/**
	 * Method attributeSkuBulkUpdateProcessor#updateSkuAttributeDisplayNameInOffers shouldn't calls TimeService#getCurrentTime for each updated
	 * offer.
	 */
	@Test
	public void testThatUpdateSkuAttributeDisplayNameInOffersShouldCallsGetCurrentTimeZeroTimes() {
		final int offersNumber = 2;
		final List<String> offersCodes = Arrays.asList(FIRST_PRODUCT, SECOND_PRODUCT);

		final Offer offer = mock(Offer.class);
		when(offer.isDeleted()).thenReturn(true);
		when(catalogService.readAll(OFFER_IDENTITY_TYPE, offersCodes)).thenReturn(Collections.nCopies(offersNumber, offer));

		attributeSkuBulkUpdateProcessor.updateSkuAttributeDisplayNameInOffers(offersCodes, ATTRIBUTE_CODE);

		verify(timeService, never()).getCurrentTime();
	}
}
