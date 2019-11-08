/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.Converter;
import com.elasticpath.catalog.update.processor.projection.service.ProjectionService;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.support.FetchGroupConstants;
import com.elasticpath.persistence.support.impl.FetchGroupLoadTunerImpl;
import com.elasticpath.service.store.StoreService;

/**
 * Implementation of {@link ProjectionService} for {@link Projection} projection {@link com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata}.
 *
 * @param <S> is source entity for projection.
 * @param <P> is projection type.
 */
public class ProjectionServiceImpl<S, P extends Projection> implements ProjectionService<S, P> {

	private final StoreService storeService;
	private final Converter<S, P> converter;

	/**
	 * FieldMetadataProjectionService constructor.
	 *
	 * @param storeService {@link StoreService}.
	 * @param converter    {@link Converter}.
	 */
	public ProjectionServiceImpl(final StoreService storeService, final Converter<S, P> converter) {
		this.storeService = storeService;
		this.converter = converter;
	}

	/**
	 * Build list of projections from source.
	 *
	 * @param source  is source entity for projection building
	 * @param catalog is catalog that this object belongs to.
	 * @return list of projections.
	 */
	@Override
	public List<P> buildProjections(final S source, final Catalog catalog) {
		final String catalogCode = catalog.getCode();
		final Collection<Store> stores = storeService.findStoresWithCatalogCode(catalogCode);

		return stores.stream()
				.map(store -> converter.convert(source, store, catalog))
				.collect(Collectors.toList());
	}

	/**
	 * Build list of projections from source.
	 *
	 * @param source is source entity for projection building
	 * @return list of projections.
	 */
	public List<P> buildAllStoresProjections(final S source) {
		final FetchGroupLoadTuner fetchGroupLoadTuner = new FetchGroupLoadTunerImpl();
		fetchGroupLoadTuner.addFetchGroup(FetchGroupConstants.STORE_SHARING);
		fetchGroupLoadTuner.addFetchGroup(FetchGroupConstants.CATALOG);
		fetchGroupLoadTuner.addFetchGroup(FetchGroupConstants.DEFAULT);

		final List<Long> uids = storeService.findAllStoreUids();
		final Collection<Store> stores = storeService.getTunedStores(uids, fetchGroupLoadTuner);

		return stores.stream()
				.map(store -> converter.convert(source, store, store.getCatalog()))
				.collect(Collectors.toList());
	}
}
