/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.brand;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.BRAND_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;
import static org.mockito.Mockito.mock;
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
import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.service.misc.TimeService;

/**
 * Tests {@link BrandBulkUpdateProcessorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BrandBulkUpdateProcessorImplTest {

	private static final String BRAND_CODE = "brandCode";
	private static final String STORE = "store";

	@Mock
	private CatalogService catalogService;

	@Mock
	private TimeService timeService;

	@InjectMocks
	private BrandBulkUpdateProcessorImpl brandBulkUpdateProcessor;

	/**
	 * Set up tests.
	 */
	@Before
	public void setUp() {
		final Date now = new Date();
		when(timeService.getCurrentTime()).thenReturn(now);
	}

	/**
	 * Method BrandBulkUpdateProcessor#updateBrandDisplayNamesInOffers should calls CatalogService#readAll by brand code and offers codes.
	 */
	@Test
	public void testThatUpdateBrandDisplayNamesInOffersShouldCallsReadAllBrandAndReadAllOffers() {
		final List<String> offerCodes = Arrays.asList("product1", "product2");

		brandBulkUpdateProcessor.updateBrandDisplayNamesInOffers(offerCodes, BRAND_CODE);

		verify(catalogService).readAll(BRAND_IDENTITY_TYPE, BRAND_CODE);
		verify(catalogService).readAll(OFFER_IDENTITY_TYPE, offerCodes);
	}

	/**
	 * Method BrandBulkUpdateProcessor#updateBrandDisplayNamesInOffers should calls TimeService#getCurrentTime for each updated offer.
	 */
	@Test
	public void testThatUpdateBrandDisplayNamesInOffersShouldCallsGetCurrentTimeSameAsOfferNumberTimes() {
		final int offersNumber = 2;
		final List<String> offersCodes = Arrays.asList("product1", "product2");

		final NameIdentity brandIdentity = mock(NameIdentity.class);
		when(brandIdentity.getStore()).thenReturn(STORE);

		final Brand brand = mock(Brand.class);
		when((brand.getIdentity())).thenReturn(brandIdentity);

		final NameIdentity offerIdentity = mock(NameIdentity.class);
		when(offerIdentity.getStore()).thenReturn(STORE);

		final Offer offer = mock(Offer.class);
		when(offer.getIdentity()).thenReturn(offerIdentity);

		when(catalogService.readAll(BRAND_IDENTITY_TYPE, BRAND_CODE)).thenReturn(Collections.singletonList(brand));
		when(catalogService.readAll(OFFER_IDENTITY_TYPE, offersCodes)).thenReturn(Collections.nCopies(offersNumber, offer));

		brandBulkUpdateProcessor.updateBrandDisplayNamesInOffers(offersCodes, BRAND_CODE);

		verify(timeService, times(offersNumber)).getCurrentTime();
	}

}