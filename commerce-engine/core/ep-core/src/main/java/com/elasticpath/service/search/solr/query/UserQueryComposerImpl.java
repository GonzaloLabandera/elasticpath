/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.search.solr.query;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import com.elasticpath.domain.misc.SearchConfig;
import com.elasticpath.service.search.query.EpEmptySearchCriteriaException;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.query.SortOrder;
import com.elasticpath.service.search.query.StandardSortBy;
import com.elasticpath.service.search.query.UserSearchCriteria;
import com.elasticpath.service.search.solr.SolrIndexConstants;

/**
 * A query compose for users search.
 */
public class UserQueryComposerImpl extends AbstractQueryComposerImpl {

	@Override
	protected boolean isValidSearchCriteria(final SearchCriteria searchCriteria) {
		return searchCriteria instanceof UserSearchCriteria;
	}

	@Override
	public Query composeQueryInternal(final SearchCriteria searchCriteria, final SearchConfig searchConfig) {
		final UserSearchCriteria userSearchCriteria = (UserSearchCriteria) searchCriteria;
		final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		boolean hasSomeCriteria = false;

		hasSomeCriteria |= addSplitFieldToQuery(SolrIndexConstants.FIRST_NAME, userSearchCriteria.getFirstName(), null, searchConfig,
				booleanQueryBuilder, Occur.MUST, true);
		hasSomeCriteria |= addSplitFieldToQuery(SolrIndexConstants.LAST_NAME, userSearchCriteria.getLastName(), null, searchConfig,
				booleanQueryBuilder, Occur.MUST, true);
		hasSomeCriteria |= addSplitFieldToQuery(SolrIndexConstants.USER_NAME, userSearchCriteria.getUserName(), null, searchConfig,
				booleanQueryBuilder, Occur.MUST, true);

		hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.EMAIL, userSearchCriteria.getEmail(), null, searchConfig, booleanQueryBuilder,
				Occur.MUST, true);

		if (userSearchCriteria.getStoreCode() != null) {
			final BooleanQuery.Builder innerQueryBuilder = new BooleanQuery.Builder();
			hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.ALL_STORES_ACCESS, String.valueOf(true), null, searchConfig, innerQueryBuilder,
					Occur.SHOULD, true);

			hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.STORE_CODE, userSearchCriteria.getStoreCode(), null, searchConfig,
					innerQueryBuilder, Occur.SHOULD, true);
			booleanQueryBuilder.add(innerQueryBuilder.build(), Occur.MUST);
		}

		if (userSearchCriteria.getCatalogCode() != null) {
			final BooleanQuery.Builder innerQueryBuilder = new BooleanQuery.Builder();
			hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.ALL_CATALOGS_ACCESS, String.valueOf(true), null, searchConfig,
					innerQueryBuilder, Occur.SHOULD, true);
			hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.CATALOG_CODE, userSearchCriteria.getCatalogCode(), null, searchConfig,
					innerQueryBuilder, Occur.SHOULD, true);
			booleanQueryBuilder.add(innerQueryBuilder.build(), Occur.MUST);
		}

		if (userSearchCriteria.getUserStatus() != null) {
			hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.STATUS, userSearchCriteria.getUserStatus().getPropertyKey(), null,
					searchConfig, booleanQueryBuilder, Occur.MUST, true);
		}

		if (userSearchCriteria.getUserRoleName() != null) {
			hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.USER_ROLE, userSearchCriteria.getUserRoleName(), null, searchConfig,
					booleanQueryBuilder, Occur.MUST, true);
		}

		if (!hasSomeCriteria) {
			throw new EpEmptySearchCriteriaException("Empty search criteria is not allowed!");
		}

		return booleanQueryBuilder.build();
	}

	@Override
	public Query composeFuzzyQueryInternal(final SearchCriteria searchCriteria, final SearchConfig searchConfig) {
		final UserSearchCriteria userSearchCriteria = (UserSearchCriteria) searchCriteria;
		final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		boolean hasSomeCriteria = false;

		hasSomeCriteria |= addSplitFuzzyFieldToQuery(SolrIndexConstants.FIRST_NAME, userSearchCriteria.getFirstName(), null, searchConfig,
				booleanQueryBuilder, Occur.MUST, true);
		hasSomeCriteria |= addSplitFuzzyFieldToQuery(SolrIndexConstants.LAST_NAME, userSearchCriteria.getLastName(), null, searchConfig,
				booleanQueryBuilder, Occur.MUST, true);
		hasSomeCriteria |= addSplitFuzzyFieldToQuery(SolrIndexConstants.USER_NAME, userSearchCriteria.getUserName(), null, searchConfig,
				booleanQueryBuilder, Occur.MUST, true);

		hasSomeCriteria |= addWholeFuzzyFieldToQuery(SolrIndexConstants.EMAIL, userSearchCriteria.getEmail(), null, searchConfig, booleanQueryBuilder,
				Occur.MUST, true);


		if (userSearchCriteria.getStoreCode() != null) {
			final BooleanQuery.Builder innerQueryBuilder = new BooleanQuery.Builder();
			hasSomeCriteria |= addWholeFuzzyFieldToQuery(SolrIndexConstants.ALL_STORES_ACCESS, String.valueOf(true), null, searchConfig,
					innerQueryBuilder, Occur.SHOULD, true);
			hasSomeCriteria |= addWholeFuzzyFieldToQuery(SolrIndexConstants.STORE_CODE, userSearchCriteria.getStoreCode(), null, searchConfig,
					innerQueryBuilder, Occur.SHOULD, true);
			booleanQueryBuilder.add(innerQueryBuilder.build(), Occur.MUST);
		}


		if (userSearchCriteria.getCatalogCode() != null) {
			final BooleanQuery.Builder innerQueryBuilder = new BooleanQuery.Builder();
			hasSomeCriteria |= addWholeFuzzyFieldToQuery(SolrIndexConstants.ALL_CATALOGS_ACCESS, String.valueOf(true), null, searchConfig,
					innerQueryBuilder, Occur.SHOULD, true);
			hasSomeCriteria |= addWholeFuzzyFieldToQuery(SolrIndexConstants.CATALOG_CODE, userSearchCriteria.getCatalogCode(), null, searchConfig,
					innerQueryBuilder, Occur.SHOULD, true);
			booleanQueryBuilder.add(innerQueryBuilder.build(), Occur.MUST);
		}

		if (userSearchCriteria.getUserStatus() != null) {
			hasSomeCriteria |= addWholeFuzzyFieldToQuery(SolrIndexConstants.STATUS, userSearchCriteria.getUserStatus().getPropertyKey(), null,
					searchConfig, booleanQueryBuilder, Occur.MUST, true);
		}

		if (userSearchCriteria.getUserRoleName() != null) {
			hasSomeCriteria |= addWholeFuzzyFieldToQuery(SolrIndexConstants.USER_ROLE, userSearchCriteria.getUserRoleName(), null,
					searchConfig, booleanQueryBuilder, Occur.MUST, true);
		}

		if (!hasSomeCriteria) {
			throw new EpEmptySearchCriteriaException("Empty search criteria is not allowed!");
		}

		return booleanQueryBuilder.build();
	}
	
	@Override
	protected void fillSortFieldMap(final Map<String, SortOrder> sortFieldMap, final SearchCriteria searchCriteria, final SearchConfig searchConfig) {
		final String sortField = resolveSortField(searchCriteria);
		if (StringUtils.isNotEmpty(sortField)) {
			sortFieldMap.put(sortField, searchCriteria.getSortingOrder());
		}
	}
	
	private String resolveSortField(final SearchCriteria searchCriteria) {
		switch (searchCriteria.getSortingType().getOrdinal()) {
		case StandardSortBy.EMAIL_ORDINAL:
			return SolrIndexConstants.EMAIL_EXACT;
		case StandardSortBy.STATUS_ORDINAL:
			return SolrIndexConstants.STATUS;
		case StandardSortBy.NAME_ORDINAL:
			return SolrIndexConstants.USER_NAME;
		case StandardSortBy.LAST_NAME_ORDINAL:
			return SolrIndexConstants.LAST_NAME_EXACT;
		case StandardSortBy.FIRST_NAME_ORDINAL:
			return SolrIndexConstants.FIRST_NAME_EXACT;
		default:
			return null;
		}
	}
	
	/**
	 * @return SolrIndexConstants.USER_NAME
	 */
	@Override
	protected String getBusinessCodeField() {
		return SolrIndexConstants.USER_NAME;
	}

}
