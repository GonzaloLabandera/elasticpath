/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.price.impl;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.searches.FacetEntity;
import com.elasticpath.rest.definition.searches.FacetIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Repository that implements retrieving facet entity for a facet identifier
 *
 * @param <E> extends FacetEntity
 * @param <I> extends FacetIdentifier
 */
@Component
public class FacetEntityRepositoryImpl<E extends FacetEntity, I extends FacetIdentifier>
		implements Repository<FacetEntity, FacetIdentifier> {

	private SearchRepository searchRepository;

	@Override
	public Single<FacetEntity> findOne(final FacetIdentifier identifier) {
		final String facetGuid = identifier.getFacetId().getValue();
		return searchRepository.getDisplayNameByGuid(facetGuid).map(displayName -> FacetEntity.builder()
				.withDisplayName(displayName)
				.build());
	}

	@Reference
	public void setSearchRepository(final SearchRepository searchRepository) {
		this.searchRepository = searchRepository;
	}

}
