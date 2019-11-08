/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OfferSearchUtil.buildFacetIdentifier;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.offersearches.FacetIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetsIdentifier;
import com.elasticpath.rest.definition.offersearches.SearchOfferEntity;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Repository for getting all facet fields.
 * @param <I> facets identifier
 * @param <LI> facet identifier
 */
@Component
public class FacetRepositoryImpl<I extends FacetsIdentifier, LI extends FacetIdentifier>
		implements LinksRepository<FacetsIdentifier, FacetIdentifier> {

	private SearchRepository searchRepository;

	private ResourceOperationContext resourceOperationContext;

	@Override
	public Observable<FacetIdentifier> getElements(final FacetsIdentifier identifier) {

		String pageSizeString = identifier.getSearchId().getValue()
				.get(PaginationEntity.PAGE_SIZE_PROPERTY);

		if ("null".equals(pageSizeString) || StringUtils.isBlank(pageSizeString)) {
			pageSizeString = "0";
		}

		if (!NumberUtils.isDigits(pageSizeString)) {
			return Observable.error(ResourceOperationFailure.badRequestBody("Invalid page size"));
		}

		int pageSize = Integer.parseInt(pageSizeString);

		Map<String, String> searchId = identifier.getSearchId().getValue();

		Subject subject = resourceOperationContext.getSubject();
		Locale locale = SubjectUtil.getLocale(subject);
		Currency currency = SubjectUtil.getCurrency(subject);

		String keyword = searchId.get(SearchOfferEntity.KEYWORDS_PROPERTY);
		String categoryCode = searchId.get(OffersResourceConstants.CATEGORY_CODE_PROPERTY);
		String scope = identifier.getScope().getValue();
		Map<String, String> appliedFacets = identifier.getAppliedFacets().getValue();

		OfferSearchData offerSearchData = new OfferSearchData(1, 1, scope, appliedFacets, keyword);
		offerSearchData.setCategoryCode(categoryCode);

		return searchRepository.getSearchCriteria(offerSearchData, locale, currency)
				.flatMapObservable(searchCriteria -> searchRepository.getFacetFields(searchCriteria, 1,
						pageSize))
				.map(facetGuid -> buildFacetIdentifier(identifier, facetGuid));
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
