/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.offersearches.OfferSearchFormIdentifier;
import com.elasticpath.rest.definition.offersearches.SearchOfferEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Repository for search offer entity.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class SearchOfferEntitySearchFormRepositoryImpl<E extends SearchOfferEntity, I extends OfferSearchFormIdentifier>
		implements Repository<SearchOfferEntity, OfferSearchFormIdentifier> {


	private SearchRepository searchRepository;

	@Reference
	public void setSearchRepository(final SearchRepository searchRepository) {
		this.searchRepository = searchRepository;
	}

	@Override
	public Single<SearchOfferEntity> findOne(final OfferSearchFormIdentifier identifier) {
		return searchRepository.getDefaultPageSize(identifier.getScope().getValue())
				.map(pageSize -> SearchOfferEntity.builder()
						.withKeywords(StringUtils.EMPTY)
						.withPageSize(pageSize)
						.build());
	}
}
