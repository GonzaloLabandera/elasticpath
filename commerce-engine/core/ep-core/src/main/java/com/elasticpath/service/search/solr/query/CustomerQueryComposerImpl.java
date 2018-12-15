/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.search.solr.query;

import static com.elasticpath.service.search.solr.SolrIndexConstants.CREATE_TIME;
import static com.elasticpath.service.search.solr.SolrIndexConstants.CUSTOMER_NUMBER;
import static com.elasticpath.service.search.solr.SolrIndexConstants.EMAIL_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.FIRST_NAME_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.LAST_NAME_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.OBJECT_UID;
import static com.elasticpath.service.search.solr.SolrIndexConstants.PHONE_NUMBER_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.PREFERRED_BILLING_ADDRESS_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.STORE_CODE;
import static com.elasticpath.service.search.solr.SolrIndexConstants.USER_ID_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.ZIP_POSTAL_CODE_EXACT;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.springframework.util.CollectionUtils;

import com.elasticpath.domain.misc.SearchConfig;
import com.elasticpath.service.search.query.CustomerSearchCriteria;
import com.elasticpath.service.search.query.EpEmptySearchCriteriaException;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.query.SortOrder;
import com.elasticpath.service.search.query.StandardSortBy;

/**
 * A query compose for customer search.
 */
public class CustomerQueryComposerImpl extends AbstractQueryComposerImpl {

	@Override
	protected boolean isValidSearchCriteria(final SearchCriteria searchCriteria) {
		return searchCriteria instanceof CustomerSearchCriteria;
	}

	@Override
	public Query composeQueryInternal(final SearchCriteria searchCriteria, final SearchConfig searchConfig) {
		setAnalyzer(new CustomerQueryAnalyzerImpl());
		final CustomerSearchCriteria criteria = (CustomerSearchCriteria) searchCriteria;
		final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		final BooleanQuery.Builder emailQueryBuilder = new BooleanQuery.Builder();
		final BooleanQuery.Builder userIdQueryBuilder = new BooleanQuery.Builder();
		boolean hasUserIdCriteria = addToQuery(searchConfig, userIdQueryBuilder, USER_ID_EXACT, criteria.getUserId(), Occur.SHOULD, true);
		boolean hasEmailCriteria = addToQuery(searchConfig, emailQueryBuilder, EMAIL_EXACT, criteria.getEmail(), Occur.SHOULD, true);
		boolean hasSomeCriteria = hasUserIdCriteria || hasEmailCriteria;
		if (hasSomeCriteria) {
			if (criteria.isUserIdAndEmailMutualSearch()) {
				BooleanQuery.Builder innerQueryBuilder = new BooleanQuery.Builder();
				if (hasEmailCriteria) {
					innerQueryBuilder.add(emailQueryBuilder.build(), Occur.SHOULD);
				}
				if (hasUserIdCriteria) {
					innerQueryBuilder.add(userIdQueryBuilder.build(), Occur.SHOULD);
				}
				booleanQueryBuilder.add(innerQueryBuilder.build(), Occur.MUST);
			} else {
				if (hasEmailCriteria) {
					booleanQueryBuilder.add(emailQueryBuilder.build(), Occur.MUST);
				}
				if (hasUserIdCriteria) {
					booleanQueryBuilder.add(userIdQueryBuilder.build(), Occur.MUST);
				}
			}
		}
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, FIRST_NAME_EXACT, criteria.getFirstName(), Occur.MUST, true);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, LAST_NAME_EXACT, criteria.getLastName(), Occur.MUST, true);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, ZIP_POSTAL_CODE_EXACT, criteria.getZipOrPostalCode(), Occur.MUST, true);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, CUSTOMER_NUMBER, criteria.getCustomerNumber(), Occur.MUST, false);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, PHONE_NUMBER_EXACT, criteria.getPhoneNumber(), Occur.MUST, true);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, STORE_CODE, criteria.getStoreCodes(), Occur.MUST);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, OBJECT_UID, criteria.getFilteredUids(), Occur.MUST_NOT);
		hasSomeCriteria |= addTermForCreateDate(criteria, booleanQueryBuilder, searchConfig);
		if (!hasSomeCriteria) {
			throw new EpEmptySearchCriteriaException("Empty search criteria is not allowed!");
		}
		return booleanQueryBuilder.build();
	}

	private boolean addToQuery(final SearchConfig conf, final BooleanQuery.Builder queryBuilder, final String field,
							   final String value, final Occur occur, final boolean wildcards) {
		if (StringUtils.isBlank(field) || StringUtils.isBlank(value) || queryBuilder == null || conf == null) {
			return false;
		}
		return addWholeFieldToQuery(field, wildcards ? QueryComposerHelper.addWildcards(value) : value, null, conf, queryBuilder, occur, false);
	}

	private boolean addToQuery(final SearchConfig conf, final BooleanQuery.Builder queryBuilder, final String field,
							   final Collection<?> value, final Occur occur) {
		if (StringUtils.isBlank(field) || CollectionUtils.isEmpty(value) || queryBuilder == null || conf == null) {
			return false;
		}
		return addWholeFieldToQuery(field, value, null, conf, queryBuilder, occur, false);
	}

	@Override
	public Query composeFuzzyQueryInternal(final SearchCriteria searchCriteria, final SearchConfig searchConfig) {
		return composeQueryInternal(searchCriteria, searchConfig);
	}

	private boolean addTermForCreateDate(final CustomerSearchCriteria criteria, final BooleanQuery.Builder queryBuilder,
										 final SearchConfig config) {
		if (criteria == null || queryBuilder == null || config == null) {
			return false;
		}
		boolean hasCriteria = false;
		if (criteria.getFromDate() != null) {
			hasCriteria = true;
			final String fromDateAnalyzed = getAnalyzer().analyze(criteria.getFromDate());
			final Query fromDateRangeQuery = new BoostQuery(TermRangeQuery.newStringRange(CREATE_TIME,
					fromDateAnalyzed, null, true, true), config.getBoostValue(CREATE_TIME));
			queryBuilder.add(fromDateRangeQuery, Occur.MUST);
		}
		return hasCriteria;
	}

	@Override
	protected void fillSortFieldMap(final Map<String, SortOrder> sortFieldMap, final SearchCriteria searchCriteria, final SearchConfig
			searchConfig) {
		final String sortField = resolveSortField(searchCriteria);
		if (StringUtils.isNotEmpty(sortField)) {
			sortFieldMap.put(sortField, searchCriteria.getSortingOrder());
		}
	}

	private String resolveSortField(final SearchCriteria searchCriteria) {
		switch (searchCriteria.getSortingType().getOrdinal()) {
			case StandardSortBy.CUSTOMER_ID_ORDINAL:
				return CUSTOMER_NUMBER;
			case StandardSortBy.ADDRESS_ORDINAL:
				return PREFERRED_BILLING_ADDRESS_EXACT;
			case StandardSortBy.EMAIL_ORDINAL:
				return EMAIL_EXACT;
			case StandardSortBy.FIRST_NAME_ORDINAL:
				return FIRST_NAME_EXACT;
			case StandardSortBy.LAST_NAME_ORDINAL:
				return LAST_NAME_EXACT;
			case StandardSortBy.PHONE_ORDINAL:
				return PHONE_NUMBER_EXACT;
			case StandardSortBy.USER_ID_ORDINAL:
				return USER_ID_EXACT;
			case StandardSortBy.STORE_CODE_ORDINAL:
				return STORE_CODE;
			default:
				return null;
		}
	}

	/**
	 * @return SolrIndexConstants.CUSTOMER_NUMBER
	 */
	@Override
	protected String getBusinessCodeField() {
		return CUSTOMER_NUMBER;
	}

}
