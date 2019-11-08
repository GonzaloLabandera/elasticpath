/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.spi.CatalogProjectionPlugin;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;
import com.elasticpath.catalog.spi.capabilities.FieldMetadataWriterRepository;
import com.elasticpath.catalog.update.processor.capabilities.ModifierGroupUpdateProcessor;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.modifier.ModifierGroup;

/**
 * Tests {@link ModifierGroupUpdateProcessorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierGroupUpdateProcessorImplTest {

	@Mock
	private final ModifierGroup cartItemModifierGroup = mock(ModifierGroup.class);
	@Mock
	private ProjectionService<ModifierGroup, FieldMetadata> projectionService;
	@Mock
	private CatalogProjectionPluginProvider provider;
	@Mock
	private CatalogProjectionPlugin plugin;
	@Mock
	private FieldMetadataWriterRepository repository;
	private ModifierGroupUpdateProcessor cartItemModifierGroupUpdateProcessor;

	/**
	 * Setup for the database.
	 */
	@Before
	public void setUp() {
		when(cartItemModifierGroup.getGuid()).thenReturn("guid");
		when(projectionService.buildAllStoresProjections(cartItemModifierGroup)).thenReturn(Arrays.asList(mock(FieldMetadata.class),
				mock(FieldMetadata.class)));
		when(provider.getCatalogProjectionPlugin()).thenReturn(plugin);
		when(plugin.getWriterCapability(FieldMetadataWriterRepository.class)).thenReturn(Optional.of(repository));

		cartItemModifierGroupUpdateProcessor = new ModifierGroupUpdateProcessorImpl(projectionService, provider);
	}

	/**
	 * Processing of MODIFIER_GROUP_CREATED event.
	 */
	@Test
	public void processModifierGroupCreatedTest() {
		cartItemModifierGroupUpdateProcessor.processModifierGroupCreated(cartItemModifierGroup);

		verify(projectionService).buildAllStoresProjections(cartItemModifierGroup);
		verify(provider, times(1)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(FieldMetadataWriterRepository.class);
		verify(repository, times(2)).write(any());
	}

	/**
	 * Processing of MODIFIER_GROUP_UPDATED event.
	 */
	@Test
	public void processModifierGroupUpdatedTest() {
		cartItemModifierGroupUpdateProcessor.processModifierGroupUpdated(cartItemModifierGroup);

		verify(projectionService).buildAllStoresProjections(cartItemModifierGroup);
		verify(provider, times(1)).getCatalogProjectionPlugin();
		verify(plugin, times(1)).getWriterCapability(FieldMetadataWriterRepository.class);
		verify(repository, times(2)).write(any());
	}

	/**
	 * Processing of MODIFIER_GROUP_DELETED event.
	 */
	@Test
	public void processModifierGroupDeletedTest() {
		cartItemModifierGroupUpdateProcessor.processModifierGroupDeleted(cartItemModifierGroup.getGuid());

		verify(provider).getCatalogProjectionPlugin();
		verify(plugin).getWriterCapability(FieldMetadataWriterRepository.class);
		verify(repository).delete(anyString());
		verify(cartItemModifierGroup).getGuid();
	}
}