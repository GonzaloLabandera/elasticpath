/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static com.elasticpath.catalog.bulk.DomainBulkEventType.OPTION_BULK_UPDATE;
import static com.elasticpath.catalog.update.processor.connectivity.impl.SkuOptionUpdateProcessorImpl.PRODUCTS;
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

import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.spi.CatalogProjectionPlugin;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.OptionWriterRepository;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.dao.ProductDao;
/**
 * Tests {@link SkuOptionUpdateProcessorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SkuOptionUpdateProcessorImplTest {

	private static final long UID_FIRST = 1L;
	private static final long UID_SECOND = 2L;
	private static final long UID_THIRD = 3L;
	private static final String FIRST = "1";
	private static final String SECOND = "2";
	private static final String THIRD = "3";
	@Mock
	private final SkuOption skuOption = mock(SkuOption.class);
	@Mock
	private ProjectionService<SkuOption, Option> projectionService;
	@Mock
	private CatalogProjectionPluginProvider provider;
	@Mock
	private CatalogProjectionPlugin plugin;
	@Mock
	private OptionWriterRepository repository;
	@Mock
	private ProductDao productDao;
	@Mock
	private EventMessageFactory eventMessageFactory;
	@Mock
	private EventMessagePublisher eventMessagePublisher;

	private SkuOptionUpdateProcessorImpl skuOptionUpdateProcessor;

	private static final int BULK_CHANGE_EVENT_SIZE = 2;

	/**
	 * Setup for the database.
	 */
	@Before
	public void setUp() {
		when(skuOption.getGuid()).thenReturn("guid");
		when(projectionService.buildAllStoresProjections(skuOption)).thenReturn(Arrays.asList(mock(Option.class), mock(Option.class)));
		when(provider.getCatalogProjectionPlugin()).thenReturn(plugin);
		when(plugin.getWriterCapability(OptionWriterRepository.class)).thenReturn(Optional.of(repository));

		skuOptionUpdateProcessor = new SkuOptionUpdateProcessorImpl(projectionService, provider,
				eventMessagePublisher, eventMessageFactory, productDao, BULK_CHANGE_EVENT_SIZE);
	}

	/**
	 * Processing of SKU_OPTION_CREATED event.
	 */
	@Test
	public void processSkuOptionCreatedTest() {
		skuOptionUpdateProcessor.processSkuOptionCreated(skuOption);

		verify(projectionService).buildAllStoresProjections(skuOption);
		verify(provider, times(1)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(OptionWriterRepository.class);
		verify(repository, times(2)).write(any());
	}

	/**
	 * Processing of SKU_OPTION_UPDATED event.
	 */
	@Test
	public void processSkuOptionUpdatedTest() {
		skuOptionUpdateProcessor.processSkuOptionUpdated(skuOption);

		verify(projectionService).buildAllStoresProjections(skuOption);
		verify(provider, times(1)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(OptionWriterRepository.class);
		verify(repository, times(2)).write(any());
	}

	/**
	 * Processing of SKU_OPTION_DELETED event.
	 */
	@Test
	public void processSkuOptionDeletedTest() {
		skuOptionUpdateProcessor.processSkuOptionDeleted(skuOption.getGuid());

		verify(provider).getCatalogProjectionPlugin();
		verify(plugin).getWriterCapability(OptionWriterRepository.class);
		verify(repository).delete(anyString());
		verify(skuOption).getGuid();
	}

	@Test
	public void processSkuOptionUpdatedShouldCallPublishEventMessageWithProductCodesInAscendingProductCodeOrder() {
		Map<Long, String> productCodes = new HashMap<>();
		productCodes.put(UID_THIRD, THIRD);
		productCodes.put(UID_SECOND, SECOND);
		productCodes.put(UID_FIRST, FIRST);

		when(productDao.findUidsBySkuOption(skuOption)).thenReturn(Collections.singletonList(1L));
		when(productDao.findCodesByUids(Collections.singletonList(1L))).thenReturn(productCodes);
		when(repository.write(any(Option.class))).thenReturn(true);

		final EventMessage secondMessage = mock(EventMessage.class);
		final EventMessage firstMessage = mock(EventMessage.class);

		when(eventMessageFactory.createEventMessage(OPTION_BULK_UPDATE, skuOption.getGuid(), eventDataWithProductsCodes(FIRST, SECOND)))
				.thenReturn(firstMessage);
		when(eventMessageFactory.createEventMessage(OPTION_BULK_UPDATE, skuOption.getGuid(), eventDataWithProductsCodes(THIRD)))
				.thenReturn(secondMessage);

		skuOptionUpdateProcessor.processSkuOptionUpdated(skuOption);

		final InOrder inOrder = inOrder(eventMessagePublisher);
		inOrder.verify(eventMessagePublisher).publish(firstMessage);
		inOrder.verify(eventMessagePublisher).publish(secondMessage);
	}

	private Map<String, Object> eventDataWithProductsCodes(final String... codes) {
		return Collections.singletonMap(PRODUCTS, Arrays.asList(codes));
	}
}