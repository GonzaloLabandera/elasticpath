/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.bulk.DomainBulkEventType.BRAND_BULK_UPDATE;
import static com.elasticpath.catalog.update.processor.connectivity.impl.BrandUpdateProcessorImpl.PRODUCTS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.spi.CatalogProjectionPlugin;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.BrandWriterRepository;
import com.elasticpath.catalog.update.processor.capabilities.BrandUpdateProcessor;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.service.catalog.impl.ProductQueryService;
import com.elasticpath.service.query.CriteriaBuilder;
import com.elasticpath.service.query.QueryResult;
import com.elasticpath.service.query.ResultType;
import com.elasticpath.service.query.relations.BrandRelation;

/**
 * Tests {@link BrandUpdateProcessorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BrandUpdateProcessorImplTest {

	private static final String PRODUCT_CODE = "productCode";
	private static final String BRAND_CODE = "brandCode";
	private static final String BRAND_GUID = "guid";
	private static final String FIRST = "1";
	private static final String SECOND = "2";
	private static final String THIRD = "3";
	private static final int BULK_CHANGE_EVENT_SIZE = 2;

	@Mock
	private final com.elasticpath.domain.catalog.Brand brand = mock(com.elasticpath.domain.catalog.Brand.class);
	@Mock
	private ProjectionService<com.elasticpath.domain.catalog.Brand, Brand> projectionService;
	@Mock
	private CatalogProjectionPluginProvider provider;
	@Mock
	private CatalogProjectionPlugin plugin;
	@Mock
	private BrandWriterRepository repository;
	@Mock
	private EventMessagePublisher eventMessagePublisher;
	@Mock
	private ProductQueryService productQueryService;
	@Mock
	private EventMessageFactory eventMessageFactory;
	@Mock
	private EventMessage eventMessage;

	private BrandUpdateProcessor brandUpdateProcessor;

	/**
	 * Setup for the database.
	 */
	@Before
	public void setUp() {
		when(brand.getGuid()).thenReturn(BRAND_GUID);
		when(brand.getCode()).thenReturn(BRAND_CODE);
		when(projectionService.buildAllStoresProjections(brand)).thenReturn(Arrays.asList(mock(Brand.class), mock(Brand.class)));
		when(provider.getCatalogProjectionPlugin()).thenReturn(plugin);
		when(plugin.getWriterCapability(BrandWriterRepository.class)).thenReturn(Optional.of(repository));
		when(repository.write(any())).thenReturn(true);

		final QueryResult<Product> productQueryResult = mockQueryResult(Collections.emptyList());
		when(productQueryService.<Product>query(any())).thenReturn(productQueryResult);

		brandUpdateProcessor = new BrandUpdateProcessorImpl(projectionService, provider, eventMessagePublisher, productQueryService,
				eventMessageFactory, BULK_CHANGE_EVENT_SIZE);
	}

	/**
	 * Processing of MODIFIER_GROUP_CREATED event.
	 */
	@Test
	public void processModifierGroupCreatedTest() {
		brandUpdateProcessor.processBrandCreated(brand);

		verify(projectionService).buildAllStoresProjections(brand);
		verify(provider, times(1)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(BrandWriterRepository.class);
		verify(repository, times(2)).write(any());
	}

	/**
	 * Processing of BRAND_UPDATED event.
	 */
	@Test
	public void processBrandUpdatedTest() {
		brandUpdateProcessor.processBrandUpdated(brand);

		verify(projectionService).buildAllStoresProjections(brand);
		verify(provider, times(1)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(BrandWriterRepository.class);
		verify(repository, times(2)).write(any());
	}

	/**
	 * Processing of MODIFIER_GROUP_DELETED event.
	 */
	@Test
	public void processModifierGroupDeletedTest() {
		brandUpdateProcessor.processBrandDeleted(brand.getGuid());

		verify(provider).getCatalogProjectionPlugin();
		verify(plugin).getWriterCapability(BrandWriterRepository.class);
		verify(repository).delete(anyString());
		verify(brand).getGuid();
	}

	@Test
	public void processBrandUpdatedShouldCallQueryWithBrandCode() {
		brandUpdateProcessor.processBrandUpdated(brand);

		verify(productQueryService).query(CriteriaBuilder.criteriaFor(Product.class)
				.with(BrandRelation.having().codes(BRAND_CODE))
				.returning(ResultType.ENTITY));
	}

	@Test
	public void processBrandUpdatedShouldCallCreateEventMessageWithBrandBulkUpdateEventBrandCodeAndProductCode() {
		final Product product = mockProduct(PRODUCT_CODE);

		final QueryResult<Product> queryResult = mockQueryResult(Collections.singletonList(product));
		when(productQueryService.<Product>query(any())).thenReturn(queryResult);

		when(eventMessageFactory.createEventMessage(BRAND_BULK_UPDATE, BRAND_CODE, eventDataWithProductsCodes(PRODUCT_CODE)))
				.thenReturn(eventMessage);

		brandUpdateProcessor.processBrandUpdated(brand);

		verify(eventMessageFactory).createEventMessage(BRAND_BULK_UPDATE, BRAND_CODE, Collections.singletonMap(PRODUCTS,
				Collections.singletonList(PRODUCT_CODE)));
	}

	@Test
	public void processBrandUpdatedShouldCallPublishEventMessage3TimesWhenProductsCountIs5AndBulkChangeEventSizeIs2() {
		final int productsCount = 5;
		final int expectedMessagesCount = 3;

		final Product product = mockProduct(PRODUCT_CODE);

		final QueryResult<Product> queryResult = mockQueryResult(Collections.nCopies(productsCount, product));
		when(productQueryService.<Product>query(any())).thenReturn(queryResult);

		when(eventMessageFactory.createEventMessage(BRAND_BULK_UPDATE, BRAND_CODE, eventDataWithProductsCodes(PRODUCT_CODE)))
				.thenReturn(eventMessage);
		when(eventMessageFactory.createEventMessage(BRAND_BULK_UPDATE, BRAND_CODE, eventDataWithProductsCodes(PRODUCT_CODE, PRODUCT_CODE)))
				.thenReturn(eventMessage);

		brandUpdateProcessor.processBrandUpdated(brand);

		verify(eventMessagePublisher, times(expectedMessagesCount)).publish(any(EventMessage.class));
	}

	@Test
	public void processBrandUpdatedShouldCallPublishEventMessageWithProductCodesInAscendingProductCodeOrder() {
		final Product product1 = mockProduct(FIRST);
		final Product product2 = mockProduct(SECOND);
		final Product product3 = mockProduct(THIRD);

		final QueryResult<Product> queryResult = mockQueryResult(Arrays.asList(product3, product2, product1));
		when(productQueryService.<Product>query(any())).thenReturn(queryResult);

		final EventMessage secondMessage = mock(EventMessage.class);
		final EventMessage firstMessage = mock(EventMessage.class);

		when(eventMessageFactory.createEventMessage(BRAND_BULK_UPDATE, BRAND_CODE, eventDataWithProductsCodes(FIRST, SECOND)))
				.thenReturn(firstMessage);
		when(eventMessageFactory.createEventMessage(BRAND_BULK_UPDATE, BRAND_CODE, eventDataWithProductsCodes(THIRD)))
				.thenReturn(secondMessage);

		brandUpdateProcessor.processBrandUpdated(brand);

		final InOrder inOrder = inOrder(eventMessagePublisher);
		inOrder.verify(eventMessagePublisher).publish(firstMessage);
		inOrder.verify(eventMessagePublisher).publish(secondMessage);
	}

	private Product mockProduct(final String code) {
		final Product product = mock(Product.class);
		when(product.getCode()).thenReturn(code);

		return product;
	}

	@SuppressWarnings("unchecked")
	private QueryResult<Product> mockQueryResult(final List<Product> products) {
		final QueryResult<Product> queryResult = (QueryResult<Product>) mock(QueryResult.class);
		when(queryResult.getResults()).thenReturn(products);

		return queryResult;
	}

	private Map<String, Object> eventDataWithProductsCodes(final String... codes) {
		return Collections.singletonMap(PRODUCTS, Arrays.asList(codes));
	}

}
