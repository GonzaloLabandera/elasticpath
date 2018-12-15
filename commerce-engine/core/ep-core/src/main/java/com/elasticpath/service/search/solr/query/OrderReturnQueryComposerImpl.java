/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.search.solr.query;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import com.elasticpath.domain.misc.SearchConfig;
import com.elasticpath.service.search.query.EpEmptySearchCriteriaException;
import com.elasticpath.service.search.query.OrderReturnSearchCriteria;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.solr.SolrIndexConstants;

/**
 * A query compose for order returns search.
 * 
 * @deprecated the order return search should use the ReturnAndExchangeService to query the database directly.
 */
@Deprecated
public class OrderReturnQueryComposerImpl extends AbstractQueryComposerImpl {
	
	@Override
	protected boolean isValidSearchCriteria(final SearchCriteria searchCriteria) {
		return searchCriteria instanceof OrderReturnSearchCriteria;
	}

	@Override
	public Query composeQueryInternal(final SearchCriteria searchCriteria, final SearchConfig searchConfig) {
		final OrderReturnSearchCriteria orderReturnSearchCriteria = (OrderReturnSearchCriteria) searchCriteria;

		final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		boolean hasSomeCriteria = false;

		if (orderReturnSearchCriteria.getCustomerSearchCriteria() != null
				&& composeQueryForCustomerSearchCriteria(orderReturnSearchCriteria, booleanQueryBuilder, searchConfig)) {
			hasSomeCriteria = true;
		}

		hasSomeCriteria |= composeQueryForOtherSearchCriteria(orderReturnSearchCriteria, booleanQueryBuilder, searchConfig);

		if (!hasSomeCriteria) {
			throw new EpEmptySearchCriteriaException("Empty search criteria is not allowed!");
		}

		return booleanQueryBuilder.build();
	}

	@Override
	public Query composeFuzzyQueryInternal(final SearchCriteria searchCriteria, final SearchConfig searchConfig) {
		final OrderReturnSearchCriteria orderReturnSearchCriteria = (OrderReturnSearchCriteria) searchCriteria;

		final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
		boolean hasSomeCriteria = false;

		if (orderReturnSearchCriteria.getCustomerSearchCriteria() != null
				&& composeFuzzyQueryForCustomerSearchCriteria(orderReturnSearchCriteria, booleanQueryBuilder, searchConfig)) {
			hasSomeCriteria = true;
		}

		hasSomeCriteria |= composeQueryForOtherSearchCriteria(orderReturnSearchCriteria, booleanQueryBuilder, searchConfig);

		if (!hasSomeCriteria) {
			throw new EpEmptySearchCriteriaException("Empty search criteria is not allowed!");
		}

		return booleanQueryBuilder.build();
	}

	private boolean composeQueryForOtherSearchCriteria(final OrderReturnSearchCriteria orderReturnSearchCriteria,
			final BooleanQuery.Builder booleanQueryBuilder, final SearchConfig searchConfig) {
		boolean hasSomeCriteria = false;

		hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.ORDER_NUMBER, orderReturnSearchCriteria.getOrderNumber(),
				null, searchConfig, booleanQueryBuilder, Occur.MUST, true);
		hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.RMA_CODE, orderReturnSearchCriteria.getRmaCode(), null,
				searchConfig, booleanQueryBuilder, Occur.MUST, true);
		hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.OBJECT_UID, orderReturnSearchCriteria.getFilteredUids(), null,
				searchConfig, booleanQueryBuilder, Occur.MUST_NOT, false);
		hasSomeCriteria |= addWholeFieldToQuery(SolrIndexConstants.WAREHOUSES_CODE, orderReturnSearchCriteria.getWarehouseCodes(), null,
				searchConfig, booleanQueryBuilder, Occur.MUST, true);

		return hasSomeCriteria;
	}

	private boolean composeQueryForCustomerSearchCriteria(final OrderReturnSearchCriteria orderReturnSearchCriteria,
			final BooleanQuery.Builder booleanQueryBuilder, final SearchConfig searchConfig) {
		boolean hasSomeCriteria = false;

		hasSomeCriteria |= addSplitFieldToQuery(SolrIndexConstants.FIRST_NAME, orderReturnSearchCriteria
				.getCustomerSearchCriteria().getFirstName(), null, searchConfig, booleanQueryBuilder, Occur.MUST, true);
		hasSomeCriteria |= addSplitFieldToQuery(SolrIndexConstants.LAST_NAME, orderReturnSearchCriteria
				.getCustomerSearchCriteria().getLastName(), null, searchConfig, booleanQueryBuilder, Occur.MUST, true);

		return hasSomeCriteria;
	}

	private boolean composeFuzzyQueryForCustomerSearchCriteria(final OrderReturnSearchCriteria orderReturnSearchCriteria,
			final BooleanQuery.Builder booleanQueryBuilder, final SearchConfig searchConfig) {
		boolean hasSomeCriteria = false;

		hasSomeCriteria |= addSplitFuzzyFieldToQuery(SolrIndexConstants.FIRST_NAME, orderReturnSearchCriteria
				.getCustomerSearchCriteria().getFirstName(), null, searchConfig, booleanQueryBuilder, Occur.MUST, true);
		hasSomeCriteria |= addSplitFuzzyFieldToQuery(SolrIndexConstants.LAST_NAME, orderReturnSearchCriteria
				.getCustomerSearchCriteria().getLastName(), null, searchConfig, booleanQueryBuilder, Occur.MUST, true);

		return hasSomeCriteria;
	}

}
