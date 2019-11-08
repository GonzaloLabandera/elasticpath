/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.bulk.DomainBulkEventType.OFFER_BULK_UPDATE;
import static com.elasticpath.catalog.update.processor.connectivity.impl.ProductUpdateProcessorImpl.PRODUCTS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.spi.CatalogProjectionPlugin;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.OfferWriterRepository;
import com.elasticpath.catalog.update.processor.capabilities.ProductUpdateProcessor;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;

/**
 * Tests {@link ProductUpdateProcessorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductUpdateProcessorImplTest {

	private static final String PRODUCT = "product";
	private static final String BUNDLE_1 = "bundle1";
	private static final String BUNDLE_2 = "bundle2";
	private static final String BUNDLE_3 = "bundle3";

	@Mock
	private ProjectionService<Product, Offer> projectionService;

	@Mock
	private CatalogProjectionPluginProvider catalogProjectionPluginProvider;

	@Mock
	private CatalogProjectionPlugin catalogProjectionPlugin;

	@Mock
	private OfferWriterRepository offerWriterRepository;

	@Mock
	private EventMessagePublisher eventMessagePublisher;

	@Mock
	private EventMessageFactory eventMessageFactory;

	@Mock
	private Product product;

	private ProductUpdateProcessor productUpdateProcessor;

	@Before
	public void setUp() {
		final int bulkChangeMaxEventSize = 2;

		when(catalogProjectionPlugin.getWriterCapability(OfferWriterRepository.class)).thenReturn(Optional.of(offerWriterRepository));
		when(catalogProjectionPluginProvider.getCatalogProjectionPlugin()).thenReturn(catalogProjectionPlugin);

		productUpdateProcessor = new ProductUpdateProcessorImpl(projectionService, catalogProjectionPluginProvider, eventMessagePublisher,
				eventMessageFactory, bulkChangeMaxEventSize);
	}

	@Test
	public void shouldCallBuildProjectionsForEachProductCatalogWhenProcessProductCreatedAndProductExistsInTwoCatalogs() {
		final Catalog catalog1 = mock(Catalog.class);
		final Catalog catalog2 = mock(Catalog.class);

		when(product.getCatalogs()).thenReturn(new HashSet<>(Arrays.asList(catalog1, catalog2)));
		when(projectionService.buildProjections(product, catalog1)).thenReturn(Collections.emptyList());
		when(projectionService.buildProjections(product, catalog2)).thenReturn(Collections.emptyList());

		productUpdateProcessor.processProductCreated(product);

		verify(projectionService).buildProjections(product, catalog1);
		verify(projectionService).buildProjections(product, catalog2);
	}

	@Test
	public void shouldCallWriteForEachOfferWhenProcessProductCreatedAndTwoOffersAreCreatedByProjectionService() {
		final Catalog catalog = mock(Catalog.class);
		when(product.getCatalogs()).thenReturn(Collections.singleton(catalog));

		final Offer offer1 = mock(Offer.class);
		final Offer offer2 = mock(Offer.class);
		when(projectionService.buildProjections(product, catalog)).thenReturn(Arrays.asList(offer1, offer2));

		productUpdateProcessor.processProductCreated(product);

		verify(offerWriterRepository).write(offer1);
		verify(offerWriterRepository).write(offer2);
	}

	@Test
	public void shouldCallBuildProjectionsForEachProductCatalogWhenProcessProductUpdatedAndProductExistsInTwoCatalogs() {
		final Catalog catalog1 = mock(Catalog.class);
		final Catalog catalog2 = mock(Catalog.class);

		when(product.getCatalogs()).thenReturn(new HashSet<>(Arrays.asList(catalog1, catalog2)));
		when(projectionService.buildProjections(product, catalog1)).thenReturn(Collections.emptyList());
		when(projectionService.buildProjections(product, catalog2)).thenReturn(Collections.emptyList());

		productUpdateProcessor.processProductUpdated(product);

		verify(projectionService).buildProjections(product, catalog1);
		verify(projectionService).buildProjections(product, catalog2);
	}

	@Test
	public void shouldCallWriteForEachOfferWhenProcessProductUpdatedAndTwoOffersAreCreatedByProjectionService() {
		final Catalog catalog = mock(Catalog.class);
		when(product.getCatalogs()).thenReturn(Collections.singleton(catalog));

		final Offer offer1 = mock(Offer.class);
		final Offer offer2 = mock(Offer.class);
		when(projectionService.buildProjections(product, catalog)).thenReturn(Arrays.asList(offer1, offer2));

		productUpdateProcessor.processProductUpdated(product);

		verify(offerWriterRepository).write(offer1);
		verify(offerWriterRepository).write(offer2);
	}

	@Test
	public void shouldCallCreateEventMessageWithProductBundleCodesAreSeparatedDependOfBulkChangeMaxEventSizeAndSortedByAscendingOrder() {
		final Catalog catalog = mock(Catalog.class);
		when(product.getCode()).thenReturn(PRODUCT);
		when(product.getCatalogs()).thenReturn(Collections.singleton(catalog));

		final Offer offer = mock(Offer.class);
		when(projectionService.buildProjections(product, catalog)).thenReturn(Collections.singletonList(offer));

		when(offerWriterRepository.write(any(Offer.class))).thenReturn(true);

		productUpdateProcessor.processProductUpdated(product, BUNDLE_3, BUNDLE_1, BUNDLE_2);

		verify(eventMessageFactory).createEventMessage(OFFER_BULK_UPDATE, PRODUCT, Collections.singletonMap(PRODUCTS, Arrays.asList(BUNDLE_1,
				BUNDLE_2)));
		verify(eventMessageFactory).createEventMessage(OFFER_BULK_UPDATE, PRODUCT, Collections.singletonMap(PRODUCTS,
				Collections.singletonList(BUNDLE_3)));
	}

	@Test
	public void shouldNotCallCreateEventMessageOfferWriterRepositoryWriteReturnsFalse() {
		final Catalog catalog = mock(Catalog.class);
		when(product.getCatalogs()).thenReturn(Collections.singleton(catalog));

		final Offer offer = mock(Offer.class);
		when(projectionService.buildProjections(product, catalog)).thenReturn(Collections.singletonList(offer));

		when(offerWriterRepository.write(any(Offer.class))).thenReturn(false);

		productUpdateProcessor.processProductUpdated(product, BUNDLE_1, BUNDLE_2, BUNDLE_3);

		verify(eventMessageFactory, never()).createEventMessage(any(), any(), any());
	}

	@Test
	public void shouldCallPublishEventMessageWithProductBundleCodesAreSeparatedDependOfBulkChangeMaxEventSize() {
		final Catalog catalog = mock(Catalog.class);
		when(product.getCode()).thenReturn(PRODUCT);
		when(product.getCatalogs()).thenReturn(Collections.singleton(catalog));

		final Offer offer = mock(Offer.class);
		when(projectionService.buildProjections(product, catalog)).thenReturn(Collections.singletonList(offer));

		when(offerWriterRepository.write(any(Offer.class))).thenReturn(true);

		final EventMessage eventMessage1 = mock(EventMessage.class);
		final EventMessage eventMessage2 = mock(EventMessage.class);

		when(eventMessageFactory.createEventMessage(OFFER_BULK_UPDATE, PRODUCT, Collections.singletonMap(PRODUCTS, Arrays.asList(BUNDLE_1,
				BUNDLE_2)))).thenReturn(eventMessage1);

		when(eventMessageFactory.createEventMessage(OFFER_BULK_UPDATE, PRODUCT, Collections.singletonMap(PRODUCTS,
				Collections.singletonList(BUNDLE_3)))).thenReturn(eventMessage2);

		productUpdateProcessor.processProductUpdated(product, BUNDLE_1, BUNDLE_2, BUNDLE_3);

		verify(eventMessagePublisher).publish(eventMessage1);
		verify(eventMessagePublisher).publish(eventMessage2);
	}

	@Test
	public void shouldNotCallPublishEventMessageWhenOfferWriterRepositoryWriteReturnsFalse() {
		final Catalog catalog = mock(Catalog.class);
		when(product.getCatalogs()).thenReturn(Collections.singleton(catalog));

		final Offer offer = mock(Offer.class);
		when(projectionService.buildProjections(product, catalog)).thenReturn(Collections.singletonList(offer));

		when(offerWriterRepository.write(any(Offer.class))).thenReturn(false);

		productUpdateProcessor.processProductUpdated(product, BUNDLE_1, BUNDLE_2, BUNDLE_3);

		verify(eventMessagePublisher, never()).publish(any());
	}

	@Test
	public void shouldCallRepositoryDeleteWithProductGuidWhenProcessProductDeletedCalling() {
		productUpdateProcessor.processProductDeleted(PRODUCT);

		verify(offerWriterRepository).delete(PRODUCT);
	}

}
