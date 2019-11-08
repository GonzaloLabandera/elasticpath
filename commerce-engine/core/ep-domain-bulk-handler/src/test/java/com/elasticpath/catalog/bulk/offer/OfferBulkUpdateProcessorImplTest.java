/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk.offer;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OFFER_IDENTITY_TYPE;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.NameIdentity;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.Converter;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.test.util.Utils;

/**
 * Tests {@link OfferBulkUpdateProcessorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class OfferBulkUpdateProcessorImplTest {

	private static final String OFFER = "offer";
	private static final String STORE = "store";

	@Mock
	private CatalogService catalogService;

	@Mock
	private ProductLookup productLookup;

	@Mock
	private StoreService storeService;

	@Mock
	private Converter<Product, Offer> converter;

	@InjectMocks
	private OfferBulkUpdateProcessorImpl offerBulkUpdateProcessor;

	@Test
	public void shouldCallCatalogServiceReadAllForListOfAllOfferCodes() {
		final String code1 = Utils.uniqueCode(OFFER);
		final String code2 = Utils.uniqueCode(OFFER);

		offerBulkUpdateProcessor.updateOffers(Arrays.asList(code1, code2));

		verify(catalogService).readAll(OFFER_IDENTITY_TYPE, Arrays.asList(code1, code2));
	}

	@Test
	public void shouldCallProductLookupFindByGuidForEachBundleCode() {
		final String code1 = Utils.uniqueCode(OFFER);
		final String code2 = Utils.uniqueCode(OFFER);
		final String storeCode = Utils.uniqueCode(STORE);

		final Offer offer1 = mockOffer(code1, storeCode);
		final Offer offer2 = mockOffer(code2, storeCode);

		when(catalogService.readAll(OFFER_IDENTITY_TYPE, Arrays.asList(code1, code2))).thenReturn(Arrays.asList(offer1, offer2));

		final ProductBundle productBundle1 = mock(ProductBundle.class);
		when(productLookup.findByGuid(code1)).thenReturn(productBundle1);
		final ProductBundle productBundle2 = mock(ProductBundle.class);
		when(productLookup.findByGuid(code2)).thenReturn(productBundle2);

		final Catalog catalog = mock(Catalog.class);
		final Store store = mock(Store.class);
		when(store.getCatalog()).thenReturn(catalog);
		when(storeService.findStoreWithCode(storeCode)).thenReturn(store);

		final Offer updatedOffer1 = mockOffer(code1, storeCode);
		when(updatedOffer1.isDeleted()).thenReturn(false);
		final Offer updatedOffer2 = mockOffer(code2, storeCode);
		when(updatedOffer2.isDeleted()).thenReturn(true);

		when(converter.convert(productBundle1, store, catalog)).thenReturn(updatedOffer1);
		when(converter.convert(productBundle2, store, catalog)).thenReturn(updatedOffer2);

		offerBulkUpdateProcessor.updateOffers(Arrays.asList(code1, code2));

		verify(productLookup).findByGuid(code1);
		verify(productLookup).findByGuid(code2);
	}

	@Test
	public void shouldCallStoreServiceFindStoreWithCodeWithBundleStoreCode() {
		final String code = Utils.uniqueCode(OFFER);
		final String storeCode = Utils.uniqueCode(STORE);
		final Offer offer = mockOffer(code, storeCode);

		when(catalogService.readAll(OFFER_IDENTITY_TYPE, Collections.singletonList(code))).thenReturn(Collections.singletonList(offer));
		final ProductBundle productBundle = mock(ProductBundle.class);
		when(productLookup.findByGuid(code)).thenReturn(productBundle);

		final Catalog catalog = mock(Catalog.class);
		final Store store = mock(Store.class);
		when(store.getCatalog()).thenReturn(catalog);
		when(storeService.findStoreWithCode(storeCode)).thenReturn(store);

		final Offer updatedOffer = mockOffer(code, storeCode);
		when(updatedOffer.isDeleted()).thenReturn(false);
		when(converter.convert(productBundle, store, catalog)).thenReturn(updatedOffer);

		offerBulkUpdateProcessor.updateOffers(Collections.singletonList(code));

		verify(storeService).findStoreWithCode(storeCode);
	}

	@Test
	public void shouldCallConverterConvertWithProductBundleAndStoreAndCatalog() {
		final String code = Utils.uniqueCode(OFFER);
		final String storeCode = Utils.uniqueCode(STORE);
		final Offer offer = mockOffer(code, storeCode);

		when(catalogService.readAll(OFFER_IDENTITY_TYPE, Collections.singletonList(code))).thenReturn(Collections.singletonList(offer));
		final ProductBundle productBundle = mock(ProductBundle.class);
		when(productLookup.findByGuid(code)).thenReturn(productBundle);

		final Catalog catalog = mock(Catalog.class);
		final Store store = mock(Store.class);
		when(store.getCatalog()).thenReturn(catalog);
		when(storeService.findStoreWithCode(storeCode)).thenReturn(store);

		final Offer updatedOffer = mockOffer(code, storeCode);
		when(updatedOffer.isDeleted()).thenReturn(false);
		when(converter.convert(productBundle, store, catalog)).thenReturn(updatedOffer);

		offerBulkUpdateProcessor.updateOffers(Collections.singletonList(code));

		verify(converter).convert(productBundle, store, catalog);
	}

	@Test
	public void shouldCallSaveOrUpdateAllForUpdatedOffersWhenIsDeletedFalse() {
		final String code = Utils.uniqueCode(OFFER);
		final String storeCode = Utils.uniqueCode(STORE);

		final Offer offer = mockOffer(code, storeCode);
		when(catalogService.readAll(OFFER_IDENTITY_TYPE, Collections.singletonList(code))).thenReturn(Collections.singletonList(offer));

		final ProductBundle productBundle = mock(ProductBundle.class);
		when(productLookup.findByGuid(code)).thenReturn(productBundle);

		final Catalog catalog = mock(Catalog.class);
		final Store store = mock(Store.class);
		when(store.getCatalog()).thenReturn(catalog);
		when(storeService.findStoreWithCode(storeCode)).thenReturn(store);

		final Offer updatedOffer = mockOffer(code, storeCode);
		when(updatedOffer.isDeleted()).thenReturn(false);

		when(converter.convert(productBundle, store, catalog)).thenReturn(updatedOffer);

		offerBulkUpdateProcessor.updateOffers(Collections.singletonList(code));

		verify(catalogService).saveOrUpdateAll(Collections.singletonList(updatedOffer));
		verify(catalogService).deleteAll(argThat(List::isEmpty));
	}

	@Test
	public void shouldCallDeleteAllForUpdatedOffersWhenIsDeletedTrue() {
		final String code = Utils.uniqueCode(OFFER);
		final String storeCode = Utils.uniqueCode(STORE);

		final Offer offer = mockOffer(code, storeCode);
		when(offer.isDeleted()).thenReturn(false);

		when(catalogService.readAll(OFFER_IDENTITY_TYPE, Collections.singletonList(code))).thenReturn(Collections.singletonList(offer));

		final ProductBundle productBundle = mock(ProductBundle.class);
		when(productLookup.findByGuid(code)).thenReturn(productBundle);

		final Catalog catalog = mock(Catalog.class);
		final Store store = mock(Store.class);
		when(store.getCatalog()).thenReturn(catalog);
		when(storeService.findStoreWithCode(storeCode)).thenReturn(store);

		final Offer updatedOffer = mockOffer(code, storeCode);
		when(updatedOffer.isDeleted()).thenReturn(true);

		when(converter.convert(productBundle, store, catalog)).thenReturn(updatedOffer);

		offerBulkUpdateProcessor.updateOffers(Collections.singletonList(code));

		verify(catalogService).deleteAll(Collections.singletonList(updatedOffer));
		verify(catalogService).saveOrUpdateAll(argThat(List::isEmpty));
	}

	@Test
	public void shouldNotCallDeleteAllForUpdatedOffersWhenIsDeletedTrueAndOfferIsDeletedTrue() {
		final String code = Utils.uniqueCode(OFFER);
		final String storeCode = Utils.uniqueCode(STORE);

		final Offer offer = mockOffer(code, storeCode);
		when(offer.isDeleted()).thenReturn(true);

		when(catalogService.readAll(OFFER_IDENTITY_TYPE, Collections.singletonList(code))).thenReturn(Collections.singletonList(offer));

		final ProductBundle productBundle = mock(ProductBundle.class);
		when(productLookup.findByGuid(code)).thenReturn(productBundle);

		final Catalog catalog = mock(Catalog.class);
		final Store store = mock(Store.class);
		when(store.getCatalog()).thenReturn(catalog);
		when(storeService.findStoreWithCode(storeCode)).thenReturn(store);

		final Offer updatedOffer = mockOffer(code, storeCode);
		when(updatedOffer.isDeleted()).thenReturn(true);

		when(converter.convert(productBundle, store, catalog)).thenReturn(updatedOffer);

		offerBulkUpdateProcessor.updateOffers(Collections.singletonList(code));

		verify(catalogService).saveOrUpdateAll(argThat(List::isEmpty));
		verify(catalogService).deleteAll(argThat(List::isEmpty));
	}

	@Test
	public void shouldCallSaveOrUpdateAllWithEmptyListWhenCatalogServiceReturnsEmptyOffersList() {
		final String code1 = Utils.uniqueCode(OFFER);
		final String code2 = Utils.uniqueCode(OFFER);

		when(catalogService.readAll(OFFER_IDENTITY_TYPE, Arrays.asList(code1, code2))).thenReturn(Collections.emptyList());

		offerBulkUpdateProcessor.updateOffers(Arrays.asList(code1, code2));

		verify(catalogService).saveOrUpdateAll(argThat(List::isEmpty));
	}

	private Offer mockOffer(final String code, final String store) {
		final NameIdentity nameIdentity = mock(NameIdentity.class);
		when(nameIdentity.getCode()).thenReturn(code);
		when(nameIdentity.getStore()).thenReturn(store);

		final Offer offer = mock(Offer.class);
		when(offer.getIdentity()).thenReturn(nameIdentity);

		return offer;
	}
}
