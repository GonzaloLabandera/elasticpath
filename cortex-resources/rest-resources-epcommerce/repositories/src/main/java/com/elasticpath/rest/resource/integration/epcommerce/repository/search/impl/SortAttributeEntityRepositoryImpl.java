/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.offersearches.SortAttributeEntity;
import com.elasticpath.rest.definition.offersearches.SortAttributeIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Repository to read a sort attribute entity.
 * @param <E> extends SortAttributeEntity
 * @param <I> extends SortAttributeIdentifier
 */
@Component
public class SortAttributeEntityRepositoryImpl<E extends SortAttributeEntity, I extends SortAttributeIdentifier>
		implements Repository<SortAttributeEntity, SortAttributeIdentifier> {

	private SearchRepository searchRepository;

	private ResourceOperationContext resourceOperationContext;

	@Override
	public Single<SortAttributeEntity> findOne(final SortAttributeIdentifier identifier) {
		String localeCode = SubjectUtil.getLocale(resourceOperationContext.getSubject()).getLanguage();
		String guid = identifier.getSortAttributeSelectorChoice().getSortAttributeId().getValue();
		return searchRepository.getSortValueByGuidAndLocaleCode(guid, localeCode)
				.map(sortValue -> SortAttributeEntity.builder()
						.withDisplayName(sortValue.getName())
						.build());
	}

	@Reference
	public void setSearchRepository(final SearchRepository searchRepository) {
		this.searchRepository = searchRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}
}
