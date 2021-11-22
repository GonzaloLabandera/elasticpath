/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.search.solr.query;

import static com.elasticpath.service.search.solr.SolrIndexConstants.CREATE_TIME;
import static com.elasticpath.service.search.solr.SolrIndexConstants.CUSTOMER_NUMBER;
import static com.elasticpath.service.search.solr.SolrIndexConstants.CUSTOMER_TYPE;
import static com.elasticpath.service.search.solr.SolrIndexConstants.EMAIL_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.FIRST_NAME_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.LAST_NAME_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.OBJECT_UID;
import static com.elasticpath.service.search.solr.SolrIndexConstants.PHONE_NUMBER_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.PREFERRED_BILLING_ADDRESS_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.SHARED_ID_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.STORE_CODE;
import static com.elasticpath.service.search.solr.SolrIndexConstants.USERNAME_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.ZIP_POSTAL_CODE_EXACT;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.springframework.util.CollectionUtils;

import com.elasticpath.domain.customer.CustomerType;
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
		boolean hasSomeCriteria = false;

		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, SHARED_ID_EXACT, criteria.getSharedId(), Occur.MUST, false);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, EMAIL_EXACT, criteria.getEmail(), Occur.MUST, true);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, USERNAME_EXACT, criteria.getUsername(), Occur.MUST, true);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, FIRST_NAME_EXACT, criteria.getFirstName(), Occur.MUST, true);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, LAST_NAME_EXACT, criteria.getLastName(), Occur.MUST, true);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, ZIP_POSTAL_CODE_EXACT, criteria.getZipOrPostalCode(), Occur.MUST, true);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, CUSTOMER_NUMBER, criteria.getCustomerNumber(), Occur.MUST, false);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, PHONE_NUMBER_EXACT, criteria.getPhoneNumber(), Occur.MUST, true);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, STORE_CODE, criteria.getStoreCodes(), Occur.MUST);
		hasSomeCriteria |= addToQuery(searchConfig, booleanQueryBuilder, OBJECT_UID, criteria.getFilteredUids(), Occur.MUST_NOT);
		hasSomeCriteria |= addTermForCreateDate(criteria, booleanQueryBuilder, searchConfig);
		addCustomerType(hasSomeCriteria, searchConfig, booleanQueryBuilder);
		if (!hasSomeCriteria) {
			throw new EpEmptySearchCriteriaException("Empty search criteria is not allowed!");
		}
		return booleanQueryBuilder.build();
	}

	private void addCustomerType(final boolean hasSomeCriteria, final SearchConfig searchConfig, final BooleanQuery.Builder booleanQueryBuilder) {
		if (hasSomeCriteria) {
			final BooleanQuery.Builder singleSessionQueryBuilder = new BooleanQuery.Builder();
			addToQuery(searchConfig, singleSessionQueryBuilder, CUSTOMER_TYPE, CustomerType.SINGLE_SESSION_USER.getName(), Occur.SHOULD, false);

			final BooleanQuery.Builder registeredQueryBuilder = new BooleanQuery.Builder();
			addToQuery(searchConfig, registeredQueryBuilder, CUSTOMER_TYPE, CustomerType.REGISTERED_USER.getName(), Occur.SHOULD, false);

			final BooleanQuery.Builder typeBuilder = new BooleanQuery.Builder();
			typeBuilder.add(singleSessionQueryBuilder.build(), Occur.SHOULD);
			typeBuilder.add(registeredQueryBuilder.build(), Occur.SHOULD);

			booleanQueryBuilder.add(typeBuilder.build(), Occur.MUST);
		}
	}

	/**
	 * Attempts to add a term query to the given boolean query with the given field and value. The
	 * whole field is added to the query. Returns a value to indicate whether the addition was
	 * successful or a non-fatal error occurred.
	 *
	 * @param conf         the search configuration to use.
	 * @param queryBuilder the query to add a term query to.
	 * @param field        the field to add a term for.
	 * @param value        the actual value to search for.
	 * @param occur        the occurrence value of the added query.
	 * @param wildcards    whether to add the wildcards to the value.
	 * @return whether the addition was successful or a non-fatal error occurred.
	 */
	protected boolean addToQuery(final SearchConfig conf, final BooleanQuery.Builder queryBuilder, final String field,
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

	/**
	 * Resolves sort fields based on user profile attributes.
	 *
	 * @param searchCriteria the given search criteria.
	 * @return the sort field index constant.
	 */
	protected String resolveSortField(final SearchCriteria searchCriteria) {
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
			case StandardSortBy.SHARED_ID_ORDINAL:
				return SHARED_ID_EXACT;
			case StandardSortBy.USERNAME_ORDINAL:
				return USERNAME_EXACT;
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
