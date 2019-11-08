/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.bulk.DomainBulkEventType.ATTRIBUTE_BULK_UPDATE;
import static com.elasticpath.catalog.bulk.DomainBulkEventType.ATTRIBUTE_CATEGORY_BULK_UPDATE;
import static com.elasticpath.catalog.bulk.DomainBulkEventType.ATTRIBUTE_SKU_BULK_UPDATE;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.spi.CatalogProjectionPlugin;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.AttributeWriterRepository;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.dao.ProductDao;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.catalog.ProductSkuService;

/**
 * Tests {@link AttributeUpdateProcessorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributeUpdateProcessorImplTest {

	public static final long UID_FIRST = 1L;
	public static final long UID_SECOND = 2L;
	public static final long UID_THIRD = 3L;
	@Mock
	private final Attribute attribute = mock(Attribute.class);
	@Mock
	private ProjectionService<Attribute, com.elasticpath.catalog.entity.attribute.Attribute> projectionService;
	@Mock
	private CatalogProjectionPluginProvider provider;
	@Mock
	private CatalogProjectionPlugin plugin;
	@Mock
	private AttributeWriterRepository repository;
	@Mock
	private ProductDao productDao;
	@Mock
	private CategoryService categoryService;
	@Mock
	private ProductSkuService productSkuService;
	@Mock
	private EventMessageFactory eventMessageFactory;
	@Mock
	private EventMessagePublisher eventMessagePublisher;

	private AttributeUpdateProcessorImpl attributeUpdateProcessor;

	private static final int BULK_CHANGE_EVENT_SIZE = 2;
	private static final String FIRST = "1";
	private static final String SECOND = "2";
	private static final String THIRD = "3";

	/**
	 * Setup for the database.
	 */
	@Before
	public void setUp() {
		when(attribute.getGuid()).thenReturn("guid");
		when(projectionService.buildAllStoresProjections(attribute)).thenReturn(Arrays.asList(
				mock(com.elasticpath.catalog.entity.attribute.Attribute.class),
				mock(com.elasticpath.catalog.entity.attribute.Attribute.class)));
		when(provider.getCatalogProjectionPlugin()).thenReturn(plugin);
		when(plugin.getWriterCapability(AttributeWriterRepository.class)).thenReturn(Optional.of(repository));

		attributeUpdateProcessor = new AttributeUpdateProcessorImpl(projectionService, provider, eventMessagePublisher, eventMessageFactory,
				productDao, categoryService, productSkuService, BULK_CHANGE_EVENT_SIZE);
	}

	/**
	 * Processing of ATTRIBUTE_CREATED event.
	 */
	@Test
	public void processAttributeCreatedTest() {
		attributeUpdateProcessor.processAttributeCreated(attribute);

		verify(projectionService).buildAllStoresProjections(attribute);
		verify(provider, times(1)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(AttributeWriterRepository.class);
		verify(repository, times(2)).write(any());
	}

	/**
	 * Processing of ATTRIBUTE_UPDATED event.
	 */
	@Test
	public void processAttributeUpdatedTest() {
		attributeUpdateProcessor.processAttributeUpdated(attribute);

		verify(projectionService).buildAllStoresProjections(attribute);
		verify(provider, times(1)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(AttributeWriterRepository.class);
		verify(repository, times(2)).write(any());
	}

	/**
	 * Processing of ATTRIBUTE_DELETED event.
	 */
	@Test
	public void processAttributeDeletedTest() {
		attributeUpdateProcessor.processAttributeDeleted(attribute.getGuid());

		verify(provider).getCatalogProjectionPlugin();
		verify(plugin).getWriterCapability(AttributeWriterRepository.class);
		verify(repository).delete(anyString());
		verify(attribute).getGuid();
	}

	@Test
	public void processProductAttributeUpdatedShouldCallPublishEventMessageWithProductCodesInAscendingProductCodeOrder() {
		Map<Long, String> productCodes = new HashMap<>();
		productCodes.put(UID_THIRD, THIRD);
		productCodes.put(UID_SECOND, SECOND);
		productCodes.put(UID_FIRST, FIRST);

		when(productDao.findUidsByAttribute(attribute)).thenReturn(Collections.singletonList(1L));
		when(productDao.findCodesByUids(Collections.singletonList(1L))).thenReturn(productCodes);
		when(repository.write(any(com.elasticpath.catalog.entity.attribute.Attribute.class))).thenReturn(true);

		final EventMessage secondMessage = mock(EventMessage.class);
		final EventMessage firstMessage = mock(EventMessage.class);

		when(eventMessageFactory.createEventMessage(ATTRIBUTE_BULK_UPDATE, attribute.getGuid(), eventDataWithProductsCodes(FIRST, SECOND)))
				.thenReturn(firstMessage);
		when(eventMessageFactory.createEventMessage(ATTRIBUTE_BULK_UPDATE, attribute.getGuid(), eventDataWithProductsCodes(THIRD)))
				.thenReturn(secondMessage);

		attributeUpdateProcessor.processAttributeUpdated(attribute);

		final InOrder inOrder = inOrder(eventMessagePublisher);
		inOrder.verify(eventMessagePublisher).publish(firstMessage);
		inOrder.verify(eventMessagePublisher).publish(secondMessage);
	}

	@Test
	public void processCategoryAttributeUpdatedShouldCallPublishEventMessageWithCategoryCodesInAscendingCategoryCodeOrder() {
		when(categoryService.findUidsByAttribute(attribute)).thenReturn(Collections.singletonList(1L));
		when(categoryService.findCodesByUids(Collections.singletonList(1L))).thenReturn(Arrays.asList(THIRD, SECOND, FIRST));
		when(repository.write(any(com.elasticpath.catalog.entity.attribute.Attribute.class))).thenReturn(true);

		final EventMessage secondMessage = mock(EventMessage.class);
		final EventMessage firstMessage = mock(EventMessage.class);

		when(eventMessageFactory.createEventMessage(ATTRIBUTE_CATEGORY_BULK_UPDATE, attribute.getGuid(), eventDataWithProductsCodes(FIRST, SECOND)))
				.thenReturn(firstMessage);
		when(eventMessageFactory.createEventMessage(ATTRIBUTE_CATEGORY_BULK_UPDATE, attribute.getGuid(), eventDataWithProductsCodes(THIRD)))
				.thenReturn(secondMessage);

		attributeUpdateProcessor.processAttributeUpdated(attribute);

		final InOrder inOrder = inOrder(eventMessagePublisher);
		inOrder.verify(eventMessagePublisher).publish(firstMessage);
		inOrder.verify(eventMessagePublisher).publish(secondMessage);
	}

	@Test
	public void processSkuAttributeUpdatedShouldCallPublishEventMessageWithSkuCodesInAscendingSkuCodeOrder() {
		when(productSkuService.findCodesByAttribute(attribute)).thenReturn(Arrays.asList(THIRD, SECOND, FIRST));
		when(repository.write(any(com.elasticpath.catalog.entity.attribute.Attribute.class))).thenReturn(true);

		final EventMessage secondMessage = mock(EventMessage.class);
		final EventMessage firstMessage = mock(EventMessage.class);

		when(eventMessageFactory.createEventMessage(ATTRIBUTE_SKU_BULK_UPDATE, attribute.getGuid(), eventDataWithProductsCodes(FIRST, SECOND)))
				.thenReturn(firstMessage);
		when(eventMessageFactory.createEventMessage(ATTRIBUTE_SKU_BULK_UPDATE, attribute.getGuid(), eventDataWithProductsCodes(THIRD)))
				.thenReturn(secondMessage);

		attributeUpdateProcessor.processAttributeUpdated(attribute);

		final InOrder inOrder = inOrder(eventMessagePublisher);
		inOrder.verify(eventMessagePublisher).publish(firstMessage);
		inOrder.verify(eventMessagePublisher).publish(secondMessage);
	}

	private Map<String, Object> eventDataWithProductsCodes(final String... codes) {
		return Collections.singletonMap(PRODUCTS, Arrays.asList(codes));
	}

}