/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl.FieldMetadataToProjectionConverter;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.impl.ModifierGroupImpl;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.service.store.StoreService;

/**
 * Tests {@link com.elasticpath.catalog.update.processor.projection.service.ProjectionService}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectionServiceTest {

	private static final String CATALOG_CODE = "catalog1";
	private static final int NUMBER_OF_STORES = 3;
	private static final String CODE = "name";
	private static final String STORE = "store";

	/**
	 * Test for ensure that method buildProjections
	 * call method convert from {@link com.elasticpath.catalog.update.processor.projection.service.ProjectionService} exactly times same as stores
	 * processed.
	 */
	@Test
	public void buildProjectionShouldCallMethodConvertFromFieldMetadataToProjectionConverterExactlyTimesSameAsStoresNumber() {

		Catalog catalog = new CatalogImpl();
		catalog.setCode(CATALOG_CODE);
		ModifierGroup group = new ModifierGroupImpl();

		Store store = new StoreImpl();
		List<Store> stores = Collections.nCopies(NUMBER_OF_STORES, store);
		StoreService storeService = mock(StoreService.class);
		when(storeService.findStoresWithCatalogCode(any())).thenReturn(stores);

		FieldMetadata fieldMetadataProjection = new FieldMetadata(CODE, STORE, new ArrayList<>(), ZonedDateTime.now(), false);
		FieldMetadataToProjectionConverter converter = mock(FieldMetadataToProjectionConverter.class);
		when(converter.convert(any(), any(), any())).thenReturn(fieldMetadataProjection);

		ProjectionServiceImpl<ModifierGroup, FieldMetadata> service = new ProjectionServiceImpl<>(storeService, converter);
		service.buildProjections(group, catalog);

		verify(converter, times(NUMBER_OF_STORES)).convert(group, store, catalog);
	}

	/**
	 * Test for ensure that method buildProjections
	 * already call method findStoresWithCatalogCode from {@link StoreService}.
	 */
	@Test
	public void buildProjectionShouldCallMethodFindStoresWithCatalogCode() {

		Catalog catalog = new CatalogImpl();
		catalog.setCode(CATALOG_CODE);
		ModifierGroup group = new ModifierGroupImpl();

		Store store = new StoreImpl();
		List<Store> stores = Collections.nCopies(NUMBER_OF_STORES, store);
		StoreService storeService = mock(StoreService.class);
		when(storeService.findStoresWithCatalogCode(any())).thenReturn(stores);

		FieldMetadata fieldMetadataProjection = new FieldMetadata(CODE, STORE, new ArrayList<>(), ZonedDateTime.now(), false);
		FieldMetadataToProjectionConverter converter = mock(FieldMetadataToProjectionConverter.class);
		when(converter.convert(any(), any(), any())).thenReturn(fieldMetadataProjection);

		ProjectionServiceImpl<ModifierGroup, FieldMetadata> service = new ProjectionServiceImpl<>(storeService, converter);
		service.buildProjections(group, catalog);

		verify(storeService).findStoresWithCatalogCode(CATALOG_CODE);

	}

	/**
	 * Test for ensure that number of projections which return method buildProjections
	 * is same with stores number.
	 */
	@Test
	public void buildProjectionShouldReturnSameNumberOfProjectionsAsStores() {

		Catalog catalog = new CatalogImpl();
		catalog.setCode(CATALOG_CODE);
		ModifierGroup group = new ModifierGroupImpl();

		Store store = new StoreImpl();
		List<Store> stores = Collections.nCopies(NUMBER_OF_STORES, store);
		StoreService storeService = mock(StoreService.class);
		when(storeService.findStoresWithCatalogCode(any())).thenReturn(stores);

		FieldMetadata projection = new FieldMetadata(CODE, STORE, new ArrayList<>(), ZonedDateTime.now(), false);
		FieldMetadataToProjectionConverter converter = mock(FieldMetadataToProjectionConverter.class);
		when(converter.convert(any(), any(), any())).thenReturn(projection);

		ProjectionServiceImpl<ModifierGroup, FieldMetadata> service = new ProjectionServiceImpl<>(storeService, converter);
		List<FieldMetadata> projections = service.buildProjections(group, catalog);

		assertThat(projections).hasSize(NUMBER_OF_STORES);

	}

	/**
	 * Test for ensure that method buildAllStoresProjections
	 * call method convert from {@link com.elasticpath.catalog.update.processor.projection.service.ProjectionService} exactly times same as stores
	 * processed.
	 */
	@Test
	public void buildAllStoresProjectionsShouldCallMethodConvertFromFieldMetadataToProjectionConverterExactlyTimesSameAsStoresNumber() {
		Catalog catalog = new CatalogImpl();
		catalog.setCode(CATALOG_CODE);
		StoreService storeService = mock(StoreService.class);
		Store store = mock(Store.class);
		when(store.getCatalog()).thenReturn(catalog);
		List<Store> stores = Collections.nCopies(NUMBER_OF_STORES, store);
		List<Long> storeUids = Collections.nCopies(NUMBER_OF_STORES, 1L);
		when(storeService.findAllStoreUids()).thenReturn(storeUids);
		when(storeService.getTunedStores(any(), any())).thenReturn(stores);

		ModifierGroup group = new ModifierGroupImpl();

		FieldMetadata fieldMetadataProjection = new FieldMetadata(CODE, STORE, new ArrayList<>(), ZonedDateTime.now(), false);
		FieldMetadataToProjectionConverter converter = mock(FieldMetadataToProjectionConverter.class);
		when(converter.convert(any(), any(), any())).thenReturn(fieldMetadataProjection);

		ProjectionServiceImpl<ModifierGroup, FieldMetadata> service = new ProjectionServiceImpl<>(storeService, converter);
		service.buildAllStoresProjections(group);

		verify(converter, times(NUMBER_OF_STORES)).convert(group, store, catalog);
	}

	/**
	 * Test for ensure that method buildAllStoresProjections
	 * already call method findAllStoreUids from {@link StoreService}.
	 */
	@Test
	public void buildAllStoresProjectionsShouldCallMethodFindAllStoreUids() {
		StoreService storeService = mock(StoreService.class);
		ProjectionServiceImpl<ModifierGroup, FieldMetadata> service = new ProjectionServiceImpl<>(storeService,
				mock(FieldMetadataToProjectionConverter.class));

		service.buildAllStoresProjections(mock(ModifierGroupImpl.class));

		verify(storeService).findAllStoreUids();
	}

	/**
	 * Test for ensure that method buildAllStoresProjections
	 * already call method getTunedStores from {@link StoreService}.
	 */
	@Test
	public void buildAllStoresProjectionsShouldCallMethodGetTunedStores() {
		List<Long> storeUids = Collections.nCopies(NUMBER_OF_STORES, 1L);
		StoreService storeService = mock(StoreService.class);
		when(storeService.findAllStoreUids()).thenReturn(storeUids);
		ProjectionServiceImpl<ModifierGroup, FieldMetadata> service = new ProjectionServiceImpl<>(storeService,
				mock(FieldMetadataToProjectionConverter.class));

		service.buildAllStoresProjections(mock(ModifierGroupImpl.class));

		verify(storeService).getTunedStores(any(), any());
	}


	/**
	 * Test for ensure that number of projections which return method buildAllStoresProjections
	 * is same with stores number.
	 */
	@Test
	public void buildAllStoresProjectionsShouldReturnSameNumberOfProjectionsAsStores() {
		Catalog catalog = new CatalogImpl();
		catalog.setCode(CATALOG_CODE);
		StoreService storeService = mock(StoreService.class);
		Store store = mock(Store.class);
		when(store.getCatalog()).thenReturn(catalog);
		List<Store> stores = Collections.nCopies(NUMBER_OF_STORES, store);
		List<Long> storeUids = Collections.nCopies(NUMBER_OF_STORES, 1L);
		when(storeService.findAllStoreUids()).thenReturn(storeUids);

		when(storeService.getTunedStores(any(), any())).thenReturn(stores);

		ModifierGroup group = new ModifierGroupImpl();

		FieldMetadata fieldMetadataProjection = new FieldMetadata(CODE, STORE, new ArrayList<>(), ZonedDateTime.now(), false);
		FieldMetadataToProjectionConverter converter = mock(FieldMetadataToProjectionConverter.class);
		when(converter.convert(any(), any(), any())).thenReturn(fieldMetadataProjection);

		ProjectionServiceImpl<ModifierGroup, FieldMetadata> service = new ProjectionServiceImpl<>(storeService, converter);
		List<FieldMetadata> projections = service.buildAllStoresProjections(group);

		assertThat(projections).hasSize(NUMBER_OF_STORES);
	}
}
