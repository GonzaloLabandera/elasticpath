/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.search.solr;

import java.util.Date;
import java.util.Map;

import org.apache.lucene.search.BooleanQuery;
import org.apache.solr.client.solrj.SolrQuery;

import com.elasticpath.domain.catalogview.Filter;
import com.elasticpath.domain.misc.SearchConfig;
import com.elasticpath.service.search.SpellSuggestionSearchCriteria;
import com.elasticpath.service.search.index.QueryComposer;
import com.elasticpath.service.search.query.KeywordSearchCriteria;
import com.elasticpath.service.search.query.SearchCriteria;

/**
 * Factory interface.
 */
public interface SolrQueryFactory {

	/**
	 * Composes a specific query.
	 *
	 * @param luceneQueryComposer the Lucene query compose to use
	 * @param searchCriteria the search criteria
	 * @param startIndex the initial index to display results for
	 * @param maxResults the maximum results to return from given start index (maximum per page)
	 * @param searchConfig the search configuration to use
	 * @param fuzzyQuery whether the composed query should be fuzzy
	 * @param filterLookup a map keyed on solr query filter with a value containing the filter
	 * @return a {@link SolrQuery} specific query
	 */
	SolrQuery composeSpecificQuery(QueryComposer luceneQueryComposer, SearchCriteria searchCriteria,
								   int startIndex, int maxResults, SearchConfig searchConfig, boolean fuzzyQuery, Map<String, Filter<?>> filterLookup);

	/**
	 * Composes a keyword search query.
	 *
	 * @param keywordSearchCriteria the search criteria
	 * @param startIndex the initial index to display results for
	 * @param maxResults the maximum results to return from given start index (maximum per page)
	 * @param searchConfig the search configuration to use
	 * @param fuzzyQuery whether the composed query should be fuzzy
	 * @param filterLookup a map keyed on solr query filter with a value containing the filter
	 * @return a {@link SolrQuery} keyword query
	 */
	SolrQuery composeKeywordQuery(KeywordSearchCriteria keywordSearchCriteria, int startIndex, int maxResults, SearchConfig searchConfig,
								  boolean fuzzyQuery, Map<String, Filter<?>> filterLookup);

	/**
	 * Gets the searchable attributes from filter attributes.
	 * @param searchCriteria the search criteria.
	 * @param searchConfig the search config.
	 * @return the searchable attribute string.
	 */
	String getSearchableAttributesFromFilterAttributes(KeywordSearchCriteria searchCriteria, SearchConfig searchConfig);

	/**
	 * Gets the searchable attributes.
	 * @param searchConfig the search config.
	 * @param searchCriteria the search criteria.
	 * @return the searchable attribute.
	 */
	String getSearchableAttributes(SearchConfig searchConfig, KeywordSearchCriteria searchCriteria);

	/**
	 * Composes a query for spell suggestions.
	 *
	 * @param searchCriteria the search criteria
	 * @param config the search config to use for this suggestion
	 * @return a {@link SolrQuery} spell suggestion query
	 */
	SolrQuery composeSpellingQuery(SpellSuggestionSearchCriteria searchCriteria, SearchConfig config);

	/**
	 * Generates a SOLR date range query to only selected active products, thus having the current date fall
	 * between its start and end dates. Products may or may not have an end date.
	 *
	 * @param date The date the start and end date of the product must fall in.
	 * @return the query for the start and date range based on the given date
	 */
	BooleanQuery createTermsForStartEndDateRange(Date date);

	/**
	 * Returns the solr filter query string of a filter.
	 * @param filter filter
	 * @param searchCriteria search criteria
	 * @return solr fitler query string
	 */
	String constructSolrQuery(Filter<?> filter, SearchCriteria searchCriteria);
}