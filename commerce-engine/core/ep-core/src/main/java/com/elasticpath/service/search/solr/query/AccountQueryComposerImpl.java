/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.search.solr.query;

import static com.elasticpath.service.search.solr.SolrIndexConstants.AP_FAX_NUMBER_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.AP_PHONE_NUMBER_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.AP_TAX_EXEMPTION_ID_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.BUSINESS_NAME_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.BUSINESS_NUMBER_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.CUSTOMER_NUMBER;
import static com.elasticpath.service.search.solr.SolrIndexConstants.CUSTOMER_TYPE;
import static com.elasticpath.service.search.solr.SolrIndexConstants.PREFERRED_BILLING_ADDRESS_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.ROOT_LEVEL;
import static com.elasticpath.service.search.solr.SolrIndexConstants.SHARED_ID_EXACT;
import static com.elasticpath.service.search.solr.SolrIndexConstants.ZIP_POSTAL_CODE_EXACT;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.misc.SearchConfig;
import com.elasticpath.service.search.query.AccountSearchCriteria;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.query.SortOrder;
import com.elasticpath.service.search.query.StandardSortBy;

/**
 * A query compose for customer account search.
 */
public class AccountQueryComposerImpl extends CustomerQueryComposerImpl {
	@Override
	protected boolean isValidSearchCriteria(final SearchCriteria searchCriteria) {
		return searchCriteria instanceof AccountSearchCriteria;
	}

	@Override
	public Query composeQueryInternal(final SearchCriteria searchCriteria, final SearchConfig searchConfig) {
		setAnalyzer(new CustomerQueryAnalyzerImpl());
		final AccountSearchCriteria criteria = (AccountSearchCriteria) searchCriteria;
		final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		addToQuery(searchConfig, booleanQueryBuilder, SHARED_ID_EXACT, criteria.getSharedId(), Occur.MUST, false);
		addToQuery(searchConfig, booleanQueryBuilder, BUSINESS_NAME_EXACT, criteria.getBusinessName(), Occur.MUST, true);
		addToQuery(searchConfig, booleanQueryBuilder, BUSINESS_NUMBER_EXACT, criteria.getBusinessNumber(), Occur.MUST, true);
		addToQuery(searchConfig, booleanQueryBuilder, AP_PHONE_NUMBER_EXACT, criteria.getPhoneNumber(), Occur.MUST, true);
		addToQuery(searchConfig, booleanQueryBuilder, AP_FAX_NUMBER_EXACT, criteria.getFaxNumber(), Occur.MUST, true);
		addToQuery(searchConfig, booleanQueryBuilder, ZIP_POSTAL_CODE_EXACT, criteria.getZipOrPostalCode(), Occur.MUST, true);
		addToQuery(searchConfig, booleanQueryBuilder, AP_TAX_EXEMPTION_ID_EXACT, criteria.getTaxExemptionId(), Occur.MUST, true);
		addToQuery(searchConfig, booleanQueryBuilder, ROOT_LEVEL, calculateRootLevel(criteria), Occur.MUST, false);
		addToQuery(searchConfig, booleanQueryBuilder, CUSTOMER_TYPE, CustomerType.ACCOUNT.getName(), Occur.MUST, false);

		return booleanQueryBuilder.build();
	}

	@Override
	public Query composeFuzzyQueryInternal(final SearchCriteria searchCriteria, final SearchConfig searchConfig) {
		return composeQueryInternal(searchCriteria, searchConfig);
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
	 * Returns field to be used as second sorting field.
	 *
	 * @return SolrIndexConstants.CUSTOMER_NUMBER
	 */
	@Override
	protected String getBusinessCodeField() {
		return CUSTOMER_NUMBER;
	}

	/**
	 * Resolves sort fields based on account profile attributes.
	 *
	 * @param searchCriteria the given search criteria.
	 * @return the sort field index constant.
	 */
	@Override
	protected String resolveSortField(final SearchCriteria searchCriteria) {
		switch (searchCriteria.getSortingType().getOrdinal()) {
			case StandardSortBy.SHARED_ID_ORDINAL:
				return SHARED_ID_EXACT;
			case StandardSortBy.BUSINESS_NAME_ORDINAL:
				return BUSINESS_NAME_EXACT;
			case StandardSortBy.BUSINESS_NUMBER_ORDINAL:
				return BUSINESS_NUMBER_EXACT;
			case StandardSortBy.ADDRESS_ORDINAL:
				return PREFERRED_BILLING_ADDRESS_EXACT;
			default:
				return null;
		}
	}

	private String calculateRootLevel(final AccountSearchCriteria criteria) {
		return criteria.isSearchRootAccountsOnly() ? Boolean.TRUE.toString() : null;
	}
}
