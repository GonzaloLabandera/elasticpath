/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.caching.core.faceting;

import java.util.Date;
import java.util.Map;

import org.apache.lucene.search.BooleanQuery;
import org.apache.solr.client.solrj.SolrQuery;

import com.elasticpath.cache.Cache;
import com.elasticpath.domain.catalogview.Filter;
import com.elasticpath.domain.misc.SearchConfig;
import com.elasticpath.service.search.SpellSuggestionSearchCriteria;
import com.elasticpath.service.search.index.QueryComposer;
import com.elasticpath.service.search.query.KeywordSearchCriteria;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.solr.SolrQueryFactory;

/**
 * Caching version of the SolrQueryFactory.
 */
public class CachingSolrQueryFactoryImpl implements SolrQueryFactory {

	private Cache<String, String> searchableAttributeCache;
	private SolrQueryFactory fallbackQueryFactory;

	@Override
	public SolrQuery composeSpecificQuery(
			final QueryComposer luceneQueryComposer,
			final SearchCriteria searchCriteria,
			final int startIndex,
			final int maxResults,
			final SearchConfig searchConfig,
			final boolean fuzzyQuery,
			final Map<String, Filter<?>> filterLookup) {
		return fallbackQueryFactory.composeSpecificQuery(luceneQueryComposer,
				searchCriteria, startIndex, maxResults, searchConfig, fuzzyQuery, filterLookup);
	}

	@Override
	public SolrQuery composeKeywordQuery(final KeywordSearchCriteria keywordSearchCriteria,
										 final int startIndex,
										 final int maxResults,
										 final SearchConfig searchConfig,
										 final boolean fuzzyQuery,
										 final Map<String, Filter<?>> filterLookup) {
		return fallbackQueryFactory.composeKeywordQuery(keywordSearchCriteria, startIndex, maxResults, searchConfig, fuzzyQuery, filterLookup);
	}

	@Override
	public String getSearchableAttributesFromFilterAttributes(final KeywordSearchCriteria searchCriteria, final SearchConfig searchConfig) {
		String result = searchableAttributeCache.get(searchCriteria.getStoreCode());
		if  (result != null) {
			return result;
		}
		result = fallbackQueryFactory.getSearchableAttributesFromFilterAttributes(searchCriteria,
				searchConfig);
		searchableAttributeCache.put(searchCriteria.getStoreCode(), result);
		return result;
	}

	@Override
	public String getSearchableAttributes(final SearchConfig searchConfig, final KeywordSearchCriteria searchCriteria) {
		String storeCode = searchCriteria.getStoreCode();
		String result = this.searchableAttributeCache.get(storeCode);

		if (result == null) {
			result = fallbackQueryFactory.getSearchableAttributes(searchConfig, searchCriteria);
			searchableAttributeCache.put(storeCode, result);
		}

		return result;
	}

	@Override
	public SolrQuery composeSpellingQuery(final SpellSuggestionSearchCriteria searchCriteria, final SearchConfig config) {
		return fallbackQueryFactory.composeSpellingQuery(searchCriteria, config);
	}

	@Override
	public BooleanQuery createTermsForStartEndDateRange(final Date date) {
		return fallbackQueryFactory.createTermsForStartEndDateRange(date);
	}

	@Override
	public String constructSolrQuery(final Filter<?> filter, final SearchCriteria searchCriteria) {
		return fallbackQueryFactory.constructSolrQuery(filter, searchCriteria);
	}

	public void setSearchableAttributeCache(final Cache<String, String> searchableAttributeCache) {
		this.searchableAttributeCache = searchableAttributeCache;
	}

	public void setFallbackQueryFactory(final SolrQueryFactory fallbackQueryFactory) {
		this.fallbackQueryFactory = fallbackQueryFactory;
	}
}
