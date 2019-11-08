/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.option;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OPTION_IDENTITY_TYPE;
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
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.service.misc.TimeService;

/**
 * Tests {@link SkuOptionBulkUpdateProcessorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OptionBulkUpdateProcessorImplTest {
	private static final String OPTION_CODE = "optionCode";
	private static final String STORE = "store";

	@Mock
	private CatalogService catalogService;

	@Mock
	private TimeService timeService;

	@InjectMocks
	private SkuOptionBulkUpdateProcessorImpl skuOptionBulkUpdateProcessor;

	/**
	 * Set up tests.
	 */
	@Before
	public void setUp() {
		final Date now = new Date();
		when(timeService.getCurrentTime()).thenReturn(now);
	}

	/**
	 * Method skuOptionBulkUpdateProcessor#updateSkuOptionDisplayNamesInOffers should calls CatalogService#readAll by option code and offers codes.
	 */
	@Test
	public void testThatUpdateOptionDisplayNamesInOffersShouldCallsReadAllOptionAndReadAllOffers() {
		final List<String> offerCodes = Arrays.asList("product1", "product2");

		skuOptionBulkUpdateProcessor.updateSkuOptionDisplayNamesInOffers(offerCodes, OPTION_CODE);

		verify(catalogService).readAll(OPTION_IDENTITY_TYPE, OPTION_CODE);
		verify(catalogService).readAll(OFFER_IDENTITY_TYPE, offerCodes);
	}

	/**
	 * Method skuOptionBulkUpdateProcessor#updateSkuOptionDisplayNamesInOffers should calls TimeService#getCurrentTime for each updated offer.
	 */
	@Test
	public void testThatUpdateOptionDisplayNamesInOffersShouldCallsGetCurrentTimeSameAsOfferNumberTimes() {
		final int offersNumber = 2;
		final List<String> offersCodes = Arrays.asList("product1", "product2");

		final NameIdentity identity = mock(NameIdentity.class);
		when(identity.getStore()).thenReturn(STORE);

		final Option option = mock(Option.class);
		when((option.getIdentity())).thenReturn(identity);

		final NameIdentity offerIdentity = mock(NameIdentity.class);
		when(offerIdentity.getStore()).thenReturn(STORE);

		final Offer offer = mock(Offer.class);
		when(offer.getIdentity()).thenReturn(offerIdentity);

		when(catalogService.readAll(OPTION_IDENTITY_TYPE, OPTION_CODE)).thenReturn(Collections.singletonList(option));
		when(catalogService.readAll(OFFER_IDENTITY_TYPE, offersCodes)).thenReturn(Collections.nCopies(offersNumber, offer));

		skuOptionBulkUpdateProcessor.updateSkuOptionDisplayNamesInOffers(offersCodes, OPTION_CODE);

		verify(timeService, times(offersNumber)).getCurrentTime();
	}


	/**
	 * Method skuOptionBulkUpdateProcessor#updateSkuOptionDisplayNamesInOffers shouldn't calls TimeService#getCurrentTime for each updated
	 * offer.
	 */
	@Test
	public void testThatUpdateSkuOptionDisplayNameInOffersShouldCallsGetCurrentTimeZeroTimes() {
		final int offersNumber = 2;
		final List<String> offersCodes = Arrays.asList("product1", "product2");

		final Offer offer = mock(Offer.class);
		when(offer.isDeleted()).thenReturn(true);
		when(catalogService.readAll(OFFER_IDENTITY_TYPE, offersCodes)).thenReturn(Collections.nCopies(offersNumber, offer));

		skuOptionBulkUpdateProcessor.updateSkuOptionDisplayNamesInOffers(offersCodes, OPTION_CODE);

		verify(timeService, never()).getCurrentTime();
	}
}
